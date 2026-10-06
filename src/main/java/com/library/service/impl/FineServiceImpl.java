package com.library.service.impl;

import com.library.dao.FineDAO;
import com.library.model.Fine;
import com.library.service.FineService;
import java.util.List;

public class FineServiceImpl implements FineService {
    private final FineDAO fines;
    public FineServiceImpl(FineDAO fines){this.fines=fines;}
    @Override public List<Fine> all(){return fines.findAll();}
    @Override public List<Fine> forUser(long userId){return fines.findForUser(userId);}
    @Override public void markPaid(long fineId){fines.markPaid(fineId);}
    @Override public void accrueOverdue(long issueId,java.math.BigDecimal amount){fines.accrueOverdue(issueId,amount);}
}
