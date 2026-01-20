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

import static name.berries.wicket.util.app.AppConfigProvider.getDefaultConfiguration;

import org.apache.commons.lang3.StringUtils;

/**
 * Configuration for SFTP connection parameters. Holds host, port, directories for download/upload,
 * and path to credentials file.
 */
public class SftpConfig
{
	private static final int DEFAULT_PORT = getDefaultConfiguration().getInt("sftp.port");
	private static final String DEFAULT_HOST = getDefaultConfiguration().getString("sftp.host");
	private static final String DEFAULT_CREDENTIALS_FILE = getDefaultConfiguration().getString("sftp.credentialsFile");
	private static final String DEFAULT_DOWNLOAD_DIRECTORY = getDefaultConfiguration().getString("sftp.downloadDirectory");
	private static final String DEFAULT_UPLOAD_DIRECTORY = getDefaultConfiguration().getString("sftp.uploadDirectory");

	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;
	private String downloadDirectory = DEFAULT_DOWNLOAD_DIRECTORY;
	private String uploadDirectory = DEFAULT_UPLOAD_DIRECTORY;
	private String credentialsFile = DEFAULT_CREDENTIALS_FILE;

	/**
	 * Construct.
	 */
	public SftpConfig()
	{
		super();
	}

	/**
	 * @return the SFTP server hostname
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @param host
	 *            the SFTP server hostname
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * @return the SFTP port (default 22)
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port
	 *            the SFTP port
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @return the remote directory to download XML files from
	 */
	public String getDownloadDirectory()
	{
		return downloadDirectory;
	}

	/**
	 * @param downloadDirectory
	 *            the remote directory to download XML files from
	 */
	public void setDownloadDirectory(String downloadDirectory)
	{
		this.downloadDirectory = downloadDirectory;
	}

	/**
	 * @return the remote directory to upload processed files to
	 */
	public String getUploadDirectory()
	{
		return uploadDirectory;
	}

	/**
	 * @param uploadDirectory
	 *            the remote directory to upload processed files to
	 */
	public void setUploadDirectory(String uploadDirectory)
	{
		this.uploadDirectory = uploadDirectory;
	}

	/**
	 * @return the path to the credentials file (default: /data/private/app-secrets/ftp.solight.cz)
	 */
	public String getCredentialsFile()
	{
		return credentialsFile;
	}

	/**
	 * @param credentialsFile
	 *            the path to the credentials file
	 */
	public void setCredentialsFile(String credentialsFile)
	{
		this.credentialsFile = credentialsFile;
	}

	/**
	 * Validates that all required configuration fields are set.
	 *
	 * @throws IllegalStateException
	 *             if required fields are missing
	 */
	public void validate()
	{
		if (StringUtils.isBlank(host))
		{
			throw new IllegalStateException("SFTP host is required");
		}
		if (StringUtils.isBlank(downloadDirectory))
		{
			throw new IllegalStateException("SFTP download directory is required");
		}
		if (StringUtils.isBlank(uploadDirectory))
		{
			throw new IllegalStateException("SFTP upload directory is required");
		}
		if (StringUtils.isBlank(credentialsFile))
		{
			throw new IllegalStateException("SFTP credentials file path is required");
		}
	}

	@Override
	public String toString()
	{
		return "SftpConfig[host=" + host + ", port=" + port + ", downloadDirectory=" + downloadDirectory + ", uploadDirectory="
			+ uploadDirectory + "]";
	}
}
