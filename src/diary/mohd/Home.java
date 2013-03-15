package diary.mohd;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.regex.Pattern;

import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;

import android.content.Intent;
import android.database.Cursor;

import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import android.widget.ListView;


/**This class is the default Home of the Application*/
public class Home extends Activity implements OnClickListener{
	
	//Instance Variable used to hold data that will used by the Activity
	private ArrayList<String> events;
	private ArrayList<String> eventids;
	
	private HashMap<String,String> eventDetails;
		
	private String userAccount;
	
	private ListView eventList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {				
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		
		//Algorithm to get Google accounts for device
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; 
		Account[] accounts = AccountManager.get(this).getAccounts();
		for (Account account : accounts) {
		    if (emailPattern.matcher(account.name).matches()) {
		        userAccount = account.name;
		    }
		}
		
		initEvents();
		ArrayAdapter<String> adapter;
		
		// If Events are not Found display the Events Not Found Message
		if(events.isEmpty()){
			events.add("No Events Found.");
			adapter = new ArrayAdapter<String>(this,
					  android.R.layout.simple_list_item_1, android.R.id.text1, events);
		
		}
		
		else{
			adapter = new ArrayAdapter<String>(this,
					  android.R.layout.simple_list_item_checked, android.R.id.text1, events);					
		}
		
		eventList=(ListView)findViewById(R.id.Events);
		eventList.setAdapter(adapter);
		
		//IF An Item from the Event List is clicked
		eventList.setOnItemClickListener(new OnItemClickListener(){			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg, int position,
					long arg3) {
				String eventitle=events.get(position);
				if(eventitle=="No Events Found."){
					//Do nothing
				}else{
					
				// Load the Update Entry Activity
				Intent updateentry= new Intent(getApplicationContext(), UpdateEntry.class);
				updateentry.putExtra("EventDetails", getEventDetails(position));
				startActivity(updateentry);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.addentry){
	        //handle the click here
			Intent newentry= new Intent(this, NewEntry.class);
			startActivity(newentry);
	    }
	}

	/**This method loads all the events in the calendar to the events arraylist*/
	public void initEvents(){
		// Projection array. Creating indices for this array instead of doing
		// dynamic lookups improves performance.
		final String[] EVENT_PROJECTION = new String[] {
		    Calendars._ID,                           // 0
		    Calendars.ACCOUNT_NAME,                  // 1
		    Calendars.CALENDAR_DISPLAY_NAME,         // 2
		    Calendars.OWNER_ACCOUNT                  // 3
		};
		  
		// The indices for the projection array above.
		final int PROJECTION_ID_INDEX = 0;
		
		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
		                        + Calendars.ACCOUNT_TYPE + " = ?) AND ("
		                        + Calendars.OWNER_ACCOUNT + " = ?))";
		String[] selectionArgs = new String[] {"MohdDiary", "com.google",
				userAccount};
		
		// Submit the query and get a Cursor object back. 
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null); 

		//Initialize the events Arraylist
		events=new ArrayList<String>();
		eventids=new ArrayList<String>();
		
		//Initialize Calendar ID
		String calendarID="";
		
		//Get Calendar
			while (cur.moveToNext()) {
				   
				    calendarID = String.valueOf(cur.getLong(PROJECTION_ID_INDEX));
				    
				   } 
			
		//Query Calendar for the title and id	
		    Cursor cursor = cr.query(
		            		Events.CONTENT_URI,
		            		new String[] {"title","_id" },
		            		"CALENDAR_ID=?", new String[]{calendarID},
		                    null);
		    
		//Add the Title and Id's of the events to the instance variables    
		    while(cursor.moveToNext()){

		    	events.add(cursor.getString(0));
		    	eventids.add(cursor.getString(1));
		        		    
		    }
	}

	/**This method gets the details of a particular event*/
	public HashMap<String,String> getEventDetails(int index){
		
			// Projection array. Creating indices for this array instead of doing
			// dynamic lookups improves performance.
			final String[] EVENT_PROJECTION = new String[] {
			    Calendars._ID,                           // 0
			    Calendars.ACCOUNT_NAME,                  // 1
			    Calendars.CALENDAR_DISPLAY_NAME,         // 2
			    Calendars.OWNER_ACCOUNT                  // 3
			};
			  
			// The indices for the projection array above.
			final int PROJECTION_ID_INDEX = 0;
			
			Cursor cur = null;
			ContentResolver cr = getContentResolver();
			Uri uri = Calendars.CONTENT_URI;   
			String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
			                        + Calendars.ACCOUNT_TYPE + " = ?) AND ("
			                        + Calendars.OWNER_ACCOUNT + " = ?))";
			String[] selectionArgs = new String[] {"MohdDiary", "com.google",
					userAccount};
			
			// Submit the query to get the calendar id. 
			cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null); 
			String calendarID="";
				   while (cur.moveToNext()) {
					    calendarID = String.valueOf(cur.getLong(PROJECTION_ID_INDEX));
					    
					   } 
				
			// Submit the query to get the event details.   
			 Cursor cursor = cr.query(
			            		Events.CONTENT_URI,
			            		new String[] {"title","description","dtstart","dtend","duration","rdate",
			            				"rrule","_id"},
			            		"(CALENDAR_ID=?)AND(_ID=?)", new String[]{calendarID,eventids.get(index)},
			                    null);
			    
			 eventDetails=new HashMap<String,String>();
			    
			 // get all details of an event.
			    while(cursor.moveToNext()){
		
			        eventDetails.put("title",cursor.getString(0));
			        eventDetails.put("description",cursor.getString(1));
			        eventDetails.put("dtstart",cursor.getString(2));
			        eventDetails.put("dtend",cursor.getString(3));
			        eventDetails.put("duration",cursor.getString(4));
			        eventDetails.put("rdate",cursor.getString(5));
			        eventDetails.put("rrule",cursor.getString(6));
			        eventDetails.put("_id",cursor.getString(7));
	
			    }
				return eventDetails;
		}
}
