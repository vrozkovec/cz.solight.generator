package cz.solight.generator.xmltopdf.wicket.pages;

import static name.berries.app.guice.GuiceStaticHolder.getInstance;

import java.nio.file.Path;

import org.apache.wicket.Component;

import cz.solight.generator.xmltopdf.pojo.ProductSheet;
import cz.solight.generator.xmltopdf.pojo.ProductSheetFormat;
import cz.solight.generator.xmltopdf.service.ProductSheetPdfGenerator;
import cz.solight.generator.xmltopdf.service.ProductSheetXmlParser;
import cz.solight.generator.xmltopdf.wicket.components.ParserPanel;
import cz.solight.generator.xmltopdf.wicket.pages.base.BasePage;

import name.berries.wicket.util.app.WicketAppUtil;

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

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// JobAction action = new JobAction();
		// // if (!WicketAppUtil.localMode())
		// JobOneTime.uploadConvertedProductSheets(action);


		if (WicketAppUtil.localMode())
		{
			var parsed = getInstance(ProductSheetXmlParser.class)
				.parse(ProductSheetXmlParser.class.getResourceAsStream("templates/produktove_listy_test.xml"));
			try
			{
				for (ProductSheet productSheet : parsed)
				{
					getInstance(ProductSheetPdfGenerator.class).generatePdf(productSheet, ProductSheetFormat.A4_SHORT,
						Path.of("/data/tmp/ps/" + ProductSheetFormat.A4_SHORT.buildFilename(productSheet.getCode())));

					getInstance(ProductSheetPdfGenerator.class).generatePdf(productSheet, ProductSheetFormat.FULL_LENGTH,
						Path.of("/data/tmp/ps/" + ProductSheetFormat.FULL_LENGTH.buildFilename(productSheet.getCode())));

					break;
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

	}
}
