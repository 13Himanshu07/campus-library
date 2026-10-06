package com.library.concurrent;

import com.library.model.BookIssue;
import com.library.service.FineService;
import com.library.service.IssueService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Recalculates and persists current unpaid fines for overdue loans. */
public final class FineCalculationTask implements Runnable {
    private static final Logger LOGGER=Logger.getLogger(FineCalculationTask.class.getName());
    private final IssueService issues;
    private final FineService fines;
    private final BigDecimal finePerDay;
    public FineCalculationTask(IssueService issues,FineService fines,BigDecimal finePerDay){if(issues==null||fines==null||finePerDay==null||finePerDay.signum()<0)throw new IllegalArgumentException("Fine task dependencies and rate must be valid.");this.issues=issues;this.fines=fines;this.finePerDay=finePerDay;}
    @Override public void run(){try{int processed=0;for(BookIssue issue:issues.overdue()){try{long days=Math.max(0,LocalDate.now().toEpochDay()-issue.getDueDate().toEpochDay());fines.accrueOverdue(issue.getId(),finePerDay.multiply(BigDecimal.valueOf(days)));processed++;}catch(RuntimeException e){LOGGER.log(Level.SEVERE,"Could not calculate overdue fine for issue "+issue.getId(),e);}}LOGGER.info("Overdue fine calculation completed; updated "+processed+" loan(s).");}catch(RuntimeException e){LOGGER.log(Level.SEVERE,"Could not load overdue loans for fine calculation.",e);}}
}
