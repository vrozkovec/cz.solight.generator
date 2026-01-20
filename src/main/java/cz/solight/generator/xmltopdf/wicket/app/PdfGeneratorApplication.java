package cz.solight.generator.xmltopdf.wicket.app;

import org.apache.wicket.Application;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.solight.generator.xmltopdf.scheduler.Scheduler;
import cz.solight.generator.xmltopdf.wicket.pages.HomePage;

import name.berries.app.guice.GuiceStaticHolder;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;

/**
 * Application object for your web application. If you want to run this application without
 * deploying, run the Start class.
 */
public class PdfGeneratorApplication extends WebApplication
{
	private static final Logger LOG = LoggerFactory.getLogger(PdfGeneratorApplication.class);

	/**
	 *
	 */
	public static final String MOUNTPOINT_LOGOUT = "/odhlaseni";

	private Injector injector;
	private Scheduler scheduler;

	/**
	 * Construct.
	 */
	public PdfGeneratorApplication()
	{
		super();
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();

		// Initialize Guice
		injector = Guice.createInjector(new GeneratorModule());
		GuiceStaticHolder.setInjector(injector);

		// TODO when tests are present, do not start scheduler
		scheduler = new Scheduler();
		scheduler.startScheduler(this);
		LOG.info("Starting scheduler {}", scheduler);

		getComponentInstantiationListeners().add(new GuiceComponentInjector(this, injector));

		getCspSettings().blocking().disabled();

		getExceptionSettings().setUnexpectedExceptionDisplay(ExceptionSettings.SHOW_EXCEPTION_PAGE);

		getApplicationSettings().setDefaultMaximumUploadSize(Bytes.megabytes(108));

		/***************************************************
		 * BOOTSTRAP
		 ****************************************************/
		BootstrapSettings settings = new BootstrapSettings();
		settings.setJsResourceFilterName("footer-container");
		settings.setCssResourceReference(
			new UrlResourceReference(Url.parse("https://cdn.jsdelivr.net/npm/bootstrap@5/dist/css/bootstrap.min.css")));
		settings.setJsResourceReference(
			new UrlResourceReference(Url.parse("https://cdn.jsdelivr.net/npm/bootstrap@5/dist/js/bootstrap.bundle.min.js")));
		Bootstrap.install(this, settings);
		getHeaderResponseDecorators().add(response -> new JavaScriptFilteredIntoFooterHeaderResponse(response, "footer-container"));
	}

	/**
	 * Returns the Guice injector for accessing services.
	 *
	 * @return the Guice injector
	 */
	public Injector getInjector()
	{
		return injector;
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @return this application
	 */
	public static PdfGeneratorApplication get()
	{
		return (PdfGeneratorApplication)Application.get();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (scheduler != null)
			scheduler.stopScheduler();

		GuiceStaticHolder.unset();
	}
}