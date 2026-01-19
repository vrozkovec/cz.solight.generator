package cz.solight.generator.xmltopdf.wicket.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.berries.wicket.notifications.AjaxNotificationPanel;

/**
 * Wicket panel for uploading and parsing food menu Excel files
 */
public class ParserPanel extends Panel
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ParserPanel.class);

	private FileUploadField fileUploadField;

	/**
	 * Constructor for ParserPanel
	 *
	 * @param id
	 *            wicket component id
	 */
	public ParserPanel(String id)
	{
		super(id);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// Create upload form
		var uploadForm = new Form<Void>("uploadForm");
		uploadForm.setMultiPart(true);
		uploadForm.setMaxSize(Bytes.megabytes(10));
		add(uploadForm);

		// Add file upload field
		fileUploadField = new FileUploadField("fileInput");
		uploadForm.add(fileUploadField);

		// Add upload button
		uploadForm.add(new AjaxButton("uploadButton", uploadForm)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				processUploadedFile(target);
				AjaxNotificationPanel.notifyAllIfTargetExists();
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				AjaxNotificationPanel.notifyAllIfTargetExists();
			}
		});
	}

	/**
	 * Process the uploaded Excel file
	 *
	 * @param target
	 *            ajax request target
	 */
	private void processUploadedFile(AjaxRequestTarget target)
	{
		var upload = fileUploadField.getFileUpload();

		if (upload == null)
		{
			error("Prosím vyberte soubor k nahrání");
			AjaxNotificationPanel.notifyAllIfTargetExists();
			return;
		}

		try
		{
			var fileName = upload.getClientFileName();
			LOG.info("Processing uploaded file: {}", fileName);

			info("Soubor byl úspěšně zpracován");

		}
		catch (Exception e)
		{
			LOG.error("Error processing file", e);
			error("Chyba při zpracování souboru: " + e.getMessage());
		}
	}
}