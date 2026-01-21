package cz.solight.generator.xmltopdf.wicket.pages;

import org.apache.wicket.Component;

import cz.solight.generator.xmltopdf.wicket.components.ParserPanel;
import cz.solight.generator.xmltopdf.wicket.pages.base.BasePage;

/**
 * Home page with navigation links to all available pages.
 */
public class HomePage extends BasePage
{

	/**
	 * Creates a new HomePage.
	 */
	public HomePage()
	{
		super();
	}

	@Override
	protected Component newContentPanel(String id)
	{
		return new ParserPanel(id);
	}

	// @Override
	// protected void onInitialize()
	// {
	// super.onInitialize();
	// var parsed = getInstance(ProductSheetXmlParser.class)
	// .parse(ProductSheetXmlParser.class.getResourceAsStream("templates/produktove_listy_test.xml"));
	// try
	// {
	// getInstance(ProductSheetPdfGenerator.class).generatePdf(parsed.get(0),
	// ProductSheetFormat.A4_SHORT,
	// Path.of("/data/tmp/psa4.pdf"));
	// getInstance(ProductSheetPdfGenerator.class).generatePdf(parsed.get(0),
	// ProductSheetFormat.FULL_LENGTH,
	// Path.of("/data/tmp/psfull.pdf"));
	// }
	// catch (Exception e)
	// {
	// throw new RuntimeException(e);
	// }
	//
	// }
}
