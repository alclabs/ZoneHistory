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

import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.green.addonsupport.access.Location;

public class ColorTrendSource
{
   private final String displayPath;
   private final String transientLookupString;

   public ColorTrendSource(Location ancestor, Location eqLocation) throws Exception
   {
       /*
       String parentPath = eqLocation.getParent().getDisplayPath();
       parentPath = parentPath.substring(parentPath.lastIndexOf("/") + 1);
       String childDisplayPath = eqLocation.getDisplayPath();

       displayPath = childDisplayPath.substring(childDisplayPath.indexOf(parentPath));
       */
       displayPath = LocationUtilities.relativeDisplayPath(ancestor, eqLocation);
       transientLookupString = eqLocation.getTransientLookupString();
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

