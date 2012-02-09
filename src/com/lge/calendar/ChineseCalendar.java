package com.lge.calendar;

import java.util.Date;

import com.ibm.icu.util.Calendar;


public class ChineseCalendar extends java.util.Calendar {
	/**
	 * 
	 */
	private static final long serialVersionUID = -247649720561505038L;
	com.ibm.icu.util.ChineseCalendar mCalendar;

	public ChineseCalendar(Date time) {
		mCalendar = new com.ibm.icu.util.ChineseCalendar(time);
	}

	@Override
	public void add(int field, int value) {
		mCalendar.add(field, value);
	}
	
	@Override
	public int getActualMaximum(int field) {
		return mCalendar.getActualMaximum(field);
	}
	
	@Override
	public int getActualMinimum(int field) {
		return mCalendar.getActualMinimum(field);
	}

	@Override
	public int getGreatestMinimum(int field) {
		return mCalendar.getGreatestMinimum(field);
	}

	@Override
	public int getLeastMaximum(int field) {
		return mCalendar.getLeastMaximum(field);
	}

	@Override
	public int getMaximum(int field) {
		return mCalendar.getMaximum(field);
	}

	@Override
	public int getMinimum(int field) {
		return mCalendar.getMaximum(field);
	}

	@Override
	public void roll(int field, boolean increment) {
		mCalendar.roll(field, increment);
	}
	
	@Override
	public void set(int field, int value) {
		mCalendar.set(field, value);
	}
	
	@Override
	public int get(int field) {
		if (field == Calendar.YEAR)
			return (mCalendar.get(Calendar.ERA)-1) * 60 - 2637 + mCalendar.get(Calendar.YEAR);
		return mCalendar.get(field);
	}

	
	@Override
	public void setTimeInMillis(long milliseconds) {
		mCalendar.setTimeInMillis(milliseconds);
	}
	
	public boolean isLeapMonth() {
		return mCalendar.get(com.ibm.icu.util.ChineseCalendar.IS_LEAP_MONTH) == 1;
	}
	
	@Override
	public long getTimeInMillis() {
		return mCalendar.getTimeInMillis();
	}
	
	@Override
	public Object clone() {
		return new ChineseCalendar(getTime());
	}

	@Override
	protected void computeFields() {
	}

	@Override
	protected void computeTime() {
	}

}
