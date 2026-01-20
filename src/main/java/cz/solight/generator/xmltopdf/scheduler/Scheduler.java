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

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import name.berries.app.scheduler.SchedulerLocator;

/**
 * @author rozkovec
 */
public class Scheduler extends SchedulerLocator
{

	@Override
	public void scheduleJobs() throws SchedulerException
	{
		JobBuilder jobOneDayInTheMorning = newJob(JobDailyInTheMorning.class).withIdentity("JobDailyInTheMorning", "group1");
		TriggerBuilder<CronTrigger> triggerOneDay = newTrigger().withIdentity("trigger.oneday", "group1")
			.withSchedule(dailyAtHourAndMinute(01, 8)).startNow();

		addSchedule(jobOneDayInTheMorning, triggerOneDay);


		JobBuilder jobOneTime = newJob(JobOneTime.class).withIdentity("JobOneTime", "group1");
		TriggerBuilder<Trigger> triggerOneTime = newTrigger().withIdentity("triggerJobOneTime", "group1").startNow();

		addSchedule(jobOneTime, triggerOneTime);
	}

}