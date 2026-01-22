package cz.solight.generator.xmltopdf.wicket.components;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import cz.solight.generator.xmltopdf.pojo.PdfDisplayOptions;
import cz.solight.generator.xmltopdf.pojo.PdfLocale;
import cz.solight.generator.xmltopdf.service.OfferPdfGenerator;
import cz.solight.generator.xmltopdf.service.OfferXmlParser;

import name.berries.wicket.components.YesNoCheckBox;
import name.berries.wicket.notifications.AjaxNotificationPanel;
import name.berries.wicket.util.app.WicketAppUtil;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.ButtonGroup;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons.Type;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.FileInputConfig;
import de.agilecoders.wicket.jquery.Key;

/**
 * Wicket panel for uploading XML offer files and generating PDF catalogs. Provides checkboxes to
 * control which prices are displayed in the output.
 */
public class ParserPanel extends Panel
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ParserPanel.class);
	private static final String PDF_OUTPUT_DIR = "/data/tmp/";
	private static final String PDF_RESOURCE_KEY = "inline-pdf-resource";

	@Inject
	private OfferXmlParser xmlOfferParser;

	@Inject
	private OfferPdfGenerator offerPdfGenerator;

	private BootstrapFileInputField fileUploadField;
	private Model<Boolean> showPriceMyModel = Model.of(true);
	private Model<Boolean> showPriceVOCModel = Model.of(true);
	private Model<Boolean> showPriceMOCModel = Model.of(true);
	private Model<PdfLocale> localeModel = Model.of(PdfLocale.CZ);
	private WebMarkupContainer pdfPreviewContainer;
	private WebMarkupContainer pdfFrame;
	private DownloadLink downloadLink;
	private IModel<File> pdfFileModel = Model.of();

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

		// Register shared resource for serving PDF files (if not already registered)
		registerPdfResource();

		// Create PDF preview container (initially hidden)
		pdfPreviewContainer = new WebMarkupContainer("pdfPreviewContainer");
		pdfPreviewContainer.setOutputMarkupId(true);
		pdfPreviewContainer.setOutputMarkupPlaceholderTag(true);
		add(pdfPreviewContainer);

		// Create iframe for PDF display
		pdfFrame = new WebMarkupContainer("pdfFrame");
		pdfFrame.setOutputMarkupId(true);
		pdfPreviewContainer.add(pdfFrame);

		// Create download link using native Wicket DownloadLink
		downloadLink = new DownloadLink("downloadLink", pdfFileModel);
		downloadLink.setOutputMarkupId(true);
		pdfPreviewContainer.add(downloadLink);

		// Create upload form
		var uploadForm = new Form<Void>("uploadForm");
		uploadForm.setMultiPart(true);
		uploadForm.setMaxSize(Bytes.megabytes(10));
		add(uploadForm);

		// Add file upload field
		fileUploadField = new BootstrapFileInputField("fileInput");
		FileInputConfig fileInputConfig = fileUploadField.getConfig();
		fileInputConfig.showCaption(true);
		// fileInputConfig.showPreview(false);
		fileInputConfig.showUpload(false);
		fileInputConfig.withLocale("cs");
		fileInputConfig.maxFileCount(1);
		fileInputConfig.put(new Key<>("browseOnZoneClick"), true);
		fileInputConfig.withDropZoneEnabled(true);

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

			// Build URL for the PDF using the shared resource
			var params = new PageParameters();
			params.add("file", pdfFileName);
			var pdfUrl = RequestCycle.get()
				.urlFor(new SharedResourceReference(ParserPanel.class, PDF_RESOURCE_KEY), params)
				.toString();

			// Update iframe src, set download file, and show preview container
			pdfFrame.add(AttributeModifier.replace("src", pdfUrl));
			pdfFileModel.setObject(outputPath.toFile());
			pdfPreviewContainer.add(AttributeModifier.replace("style", "display: block;"));

			// Add components to AJAX target for refresh
			target.add(pdfPreviewContainer);
		}
		catch (Exception e)
		{
			LOG.error("Error processing file", e);
			error("Chyba při zpracování souboru: " + e.getMessage());
		}
	}

	/**
	 * Registers the shared resource for serving PDF files inline (for iframe display). The resource
	 * is registered only once per application lifecycle.
	 */
	private void registerPdfResource()
	{
		var sharedResources = getApplication().getSharedResources();

		// Check if resource is already registered
		if (sharedResources.get(ParserPanel.class, PDF_RESOURCE_KEY, null, null, null, true) != null)
		{
			return;
		}

		// Register inline PDF resource (for iframe display)
		sharedResources.add(ParserPanel.class, PDF_RESOURCE_KEY, null, null, null, new AbstractResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes)
			{
				var fileName = attributes.getParameters().get("file").toString();
				var response = new ResourceResponse();

				// Security: validate filename to prevent path traversal
				if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\"))
				{
					LOG.error("Invalid PDF filename requested: {}", fileName);
					response.setError(400);
					return response;
				}

				var file = new File(PDF_OUTPUT_DIR, fileName);
				response.setCacheDuration(Duration.ZERO);
				response.setCacheScope(CacheScope.PRIVATE);
				response.setFileName(fileName);
				response.setContentDisposition(ContentDisposition.INLINE);
				response.setContentType("application/pdf");

				if (!file.exists())
				{
					LOG.error("PDF file not found: {}", file.getAbsolutePath());
					response.setError(404);
					return response;
				}

				try
				{
					byte[] pdfData = FileUtils.readFileToByteArray(file);
					response.setWriteCallback(new WriteCallback()
					{
						@Override
						public void writeData(Attributes attributes)
						{
							attributes.getResponse().write(pdfData);
						}
					});
				}
				catch (Exception e)
				{
					LOG.error("Error reading PDF file", e);
					response.setError(500);
				}

				return response;
			}
		});
	}
}
