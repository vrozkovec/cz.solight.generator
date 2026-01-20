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
 * Defines the output format variants for product sheet PDFs.
 */
public enum ProductSheetFormat
{
	/**
	 * Fixed A4 height with content cutoff and "KOMPLETNI POPIS" button.
	 * Uses standard A4 dimensions (210mm x 297mm).
	 */
	A4_SHORT("A4", "_produktovy_list_A4.pdf"),

	/**
	 * Single continuous page with A4 width and tall height to fit all content.
	 * Uses 210mm width (A4) and 3000mm height to accommodate long descriptions.
	 */
	FULL_LENGTH(null, "_produktovy_list_full.pdf");

	private final String pageFormat;
	private final String fileSuffix;

	/**
	 * Creates a new format with specified page format and file suffix.
	 *
	 * @param pageFormat
	 *            the page format string for PdfOptions
	 * @param fileSuffix
	 *            the file suffix to append to product code
	 */
	ProductSheetFormat(String pageFormat, String fileSuffix)
	{
		this.pageFormat = pageFormat;
		this.fileSuffix = fileSuffix;
	}

	/**
	 * Gets the page format string for PdfOptions.
	 *
	 * @return the page format
	 */
	public String getPageFormat()
	{
		return pageFormat;
	}

	/**
	 * Gets the file suffix to append to product code.
	 *
	 * @return the file suffix (e.g., "_produktovy_list_A4.pdf")
	 */
	public String getFileSuffix()
	{
		return fileSuffix;
	}

	/**
	 * Builds the output filename for a product.
	 *
	 * @param productCode
	 *            the product code
	 * @return the complete filename (e.g., "1D81_produktovy_list_A4.pdf")
	 */
	public String buildFilename(String productCode)
	{
		return productCode + fileSuffix;
	}
}
