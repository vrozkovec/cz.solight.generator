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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.berries.wicket.util.app.AppConfigProvider;
import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;

/**
 * Utility for converting UNC image paths to HTTP URLs. Converts paths like
 * "\\webserver\storecards\1D31A.jpg" to HTTP URLs.
 *
 * <p>
 * Uses a server-side file finder endpoint to handle case-insensitive image lookups.
 */
public class ImagePathConverter
{
	private static final Logger LOG = LoggerFactory.getLogger(ImagePathConverter.class);
	private static final String DEFAULT_UNC_PREFIX = "\\\\webserver\\storecards\\";
	private static final String DEFAULT_HTTP_BASE_URL = AppConfigProvider.getDefaultConfiguration()
		.getString(ConfigKey.APP_BASE_IMAGES_URL) + "/obrazky/";
	private static final String FILE_FINDER_URL = AppConfigProvider.getDefaultConfiguration().getString("files.finderUrl");
	private static final int CONNECTION_TIMEOUT_MS = 3000;

	private final String uncPrefix;
	private final String httpBaseUrl;
	private final Map<String, String> cache = new HashMap<>();

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
	 * Converts a UNC path to an HTTP URL using a server-side file finder endpoint.
	 *
	 * <p>
	 * Example: "\\webserver\storecards\1D31A.jpg" -&gt;
	 * "https://generator.solight.cz/obrazky/1D31A.jpg"
	 *
	 * <p>
	 * Uses the fileFinder.php endpoint to handle case-insensitive lookups on the server side.
	 *
	 * @param productCode
	 *            code of the product
	 * @param uncPath
	 *            the UNC path from the XML
	 * @return the HTTP URL for the image, or null if not found
	 */
	public String convertToUrl(String productCode, String uncPath)
	{
		if (StringUtils.isBlank(uncPath))
		{
			return null;
		}

		// Extract filename from UNC path
		var filename = extractFilename(uncPath);
		if (StringUtils.isBlank(filename))
		{
			return null;
		}

		// Check cache first
		var cacheKey = buildCacheKey(productCode, filename);
		var cachedUrl = cache.get(cacheKey);
		if (cachedUrl != null)
		{
			LOG.debug("Cache hit for {}", cacheKey);
			return cachedUrl;
		}

		// Use server-side file finder for case-insensitive lookup
		var foundUrl = findImageUrl(filename);
		if (foundUrl != null)
		{
			LOG.debug("Image found via fileFinder: {}", foundUrl);
			cache.put(cacheKey, foundUrl);
			return foundUrl;
		}

		LOG.warn("Image not found: {}", filename);
		return null;
	}

	/**
	 * Finds an image URL using the server-side file finder endpoint.
	 *
	 * <p>
	 * Makes a GET request to the fileFinder.php endpoint which performs case-insensitive file
	 * lookup on the server side.
	 *
	 * @param filename
	 *            the image filename to find
	 * @return the full image URL if found, or null if not found or on error
	 */
	private String findImageUrl(String filename)
	{
		try
		{
			var url = FILE_FINDER_URL + URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
			var connection = (HttpURLConnection)URI.create(url).toURL().openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
			connection.setReadTimeout(CONNECTION_TIMEOUT_MS);

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK)
			{
				try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
				{
					var responseBody = reader.readLine();
					connection.disconnect();
					return responseBody;
				}
			}

			connection.disconnect();
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LOG.debug("Error finding image via fileFinder for {}: {}", filename, e.getMessage());
			return null;
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

	/**
	 * Builds a cache key from product code and filename.
	 *
	 * @param productCode
	 *            the product code
	 * @param filename
	 *            the image filename
	 * @return the cache key
	 */
	private String buildCacheKey(String productCode, String filename)
	{
		return productCode + "|" + filename;
	}
}
