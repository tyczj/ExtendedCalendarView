package com.tyczj.extendedcalendarview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.format.Time;
import android.widget.BaseAdapter;

public class Day{
	
	int startDay;
	int monthEndDay;
	int day;
	int year;
	int month;
	Context context;
	BaseAdapter adapter;
	ArrayList<Event> events = new ArrayList<Event>();
	
	Day(Context context,int day, int year, int month){
		this.day = day;
		this.year = year;
		this.month = month;
		this.context = context;
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, day);
		int end = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(year, month, end);
		TimeZone tz = TimeZone.getDefault();
		monthEndDay = Time.getJulianDay(cal.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
	}
	
//	public long getStartTime(){
//		return startTime;
//	}
//	
//	public long getEndTime(){
//		return endTime;
//	}
	
	public int getMonth(){
		return month;
	}
	
	public int getYear(){
		return year;
	}
	
	public void setDay(int day){
		this.day = day;
	}
	
	public int getDay(){
		return day;
	}
	
	/**
	 * Add an event to the day
	 * 
	 * @param event
	 */
	public void addEvent(Event event){
		events.add(event);
	}
	
	/**
	 * Set the start day
	 * 
	 * @param startDay
	 */
	public void setStartDay(int startDay){
		this.startDay = startDay;
		new GetEvents().execute();
	}
	
	public int getStartDay(){
		return startDay;
	}
	
	public int getNumOfEvenets(){
		return events.size();
	}
	
	/**
	 * Returns a list of all the colors on a day
	 * 
	 * @return list of colors
	 */
	public Set<Integer> getColors(){
		Set<Integer> colors = new HashSet<Integer>();
		for(Event event : events){
			colors.add(event.getColor());
		}
		
		return colors;
	}
	
	/**
	 * Get all the events on the day
	 * 
	 * @return list of events
	 */
	public ArrayList<Event> getEvents(){
		return events;
	}
	
	public void setAdapter(BaseAdapter adapter){
		this.adapter = adapter;
	}
	
	private class GetEvents extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Cursor c = context.getContentResolver().query(CalendarProvider.CONTENT_URI,new String[] {CalendarProvider.ID,CalendarProvider.EVENT,
					CalendarProvider.DESCRIPTION,CalendarProvider.LOCATION,CalendarProvider.START,CalendarProvider.END,CalendarProvider.COLOR},"?>="+CalendarProvider.START_DAY+" AND "+ CalendarProvider.END_DAY+">=?",
					new String[] {String.valueOf(startDay),String.valueOf(startDay)}, null);
			if(c != null){
				if(c.moveToFirst()){
					do{
						Event event = new Event(c.getLong(0),c.getLong(4),c.getLong(5));
						event.setName(c.getString(1));
						event.setDescription(c.getString(2));
						event.setLocation(c.getString(3));
						event.setColor(c.getInt(6));
						events.add(event);
					}while(c.moveToNext());	
				}	
				c.close();
			}
			
			return null;
		}
		
		protected void onPostExecute(Void par){
			adapter.notifyDataSetChanged();
		}
		
	}
	

}
