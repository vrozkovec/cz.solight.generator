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

import org.apache.wicket.Application;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author vit
 */
public abstract class SchedulerLocator implements ISchedulerProvider
{
	private Scheduler scheduler;

	private JobDataMap jobDataMap;

	/**
	 * Construct.
	 */
	public SchedulerLocator()
	{
		scheduler = createScheduler();
	}

	@Override
	public Scheduler getScheduler()
	{
		return scheduler;
	}

	@Override
	public final ISchedulerProvider startScheduler(Application application)
	{
		jobDataMap = new JobDataMap();
		jobDataMap.put(ISchedulerProvider.WICKET_APPLICATION_KEY, application.getApplicationKey());

		return ISchedulerProvider.super.startScheduler(application);
	}

	/**
	 * @see name.berries.app.scheduler.ISchedulerProvider#getJobDataWithApplicationKey()
	 */
	@Override
	public final JobDataMap getJobDataWithApplicationKey()
	{
		return jobDataMap;
	}

	/**
	 * Creates default scheduler, convenience method.
	 *
	 * @return scheduler
	 */
	private Scheduler createScheduler()
	{
		SchedulerFactory sf = new StdSchedulerFactory();
		try
		{
			return sf.getScheduler();
		}
		catch (SchedulerException e)
		{
			throw new RuntimeException(e);
		}
	}
}
