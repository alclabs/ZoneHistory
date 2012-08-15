/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ColorTrendResults

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.ColorPie;
import com.controlj.addon.zonehistory.ColorSlice;
import com.controlj.addon.zonehistory.ColorTrendSource;
import com.controlj.green.addonsupport.access.EquipmentColor;

import java.util.*;

public class ReportResults
{
   private final Map<ColorTrendSource, Map<EquipmentColor, Long>> results;

   public ReportResults(Map<ColorTrendSource, Map<EquipmentColor, Long>> results)
   {
      this.results = results;
   }

   public Set<ColorTrendSource> getSources()
   {
      return results.keySet();
   }

   public ColorPie getPieForSource(ColorTrendSource source)
   {
      Map<EquipmentColor, Long> equipmentColorMap = results.get(source);
      return computeResult(Collections.singleton(equipmentColorMap));
   }

   public ColorPie getTotalPie()
   {
      return computeResult(results.values());
   }

   private ColorPie computeResult(Collection<Map<EquipmentColor, Long>> mapList)
   {
      Map<EquipmentColor, ColorSlice> results = new HashMap<EquipmentColor, ColorSlice>();
      for (Map<EquipmentColor, Long> colorMap : mapList)
      {
         for (Map.Entry<EquipmentColor, Long> colorEntry : colorMap.entrySet())
         {
            EquipmentColor color = colorEntry.getKey();
            ColorSlice slice = results.get(color);
            if (slice == null)
            {
               slice = new ColorSlice(color);
               results.put(color, slice);
            }
            // add to a slice's timeInColor
            slice.addTimeInColor(colorEntry.getValue());
         }
      }

      return new ColorPie(results.values());
   }
}