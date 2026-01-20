package name.berries.pdf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Margin;

/**
 * Service class that handles PDF generation using Playwright with Chromium browser.
 *
 * This service provides methods to convert HTML content or URLs to PDF files with configurable
 * options for page format, margins, and rendering behavior.
 */
public class PdfGeneratorService
{
	private static final Logger logger = LoggerFactory.getLogger(PdfGeneratorService.class);

	/**
	 * Generates a PDF from HTML content string.
	 *
	 * @param htmlContent
	 *            The HTML content to convert
	 * @param outputPath
	 *            The output PDF file path
	 * @param options
	 *            PDF generation options
	 * @throws Exception
	 *             if PDF generation fails
	 */
	public void generatePdfFromHtml(String htmlContent, String outputPath, PdfOptions options) throws Exception
	{
		logger.info("Generating PDF from HTML content to: {}", outputPath);

		try (Playwright playwright = Playwright.create())
		{
			BrowserType chromium = playwright.chromium();

			try (Browser browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true)
				.setArgs(java.util.List.of("--no-sandbox", "--disable-dev-shm-usage"))))
			{

				Page page = browser.newPage();

				// Set HTML content
				page.setContent(htmlContent);

				// Load external stylesheets
				loadStylesheets(page, options);

				// Wait for content to load
				waitForContent(page, options.getWaitFor());

				// Generate PDF
				generatePdf(page, outputPath, options);

				logger.info("PDF generated successfully");
			}
		}
	}

	/**
	 * Generates a PDF from a URL.
	 *
	 * @param url
	 *            The URL to convert
	 * @param outputPath
	 *            The output PDF file path
	 * @param options
	 *            PDF generation options
	 * @throws Exception
	 *             if PDF generation fails
	 */
	public void generatePdfFromUrl(String url, String outputPath, PdfOptions options) throws Exception
	{
		logger.info("Generating PDF from URL: {} to: {}", url, outputPath);

		try (Playwright playwright = Playwright.create())
		{
			BrowserType chromium = playwright.chromium();

			try (Browser browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true)
				.setArgs(java.util.List.of("--no-sandbox", "--disable-dev-shm-usage"))))
			{

				Page page = browser.newPage();

				// Navigate to URL
				page.navigate(url);

				// Load external stylesheets
				loadStylesheets(page, options);

				// Wait for content to load
				waitForContent(page, options.getWaitFor());

				// Generate PDF
				generatePdf(page, outputPath, options);

				logger.info("PDF generated successfully");
			}
		}
	}

	/**
	 * Waits for page content to be ready based on the wait condition.
	 */
	private void waitForContent(Page page, String waitFor)
	{
		if (StringUtils.isBlank(waitFor))
		{
			waitFor = "networkidle";
		}

		logger.debug("Waiting for content with condition: {}", waitFor);

		try
		{
			if ("networkidle".equals(waitFor))
			{
				page.waitForLoadState(LoadState.NETWORKIDLE);
			}
			else if ("load".equals(waitFor))
			{
				page.waitForLoadState(LoadState.LOAD);
			}
			else
			{
				// Try to parse as timeout in milliseconds
				try
				{
					int timeoutMs = Integer.parseInt(waitFor);
					page.waitForTimeout(timeoutMs);
				}
				catch (NumberFormatException e)
				{
					logger.warn("Invalid wait condition '{}', using networkidle instead", waitFor);
					page.waitForLoadState(LoadState.NETWORKIDLE);
				}
			}
		}
		catch (Exception e)
		{
			logger.warn("Error waiting for content: {}", e.getMessage());
			// Continue with PDF generation anyway
		}
	}

	/**
	 * Loads external stylesheets into the page. These stylesheets will be available to both main
	 * content and header/footer templates.
	 */
	private void loadStylesheets(Page page, PdfOptions options)
	{
		if (options.getStylesheetUrls() == null || options.getStylesheetUrls().isEmpty())
		{
			return;
		}

		logger.debug("Loading {} external stylesheets", options.getStylesheetUrls().size());

		for (String stylesheetUrl : options.getStylesheetUrls())
		{
			try
			{
				logger.debug("Loading stylesheet: {}", stylesheetUrl);
				page.addStyleTag(new Page.AddStyleTagOptions().setUrl(stylesheetUrl));
				logger.debug("Successfully loaded stylesheet: {}", stylesheetUrl);
			}
			catch (Exception e)
			{
				logger.warn("Failed to load stylesheet {}: {}", stylesheetUrl, e.getMessage());
				// Continue with other stylesheets
			}
		}
	}

	/**
	 * Generates the PDF with the specified options.
	 */
	private void generatePdf(Page page, String outputPath, PdfOptions options) throws Exception
	{
		// Ensure output directory exists
		Path outputFilePath = Paths.get(outputPath);
		Path outputDir = outputFilePath.getParent();
		if (outputDir != null && !Files.exists(outputDir))
		{
			Files.createDirectories(outputDir);
		}

		// Configure PDF options
		Page.PdfOptions playwrightPdfOptions = new Page.PdfOptions();

		// Set format
		playwrightPdfOptions.setFormat(options.getFormat());

		// Set orientation
		playwrightPdfOptions.setLandscape(options.isLandscape());

		if (options.getWidth() != null)
			playwrightPdfOptions.setWidth(options.getWidth());

		if (options.getHeight() != null)
			playwrightPdfOptions.setHeight(options.getHeight());

		// Set margins
		Margin margin = new Margin().setTop(options.getMarginTop()).setBottom(options.getMarginBottom())
			.setLeft(options.getMarginLeft()).setRight(options.getMarginRight());
		playwrightPdfOptions.setMargin(margin);

		// Set print background
		playwrightPdfOptions.setPrintBackground(options.isPrintBackground());

		// Set header and footer templates
		if (options.isDisplayHeaderFooter())
		{
			playwrightPdfOptions.setDisplayHeaderFooter(true);
			if (options.getHeaderTemplate() != null)
			{
				playwrightPdfOptions.setHeaderTemplate(options.getHeaderTemplate());
			}
			if (options.getFooterTemplate() != null)
			{
				playwrightPdfOptions.setFooterTemplate(options.getFooterTemplate());
			}
		}

		// Set output path
		playwrightPdfOptions.setPath(outputFilePath);

		logger.debug("PDF options: format={}, landscape={}, printBackground={}, margins={}|{}|{}|{}", options.getFormat(),
			options.isLandscape(), options.isPrintBackground(), options.getMarginTop(), options.getMarginRight(),
			options.getMarginBottom(), options.getMarginLeft());

		// Generate PDF
		page.pdf(playwrightPdfOptions);
	}
}