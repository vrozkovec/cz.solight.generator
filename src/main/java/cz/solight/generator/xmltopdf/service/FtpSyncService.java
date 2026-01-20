/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.solight.generator.xmltopdf.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Service for synchronizing XML files via SFTP. Downloads XML files from a remote directory,
 * processes them using a callback pattern that supports generating multiple output files per XML,
 * and uploads the generated files to a different remote directory.
 */
public class FtpSyncService
{
	private static final Logger LOG = LoggerFactory.getLogger(FtpSyncService.class);
	private static final String XML_EXTENSION = ".xml";
	private static final int SFTP_TIMEOUT_MS = 30_000;

	/**
	 * Construct.
	 */
	public FtpSyncService()
	{
		super();
	}

	/**
	 * Synchronizes XML files from SFTP server. Downloads all .xml files from the configured
	 * download directory, processes them using the provided processor (which may generate multiple
	 * output files per XML), and uploads all generated files to the configured upload directory.
	 * Source XML files are left in place on the SFTP server.
	 *
	 * @param config
	 *            SFTP connection configuration
	 * @param fileProcessor
	 *            processor that receives the XML file and a callback to register output files. The
	 *            callback should be called for each generated output file that needs to be
	 *            uploaded.
	 */
	public void syncXmlFiles(SftpConfig config, BiConsumer<File, Consumer<File>> fileProcessor)
	{
		config.validate();
		LOG.info("Starting SFTP sync with config: {}", config);

		Path tempDir = null;
		Session session = null;
		ChannelSftp sftpChannel = null;

		try
		{
			// Load credentials
			var credentials = loadCredentials(Path.of(config.getCredentialsFile()));

			// Create temp directory for downloads
			tempDir = Files.createTempDirectory("sftp-sync-");
			LOG.debug("Created temp directory: {}", tempDir);

			// Connect to SFTP
			session = connect(config, credentials);
			sftpChannel = openSftpChannel(session);

			// Download XML files
			var xmlFiles = downloadXmlFiles(sftpChannel, config.getDownloadDirectory(), tempDir);
			LOG.info("Downloaded {} XML files", xmlFiles.size());

			// Process each XML file
			for (File xmlFile : xmlFiles)
			{
				processXmlFile(xmlFile, fileProcessor, sftpChannel, config.getUploadDirectory());
			}

			LOG.info("SFTP sync completed successfully");
		}
		catch (Exception e)
		{
			LOG.error("SFTP sync failed", e);
			throw new RuntimeException("SFTP sync failed: " + e.getMessage(), e);
		}
		finally
		{
			// Clean up
			disconnect(session, sftpChannel);
			cleanupTempDirectory(tempDir);
		}
	}

	/**
	 * Loads credentials from the specified file. The file format is:
	 * <ul>
	 * <li>Line 1: username</li>
	 * <li>Line 2: password</li>
	 * </ul>
	 *
	 * @param credentialsPath
	 *            path to the credentials file
	 * @return the loaded credentials
	 * @throws IOException
	 *             if the file cannot be read or has invalid format
	 */
	SftpCredentials loadCredentials(Path credentialsPath) throws IOException
	{
		LOG.debug("Loading credentials from: {}", credentialsPath);

		var lines = Files.readAllLines(credentialsPath);
		if (lines.size() < 2)
		{
			throw new IOException("Credentials file must contain at least 2 lines (username and password)");
		}

		var username = lines.get(0).trim();
		var password = lines.get(1).trim();

		if (username.isEmpty() || password.isEmpty())
		{
			throw new IOException("Username and password cannot be empty");
		}

		LOG.debug("Loaded credentials for user: {}", username);
		return new SftpCredentials(username, password);
	}

	/**
	 * Establishes SFTP connection to the server.
	 *
	 * @param config
	 *            SFTP configuration
	 * @param credentials
	 *            authentication credentials
	 * @return the connected session
	 * @throws JSchException
	 *             if connection fails
	 */
	Session connect(SftpConfig config, SftpCredentials credentials) throws JSchException
	{
		LOG.info("Connecting to SFTP server: {}:{}", config.getHost(), config.getPort());

		var jsch = new JSch();
		var session = jsch.getSession(credentials.username(), config.getHost(), config.getPort());
		session.setPassword(credentials.password());

		// Disable strict host key checking for simplicity
		// In production, consider using known_hosts file
		session.setConfig("StrictHostKeyChecking", "no");
		session.setTimeout(SFTP_TIMEOUT_MS);

		session.connect();
		LOG.info("Connected to SFTP server successfully");

		return session;
	}

	/**
	 * Opens an SFTP channel on the given session.
	 *
	 * @param session
	 *            the connected session
	 * @return the opened SFTP channel
	 * @throws JSchException
	 *             if channel cannot be opened
	 */
	private ChannelSftp openSftpChannel(Session session) throws JSchException
	{
		if (session.openChannel("sftp") instanceof ChannelSftp channel)
		{
			channel.connect(SFTP_TIMEOUT_MS);
			LOG.debug("SFTP channel opened");
			return channel;
		}
		throw new JSchException("Failed to open SFTP channel");
	}

	/**
	 * Downloads all XML files from the remote directory to a local temp directory.
	 *
	 * @param sftpChannel
	 *            the SFTP channel
	 * @param remoteDirectory
	 *            the remote directory to download from
	 * @param localDirectory
	 *            the local directory to download to
	 * @return list of downloaded XML files
	 * @throws SftpException
	 *             if SFTP operation fails
	 */
	List<File> downloadXmlFiles(ChannelSftp sftpChannel, String remoteDirectory, Path localDirectory) throws SftpException
	{
		LOG.info("Downloading XML files from: {}", remoteDirectory);

		var downloadedFiles = new ArrayList<File>();

		var entries = sftpChannel.ls(remoteDirectory);

		for (var entry : entries)
		{
			var filename = entry.getFilename();
			if (filename.toLowerCase().endsWith(XML_EXTENSION) && !entry.getAttrs().isDir())
			{
				var remotePath = remoteDirectory + "/" + filename;
				var localFile = localDirectory.resolve(filename).toFile();

				LOG.debug("Downloading: {} -> {}", remotePath, localFile);
				sftpChannel.get(remotePath, localFile.getAbsolutePath());
				downloadedFiles.add(localFile);
			}
		}

		return downloadedFiles;
	}

	/**
	 * Processes a single XML file and uploads generated output files.
	 *
	 * @param xmlFile
	 *            the XML file to process
	 * @param fileProcessor
	 *            the processor callback
	 * @param sftpChannel
	 *            the SFTP channel for uploads
	 * @param uploadDirectory
	 *            the remote upload directory
	 */
	private void processXmlFile(File xmlFile, BiConsumer<File, Consumer<File>> fileProcessor, ChannelSftp sftpChannel,
		String uploadDirectory)
	{
		LOG.info("Processing XML file: {}", xmlFile.getName());

		var outputFiles = new ArrayList<File>();

		try
		{
			// Call processor with callback that collects output files
			fileProcessor.accept(xmlFile, pdf -> {
				uploadFile(sftpChannel, pdf, uploadDirectory);
				outputFiles.add(pdf);
				try
				{
					LOG.info("Deleting PDF file after upload: {}", pdf);
					Files.delete(pdf.toPath());
				}
				catch (IOException e)
				{
					e.printStackTrace();
					pdf.deleteOnExit();
				}
			});

			LOG.info("Processed {} and uploaded {} output files", xmlFile.getName(), outputFiles.size());
		}
		catch (Exception e)
		{
			LOG.error("Failed to process XML file: {}", xmlFile.getName(), e);
			// Continue with remaining files instead of aborting entire sync
		}
	}

	/**
	 * Uploads a file to the remote directory.
	 *
	 * @param sftpChannel
	 *            the SFTP channel
	 * @param file
	 *            the local file to upload
	 * @param remoteDirectory
	 *            the remote directory to upload to
	 */
	void uploadFile(ChannelSftp sftpChannel, File file, String remoteDirectory)
	{
		var remotePath = remoteDirectory + "/" + file.getName();
		LOG.debug("Uploading: {} -> {}", file.getAbsolutePath(), remotePath);

		try (var inputStream = new FileInputStream(file))
		{
			sftpChannel.put(inputStream, remotePath);
			LOG.info("Uploaded: {}", remotePath);
		}
		catch (SftpException | IOException e)
		{
			LOG.error("Failed to upload file: {}", file.getName(), e);
			throw new RuntimeException("Failed to upload file: " + file.getName() + " - " + e.getMessage(), e);
		}
	}

	/**
	 * Disconnects SFTP channel and session safely.
	 *
	 * @param session
	 *            the session to disconnect (may be null)
	 * @param sftpChannel
	 *            the channel to disconnect (may be null)
	 */
	void disconnect(Session session, ChannelSftp sftpChannel)
	{
		if (sftpChannel != null && sftpChannel.isConnected())
		{
			try
			{
				sftpChannel.disconnect();
				LOG.debug("SFTP channel disconnected");
			}
			catch (Exception e)
			{
				LOG.warn("Error disconnecting SFTP channel", e);
			}
		}

		if (session != null && session.isConnected())
		{
			try
			{
				session.disconnect();
				LOG.info("SFTP session disconnected");
			}
			catch (Exception e)
			{
				LOG.warn("Error disconnecting SFTP session", e);
			}
		}
	}

	/**
	 * Cleans up the temporary directory.
	 *
	 * @param tempDir
	 *            the temp directory to clean up (may be null)
	 */
	private void cleanupTempDirectory(Path tempDir)
	{
		if (tempDir != null)
		{
			try
			{
				FileUtils.deleteDirectory(tempDir.toFile());
				LOG.debug("Cleaned up temp directory: {}", tempDir);
			}
			catch (IOException e)
			{
				LOG.warn("Failed to clean up temp directory: {}", tempDir, e);
			}
		}
	}
}
