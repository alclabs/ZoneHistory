/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ColorSource

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.zonehistory.util;

import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.UnresolvableException;

public class ColorTrendSource
{
    private final String displayPath;
    private final String transientLookupString;
    private String transientLookupPathString;

    public ColorTrendSource(Location ancestor, Location eqLocation) throws Exception
    {
        displayPath = LocationUtilities.relativeDisplayPath(ancestor, eqLocation);
        transientLookupString = eqLocation.getTransientLookupString();
        transientLookupPathString = createTransientLookupPathString(eqLocation);
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

    public String getTransientLookupPathString()
    {
        return transientLookupPathString;
    }

    public String createTransientLookupPathString(Location loc)
    {
        StringBuffer result = new StringBuffer();
        Location current = loc;
        while (true)
        {
            if (result.length() != 0)
            {   // there is other content, prepend delimeter
                result.insert(0, "/");
            }
            result.insert(0, current.getTransientLookupString());
            if (current.hasParent())
            {
                try
                {
                    current = current.getParent();
                }
                catch (UnresolvableException e)
                {
                    throw new RuntimeException("programming error - can't find parent when hasParent is true");
                } // shouldn't happen
            }
            else
            {
                break;
            }
        }
        return result.toString();
    }
}

