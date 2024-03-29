package com.lge.calendar;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DatePickerTestActivity extends Activity {
	private TextView mDateDisplay;
	private Button mPickDate;
	private Calendar mDate;
	
	static final int DATE_DIALOG_ID = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// capture our View elements
		mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
		mPickDate = (Button) findViewById(R.id.pickDate);

		// add a click listener to the button
		mPickDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		// get the current date
		mDate = Calendar.getInstance();

		// display the current date (this method is below)
		updateDisplay();
	}

	// updates the date in the TextView
	private void updateDisplay() {
		mDateDisplay.setText(android.text.format.DateFormat.getMediumDateFormat(this).format(mDate.getTime()));
	}

	// the callback received when the user "sets" the date in the dialog
	private DualDatePickerDialog.OnDateSetListener mDateSetListener = new DualDatePickerDialog.OnDateSetListener() {

		public void onDateSet(CalendarDatePicker view, Calendar newDate) {
			mDate = newDate;
			updateDisplay();
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DualDatePickerDialog(this, mDateSetListener, mDate, new KoreanLunisolarCalendar(mDate.getTime()));
		}
		return null;
	}
}