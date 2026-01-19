package name.berries.wicket.util.css;

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * Trida pro praci s CSS.
 *
 * @author rozkovec
 *
 */
public class Css
{

	/**
	 * Nastavi dany tagu na patricnou hodnotu.
	 *
	 * @param attribute
	 * @param value
	 * @return {@link AttributeModifier}
	 */
	public static AttributeModifier getModifier(String attribute, Serializable value)
	{
		return new AttributeModifier(attribute, value);
	}

	/**
	 * Nastavi dany tagu na patricnou hodnotu.
	 *
	 * @param attribute
	 * @param replaceModel
	 * @return {@link AttributeModifier}
	 */
	public static AttributeModifier getModifier(String attribute, IModel<?> replaceModel)
	{
		return new AttributeModifier(attribute, replaceModel);
	}

	/**
	 * Prida dany k danemu tagu danou hodnotu.
	 *
	 * @param attribute
	 * @param value
	 * @return {@link AttributeAppender}
	 */
	public static AttributeAppender getAppender(String attribute, String value)
	{
		return new AttributeAppender(attribute, Model.of(value), " ");
	}

	/**
	 * Prida dany k danemu tagu danou hodnotu.
	 *
	 * @param attribute
	 *            tag
	 * @param appendModel
	 *            hodnota
	 * @return {@link AttributeAppender}
	 */
	public static AttributeAppender getAppender(String attribute, IModel<?> appendModel)
	{
		return new AttributeAppender(attribute, appendModel, " ");
	}

	/**
	 * Prida k tagu "class" danou hodnotu.
	 *
	 * @param cssClass
	 *            text, ktery bude k parametru pridan
	 * @return css appender
	 */
	public static AttributeAppender getClassAppender(String cssClass)
	{
		return getClassAppender(new Model<>(cssClass));
	}

	/**
	 * Prida k tagu "class" danou hodnotu.
	 *
	 * @param cssClassModel
	 *            text, ktery bude k parametru pridan
	 * @return css appender
	 */
	public static AttributeAppender getClassAppender(IModel<String> cssClassModel)
	{
		return new AttributeAppender("class", cssClassModel, " ");
	}

	/**
	 * Nastavi k tagu "title" danou hodnotu, pokud jiz nejakou hodnotu mel, prepise ji.
	 *
	 * @param title
	 *            text, ktery bude k parametru pridan
	 * @return css modifier
	 */
	public static AttributeModifier getTitleModifier(String title)
	{
		return getTitleModifier(new Model<>(title));
	}

	/**
	 * Nastavi k tagu "title" danou hodnotu, pokud jiz nejakou hodnotu mel, prepise ji.
	 *
	 * @param titleModel
	 *            text, ktery bude k parametru pridan
	 * @return css modifier
	 */
	public static AttributeModifier getTitleModifier(IModel<String> titleModel)
	{
		return new AttributeModifier("title", titleModel);
	}

	/**
	 * Nastavi k tagu "style" danou hodnotu, pokud jiz nejakou hodnotu mel, prepise ji.
	 *
	 * @param style
	 *            text, ktery bude k parametru pridan
	 * @return css modifier
	 */
	public static AttributeModifier getStyleModifier(String style)
	{
		return getStyleModifier(new Model<>(style));
	}

	/**
	 * Nastavi k tagu "style" danou hodnotu, pokud jiz nejakou hodnotu mel, prepise ji.
	 *
	 * @param styleModel
	 *            text, ktery bude k parametru pridan
	 * @return css modifier
	 */
	public static AttributeModifier getStyleModifier(IModel<String> styleModel)
	{
		return new AttributeModifier("style", styleModel);
	}

	/**
	 * Nastavi k tagu "class" danou hodnotu, pokud jiz nejakou hodnotu mel, prepise ji.
	 *
	 * @param cssClass
	 *            text, ktery bude k parametru pridan
	 * @return css modifier
	 */
	public static AttributeModifier getClassModifier(String cssClass)
	{
		return getClassModifier(new Model<>(cssClass));
	}

	/**
	 * Nastavi k tagu "class" danou hodnotu, pokud jiz nejakou hodnotu mel, prepise ji.
	 *
	 * @param cssClassModel
	 *            text, ktery bude k parametru pridan
	 * @return css modifier
	 */
	public static AttributeModifier getClassModifier(IModel<String> cssClassModel)
	{
		return new AttributeModifier("class", cssClassModel);
	}


	/**
	 * Odebere z tagu "class" danou hodnotu.
	 *
	 * @param cssClassValue
	 *            hodnota k odebrani
	 * @return css modifier
	 */
	public static AttributeModifier getClassValueRemover(String cssClassValue)
	{
		return new CssClassRemover(cssClassValue);
	}


	/**
	 * Prida css soubor do stranky. Soubor musi existovat ve stejnem baliku a musi mit shodny nazev
	 * jako trida v parametru.
	 *
	 * @param clazz
	 * @return header contributor
	 */
	public static CssReferenceBehavior forClass(Class<? extends Object> clazz)
	{
		return new CssReferenceBehavior(new CssResourceReference(clazz, clazz.getSimpleName() + ".css"));
	}

	/**
	 * Returns css header item to be added in
	 * {@link Component#renderHead(org.apache.wicket.markup.head.IHeaderResponse)}
	 *
	 * @param clazz
	 * @return header contributor
	 */
	// TODO store instances somewhere to prevent repeated instantiations?
	public static CssReferenceHeaderItem headerItemforClass(Class<? extends Object> clazz)
	{
		return CssHeaderItem.forReference(new CssResourceReference(clazz, clazz.getSimpleName() + ".css"));
	}

	/**
	 * Prida css soubor do stranky. Soubor musi existovat ve stejnem baliku a musi mit shodny nazev
	 * jako trida v parametru. Format souboru musi byt v tomto pripade ClassName.version.css.
	 *
	 * @param clazz
	 * @param version
	 * @return header contributor
	 */
	public static CssReferenceBehavior forClass(Class<? extends Object> clazz, int version)
	{
		return new CssReferenceBehavior(new CssResourceReference(clazz, clazz.getSimpleName() + "." + version + ".css"));
	}

	/**
	 * Prida css soubor do stranky. Soubor musi existovat ve stejnem baliku jako třída.
	 *
	 * @param clazz
	 * @param name
	 * @return header contributor
	 */
	public static CssReferenceBehavior forClass(Class<? extends Object> clazz, String name)
	{
		return new CssReferenceBehavior(new CssResourceReference(clazz, name));
	}

	/**
	 * Css s tridami k vseobecnemu pouziti.
	 *
	 * @return header contributor
	 */
	public static CssReferenceBehavior getCommonCss()
	{
		return new CssReferenceBehavior(new CssResourceReference(Css.class, "common.min.css"));
	}
}
