package cz.solight.generator.xmltopdf.wicket.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

/**
 * Main Guice module for the Surveys application. Configures database connection, JDBI, and
 * services.
 */
public class GeneratorModule extends AbstractModule
{
	private static final Logger LOG = LoggerFactory.getLogger(GeneratorModule.class);

	@Override
	protected void configure()
	{
	}
}
