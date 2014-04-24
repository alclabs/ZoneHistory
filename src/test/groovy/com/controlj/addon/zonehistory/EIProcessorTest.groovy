package com.controlj.addon.zonehistory

import com.controlj.green.addonsupport.access.trend.TrendAnalogSample
import com.controlj.green.addonsupport.access.trend.TrendType
import spock.lang.Specification
import com.controlj.addon.zonehistory.reports.EnvironmentalIndexProcessor
import static java.util.concurrent.TimeUnit.*
import static java.util.Calendar.*
import static org.hamcrest.Matchers.closeTo


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

        TestSample(double value, long timestamp)
        {
            this.value = value;
            this.date = new Date(timestamp);
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

    def day(float elapsed)
    {
        def time = new Date(113, SEPTEMBER, 1);
        new Date(time.time + (long)(elapsed * 1000 * 60 * 60 * 24))
    }

    def inDays(long milliTime) {
        milliTime / 1000.0 / 60 / 60 / 24
    }

    def makeSamples(data)   // array of arrays, inner array is [value, days elapsed]
    {
        data.collect([]) {
            if (it)
            {
                if (it.size() == 2)
                {
                    return new TestSample(it[0], day(it[1]))
                } else if (it.size() == 3)
                {
                    return [day(it[1]), day(it[2])]
                }
            }
            return null
        }
    }

    def processData(data, start, end)
    {
        def startTime = day(start);
        def endTime = day(end);

        assert(data.size() >= 2)

        def startBookend = data.head();
        def endBookend = data.last();
        def samples = data.subList(1, data.size()-1)

        def processor = new EnvironmentalIndexProcessor()

        processor.processStart(startTime, (TrendAnalogSample) startBookend)

        samples.each {
            if (it instanceof TrendAnalogSample)
                processor.processData(it)
            else
                processor.processHole(it[0], it[1])
        }

        processor.processEnd(endTime, (TrendAnalogSample) endBookend)
        return processor
    }


    def "test both bookends and one sample"()
    {
        when:
            def p = processData(makeSamples([
                    [70, 0],    // start bookend
                                // start range at 1
                    [90, 2],
                                // end range at 3
                    [95, 4]     // end bookend
            ]), 1, 3)
        then:
            p.averageEI == 88.125
            inDays(p.occupiedTime) == 2
            p.totalTime == p.occupiedTime


        when: "move the start and end out by .5"
            p = processData(makeSamples([
                    [70, 0],    // start bookend
                                // start range at .5
                    [90, 2],
                                // end range at 3.5
                    [95, 4]     // end bookend
            ]), 0.5, 3.5)
        then:
            p.averageEI == 87.1875
            inDays(p.occupiedTime) == 3
            p.totalTime == p.occupiedTime
    }

    def "test unoccupied"()
    {
        /*
        when:
            def p = processData(makeSamples([
                    [90, 0],    // start bookend
                                // start range at 1
                    [90, 2],
                    [0, 2],     // unoccupied for 2
                                // end range at 3
                    [90, 4]     // end bookend
            ]), 1, 4)
        then:
            p.averageEI == 90
            inDays(p.occupiedTime) == 1
            inDays(p.totalTime) == 3

        */
        when:
            def p = processData(makeSamples([
                    [0, 0],    //start bookend
                    [90, 1.5],
                    [90, 2]

            ]), 1, 2)
        then:
        p.averageEI == 90
        inDays(p.occupiedTime) == 0.5
        inDays(p.totalTime) == 1
    }

    def "test no end bookend"()
    {
        when:
            def p = processData(makeSamples([
                    [70, 0],    // start bookend
                                // start range at 1
                    [80, 2],
                                // end range at 3
                    null        // end bookend
            ]), 1, 3)
        then:
            p.averageEI == 78.75    // assume last sample doesn't change until end of range
            inDays(p.occupiedTime) == 2
            p.totalTime == p.occupiedTime
    }

    def "test no start bookend"()
    {
        when:
            def p = processData(makeSamples([
                    null,       // start bookend
                                // start range at 1
                    [70, 1],
                    [80, 2],
                                // end range at 3
                    [90, 4]     // end bookend
            ]), 0.5, 3)
        then:
            p.averageEI == 77
            inDays(p.occupiedTime) == 2.5
            inDays(p.totalTime) == 2.5
    }

    def "no bookends"()
    {
        when:
            def p = processData(makeSamples([
                    null,       // start bookend
                                // start range at 1
                    [70, 1],
                    [80, 2],
                    [60, 3],
                                // end range at 4
                    null        // end bookend
            ]), 0, 4)
        then:
            p.averageEI == 68.75
            inDays(p.occupiedTime) == 4
            inDays(p.totalTime) == 4
    }


    def "test both bookends, samples and a hole"()
    {
        when: "move the start and end out by .5"
            def p = processData(makeSamples([
                    [70, 0],    // start bookend
                                // start range at 1
                    [80, 2],
                    [0,  3, 4], // hole from 3-4
                                // end range at 5
                    [90, 5]     // end bookend
            ]), 1, 5)
        then:
            p.averageEI == 82.5
            inDays(p.occupiedTime) == 3
            inDays(p.totalTime) == 4
    }


    def "test change of color within range"()
    {
        given:
            def start = date(2011, SEPTEMBER, 21)
            def end = date(2011, SEPTEMBER, 23)
            def startBookend = new TestSample(70, start-1)
            def endBookend = new TestSample(95, end+1)
            def rangeSample = new TestSample(90, date(2011, SEPTEMBER, 22))

        when: "have all needed info and ei has changed within range"
            def processor = new EnvironmentalIndexProcessor()
            processor.processStart(start, startBookend)
            processor.processData(rangeSample)
            processor.processEnd(end, endBookend)

        then: "average ei should be 90 * value of time between changes + 95 + day of change + 0 * day divided by the total operational time"
            processor.averageEI == 88.125;
    }

 }
