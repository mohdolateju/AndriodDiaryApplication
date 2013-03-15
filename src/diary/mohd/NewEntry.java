package diary.mohd;


import java.util.Calendar;
import java.util.regex.Pattern;

import android.net.Uri;
import android.os.Bundle;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TimePicker;
import android.widget.Toast;

/**This Class is used to Create new events in the Calendar*/
public class NewEntry extends Activity implements OnClickListener, DatePickerDialog.OnDateSetListener{

	//Buttons to be used to Set Event Date,Occurences and Duration
	private Button entryTimeButton;
	private Button entryDateButton;
	private Button entryDurationButton;
	private Button entryOccurencesButton;
	
	//Calendars for creating events
	private Calendar setDate;
	private Calendar rightNow;
	private Calendar endDate;
	
	//Dialog Listeners for the duration and time of the event
	private TimePickerDialog.OnTimeSetListener durationDialogListener;
	private TimePickerDialog.OnTimeSetListener timeValueDialogListener;
	
	//The Variables to Hold the date of the event
	private int setDay;
	private int setMonth;
	private int setYear;
	
	//The Variables to Hold the Time of the Event in 12 hour format
	private int setTimeHr;
	private int setTimeMin;
	private String AmPm;
	
	//The Variables to hold the End Time of the Event
	private int endTimeHr;
	private int endTimeMin;	
	
	//The Initializing Default Duration
	private int setDurationMin=0;
	private int setDurationHrs=0;
	
	//Holds the User's Google Account Name
	private String userAccount;
	
	//Holds the Title and the Description of the Event
	private String entryTitle;
	private String entryDescription;
	
	private String occurenceType="DAILY";
	private int occurenceNo;
	
	/**Used to Initialize the Application*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_entry);

		//Algorithm for Getting the accounts in the device, if there multiple it selects the last one.
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; 
		Account[] accounts = AccountManager.get(this).getAccounts();
		for (Account account : accounts) {
		    if (emailPattern.matcher(account.name).matches()) {
		        this.userAccount = account.name;
		    }
		}
		
		//if The Calendar Account Doesnt't Exist on the Device Create one
		if(!calccountexist()){
			
				//Create calendar uri with asSyncAdapter Method
				Uri calUri= asSyncAdapter(CalendarContract.Calendars.CONTENT_URI,"MohdDiary",
										  userAccount);
				
				//Add Values of the new account
				ContentValues vals = new ContentValues();				
				vals.put(Calendars.ACCOUNT_NAME,"MohdDiary");
				vals.put(Calendars.ACCOUNT_TYPE,"com.google");
				vals.put(Calendars.NAME,"Mohammed's Diary");
				vals.put(Calendars.CALENDAR_DISPLAY_NAME,"Mohammed's Diary");
				vals.put(Calendars.CALENDAR_COLOR,0xffff0000);
				vals.put(Calendars.CALENDAR_ACCESS_LEVEL,Calendars.CAL_ACCESS_OWNER);
				vals.put(Calendars.OWNER_ACCOUNT,userAccount);
				vals.put(Calendars.SYNC_EVENTS, 1);
				
				//Create Calendar
				getContentResolver().insert(calUri, vals);
		}
		
	    //Get Current system time   
		this.rightNow = Calendar.getInstance();
		
		//Assign the default values of the time in case user doesn't select different time
		this.setTimeHr=twelvehour(this.rightNow.get(Calendar.HOUR_OF_DAY));
		this.setTimeMin=this.rightNow.get(Calendar.MINUTE);
		this.AmPm=amorpnm(this.rightNow.get(Calendar.HOUR_OF_DAY));

		//Assign the default values of the time in case user doesn't select different day
		this.setDay=this.rightNow.get(Calendar.DAY_OF_MONTH);
		this.setMonth=(this.rightNow.get(Calendar.MONTH)+1);
		this.setYear=this.rightNow.get(Calendar.YEAR);
		
		//Assign time to Button
		this.entryTimeButton=(Button) findViewById(R.id.TimeValue);
		this.entryTimeButton.setText(this.setTimeHr+":"+this.setTimeMin+" "+this.AmPm);
		
		//Assign Date to Button
		this.entryDateButton=(Button) findViewById(R.id.DateValue);
		this.entryDateButton.setText(this.setDay+"/"+this.setMonth+"/"+this.setYear);
		
		//Setting Default Duration as Button Text
		this.entryDurationButton=(Button) findViewById(R.id.DurationValue);
		this.entryDurationButton.setText(this.setDurationMin+" : "+this.setDurationHrs);
		
		//No Occurences Selected
		this.entryOccurencesButton=(Button) findViewById(R.id.Occurences);
		this.entryOccurencesButton.setText("No Occurences");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_new_entry, menu);
		return true;
	}
	
	/**Used to Respond to Button Click*/
	@Override
	public void onClick(View view) {
				
		// If Button Click is the Time
		if(view.getId() == R.id.TimeValue){
			
			//Define the TimePickerDialog Listener for the Button
			timeValueDialogListener=
						new TimePickerDialog.OnTimeSetListener() {				
								@Override
								public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
									
									setTimeHr=twelvehour(hourOfDay);
									setTimeMin=minute;
									AmPm=amorpnm(hourOfDay);
									
									entryTimeButton.setText(setTimeHr+": "+setTimeMin+" "+AmPm);
								}
					};
			
			TimePickerDialog timeDialog = new TimePickerDialog(this,timeValueDialogListener,
																this.rightNow.get(Calendar.HOUR_OF_DAY), 
																this.rightNow.get(Calendar.MINUTE), false);
			timeDialog.setTitle("Select Appointment Time");	        	        
			timeDialog.show();
	    }
		
		//If DateButton Was Clicked
		else if(view.getId() == R.id.DateValue){
			DatePickerDialog dateDialog = new DatePickerDialog(this,this, 
																this.rightNow.get(Calendar.YEAR), 
																this.rightNow.get(Calendar.MONTH), 
																this.rightNow.get(Calendar.DAY_OF_MONTH));
	        dateDialog.setTitle("Select Appointment Date");	        	        
	        dateDialog.show();
	    }
	
		//If Duration Button is Clicked
		else if(view.getId() == R.id.DurationValue){
			
			//Define the TimePickerDialog Listener for the Button
			durationDialogListener=new TimePickerDialog.OnTimeSetListener() {
				
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					setDurationHrs=hourOfDay;
					setDurationMin=minute;
					
					entryDurationButton.setText(setDurationHrs+":"+setDurationMin);
				}
			};
			
			TimePickerDialog durationDialog = new TimePickerDialog(this, 0,durationDialogListener,0, 0, true);
	        durationDialog.setTitle("Appointment Duration (HRS)");	        	        
	        durationDialog.show();
	    }
		
		//If the occurence button is clicked
		if(view.getId() == R.id.Occurences){
			
			// Where we track the selected items  
			AlertDialog.Builder occurenceDialog = new AlertDialog.Builder(this);
			
			//Setting Range of the numbers which will change depending on whether
			//the user wants to set Re-Occurence for days weeks or months
			final NumberPicker occurenceNoPicker=new NumberPicker(this);
			occurenceNoPicker.setMinValue(1);
			occurenceNoPicker.setOnValueChangedListener(new OnValueChangeListener(){

				@Override
				public void onValueChange(NumberPicker arg0, int olvVal, int newVal) {
					occurenceNo=newVal;					
				}});
			
		    // Set the dialog title
		    occurenceDialog.setTitle("Select No of Re-Occurences")
		    
		    // Specify the list array, the items to be selected by default (null for none),
		    // and the listener through which to receive callbacks when items are selected
		           .setSingleChoiceItems(R.array.occurences,-1 ,
		                      new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//Daily Re-Occurence is selected Set max re-occurence number to 365
									if(which==0){
										occurenceNo=1;
										occurenceType="DAILY";
										occurenceNoPicker.setMaxValue(365);
									}
									//Weekly Re-Occurence is selected Set max re-occurence number to 52
									else if(which==1){
										occurenceNo=1;
										occurenceType="WEEKLY";
										occurenceNoPicker.setMaxValue(52);
									}
									//Monthly Re-Occurence is selected Set max re-occurence number to 12
									else if(which==2){
										occurenceNo=1;
										occurenceType="MONTHLY";
										occurenceNoPicker.setMaxValue(12);
									}
								}
							})
							
				// Set the action buttons
		           .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        	   
		               @Override
		               public void onClick(DialogInterface dialog, int which) {
		            	 //Daily Re-Occurence is selected Set max re-occurence number to 365		            	   								
								entryOccurencesButton.setText(occurenceType+" "+occurenceNo);
		            	   
		               }
		           })
		           
		           //if cancel button is clicked erase occurency values
		           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		            	   occurenceType="DAILY";
		            	   occurenceNo=0;
		            	   entryOccurencesButton.setText("No-Occurences");
		               }
		           });
		    
		    
		    
		    occurenceDialog.setView(occurenceNoPicker);
		    occurenceDialog.create();				        	        
	        occurenceDialog.show();
	    }
		
		//If Submit Button is clicked 
		if(view.getId()==R.id.Submit){
			
			//Get Entered title 
			EditText TitleButton=(EditText) findViewById(R.id.TitleText);
			entryTitle=TitleButton.getText().toString();
			
			//Get Entered description
			EditText descText=(EditText) findViewById(R.id.DescriptionValue);
			entryDescription=descText.getText().toString();
			
			//if title or description is empty don't proceed with new entry creation
			//And give filled required notification
			if(entryTitle.isEmpty()||entryDescription.isEmpty()){				
				Toast toast = Toast.makeText(this, "Please Fill All Fields & Entry Duration", 
													Toast.LENGTH_LONG);
				toast.show();
				
			}
			// If Title and description where entered initiate new entry creation process
			else{
				
			//If set Time is in pm add 12 hours to begin date of the event 	
			int starthr24=setTimeHr;
			if(AmPm=="PM"){starthr24+=12;}
			
			//Initialize set date specifically for the setDate Calendar Object 
			setDate = Calendar.getInstance();
			setDate.set(setYear, setMonth, setDay,starthr24, setTimeMin);
			
			//Call the method to calculate the End date of the event
			setEndDate();
			
			Intent intent = new Intent(Intent.ACTION_INSERT)
					.setData(Events.CONTENT_URI)
			        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, setDate.getTimeInMillis())
			        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.getTimeInMillis())
			        .putExtra(Calendars.NAME,"Mohammed's Diary")
					.putExtra(Calendars.CALENDAR_DISPLAY_NAME,"Mohammed's Diary")
			        .putExtra(Calendars.OWNER_ACCOUNT,userAccount)
			        .putExtra(Events.TITLE, entryTitle)
					.putExtra(Events.DESCRIPTION, entryDescription);
					if(this.occurenceNo>0){		        
			        intent.putExtra(Events.RRULE,"FREQ="+this.occurenceType+";COUNT="+this.occurenceNo);
			        }			            
			        
			
			startActivity(intent);
			
			
			}			
			
		}
	}
	
	/**CallBack method For the DatePicker Dialog*/
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear , int dayOfMonth) {
		
		//Set Values from DatePicker Dialog to Entry Date Button
		this.setDay=dayOfMonth;
		this.setMonth=(monthOfYear+1);
		this.setYear=year;
		
		entryDateButton.setText(this.setDay+"/"+this.setMonth+"/"+this.setYear);
	}

	/**This method tests to see if a calendar account exists
	 *@return TRUE if calendar account exist FALSE if calendar account doesnt 
	 */
	public boolean calccountexist(){
		
		// Projection array. Creating indices for this array instead of doing
		// dynamic lookups improves performance.
		final String[] EVENT_PROJECTION = new String[] {
		    Calendars._ID,                           // 0
		    Calendars.ACCOUNT_NAME,                  // 1
		    Calendars.CALENDAR_DISPLAY_NAME,         // 2
		    Calendars.OWNER_ACCOUNT                  // 3
		};
		
		  


		// Set Values for the Query	
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
			
			   if (cur.moveToNext()) {
				   return true;
				   } 
			
			   return false; 
			
	}
	
	/**Method to return a constructed uri	  
	 * @param uri
	 * @param account
	 * @param accountType
	 * @return Uri
	 */
	static Uri asSyncAdapter(Uri uri, String account, String accountType) {
	    return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	 }
	
	/**Method to convert 24 hours hour to 12 hour
	 *@param int 24 Hour 
	 *@return int 12 hour 
	 */
	private int twelvehour(int hour){
		
		if(hour==0){
			return 12;
		}
		else if(hour<=12){
			return hour;
		}
		else{
			return hour-12;
				}
	}
	
	/**Method to convert 24 hours hour to 12 hour and return the AM OR PM Value
	 *@param int 24 Hour 
	 *@return String 12 hour AM or PM
	 */
	private String amorpnm(int hour){
		
	
		if(hour<12){
			return "AM";
		}else{
			return "PM";
				}
	}

	/**
	 *Calculates the End Date of the event
	 */
	private void setEndDate(){
		endDate = Calendar.getInstance();
		
		
		endTimeMin=setTimeMin+setDurationMin;
		if(endTimeMin>60){
			endTimeMin-=60;
			++setTimeHr;
		}
		
		if(AmPm=="PM"){endTimeHr+=12;}
		
		
		
		endTimeHr=setTimeHr+setDurationHrs;
		
		
		endDate.set(setYear, setMonth, setDay, endTimeHr, endTimeMin);
		
		if(endTimeHr>24){
			endTimeHr-=24;
			endDate.add(Calendar.DAY_OF_MONTH, 1);			
		}
		
		
	}
	
}
