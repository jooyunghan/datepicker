package com.lge.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateFormatUtil {

	
	/* make an array for minMonth <= month <= maxMonth by adding one
	 *  in order to handle intercalary month
	 */
	public static String[] getMonthStrings(Calendar date) {
		if (date instanceof ChineseCalendar) {
			int leapMonth = getLeapMonth(date.get(Calendar.YEAR));
			return leapMonthString[leapMonth];
		} else if (date instanceof GregorianCalendar) {
			return leapMonthString[0];
		} else {
			ArrayList<String> monthStrings =  new ArrayList<String>(13);
			
			Calendar c = (Calendar) date.clone();
			int year = c.get(Calendar.YEAR);
			c.set(Calendar.MONTH, 0);
			while (c.get(Calendar.YEAR) == year) {
				monthStrings.add(""+c.get(Calendar.MONTH));
				c.add(Calendar.MONTH, 1);
			}
			return (String[]) monthStrings.toArray(new String[monthStrings.size()]);
		}
	}
	
	static String[][] leapMonthString = new String[13][]; 
	static int[]    leapMonthYear;
	static int[]    leapMonthMonth;	
	static {
		leapMonthString[0] = new String[12];
		for (int i=0; i<12; i++) {
			leapMonthString[0][i] = (i+1) + "¿ù";
		}
		for (int leapMonth=1; leapMonth<=12; leapMonth++) {
			leapMonthString[leapMonth] = new String[13];
			leapMonthString[leapMonth][leapMonth] = leapMonth + "¿ù(À±)";
			for (int m=0; m<12; m++) {
				if (m+1 <= leapMonth) {
					leapMonthString[leapMonth][m] = (m+1) + "¿ù";
				} else {
					leapMonthString[leapMonth][m+1] = (m+1) + "¿ù";
				}
			}
		}		
		
		leapMonthYear = new int[] {
				1900,
				1903,
				1906,
				1909,
				1911,
				1914,
				1917,
				1919,
				1922,
				1925,
				1928,
				1930,
				1933,
				1936,
				1938,
				1941,
				1944,
				1947,
				1949,
				1952,
				1955,
				1957,
				1960,
				1963,
				1966,
				1968,
				1971,
				1974,
				1976,
				1979,
				1982,
				1984,
				1987,
				1990,
				1993,
				1995,
				1998,
				2001,
				2004,
				2006,
				2009,
				2012,
				2014,
				2017,
				2020,
				2023,
				2025,
				2028,
				2031,
				2033,
				2036,
				2039,
				2042,
				2044,
				2047,
				2050,
				2052,
				2055,
				2058,
				2061,
				2063,
				2066,
				2069,
				2071,
				2074,
				2077,
				2080,
				2082,
				2085,
				2088,
				2090,
				2093,
				2096,
				2099,
		};
		
		leapMonthMonth = new int[] {
				8,
				5,
				4,
				2,
				6,
				5,
				2,
				7,
				5,
				4,
				2,
				6,
				5,
				3,
				7,
				6,
				4,
				2,
				7,
				5,
				3,
				8,
				6,
				4,
				3,
				7,
				5,
				4,
				8,
				6,
				4,
				10,
				6,
				5,
				3,
				8,
				5,
				4,
				2,
				7,
				5,
				4,
				9,
				5,
				4, //3 for korea
				2,
				6,
				5,
				3,
				11,
				6,
				5,
				2,
				7,
				5,
				3,
				8,
				6,
				4,
				3,
				7,
				5,
				4,
				8,
				6,
				4,
				3,
				7,
				5,
				4,
				8,
				6,
				4,
				3
		};
	}

	private static int getLeapMonth(int year) {
		int index = Arrays.binarySearch(leapMonthYear, year);
		if (index > 0) {
			return leapMonthMonth[index];
		}
		return 0;
	}

}
