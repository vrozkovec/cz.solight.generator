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

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.solight.generator.xmltopdf.pojo.FirmInfo;
import cz.solight.generator.xmltopdf.pojo.IssuedOffer;
import cz.solight.generator.xmltopdf.pojo.ProductPrice;
import cz.solight.generator.xmltopdf.pojo.ProductRow;

import jakarta.inject.Inject;

/**
 * Service for parsing XML offer files into {@link IssuedOffer} objects. Handles Windows-1250
 * encoding and HTML entity decoding.
 */
public class OfferXmlParser
{
	private static final Logger LOG = LoggerFactory.getLogger(OfferXmlParser.class);
	private static final DateTimeFormatter XML_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Inject
	private ImagePathConverter imagePathConverter;

	/**
	 * Construct.
	 */
	public OfferXmlParser()
	{
		super();
	}

	/**
	 * Parses an XML input stream into an IssuedOffer object.
	 *
	 * @param inputStream
	 *            the XML input stream
	 * @return the parsed offer
	 * @throws XmlParseException
	 *             if parsing fails
	 */
	public IssuedOffer parse(InputStream inputStream) throws XmlParseException
	{
		try
		{
			var factory = DocumentBuilderFactory.newInstance();
			var builder = factory.newDocumentBuilder();
			var document = builder.parse(inputStream);
			document.getDocumentElement().normalize();

			return parseDocument(document);
		}
		catch (Exception e)
		{
			LOG.error("Failed to parse XML offer", e);
			throw new XmlParseException("Failed to parse XML offer: " + e.getMessage(), e);
		}
	}

	/**
	 * Parses the DOM document into an IssuedOffer.
	 *
	 * @param document
	 * @return offer
	 */
	private IssuedOffer parseDocument(Document document)
	{
		var offer = new IssuedOffer();

		var offerElements = document.getElementsByTagName("IssuedOffer");
		if (offerElements.getLength() == 0)
		{
			throw new XmlParseException("No IssuedOffer element found in XML");
		}

		if (!(offerElements.item(0) instanceof Element offerElement))
		{
			throw new XmlParseException("IssuedOffer element is not a valid Element");
		}

		// Parse firm info
		offer.setFirm(parseFirmInfo(offerElement));

		// Parse document metadata
		offer.setDocNumber(getElementText(offerElement, "DocNumber"));
		offer.setDocDate(parseDate(getElementText(offerElement, "DocDate")));
		offer.setValidTill(parseDate(getElementText(offerElement, "ValidTill")));
		offer.setDescription(getElementText(offerElement, "Description"));
		offer.setCurrency(getElementText(offerElement, "Currency"));

		// Parse creator info - note the element name has a hyphen
		offer.setCreator(getElementText(offerElement, "Creator"));
		offer.setCreatorEmail(getElementText(offerElement, "CreatorE-mail"));

		// Parse product rows
		offer.setProducts(parseProducts(offerElement));

		LOG.info("Parsed offer {} with {} products for firm {}", offer.getDocNumber(), offer.getProducts().size(),
			offer.getFirm() != null ? offer.getFirm().getName() : "unknown");

		return offer;
	}

	/**
	 * Parses the FIRM_ID element into FirmInfo.
	 */
	private FirmInfo parseFirmInfo(Element offerElement)
	{
		var firmElements = offerElement.getElementsByTagName("FIRM_ID");
		if (firmElements.getLength() == 0)
		{
			return null;
		}

		if (firmElements.item(0) instanceof Element firmElement)
		{
			var firm = new FirmInfo();

			firm.setName(firmElement.getAttribute("Name"));
			firm.setOrgIdentNumber(firmElement.getAttribute("OrgIdentNumber"));
			firm.setVatIdentNumber(firmElement.getAttribute("VATIdentNumber"));
			firm.setStreet(firmElement.getAttribute("Address_Street"));
			firm.setCity(firmElement.getAttribute("Address_City"));
			firm.setPostCode(firmElement.getAttribute("Address_PostCode"));
			firm.setCountryCode(firmElement.getAttribute("Address_CountryCode"));

			return firm;
		}
		return null;
	}

	/**
	 * Parses all ROW elements into ProductRow objects.
	 */
	private List<ProductRow> parseProducts(Element offerElement)
	{
		List<ProductRow> products = new ArrayList<>();

		var rowElements = offerElement.getElementsByTagName("ROW");
		for (int i = 0; i < rowElements.getLength(); i++)
		{
			if (rowElements.item(i) instanceof Element rowElement)
			{
				products.add(parseProductRow(rowElement));
			}
		}

		return products;
	}

	/**
	 * Parses a single ROW element into a ProductRow.
	 */
	private ProductRow parseProductRow(Element rowElement)
	{
		var product = new ProductRow();

		// Get StoreCard_ID element
		var storeCardElements = rowElement.getElementsByTagName("StoreCard_ID");
		if (storeCardElements.getLength() > 0 && storeCardElements.item(0) instanceof Element storeCard)
		{

			product.setCode(getElementText(storeCard, "Code"));
			product.setName(getElementText(storeCard, "Name"));
			product.setProductId(getElementText(storeCard, "ID"));
			product.setBrand(getElementText(storeCard, "Brand"));
			product.setCategory(getElementText(storeCard, "Category"));
			product.setEan(getElementText(storeCard, "EAN"));

			// Convert picture path to URL
			var picturePath = getElementText(storeCard, "PicturePath");
			product.setPictureUrl(imagePathConverter.convertToUrl(product.getCode(), picturePath));

			// Parse and format description
			var rawDescription = getElementText(storeCard, "Description");
			product.setDescription(formatDescription(rawDescription));

			// Parse price
			product.setPrice(parsePrice(storeCard));
		}

		// Parse unit price (customer's price)
		var unitPriceText = getElementText(rowElement, "UPrice");
		product.setUnitPrice(parseBigDecimal(unitPriceText));

		return product;
	}

	/**
	 * Parses the Price element containing VOC and MOC.
	 */
	private ProductPrice parsePrice(Element storeCard)
	{
		var price = new ProductPrice();

		var priceElements = storeCard.getElementsByTagName("Price");
		if (priceElements.getLength() > 0 && priceElements.item(0) instanceof Element priceElement)
		{

			price.setVoc(parseBigDecimal(getElementText(priceElement, "VOC")));
			price.setMoc(parseBigDecimal(getElementText(priceElement, "MOC")));
		}

		return price;
	}

	/**
	 * Formats the description HTML by truncating and reformatting list items. Ported from PHP
	 * getDescription function.
	 *
	 * @param xmlDescription
	 *            the raw XML description
	 * @return the formatted description HTML
	 */
	private String formatDescription(String xmlDescription)
	{
		if (StringUtils.isBlank(xmlDescription))
		{
			return "";
		}

		var description = xmlDescription;

		// Convert <li> to temp markers, remove <ul>
		description = description.replaceAll("(?i)</li>", "<br />");
		description = description.replaceAll("(?i)<li>", "### ");
		description = description.replaceAll("(?i)<ul>", "");
		description = description.replaceAll("(?i)</ul>", "");

		// Determine line count based on length
		int lineCount = description.length() > 1500 ? 10 : 15;

		// Split by newlines and take first N lines
		var lines = description.split("\n");
		var limitedLines = new ArrayList<String>();
		for (int i = 0; i < Math.min(lineCount, lines.length); i++)
		{
			limitedLines.add(lines[i]);
		}
		description = String.join("\n", limitedLines);

		// Convert markers back to list items
		description = description.replace("### ", "<li>");
		description = description.replace("<br />", "</li>");

		// Wrap in <ul>
		description = "<ul>" + description + "</ul>";

		return description;
	}

	/**
	 * Gets the text content of a child element.
	 */
	private String getElementText(Element parent, String tagName)
	{
		var elements = parent.getElementsByTagName(tagName);
		if (elements.getLength() > 0)
		{
			return elements.item(0).getTextContent();
		}
		return "";
	}

	/**
	 * Parses a date string in yyyy-MM-dd format.
	 */
	private LocalDate parseDate(String dateText)
	{
		if (StringUtils.isBlank(dateText))
		{
			return null;
		}
		try
		{
			return LocalDate.parse(dateText, XML_DATE_FORMAT);
		}
		catch (Exception e)
		{
			LOG.warn("Failed to parse date: {}", dateText);
			return null;
		}
	}

	/**
	 * Parses a BigDecimal from string.
	 */
	private BigDecimal parseBigDecimal(String text)
	{
		if (StringUtils.isBlank(text))
		{
			return null;
		}
		try
		{
			return new BigDecimal(text);
		}
		catch (Exception e)
		{
			LOG.warn("Failed to parse BigDecimal: {}", text);
			return null;
		}
	}

	/**
	 * Exception thrown when XML parsing fails.
	 */
	public static class XmlParseException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new parse exception.
		 *
		 * @param message
		 *            the error message
		 */
		public XmlParseException(String message)
		{
			super(message);
		}

		/**
		 * Creates a new parse exception with cause.
		 *
		 * @param message
		 *            the error message
		 * @param cause
		 *            the underlying cause
		 */
		public XmlParseException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
