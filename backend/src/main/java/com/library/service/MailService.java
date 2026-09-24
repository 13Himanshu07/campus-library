package com.library.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.*;
@Service
public class MailService {
 private static final Logger log=LoggerFactory.getLogger(MailService.class);
 private final ObjectProvider<JavaMailSender> sender;
 public MailService(ObjectProvider<JavaMailSender> sender){this.sender=sender;}
 public void send(String to,String subject,String body){try{JavaMailSender s=sender.getIfAvailable();if(s==null)return;var m=new SimpleMailMessage();m.setTo(to);m.setSubject(subject);m.setText(body);s.send(m);}catch(Exception e){log.warn("Email delivery failed for recipient {}: {}",to,e.getMessage());}}
}
