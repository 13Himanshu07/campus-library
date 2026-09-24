package com.library.service;

import com.library.entity.*;
import com.library.repository.TransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDate;

@Component
public class OverdueScheduler {
 private final TransactionRepository transactions;private final NotificationService notifications;private final int reminderDays;
 public OverdueScheduler(TransactionRepository t,NotificationService n,@Value("${app.reminder-days:2}") int d){transactions=t;notifications=n;reminderDays=d;}
 @Scheduled(cron="${OVERDUE_CRON:0 0 2 * * *}") @Transactional public void checkOverdue(){for(var t:transactions.findOverdueOpen()){if(t.getStatus()!=LibraryTransaction.Status.OVERDUE)t.setStatus(LibraryTransaction.Status.OVERDUE);notifications.create(t.getMember(),"Book overdue",t.getBook().getTitle()+" is overdue. Please return it as soon as possible.",Notification.Type.OVERDUE,true);}}
 @Scheduled(cron="${REMINDER_CRON:0 0 9 * * *}") @Transactional public void dueReminders(){LocalDate reminderDate=LocalDate.now().plusDays(reminderDays);for(var t:transactions.findDueOn(reminderDate))notifications.create(t.getMember(),"Due date reminder","Please return "+t.getBook().getTitle()+" by "+t.getDueDate()+".",Notification.Type.DUE_DATE,true);}
}
