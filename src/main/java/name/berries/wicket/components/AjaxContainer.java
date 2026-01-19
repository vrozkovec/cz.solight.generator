/**
 *
 */
package name.berries.wicket.components;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * {@link WebMarkupContainer} ma zapnute {@link #setOutputMarkupId(boolean)} a navic ma
 * konstruktory, ktere umozni rovnou ID nastavit.
 *
 * @author rozkovec
 *
 * @see Component#setVisible(boolean)
 */
public class AjaxContainer extends WebMarkupContainer
{

	/**
	 * @param id
	 */
	public AjaxContainer(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param model
	 */
	public AjaxContainer(String id, IModel<?> model)
	{
		super(id, model);
	}


	/**
	 * @param id
	 * @param cssId
	 */
	public AjaxContainer(String id, String cssId)
	{
		super(id);
		setMarkupId(cssId);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param model
	 * @param cssId
	 */
	public AjaxContainer(String id, IModel<?> model, String cssId)
	{
		super(id, model);
		setMarkupId(cssId);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		setOutputMarkupPlaceholderTag(true);
	}

}
