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
package com.basedt.dms.scheduler.config;

//import com.basedt.dms.scheduler.job.MySampleJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

//    @Bean
//    public JobDetail sampleJob() {
//        return JobBuilder.newJob(MySampleJob.class)
//                .withIdentity("sampleJob")
//                .storeDurably()
//                .build();
//    }

//    @Bean
//    public Trigger downloadTrigger() {
//        return TriggerBuilder.newTrigger()
//                .forJob(sampleJob())
//                .withIdentity("sampleTrigger")
//                .withSchedule(CronScheduleBuilder.cronSchedule("*/5 * * * * ? *"))
//                .build();
//    }
}
