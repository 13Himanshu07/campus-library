package com.library.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.*;
@Service
public class MailService {
 private static final Logger log=LoggerFactory.getLogger(MailService.class);
 private final ObjectProvider<JavaMailSender> sender;
 private final String from;
 public MailService(ObjectProvider<JavaMailSender> sender,@Value("${spring.mail.username:}") String from){this.sender=sender;this.from=from;}
 public void send(String to,String subject,String body){try{JavaMailSender s=sender.getIfAvailable();if(s==null){log.warn("Email was not sent because mail delivery is not configured");return;}var m=new SimpleMailMessage();if(from!=null&&!from.isBlank())m.setFrom(from);m.setTo(to);m.setSubject(subject);m.setText(body);s.send(m);}catch(Exception e){log.warn("Email delivery failed: {}",e.getMessage());}}
}
