package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.mobile.app.R;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddEvent extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button date_timeButton;
    EditText Name;
    Date date;
    int eventID;
    int H=0;
    int M=0;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LANGUAGE = "Vn";


    protected void onCreate(Bundle savedInstanceState) {
        try {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        getSupportActionBar().hide();

        Name = findViewById(R.id.name);
        date_timeButton = findViewById(R.id.date_time);
        date=new Date();
        date.setYear(getIntent().getIntExtra("year",0)-1900);
        date.setMonth(getIntent().getIntExtra("month",0));
        date.setDate(getIntent().getIntExtra("day",0));
        date.setHours(0);
        date.setMinutes(0);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd                                      hh:mm a");
        date_timeButton.setText(formatter.format(date));
        date_timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTimeButton();
            }
        });
        if(loadData().equals("En")) {
            Name.setHint("Name of the reminder");
            Button button=findViewById(R.id.save);
            button.setText("Save");
            button.setAllCaps(false);
        }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    @SuppressLint("ScheduleExactAlarm")
    public void addEvent(View view){
        try {
            if(Name.getText().toString().trim().length() == 0) {
                if(loadData().equals("En")) {
                    Toast.makeText(this,"Name is empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Truờng Name đang trống", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(this, "User is not signed in", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentReference newEventRef = db.collection("users").document(uid).collection("Events").document();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(AddEvent.this, "User data not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                User user = documentSnapshot.toObject(User.class);
                if (user == null) {
                    Toast.makeText(AddEvent.this, "Failed to parse user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                eventID = user.getNumOfEvent();
                Event event = new Event(newEventRef.getId(), eventID, Name.getText().toString(), date);
                userRef.update("numOfEvent", FieldValue.increment(1));
                newEventRef.set(event); // add to firebase

                Date now = new Date();
                if(date.after(now)) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(getApplication(), AlertReceiver.class);
                    intent.putExtra("name", event.getName());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplication(), eventID, intent, PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, event.getTime().getTime(), pendingIntent);
                }

                if(loadData().equals("En")) {
                    Toast.makeText(AddEvent.this,"Reminder has been added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddEvent.this,"Lời nhắc nhỡ đã được tạo", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(AddEvent.this, Calendar.class);
                startActivity(intent);
                finish();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("AddEvent", "Exception in addEvent", e);
        }
    }


    private void handleTimeButton() {
        try {
        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                M=minute;
                H=hour;
                date.setHours(hour);
                date.setMinutes(minute);
                date.setSeconds(0);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd                                      hh:mm a");
                date_timeButton.setText(formatter.format(date));
            }
        },0,0, is24HourFormat);

        timePickerDialog.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public String loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String currentLanguage = sharedPreferences.getString(LANGUAGE, "Ar");
        return currentLanguage;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddEvent.this, Calendar.class);
        startActivity(intent);
        finish();
    }
}
