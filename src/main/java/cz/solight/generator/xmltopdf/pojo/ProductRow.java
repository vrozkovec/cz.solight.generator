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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Represents a single product row from the XML offer.
 * Contains product details including code, name, pricing, and description.
 */
public class ProductRow implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final DecimalFormat PRICE_FORMAT;

	static
	{
		var symbols = new DecimalFormatSymbols(new Locale("cs", "CZ"));
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator(' ');
		PRICE_FORMAT = new DecimalFormat("#,##0.00", symbols);
	}

	private String code;
	private String name;
	private String productId;
	private String brand;
	private String category;
	private String pictureUrl;
	private String ean;
	private String description;
	private ProductPrice price;
	private BigDecimal unitPrice;

	/**
	 * Gets the product code.
	 *
	 * @return the product code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * Sets the product code.
	 *
	 * @param code
	 *            the product code
	 */
	public void setCode(String code)
	{
		this.code = code;
	}

	/**
	 * Gets the product name.
	 *
	 * @return the product name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the product name.
	 *
	 * @param name
	 *            the product name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the internal product ID for B2B portal links.
	 *
	 * @return the product ID
	 */
	public String getProductId()
	{
		return productId;
	}

	/**
	 * Sets the internal product ID for B2B portal links.
	 *
	 * @param productId
	 *            the product ID
	 */
	public void setProductId(String productId)
	{
		this.productId = productId;
	}

	/**
	 * Gets the product brand.
	 *
	 * @return the brand name
	 */
	public String getBrand()
	{
		return brand;
	}

	/**
	 * Sets the product brand.
	 *
	 * @param brand
	 *            the brand name
	 */
	public void setBrand(String brand)
	{
		this.brand = brand;
	}

	/**
	 * Gets the product category.
	 *
	 * @return the category name
	 */
	public String getCategory()
	{
		return category;
	}

	/**
	 * Sets the product category.
	 *
	 * @param category
	 *            the category name
	 */
	public void setCategory(String category)
	{
		this.category = category;
	}

	/**
	 * Gets the product image URL.
	 *
	 * @return the image URL
	 */
	public String getPictureUrl()
	{
		return pictureUrl;
	}

	/**
	 * Sets the product image URL.
	 *
	 * @param pictureUrl
	 *            the image URL
	 */
	public void setPictureUrl(String pictureUrl)
	{
		this.pictureUrl = pictureUrl;
	}

	/**
	 * Gets the product EAN barcode.
	 *
	 * @return the EAN code
	 */
	public String getEan()
	{
		return ean;
	}

	/**
	 * Sets the product EAN barcode.
	 *
	 * @param ean
	 *            the EAN code
	 */
	public void setEan(String ean)
	{
		this.ean = ean;
	}

	/**
	 * Gets the product description HTML.
	 *
	 * @return the description HTML
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the product description HTML.
	 *
	 * @param description
	 *            the description HTML
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the product pricing (VOC/MOC).
	 *
	 * @return the price object
	 */
	public ProductPrice getPrice()
	{
		return price;
	}

	/**
	 * Sets the product pricing (VOC/MOC).
	 *
	 * @param price
	 *            the price object
	 */
	public void setPrice(ProductPrice price)
	{
		this.price = price;
	}

	/**
	 * Gets the customer's unit price.
	 *
	 * @return the unit price
	 */
	public BigDecimal getUnitPrice()
	{
		return unitPrice;
	}

	/**
	 * Sets the customer's unit price.
	 *
	 * @param unitPrice
	 *            the unit price
	 */
	public void setUnitPrice(BigDecimal unitPrice)
	{
		this.unitPrice = unitPrice;
	}

	/**
	 * Gets the formatted unit price with Czech decimal format.
	 *
	 * @return the formatted unit price string
	 */
	public String getUnitPriceFormatted()
	{
		return unitPrice != null ? PRICE_FORMAT.format(unitPrice) : "";
	}

	/**
	 * Gets the anchor name for HTML links (dots replaced with underscores).
	 *
	 * @return the anchor name
	 */
	public String getAnchorName()
	{
		return code != null ? code.replace(".", "_") : "";
	}
}
