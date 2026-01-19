/**
 *
 */
package cz.solight.generator.xmltopdf.wicket.pages.base;

import java.util.Date;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.EnclosureContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import name.berries.wicket.behaviors.nprogress.NprogressBehavior;
import name.berries.wicket.components.InvisibleLabel;
import name.berries.wicket.notifications.BootstrapNotifyAjaxPanel;
import name.berries.wicket.util.css.Css;

import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.util.CssClassNames;
import de.agilecoders.wicket.core.util.CssClassNames.Grid;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome5CssReference;

/**
 * Base setup page with common layout elements.
 */
public abstract class SetupPage extends WebPage
{
	/**
	 *
	 */
	protected SetupPage()
	{
	}

	/**
	 * @param parameters
	 */
	protected SetupPage(PageParameters parameters)
	{
		super(parameters);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		TransparentWebMarkupContainer body = new TransparentWebMarkupContainer("body");
		add(body);
		if (getBodyCssClass() != null)
		{
			body.add(Css.getClassAppender(getBodyCssClass()));
		}

		add(newNavbar("navbar"));

		add(new NprogressBehavior());
		add(new BootstrapNotifyAjaxPanel("topfeedback"));

		add(new TransparentWebMarkupContainer("container").add(Css.getClassModifier(getContainerCssClass()))
			.add(Css.getClassAppender("main-content")));

		add(new HeaderResponseContainer("footer-container", "footer-container"));

		InvisibleLabel titleLabel = new InvisibleLabel("title", getPageTitleModel());
		add(new EnclosureContainer("titleEnclosure", titleLabel).add(titleLabel));
	}

	protected String getBodyCssClass()
	{
		return "";
	}

	protected IModel<String> getPageTitleModel()
	{
		return null;
	}

	/**
	 * @return css class for container
	 * @see Grid
	 */
	protected String getContainerCssClass()
	{
		return CssClassNames.Grid.container;
	}

	protected abstract Navbar newNavbar(String string);

	private static final String cssVersion = new Date().getTime() + "";

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(new PriorityHeaderItem(
			JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference())));

		response.render(CssHeaderItem.forReference(FontAwesome5CssReference.instance()));
	}


	/**
	 * @param component
	 * @return page
	 */
	public static SetupPage get(Component component)
	{
		return (SetupPage)component.getPage();
	}
}