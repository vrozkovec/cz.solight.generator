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
 * Represents product pricing information including VOC (wholesale) and MOC (retail) prices.
 */
public class ProductPrice implements Serializable
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

	private BigDecimal voc;
	private BigDecimal moc;

	/**
	 * Gets the VOC (wholesale) price.
	 *
	 * @return the VOC price
	 */
	public BigDecimal getVoc()
	{
		return voc;
	}

	/**
	 * Sets the VOC (wholesale) price.
	 *
	 * @param voc
	 *            the VOC price
	 */
	public void setVoc(BigDecimal voc)
	{
		this.voc = voc;
	}

	/**
	 * Gets the MOC (retail/suggested) price.
	 *
	 * @return the MOC price
	 */
	public BigDecimal getMoc()
	{
		return moc;
	}

	/**
	 * Sets the MOC (retail/suggested) price.
	 *
	 * @param moc
	 *            the MOC price
	 */
	public void setMoc(BigDecimal moc)
	{
		this.moc = moc;
	}

	/**
	 * Gets the formatted VOC price with Czech decimal format.
	 *
	 * @return the formatted VOC price string
	 */
	public String getVocFormatted()
	{
		return voc != null ? PRICE_FORMAT.format(voc) : "";
	}

	/**
	 * Gets the formatted MOC price with Czech decimal format.
	 *
	 * @return the formatted MOC price string
	 */
	public String getMocFormatted()
	{
		return moc != null ? PRICE_FORMAT.format(moc) : "";
	}
}
