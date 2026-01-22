package cz.solight.generator.xmltopdf.wicket.pages;

import org.apache.wicket.Component;

import cz.solight.generator.xmltopdf.wicket.components.ProductSheetUploadPanel;
import cz.solight.generator.xmltopdf.wicket.pages.base.BasePage;

/**
 * Page for uploading product sheets. Protected by password authentication.
 */
public class ProductSheetUploadPage extends BasePage
{

	/**
	 * Creates a new ProductSheetUploadPage.
	 */
	public ProductSheetUploadPage()
	{
		super();
	}

	@Override
	protected Component newContentPanel(String id)
	{
		return new ProductSheetUploadPanel(id);
	}
}
