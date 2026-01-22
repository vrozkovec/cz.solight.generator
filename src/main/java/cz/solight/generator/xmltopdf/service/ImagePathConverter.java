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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * If the image is not found at the original URL, the converter will try all possible letter case
 * combinations until the image is found.
 */
public class ImagePathConverter
{
	private static final Logger LOG = LoggerFactory.getLogger(ImagePathConverter.class);
	private static final String DEFAULT_UNC_PREFIX = "\\\\webserver\\storecards\\";
	private static final String DEFAULT_HTTP_BASE_URL = AppConfigProvider.getDefaultConfiguration()
		.getString(ConfigKey.APP_BASE_IMAGES_URL) + "/obrazky/";
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
	 * Converts a UNC path to an HTTP URL, trying all letter case combinations if the original is
	 * not found.
	 *
	 * <p>
	 * Example: "\\webserver\storecards\1D31A.jpg" -&gt;
	 * "https://generator.solight.cz/obrazky/1D31A.jpg"
	 *
	 * <p>
	 * If the image is not found, tries all possible letter case combinations (e.g., for "Ab.jpg":
	 * "Ab.jpg", "AB.jpg", "ab.jpg", "aB.jpg", "Ab.JPG", etc.) until a working URL is found.
	 *
	 * @param productCode
	 *            code of the product
	 * @param uncPath
	 *            the UNC path from the XML
	 * @param picturePath
	 * @return the HTTP URL for the image (with working case variant if found)
	 */
	public String convertToUrl(String productCode, String uncPath)
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

		// Check cache first
		var cacheKey = buildCacheKey(productCode, filename);
		var cachedUrl = cache.get(cacheKey);
		if (cachedUrl != null)
		{
			LOG.debug("Cache hit for {}", cacheKey);
			return cachedUrl;
		}

		// Try original filename first
		var originalUrl = httpBaseUrl + filename;
		if (imageExists(originalUrl))
		{
			cache.put(cacheKey, originalUrl);
			return originalUrl;
		}

		// Generate and try all case combinations
		var caseCombinations = generateCaseCombinations(filename);
		for (var variant : caseCombinations)
		{
			if (variant.equals(filename))
			{
				continue; // Already tried original
			}
			var variantUrl = httpBaseUrl + variant;
			if (imageExists(variantUrl))
			{
				LOG.debug("Image found with case variant: {}", variant);
				cache.put(cacheKey, variantUrl);
				return variantUrl;
			}
		}

		// Return original URL even if not found (Playwright will handle missing images)
		LOG.warn("Image not found at any case variant: {} (tried {} combinations)", filename, caseCombinations.size());
		return originalUrl;
	}

	/**
	 * Generates all possible letter case combinations of the given string.
	 *
	 * <p>
	 * For each letter character, both uppercase and lowercase variants are included. Non-letter
	 * characters remain unchanged.
	 *
	 * @param input
	 *            the input string
	 * @return list of all case combinations
	 */
	private List<String> generateCaseCombinations(String input)
	{
		List<String> results = new ArrayList<>();
		generateCaseCombinationsRecursive(input.toCharArray(), 0, results);
		return results;
	}

	/**
	 * Recursively generates all case combinations by branching at each letter character.
	 *
	 * @param chars
	 *            the character array being modified
	 * @param index
	 *            current position in the array
	 * @param results
	 *            accumulator for generated combinations
	 */
	private void generateCaseCombinationsRecursive(char[] chars, int index, List<String> results)
	{
		if (index == chars.length)
		{
			results.add(new String(chars));
			return;
		}

		char c = chars[index];
		if (Character.isLetter(c))
		{
			// Try lowercase variant
			chars[index] = Character.toLowerCase(c);
			generateCaseCombinationsRecursive(chars, index + 1, results);

			// Try uppercase variant
			chars[index] = Character.toUpperCase(c);
			generateCaseCombinationsRecursive(chars, index + 1, results);
		}
		else
		{
			// Non-letter character, just continue
			generateCaseCombinationsRecursive(chars, index + 1, results);
		}
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
