package com.lge.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.NumberPicker.OnValueChangeListener;

/*
 * TODO followings
 * - ime input state change, ordering
 * - start/end range
 * -  accessibility
 */
public class MyDatePicker extends FrameLayout implements OnCheckedChangeListener {

    private static final String LOG_TAG = DatePicker.class.getSimpleName();

    private static final String DATE_FORMAT = "MM/dd/yyyy";

    private static final int DEFAULT_START_YEAR = 1900;

    private static final int DEFAULT_END_YEAR = 2100;

//    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = true;
//
//    private static final boolean DEFAULT_SPINNERS_SHOWN = true;

    private static final boolean DEFAULT_ENABLED_STATE = true;

    private final LinearLayout mSpinners;

    private final NumberPicker mDaySpinner;

    private final NumberPicker mMonthSpinner;

    private final NumberPicker mYearSpinner;

    private Locale mCurrentLocale;

    private OnDateChangedListener mOnDateChangedListener;

    private String[] mShortMonths;

    private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

    private int mNumberOfMonths;

    private Calendar mTempDate;

    private Calendar mMinDate;

    private Calendar mMaxDate;

    private Calendar mCurrentDate;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

	private boolean mAuxUse;

	private Calendar mAuxCalendar;

	private TextView mAlternateDateText;

    /**
     * The callback used to indicate the user changes\d the date.
     */
    public interface OnDateChangedListener {

        /**
         * Called upon a date change.
         *
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *            with {@link java.util.Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        void onDateChanged(MyDatePicker view, Calendar date);
    }

    public MyDatePicker(Context context) {
        this(context, null);
    }

    public MyDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.datePickerStyle);
    }

    public MyDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        String minDate = "01/01/1950";
        String maxDate = "12/31/2050";

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.date_picker, this, true);

        OnValueChangeListener onChangeListener = new OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                updateInputState();
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                // take care of wrapping of days and months to update greater fields
                if (picker == mDaySpinner) {
                    int maxDayOfMonth = mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
                    }
                } else if (picker == mMonthSpinner) {
                    if (oldVal == 11 && newVal == 0) {
                        mTempDate.add(Calendar.MONTH, 1);
                    } else if (oldVal == 0 && newVal == 11) {
                        mTempDate.add(Calendar.MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.MONTH, newVal - oldVal);
                    }
                } else if (picker == mYearSpinner) {
                    mTempDate.set(Calendar.YEAR, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                // now set the date to the adjusted one
                setDate(mTempDate);
                updateSpinners();
                updateAuxCalendar();
//                updateCalendarView();
                notifyDateChanged();
            }
        };

        mSpinners = (LinearLayout) findViewById(R.id.pickers);

//        // calendar view day-picker
//        mCalendarView = (CalendarView) findViewById(R.id.calendar_view);
//        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            public void onSelectedDayChange(CalendarView view, int year, int month, int monthDay) {
//                setDate(year, month, monthDay);
//                updateSpinners();
//                notifyDateChanged();
//            }
//        });

        // day
        mDaySpinner = (NumberPicker) findViewById(R.id.day);
        mDaySpinner.setOnLongPressUpdateInterval(100);
        mDaySpinner.setOnValueChangedListener(onChangeListener);

        // month
        mMonthSpinner = (NumberPicker) findViewById(R.id.month);
        mMonthSpinner.setMinValue(0);
        mMonthSpinner.setMaxValue(mNumberOfMonths - 1);
        mMonthSpinner.setDisplayedValues(mShortMonths);
        mMonthSpinner.setOnLongPressUpdateInterval(200);
        mMonthSpinner.setOnValueChangedListener(onChangeListener);

        // year
        mYearSpinner = (NumberPicker) findViewById(R.id.year);
        mYearSpinner.setOnLongPressUpdateInterval(100);
        mYearSpinner.setOnValueChangedListener(onChangeListener);

    

        // set the min date giving priority of the minDate over startYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(minDate)) {
            if (!parseDate(minDate, mTempDate)) {
                mTempDate.set(DEFAULT_START_YEAR, 0, 1);
            }
        } else {
            mTempDate.set(DEFAULT_START_YEAR, 0, 1);
        }
        setMinDate(mTempDate.getTimeInMillis());

        // set the max date giving priority of the maxDate over endYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(maxDate)) {
            if (!parseDate(maxDate, mTempDate)) {
                mTempDate.set(DEFAULT_END_YEAR, 11, 31);
            }
        } else {
            mTempDate.set(DEFAULT_END_YEAR, 11, 31);
        }
        setMaxDate(mTempDate.getTimeInMillis());

        // initialize to current date
        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(mCurrentDate, null);

        // re-order the number spinners to match the current date format
        reorderSpinners();

//        // set content descriptions
//        if (AccessibilityManager.getInstance(mContext).isEnabled()) {
//            setContentDescriptions();
//        }
        
        Switch sw = (Switch) findViewById(R.id.switch_calendar);
        sw.setOnCheckedChangeListener(this);
        
        mAlternateDateText = (TextView) findViewById(R.id.alternate_date);
    }
    
    
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	mAlternateDateText.setText(isChecked + "");
    }


//    /**
//     * Gets the minimal date supported by this {@link DatePicker} in
//     * milliseconds since January 1, 1970 00:00:00 in
//     * {@link TimeZone#getDefault()} time zone.
//     * <p>
//     * Note: The default minimal date is 01/01/1900.
//     * <p>
//     *
//     * @return The minimal supported date.
//     */
//    public long getMinDate() {
//        return mCalendarView.getMinDate();
//    }

    /**
     * Sets the minimal date supported by this {@link NumberPicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param minDate The minimal supported date.
     */
    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMinDate.setTimeInMillis(minDate);
//        mCalendarView.setMinDate(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
//            updateCalendarView();
        }
        updateSpinners();
    }

//    /**
//     * Gets the maximal date supported by this {@link DatePicker} in
//     * milliseconds since January 1, 1970 00:00:00 in
//     * {@link TimeZone#getDefault()} time zone.
//     * <p>
//     * Note: The default maximal date is 12/31/2100.
//     * <p>
//     *
//     * @return The maximal supported date.
//     */
//    public long getMaxDate() {
//        return mCalendarView.getMaxDate();
//    }

    /**
     * Sets the maximal date supported by this {@link DatePicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param maxDate The maximal supported date.
     */
    public void setMaxDate(long maxDate) {
        mTempDate.setTimeInMillis(maxDate);
        if (mTempDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMaxDate.setTimeInMillis(maxDate);
//        mCalendarView.setMaxDate(maxDate);
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
//            updateCalendarView();
        }
        updateSpinners();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mDaySpinner.setEnabled(enabled);
        mMonthSpinner.setEnabled(enabled);
        mYearSpinner.setEnabled(enabled);
//        mCalendarView.setEnabled(enabled);
        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

//    @Override
//    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
//        super.onPopulateAccessibilityEvent(event);
//
//        final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
//        String selectedDateUtterance = DateUtils.formatDateTime(mContext,
//                mCurrentDate.getTimeInMillis(), flags);
//        event.getText().add(selectedDateUtterance);
//    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

//    /**
//     * Gets whether the {@link CalendarView} is shown.
//     *
//     * @return True if the calendar view is shown.
//     * @see #getCalendarView()
//     */
//    public boolean getCalendarViewShown() {
//        return mCalendarView.isShown();
//    }
//
//    /**
//     * Gets the {@link CalendarView}.
//     *
//     * @return The calendar view.
//     * @see #getCalendarViewShown()
//     */
//    public CalendarView getCalendarView () {
//        return mCalendarView;
//    }
//
//    /**
//     * Sets whether the {@link CalendarView} is shown.
//     *
//     * @param shown True if the calendar view is to be shown.
//     */
//    public void setCalendarViewShown(boolean shown) {
//        mCalendarView.setVisibility(shown ? VISIBLE : GONE);
//    }

    /**
     * Gets whether the spinners are shown.
     *
     * @return True if the spinners are shown.
     */
    public boolean getSpinnersShown() {
        return mSpinners.isShown();
    }

    /**
     * Sets whether the spinners are shown.
     *
     * @param shown True if the spinners are to be shown.
     */
    public void setSpinnersShown(boolean shown) {
        mSpinners.setVisibility(shown ? VISIBLE : GONE);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }

        mCurrentLocale = locale;

        mTempDate = getCalendarForLocale(mTempDate, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale);

        mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1;
        mShortMonths = new String[mNumberOfMonths];
        for (int i = 0; i < mNumberOfMonths; i++) {
            mShortMonths[i] = DateUtils.getMonthString(Calendar.JANUARY + i,
                    DateUtils.LENGTH_MEDIUM);
        }
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale The locale.
     */
    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }
    
    private String format(Calendar cal) {
    	return String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY)+1, cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Reorders the spinners according to the date format that is
     * explicitly set by the user and if no such is set fall back
     * to the current locale's default format.
     */
    private void reorderSpinners() {
        mSpinners.removeAllViews();
        char[] order = DateFormat.getDateFormatOrder(getContext());
        final int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case DateFormat.DATE:
                    mSpinners.addView(mDaySpinner);
//                    setImeOptions(mDaySpinner, spinnerCount, i);
                    break;
                case DateFormat.MONTH:
                    mSpinners.addView(mMonthSpinner);
//                    setImeOptions(mMonthSpinner, spinnerCount, i);
                    break;
                case DateFormat.YEAR:
                    mSpinners.addView(mYearSpinner);
//                    setImeOptions(mYearSpinner, spinnerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }


    // Override so we are in complete control of save / restore for this widget.
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getDate());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mDate);
        updateSpinners();
//        updateCalendarView();
    }

    /**
     * Initialize the state. If the provided values designate an inconsistent
     * date the values are normalized before updating the spinners.
     *
     * @param date The initial date.
     * @param onDateChangedListener How user is notified date is changed by
     *            user, can be null.
     */
    public void init(Calendar date, OnDateChangedListener onDateChangedListener) {
    	setDate(date);
    	updateSpinners();
    	mOnDateChangedListener = onDateChangedListener;
    }

    /**
     * Parses the given <code>date</code> and in case of success sets the result
     * to the <code>outDate</code>.
     *
     * @return True if the date was parsed.
     */
    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    private void setDate(Calendar date) {
        mCurrentDate.setTimeInMillis(date.getTimeInMillis());
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }

    private void updateSpinners() {
        // set the spinner ranges respecting the min and max dates
        if (mCurrentDate.equals(mMinDate)) {
            mDaySpinner.setMinValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            mDaySpinner.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mDaySpinner.setWrapSelectorWheel(false);
            mMonthSpinner.setDisplayedValues(null);
            mMonthSpinner.setMinValue(mCurrentDate.get(Calendar.MONTH));
            mMonthSpinner.setMaxValue(mCurrentDate.getActualMaximum(Calendar.MONTH));
            mMonthSpinner.setWrapSelectorWheel(false);
        } else if (mCurrentDate.equals(mMaxDate)) {
            mDaySpinner.setMinValue(mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            mDaySpinner.setMaxValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            mDaySpinner.setWrapSelectorWheel(false);
            mMonthSpinner.setDisplayedValues(null);
            mMonthSpinner.setMinValue(mCurrentDate.getActualMinimum(Calendar.MONTH));
            mMonthSpinner.setMaxValue(mCurrentDate.get(Calendar.MONTH));
            mMonthSpinner.setWrapSelectorWheel(false);
        } else {
            mDaySpinner.setMinValue(1);
            mDaySpinner.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mDaySpinner.setWrapSelectorWheel(true);
            mMonthSpinner.setDisplayedValues(null);
            mMonthSpinner.setMinValue(0);
            mMonthSpinner.setMaxValue(11);
            mMonthSpinner.setWrapSelectorWheel(true);
        }

        // make sure the month names are a zero based array
        // with the months in the month spinner
        String[] displayedValues = Arrays.copyOfRange(mShortMonths,
                mMonthSpinner.getMinValue(), mMonthSpinner.getMaxValue() + 1);
        mMonthSpinner.setDisplayedValues(displayedValues);

        // year spinner range does not change based on the current date
        mYearSpinner.setMinValue(mMinDate.get(Calendar.YEAR));
        mYearSpinner.setMaxValue(mMaxDate.get(Calendar.YEAR));
        mYearSpinner.setWrapSelectorWheel(false);

        // set the spinner values
        mYearSpinner.setValue(mCurrentDate.get(Calendar.YEAR));
        mMonthSpinner.setValue(mCurrentDate.get(Calendar.MONTH));
        mDaySpinner.setValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
    }
    
    private void updateAuxCalendar() {
    	if (mAuxUse) {
    		mAuxCalendar.setTimeInMillis(mCurrentDate.getTimeInMillis());
    		mAlternateDateText.setText(format(mAuxCalendar));
    	}
    }

//    /**
//     * Updates the calendar view with the current date.
//     */
//    private void updateCalendarView() {
//         mCalendarView.setDate(mCurrentDate.getTimeInMillis(), false, false);
//    }


    /**
     * Notifies the listener, if such, for a change in the selected date.
     */
    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, getDate());
        }
    }

//    /**
//     * Sets the IME options for a spinner based on its ordering.
//     *
//     * @param spinner The spinner.
//     * @param spinnerCount The total spinner count.
//     * @param spinnerIndex The index of the given spinner.
//     */
//    private void setImeOptions(NumberPicker spinner, int spinnerCount, int spinnerIndex) {
//        final int imeOptions;
//        if (spinnerIndex < spinnerCount - 1) {
//            imeOptions = EditorInfo.IME_ACTION_NEXT;
//        } else {
//            imeOptions = EditorInfo.IME_ACTION_DONE;
//        }
//        TextView input = (TextView) spinner.findViewById(R.id.numberpicker_input);
//        input.setImeOptions(imeOptions);
//    }
//
//    private void setContentDescriptions() {
//        // Day
//        String text = mContext.getString(R.string.date_picker_increment_day_button);
//        mDaySpinner.findViewById(R.id.increment).setContentDescription(text);
//        text = mContext.getString(R.string.date_picker_decrement_day_button);
//        mDaySpinner.findViewById(R.id.decrement).setContentDescription(text);
//        // Month
//        text = mContext.getString(R.string.date_picker_increment_month_button);
//        mMonthSpinner.findViewById(R.id.increment).setContentDescription(text);
//        text = mContext.getString(R.string.date_picker_decrement_month_button);
//        mMonthSpinner.findViewById(R.id.decrement).setContentDescription(text);
//        // Year
//        text = mContext.getString(R.string.date_picker_increment_year_button);
//        mYearSpinner.findViewById(R.id.increment).setContentDescription(text);
//        text = mContext.getString(R.string.date_picker_decrement_year_button);
//        mYearSpinner.findViewById(R.id.decrement).setContentDescription(text);
//    }

//    private void updateInputState() {
//        // Make sure that if the user changes the value and the IME is active
//        // for one of the inputs if this widget, the IME is closed. If the user
//        // changed the value via the IME and there is a next input the IME will
//        // be shown, otherwise the user chose another means of changing the
//        // value and having the IME up makes no sense.
//        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
//        if (inputMethodManager != null) {
//            if (inputMethodManager.isActive(mYearSpinnerInput)) {
//                mYearSpinnerInput.clearFocus();
//                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
//            } else if (inputMethodManager.isActive(mMonthSpinnerInput)) {
//                mMonthSpinnerInput.clearFocus();
//                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
//            } else if (inputMethodManager.isActive(mDaySpinnerInput)) {
//                mDaySpinnerInput.clearFocus();
//                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
//            }
//        }
//    }

    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

    	private Calendar mDate; 

        /**
         * Constructor called from {@link DatePicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, Calendar date) {
            super(superState);
            mDate = date;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mDate = (Calendar)in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSerializable(mDate);
        }

        @SuppressWarnings("all")
        // suppress unused and hiding
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

	public Calendar getDate() {
		return mCurrentDate;
	}

	public void setAux(boolean useAux, Calendar auxCalendar) {
		mAuxUse = useAux;
		mAuxCalendar = auxCalendar;
	}
}
