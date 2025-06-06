package com.example.project;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import com.google.firebase.auth.FirebaseUser;
import com.mobile.app.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Homepage extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user;
    FusedLocationProviderClient fusedLocationProviderClient;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LANGUAGE = "En";
    private long pressedTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        getSupportActionBar().hide();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(loadData().equals("En")) {
            ImageView imageView=findViewById(R.id.imageView2);
            imageView.setContentDescription("Sign out");
            Button button1=findViewById(R.id.b1);
            Button button2=findViewById(R.id.b2);
            Button button3=findViewById(R.id.b3);
            Button button4=findViewById(R.id.b4);
            Button button5=findViewById(R.id.b5);
            Button button6=findViewById(R.id.b6);
            button1.setAllCaps(false);
            button2.setAllCaps(false);
            button3.setAllCaps(false);
            button4.setAllCaps(false);
            button5.setAllCaps(false);
            button6.setAllCaps(false);
            button1.setText("  PROFILE");
            button2.setText("  MAPS");
            button3.setText("  COMMUNICATION");
            button4.setText("  EMERGENCY CALL");
            button5.setText("  CALENDER");
            button6.setText("  CONTACT US");
            button1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.personal_profile, 0);
            button2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.map, 0);
            button3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.communicate, 0);
            button4.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.video_call, 0);
            button5.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.calendar, 0);
            button6.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.contact, 0);
        }
    }

    public void signOut(View view) {
        mAuth.signOut();
        finish();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);

    }

    public void personalProfile(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                return;
            }
            User user = documentSnapshot.toObject(User.class);
            if (user == null) {
                Toast.makeText(this, "Failed to parse user data", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getApplicationContext(), PersonalProfile.class);
            intent.putExtra("user", user);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
        });
    }


    public void map(View view) {
        Intent intent = new Intent(this, HomeMaps.class);
        startActivity(intent);
    }

    public void communicate(View view) {
        Intent intent = new Intent(this, Conversation.class);
        startActivity(intent);
    }

    public void assistanceVideoCall(View view) {
        getlocation();
        if(isDuoInstalled()){
        Intent duo = new Intent("com.google.android.apps.tachyon.action.CALL");
        duo.setData(Uri.parse("tel: " + "1233456789"));
        duo.setPackage("com.google.android.apps.tachyon");
        startActivity(Intent.createChooser(duo, "Duo is not installed."));
        }
        else{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.google.android.apps.tachyon"));
            startActivity(intent);
        }
    }

    public void calendar(View view) {
        Intent intent = new Intent(this, Calendar.class);
        startActivity(intent);
    }

    public void feedbackForm(View view) {
        Intent intent = new Intent(this, FeedBack.class);
        startActivity(intent);
    }
    public void changeLanguage(View view){
        String currentLanguage=loadData();
        saveData(currentLanguage);
        // to reload activity
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            if(loadData().equals("En"))
                Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getBaseContext(), "Nhấp lại để thoát", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }


    void getlocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            askLocationPermission();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();

        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //We have a location
                    Log.d("TAG", "onSuccess: " + location.toString());
                    Log.d("TAG", "onSuccess: " + location.getLatitude());
                    Log.d("TAG", "onSuccess: " + location.getLongitude());
                    String googleMap="http://www.google.com/maps/place/"+location.getLatitude()+","+location.getLongitude();
                    Map<String, String> data = new HashMap<>();
                    data.put("Last Location", googleMap);
                    db.collection("users").document(FirebaseAuth.getInstance().getUid()).set(data, SetOptions.merge());
                } else  {
                    Log.d("TAG", "onSuccess: Location was null...");
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: " + e.getLocalizedMessage() );
            }
        });
    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("TAG", "askLocationPermission: you should show an alert dialog...");
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLastLocation();
            } else {
                //Permission not granted
            }
        }
    }
    private boolean isDuoInstalled() {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo("com.google.android.apps.tachyon", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
    public void saveData(String currentLanguage) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(currentLanguage.equals("Vn"))
            editor.putString(LANGUAGE,"En");
        else
            editor.putString(LANGUAGE,"Vn");
        editor.apply();
    }
    public String loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String currentLanguage = sharedPreferences.getString(LANGUAGE, "Vn");
        return currentLanguage;
    }
}
