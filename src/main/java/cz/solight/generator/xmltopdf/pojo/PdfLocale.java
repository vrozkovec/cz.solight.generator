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
package cz.solight.generator.xmltopdf.pojo;

/**
 * Locale settings for PDF generation supporting Czech and Slovak languages.
 * Contains all localized strings, URLs, and currency symbols.
 */
public enum PdfLocale
{
	/** Czech locale with CZK currency and Czech text. */
	CZ("Kč", "b2b.solight.cz", "www.solight.cz", "solight@solight.cz",
		"PRODUKTOVÁ NABÍDKA PRO:", "Vaše cena:", "KOMPLETNÍ POPIS",
		"Nabídku vytvořil:", "Dne:", "NAHORU", "OBSAH", "Kód", "Popis", "Cena", "Platná do:"),

	/** Slovak locale with EUR currency and Slovak text. */
	SK("EUR", "b2b.solight.sk", "www.solight.sk", "solight@solight.sk",
		"PRODUKTOVÁ PONUKA PRE:", "Vaša cena:", "KOMPLETNÝ POPIS",
		"Ponuku vytvoril:", "Dňa:", "NAHOR", "OBSAH", "Kód", "Popis", "Cena", "Platná do:");

	private final String currencySymbol;
	private final String b2bDomain;
	private final String websiteUrl;
	private final String defaultEmail;
	private final String headerTitle;
	private final String yourPriceLabel;
	private final String fullDescriptionLabel;
	private final String createdByLabel;
	private final String datePrefix;
	private final String backToTopLabel;
	private final String tocLabel;
	private final String codeLabel;
	private final String descriptionLabel;
	private final String priceLabel;
	private final String validUntilLabel;

	/**
	 * Constructor for PdfLocale.
	 *
	 * @param currencySymbol
	 *            the currency symbol (e.g., "Kč", "EUR")
	 * @param b2bDomain
	 *            the B2B website domain
	 * @param websiteUrl
	 *            the main website URL
	 * @param defaultEmail
	 *            the default email address
	 * @param headerTitle
	 *            the header title text
	 * @param yourPriceLabel
	 *            the "Your price" label
	 * @param fullDescriptionLabel
	 *            the "Full description" link text
	 * @param createdByLabel
	 *            the "Created by" label
	 * @param datePrefix
	 *            the date prefix (e.g., "Dne:", "Dňa:")
	 * @param backToTopLabel
	 *            the "Back to top" link text
	 * @param tocLabel
	 *            the table of contents label
	 * @param codeLabel
	 *            the "Code" column header
	 * @param descriptionLabel
	 *            the "Description" column header
	 * @param priceLabel
	 *            the "Price" column header
	 * @param validUntilLabel
	 *            the "Valid until" label
	 */
	PdfLocale(String currencySymbol, String b2bDomain, String websiteUrl, String defaultEmail,
		String headerTitle, String yourPriceLabel, String fullDescriptionLabel,
		String createdByLabel, String datePrefix, String backToTopLabel,
		String tocLabel, String codeLabel, String descriptionLabel, String priceLabel,
		String validUntilLabel)
	{
		this.currencySymbol = currencySymbol;
		this.b2bDomain = b2bDomain;
		this.websiteUrl = websiteUrl;
		this.defaultEmail = defaultEmail;
		this.headerTitle = headerTitle;
		this.yourPriceLabel = yourPriceLabel;
		this.fullDescriptionLabel = fullDescriptionLabel;
		this.createdByLabel = createdByLabel;
		this.datePrefix = datePrefix;
		this.backToTopLabel = backToTopLabel;
		this.tocLabel = tocLabel;
		this.codeLabel = codeLabel;
		this.descriptionLabel = descriptionLabel;
		this.priceLabel = priceLabel;
		this.validUntilLabel = validUntilLabel;
	}

	/**
	 * Gets the currency symbol.
	 *
	 * @return the currency symbol
	 */
	public String getCurrencySymbol()
	{
		return currencySymbol;
	}

	/**
	 * Gets the B2B website domain.
	 *
	 * @return the B2B domain
	 */
	public String getB2bDomain()
	{
		return b2bDomain;
	}

	/**
	 * Gets the main website URL.
	 *
	 * @return the website URL
	 */
	public String getWebsiteUrl()
	{
		return websiteUrl;
	}

	/**
	 * Gets the default email address.
	 *
	 * @return the default email
	 */
	public String getDefaultEmail()
	{
		return defaultEmail;
	}

	/**
	 * Gets the header title text.
	 *
	 * @return the header title
	 */
	public String getHeaderTitle()
	{
		return headerTitle;
	}

	/**
	 * Gets the "Your price" label.
	 *
	 * @return the your price label
	 */
	public String getYourPriceLabel()
	{
		return yourPriceLabel;
	}

	/**
	 * Gets the "Full description" link text.
	 *
	 * @return the full description label
	 */
	public String getFullDescriptionLabel()
	{
		return fullDescriptionLabel;
	}

	/**
	 * Gets the "Created by" label.
	 *
	 * @return the created by label
	 */
	public String getCreatedByLabel()
	{
		return createdByLabel;
	}

	/**
	 * Gets the date prefix.
	 *
	 * @return the date prefix
	 */
	public String getDatePrefix()
	{
		return datePrefix;
	}

	/**
	 * Gets the "Back to top" link text.
	 *
	 * @return the back to top label
	 */
	public String getBackToTopLabel()
	{
		return backToTopLabel;
	}

	/**
	 * Gets the table of contents label.
	 *
	 * @return the TOC label
	 */
	public String getTocLabel()
	{
		return tocLabel;
	}

	/**
	 * Gets the "Code" column header.
	 *
	 * @return the code label
	 */
	public String getCodeLabel()
	{
		return codeLabel;
	}

	/**
	 * Gets the "Description" column header.
	 *
	 * @return the description label
	 */
	public String getDescriptionLabel()
	{
		return descriptionLabel;
	}

	/**
	 * Gets the "Price" column header.
	 *
	 * @return the price label
	 */
	public String getPriceLabel()
	{
		return priceLabel;
	}

	/**
	 * Gets the "Valid until" label.
	 *
	 * @return the valid until label
	 */
	public String getValidUntilLabel()
	{
		return validUntilLabel;
	}
}
