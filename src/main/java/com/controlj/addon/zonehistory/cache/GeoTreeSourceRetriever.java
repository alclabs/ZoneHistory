package com.controlj.addon.zonehistory.cache;

import com.controlj.addon.zonehistory.reports.ReportResults;
import com.controlj.addon.zonehistory.util.EnabledColorTrendWithSetpointAcceptor;
import com.controlj.green.addonsupport.access.AspectAcceptor;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import com.controlj.green.addonsupport.access.util.Acceptors;

import java.util.Collection;

/*
* Purpose is to abstract away the cache and the searching for sources given an aspect or source type
* and return a collection of locations and results data (or nulls if no results exist for a particular test)
* */
public class GeoTreeSourceRetriever
{
    private final ReportResults reportResults;

    public GeoTreeSourceRetriever(ReportResults reportResults)
    {
        this.reportResults = reportResults;
    }

    public Collection<AnalogTrendSource> findEISources()
    {
        return collect(AnalogTrendSource.class, Acceptors.aspectByName(AnalogTrendSource.class, "zn_enviro_indx_tn"));
    }

    public Collection<EquipmentColorTrendSource> findColorSources()
    {
        return collect(EquipmentColorTrendSource.class, new EnabledColorTrendWithSetpointAcceptor());
    }

    private <T extends TrendSource> Collection<T> collect(Class<T> aspectClass, AspectAcceptor<T> acceptor)
    {
        return reportResults.getAncestor().find(aspectClass, acceptor);
    }



//    private <T extends TrendSource> T getAspect(Class<T> aspectClass) throws NoSuchAspectException
//    {
//        return reportResults.getAncestor().getAspect(aspectClass);
//    }

//  This is here to potentially speedup the database query for trend sources - a bug in the 1.2.x api prevents us from using this
    /*public Collection<TrendSource> collectTrendSources()
    {
        List<TrendSource> results = new ArrayList<TrendSource>();
        results.addAll(collect(AnalogTrendSource.class, new AspectAcceptor<AnalogTrendSource>()
        {
            @Override public boolean accept(@NotNull AnalogTrendSource source)
            {
                return source.isEnabled() && source.getLocation().getReferenceName().equals("zn_enviro_indx_tn");
            }
        }));
        if (hasAspect(SetPoint.class))
        {
            try {
                results.add(getAspect(EquipmentColorTrendSource.class));
            } catch (NoSuchAspectException ignored) { /* ok, so we won't add one then  }
        }

        return results;
    }   */
}
