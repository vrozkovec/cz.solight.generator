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
package cz.solight.generator.xmltopdf.scheduler;

import static name.berries.app.guice.GuiceStaticHolder.getInstance;

import java.io.FileInputStream;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.solight.generator.xmltopdf.service.FtpSyncService;
import cz.solight.generator.xmltopdf.service.ProductSheetPdfGenerator;
import cz.solight.generator.xmltopdf.service.ProductSheetXmlParser;
import cz.solight.generator.xmltopdf.service.SftpConfig;
import cz.solight.generator.xmltopdf.wicket.app.PdfGeneratorApplication;

import name.berries.app.scheduler.WicketAppBoundJob;
import name.berries.wicket.util.app.WicketAppUtil;

/**
 * @author rozkovec
 */
public class JobOneTime extends WicketAppBoundJob<PdfGeneratorApplication>
{
	private static final Logger log = LoggerFactory.getLogger(JobOneTime.class);

	/**
	 * Construct.
	 */
	public JobOneTime()
	{
	}

	@Override
	public void executeWithAppBoundInContext(JobExecutionContext context, PdfGeneratorApplication application)
		throws JobExecutionException
	{
		JobAction action = new JobAction();

		if (!WicketAppUtil.localMode())
			uploadConvertedProductSheets(action);
	}

	/**
	 * Executes the product sheet upload job: downloads XMLs from FTP, converts them to PDF, and
	 * uploads back.
	 *
	 * @param action
	 *            the job action for error handling and logging
	 */
	public static void uploadConvertedProductSheets(JobAction action)
	{
		action.accept("Download XMLs from FTP, convert to PDF, upload back", () -> {

			var ftp = getInstance(FtpSyncService.class);
			ftp.syncXmlFiles(new SftpConfig(), (file, consumer) -> {
				try
				{
					ProductSheetXmlParser sheetXmlParser = getInstance(ProductSheetXmlParser.class);
					ProductSheetPdfGenerator sheetPdfGenerator = getInstance(ProductSheetPdfGenerator.class);

					var products = sheetXmlParser.parse(new FileInputStream(file));
					sheetPdfGenerator.generateAllPdfs(products, consumer);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			});

		});
	}
}
