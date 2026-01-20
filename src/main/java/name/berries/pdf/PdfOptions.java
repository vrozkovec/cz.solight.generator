package name.berries.pdf;

/**
 * Configuration class for PDF generation options.
 *
 * Encapsulates all the settings needed to customize PDF output including page format, margins,
 * orientation, and rendering behavior.
 */
public class PdfOptions
{

	private String format = "A4";
	private String width = null;
	private String height = null;
	private boolean landscape = false;
	private String marginTop = "0";
	private String marginBottom = "0";
	private String marginLeft = "0";
	private String marginRight = "0";
	private boolean printBackground = true;
	private String waitFor = "networkidle";
	private java.util.List<String> stylesheetUrls = new java.util.ArrayList<>();
	private String headerTemplate = null;
	private String footerTemplate = null;
	private boolean displayHeaderFooter = false;

	/**
	 * Default constructor with sensible defaults.
	 */
	public PdfOptions()
	{
		// Default values are set in field declarations
	}

	/**
	 * Gets the PDF page format (e.g., A4, A3, Letter, Legal).
	 */
	public String getFormat()
	{
		return format;
	}

	/**
	 * Sets the PDF page format.
	 *
	 * @param format
	 *            Page format (A4, A3, Letter, Legal, Tabloid, Ledger, or custom size like
	 *            "100mm:200mm")
	 */
	public void setFormat(String format)
	{
		this.format = format;
	}

	/**
	 * Gets width.
	 *
	 * @return width
	 */
	public String getWidth()
	{
		return width;
	}

	/**
	 * Sets width.
	 *
	 * @param width
	 *            width
	 */
	public void setWidth(String width)
	{
		this.width = width;
	}

	/**
	 * Gets height.
	 *
	 * @return height
	 */
	public String getHeight()
	{
		return height;
	}

	/**
	 * Sets height.
	 *
	 * @param height
	 *            height
	 */
	public void setHeight(String height)
	{
		this.height = height;
	}

	/**
	 * Checks if landscape orientation is enabled.
	 */
	public boolean isLandscape()
	{
		return landscape;
	}

	/**
	 * Sets landscape orientation.
	 *
	 * @param landscape
	 *            true for landscape, false for portrait
	 */
	public void setLandscape(boolean landscape)
	{
		this.landscape = landscape;
	}

	/**
	 * Gets the top margin.
	 */
	public String getMarginTop()
	{
		return marginTop;
	}

	/**
	 * Sets the top margin.
	 *
	 * @param marginTop
	 *            Margin size (e.g., "10mm", "1in", "20px")
	 */
	public void setMarginTop(String marginTop)
	{
		this.marginTop = marginTop;
	}

	/**
	 * Gets the bottom margin.
	 */
	public String getMarginBottom()
	{
		return marginBottom;
	}

	/**
	 * Sets the bottom margin.
	 *
	 * @param marginBottom
	 *            Margin size (e.g., "10mm", "1in", "20px")
	 */
	public void setMarginBottom(String marginBottom)
	{
		this.marginBottom = marginBottom;
	}

	/**
	 * Gets the left margin.
	 */
	public String getMarginLeft()
	{
		return marginLeft;
	}

	/**
	 * Sets the left margin.
	 *
	 * @param marginLeft
	 *            Margin size (e.g., "10mm", "1in", "20px")
	 */
	public void setMarginLeft(String marginLeft)
	{
		this.marginLeft = marginLeft;
	}

	/**
	 * Gets the right margin.
	 */
	public String getMarginRight()
	{
		return marginRight;
	}

	/**
	 * Sets the right margin.
	 *
	 * @param marginRight
	 *            Margin size (e.g., "10mm", "1in", "20px")
	 */
	public void setMarginRight(String marginRight)
	{
		this.marginRight = marginRight;
	}

	/**
	 * Checks if background colors and images should be printed.
	 */
	public boolean isPrintBackground()
	{
		return printBackground;
	}

	/**
	 * Sets whether to print background colors and images.
	 *
	 * @param printBackground
	 *            true to include backgrounds, false to omit them
	 */
	public void setPrintBackground(boolean printBackground)
	{
		this.printBackground = printBackground;
	}

	/**
	 * Gets the wait condition for content loading.
	 */
	public String getWaitFor()
	{
		return waitFor;
	}

	/**
	 * Sets the wait condition for content loading.
	 *
	 * @param waitFor
	 *            Wait condition: "networkidle", "load", or timeout in milliseconds
	 */
	public void setWaitFor(String waitFor)
	{
		this.waitFor = waitFor;
	}

	/**
	 * Gets the list of external stylesheet URLs to load.
	 */
	public java.util.List<String> getStylesheetUrls()
	{
		return stylesheetUrls;
	}

	/**
	 * Adds an external stylesheet URL that will be loaded before PDF generation. These stylesheets
	 * will be available to both main content and header/footer templates.
	 *
	 * @param stylesheetUrl
	 *            URL to CSS file (http://, https://, or file:// URL)
	 */
	public void addStylesheetUrl(String stylesheetUrl)
	{
		stylesheetUrls.add(stylesheetUrl);
	}

	/**
	 * Sets multiple external stylesheet URLs.
	 *
	 * @param stylesheetUrls
	 *            List of CSS URLs
	 */
	public void setStylesheetUrls(java.util.List<String> stylesheetUrls)
	{
		this.stylesheetUrls = stylesheetUrls;
	}

	/**
	 * Gets the header template HTML.
	 */
	public String getHeaderTemplate()
	{
		return headerTemplate;
	}

	/**
	 * Sets the header template HTML. Use special CSS classes: pageNumber, totalPages, url, title,
	 * date
	 *
	 * @param headerTemplate
	 *            HTML template for header
	 */
	public void setHeaderTemplate(String headerTemplate)
	{
		this.headerTemplate = headerTemplate;
	}

	/**
	 * Gets the footer template HTML.
	 */
	public String getFooterTemplate()
	{
		return footerTemplate;
	}

	/**
	 * Sets the footer template HTML. Use special CSS classes: pageNumber, totalPages, url, title,
	 * date
	 *
	 * @param footerTemplate
	 *            HTML template for footer
	 */
	public void setFooterTemplate(String footerTemplate)
	{
		this.footerTemplate = footerTemplate;
	}

	/**
	 * Checks if header and footer display is enabled.
	 */
	public boolean isDisplayHeaderFooter()
	{
		return displayHeaderFooter;
	}

	/**
	 * Enables or disables header and footer display. Must be true to show headerTemplate and
	 * footerTemplate.
	 *
	 * @param displayHeaderFooter
	 *            true to show headers/footers
	 */
	public void setDisplayHeaderFooter(boolean displayHeaderFooter)
	{
		this.displayHeaderFooter = displayHeaderFooter;
	}

	/**
	 * Sets all margins to the same value.
	 *
	 * @param margin
	 *            Margin size for all sides (e.g., "10mm", "1in", "20px")
	 */
	public void setMargin(String margin)
	{
		marginTop = margin;
		marginBottom = margin;
		marginLeft = margin;
		marginRight = margin;
	}

	/**
	 * Sets margins with individual values for each side.
	 *
	 * @param top
	 *            Top margin
	 * @param right
	 *            Right margin
	 * @param bottom
	 *            Bottom margin
	 * @param left
	 *            Left margin
	 */
	public void setMargins(String top, String right, String bottom, String left)
	{
		marginTop = top;
		marginRight = right;
		marginBottom = bottom;
		marginLeft = left;
	}

	@Override
	public String toString()
	{
		return "PdfOptions{" + "format='" + format + '\'' + ", landscape=" + landscape + ", marginTop='" + marginTop + '\''
			+ ", marginBottom='" + marginBottom + '\'' + ", marginLeft='" + marginLeft + '\'' + ", marginRight='" + marginRight
			+ '\'' + ", printBackground=" + printBackground + ", waitFor='" + waitFor + '\'' + ", stylesheetUrls=" + stylesheetUrls
			+ ", headerTemplate='" + headerTemplate + '\'' + ", footerTemplate='" + footerTemplate + '\'' + ", displayHeaderFooter="
			+ displayHeaderFooter + '}';
	}
}