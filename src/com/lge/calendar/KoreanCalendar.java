package com.lge.calendar;

import java.util.Date;

import com.ibm.icu.util.ChineseCalendar;



public class KoreanCalendar extends java.util.Calendar {
	/**
	 * 
	 */
	private static final long serialVersionUID = -247649720561505038L;
	ChineseCalendar mCalendar;

	public KoreanCalendar(Date time) {
		mCalendar = new ChineseCalendar(time);
	}

	@Override
	public void add(int field, int value) {
		mCalendar.add(field, value);
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
		return mCalendar.get(field);
	}

	
	@Override
	public void setTimeInMillis(long milliseconds) {
		mCalendar.setTimeInMillis(milliseconds);
	}
	
	public boolean isLeapMonth() {
		return mCalendar.get(ChineseCalendar.IS_LEAP_MONTH) == 1;
	}
	

	@Override
	protected void computeFields() {
	}

	@Override
	protected void computeTime() {
	}

}
