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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.solight.generator.xmltopdf.pojo.ProductSheet;
import cz.solight.generator.xmltopdf.pojo.ProductSheetFormat;
import cz.solight.generator.xmltopdf.util.ContextUtil;

import name.berries.wicket.util.app.AppConfigProvider;
import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.MultipartBody;
import kong.unirest.core.Unirest;

/**
 * Service for generating product sheet PDFs from ProductSheet data using Gotenberg. Generates both
 * A4 short version (with repeating header/footer on each page) and full-length version (single
 * continuous page with dynamic height) for each product.
 *
 * <p>
 * Both formats use Gotenberg's native header/footer support with proper margins.
 * </p>
 * <p>
 * For full-length: Uses two-pass approach (screenshot to measure content, then generate with exact
 * height = content + header margin + footer margin).
 * </p>
 */
public class ProductSheetPdfGenerator
{
	private static final Logger log = LoggerFactory.getLogger(ProductSheetPdfGenerator.class);

	private static final String TEMPLATE_PATH = "templates/product-sheet.vm";
	private static final String HEADER_TEMPLATE_PATH = "templates/product-sheet-header.vm";
	private static final String FOOTER_TEMPLATE_PATH = "templates/product-sheet-footer.vm";


	/** A4 width in inches for Gotenberg. */
	private static final double A4_WIDTH_INCHES = 8.27;

	/** Default DPI used by Gotenberg/Chromium for print. */
	private static final int PRINT_DPI = 96;

	/** A4 width in pixels at print DPI. */
	private static final int A4_WIDTH_PX = (int)(A4_WIDTH_INCHES * PRINT_DPI); // ~794px

	/** Header height in inches (100px at 96 DPI). */
	private static final String HEADER_MARGIN = "0";
	private static final double HEADER_MARGIN_INCHES = 0;

	/** Footer height in inches (80px at 96 DPI). */
	private static final String FOOTER_MARGIN = "0";
	private static final double FOOTER_MARGIN_INCHES = 0;

	/** Gotenberg server URL. */
	private final String gotenbergUrl;

	/**
	 * Creates a new PDF generator using Gotenberg URL from configuration.
	 */
	public ProductSheetPdfGenerator()
	{
		gotenbergUrl = AppConfigProvider.getDefaultConfiguration().getString(ConfigKey.GOTENBERG_URL);
	}

	/**
	 * Creates a new PDF generator with the specified Gotenberg URL.
	 *
	 * @param gotenbergUrl
	 *            the Gotenberg server URL (e.g., "http://localhost:3000")
	 */
	public ProductSheetPdfGenerator(String gotenbergUrl)
	{
		this.gotenbergUrl = gotenbergUrl;
	}

	/**
	 * Generates PDF files for all products in both A4_SHORT and FULL_LENGTH formats.
	 *
	 * @param products
	 *            the list of products to generate PDFs for
	 * @param fileConsumer
	 * @throws Exception
	 *             if PDF generation fails
	 */
	public void generateAllPdfs(List<ProductSheet> products, Consumer<File> fileConsumer) throws Exception
	{
		Path outputDir = Files.createTempDirectory("product-sheets-");
		log.info("Generating PDFs via Gotenberg for {} products to {}", products.size(), outputDir);

		int successCount = 0;
		int failCount = 0;

		for (var product : products)
		{
			try
			{
				// Generate A4 short version
				var a4Path = outputDir.resolve(ProductSheetFormat.A4_SHORT.buildFilename(product.getCode()));
				generatePdf(product, ProductSheetFormat.A4_SHORT, a4Path);
				fileConsumer.accept(a4Path.toFile());

				// Generate full-length version
				var fullPath = outputDir.resolve(ProductSheetFormat.FULL_LENGTH.buildFilename(product.getCode()));
				generatePdf(product, ProductSheetFormat.FULL_LENGTH, fullPath);
				fileConsumer.accept(fullPath.toFile());
				successCount++;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				log.error("Failed to generate PDFs for product {}: {}", product.getCode(), e.getMessage());
				failCount++;
			}
		}

		log.info("PDF generation complete: {} successful, {} failed", successCount, failCount);
	}

	/**
	 * Generates a single PDF for a product in the specified format.
	 *
	 * @param product
	 *            the product data
	 * @param format
	 *            the output format (A4_SHORT or FULL_LENGTH)
	 * @param outputPath
	 *            the output PDF file path
	 * @throws Exception
	 *             if PDF generation fails
	 */
	public void generatePdf(ProductSheet product, ProductSheetFormat format, Path outputPath) throws Exception
	{
		log.debug("Generating {} PDF via Gotenberg for product {} to {}", format, product.getCode(), outputPath);

		// Build Velocity context
		Map<String, Object> context = buildContext(product, format);

		// Render templates
		var mainHtml = renderVelocityTemplate(TEMPLATE_PATH, context);
		var headerHtml = renderVelocityTemplate(HEADER_TEMPLATE_PATH, context);
		var footerHtml = renderVelocityTemplate(FOOTER_TEMPLATE_PATH, context);

		// Generate PDF based on format (both use Gotenberg's native header/footer)
		byte[] pdfBytes;
		if (format == ProductSheetFormat.FULL_LENGTH)
		{
			// Full-length: dynamic height based on content
			pdfBytes = generateDynamicHeightPdf(mainHtml, headerHtml, footerHtml);
		}
		else
		{
			// A4: fixed page size
			pdfBytes = generateFixedA4PdfWithHeaderFooter(mainHtml, headerHtml, footerHtml);
		}

		// Write to output file
		Files.write(outputPath, pdfBytes);

		log.debug("PDF generated: {}", outputPath);
	}

	/**
	 * Generates a single-page PDF with A4 width and dynamic height to fit all content. Uses
	 * two-pass approach: measure content height via screenshot, then generate PDF with exact
	 * dimensions. Header and footer are added via Gotenberg's native support.
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
	private byte[] generateDynamicHeightPdf(String mainHtml, String headerHtml, String footerHtml) throws Exception
	{
		// Step 1: Measure content height via screenshot
		int contentHeightPx = measureContentHeight(mainHtml, headerHtml, footerHtml);
		log.info("Measured content height: {}px", contentHeightPx);

		// Step 2: Generate PDF with exact dimensions
		return generatePdfWithHeight(mainHtml, headerHtml, footerHtml, contentHeightPx);
	}

	/**
	 * Generates a fixed A4 PDF with Gotenberg's native header/footer support. Header and footer are
	 * sent as separate files and repeat on each page.
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
	private byte[] generateFixedA4PdfWithHeaderFooter(String mainHtml, String headerHtml, String footerHtml) throws Exception
	{
		MultipartBody request = Unirest.post(gotenbergUrl + "/forms/chromium/convert/html")
			.field("files", new ByteArrayInputStream(mainHtml.getBytes(StandardCharsets.UTF_8)), "index.html")
			.field("files", new ByteArrayInputStream(headerHtml.getBytes(StandardCharsets.UTF_8)), "header.html")
			.field("files", new ByteArrayInputStream(footerHtml.getBytes(StandardCharsets.UTF_8)), "footer.html")
			.field("preferCssPageSize", "true").field("printBackground", "true").field("marginTop", HEADER_MARGIN)
			.field("marginBottom", FOOTER_MARGIN).field("marginLeft", "0").field("marginRight", "0");

		HttpResponse<byte[]> response = request.asBytes();

		if (!response.isSuccess())
		{
			throw new RuntimeException(
				"PDF generation failed: " + response.getStatus() + " - " + new String(response.getBody(), StandardCharsets.UTF_8));
		}

		return response.getBody();
	}

	/**
	 * Measures content height by taking a full-page screenshot. Only measures the main content
	 * (index.html) - the screenshot endpoint doesn't support header/footer files.
	 *
	 * @param mainHtml
	 *            the main HTML content
	 * @return content height in pixels
	 * @throws Exception
	 *             if measurement fails
	 */
	private int measureContentHeight(String mainHtml, String headerHtml, String footerHtml) throws Exception
	{
		HttpResponse<byte[]> response = Unirest.post(gotenbergUrl + "/forms/chromium/screenshot/html")
			.field("files", new ByteArrayInputStream(mainHtml.getBytes(StandardCharsets.UTF_8)), "index.html")
			.field("files", new ByteArrayInputStream(headerHtml.getBytes(StandardCharsets.UTF_8)), "header.html")
			.field("files", new ByteArrayInputStream(footerHtml.getBytes(StandardCharsets.UTF_8)), "footer.html")
			.field("width", String.valueOf(A4_WIDTH_PX)) // A4 width in pixels at 96 DPI
			.field("clip", "false") // Don't clip - capture full content height
			.field("format", "png").field("optimizeForSpeed", "true").asBytes();

		if (!response.isSuccess())
		{
			throw new RuntimeException(
				"Screenshot failed: " + response.getStatus() + " - " + new String(response.getBody(), StandardCharsets.UTF_8));
		}

		// Read image dimensions
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.getBody()));
		int height = image.getHeight();
		log.debug("Screenshot dimensions: {}x{} px", image.getWidth(), height);
		return height;
	}

	/**
	 * Generates PDF with specified content height plus header/footer margins.
	 *
	 * @param mainHtml
	 *            the main HTML content
	 * @param headerHtml
	 *            the header HTML (complete HTML document)
	 * @param footerHtml
	 *            the footer HTML (complete HTML document)
	 * @param contentHeightPx
	 *            the main content height in pixels (excluding header/footer)
	 * @return PDF bytes
	 * @throws Exception
	 *             if generation fails
	 */
	private byte[] generatePdfWithHeight(String mainHtml, String headerHtml, String footerHtml, int contentHeightPx)
		throws Exception
	{
		// Convert content pixels to inches for Gotenberg (at 96 DPI)
		double contentHeightInches = (double)contentHeightPx / PRINT_DPI;

		// Add 5% buffer to account for rendering differences between screenshot and PDF
		contentHeightInches += 0.5;

		// Total page height = header margin + content + footer margin
		double totalHeightInches = HEADER_MARGIN_INCHES + contentHeightInches + FOOTER_MARGIN_INCHES;

		log.debug("Generating PDF: {}x{} inches (header={}, content={}px/{}, footer={})", A4_WIDTH_INCHES, totalHeightInches,
			HEADER_MARGIN_INCHES, contentHeightPx, contentHeightInches, FOOTER_MARGIN_INCHES);

		HttpResponse<byte[]> response = Unirest.post(gotenbergUrl + "/forms/chromium/convert/html")
			.field("files", new ByteArrayInputStream(mainHtml.getBytes(StandardCharsets.UTF_8)), "index.html")
			.field("files", new ByteArrayInputStream(headerHtml.getBytes(StandardCharsets.UTF_8)), "header.html")
			.field("files", new ByteArrayInputStream(footerHtml.getBytes(StandardCharsets.UTF_8)), "footer.html")
			.field("paperWidth", String.valueOf(A4_WIDTH_INCHES)).field("paperHeight", String.valueOf(totalHeightInches))
			.field("marginTop", HEADER_MARGIN).field("marginBottom", FOOTER_MARGIN).field("marginLeft", "0")
			.field("marginRight", "0").field("printBackground", "true").field("preferCssPageSize", "false").field("scale", "1")
			.asBytes();

		if (!response.isSuccess())
		{
			throw new RuntimeException(
				"PDF generation failed: " + response.getStatus() + " - " + new String(response.getBody(), StandardCharsets.UTF_8));
		}

		return response.getBody();
	}

	/**
	 * Builds the Velocity context with all required variables.
	 *
	 * @param product
	 *            the product data
	 * @param format
	 *            the output format
	 * @return map of context variables for Velocity template
	 */
	private Map<String, Object> buildContext(ProductSheet product, ProductSheetFormat format)
	{
		var context = new HashMap<String, Object>();

		// Product data
		context.put("product", product);

		// Format-specific flags
		context.put("isFullLength", format == ProductSheetFormat.FULL_LENGTH);

		ContextUtil.addCommonValues(context);

		return context;
	}

	/**
	 * Renders a Velocity template from the given path.
	 *
	 * @param templatePath
	 *            the template resource path
	 * @param context
	 *            the Velocity context
	 * @return rendered HTML string
	 */
	private String renderVelocityTemplate(String templatePath, Map<String, Object> context)
	{
		try (var textTemplate = new PackageTextTemplate(ProductSheetPdfGenerator.class, templatePath);
			var reader = new StringReader(textTemplate.asString());
			var writer = new StringWriter())
		{
			var velocityContext = new VelocityContext(context);
			Velocity.evaluate(velocityContext, writer, templatePath, reader);
			return writer.toString();
		}
		catch (Exception e)
		{
			log.error("Failed to render template: {}", templatePath, e);
			throw new RuntimeException("Failed to render template: " + templatePath + " - " + e.getMessage(), e);
		}
	}

}
