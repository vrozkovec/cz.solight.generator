/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package name.berries.wicket.util.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vit
 */
public class AppConfigProvider
{
	private static final Logger log = LoggerFactory.getLogger(AppConfigProvider.class);

	/**
	 * Parameter that sets profile via -Dprofile=profilename VM parameter.
	 */
	public static final String PROFILE_PARAM = "profile";

	private static final Map<String, AppConfigProvider> CONFIGS = new ConcurrentHashMap<>();

	private static final String DEFAULT_INSTANCE = "~~~default~~~";

	private static final String DEFAULT_CONFIG_FILE = "appconfig.yml";
	private static final String PROFILE_CONFIG_FILE_PREFIX = "appconfig-";
	private static final String PROFILE_CONFIG_FILE_SUFFIX = ".yml";

	private CombinedConfiguration config;

	static
	{
		addDefaultInstance();
	}

	private static void addDefaultInstance()
	{
		CONFIGS.put(DEFAULT_INSTANCE, new AppConfigProvider());
	}

	/**
	 * Construct.
	 *
	 * @param mapKey
	 */
	public AppConfigProvider()
	{
		this(null);
	}

	/**
	 * Construct.
	 *
	 * @param directory
	 */
	public AppConfigProvider(String directory)
	{
		loadConfig(directory);
	}

	private void loadConfig(String directory)
	{
		log.info("[{}] loading config", directory);

		boolean defaultDirectory;
		try
		{
			directory = StringUtils.trimToEmpty(directory);
			directory = org.apache.commons.lang3.Strings.CS.removeStart(directory, File.separator);
			directory = org.apache.commons.lang3.Strings.CS.removeEnd(directory, File.separator);
			if (!Strings.isEmpty(directory))
			{
				directory += File.separator;
				defaultDirectory = false;
			}
			else
			{
				defaultDirectory = true;
			}


			log.info("[{}] default config: {}", directory, defaultDirectory);

			Configurations configs = new Configurations();
			config = new CombinedConfiguration(new OverrideCombiner());

			// Load the default configuration
			String appConfigPath = "/" + directory + DEFAULT_CONFIG_FILE;
			String configResource = appConfigPath;

			// Override with profile configuration if exists
			String profile = System.getProperty(PROFILE_PARAM);
			if (profile != null && !profile.isEmpty())
			{
				String profileConfigFile = PROFILE_CONFIG_FILE_PREFIX + profile + PROFILE_CONFIG_FILE_SUFFIX;
				try
				{
					String profileConfigPath = "/" + directory + profileConfigFile;


					var builder = configs.fileBasedBuilder(YAMLConfiguration.class, this.getClass().getResource(profileConfigPath));
					config.addConfiguration(builder.getConfiguration());

					log.info("[{}] added config: {}", directory, profileConfigPath);
					debugConfig(directory);
				}
				catch (ConfigurationException e)
				{
					log.error("Profile specific configuration not found for: {}", profile);
					e.printStackTrace();
				}
			}

			var builder = configs.fileBasedBuilder(YAMLConfiguration.class, this.getClass().getResource(configResource));
			YAMLConfiguration appConfigDefault = builder.getConfiguration();
			config.addConfiguration(appConfigDefault);

			log.info("[{}] added config: {}", directory, appConfigPath);
			debugConfig(directory);

			if (!defaultDirectory)
			{
				Configuration defaultConfiguration = getDefaultConfiguration();
				log.info("[{}] added default config", directory);
				config.addConfiguration(defaultConfiguration);
				debugConfig(directory);
			}
		}
		catch (ConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	private void debugConfig(String directory)
	{
		if (log.isTraceEnabled())
		{
			var keys = new ArrayList<String>();
			config.getKeys().forEachRemaining(keys::add);
			for (String key : keys)
			{
				log.trace("[{}] config key->val: {} -> {}", directory, key, config.getString(key));
			}
		}
	}

	/**
	 * @return config instance
	 */
	public static Configuration getDefaultConfiguration()
	{
		return CONFIGS.get(DEFAULT_INSTANCE).config;
	}

	/**
	 * @param directory
	 * @return config instance
	 */
	public static Configuration getConfiguration(String directory)
	{
		return CONFIGS.computeIfAbsent(directory, AppConfigProvider::new).config;
	}

	/**
	 * Clears config instance and resets it to initial state.
	 */
	public static void clear()
	{
		CONFIGS.clear();
		addDefaultInstance();
	}

	/**
	 * @author vit
	 */
	@SuppressWarnings("javadoc")
	public static class ConfigKey
	{
		public static final String FILES_FOLDER_PUBLIC = "files.folder.public";
		public static final String FILES_FOLDER_PRIVATE = "files.folder.private";
		public static final String FILES_DOMAIN = "files.domain";
		public static final String APP_BASE_URL = "app.baseUrl";
		public static final String APP_BASE_IMAGES_URL = "app.baseImagesUrl";
		public static final String APP_IDENTIFIER = "app.identifier";
		public static final String APP_EMAILFROM_ADDRESS = "app.emailFrom.address";
		public static final String APP_EMAILFROM_NAME = "app.emailFrom.name";

		public static final String APP_DIRECTORIES_COMMON_RELATIVE_PATH_TO_TEMPLATES = "app.directories.common.relativePathToTemplates";
		public static final String APP_DIRECTORIES_COMMON_TEMP = "app.directories.common.temp";
		public static final String APP_DIRECTORIES_LOCAL_PROJECT_BASE = "app.directories.local.projectBase";
		public static final String APP_DIRECTORIES_LOCAL_FLYWAY_MIGRATIONS_PARENT_DIR = "app.directories.local.flywayMigrationsParentDir";

		private ConfigKey()
		{
		}
	}
}
