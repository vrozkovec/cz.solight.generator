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
 * Represents a product sheet from the produktove_listy.xml catalog.
 * Contains product details including code, name, brand, description, and images.
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
	private String picture1;
	private String picture2;
	private String picture3;

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
	 * Gets the primary picture filename.
	 *
	 * @return the picture filename
	 */
	public String getPicture1()
	{
		return picture1;
	}

	/**
	 * Sets the primary picture filename.
	 *
	 * @param picture1
	 *            the picture filename
	 */
	public void setPicture1(String picture1)
	{
		this.picture1 = picture1;
	}

	/**
	 * Gets the secondary picture filename.
	 *
	 * @return the picture filename
	 */
	public String getPicture2()
	{
		return picture2;
	}

	/**
	 * Sets the secondary picture filename.
	 *
	 * @param picture2
	 *            the picture filename
	 */
	public void setPicture2(String picture2)
	{
		this.picture2 = picture2;
	}

	/**
	 * Gets the tertiary picture filename.
	 *
	 * @return the picture filename
	 */
	public String getPicture3()
	{
		return picture3;
	}

	/**
	 * Sets the tertiary picture filename.
	 *
	 * @param picture3
	 *            the picture filename
	 */
	public void setPicture3(String picture3)
	{
		this.picture3 = picture3;
	}

	/**
	 * Gets the full URL for the primary picture.
	 *
	 * @return the picture URL, or empty string if no picture
	 */
	public String getPicture1Url()
	{
		return buildPictureUrl(picture1);
	}

	/**
	 * Gets the full URL for the secondary picture.
	 *
	 * @return the picture URL, or empty string if no picture
	 */
	public String getPicture2Url()
	{
		return buildPictureUrl(picture2);
	}

	/**
	 * Gets the full URL for the tertiary picture.
	 *
	 * @return the picture URL, or empty string if no picture
	 */
	public String getPicture3Url()
	{
		return buildPictureUrl(picture3);
	}

	/**
	 * Builds a full URL from a picture filename.
	 *
	 * @param filename
	 *            the picture filename
	 * @return the full URL, or empty string if filename is blank
	 */
	private String buildPictureUrl(String filename)
	{
		if (StringUtils.isBlank(filename))
		{
			return "";
		}
		return BASE_IMAGES_URL + filename;
	}

	/**
	 * Checks if this product has valid data for PDF generation.
	 *
	 * @return true if the product has at least a code and name
	 */
	public boolean isValid()
	{
		return StringUtils.isNotBlank(code) && StringUtils.isNotBlank(name);
	}
}
