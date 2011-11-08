/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)TreeIcons

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.zonehistory.util;

import com.controlj.green.addonsupport.access.LocationType;

import static com.controlj.green.addonsupport.access.LocationType.*;

public enum TreeIcon
{
   SystemIcon(System, "system.gif"),
   AreaIcon(Area, "area.gif"),
   SiteIcon(Site, "site.gif"),
   NetworkIcon(Network, "network.gif"),
   DeviceIcon(Device, "hardware.gif"),
   DriverIcon(Driver, "dir.gif"),
   EquipmentIcon(Equipment, "equipment.gif"),
   MicroblockIcon(Microblock, "io_point.gif"),
   MicroblockComponentIcon(MicroblockComponent, "io_point.gif"),
   UnknownIcon(null, "unknown.gif");

   private static final String IMAGE_URL_BASE = "../../../_common/lvl5/skin/graphics/type/";

   private final LocationType locationType;
   private final String image;

   TreeIcon(LocationType locationType, String image)
   {
      this.locationType = locationType;
      this.image = image;
   }

   public String getImageUrl() { return IMAGE_URL_BASE + image; }

   public static TreeIcon findIcon(LocationType locationType)
   {
      for (TreeIcon icon : values())
      {
         if (icon.locationType == locationType)
            return icon;
      }
      return UnknownIcon;
   }
}