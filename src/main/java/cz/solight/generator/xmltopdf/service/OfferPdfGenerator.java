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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.solight.generator.xmltopdf.pojo.IssuedOffer;
import cz.solight.generator.xmltopdf.pojo.PdfDisplayOptions;
import cz.solight.generator.xmltopdf.util.ContextUtil;

import name.berries.wicket.util.app.AppConfigProvider;
import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

/**
 * Service for generating PDF catalogs from parsed offer data. Uses Velocity templates for HTML
 * generation and Gotenberg for PDF conversion with repeating header/footer on each page.
 */
public class OfferPdfGenerator
{
	private static final Logger LOG = LoggerFactory.getLogger(OfferPdfGenerator.class);
	private static final DateTimeFormatter CZECH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private static final String TEMPLATE_PATH = "templates/offer-catalog.vm";
	private static final String HEADER_TEMPLATE_PATH = "templates/offer-header.vm";
	private static final String FOOTER_TEMPLATE_PATH = "templates/offer-footer.vm";
	private static final int TOC_FIRST_PAGE_LIMIT = 12;
	private static final int TOC_OTHER_PAGE_LIMIT = 15;

	/** Header/footer margin in inches (108px at 96 DPI = 1.125 inches). */
	private static final String HEADER_MARGIN = "1in";
	private static final String FOOTER_MARGIN = "1.125in";

	/** Gotenberg server URL. */
	private final String gotenbergUrl;

	/**
	 * Creates a new PDF generator using Gotenberg URL from configuration.
	 */
	public OfferPdfGenerator()
	{
		gotenbergUrl = AppConfigProvider.getDefaultConfiguration().getString(ConfigKey.GOTENBERG_URL);
	}

	/**
	 * Generates a PDF catalog from the offer data.
	 *
	 * @param offer
	 *            the parsed offer data
	 * @param options
	 *            the display options
	 * @param outputPath
	 *            the output PDF path
	 * @throws Exception
	 *             if PDF generation fails
	 */
	public void generatePdf(IssuedOffer offer, PdfDisplayOptions options, Path outputPath) throws Exception
	{
		LOG.info("Generating PDF for offer {} with {} products to {}", offer.getDocNumber(), offer.getProducts().size(),
			outputPath);

		// Build Velocity context
		Map<String, Object> context = buildContext(offer, options);

		// Render all three templates (main, header, footer)
		var mainHtml = renderTemplate(context);
		var headerHtml = renderVelocityTemplate(HEADER_TEMPLATE_PATH, context);
		var footerHtml = renderVelocityTemplate(FOOTER_TEMPLATE_PATH, context);

		// Generate PDF via Gotenberg with repeating header/footer
		byte[] pdfBytes = generatePdfWithHeaderFooter(mainHtml, headerHtml, footerHtml);

		// Write to output file
		Files.write(outputPath, pdfBytes);

		LOG.info("PDF generated successfully: {}", outputPath);
	}

	/**
	 * Builds the Velocity context with all required variables.
	 *
	 * @param offer
	 *            the parsed offer
	 * @param options
	 *            the display options including locale
	 * @return map of context variables for Velocity template
	 */
	private Map<String, Object> buildContext(IssuedOffer offer, PdfDisplayOptions options)
	{
		var context = new HashMap<String, Object>();
		var locale = options.getLocale();

		// Apply locale's default email if creator email is blank
		if (StringUtils.isBlank(offer.getCreatorEmail()))
		{
			offer.setCreatorEmail(locale.getDefaultEmail());
		}


		// Offer data
		context.put("offer", offer);
		context.put("firm", offer.getFirm());
		context.put("products", offer.getProducts());

		// Display options
		context.put("options", options);
		context.put("showPriceMy", options.isShowPriceMy());
		context.put("showPriceVOC", options.isShowPriceVOC());
		context.put("showPriceMOC", options.isShowPriceMOC());

		// Locale for internationalization
		context.put("locale", locale);

		// Formatting helpers
		context.put("currentDate", LocalDate.now().format(CZECH_DATE_FORMAT));
		// Use locale's currency symbol instead of offer's
		context.put("currencySymbol", locale.getCurrencySymbol());

		// TOC pagination limits
		context.put("tocFirstPageLimit", TOC_FIRST_PAGE_LIMIT);
		context.put("tocOtherPageLimit", TOC_OTHER_PAGE_LIMIT);

		ContextUtil.addCommonValues(context);


		return context;
	}

	/**
	 * Renders the main Velocity template with the given context.
	 *
	 * @param context
	 *            the Velocity context
	 * @return rendered HTML string
	 */
	private String renderTemplate(Map<String, Object> context)
	{
		return renderVelocityTemplate(TEMPLATE_PATH, context);
	}

	/**
	 * Generates a PDF with Gotenberg using native header/footer support. Header and footer are sent
	 * as separate files and repeat on each page.
	 *
	 * @param mainHtml
	 *            the main content HTML
	 * @param headerHtml
	 *            the header HTML (complete HTML document)
	 * @param footerHtml
	 *            the footer HTML (complete HTML document)
	 * @return PDF bytes
	 * @throws Exception
	 *             if generation fails
	 */
	private byte[] generatePdfWithHeaderFooter(String mainHtml, String headerHtml, String footerHtml) throws Exception
	{
		HttpResponse<byte[]> response = Unirest.post(gotenbergUrl + "/forms/chromium/convert/html")
			.field("files", new ByteArrayInputStream(mainHtml.getBytes(StandardCharsets.UTF_8)), "index.html")
			.field("files", new ByteArrayInputStream(headerHtml.getBytes(StandardCharsets.UTF_8)), "header.html")
			.field("files", new ByteArrayInputStream(footerHtml.getBytes(StandardCharsets.UTF_8)), "footer.html")
			.field("preferCssPageSize", "true").field("printBackground", "true").field("marginTop", HEADER_MARGIN)
			.field("marginBottom", FOOTER_MARGIN).field("marginLeft", "0").field("marginRight", "0").asBytes();

		if (!response.isSuccess())
		{
			throw new RuntimeException(
				"PDF generation failed: " + response.getStatus() + " - " + new String(response.getBody(), StandardCharsets.UTF_8));
		}

		return response.getBody();
	}

	/**
	 * Renders a Velocity template from the given path with the context.
	 *
	 * @param templatePath
	 *            the template resource path
	 * @param context
	 *            the Velocity context
	 * @return rendered HTML string
	 */
	private String renderVelocityTemplate(String templatePath, Map<String, Object> context)
	{
		try (var textTemplate = new PackageTextTemplate(OfferPdfGenerator.class, templatePath);
			var reader = new StringReader(textTemplate.asString());
			var writer = new StringWriter())
		{
			var velocityContext = new VelocityContext(context);
			Velocity.evaluate(velocityContext, writer, templatePath, reader);
			return writer.toString();
		}
		catch (Exception e)
		{
			var errorMsg = "Failed to render template: " + templatePath + " - " + e.getMessage();
			LOG.error("Failed to render template: {}", templatePath, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
}
