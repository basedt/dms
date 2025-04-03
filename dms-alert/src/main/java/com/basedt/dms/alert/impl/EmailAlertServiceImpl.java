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
package com.basedt.dms.alert.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.basedt.dms.service.sys.dto.EmailConfigDTO;
import com.basedt.dms.common.constant.Constants;
import com.basedt.dms.service.sys.SysConfigService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.Properties;

@Slf4j
@Service(value = "emailAlertService")
public class EmailAlertServiceImpl {

    @Value("${spring.mail.username}")
    private String sender;

    private final SysConfigService sysConfigService;

    public EmailAlertServiceImpl(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @Async("asyncExecutor")
    public void send(String receiver, String title, String content) {
        String emailConfig = this.sysConfigService.selectValueByKey(Constants.CFG_EMAIL_CODE);
        EmailConfigDTO config = JSONUtil.toBean(emailConfig, EmailConfigDTO.class);
        try {
            JavaMailSender mailSender = configMailInstance(config);
            if (mailSender == null) {
                throw new NullPointerException("java mail sender is null");
            }
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setSubject(title);
            mimeMessageHelper.setTo(receiver);
            mimeMessageHelper.setFrom(getSender());
            mimeMessageHelper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException | NullPointerException e) {
            log.error("Email Send Error : {}", e.getMessage());
        }
    }

    private InternetAddress getSender() throws AddressException {
        String config = this.sysConfigService.selectValueByKey(Constants.CFG_EMAIL_CODE);
        if (StrUtil.isNotBlank(config)) {
            EmailConfigDTO mailConfig = JSONUtil.toBean(config, EmailConfigDTO.class);
            return new InternetAddress(mailConfig.getEmail());
        }
        return new InternetAddress(this.sender);
    }

    private JavaMailSender configMailInstance(EmailConfigDTO emailConfig) {
        if (emailConfig != null) {
            JavaMailSenderImpl jms = new JavaMailSenderImpl();
            jms.setUsername(emailConfig.getEmail());
            String passwd = emailConfig.getPassword().substring(Constants.CODEC_STR_PREFIX.length());
            jms.setPassword(Base64.decodeStr(passwd));
            jms.setHost(emailConfig.getHost());
            jms.setPort(emailConfig.getPort());
            jms.setDefaultEncoding("UTF-8");
            Properties props = new Properties();
            props.setProperty("mail.smtp.auth", "true");
            jms.setJavaMailProperties(props);
            return jms;
        }
        return null;
    }
}
