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

public class Logging
{
   public static final PrintWriter LOGGER = new PrintWriter(AddOnInfo.getAddOnInfo().getDateStampLogger());
}

