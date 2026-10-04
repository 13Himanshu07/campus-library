package com.library.service.impl;

import com.library.dao.DashboardDAO;
import com.library.service.DashboardService;
import java.util.Map;

public class DashboardServiceImpl implements DashboardService {
    private final DashboardDAO dashboard;
    public DashboardServiceImpl(DashboardDAO dashboard){this.dashboard=dashboard;}
    @Override public Map<String,Long> statistics(){return dashboard.statistics();}
}
