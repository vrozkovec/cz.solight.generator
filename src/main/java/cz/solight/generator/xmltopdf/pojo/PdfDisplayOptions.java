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
 * Configuration options for PDF display controlling which prices are shown.
 */
public class PdfDisplayOptions implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean showPriceMy = true;
	private boolean showPriceVOC = true;
	private boolean showPriceMOC = true;
	private PdfLocale locale = PdfLocale.CZ;

	/**
	 * Creates default display options with all prices shown.
	 */
	public PdfDisplayOptions()
	{
	}

	/**
	 * Creates display options with specified visibility settings.
	 *
	 * @param showPriceMy
	 *            whether to show the customer's price
	 * @param showPriceVOC
	 *            whether to show the VOC (wholesale) price
	 * @param showPriceMOC
	 *            whether to show the MOC (retail) price
	 */
	public PdfDisplayOptions(boolean showPriceMy, boolean showPriceVOC, boolean showPriceMOC)
	{
		this.showPriceMy = showPriceMy;
		this.showPriceVOC = showPriceVOC;
		this.showPriceMOC = showPriceMOC;
	}

	/**
	 * Checks if the customer's price should be displayed.
	 *
	 * @return true if customer price should be shown
	 */
	public boolean isShowPriceMy()
	{
		return showPriceMy;
	}

	/**
	 * Sets whether to display the customer's price.
	 *
	 * @param showPriceMy
	 *            true to show customer price
	 */
	public void setShowPriceMy(boolean showPriceMy)
	{
		this.showPriceMy = showPriceMy;
	}

	/**
	 * Checks if the VOC (wholesale) price should be displayed.
	 *
	 * @return true if VOC price should be shown
	 */
	public boolean isShowPriceVOC()
	{
		return showPriceVOC;
	}

	/**
	 * Sets whether to display the VOC (wholesale) price.
	 *
	 * @param showPriceVOC
	 *            true to show VOC price
	 */
	public void setShowPriceVOC(boolean showPriceVOC)
	{
		this.showPriceVOC = showPriceVOC;
	}

	/**
	 * Checks if the MOC (retail) price should be displayed.
	 *
	 * @return true if MOC price should be shown
	 */
	public boolean isShowPriceMOC()
	{
		return showPriceMOC;
	}

	/**
	 * Sets whether to display the MOC (retail) price.
	 *
	 * @param showPriceMOC
	 *            true to show MOC price
	 */
	public void setShowPriceMOC(boolean showPriceMOC)
	{
		this.showPriceMOC = showPriceMOC;
	}

	/**
	 * Gets the locale for PDF generation.
	 *
	 * @return the PDF locale
	 */
	public PdfLocale getLocale()
	{
		return locale;
	}

	/**
	 * Sets the locale for PDF generation.
	 *
	 * @param locale
	 *            the PDF locale (CZ or SK)
	 */
	public void setLocale(PdfLocale locale)
	{
		this.locale = locale;
	}
}
