package com.controlj.addon.zonehistory;

import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.trend.*;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ColorTrendReport
{
   private SystemConnection system;

   public ColorTrendReport(SystemConnection system) throws InvalidConnectionRequestException
   {
      this.system = system;
   }

   public ColorTrendResults runReport(final Date startDate, final Date endDate, final Location start)
         throws SystemException, ActionExecutionException
   {
      final TrendRange range = TrendRangeFactory.byDateRange(startDate, endDate);
      return system.runReadAction(FieldAccessFactory.newFieldAccess(), new ReadActionResult<ColorTrendResults>()
      {
         @Override public ColorTrendResults execute(@NotNull SystemAccess systemAccess) throws Exception
         {
            Collection<EquipmentColorTrendSource> sources = start.find(EquipmentColorTrendSource.class, new ColorAndSetPointAcceptor());

            Map<ColorTrendSource, Map<EquipmentColor, Long>> results = new HashMap<ColorTrendSource, Map<EquipmentColor, Long>>();
            for (EquipmentColorTrendSource source : sources)
               results.put(new ColorTrendSource(source), processTrendData(source, range).getColorMap());

            return new ColorTrendResults(endDate.getTime() - startDate.getTime(), results);
         }
      });
   }

   private ColorTrendProcessor processTrendData(EquipmentColorTrendSource source, TrendRange range) throws TrendException
   {
      TrendData<TrendEquipmentColorSample> tdata = source.getTrendData(range);
      return tdata.process(new ColorTrendProcessor());
   }
}
