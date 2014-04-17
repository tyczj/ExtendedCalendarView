package com.tyczj.extendedcalendarview;

import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class ExtendedCalendarView extends RelativeLayout implements OnItemClickListener,
	OnClickListener{
	
	private Context context;
	private OnDayClickListener dayListener;
	private GridView calendar;
	private CalendarAdapter mAdapter;
	private Calendar cal;
	private TextView month;
	private RelativeLayout base;
	private ImageView next,prev;
	private int gestureType = 0;
	private final GestureDetector calendarGesture = new GestureDetector(context,new GestureListener());
	
	public static final int NO_GESTURE = 0;
	public static final int LEFT_RIGHT_GESTURE = 1;
	public static final int UP_DOWN_GESTURE = 2;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	public interface OnDayClickListener{
		public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day);
	}

	public ExtendedCalendarView(Context context) {
		super(context);
		this.context = context;
		init();
	}
	
	public ExtendedCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}
	
	public ExtendedCalendarView(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}
	
	private void init(){
		cal = Calendar.getInstance();
		base = new RelativeLayout(context);
		base.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		base.setMinimumHeight(50);
		
		base.setId(4);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = 16;
		params.topMargin = 50;
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		prev = new ImageView(context);
		prev.setId(1);
		prev.setLayoutParams(params);
		prev.setImageResource(R.drawable.navigation_previous_item);
		prev.setOnClickListener(this);
		base.addView(prev);
		
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		month = new TextView(context);
		month.setId(2);
		month.setLayoutParams(params);
		month.setTextAppearance(context, android.R.attr.textAppearanceLarge);
		month.setText(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())+" "+cal.get(Calendar.YEAR));
		month.setTextSize(25);
		
		base.addView(month);
		
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.rightMargin = 16;
		params.topMargin = 50;
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		next = new ImageView(context);
		next.setImageResource(R.drawable.navigation_next_item);
		next.setLayoutParams(params);
		next.setId(3);
		next.setOnClickListener(this);
		
		base.addView(next);
		
		addView(base);
		
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.bottomMargin = 20;
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.BELOW, base.getId());
		
		calendar = new GridView(context);
		calendar.setLayoutParams(params);
		calendar.setVerticalSpacing(4);
		calendar.setHorizontalSpacing(4);
		calendar.setNumColumns(7);
		calendar.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
		calendar.setDrawSelectorOnTop(true);
		
		mAdapter = new CalendarAdapter(context,cal);
		calendar.setAdapter(mAdapter);
		calendar.setOnTouchListener(new OnTouchListener() {
			
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            return calendarGesture.onTouchEvent(event);
	        }
	    });
		
		addView(calendar);
	}

	private class GestureListener extends SimpleOnGestureListener {
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
	    	
	    	if(gestureType == LEFT_RIGHT_GESTURE){
	    		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		            nextMonth();
		            return true; // Right to left
		        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		            previousMonth();
		            return true; // Left to right
		        }
	    	}else if(gestureType == UP_DOWN_GESTURE){
	        	if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		        	nextMonth();
		            return true; // Bottom to top
		        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
		        	previousMonth();
		            return true; // Top to bottom
		        }
	        }
	        return false;
	    }
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(dayListener != null){
			Day d = (Day) mAdapter.getItem(arg2);
			if(d.getDay() != 0){
				dayListener.onDayClicked(arg0, arg1, arg2, arg3,d);
			}
		}
	}
	
	/**
	 * 
	 * @param listener
	 * 
	 * Set a listener for when you press on a day in the month
	 */
	public void setOnDayClickListener(OnDayClickListener listener){
		if(calendar != null){
			dayListener = listener;
			calendar.setOnItemClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case 1:
			previousMonth();
			break;
		case 3:
			nextMonth();
			break;
		default:
			break;
		}
	}
	
	private void previousMonth(){
		if(cal.get(Calendar.MONTH) == cal.getActualMinimum(Calendar.MONTH)) {				
			cal.set((cal.get(Calendar.YEAR)-1),cal.getActualMaximum(Calendar.MONTH),1);
		} else {
			cal.set(Calendar.MONTH,cal.get(Calendar.MONTH)-1);
		}
		rebuildCalendar();
	}
	
	private void nextMonth(){
		if(cal.get(Calendar.MONTH) == cal.getActualMaximum(Calendar.MONTH)) {				
			cal.set((cal.get(Calendar.YEAR)+1),cal.getActualMinimum(Calendar.MONTH),1);
		} else {
			cal.set(Calendar.MONTH,cal.get(Calendar.MONTH)+1);
		}
		rebuildCalendar();
	}
	
	private void rebuildCalendar(){
		if(month != null){
			month.setText(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())+" "+cal.get(Calendar.YEAR));
			refreshCalendar();
		}
	}
	
	/**
	 * Refreshes the month
	 */
	public void refreshCalendar(){
		mAdapter.refreshDays();
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 
	 * @param color
	 * 
	 * Sets the background color of the month bar
	 */
	public void setMonthTextBackgroundColor(int color){
		base.setBackgroundColor(color);
	}
	
	@SuppressLint("NewApi")
	/**
	 * 
	 * @param drawable
	 * 
	 * Sets the background color of the month bar. Requires at least API level 16
	 */
	public void setMonthTextBackgroundDrawable(Drawable drawable){
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
			base.setBackground(drawable);
		}
		
	}
	
	/**
	 * 
	 * @param resource
	 * 
	 * Sets the background color of the month bar
	 */
	public void setMonehtTextBackgroundResource(int resource){
		base.setBackgroundResource(resource);
	}
	
	/**
	 * 
	 * @param recource
	 * 
	 * change the image of the previous month button
	 */
	public void setPreviousMonthButtonImageResource(int recource){
		prev.setImageResource(recource);
	}
	
	/**
	 * 
	 * @param bitmap
	 * 
	 * change the image of the previous month button
	 */
	public void setPreviousMonthButtonImageBitmap(Bitmap bitmap){
		prev.setImageBitmap(bitmap);
	}
	
	/**
	 * 
	 * @param drawable
	 * 
	 * change the image of the previous month button
	 */
	public void setPreviousMonthButtonImageDrawable(Drawable drawable){
		prev.setImageDrawable(drawable);
	}
	
	/**
	 * 
	 * @param recource
	 * 
	 * change the image of the next month button
	 */
	public void setNextMonthButtonImageResource(int recource){
		next.setImageResource(recource);
	}
	
	/**
	 * 
	 * @param bitmap
	 * 
	 * change the image of the next month button
	 */
	public void setNextMonthButtonImageBitmap(Bitmap bitmap){
		next.setImageBitmap(bitmap);
	}
	
	/**
	 * 
	 * @param drawable
	 * 
	 * change the image of the next month button
	 */
	public void setNextMonthButtonImageDrawable(Drawable drawable){
		next.setImageDrawable(drawable);
	}
	
	/**
	 * 
	 * @param gestureType
	 * 
	 * Allow swiping the calendar left/right or up/down to change the month. 
	 * 
	 * Default value no gesture
	 */
	public void setGesture(int gestureType){
		this.gestureType = gestureType;
	}

}
