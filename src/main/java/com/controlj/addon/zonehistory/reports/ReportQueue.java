package com.controlj.addon.zonehistory.reports;

public class ReportQueue
{
    /*
    * This class is to allow the results servlet to run reports
    * Class will then collect the sources using the GeoSourceTreeRetriever and run the reports using the sources in the collection
    * Each report will cache the data but only change the needed data within each test (EI will only change stuff related to EI)
    *
    * This will be messy because there is no easy way to handle tests that depend on another's data and continue processing using another report and
     * caching only some data. It is possible to multithread this to speed up the report.
     *
     *
     * NOTE:
     * This was intended to help with tests that required a sequence in order to run. If it does not, this is inappropriate.
     * A class that changes the data with independent reports is better.
     *
    * */
}
