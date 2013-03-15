package diary.mohd;




import java.util.Calendar;

import java.util.HashMap;



import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;

import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.NumberPicker.OnValueChangeListener;


/**This Class Updates and Deletes Entries in the Calendar*/
public class UpdateEntry extends Activity implements OnClickListener, DatePickerDialog.OnDateSetListener {

	//Buttons of the Activity
	private Button entryTimeButton;
	private Button entryDateButton;
	private Button entryDurationButton;
	private Button entryOccurencesButton;
	
	//Editable Text of the Activity
	private EditText entryTitleEditText;
	private EditText entryDescriptionEditText;
	
	//Dialog Listeners for the duration and time of the event
	private TimePickerDialog.OnTimeSetListener durationDialogListener;
	private TimePickerDialog.OnTimeSetListener timeValueDialogListener;
	
	//Date for the particular events
	private Calendar eventStartDate;
	private Calendar eventEndDate;
	private long eventID;
	
	//The Variables to Hold the date of the event
	private int entryStartDay;
	private int entryStartMonth;
	private int entryStartYear;
		
	//The Variables to Hold the Time of the Event in 12 hour format
	private int entryStartTimeHr;
	private int entryStartTimeMin;
	private String entryStartAmPm;
		
	//The Variables to hold the End Time of the Event
	private int endTimeHr;
	private int endTimeMin;	
		
	//The Initializing Default Duration
	private int durationhr;
	private int durationmin;
	
	//Holds the Title and the Description of the Event
	private String entryTitle;
	private String entryDescription;
		
	//Occurrence variable
	private String occurenceType="DAILY";
	private int occurenceNo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_entry);
		
		//Get the event Data from Intent
		Bundle text = getIntent().getExtras();
		HashMap<String,String> eventDetails=(HashMap)text.get("EventDetails");
		
		//Assign the EventId to be used for deleting and updating
		eventID=Long.parseLong((String)eventDetails.get("_id"));
		
		//Assign title to Text field
		this.entryTitleEditText=(EditText) findViewById(R.id.TitleText);
		this.entryTitleEditText.setText((String)eventDetails.get("title"));
						
		//Assign time to Button
		this.entryTimeButton=(Button) findViewById(R.id.TimeValue);
		
		//Getting the start date and converting it to the Calendar format to be displayed
		long startEvent=Long.parseLong(eventDetails.get("dtstart"));
		eventStartDate=Calendar.getInstance();		
		eventStartDate.setTimeInMillis(startEvent);
		
		entryStartTimeHr=twelvehour(eventStartDate.get(Calendar.HOUR_OF_DAY));
		entryStartTimeMin=eventStartDate.get(Calendar.MINUTE);
		this.entryTimeButton.setText(entryStartTimeHr+":"+entryStartTimeMin+" "+amorpnm(entryStartTimeHr));
				
		//Assign Date to Button
		this.entryDateButton=(Button) findViewById(R.id.DateValue);
		
		entryStartDay=eventStartDate.get(Calendar.DAY_OF_MONTH);
		entryStartMonth=eventStartDate.get(Calendar.MONTH);
		entryStartYear=eventStartDate.get(Calendar.YEAR);
		
		this.entryDateButton.setText(entryStartDay+"/"+entryStartMonth+"/"+entryStartYear);
				
		//Getting the end date and converting it to the duration of the activity
		this.entryDurationButton=(Button) findViewById(R.id.DurationValue);	
		long endEvent=Long.parseLong((String)eventDetails.get("dtend"));		
		long durationmillis=endEvent-startEvent;
		
		if((durationmillis/3600000)<0){
			durationhr=0;
		}else{
			durationhr=(int)durationmillis/3600000;
			}
		
		if((durationmillis/60000)<0){
			durationmin=0;
		}else{
		durationmin=(int)((durationmillis/60000)-(durationhr*60));
		}
		
		//Assigning the Duration of the activity to the button
		eventEndDate=Calendar.getInstance();		
		eventEndDate.setTimeInMillis(endEvent);		
		this.entryDurationButton.setText(durationhr+":"+durationmin);
				
		//Set Description field
		this.entryDescriptionEditText=(EditText) findViewById(R.id.DescriptionValue);
		this.entryDescriptionEditText.setText((String)eventDetails.get("description"));
		
		//No Occurences Selected
		this.entryOccurencesButton=(Button) findViewById(R.id.Occurences);
		occurenceType=(String)eventDetails.get("rrule");
		this.entryOccurencesButton.setText(occurenceType);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_update_entry, menu);
		return true;
	}

	@Override
	public void onClick(View button) {
		// If Button Click is the Time
				if(button.getId() == R.id.TimeValue){
					
					//Define the TimePickerDialog Listener for the Button
					timeValueDialogListener=
								new TimePickerDialog.OnTimeSetListener() {				
										@Override
										public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
											
											entryStartTimeHr=twelvehour(hourOfDay);
											entryStartTimeMin=minute;
											entryStartAmPm=amorpnm(hourOfDay);
											
											entryTimeButton.setText(entryStartTimeHr+": "
																	+entryStartTimeHr+" "
																	+entryStartAmPm);
										}
							};
					
					TimePickerDialog timeDialog = new TimePickerDialog(this,timeValueDialogListener,
																		entryStartTimeHr, 
																		entryStartTimeMin, false);
					timeDialog.setTitle("Select Appointment Time");	        	        
					timeDialog.show();
			    }
				
				//If DateButton Was Clicked
				else if(button.getId() == R.id.DateValue){
					DatePickerDialog dateDialog = new DatePickerDialog(this,this, 
																		entryStartYear, 
																		entryStartMonth, 
																		entryStartDay);
			        dateDialog.setTitle("Select Appointment Date");	        	        
			        dateDialog.show();
			    }
			
				//If Duration Button is Clicked
				else if(button.getId() == R.id.DurationValue){
					
					//Define the TimePickerDialog Listener for the Button
					durationDialogListener=new TimePickerDialog.OnTimeSetListener() {
						
						@Override
						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
							durationhr=hourOfDay;
							durationmin=minute;
							
							entryDurationButton.setText(durationhr+":"+durationmin);
						}
					};
					
					TimePickerDialog durationDialog = new TimePickerDialog(this, 0,durationDialogListener,0, 0, true);
			        durationDialog.setTitle("Appointment Duration (HRS)");	        	        
			        durationDialog.show();
			    }
				
				//If the occurence button is clicked
				else if(button.getId() == R.id.Occurences){
					
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
				
			else if(button==findViewById(R.id.UpdateEntry)){
				
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
				int starthr24=entryStartTimeHr;
				if(entryStartAmPm=="PM"){starthr24+=12;}
				
				//Initialize set date specifically for the setDate Calendar Object 	
				Calendar entryStartDate=Calendar.getInstance();
				entryStartDate.set(entryStartYear, entryStartMonth, entryStartDay,
												starthr24, entryStartTimeMin);
				setEndDate();
				
				ContentValues values = new ContentValues();
				
				values.put(Events.TITLE, this.entryTitleEditText.getText().toString());
				values.put(Events.DESCRIPTION, this.entryDescriptionEditText.getText().toString());				
		        values.put(Events.DTSTART, entryStartDate.getTimeInMillis());
		        values.put(Events.DTEND, eventEndDate.getTimeInMillis());		        
				
				if(this.occurenceNo>0){		        
		        values.put(Events.RRULE,"FREQ="+this.occurenceType+";COUNT="+this.occurenceNo);
		        }	
				
				//Update event
				String[] selArgs =new String[]{Long.toString(eventID)};
				getContentResolver().update(Events.CONTENT_URI,values, 
				               								Events._ID + " =? ", selArgs);			
							
				//After Updating the Event, Load the Home Event
				Intent home= new Intent(this, Home.class);
				startActivity(home);
				
				Toast toast = Toast.makeText(this, "Event Details Updated", 
						Toast.LENGTH_LONG);
				toast.show();
				}
		}
		
		// If Delete entry button is clicked
		else if(button==findViewById(R.id.DeleteEntry)){
			
			//Create A Dialog for Delete Event  
			AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);

		    // Set the dialog title
		    deleteDialog.setTitle("Delete Event, Are You Sure?")
		    
		    
				// Set the action buttons
		    
		    		//If Yes Button Is Clicked Start Event Deletion process 
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		        	   
		        	   //Callback Listener
		               @Override
		               public void onClick(DialogInterface dialog, int which) {
		            	   
		            	 //convert id to long
		       			String[] selArgs =new String[]{Long.toString(eventID)};
		       			
		       			//Delete even
		       			getContentResolver().delete(Events.CONTENT_URI, 
		       			               								Events._ID + " =? ", selArgs);
		            	  		            	   		            	   
		       		//After Deleting the Event, Load the Home Event
		    			Intent home= new Intent(getApplicationContext(), Home.class);
		    			startActivity(home);
		    			
		    			Toast toast = Toast.makeText(getApplicationContext(), "Event Deleted", 
		    					Toast.LENGTH_LONG);
		    			toast.show();   
		               }
		           })
		           
		           //if NO button is clicked do nothing
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		            	   
		               }
		           });
		        
		    deleteDialog.create();				        	        
	        deleteDialog.show();
	    }
					
			
			
		
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear , int dayOfMonth) {
		// TODO Auto-generated method stub
		//Set Values from DatePicker Dialog to Entry Date Button
				this.entryStartDay=dayOfMonth;
				this.entryStartMonth=(monthOfYear+1);
				this.entryStartYear=year;
				
				this.entryDateButton.setText(this.entryStartDay+"/"+
											 this.entryStartMonth+"/"+
											 this.entryStartYear);
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
		eventEndDate = Calendar.getInstance();
		
		
		endTimeMin=entryStartTimeMin+durationmin;
		if(endTimeMin>60){
			endTimeMin-=60;
			++entryStartTimeHr;
		}
		
		if(entryStartAmPm=="PM"){endTimeHr+=12;}
		
		
		endTimeHr=entryStartTimeHr+durationhr;
		
		
		eventEndDate.set(entryStartYear, entryStartMonth, entryStartDay, endTimeHr, endTimeMin);
		
		if(endTimeHr>24){
			endTimeHr-=24;
			eventEndDate.add(Calendar.DAY_OF_MONTH, 1);			
		}
		
		
	}

}
