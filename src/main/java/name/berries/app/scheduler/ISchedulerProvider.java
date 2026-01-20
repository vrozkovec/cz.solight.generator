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
import org.apache.wicket.util.lang.Args;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * @author rozkovec
 */
public interface ISchedulerProvider
{
	/**
	 * Key to be used in {@link JobDataMap} returned by {@link #getJobDataWithApplicationKey()}
	 */
	static final String WICKET_APPLICATION_KEY = "wicketApplicationKey";

	/**
	 * Schedules jobs, use {@link #addSchedule(JobBuilder, TriggerBuilder)} for scheduling new jobs
	 *
	 * @throws SchedulerException
	 */
	void scheduleJobs() throws SchedulerException;

	/**
	 * @return scheduler
	 */
	public Scheduler getScheduler();

	/**
	 * @return job data that co
	 */
	public JobDataMap getJobDataWithApplicationKey();

	/**
	 * @param job
	 * @param trigger
	 * @throws SchedulerException
	 */
	public default <T extends Trigger> void addSchedule(JobBuilder job, TriggerBuilder<T> trigger) throws SchedulerException
	{
		JobDataMap jobDataMap = getJobDataWithApplicationKey();
		JobDetail builtJob = job.setJobData(jobDataMap).build();
		Trigger builtTrigger = trigger.build();

		getScheduler().scheduleJob(builtJob, builtTrigger);
	}

	/**
	 * This method starts scheduler in the application. Method should not be called when in test
	 * mode.
	 *
	 * @param application
	 * @return starts the scheduler
	 */
	default ISchedulerProvider startScheduler(Application application)
	{
		try
		{
			JobDataMap jobData = getJobDataWithApplicationKey();
			Args.notNull(jobData, "jobData");
			Args.isTrue(jobData.containsKey(WICKET_APPLICATION_KEY), "job data must contain application key");

			scheduleJobs();

			getScheduler().startDelayed(3);
			return this;
		}
		catch (SchedulerException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * stops the scheduler
	 */
	default void stopScheduler()
	{
		try
		{
			getScheduler().shutdown();
		}
		catch (SchedulerException e)
		{
			throw new RuntimeException(e);
		}
	}
}