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

import org.apache.commons.lang3.StringUtils;

import name.berries.wicket.util.app.AppConfigProvider;
import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;

/**
 * Represents a product sheet from the produktove_listy.xml catalog. Contains product details
 * including code, name, brand, description, and images.
 */
public class ProductSheet implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String BASE_IMAGES_URL = AppConfigProvider.getDefaultConfiguration()
		.getString(ConfigKey.APP_BASE_IMAGES_URL) + "/obrazky/";

	private String code;
	private String name;
	private String brandName;
	private String brand;
	private String ean;
	private int packageCount;
	private int guaranteeLength;
	private String productId;
	private String description;
	private String picture1Url;
	private String picture2Url;
	private String picture3Url;


	/**
	 * Construct.
	 */
	public ProductSheet()
	{
		super();
	}

	// ***************************************************
	// * UTILITY METHODS
	// ***************************************************

	/**
	 * Checks if this product has valid data for PDF generation.
	 *
	 * @return true if the product has at least a code and name
	 */
	public boolean isValid()
	{
		return StringUtils.isNotBlank(code) && StringUtils.isNotBlank(name);
	}

	// ***************************************************
	// * GETTERS / SETTERS
	// ***************************************************

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
	 * Gets the brand name (combined with product name).
	 *
	 * @return the brand name
	 */
	public String getBrandName()
	{
		return brandName;
	}

	/**
	 * Sets the brand name.
	 *
	 * @param brandName
	 *            the brand name
	 */
	public void setBrandName(String brandName)
	{
		this.brandName = brandName;
	}

	/**
	 * Gets the brand.
	 *
	 * @return the brand
	 */
	public String getBrand()
	{
		return brand;
	}

	/**
	 * Sets the brand.
	 *
	 * @param brand
	 *            the brand
	 */
	public void setBrand(String brand)
	{
		this.brand = brand;
	}

	/**
	 * Gets the EAN barcode.
	 *
	 * @return the EAN code
	 */
	public String getEan()
	{
		return ean;
	}

	/**
	 * Sets the EAN barcode.
	 *
	 * @param ean
	 *            the EAN code
	 */
	public void setEan(String ean)
	{
		this.ean = ean;
	}

	/**
	 * Gets the package count.
	 *
	 * @return the package count
	 */
	public int getPackageCount()
	{
		return packageCount;
	}

	/**
	 * Sets the package count.
	 *
	 * @param packageCount
	 *            the package count
	 */
	public void setPackageCount(int packageCount)
	{
		this.packageCount = packageCount;
	}

	/**
	 * Gets the guarantee length in months.
	 *
	 * @return the guarantee length
	 */
	public int getGuaranteeLength()
	{
		return guaranteeLength;
	}

	/**
	 * Sets the guarantee length in months.
	 *
	 * @param guaranteeLength
	 *            the guarantee length
	 */
	public void setGuaranteeLength(int guaranteeLength)
	{
		this.guaranteeLength = guaranteeLength;
	}

	/**
	 * Gets the formatted guarantee length string (e.g., "24 MĚSÍCŮ").
	 *
	 * @return the formatted guarantee length
	 */
	public String getGuaranteeLengthFormatted()
	{
		if (guaranteeLength <= 0)
		{
			return "";
		}
		return guaranteeLength + " MĚSÍCŮ";
	}

	/**
	 * Gets the internal product ID.
	 *
	 * @return the product ID
	 */
	public String getProductId()
	{
		return productId;
	}

	/**
	 * Sets the internal product ID.
	 *
	 * @param productId
	 *            the product ID
	 */
	public void setProductId(String productId)
	{
		this.productId = productId;
	}

	/**
	 * Gets the product description (HTML).
	 *
	 * @return the description HTML
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the product description (HTML).
	 *
	 * @param description
	 *            the description HTML
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets picture1Url.
	 *
	 * @return picture1Url
	 */
	public String getPicture1Url()
	{
		return picture1Url;
	}

	/**
	 * Sets picture1Url.
	 *
	 * @param picture1Url
	 *            picture1Url
	 */
	public void setPicture1Url(String picture1Url)
	{
		this.picture1Url = picture1Url;
	}

	/**
	 * Gets picture2Url.
	 *
	 * @return picture2Url
	 */
	public String getPicture2Url()
	{
		return picture2Url;
	}

	/**
	 * Sets picture2Url.
	 *
	 * @param picture2Url
	 *            picture2Url
	 */
	public void setPicture2Url(String picture2Url)
	{
		this.picture2Url = picture2Url;
	}

	/**
	 * Gets picture3Url.
	 *
	 * @return picture3Url
	 */
	public String getPicture3Url()
	{
		return picture3Url;
	}

	/**
	 * Sets picture3Url.
	 *
	 * @param picture3Url
	 *            picture3Url
	 */
	public void setPicture3Url(String picture3Url)
	{
		this.picture3Url = picture3Url;
	}

}
