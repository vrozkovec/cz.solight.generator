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
package cz.solight.generator.xmltopdf.wicket.components;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Static holder for upload progress state, keyed by session ID.
 * Uses ConcurrentHashMap for thread-safe access between the job thread
 * and the Wicket request thread.
 */
public final class UploadProgressHolder
{
	private static final ConcurrentHashMap<String, UploadProgress> PROGRESS_MAP = new ConcurrentHashMap<>();

	private UploadProgressHolder()
	{
		// Utility class, no instantiation
	}

	/**
	 * Updates the progress for a given session.
	 *
	 * @param sessionId
	 *            the Wicket session ID
	 * @param progress
	 *            the current progress state
	 */
	public static void update(String sessionId, UploadProgress progress)
	{
		PROGRESS_MAP.put(sessionId, progress);
	}

	/**
	 * Gets the current progress for a given session.
	 *
	 * @param sessionId
	 *            the Wicket session ID
	 * @return the current progress state, or initial state if not found
	 */
	public static UploadProgress get(String sessionId)
	{
		return PROGRESS_MAP.getOrDefault(sessionId, UploadProgress.initial());
	}

	/**
	 * Removes the progress entry for a given session.
	 * Should be called when the upload panel is detached or progress is no longer needed.
	 *
	 * @param sessionId
	 *            the Wicket session ID
	 */
	public static void remove(String sessionId)
	{
		PROGRESS_MAP.remove(sessionId);
	}
}
