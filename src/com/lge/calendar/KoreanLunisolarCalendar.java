package com.lge.calendar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class KoreanLunisolarCalendar extends Calendar {

	private static final long serialVersionUID = -8177237662194876813L;

	private static final int KOREA_TIME_ZONE = 9 * 60 * 60 * 1000;
	private static final long HOUR_IN_MILLIS = 1000 * 60 * 60;
	private static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

	private static final int START_YEAR = 1901;

	private static int[] maximums = new int[] { 1, 2101, 12, 53, 6, 30, 385, 7,
			6, 1, 11, 23, 59, 59, 999, 14 * 3600 * 1000, 7200000 };

	private static int[] minimums = new int[] { 0, START_YEAR, 0, 1, 0, 1, 1,
			1, 1, 0, 0, 0, 0, 0, 0, -13 * 3600 * 1000, 0 };

	private static int[] leastMaximums = new int[] { 1, 2049, 11, 50, 3, 29,
			354, 7, 3, 1, 11, 23, 59, 59, 999, 50400000, 1200000 };

	static char[] daysInYear;
	static byte[][] daysInMonth;
	static byte[] leapMonthInYear;

	static private long BASE_DAYS_OFFSET;

	public KoreanLunisolarCalendar(Date time) {
		this(time.getTime());
	}

	public KoreanLunisolarCalendar() {
		this(System.currentTimeMillis());
	}

	/**
	 * @param millis
	 *            the number of milliseconds since January 1, 1970, 00:00:00 GMT
	 */
	public KoreanLunisolarCalendar(long millis) {
		setTimeInMillis(millis);
	}

	private void internal_set(int field, int value) {
		fields[field] = value;
	}

	private int internal_get(int field) {
		return fields[field];
	}

	@Override
	protected void computeTime() {
		if (!isSet[YEAR] || !isSet[MONTH] || !isSet[DAY_OF_MONTH]) {
			throw new IllegalArgumentException();
		}
		long days = 0;
		int year = internal_get(YEAR) - START_YEAR;
		for (int y = 0; y < year; y++) {
			days += daysInYear[y];
		}
		int month = internal_get(MONTH);
		for (int m = 0; m < month; m++) {
			days += daysInMonth[year][m];
		}
		days += internal_get(DAY_OF_MONTH) - 1 - BASE_DAYS_OFFSET;
		time = days * DAY_IN_MILLIS;

		if (isSet[HOUR_OF_DAY]) {
			time += internal_get(HOUR_OF_DAY) * HOUR_IN_MILLIS;
		}

		time -= KOREA_TIME_ZONE;
	}

	@Override
	protected void computeFields() {
		long days = daysSince1970(time) + BASE_DAYS_OFFSET;

		int y = 0;
		while (days >= daysInYear[y]) {
			days -= daysInYear[y];
			y++;
		}

		int m = 0;
		while (days >= daysInMonth[y][m]) {
			days -= daysInMonth[y][m];
			m++;
		}

		internal_set(YEAR, y + START_YEAR);
		internal_set(MONTH, m);
		internal_set(DAY_OF_MONTH, (int) days + 1);
		isSet[YEAR] = isSet[MONTH] = isSet[DAY_OF_MONTH] = true;
	}

	@Override
	public void add(int field, int amount) {
		switch (field) {
		case DAY_OF_MONTH: {
			long time = getTimeInMillis() + amount * DAY_IN_MILLIS;
			setTimeInMillis(time);
		}
			break;
		case MONTH: {
			int year = internal_get(YEAR) - START_YEAR;
			int newMonth = internal_get(MONTH) + amount;
			while (newMonth < 0) {
				year--;
				newMonth += daysInMonth[year].length;
			}
			while (newMonth >= daysInMonth[year].length) {
				newMonth -= daysInMonth[year].length;
				year++;
			}

			int day = internal_get(DAY_OF_MONTH);
			if (day > daysInMonth[year][newMonth]) {
				set(DAY_OF_MONTH, daysInMonth[year][newMonth]);
			}
			set(YEAR, year + START_YEAR);
			set(MONTH, newMonth);
		}
			break;
		case YEAR: {
			int newYear = internal_get(YEAR) + amount - START_YEAR;
			int month = internal_get(MONTH);
			if (month >= daysInMonth[newYear].length)
				month = daysInMonth[newYear].length - 1;
			int day = internal_get(DAY_OF_MONTH);
			if (day > daysInMonth[newYear][month])
				day = daysInMonth[newYear][month];
			set(YEAR, newYear + START_YEAR);
			set(MONTH, month);
			set(DAY_OF_MONTH, day);
		}
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void roll(int field, boolean up) {
		int[] old = Arrays.copyOfRange(fields, 0, field);
		set(field, internal_get(field) + (up ? 1 : -1));
		complete();
		for (int f = 0; f < field; f++)
			set(f, old[f]);
	}

	@Override
	public int getMinimum(int field) {
		return minimums[field];
	}

	@Override
	public int getMaximum(int field) {
		return maximums[field];
	}

	@Override
	public int getGreatestMinimum(int field) {
		return minimums[field];
	}

	@Override
	public int getLeastMaximum(int field) {
		return leastMaximums[field];
	}

	/**
	 * @param millis
	 *            since 1970, 1, 1 (GMT)
	 * @return
	 */
	public static long daysSince1970(long millis) {
		return (millis + KOREA_TIME_ZONE) / DAY_IN_MILLIS;
	}

	public static int getDaysInYear(int year) {
		return daysInYear[year - START_YEAR];
	}

	public static int getMonthsInYear(int year) {
		return daysInMonth[year - START_YEAR].length;
	}

	public static int getLeapMonthInYear(int year) {
		return leapMonthInYear[year - START_YEAR];
	}

	private static final byte[][] daysOfType = { {}, { 29 }, { 30 },
			{ 29, 29 }, { 29, 30 }, { 30, 29 }, { 30, 30 } };
	/*
	 * 1 : small 2 : big 3 : small, small(leap) 4 : small, big(leap) 5 : big,
	 * small(leap) 6 : big, big(leap)
	 */
	static byte[][] data = new byte[][] {
			{ 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1 }, /* 1901 */
			{ 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2 },
			{ 1, 2, 1, 2, 3, 2, 1, 1, 2, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1 },
			{ 2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 2, 4, 1, 2, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1 },
			{ 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2 },
			{ 1, 5, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2 },
			{ 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1 },
			{ 2, 1, 2, 1, 1, 5, 1, 2, 2, 1, 2, 2 }, /* 1911 */
			{ 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2 },
			{ 2, 2, 1, 2, 5, 1, 2, 1, 2, 1, 1, 2 },
			{ 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1 },
			{ 2, 3, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1 },
			{ 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2 },
			{ 1, 2, 1, 1, 2, 1, 5, 2, 2, 1, 2, 2 },
			{ 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2 },
			{ 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2 }, /* 1921 */
			{ 2, 1, 2, 2, 3, 2, 1, 1, 2, 1, 2, 2 },
			{ 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2 },
			{ 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1 },
			{ 2, 1, 2, 5, 2, 1, 2, 2, 1, 2, 1, 2 },
			{ 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1 },
			{ 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2 },
			{ 1, 5, 1, 2, 1, 1, 2, 2, 1, 2, 2, 2 },
			{ 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2 },
			{ 1, 2, 2, 1, 1, 5, 1, 2, 1, 2, 2, 1 },
			{ 2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1 }, /* 1931 */
			{ 2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2 },
			{ 1, 2, 2, 1, 6, 1, 2, 1, 2, 1, 1, 2 },
			{ 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2 },
			{ 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1 },
			{ 2, 1, 4, 1, 2, 1, 2, 1, 2, 2, 2, 1 },
			{ 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1 },
			{ 2, 2, 1, 1, 2, 1, 4, 1, 2, 2, 1, 2 },
			{ 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1 },
			{ 2, 2, 1, 2, 2, 4, 1, 1, 2, 1, 2, 1 }, /* 1941 */
			{ 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 1, 2 },
			{ 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2 },
			{ 1, 1, 2, 4, 1, 2, 1, 2, 2, 1, 2, 2 },
			{ 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2 },
			{ 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2 },
			{ 2, 5, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2 },
			{ 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 2, 3, 2, 1, 2, 1, 2 },
			{ 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1 },
			{ 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2 }, /* 1951 */
			{ 1, 2, 1, 2, 4, 2, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 1, 1, 2, 2, 1, 2, 2, 1, 2, 2 },
			{ 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2 },
			{ 2, 1, 4, 1, 1, 2, 1, 2, 1, 2, 2, 2 },
			{ 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2 },
			{ 2, 1, 2, 1, 2, 1, 1, 5, 2, 1, 2, 2 },
			{ 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1 },
			{ 2, 1, 2, 1, 2, 5, 2, 1, 2, 1, 2, 1 },
			{ 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2 }, /* 1961 */
			{ 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1 },
			{ 2, 1, 2, 3, 2, 1, 2, 1, 2, 2, 2, 1 },
			{ 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2 },
			{ 1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2 },
			{ 1, 2, 5, 2, 1, 1, 2, 1, 1, 2, 2, 1 },
			{ 2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 2, 1, 2, 1, 5, 2, 1, 2, 1, 2 },
			{ 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1 },
			{ 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2 },
			{ 1, 2, 1, 1, 5, 2, 1, 2, 2, 2, 1, 2 }, /* 1971 */
			{ 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1 },
			{ 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 1 },
			{ 2, 2, 1, 5, 1, 2, 1, 1, 2, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 2, 1, 5, 2, 1, 1, 2 },
			{ 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1 },
			{ 2, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1 },
			{ 2, 1, 1, 2, 1, 6, 1, 2, 2, 1, 2, 1 },
			{ 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2 },
			{ 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2 }, /* 1981 */
			{ 2, 1, 2, 3, 2, 1, 1, 2, 2, 1, 2, 2 },
			{ 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2 },
			{ 2, 1, 2, 2, 1, 1, 2, 1, 1, 5, 2, 2 },
			{ 1, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2 },
			{ 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1 },
			{ 2, 1, 2, 2, 1, 5, 2, 2, 1, 2, 1, 2 },
			{ 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1 },
			{ 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2 },
			{ 1, 2, 1, 1, 5, 1, 2, 1, 2, 2, 2, 2 },
			{ 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2 }, /* 1991 */
			{ 1, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2 },
			{ 1, 2, 5, 2, 1, 2, 1, 1, 2, 1, 2, 1 },
			{ 2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2 },
			{ 1, 2, 2, 1, 2, 2, 1, 5, 2, 1, 1, 2 },
			{ 1, 2, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2 },
			{ 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1 },
			{ 2, 1, 1, 2, 3, 2, 2, 1, 2, 2, 2, 1 },
			{ 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1 },
			{ 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1 },
			{ 2, 2, 2, 3, 2, 1, 1, 2, 1, 2, 1, 2 }, /* 2001 */
			{ 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1 },
			{ 2, 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2 },
			{ 1, 5, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 1 },
			{ 2, 1, 2, 1, 2, 1, 5, 2, 2, 1, 2, 2 },
			{ 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2 },
			{ 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2 },
			{ 2, 2, 1, 1, 5, 1, 2, 1, 2, 1, 2, 2 },
			{ 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1 }, /* 2011 */
			{ 2, 1, 6, 2, 1, 2, 1, 1, 2, 1, 2, 1 },
			{ 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 1, 2, 1, 2, 1, 2, 5, 2, 1, 2 },
			{ 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2, 1 },
			{ 2, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2 },
			{ 2, 1, 1, 2, 3, 2, 1, 2, 1, 2, 2, 2 },
			{ 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2 },
			{ 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 2, 1, 2, 5, 2, 1, 1, 2, 1, 2, 1, 2 },
			{ 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1 }, /* 2021 */
			{ 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2 },
			{ 1, 5, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2 },
			{ 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1 },
			{ 2, 1, 2, 1, 1, 5, 2, 1, 2, 2, 2, 1 },
			{ 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2 },
			{ 1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2 },
			{ 1, 2, 2, 1, 5, 1, 2, 1, 1, 2, 2, 1 },
			{ 2, 2, 1, 2, 2, 1, 1, 2, 1, 1, 2, 2 },
			{ 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1 },
			{ 2, 1, 5, 2, 1, 2, 2, 1, 2, 1, 2, 1 }, /* 2031 */
			{ 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2 },
			{ 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 5, 2 },
			{ 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1 },
			{ 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 4, 1, 1, 2, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2 },
			{ 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1 },
			{ 2, 2, 1, 2, 5, 2, 1, 2, 1, 2, 1, 1 },
			{ 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1 },
			{ 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2 }, /* 2041 */
			{ 1, 5, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2 }, // 2042
			{ 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2 }, // 2043
			{ 2, 1, 2, 1, 1, 2, 3, 2, 1, 2, 2, 2 }, // 2044
			{ 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2 }, // 2045
			{ 2, 1, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2 }, // 2046
			{ 2, 1, 2, 2, 4, 1, 2, 1, 1, 2, 1, 2 }, // 2047
			{ 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1 }, // 2048
			{ 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1 }, // 2049
	};
	static {
		leapMonthInYear = new byte[data.length];
		daysInYear = new char[data.length];
		daysInMonth = new byte[data.length][];
		byte[] daysBuffer = new byte[13]; // buffer
		for (int y = 0; y < data.length; y++) {
			byte leapMonth = 0;
			char days = 0;
			int month = 0;
			for (byte m = 0; m < data[y].length; m++) {
				byte type = data[y][m];
				if (type < 3) { // normal
					byte normalDays = daysOfType[type][0];
					daysBuffer[month++] = normalDays;
					days += normalDays;
				} else { // with leap month followed
					leapMonth = (byte) (m + 1);
					daysBuffer[month++] = daysOfType[type][0];
					days += daysOfType[type][0];
					daysBuffer[month++] = daysOfType[type][1];
					days += daysOfType[type][1];
				}
			}
			leapMonthInYear[y] = leapMonth;
			daysInYear[y] = days;
			daysInMonth[y] = Arrays.copyOf(daysBuffer, month);
		}

		GregorianCalendar lunarStart = new GregorianCalendar(1901, 1, 19, 9, 0,
				0);
		GregorianCalendar solarStart = new GregorianCalendar(1970, 0, 1, 9, 0,
				0);
		BASE_DAYS_OFFSET = (solarStart.getTimeInMillis() - lunarStart
				.getTimeInMillis()) / DAY_IN_MILLIS;
	}
}
