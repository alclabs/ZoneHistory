package com.controlj.addon.zonehistory.cache

import spock.lang.Specification
import com.controlj.green.addonsupport.access.Location
import com.controlj.addon.zonehistory.reports.ReportResultsData

/**
 * 
 */
class ZoneHistoryCacheTest extends Specification {
    Location loc1 = Mock(Location);
    Location loc2 = Mock(Location);
    Location loc3 = Mock(Location);

    DateRange dateRange1 = new DateRange(new Date(0), new Date(1516351531));
    DateRange dateRange2 = new DateRange(new Date(567467), new Date());
    ReportResultsData resultsData1 = new ReportResultsData(0, "", "", "");
    ReportResultsData resultsData2 = new ReportResultsData(0, "2", "2", "2");


    def setup() {
        ZoneHistoryCache.CACHE.reset();
    }

    def "getOfficialZoneHistory returns canonical history"() {
        setup:
        String lus =  "loc1"
        String lus2 = "loc2"

        ZoneHistoryCache.CACHE.cacheResultsData(lus, dateRange1, resultsData1)
        ZoneHistoryCache.CACHE.cacheResultsData(lus2, dateRange2, resultsData2)


        when: "get two different ZoneTimeHistory with same loc"
            ReportResultsData result1 = ZoneHistoryCache.CACHE.getCachedData(lus, dateRange1)
            ReportResultsData result1_2 = ZoneHistoryCache.CACHE.getCachedData(lus, dateRange1)

        then: "always get the same one (first)"
            result1.is(result1_2)

        when: "try a different zone history, then the original"
            result1_2 = ZoneHistoryCache.CACHE.getCachedData(lus2, dateRange2)
            !result1.is(result1_2);

        then:
            result1.is(ZoneHistoryCache.CACHE.getCachedData(lus, dateRange1))

    }

    def "descendant list works"() {
        setup:
            DateRange testRange = new DateRange(7);
            String lus = "loc1"
            def initial = ZoneHistoryCache.CACHE.getCachedData(lus, testRange)

        when: "add histories then read them back"
            ZoneHistoryCache.CACHE.cacheResultsData(lus, testRange, resultsData1);
            def result = ZoneHistoryCache.CACHE.getCachedData(lus, testRange)

        then: "initial read is null, then read set value back"
            initial == null
            result == resultsData1

        when: "Add another set to same location and read it back"
            ZoneHistoryCache.CACHE.cacheResultsData(lus, testRange, resultsData1)
            result = ZoneHistoryCache.CACHE.getCachedData(lus, testRange)

        then: "result is original"
            result == resultsData1
    }
}
