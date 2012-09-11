package com.controlj.addon.zonehistory.reports;

public class NoEnviroIndexSourcesException extends Exception
{
    private String reason;

    public NoEnviroIndexSourcesException(String s)
    {
        reason = s;
    }

    public String getMessage()
    {
        return reason;
    }
}
