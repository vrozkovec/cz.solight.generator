package cz.solight.generator.xmltopdf.wicket.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import cz.solight.generator.xmltopdf.service.FtpSyncService;
import cz.solight.generator.xmltopdf.service.ImagePathConverter;
import cz.solight.generator.xmltopdf.service.OfferPdfGenerator;
import cz.solight.generator.xmltopdf.service.OfferXmlParser;
import cz.solight.generator.xmltopdf.service.ProductSheetPdfGenerator;
import cz.solight.generator.xmltopdf.service.ProductSheetXmlParser;

import name.berries.pdf.PdfGeneratorService;

/**
 * Main Guice module for the Generator application. Configures services for XML parsing and PDF
 * generation.
 */
public class GeneratorModule extends AbstractModule
{
	private static final Logger LOG = LoggerFactory.getLogger(GeneratorModule.class);

	@Override
	protected void configure()
	{
		LOG.info("Configuring GeneratorModule bindings");
		bind(ImagePathConverter.class).in(Singleton.class);
		bind(PdfGeneratorService.class).in(Singleton.class);
		bind(FtpSyncService.class).in(Singleton.class);

		bind(OfferXmlParser.class).in(Singleton.class);
		bind(OfferPdfGenerator.class).in(Singleton.class);

		bind(ProductSheetPdfGenerator.class).in(Singleton.class);
		bind(ProductSheetXmlParser.class).in(Singleton.class);
	}
}
