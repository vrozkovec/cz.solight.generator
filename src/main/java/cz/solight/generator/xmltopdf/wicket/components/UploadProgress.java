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

/**
 * Immutable record holding the progress state of a product sheet upload job.
 * Tracks total products, current progress, running state, and any errors.
 *
 * @param total
 *            total number of products to process
 * @param current
 *            number of products processed so far
 * @param currentProductCode
 *            code of the product currently being processed
 * @param running
 *            true if the job is still running
 * @param completed
 *            true if the job completed successfully
 * @param error
 *            error message if the job failed, null otherwise
 */
public record UploadProgress(
	int total,
	int current,
	String currentProductCode,
	boolean running,
	boolean completed,
	String error)
{
	/**
	 * Calculates the progress percentage.
	 *
	 * @return percentage of progress (0-100)
	 */
	public int getPercentage()
	{
		return total > 0 ? (current * 100) / total : 0;
	}

	/**
	 * Creates an initial progress state (not started).
	 *
	 * @return initial progress state
	 */
	public static UploadProgress initial()
	{
		return new UploadProgress(0, 0, null, false, false, null);
	}

	/**
	 * Creates a running progress state.
	 *
	 * @param total
	 *            total number of products to process
	 * @param current
	 *            number of products processed so far
	 * @param productCode
	 *            code of the product currently being processed
	 * @return running progress state
	 */
	public static UploadProgress running(int total, int current, String productCode)
	{
		return new UploadProgress(total, current, productCode, true, false, null);
	}

	/**
	 * Creates a completed progress state.
	 *
	 * @param total
	 *            total number of products processed
	 * @return completed progress state
	 */
	public static UploadProgress completed(int total)
	{
		return new UploadProgress(total, total, null, false, true, null);
	}

	/**
	 * Creates a failed progress state.
	 *
	 * @param errorMessage
	 *            the error message describing what went wrong
	 * @return failed progress state
	 */
	public static UploadProgress failed(String errorMessage)
	{
		return new UploadProgress(0, 0, null, false, false, errorMessage);
	}
}
