/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)Logging

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.zonehistory.util;

import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.FileLogger;

import java.io.PrintWriter;
import java.io.Writer;

public class Logging
{
   public static final PrintWriter LOGGER;
    static {
        Writer dateStampLogger = null;
        try {
            dateStampLogger = AddOnInfo.getAddOnInfo().getDateStampLogger();
        } catch (Throwable nfe) {}
        // AddOnInfo not available during unit tests, just use System.err
        if (dateStampLogger != null) {
            LOGGER = new PrintWriter(dateStampLogger);
        } else {
            LOGGER = new PrintWriter(System.err);
        }
    }
}

