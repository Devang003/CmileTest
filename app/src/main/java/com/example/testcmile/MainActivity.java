package com.example.testcmile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.testcmile.PhoneNumberVerification.MY_PREF;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {


    private Boolean mIsLogin;
    private Boolean mIsRegister;
    private String mMobile;
    private SharedPreferences mSharedPreferences;
    private DatabaseReference mDatabaseReference;
    private SharedPreferences.Editor mEditor;
    private TextView mTxtUserName;
    private TextView mTxtMobile;

    private Button mBtnStart;

    private static final String TAG = "CurrentLocationApp";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mLocationDatabaseReference;

    private ProgressBar mProgressBar;

    String value_lat = null;
    String value_lng = null;


    final Handler mHandler = new Handler();
    private Timer mTimer;
    private TimerTask mTimerTask;
    private User user = null;

    private int LOCATION_PERMISSION_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setIcon(R.drawable.cmile);
        getSupportActionBar().setTitle("Dashboard");

        mTxtUserName = findViewById(R.id.textViewUserName);
        mTxtMobile = findViewById(R.id.textViewPhoneNumber);
        mBtnStart = findViewById(R.id.buttonStart);

        mProgressBar = findViewById(R.id.progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        mBtnStart.setTag(1);
        mBtnStart.setText("START");


        mBtnStart.setOnClickListener(new OnButtonStartClick());

        buildGoogleApiClient();
        location();


        mSharedPreferences = getSharedPreferences(PhoneNumberVerification.MY_PREF, MODE_PRIVATE);

        mIsRegister = mSharedPreferences.getBoolean(DetailActivity.IS_REGISTER, false);

        mIsLogin = mSharedPreferences.getBoolean(PhoneNumberVerification.IS_LOGIN, false);
        if (!mIsLogin) {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        }

        mMobile = mSharedPreferences.getString(PhoneNumberVerification.MOB_NUMBER, "0000000000");


        database();

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.clear();
                editor.apply();
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                finish();
                return true;
        }
        return false;
    }

    public void database() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseReference.orderByChild("number").equalTo(mMobile).addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (dataSnapshot.getValue() != null) {


                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        user = userSnapshot.getValue(User.class);
                        mTxtUserName.setText(user.getUsername().toString());
                        mTxtMobile.setText(mMobile);
                        mProgressBar.setVisibility(View.GONE);
                    }


                } else if (mIsLogin && dataSnapshot.getValue() == null) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {


            value_lat = String.valueOf(mLastLocation.getLatitude());
            value_lng = String.valueOf(mLastLocation.getLongitude());


        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mLastLocation != null) {

            value_lat = String.valueOf(mLastLocation.getLatitude());
            value_lng = String.valueOf(mLastLocation.getLongitude());


        }
    }


    class OnButtonStartClick implements View.OnClickListener {


        @Override
        public void onClick(View v) {

            final int status = (Integer) v.getTag();

            if (status == 1) {
                mTimer = new Timer();
                TimerForInterval();
                mTimer.schedule(mTimerTask, 0, 600000);
                mBtnStart.setText("STOP");
                v.setTag(0);
            } else {
                mTimer.cancel();
                mBtnStart.setText("START");
                v.setTag(1);
            }


            isAllowed();
            requestLocationPermission();

        }

    }

    public void TimerForInterval() {


        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            if (mLastLocation != null) {

                                mSharedPreferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);

                                String uID = mSharedPreferences.getString(DetailActivity.UID, null);

                                user.setUsername(mTxtUserName.getText().toString());
                                user.setNumber(mTxtMobile.getText().toString());
                                user.setLat(value_lat);
                                user.setLong(value_lng);


                                mDatabaseReference.child("users").getParent().child(uID).child("lat").setValue(value_lat);
                                mDatabaseReference.child("users").getParent().child(uID).child("long").setValue(value_lng);

                            }
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
    }


    public void location() {

        FirebaseApp.initializeApp(this);
        if (mFirebaseUser != null) {
            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        } else {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private boolean isAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    //Requesting permission
    private void requestLocationPermission() {

        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission granted now you can access Location", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }


}
