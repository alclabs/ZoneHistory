package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.ActionExecutionException;
import com.controlj.green.addonsupport.access.SystemException;
import com.controlj.green.addonsupport.access.aspect.TrendSource;

import java.util.Collection;

public interface Report
{
    public ReportResults runReport(final Collection<? extends TrendSource> sources) throws SystemException, ActionExecutionException;
}
