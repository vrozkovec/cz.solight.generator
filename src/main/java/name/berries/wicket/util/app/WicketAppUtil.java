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

import org.apache.commons.configuration2.Configuration;

import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;
import name.berries.wicket.util.app.jetty.JettyRunner;

/**
 * Utilita pro pripad, kdy potrebujeme zjistit stav, v jakem je aplikace spustena, ale jsme ve
 * vlakne, kde aplikace neni dostupna.
 *
 * @author rozkovec
 */
public class WicketAppUtil
{
	/** port system property */
	public static final String PORT = "port";
	/** */
	public static final String VALUE_UNSET = "unset";


	private static final boolean TEST_MODE;
	private static final boolean LOCAL_MODE;
	private static final boolean MAIN_BRANCH;
	private static final boolean STAGING_BRANCH;

	private static final String PROFILE;
	private static final String BRANCH;
	private static final String CONFIG_DIRECTORY;
	private static final String JETTY_TEMP_ABS_PATH;

	private static final String APP_IDENTIFIER;

	static
	{
		APP_IDENTIFIER = System.getProperty("javaAppIdentifier", VALUE_UNSET);
		PROFILE = System.getProperty("profile", VALUE_UNSET);
		BRANCH = System.getProperty("gitBranch", VALUE_UNSET);
		CONFIG_DIRECTORY = System.getProperty("configDirectory", VALUE_UNSET);
		JETTY_TEMP_ABS_PATH = System.getProperty("jettyTempAbsPath", VALUE_UNSET);

		LOCAL_MODE = System.getProperty("local", VALUE_UNSET).equalsIgnoreCase("yeah");
		TEST_MODE = System.getProperty("testMode", VALUE_UNSET).equalsIgnoreCase("yeah");
		MAIN_BRANCH = "main".equalsIgnoreCase(BRANCH) || "master".equalsIgnoreCase(BRANCH);
		STAGING_BRANCH = "staging".equalsIgnoreCase(BRANCH) || BRANCH.startsWith("feature/staging");
	}


	/**
	 * Gets filesystem path to the extracted war. Useful for example for wkhtmltopdf and commandline
	 * conversion. Does not include trailing slash.
	 *
	 * @return filestystem path
	 */
	public static String getConfiguredAbsolutePathForJettyTempDir()
	{
		if (!VALUE_UNSET.equals(JETTY_TEMP_ABS_PATH))
			return JETTY_TEMP_ABS_PATH;

		return getActiveConfiguration().getString(ConfigKey.APP_DIRECTORIES_COMMON_TEMP);
	}

	/**
	 * Returns branch that this app runs from, defaults to master
	 *
	 * @return git branch
	 */
	public static boolean isCustomConfigDirectorySet()
	{
		return !VALUE_UNSET.equals(CONFIG_DIRECTORY);
	}

	/**
	 * Returns branch that this app runs from, defaults to master
	 *
	 * @return git branch
	 */
	public static String getCustomConfigDirectory()
	{
		return CONFIG_DIRECTORY;
	}

	/**
	 * Returns branch that this app runs from, defaults to master
	 *
	 * @return git branch
	 */
	public static String getGitBranch()
	{
		return BRANCH;
	}

	/**
	 * Returns <code>true</code> if this branch is a main one.
	 *
	 * @return <code>true</code> if on main branch
	 */
	public static boolean isMainBranch()
	{
		return MAIN_BRANCH;
	}

	/**
	 * Returns <code>true</code> if this branch is a staging one.
	 *
	 * @return <code>true</code> if on staging branch
	 */
	public static boolean isStagingBranch()
	{
		return STAGING_BRANCH;
	}

	/**
	 * Returns commit that this app runs from.
	 *
	 * @return git branch
	 */
	public static String getGitCommit()
	{
		return System.getProperty("gitCommit", VALUE_UNSET);
	}

	/**
	 * Returns Jenkins' build number.
	 *
	 * @return git branch
	 */
	public static String getJenkinsBuildBumber()
	{
		return System.getProperty("jenkinsBuildNumber", VALUE_UNSET);
	}

	/**
	 * Returns commit that this app runs from.
	 *
	 * @return redis port
	 */
	public static int getRedisPort()
	{
		return getRedisPort(6379);
	}

	/**
	 * Returns commit that this app runs from.
	 *
	 * @param defaultValue
	 *
	 * @return redis port
	 */
	public static int getRedisPort(int defaultValue)
	{
		return Integer.valueOf(System.getProperty("redisPort", "" + defaultValue));
	}

	/**
	 * Returns port that the server is listening for.
	 *
	 * @return server port
	 * @see JettyRunner#getPort()
	 */
	public static int getAppPort()
	{
		return getAppPort(8080);
	}

	/**
	 * Returns port that the server is listening for.
	 *
	 * @param defaultValue
	 *
	 * @return server port
	 * @see JettyRunner#getPort()
	 */
	public static int getAppPort(int defaultValue)
	{
		return Integer.valueOf(System.getProperty(PORT, "" + defaultValue));
	}

	/**
	 * Is this test mode? Meaning tests are just running?
	 *
	 * @return mod, ve kterem se aplikace nachazi
	 */
	public static boolean testMode()
	{
		return TEST_MODE;
	}

	/**
	 * Bezi aplikace na vyvojovem pocitaci nebo na serveru?
	 *
	 * @return mod, ve kterem se aplikace nachazi
	 */
	public static boolean localMode()
	{
		return LOCAL_MODE;
	}

	/**
	 * @param location
	 * @return <code>true</code>, pokud aplikace bezi v dane lokaci
	 */
	public static boolean locationMode(String location)
	{
		return System.getProperty("location") != null && System.getProperty("location").equals(location);
	}

	/**
	 * Checks what developer is running this app. Used for local development.
	 *
	 * @param developer
	 * @return <code>true</code>, if VM param 'developer' was set to the param of the method
	 */
	public static boolean profile(String developer)
	{
		return PROFILE.equals(developer);
	}

	/**
	 * @return <code>true</code>, if this is local development for Vit Rozkovec
	 */
	public static boolean vrozkovec()
	{
		return profile("vrozkovec");
	}

	/**
	 * Returns active configuration
	 *
	 * @return active configuration
	 */
	static Configuration getActiveConfiguration()
	{
		if (WicketAppUtil.isCustomConfigDirectorySet())
			return AppConfigProvider.getConfiguration(WicketAppUtil.getCustomConfigDirectory());
		else
			return AppConfigProvider.getDefaultConfiguration();
	}

	/**
	 * Gets appIdentifier.
	 *
	 * @return appIdentifier
	 */
	public static String getAppIdentifier()
	{
		return APP_IDENTIFIER;
	}

	/**
	 * Gets deployment flag file.
	 *
	 * @return file
	 */
	public static File getDeploymentFlagFile()
	{
		return new File("/tmp/deployment_%s.flag".formatted(APP_IDENTIFIER));
	}

	/**
	 * Checks if deployment is in progress.
	 *
	 * @return <code>true</code> if it is
	 */
	public static boolean isDeploymentInProgress()
	{
		return getDeploymentFlagFile().exists();
	}

	/**
	 * Gets deployment flag file.
	 *
	 * @return file
	 */
	public static File getDeploymentOnHoldFlagFile()
	{
		return new File("/tmp/deployment_on_hold_%s.flag".formatted(APP_IDENTIFIER));
	}

	/**
	 * Checks if deployment is in progress.
	 *
	 * @return <code>true</code> if it is
	 */
	public static boolean isDeploymentOnHold()
	{
		return getDeploymentOnHoldFlagFile().exists();
	}


}
