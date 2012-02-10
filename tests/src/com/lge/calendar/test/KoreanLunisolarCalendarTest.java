package com.lge.calendar.test;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.lge.calendar.KoreanLunisolarCalendar;


import junit.framework.TestCase;

public class KoreanLunisolarCalendarTest extends TestCase {
	private static final int DAY_IN_MILLIS = 1000 * 60 * 60 * 24;


	public void test1970_01_01_gregori_inMillis() {
		GregorianCalendar gc = new GregorianCalendar(
				TimeZone.getTimeZone("GMT+0"));
		gc.setTimeInMillis(0);
		assertDay(1970, 0, 1, gc);
		assertEquals(0, gc.get(Calendar.HOUR_OF_DAY));
	}

	public void test20120101_fromGregorianCalendar() throws Exception {
		Calendar c = new KoreanLunisolarCalendar(new GregorianCalendar(2012, 0,
				23, 0, 0, 0).getTime());
		assertDay(2012, 0, 1, c);
	}

	public void test19700101_fromGregorianCalendar() throws Exception {
		Calendar c = new KoreanLunisolarCalendar(new GregorianCalendar(1970, 1,
				6, 0, 0, 0).getTime());
		assertDay(1970, 0, 1, c);
	}

	public void test1970_1_1() throws Exception {
		Calendar c = new KoreanLunisolarCalendar(0);
		assertDay(1969, 10, 24, c);
	}

	public void test1970_1_1_Gregorian() throws Exception {
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(0);
		assertDay(1970, 0, 1, c);
	}

	public void test1970_1_2_Gregorian() throws Exception {
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(DAY_IN_MILLIS);
		assertDay(1970, 0, 2, c);
	}

	public void testDaysFromMillis() throws Exception {
		assertEquals(0, KoreanLunisolarCalendar.daysSince1970(0));
		assertEquals(1, KoreanLunisolarCalendar.daysSince1970(DAY_IN_MILLIS));
	}

	private void assertDay(int year, int month, int day, Calendar c) {
		assertEquals(year, c.get(Calendar.YEAR));
		assertEquals(month, c.get(Calendar.MONTH));
		assertEquals(day, c.get(Calendar.DAY_OF_MONTH));
	}

	public void testDaysIn1969() throws Exception {
		int days = KoreanLunisolarCalendar.getDaysInYear(1969);
		assertEquals(354, days);
	}

	public void testMonthsInYear() {
		assertEquals(13, KoreanLunisolarCalendar.getMonthsInYear(2012));
	}

	public void testLeapMonth() {
		assertEquals(3, KoreanLunisolarCalendar.getLeapMonthInYear(2012));
	}

	public void testAddDay() {
		Calendar c = new KoreanLunisolarCalendar(new GregorianCalendar(2012, 0,
				22, 0, 0, 0).getTime());
		assertDay(2011, 11, 29, c);

		c.add(Calendar.DAY_OF_MONTH, 1);
		assertDay(2012, 0, 1, c);
	}
	
	public void testGetActualMax() throws Exception {
		Calendar c = new KoreanLunisolarCalendar(new GregorianCalendar(2012, 0,
				23, 0, 0, 0).getTime());
		
		Calendar cl = (Calendar) c.clone();
		cl.set(Calendar.MONTH, 11);
		assertEquals(11, cl.get(Calendar.MONTH));
		
		cl.set(Calendar.MONTH, 12);
		assertEquals(12, cl.get(Calendar.MONTH));
		
		assertEquals(12, c.getActualMaximum(Calendar.MONTH));
	}
	
	public void testGregorianBasis() {
		Calendar c = new GregorianCalendar(1970, 0, 1, 9, 0, 0);
		assertEquals(0, c.getTimeInMillis());
	}
	
	public void testGetTimeInMillis() throws Exception {
		Calendar c = new KoreanLunisolarCalendar();
		c.set(1969, 10, 24, 9, 0, 0); // korean standard time
		assertEquals(0, c.getTimeInMillis()); // because millis is from GMT+0
	}

	public void xtestRollMonth() throws Exception {
		Calendar c = new KoreanLunisolarCalendar();
		c.set(2011, 11, 29, 9, 0, 0); // korean standard time
		c.roll(Calendar.MONTH, true); // +1 for month should not change the year field
		assertDay(2011, 0, 29, c);
		c.roll(Calendar.MONTH, false); // +1 for month should not change the year field
		assertDay(2011, 11, 29, c);
	}
}
