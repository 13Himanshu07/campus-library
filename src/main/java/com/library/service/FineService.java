package com.library.service;
import com.library.model.Fine;
import java.util.List;
public interface FineService { List<Fine> all(); List<Fine> forUser(long userId); void markPaid(long fineId); void accrueOverdue(long issueId,java.math.BigDecimal amount); }
