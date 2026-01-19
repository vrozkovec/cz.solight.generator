package name.berries.wicket.components;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * label, ktery nazamenuje nepovolene znaky v textu jejich HTML entitami
 *
 * @author Vít Rozkovec, 11.6.2007
 *
 */
public class RawLabel extends Label
{
	/**
	 * Construct.
	 *
	 * @param id
	 * @param label
	 */
	public RawLabel(String id, Serializable label)
	{
		super(id, label);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param value
	 */
	public RawLabel(String id, String value)
	{
		super(id, value);
		setEscapeModelStrings(false);
		setRenderBodyOnly(true);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 * @param model
	 */
	public RawLabel(String id, IModel<?> model)
	{
		super(id, model);
		setEscapeModelStrings(false);
		setRenderBodyOnly(true);
	}

	/**
	 * Construct.
	 *
	 * @param id
	 */
	public RawLabel(String id)
	{
		super(id);
		setEscapeModelStrings(false);
		setRenderBodyOnly(true);
	}
}
