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


import org.apache.commons.configuration2.Configuration;

import name.berries.wicket.util.app.AppConfigProvider.ConfigKey;

/**
 * @author vit
 */
public class JettyUtil
{
	/**
	 * Gets filesystem path to the extracted war. Useful for example for wkhtmltopdf and commandline
	 * conversion. Does not include trailing slash.
	 *
	 * @return filestystem path
	 */
	public static String getConfiguredAbsolutePathForJettyTempDir()
	{
		int appPort = WicketAppUtil.getAppPort();
		Configuration configuration = WicketAppUtil.getActiveConfiguration();

		String tempDirectory = WicketAppUtil.getConfiguredAbsolutePathForJettyTempDir();
		String appIdentifier = configuration.getString(ConfigKey.APP_IDENTIFIER);

		return PathUtils.buildPath(false, tempDirectory, "jetty", appIdentifier, appPort + "");
	}

	/**
	 * Gets filesystem path to the extracted war. Useful for example for wkhtmltopdf and commandline
	 * conversion. Does not include trailing slash.
	 *
	 * @param jettyTempDirectory
	 * @return filestystem path
	 */
	public static String getAbsolutePathToExtractedWar(String jettyTempDirectory)
	{
		return PathUtils.buildPath(false, jettyTempDirectory, "webapp");
	}

}
