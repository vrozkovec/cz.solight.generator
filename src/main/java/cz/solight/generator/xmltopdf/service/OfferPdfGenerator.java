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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

import name.berries.pdf.PdfGeneratorService;
import name.berries.pdf.PdfOptions;
import name.berries.wicket.util.app.AppConfigProvider;
import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;

import jakarta.inject.Inject;

/**
 * Service for generating PDF catalogs from parsed offer data. Uses Velocity templates for HTML
 * generation and Playwright for PDF conversion.
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

	/** Image paths for header/footer (Playwright requires base64 embedded images). */
	private static final String IMG_LOGO = "/webapp/img/logo_nove.png";
	private static final String IMG_VLNKA = "/webapp/img/vlnka.png";

	@Inject
	private PdfGeneratorService pdfGeneratorService;

	/**
	 * Construct.
	 */
	public OfferPdfGenerator()
	{
		super();
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

		// Render HTML from template
		var html = renderTemplate(context);

		// Generate PDF with repeating header/footer
		var pdfOptions = createPdfOptions(context);
		pdfGeneratorService.generatePdfFromHtml(html, outputPath.toString(), pdfOptions);

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

		context.put("baseUrl", AppConfigProvider.getDefaultConfiguration().getString(ConfigKey.APP_BASE_URL));
		context.put("baseImagesUrl", AppConfigProvider.getDefaultConfiguration().getString(ConfigKey.APP_BASE_IMAGES_URL));

		// Base64 encoded images for header/footer templates (Playwright doesn't load external
		// images)
		context.put("logoBase64", loadImageAsBase64DataUrl(IMG_LOGO));
		context.put("vlnkaBase64", loadImageAsBase64DataUrl(IMG_VLNKA));

		return context;
	}

	/**
	 * Renders the Velocity template with the given context.
	 *
	 * @param context
	 * @return html
	 */
	private String renderTemplate(Map<String, Object> context)
	{
		try (var textTemplate = new PackageTextTemplate(OfferPdfGenerator.class, TEMPLATE_PATH))
		{
			var template = textTemplate.asString();
			var reader = new StringReader(template);

			var velocityContext = new VelocityContext(context);
			var writer = new StringWriter();

			Velocity.evaluate(velocityContext, writer, TEMPLATE_PATH, reader);

			return writer.toString();
		}
		catch (Exception e)
		{
			LOG.error("Failed to render template", e);
			throw new RuntimeException("Failed to render PDF template: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates PDF options for A4 format with repeating header and footer.
	 *
	 * @param context
	 *            the Velocity context with variables for header/footer templates
	 * @return configured PDF options
	 */
	private PdfOptions createPdfOptions(Map<String, Object> context)
	{
		var options = new PdfOptions();
		options.setFormat("A4");
		options.setPrintBackground(true);
		options.setMarginTop("108px");
		options.setMarginBottom("108px");
		options.setMarginLeft("0");
		options.setMarginRight("0");
		options.setWaitFor("networkidle");

		// Enable repeating header/footer on every page
		options.setDisplayHeaderFooter(true);
		options.setHeaderTemplate(renderHeaderTemplate(context));
		options.setFooterTemplate(renderFooterTemplate(context));

		return options;
	}

	/**
	 * Renders the header template with the given context.
	 *
	 * @param context
	 *            the Velocity context
	 * @return rendered HTML for header
	 */
	private String renderHeaderTemplate(Map<String, Object> context)
	{
		return renderVelocityTemplate(HEADER_TEMPLATE_PATH, context);
	}

	/**
	 * Renders the footer template with the given context.
	 *
	 * @param context
	 *            the Velocity context
	 * @return rendered HTML for footer
	 */
	private String renderFooterTemplate(Map<String, Object> context)
	{
		return renderVelocityTemplate(FOOTER_TEMPLATE_PATH, context);
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

	/**
	 * Loads an image from the classpath and converts it to a base64 data URL. Playwright's
	 * header/footer templates run in isolation and cannot load external images, so images must be
	 * embedded as base64 data URLs.
	 *
	 * @param resourcePath
	 *            the classpath resource path (e.g., "/webapp/img/logo.png")
	 * @return base64 data URL string (e.g., "data:image/png;base64,...")
	 */
	private String loadImageAsBase64DataUrl(String resourcePath)
	{
		try (InputStream is = getClass().getResourceAsStream(resourcePath))
		{
			if (is == null)
			{
				LOG.warn("Image not found: {}", resourcePath);
				return "";
			}
			var bytes = is.readAllBytes();
			var base64 = Base64.getEncoder().encodeToString(bytes);
			var mimeType = resourcePath.endsWith(".png") ? "image/png" : "image/jpeg";
			return "data:" + mimeType + ";base64," + base64;
		}
		catch (IOException e)
		{
			LOG.error("Failed to load image: {}", resourcePath, e);
			return "";
		}
	}
}
