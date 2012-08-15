package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.ActionExecutionException;
import com.controlj.green.addonsupport.access.SystemException;

public interface Report
{
    public ReportResults runReport() throws SystemException, ActionExecutionException;
}
