package com.controlj.addon.zonehistory.charts;

import com.controlj.addon.zonehistory.reports.Report;
import com.controlj.addon.zonehistory.reports.ReportResults;
import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class PieChartJSONBuilder
{
    public abstract JSONObject buildPieChartJSON(Report report, ReportResults reportResults) throws Exception;

    protected Map<Integer, Long> combineResultsForAllSources(ReportResults reportResults) throws Exception
    {
        // build this piechart for mainchart - worry about individual ones later

        Map<Integer, Long> results = new HashMap<Integer, Long>();
        for (TrendSource source : reportResults.getSources())
        {
            ReportResultsData resultsData = reportResults.getDataFromSource(source);
            Map<Integer, Long> data = resultsData.getData();

            for (Integer i : data.keySet())
            {
                long newTime = data.get(i);
                long currentTimeInResults = results.get(i) == null ? 0 : results.get(i);

                results.put(i, newTime + currentTimeInResults);
            }
        }

        return results;
    }
}
