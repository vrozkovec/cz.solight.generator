package cz.solight.generator.xmltopdf.wicket.components;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.FileResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import cz.solight.generator.xmltopdf.pojo.PdfDisplayOptions;
import cz.solight.generator.xmltopdf.pojo.PdfLocale;
import cz.solight.generator.xmltopdf.service.OfferPdfGenerator;
import cz.solight.generator.xmltopdf.service.XmlOfferParser;

import name.berries.wicket.components.YesNoCheckBox;
import name.berries.wicket.notifications.AjaxNotificationPanel;
import name.berries.wicket.util.app.WicketAppUtil;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.ButtonGroup;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Type;

/**
 * Wicket panel for uploading XML offer files and generating PDF catalogs. Provides checkboxes to
 * control which prices are displayed in the output.
 */
public class ParserPanel extends Panel
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ParserPanel.class);
	private static final String PDF_OUTPUT_DIR = "/data/tmp/";

	@Inject
	private XmlOfferParser xmlOfferParser;

	@Inject
	private OfferPdfGenerator offerPdfGenerator;

	private FileUploadField fileUploadField;
	private Model<Boolean> showPriceMyModel = Model.of(true);
	private Model<Boolean> showPriceVOCModel = Model.of(true);
	private Model<Boolean> showPriceMOCModel = Model.of(true);
	private Model<PdfLocale> localeModel = Model.of(PdfLocale.CZ);

	/**
	 * Constructor for ParserPanel.
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

		uploadForm.add(new ButtonGroup("locale")
		{
			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				setOutputMarkupId(true);
			}

			@Override
			protected List<AbstractLink> newButtons(String buttonMarkupId)
			{
				List<AbstractLink> list = new ArrayList<>();

				PdfLocale[] locales = PdfLocale.values();
				for (PdfLocale locale : locales)
				{
					list.add(new BootstrapAjaxLink<PdfLocale>(buttonMarkupId, Model.of(locale), Type.Default)
					{
						@Override
						public void onClick(AjaxRequestTarget target)
						{
							localeModel.setObject(getModelObject());
							target.add(findParent(ButtonGroup.class));
						}

						@Override
						protected void onConfigure()
						{
							super.onConfigure();
							if (localeModel.getObject().equals(getModelObject()))
							{
								setType(Type.Success);
							}
							else
							{
								setType(Type.Outline_Dark);
							}
						}

						@Override
						protected void onInitialize()
						{
							super.onInitialize();
							String label = (String)new EnumChoiceRenderer<PdfLocale>(this).getDisplayValue(getModelObject());
							setLabel(() -> label);
						}

					});
				}
				return list;
			}
		});

		// Add price display checkboxes
		uploadForm.add(new YesNoCheckBox("showPriceMy", showPriceMyModel));
		uploadForm.add(new YesNoCheckBox("showPriceVOC", showPriceVOCModel));
		uploadForm.add(new YesNoCheckBox("showPriceMOC", showPriceMOCModel));

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
	 * Process the uploaded XML file and generate PDF.
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

			// Parse XML
			var offer = xmlOfferParser.parse(upload.getInputStream());
			LOG.info("Parsed offer {} with {} products", offer.getDocNumber(), offer.getProducts().size());

			// Build display options from checkboxes and locale selection
			var displayOptions = new PdfDisplayOptions(Boolean.TRUE.equals(showPriceMyModel.getObject()),
				Boolean.TRUE.equals(showPriceVOCModel.getObject()), Boolean.TRUE.equals(showPriceMOCModel.getObject()));
			displayOptions.setLocale(localeModel.getObject());

			// Generate PDF
			var pdfFileName = "nabidka-" + System.currentTimeMillis() + ".pdf";

			if (WicketAppUtil.localMode())
				pdfFileName = "nabidka.pdf";

			var outputPath = Path.of(PDF_OUTPUT_DIR, pdfFileName);

			// Ensure output directory exists
			Files.createDirectories(outputPath.getParent());

			offerPdfGenerator.generatePdf(offer, displayOptions, outputPath);

			LOG.info("PDF generated successfully: {}", outputPath);
			info("PDF soubor byl úspěšně vygenerován");

			// Trigger download
			downloadPdf(outputPath, pdfFileName);
		}
		catch (Exception e)
		{
			LOG.error("Error processing file", e);
			error("Chyba při zpracování souboru: " + e.getMessage());
		}
	}

	/**
	 * Triggers the download of the generated PDF file.
	 *
	 * @param pdfPath
	 *            the path to the PDF file
	 * @param fileName
	 *            the download filename
	 */
	private void downloadPdf(Path pdfPath, String fileName)
	{
		var file = pdfPath.toFile();
		var resourceStream = new FileResourceStream(file);

		var handler = new ResourceStreamRequestHandler(resourceStream, fileName);
		handler.setContentDisposition(ContentDisposition.ATTACHMENT);

		getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
	}
}
