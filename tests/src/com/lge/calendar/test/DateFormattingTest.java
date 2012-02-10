package com.lge.calendar.test;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import com.lge.calendar.DateFormatUtil;
import com.lge.calendar.KoreanLunisolarCalendar;

public class DateFormattingTest extends TestCase {
	public void testMonthStringsForLeapYear() throws Exception {
		Calendar c = new KoreanLunisolarCalendar(new Date());
		c.set(Calendar.YEAR, 2012);
		assertEquals(2012, c.get(Calendar.YEAR));
		String[] monthString = DateFormatUtil.getMonthStrings(c);
		assertEquals(13, monthString.length);
		assertEquals("1��, 2��, 3��, 3��(��), 4��, 5��, 6��, 7��, 8��, 9��, 10��, 11��, 12��", join(monthString, ", "));
	}

	private String join(Object[] arr, String sep) {
		StringBuffer buf = new StringBuffer();
		if (arr.length != 0)
			buf.append(arr[0].toString());
		for (int i=1; i<arr.length; i++) {
			buf.append(sep);
			buf.append(arr[i]);
		}
		return buf.toString();
	}
}
