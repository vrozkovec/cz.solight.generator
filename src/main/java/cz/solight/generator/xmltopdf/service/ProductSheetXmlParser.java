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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cz.solight.generator.xmltopdf.pojo.ProductSheet;
import cz.solight.generator.xmltopdf.service.OfferXmlParser.XmlParseException;

/**
 * Service for parsing produktove_listy.xml catalog files into {@link ProductSheet} objects. Handles
 * UTF-16 BOM encoding and HTML entity decoding in Description fields.
 */
public class ProductSheetXmlParser
{
	private static final Logger LOG = LoggerFactory.getLogger(ProductSheetXmlParser.class);

	/**
	 * Parses an XML input stream into a list of ProductSheet objects.
	 *
	 * @param inputStream
	 *            the XML input stream (produktove_listy.xml format)
	 * @return list of parsed product sheets
	 * @throws XmlParseException
	 *             if parsing fails
	 */
	public List<ProductSheet> parse(InputStream inputStream) throws XmlParseException
	{
		try
		{
			// Buffer the stream to allow mark/reset for encoding detection
			var bufferedStream = new BufferedInputStream(inputStream);

			// Detect encoding by checking first bytes
			var charset = detectEncoding(bufferedStream);
			LOG.info("Using charset: {}", charset);

			// Wrap in BOMInputStream to skip BOM if present, explicitly include all common BOMs
			var bomInputStream = BOMInputStream.builder()
				.setInputStream(bufferedStream)
				.setByteOrderMarks(ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
					ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE)
				.setInclude(false) // Skip the BOM
				.get();

			// Create InputSource with proper encoding
			var reader = new InputStreamReader(bomInputStream, charset);
			var inputSource = new InputSource(reader);
			inputSource.setEncoding(charset.name());

			var factory = DocumentBuilderFactory.newInstance();
			var builder = factory.newDocumentBuilder();
			var document = builder.parse(inputSource);
			document.getDocumentElement().normalize();

			return parseDocument(document);
		}
		catch (Exception e)
		{
			LOG.error("Failed to parse product sheet XML", e);
			throw new XmlParseException("Failed to parse product sheet XML: " + e.getMessage(), e);
		}
	}

	/**
	 * Detects the encoding of the input stream by examining the first bytes. Supports UTF-8,
	 * UTF-16LE, UTF-16BE detection via BOM, and heuristic UTF-16 detection.
	 *
	 * @param stream
	 *            a buffered input stream (must support mark/reset)
	 * @return the detected charset
	 * @throws Exception
	 *             if reading fails
	 */
	private Charset detectEncoding(BufferedInputStream stream) throws Exception
	{
		stream.mark(4);
		byte[] bom = new byte[4];
		int read = stream.read(bom);
		stream.reset();

		// Log first bytes for debugging
		if (read >= 4)
		{
			LOG.info("First 4 bytes: {} {} {} {}",
				String.format("%02X", bom[0] & 0xFF),
				String.format("%02X", bom[1] & 0xFF),
				String.format("%02X", bom[2] & 0xFF),
				String.format("%02X", bom[3] & 0xFF));
		}

		if (read >= 2)
		{
			// UTF-16 LE BOM: FF FE
			if ((bom[0] & 0xFF) == 0xFF && (bom[1] & 0xFF) == 0xFE)
			{
				LOG.info("Detected UTF-16LE BOM");
				return StandardCharsets.UTF_16LE;
			}
			// UTF-16 BE BOM: FE FF
			if ((bom[0] & 0xFF) == 0xFE && (bom[1] & 0xFF) == 0xFF)
			{
				LOG.info("Detected UTF-16BE BOM");
				return StandardCharsets.UTF_16BE;
			}
			// Heuristic: If second byte is 0x00 and first is ASCII, likely UTF-16LE without BOM
			// (e.g., '<' 0x00 for "<?" in UTF-16LE)
			if ((bom[1] & 0xFF) == 0x00 && (bom[0] & 0xFF) != 0x00)
			{
				LOG.info("Heuristic: Detected UTF-16LE (no BOM, null byte pattern)");
				return StandardCharsets.UTF_16LE;
			}
			// Heuristic: If first byte is 0x00 and second is ASCII, likely UTF-16BE without BOM
			if ((bom[0] & 0xFF) == 0x00 && (bom[1] & 0xFF) != 0x00)
			{
				LOG.info("Heuristic: Detected UTF-16BE (no BOM, null byte pattern)");
				return StandardCharsets.UTF_16BE;
			}
		}
		if (read >= 3)
		{
			// UTF-8 BOM: EF BB BF
			if ((bom[0] & 0xFF) == 0xEF && (bom[1] & 0xFF) == 0xBB && (bom[2] & 0xFF) == 0xBF)
			{
				LOG.info("Detected UTF-8 BOM");
				return StandardCharsets.UTF_8;
			}
		}

		// No BOM detected - default to UTF-8
		LOG.info("No BOM detected, defaulting to UTF-8");
		return StandardCharsets.UTF_8;
	}

	/**
	 * Parses the DOM document into a list of ProductSheet objects.
	 *
	 * @param document
	 *            the parsed XML document
	 * @return list of product sheets
	 */
	private List<ProductSheet> parseDocument(Document document)
	{
		List<ProductSheet> products = new ArrayList<>();

		NodeList productNodes = document.getElementsByTagName("PRODUCT");
		LOG.info("Found {} PRODUCT elements in XML", productNodes.getLength());

		for (int i = 0; i < productNodes.getLength(); i++)
		{
			if (productNodes.item(i) instanceof Element productElement)
			{
				var product = parseProduct(productElement);
				if (product.isValid())
				{
					products.add(product);
				}
				else
				{
					LOG.info("Skipping invalid product at index {}: code='{}', name='{}' (missing required fields)",
						i, product.getCode(), product.getName());
				}
			}
		}

		LOG.info("Successfully parsed {} valid products", products.size());
		return products;
	}

	/**
	 * Parses a single PRODUCT element into a ProductSheet object.
	 *
	 * @param productElement
	 *            the PRODUCT XML element
	 * @return the parsed product sheet
	 */
	private ProductSheet parseProduct(Element productElement)
	{
		var product = new ProductSheet();

		product.setCode(getElementText(productElement, "code"));
		product.setName(getElementText(productElement, "name"));
		product.setEan(getElementText(productElement, "EAN"));
		product.setPackageCount(parseInteger(getElementText(productElement, "Package")));
		product.setGuaranteeLength(parseInteger(getElementText(productElement, "GuaranteeLength")));
		product.setBrandName(getElementText(productElement, "Brand-Name"));
		product.setBrand(getElementText(productElement, "brand"));
		product.setProductId(getElementText(productElement, "PRODUCT_ID"));

		// Decode HTML entities in description
		var rawDescription = getElementText(productElement, "Description");
		product.setDescription(decodeHtmlEntities(rawDescription));

		// Picture filenames (will be converted to URLs by the POJO)
		product.setPicture1(getElementText(productElement, "PICTURE1"));
		product.setPicture2(getElementText(productElement, "PICTURE2"));
		product.setPicture3(getElementText(productElement, "PICTURE3"));

		return product;
	}

	/**
	 * Gets the text content of a child element.
	 *
	 * @param parent
	 *            the parent element
	 * @param tagName
	 *            the child element tag name
	 * @return the text content, or empty string if not found
	 */
	private String getElementText(Element parent, String tagName)
	{
		NodeList elements = parent.getElementsByTagName(tagName);
		if (elements.getLength() > 0)
		{
			return elements.item(0).getTextContent().trim();
		}
		return "";
	}

	/**
	 * Parses an integer from string, returning 0 if parsing fails.
	 *
	 * @param text
	 *            the text to parse
	 * @return the parsed integer, or 0 if blank or invalid
	 */
	private int parseInteger(String text)
	{
		if (StringUtils.isBlank(text))
		{
			return 0;
		}
		try
		{
			return Integer.parseInt(text.trim());
		}
		catch (NumberFormatException e)
		{
			LOG.debug("Failed to parse integer: {}", text);
			return 0;
		}
	}

	/**
	 * Decodes HTML entities in the description text. The XML contains HTML-encoded content like
	 * &amp;lt;UL&amp;gt; which needs to be decoded to &lt;UL&gt;.
	 *
	 * @param text
	 *            the text with HTML entities
	 * @return the decoded HTML text
	 */
	private String decodeHtmlEntities(String text)
	{
		if (StringUtils.isBlank(text))
		{
			return "";
		}
		return StringEscapeUtils.unescapeHtml4(text);
	}
}
