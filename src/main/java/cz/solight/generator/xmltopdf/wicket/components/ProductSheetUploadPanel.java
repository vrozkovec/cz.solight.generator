package cz.solight.generator.xmltopdf.wicket.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.solight.generator.xmltopdf.scheduler.JobAction;
import cz.solight.generator.xmltopdf.scheduler.JobOneTime;

import name.berries.wicket.notifications.AjaxNotificationPanel;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Type;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome5IconType;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.spinner.SpinnerAjaxLink;

/**
 * Password-protected panel for triggering the product sheet upload job. Displays a password form
 * and, upon correct password entry, shows an Ajax link to trigger the
 * {@code uploadConvertedProductSheets} job with progress indication.
 */
public class ProductSheetUploadPanel extends Panel
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ProductSheetUploadPanel.class);
	private static final String HARDCODED_PASSWORD = "marmen";

	private final Model<String> passwordModel = Model.of("");
	private WebMarkupContainer passwordContainer;
	private WebMarkupContainer actionContainer;

	/**
	 * Constructs a new ProductSheetUploadPanel.
	 *
	 * @param id
	 *            the wicket component id
	 */
	public ProductSheetUploadPanel(String id)
	{
		super(id);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// Password container (visible initially)
		passwordContainer = new WebMarkupContainer("passwordContainer");
		passwordContainer.setOutputMarkupId(true);
		passwordContainer.setOutputMarkupPlaceholderTag(true);
		add(passwordContainer);

		// Password form
		var passwordForm = new Form<Void>("passwordForm");
		passwordContainer.add(passwordForm);

		// Password field
		var passwordField = new PasswordTextField("passwordField", passwordModel);
		passwordField.setRequired(true);
		passwordForm.add(passwordField);

		// Submit button
		passwordForm.add(new AjaxButton("submitButton", passwordForm)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				validatePassword(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxNotificationPanel.notifyAllIfTargetExists();
			}
		});

		// Action container (hidden initially)
		actionContainer = new WebMarkupContainer("actionContainer");
		actionContainer.setOutputMarkupId(true);
		actionContainer.setOutputMarkupPlaceholderTag(true);
		actionContainer.setVisible(false);
		add(actionContainer);

		// Indicating Ajax link for triggering the upload job
		actionContainer.add(new SpinnerAjaxLink<Void>("uploadLink", Type.Outline_Success)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				executeUploadJob(target);
			}

			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				setLabel(() -> "Zkonvertovat produktové listy");
				setIconType(FontAwesome5IconType.upload_s);
			}
		});
	}

	/**
	 * Validates the entered password against the hardcoded password.
	 *
	 * @param target
	 *            the ajax request target
	 */
	private void validatePassword(AjaxRequestTarget target)
	{
		var enteredPassword = passwordModel.getObject();

		if (HARDCODED_PASSWORD.equals(enteredPassword))
		{
			LOG.info("Password validated successfully, showing upload action");
			passwordContainer.setVisible(false);
			actionContainer.setVisible(true);
			target.add(passwordContainer, actionContainer);
			info("Heslo ověřeno. Nyní můžete spustit nahrávání.");
		}
		else
		{
			LOG.warn("Invalid password attempt");
			error("Nesprávné heslo");
		}

		AjaxNotificationPanel.notifyAllIfTargetExists();
	}

	/**
	 * Executes the product sheet upload job and shows feedback.
	 *
	 * @param target
	 *            the ajax request target
	 */
	private void executeUploadJob(AjaxRequestTarget target)
	{
		try
		{
			LOG.info("Starting product sheet upload job");
			JobOneTime.uploadConvertedProductSheets(new JobAction());
			LOG.info("Product sheet upload job completed successfully");
			info("Nahrávání produktových listů bylo úspěšně dokončeno.");
		}
		catch (Exception e)
		{
			LOG.error("Error executing product sheet upload job", e);
			error("Chyba při nahrávání: " + e.getMessage());
		}

		AjaxNotificationPanel.notifyAllIfTargetExists();
	}
}
