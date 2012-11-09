package com.controlj.addon.zonehistory

import com.controlj.green.addonsupport.access.trend.TrendAnalogSample
import com.controlj.green.addonsupport.access.trend.TrendType
import spock.lang.Specification

class EIProcessorTest extends Specification
{
    /*
    * Add tests that correctly sum the cooling, heating, occupied, and total times
    *
    * Add tests to correctly calculate the average EI
    *
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
    // with only bookends
    // test with one value
    // test with value that changes
    // test with values that change to 0
    // test with values that change from 0
    // test holes
    // process holes and data

 }
