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
import org.apache.wicket.ThreadContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author vit
 * @param <T>
 */
public abstract class WicketAppBoundJob<T extends Application> implements Job
{

	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void execute(JobExecutionContext context) throws JobExecutionException
	{
		String applicationKey = (String)context.getMergedJobDataMap().get(ISchedulerProvider.WICKET_APPLICATION_KEY);
		T application = (T)Application.get(applicationKey);
		ThreadContext.setApplication(application);
		try
		{
			preExecute(context, application);
			executeWithAppBoundInContext(context, application);
			postExecute(context, application);
		}
		finally
		{
			ThreadContext.setApplication(null);
			ThreadContext.detach();
		}
	}

	/**
	 * @param application
	 * @param context
	 *
	 */
	protected void postExecute(JobExecutionContext context, T application)
	{
	}

	/**
	 * @param application
	 * @param context
	 *
	 */
	protected void preExecute(JobExecutionContext context, T application)
	{
	}

	/**
	 * Executes the job
	 *
	 * @param context
	 * @param application
	 * @throws JobExecutionException
	 */
	public abstract void executeWithAppBoundInContext(JobExecutionContext context, T application)
		throws JobExecutionException;

}
