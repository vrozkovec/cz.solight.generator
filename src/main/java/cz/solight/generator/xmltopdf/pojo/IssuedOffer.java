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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete issued offer parsed from XML.
 * Contains firm information, document metadata, and product rows.
 */
public class IssuedOffer implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter CZECH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private FirmInfo firm;
	private String docNumber;
	private LocalDate docDate;
	private LocalDate validTill;
	private String description;
	private String currency;
	private String creator;
	private String creatorEmail;
	private List<ProductRow> products = new ArrayList<>();

	/**
	 * Gets the firm/company information.
	 *
	 * @return the firm info
	 */
	public FirmInfo getFirm()
	{
		return firm;
	}

	/**
	 * Sets the firm/company information.
	 *
	 * @param firm
	 *            the firm info
	 */
	public void setFirm(FirmInfo firm)
	{
		this.firm = firm;
	}

	/**
	 * Gets the document number.
	 *
	 * @return the document number
	 */
	public String getDocNumber()
	{
		return docNumber;
	}

	/**
	 * Sets the document number.
	 *
	 * @param docNumber
	 *            the document number
	 */
	public void setDocNumber(String docNumber)
	{
		this.docNumber = docNumber;
	}

	/**
	 * Gets the document date.
	 *
	 * @return the document date
	 */
	public LocalDate getDocDate()
	{
		return docDate;
	}

	/**
	 * Sets the document date.
	 *
	 * @param docDate
	 *            the document date
	 */
	public void setDocDate(LocalDate docDate)
	{
		this.docDate = docDate;
	}

	/**
	 * Gets the offer validity date.
	 *
	 * @return the validity end date
	 */
	public LocalDate getValidTill()
	{
		return validTill;
	}

	/**
	 * Sets the offer validity date.
	 *
	 * @param validTill
	 *            the validity end date
	 */
	public void setValidTill(LocalDate validTill)
	{
		this.validTill = validTill;
	}

	/**
	 * Gets the offer description.
	 *
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the offer description.
	 *
	 * @param description
	 *            the description
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the currency code.
	 *
	 * @return the currency code (e.g., CZK)
	 */
	public String getCurrency()
	{
		return currency;
	}

	/**
	 * Sets the currency code.
	 *
	 * @param currency
	 *            the currency code (e.g., CZK)
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	/**
	 * Gets the creator name.
	 *
	 * @return the creator name
	 */
	public String getCreator()
	{
		return creator;
	}

	/**
	 * Sets the creator name.
	 *
	 * @param creator
	 *            the creator name
	 */
	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	/**
	 * Gets the creator's email address.
	 *
	 * @return the creator's email
	 */
	public String getCreatorEmail()
	{
		return creatorEmail;
	}

	/**
	 * Sets the creator's email address.
	 *
	 * @param creatorEmail
	 *            the creator's email
	 */
	public void setCreatorEmail(String creatorEmail)
	{
		this.creatorEmail = creatorEmail;
	}

	/**
	 * Gets the list of product rows in this offer.
	 *
	 * @return the list of products
	 */
	public List<ProductRow> getProducts()
	{
		return products;
	}

	/**
	 * Sets the list of product rows in this offer.
	 *
	 * @param products
	 *            the list of products
	 */
	public void setProducts(List<ProductRow> products)
	{
		this.products = products;
	}

	/**
	 * Gets the validity date formatted in Czech format (dd.MM.yyyy).
	 *
	 * @return the formatted validity date
	 */
	public String getValidTillFormatted()
	{
		return validTill != null ? validTill.format(CZECH_DATE_FORMAT) : "";
	}

	/**
	 * Gets the document date formatted in Czech format (dd.MM.yyyy).
	 *
	 * @return the formatted document date
	 */
	public String getDocDateFormatted()
	{
		return docDate != null ? docDate.format(CZECH_DATE_FORMAT) : "";
	}

	/**
	 * Gets the currency symbol for display.
	 *
	 * @return the currency symbol
	 */
	public String getCurrencySymbol()
	{
		return switch (currency)
		{
			case "CZK" -> "Kč";
			case "EUR" -> "€";
			default -> currency;
		};
	}
}
