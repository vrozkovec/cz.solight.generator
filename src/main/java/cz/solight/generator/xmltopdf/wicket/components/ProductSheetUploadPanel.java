package cz.solight.generator.xmltopdf.wicket.components;

import java.time.Duration;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.solight.generator.xmltopdf.scheduler.JobAction;
import cz.solight.generator.xmltopdf.scheduler.JobOneTime;

import name.berries.wicket.behaviors.nprogress.NprogressBehavior;
import name.berries.wicket.notifications.AjaxNotificationPanel;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Type;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome5IconType;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.spinner.SpinnerAjaxLink;

/**
 * Password-protected panel for triggering the product sheet upload job. Displays a password form
 * and, upon correct password entry, shows an Ajax link to trigger the
 * {@code uploadConvertedProductSheets} job with progress indication via a determinate progress bar.
 */
public class ProductSheetUploadPanel extends Panel
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ProductSheetUploadPanel.class);
	private static final String HARDCODED_PASSWORD = "marmen";
	private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

	private final Model<String> passwordModel = Model.of("");
	private WebMarkupContainer passwordContainer;
	private WebMarkupContainer actionContainer;
	private WebMarkupContainer progressContainer;
	private WebMarkupContainer progressBar;
	private Label progressText;
	private SpinnerAjaxLink<Void> uploadLink;
	private AjaxSelfUpdatingTimerBehavior timerBehavior;
	private String sessionId;

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
		sessionId = Session.get().getId();

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
		uploadLink = new SpinnerAjaxLink<>("uploadLink", Type.Outline_Success)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				startUploadJob(target);
			}

			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				setLabel(() -> "Zkonvertovat produktové listy");
				setIconType(FontAwesome5IconType.upload_s);
			}
		};
		uploadLink.setOutputMarkupId(true);
		uploadLink.setOutputMarkupPlaceholderTag(true);
		actionContainer.add(uploadLink);

		// Progress container (hidden initially)
		progressContainer = new WebMarkupContainer("progressContainer");
		progressContainer.setOutputMarkupId(true);
		progressContainer.setOutputMarkupPlaceholderTag(true);
		progressContainer.setVisible(false);
		actionContainer.add(progressContainer);

		// Progress bar
		progressBar = new WebMarkupContainer("progressBar");
		progressBar.setOutputMarkupId(true);
		progressContainer.add(progressBar);

		// Progress text
		progressText = new Label("progressText", Model.of(""));
		progressText.setOutputMarkupId(true);
		progressContainer.add(progressText);
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
	 * Starts the upload job in a background thread and initializes progress tracking.
	 *
	 * @param target
	 *            the ajax request target
	 */
	private void startUploadJob(AjaxRequestTarget target)
	{
		LOG.info("Starting product sheet upload job with progress tracking");

		// Ensure session is bound and get its ID
		Session.get().bind();
		sessionId = Session.get().getId();

		// Initialize progress
		UploadProgressHolder.update(sessionId, UploadProgress.initial());

		// Hide upload button, show progress container
		uploadLink.setVisible(false);
		progressContainer.setVisible(true);
		updateProgressDisplay(UploadProgress.initial());
		target.add(uploadLink, progressContainer);

		// Add timer behavior for polling progress
		timerBehavior = new AjaxSelfUpdatingTimerBehavior(POLL_INTERVAL)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.getExtraParameters().put(NprogressBehavior.SKIP_NPROGRESS_ATTRIBUTE_KEY, true);
			}

			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target)
			{
				var progress = UploadProgressHolder.get(sessionId);
				updateProgressDisplay(progress);

				if (progress.completed())
				{
					handleCompletion(target, progress);
				}
				else if (progress.error() != null)
				{
					handleError(target, progress);
				}
			}
		};
		progressContainer.add(timerBehavior);
		Application application = Application.get();
		// Start job in background thread
		new Thread(() -> {
			try
			{
				ThreadContext.setApplication(application);
				JobOneTime.uploadConvertedProductSheets(new JobAction(),
					progress -> UploadProgressHolder.update(sessionId, progress));
			}
			catch (Exception e)
			{
				LOG.error("Error executing product sheet upload job", e);
				UploadProgressHolder.update(sessionId, UploadProgress.failed(e.getMessage()));
			}
		}, "ProductSheetUpload-" + sessionId).start();
	}

	/**
	 * Updates the progress bar and text display based on current progress.
	 *
	 * @param progress
	 *            the current progress state
	 */
	private void updateProgressDisplay(UploadProgress progress)
	{
		int percentage = progress.getPercentage();
		progressBar.add(AttributeModifier.replace("style", "width: " + percentage + "%"));
		progressBar.add(AttributeModifier.replace("aria-valuenow", String.valueOf(percentage)));

		String result;
		if (progress.completed())
		{
			result = "Dokončeno: " + progress.total() + " produktů zpracováno";
		}
		else if (progress.error() != null)
		{
			result = "Chyba: " + progress.error();
		}
		else if (progress.total() > 0)
		{
			result = "Zpracovávám: " + progress.current() + " / " + progress.total();
			if (progress.currentProductCode() != null)
			{
				result += " (" + progress.currentProductCode() + ")";
			}
		}
		else
		{
			result = "Načítám produkty...";
		}
		progressText.setDefaultModelObject(result);
	}

	/**
	 * Handles successful completion of the upload job.
	 *
	 * @param target
	 *            the ajax request target
	 * @param progress
	 *            the final progress state
	 */
	private void handleCompletion(AjaxRequestTarget target, UploadProgress progress)
	{
		LOG.info("Product sheet upload job completed successfully: {} products", progress.total());
		stopTimer();

		// Update progress bar to success state
		progressBar.add(AttributeModifier.replace("class", "progress-bar bg-success"));
		target.add(progressBar);

		info("Nahrávání produktových listů bylo úspěšně dokončeno. Zpracováno produktů: " + progress.total());
		AjaxNotificationPanel.notifyAllIfTargetExists();
	}

	/**
	 * Handles error during the upload job.
	 *
	 * @param target
	 *            the ajax request target
	 * @param progress
	 *            the progress state containing the error
	 */
	private void handleError(AjaxRequestTarget target, UploadProgress progress)
	{
		LOG.error("Product sheet upload job failed: {}", progress.error());
		stopTimer();

		// Update progress bar to error state
		progressBar.add(AttributeModifier.replace("class", "progress-bar bg-danger"));
		target.add(progressBar, progressText);

		// Show upload button again for retry
		uploadLink.setVisible(true);
		target.add(uploadLink);

		error("Chyba při nahrávání: " + progress.error());
		AjaxNotificationPanel.notifyAllIfTargetExists();
	}

	/**
	 * Stops the timer behavior.
	 */
	private void stopTimer()
	{
		if (timerBehavior != null)
		{
			timerBehavior.stop(null);
		}
	}

	@Override
	protected void onRemove()
	{
		super.onRemove();
		// Cleanup progress holder when panel is removed from page
		if (sessionId != null)
		{
			UploadProgressHolder.remove(sessionId);
		}
	}
}
