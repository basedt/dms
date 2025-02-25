/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.basedt.dms.scheduler.utils;

import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.common.utils.DateTimeUtil;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class QuartzSchedulerUtil {

    public static JobKey getJobKey(String groupId, String jobId) {
        String jobGroup = String.format("%s_%s", Constants.JOB_GROUP_PREFIX, groupId);
        String jobName = String.format("%s_%s", Constants.JOB_PREFIX, jobId);
        return new JobKey(jobName, jobGroup);
    }

    public static TriggerKey getTriggerKey(String groupId, String jobId) {
        String triggerGroup = String.format("%s_%s", Constants.TRIGGER_GROUP_PREFIX, groupId);
        String triggerName = String.format("%s_%s", Constants.TRIGGER_PREFIX, jobId);
        return TriggerKey.triggerKey(triggerName, triggerGroup);
    }

    private static ScheduleBuilder buildSchedule(String crontabStr) {
        return CronScheduleBuilder
                .cronSchedule(crontabStr)
                .inTimeZone(TimeZone.getDefault());
    }

    public static Trigger getTrigger(String groupId, String jobId, String crontabStr, LocalDateTime startTime, LocalDateTime endTime) {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(groupId, jobId))
                .startAt(DateTimeUtil.toDate(startTime))
                .endAt(DateTimeUtil.toDate(endTime))
                .withSchedule(buildSchedule(crontabStr))
                .build();
    }

    public static List<LocalDateTime> next5FireTime(String crontabStr) throws ParseException {
        CronTriggerImpl cronTrigger = new CronTriggerImpl(null, null, crontabStr);
        List<Date> dateList = TriggerUtils.computeFireTimes(cronTrigger, null, 5);
        return dateList.stream().map(DateTimeUtil::toLocalDateTime).collect(Collectors.toList());
    }

    public static boolean exists(Scheduler scheduler, String groupId, String jobId) {
        try {
            if (Objects.isNull(scheduler) || Objects.isNull(groupId) || Objects.isNull(jobId)) {
                return false;
            }
            return scheduler.checkExists(getJobKey(groupId, jobId));
        } catch (SchedulerException e) {
            return false;
        }
    }

    public static void schedule(Scheduler scheduler, String groupId, String jobId, String crontabStr, LocalDateTime startTime, LocalDateTime endTime, Class<? extends QuartzJobBean> clazz) throws SchedulerException {
        JobKey jobKey = getJobKey(groupId, jobId);
        if (!exists(scheduler, groupId, jobId)) {
            // TODO build dataMap    JobDataMap dataMap =
            JobDetail job = JobBuilder.newJob(clazz)
                    .withIdentity(jobKey)
                    .storeDurably()
                    .build();
            Trigger trigger = getTrigger(groupId, jobId, crontabStr, startTime, endTime);
            scheduler.scheduleJob(job, trigger);
        }
    }

    public static void unSchedule(Scheduler scheduler, String groupId, String jobId) throws SchedulerException {
        JobKey jobKey = getJobKey(groupId, jobId);
        if (exists(scheduler, groupId, jobId)) {
            scheduler.deleteJob(jobKey);
        }
    }

}
