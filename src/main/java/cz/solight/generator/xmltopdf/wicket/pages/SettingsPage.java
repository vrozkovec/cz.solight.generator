package cz.solight.generator.xmltopdf.wicket.pages;

import org.apache.wicket.Component;

import cz.solight.generator.xmltopdf.wicket.components.ProductSheetUploadPanel;
import cz.solight.generator.xmltopdf.wicket.pages.base.BasePage;

/**
 * Home page with navigation links to all available pages.
 */
public class SettingsPage extends BasePage
{

	/**
	 * Creates a new HomePage.
	 */
	public SettingsPage()
	{
		super();
	}

	@Override
	protected Component newContentPanel(String id)
	{
		return new ProductSheetUploadPanel(id);
	}
}
