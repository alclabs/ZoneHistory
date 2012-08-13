package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.ActionExecutionException;
import com.controlj.green.addonsupport.access.SystemException;

public abstract class Report
{
    public abstract ReportResults runReport() throws SystemException, ActionExecutionException;

}
