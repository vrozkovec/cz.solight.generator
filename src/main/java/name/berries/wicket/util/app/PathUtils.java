/*
90 * Licensed to the Apache Software Foundation (ASF) under one or more
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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

/**
 * @author vit
 */
public class PathUtils
{
	/**
	 * Builds a path from the given tokens, last token is expected to be a file.
	 *
	 * @param pathTokens
	 *            the path tokens
	 * @return the built path
	 */
	public static String buildPathForFile(String... pathTokens)
	{
		return buildPath(false, pathTokens);
	}

	/**
	 * Builds a path from the given tokens.
	 *
	 * @param lastSlashPresent
	 *            true if the last token is a directory, false if it is a file
	 * @param pathTokens
	 *            the path tokens
	 * @return the built path
	 */
	public static String buildPath(boolean lastSlashPresent, String... pathTokens)
	{
		Path path = Paths.get("");
		int i = 0;
		for (String token : pathTokens)
		{
			if (i > 0)
				token = StringUtils.stripStart(token, "\\/");

			token = StringUtils.stripEnd(token, "\\/");
			path = path.resolve(token);
			i++;
		}

		String builtPath = path.toString();
		if (lastSlashPresent)
			builtPath += File.separator;
		return builtPath;
	}
}
