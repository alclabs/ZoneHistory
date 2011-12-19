package com.controlj.addon.zonehistory;

import com.controlj.addon.zonehistory.util.FindNodes;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AttachedEquipment;
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
      return system.runReadAction(new ReadActionResult<ColorTrendResults>()
      {
         @Override public ColorTrendResults execute(@NotNull SystemAccess systemAccess) throws Exception
         {
            Collection<EquipmentColorTrendSource> sources = start.find(EquipmentColorTrendSource.class, new EnabledColorTrendWithSetpointAcceptor());

            Map<ColorTrendSource, Map<EquipmentColor, Long>> results = new HashMap<ColorTrendSource, Map<EquipmentColor, Long>>();
            for (EquipmentColorTrendSource source : sources)
            {
                try {
                    Location equipment = FindNodes.findMyEquipment(source.getLocation());
                    AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);
                    if (!eqAspect.getDevice().isOutOfService())
                    {
                        if (ColorTrendProcessor.trace)
                        {
                            Logging.LOGGER.println("------ Processing "+equipment.getDisplayName());
                        }
                        results.put(new ColorTrendSource(equipment), processTrendData(source, range).getColorMap());
                    }
                } catch (Exception e) {
                    Logging.LOGGER.println("Error processing trend data");
                    e.printStackTrace(Logging.LOGGER);
                }
            }

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
