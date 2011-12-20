package com.controlj.addon.zonehistory

import spock.lang.Specification
import com.controlj.green.addonsupport.access.EquipmentColor
import com.controlj.green.addonsupport.access.trend.TrendEquipmentColorSample
import com.controlj.green.addonsupport.access.trend.TrendType
import static java.util.Calendar.*
import static java.util.concurrent.TimeUnit.*
import static com.controlj.green.addonsupport.access.EquipmentColor.*
import static org.hamcrest.Matchers.closeTo

class ColorTrendProcessorTest extends Specification
{
    def date(int year, int month, int day) { new Date(year-1900, month, day) }
    class TestSample implements TrendEquipmentColorSample
    {
        EquipmentColor color
        Date date

        TestSample(EquipmentColor color, Date date)
        {
            this.color = color
            this.date = date
        }

        @Override EquipmentColor value() { color }
        @Override Date getTime() { date }
        @Override long getTimeInMillis() { date.time }
        @Override TrendType getType() { TrendType.DATA }
        @Override float getSpecialValue() { 0 }
    }
    def ONE_DAY = MILLISECONDS.convert(1, DAYS)
    def TWO_DAYS = MILLISECONDS.convert(2, DAYS)



    def "test when no data"()
    {
        given:
            def processor = new ColorTrendProcessor()
            def start = date(2011, SEPTEMBER, 21)
            def end = date(2011, SEPTEMBER, 23)

        when:
            processor.processStart(start, (TrendEquipmentColorSample) null)
            processor.processEnd(end, (TrendEquipmentColorSample) null)
        then:
            processor.colorMap == [(UNKNOWN) : TWO_DAYS]
            processor.percentCoverage == 0
    }

    def "test when only bookends"()
    {
        given:
            def start = date(2011, SEPTEMBER, 21)
            def end = date(2011, SEPTEMBER, 23)
            def startBookend = new TestSample(OCCUPIED, start-1)
            def endBookend = new TestSample(UNOCCUPIED, end+1)

        when: "only have a start bookend (no end bookend)"
            def processor = new ColorTrendProcessor()
            processor.processStart(start, startBookend)
            processor.processEnd(end, (TrendEquipmentColorSample) null)

        then: "the value should be unknown for the time requested"
            processor.colorMap == [(UNKNOWN) : TWO_DAYS]
            processor.percentCoverage == 0

        when: "only have a end bookend (no start bookend)"
            processor = new ColorTrendProcessor()
            processor.processStart(start, (TrendEquipmentColorSample) null)
            processor.processEnd(end, endBookend)
        then: "the value should be unknown for the time requested"
            processor.colorMap == [(UNKNOWN) : TWO_DAYS]
            processor.percentCoverage == 0

        when: "have both start and end bookend"
            processor = new ColorTrendProcessor()
            processor.processStart(start, startBookend)
            processor.processEnd(end, endBookend)
        then: "the value of the start bookend should be the value for the whole time requested"
            processor.colorMap == [(OCCUPIED) : TWO_DAYS]
            processor.percentCoverage == 100.0
    }

    def "test change of color within range"()
    {
        given:
            def start = date(2011, SEPTEMBER, 21)
            def end = date(2011, SEPTEMBER, 23)
            def startBookend = new TestSample(OCCUPIED, start-1)
            def endBookend = new TestSample(UNOCCUPIED, end+1)
            def rangeSample = new TestSample(OPERATIONAL, date(2011, SEPTEMBER, 22))

        when: "have all needed info and EqColorSource has changed within range"
            def processor = new ColorTrendProcessor()
            processor.processStart(start, startBookend)
            processor.processData(rangeSample)
            processor.processEnd(end, endBookend)

        then: "map should have two different colors each with 1 day's time in each"
            processor.colorMap == [(OCCUPIED) : ONE_DAY, (OPERATIONAL) : ONE_DAY]
            processor.percentCoverage == 100.0

        when: "there is data, but no ending bookend"
            processor = new ColorTrendProcessor()
            processor.processStart(start, startBookend)
            processor.processData(rangeSample)
            processor.processEnd(end, (TrendEquipmentColorSample) null)

        then: "map should have only the first day, with unknown for the rest"
            processor.colorMap == [(OCCUPIED) : ONE_DAY, (UNKNOWN) : ONE_DAY]
            processor.percentCoverage == 50.0
    }

    def "test holes within range - same color at start, middle, and end"()
    {
        given:
            def start = date(2011, SEPTEMBER, 20)
            def end = date(2011, SEPTEMBER, 23)
            def startBookend = new TestSample(OCCUPIED, start-1)
            def endBookend = new TestSample(UNOCCUPIED, end+1)

        when: "a hole in the data within the range"
            def processor = new ColorTrendProcessor()
            processor.processStart(start, startBookend)
            processor.processHole(start+1, end-1)
            processor.processEnd(end, endBookend)
        then: "should expect one color with 2 days of data"
            processor.colorMap == [(OCCUPIED) : ONE_DAY, (UNKNOWN) : TWO_DAYS]
            ((double)processor.percentCoverage) closeTo(33.3, 0.1)

        when: "a hole at the start of the beginning of the range"
            processor = new ColorTrendProcessor();
            processor.processStart(start, startBookend)
            processor.processHole(start, start+1)
            processor.processEnd(end, endBookend)
        then: "should expect to see 0 days of Occupied and total duration of unknown"
            processor.colorMap == [(OCCUPIED) : 0, (UNKNOWN) : ONE_DAY * 3]

        when: "a hole at the end of the range"
            processor = new ColorTrendProcessor();
            processor.processStart(start, startBookend)
            processor.processHole(end-2, end)
            processor.processEnd(end, endBookend)
        then:
            processor.colorMap == [(OCCUPIED) : ONE_DAY, (UNKNOWN) : TWO_DAYS]
    }

    def "process holes and data"()
    {
        given: "one week's data"
            def start = date(2011, SEPTEMBER, 20)
            def end = date(2011, SEPTEMBER, 27)
            def startBookend = new TestSample(OCCUPIED, start-1)
            def endBookend = new TestSample(UNOCCUPIED, end+1)
            def testSample1 = new TestSample(OPERATIONAL, start+3)
            def testSample2 = new TestSample(MODERATE_COOLING, end-2)

        when: "hole at start+2, 1 day hole, 1 day operational, 1 day hole, 3 day mod cool"
            def processor = new ColorTrendProcessor()
            processor.processStart(start, startBookend)
            processor.processHole(start+2, start+3)
            processor.processData(testSample1)
            processor.processHole(start+4, start+5)
            processor.processData(testSample2)
            processor.processEnd(end, endBookend)
        then: "expect 2 of 1st bookend, 1 day testSample1, 2 days testSample2"
            processor.colorMap == [(OPERATIONAL) : ONE_DAY, (OCCUPIED) : TWO_DAYS, (MODERATE_COOLING) : TWO_DAYS, (UNKNOWN) : TWO_DAYS]
            processor.totalTime == ONE_DAY * 7
            ((double)processor.percentCoverage) closeTo(500.0/7.0, 0.1)
    }


}
