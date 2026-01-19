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

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for converting UNC image paths to HTTP URLs. Converts paths like
 * "\\webserver\storecards\1D31A.jpg" to HTTP URLs.
 *
 * <p>
 * If the image is not found at the original URL, the converter will try uppercase and lowercase
 * filename variants.
 */
public class ImagePathConverter
{
	private static final Logger LOG = LoggerFactory.getLogger(ImagePathConverter.class);
	private static final String DEFAULT_UNC_PREFIX = "\\\\webserver\\storecards\\";
	private static final String DEFAULT_HTTP_BASE_URL = "https://generator.solight.cz/obrazky/";
	private static final int CONNECTION_TIMEOUT_MS = 3000;

	private final String uncPrefix;
	private final String httpBaseUrl;

	/**
	 * Creates a converter with default Solight settings.
	 */
	public ImagePathConverter()
	{
		this(DEFAULT_UNC_PREFIX, DEFAULT_HTTP_BASE_URL);
	}

	/**
	 * Creates a converter with custom paths.
	 *
	 * @param uncPrefix
	 *            the UNC path prefix to strip
	 * @param httpBaseUrl
	 *            the HTTP base URL to prepend
	 */
	public ImagePathConverter(String uncPrefix, String httpBaseUrl)
	{
		this.uncPrefix = uncPrefix;
		this.httpBaseUrl = httpBaseUrl;
	}

	/**
	 * Converts a UNC path to an HTTP URL, trying case variants if the original is not found.
	 *
	 * <p>
	 * Example: "\\webserver\storecards\1D31A.jpg" -&gt;
	 * "https://generator.solight.cz/obrazky/1D31A.jpg"
	 *
	 * <p>
	 * If the image is not found, tries uppercase (1D31A.JPG) and lowercase (1d31a.jpg) variants.
	 *
	 * @param uncPath
	 *            the UNC path from the XML
	 * @return the HTTP URL for the image (with working case variant if found)
	 */
	public String convertToUrl(String uncPath)
	{
		if (StringUtils.isBlank(uncPath))
		{
			return "";
		}

		// Extract filename from UNC path
		var filename = extractFilename(uncPath);
		if (StringUtils.isBlank(filename))
		{
			return "";
		}

		// Try original filename first
		var originalUrl = httpBaseUrl + filename;
		if (imageExists(originalUrl))
		{
			return originalUrl;
		}

		// Try uppercase filename
		var uppercaseFilename = filename.toUpperCase(Locale.ROOT);
		if (!uppercaseFilename.equals(filename))
		{
			var uppercaseUrl = httpBaseUrl + uppercaseFilename;
			if (imageExists(uppercaseUrl))
			{
				LOG.debug("Image found with uppercase filename: {}", uppercaseFilename);
				return uppercaseUrl;
			}
		}

		// Try lowercase filename
		var lowercaseFilename = filename.toLowerCase(Locale.ROOT);
		if (!lowercaseFilename.equals(filename))
		{
			var lowercaseUrl = httpBaseUrl + lowercaseFilename;
			if (imageExists(lowercaseUrl))
			{
				LOG.debug("Image found with lowercase filename: {}", lowercaseFilename);
				return lowercaseUrl;
			}
		}

		// Return original URL even if not found (Playwright will handle missing images)
		LOG.warn("Image not found at any case variant: {}", filename);
		return originalUrl;
	}

	/**
	 * Checks if an image exists at the given URL using a HEAD request.
	 *
	 * @param url
	 *            the URL to check
	 * @return true if the image exists (HTTP 200), false otherwise
	 */
	private boolean imageExists(String url)
	{
		try
		{
			var connection = (HttpURLConnection)URI.create(url).toURL().openConnection();
			connection.setRequestMethod("HEAD");
			connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
			connection.setReadTimeout(CONNECTION_TIMEOUT_MS);

			int responseCode = connection.getResponseCode();
			connection.disconnect();

			return responseCode == HttpURLConnection.HTTP_OK;
		}
		catch (Exception e)
		{
			LOG.debug("Error checking image existence at {}: {}", url, e.getMessage());
			return false;
		}
	}

	/**
	 * Extracts the filename from a UNC path.
	 *
	 * @param uncPath
	 *            the UNC path
	 * @return the filename portion
	 */
	private String extractFilename(String uncPath)
	{
		// Try to strip the known prefix
		if (uncPath.startsWith(uncPrefix))
		{
			return uncPath.substring(uncPrefix.length());
		}

		// Fallback: extract filename from last backslash
		int lastBackslash = uncPath.lastIndexOf('\\');
		if (lastBackslash >= 0)
		{
			return uncPath.substring(lastBackslash + 1);
		}

		// No backslash found, return as-is
		return uncPath;
	}
}
