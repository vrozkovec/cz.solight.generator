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

/**
 * Represents company (firm) information extracted from the XML offer.
 * Contains company name, identification numbers, and address details.
 */
public class FirmInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name;
	private String orgIdentNumber;
	private String vatIdentNumber;
	private String street;
	private String city;
	private String postCode;
	private String countryCode;

	/**
	 * Gets the company name.
	 *
	 * @return the company name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the company name.
	 *
	 * @param name
	 *            the company name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the organization identification number (IČO).
	 *
	 * @return the organization identification number
	 */
	public String getOrgIdentNumber()
	{
		return orgIdentNumber;
	}

	/**
	 * Sets the organization identification number (IČO).
	 *
	 * @param orgIdentNumber
	 *            the organization identification number
	 */
	public void setOrgIdentNumber(String orgIdentNumber)
	{
		this.orgIdentNumber = orgIdentNumber;
	}

	/**
	 * Gets the VAT identification number (DIČ).
	 *
	 * @return the VAT identification number
	 */
	public String getVatIdentNumber()
	{
		return vatIdentNumber;
	}

	/**
	 * Sets the VAT identification number (DIČ).
	 *
	 * @param vatIdentNumber
	 *            the VAT identification number
	 */
	public void setVatIdentNumber(String vatIdentNumber)
	{
		this.vatIdentNumber = vatIdentNumber;
	}

	/**
	 * Gets the street address.
	 *
	 * @return the street address
	 */
	public String getStreet()
	{
		return street;
	}

	/**
	 * Sets the street address.
	 *
	 * @param street
	 *            the street address
	 */
	public void setStreet(String street)
	{
		this.street = street;
	}

	/**
	 * Gets the city name.
	 *
	 * @return the city name
	 */
	public String getCity()
	{
		return city;
	}

	/**
	 * Sets the city name.
	 *
	 * @param city
	 *            the city name
	 */
	public void setCity(String city)
	{
		this.city = city;
	}

	/**
	 * Gets the postal code.
	 *
	 * @return the postal code
	 */
	public String getPostCode()
	{
		return postCode;
	}

	/**
	 * Sets the postal code.
	 *
	 * @param postCode
	 *            the postal code
	 */
	public void setPostCode(String postCode)
	{
		this.postCode = postCode;
	}

	/**
	 * Gets the country code (e.g., CZ).
	 *
	 * @return the country code
	 */
	public String getCountryCode()
	{
		return countryCode;
	}

	/**
	 * Sets the country code (e.g., CZ).
	 *
	 * @param countryCode
	 *            the country code
	 */
	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}

	/**
	 * Gets the full formatted address.
	 *
	 * @return the full address as a single string
	 */
	public String getFullAddress()
	{
		return "%s, %s %s".formatted(street, postCode, city);
	}
}
