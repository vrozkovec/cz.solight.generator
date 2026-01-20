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
package name.berries.app.scheduler;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.wicket.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.berries.app.guice.GuiceStaticHolder;

/**
 * Action helper to be run inside of the scheduler.
 *
 * @author vit
 * @param <T>
 */
public abstract class BaseJobAction<T extends Application> implements BiConsumer<String, Runnable>
{
	private static final Logger log = LoggerFactory.getLogger(BaseJobAction.class);

	/**
	 * Construct.
	 *
	 * @param jobClass
	 */
	protected BaseJobAction()
	{
		super();
		GuiceStaticHolder.injectMembers(this);
	}

	@Override
	public void accept(String message, Runnable a)
	{
		log.info("JobAction[{}] START", message);
		StopWatch stopWatch = StopWatch.createStarted();
		try
		{
			a.run();
			stopWatch.stop();
			log.info("JobAction[{}] STOP | elapsed: {}", message, stopWatch);

		}
		catch (Exception e)
		{
			e.printStackTrace();

			String stackTrace = ExceptionUtils.getStackTrace(e);

			log.error("JobAction[{}] ERROR: {}", message, e.getMessage());
			onError(e, stackTrace, message);
		}
		finally
		{
			if (stopWatch.isStarted())
				stopWatch.stop();
		}
	}

	protected abstract void onError(Exception e, String stackTrace, String message);
}