package com.controlj.addon.zonehistory

import com.controlj.green.addonsupport.access.trend.TrendAnalogSample
import com.controlj.green.addonsupport.access.trend.TrendType
import spock.lang.Specification
import com.controlj.addon.zonehistory.reports.EnvironmentalIndexProcessor
import static java.util.concurrent.TimeUnit.*
import static java.util.Calendar.*


class EIProcessorTest extends Specification
{
    /*
    * Add tests that correctly sum the cooling, heating, occupied, and total times
    * Add tests to correctly calculate the average EI for groups
    * */

    def date(int year, int month, int day) { new Date(year-1900, month, day) }
    class TestSample implements TrendAnalogSample
    {
        double value
        Date date

        TestSample(double value, Date date)
        {
            this.value = value
            this.date = date
        }

        @Override Date      getTime()         { date }
        @Override long      getTimeInMillis() { date.time }
        @Override TrendType getType()         { TrendType.DATA }
        @Override float     getSpecialValue() { 0 }
        @Override double    doubleValue()     { return value }
        @Override float     floatValue()      { return 0 }
    }

    def ONE_DAY  = MILLISECONDS.convert(1, DAYS)
    def TWO_DAYS = MILLISECONDS.convert(2, DAYS)

    // look at colortrendprocessortest to assist determine tests

    // test with no data
    def "test when no data"()
    {
        given:
            def processor = new EnvironmentalIndexProcessor()
            // same day so no data will be found
            def start = date(1971, SEPTEMBER, 21)
            def end = date(1971, SEPTEMBER, 21)

        when:
            processor.processStart(start, (TestSample) null)
            processor.processEnd(end, (TestSample) null)
        then:
            processor.occupiedTime == 0;
            processor.averageEI == 0;
            processor.area == 0.0;
    }

    // with only bookends
    def "test when only bookends"()
    {
        given:
            def start = date(2011, SEPTEMBER, 21)
            def end = date(2011, SEPTEMBER, 23)
            def startBookend = new TestSample(0, start-1)
            def endBookend = new TestSample(0, end+1)

        when: "only have a start bookend (no end bookend)"
            def processor = new EnvironmentalIndexProcessor()
            processor.processStart(start, startBookend)
            processor.processEnd(end, (TestSample) null)

        then: "total time for the time requested"
            processor.getTotalTime() == TWO_DAYS


        when: "only have a end bookend (no start bookend)"
            processor = new EnvironmentalIndexProcessor()
            processor.processStart(start, (TestSample) null)
            processor.processEnd(end, endBookend)
        then: "the value should be unoccupied for the time requested"
            processor.occupiedTime == 0

        when: "have both start and end bookend"
            processor = new EnvironmentalIndexProcessor()
            processor.processStart(start, startBookend)
            processor.processEnd(end, endBookend)
        then: "the value of the start bookend should be the value for the whole time requested"
            processor.area == 0.0
            processor.totalTime == TWO_DAYS
    }

    def "test change of color within range"()
    {
        given:
            def start = date(2011, SEPTEMBER, 21)
            def end = date(2011, SEPTEMBER, 23)
            def startBookend = new TestSample(0, start-1)
            def endBookend = new TestSample(95, end+1)
            def rangeSample = new TestSample(90, date(2011, SEPTEMBER, 22))

        when: "have all needed info and ei has changed within range"
            def processor = new EnvironmentalIndexProcessor()
            processor.processStart(start, startBookend)
            processor.processData(rangeSample)
            processor.processEnd(end, endBookend)

        then: "average ei should be 90 * value of time between changes + 95 + day of change + 0 * day divided by the total operational time"
            processor.averageEI == 22.5;

        when: "there is data, but no ending bookend"
            processor = new EnvironmentalIndexProcessor()
            processor.processStart(start, startBookend)
            processor.processData(rangeSample)
            processor.processEnd(end, (TestSample) null)

        then: "no ending bookend will not be the same value"
            processor.averageEI == 45.0;
    }
 }
