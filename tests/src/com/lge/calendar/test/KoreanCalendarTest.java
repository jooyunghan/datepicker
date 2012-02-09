package com.lge.calendar.test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import com.ibm.icu.util.ChineseCalendar;
import com.lge.calendar.KoreanCalendar;

public class KoreanCalendarTest extends TestCase {
	
	public void testChineseCal() throws Exception {
		ChineseCalendar cal = new ChineseCalendar(new GregorianCalendar(2012, 4-1, 21).getTime());
		assertEquals("4/1", format(cal));
	}
	
	public void testKorean() throws Exception {
		KoreanCalendar cal = new KoreanCalendar(new GregorianCalendar(2012, 4-1, 21).getTime());
		assertEquals("4/1", format(cal));
		assertFalse(cal.isLeapMonth());
	}
	
	private String format(ChineseCalendar cal) {
		return String.format("%d/%d", cal.get(ChineseCalendar.MONTH)+1, cal.get(ChineseCalendar.DAY_OF_MONTH));
	}
	
	private String format(Calendar cal) {
		return String.format("%d/%d", cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
	}

	public void testKorean2() throws Exception {
		KoreanCalendar cal = new KoreanCalendar(new GregorianCalendar(2012, 5-1, 21).getTime());
		assertEquals("4/1", format(cal));
		assertTrue(cal.isLeapMonth());
	}
}
