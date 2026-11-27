package com.library.concurrent;

import com.library.model.BookIssue;
import com.library.service.FineService;
import com.library.service.IssueService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Recalculates and persists current unpaid fines for overdue loans. */
public final class FineCalculationTask implements Runnable {
    private static final Logger LOGGER=LoggerFactory.getLogger(FineCalculationTask.class);
    private final IssueService issues;
    private final FineService fines;
    private final BigDecimal finePerDay;
    public FineCalculationTask(IssueService issues,FineService fines,BigDecimal finePerDay){if(issues==null||fines==null||finePerDay==null||finePerDay.signum()<0)throw new IllegalArgumentException("Fine task dependencies and rate must be valid.");this.issues=issues;this.fines=fines;this.finePerDay=finePerDay;}
    @Override public void run(){try{int processed=0;for(BookIssue issue:issues.overdue()){try{long days=Math.max(0,LocalDate.now().toEpochDay()-issue.getDueDate().toEpochDay());fines.accrueOverdue(issue.getId(),finePerDay.multiply(BigDecimal.valueOf(days)));processed++;}catch(RuntimeException e){LOGGER.error("fine_calculation_failed issue_id={} exception_type=\"{}\"",issue.getId(),com.library.util.LogFields.quoted(e.getClass().getSimpleName()),e);}}LOGGER.info("fine_calculation_complete updated_loans={}",processed);}catch(RuntimeException e){LOGGER.error("fine_calculation_load_failed exception_type=\"{}\"",com.library.util.LogFields.quoted(e.getClass().getSimpleName()),e);}}
}
