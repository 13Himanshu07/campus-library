package com.library.dao;
import com.library.model.Fine;
import java.util.List;
public interface FineDAO { List<Fine> findAll(); List<Fine> findForUser(long userId); void markPaid(long fineId); }
