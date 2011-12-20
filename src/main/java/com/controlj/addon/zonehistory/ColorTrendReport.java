package com.controlj.addon.zonehistory;

import com.controlj.addon.zonehistory.cache.ZoneHistory;
import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AttachedEquipment;
import com.controlj.green.addonsupport.access.trend.*;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import org.apache.commons.lang.time.StopWatch;
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
            StopWatch timer = new StopWatch();
            timer.start();
            Collection<ZoneHistory> zoneHistories = ZoneHistoryCache.INSTANCE.getDescendantZoneHistories(start);
            if (zoneHistories == null)
            {
                Collection<EquipmentColorTrendSource> sources = start.find(EquipmentColorTrendSource.class, new EnabledColorTrendWithSetpointAcceptor());
                ArrayList<ZoneHistory> newHistories = new ArrayList<ZoneHistory>();
                for (EquipmentColorTrendSource source : sources) {
                    newHistories.add(new ZoneHistory(source.getLocation()));
                }
                zoneHistories = ZoneHistoryCache.INSTANCE.addDescendantZoneHistories(start, newHistories);
             }
            timer.stop();
            Logging.LOGGER.println("Search for trend sources beneath '"+start.getDisplayPath()+"' took "+timer);


            StopWatch processTimer = new StopWatch();
            processTimer.start();
            processTimer.suspend();

            Map<ColorTrendSource, Map<EquipmentColor, Long>> results = new HashMap<ColorTrendSource, Map<EquipmentColor, Long>>();
            for (ZoneHistory zoneHistory : zoneHistories)
            {
                EquipmentColorTrendSource source = zoneHistory.getEquipmentColorLocation().getAspect(EquipmentColorTrendSource.class);
                try {
                    Location equipment = LocationUtilities.findMyEquipment(source.getLocation());
                    AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);
                    if (!eqAspect.getDevice().isOutOfService())
                    {
                        if (ColorTrendProcessor.trace)
                        {
                            Logging.LOGGER.println("------ Processing "+equipment.getDisplayName());
                        }
                        processTimer.resume();
                        ColorTrendProcessor processor = processTrendData(source, range);
                        processTimer.suspend();

                        results.put(new ColorTrendSource(start, equipment), processor.getColorMap());
                    }
                } catch (Exception e) {
                    Logging.LOGGER.println("Error processing trend data");
                    e.printStackTrace(Logging.LOGGER);
                }
            }
            Logging.LOGGER.println("Processing trend sources beneath '"+start.getDisplayPath()+"' took "+processTimer);
            return new ColorTrendResults(results);
         }
      });
   }

   private ColorTrendProcessor processTrendData(EquipmentColorTrendSource source, TrendRange range) throws TrendException
   {
      TrendData<TrendEquipmentColorSample> tdata = source.getTrendData(range);
      return tdata.process(new ColorTrendProcessor());
   }

}
