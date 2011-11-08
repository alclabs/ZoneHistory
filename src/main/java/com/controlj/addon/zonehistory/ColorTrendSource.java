/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ColorSource

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.zonehistory;

import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;

public class ColorTrendSource
{
   private final String displayPath;
   private final String transientLookupString;

   public ColorTrendSource(EquipmentColorTrendSource source) throws Exception
   {
       String parentPath = source.getLocation().getParent().getDisplayPath();
       parentPath = parentPath.substring(parentPath.lastIndexOf("/") + 1);
       String childDisplayPath = source.getLocation().getDisplayPath();

       displayPath = childDisplayPath.substring(childDisplayPath.indexOf(parentPath));
       transientLookupString = source.getLocation().getTransientLookupString();
   }

   public ColorTrendSource(String displayPath, String transientLookupString)
   {
      this.displayPath = displayPath;
      this.transientLookupString = transientLookupString;
   }

   public String getDisplayPath()
   {
      return displayPath;
   }

   public String getTransientLookupString()
   {
      return transientLookupString;
   }
}

