package com.example.testcmile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.example.testcmile.PhoneNumberVerification.IS_LOGIN;
import static com.example.testcmile.PhoneNumberVerification.MY_PREF;

public class DetailActivity extends AppCompatActivity {

    public static final String IS_REGISTER = "isRegister";
    public static final String UID = "UID";
    private SharedPreferences.Editor mEditor;
    private EditText mEdtUserName;
    private EditText mEdtMobileNumber;
    private Button mBtnSave;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setTitle("User Detail");

        mEdtUserName = findViewById(R.id.EditTextUserName);
        mEdtMobileNumber = findViewById(R.id.EditTextMobileNumber);
        mBtnSave = findViewById(R.id.ButtonSave);

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = mEdtUserName.getText().toString();
                String mobile = mEdtMobileNumber.getText().toString();

                DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

                String userId = mDatabaseRef.push().getKey();

                User user = new User();
                user.setUsername(username);
                user.setNumber(mobile);
                user.setLat("");
                user.setLong("");
                mDatabaseRef.child(userId).setValue(user);

                mEditor = getSharedPreferences(MY_PREF, MODE_PRIVATE).edit();
                mEditor.putBoolean(IS_REGISTER, true);
                mEditor.putString(UID, userId);
                mEditor.apply();

                Intent intent = new Intent(DetailActivity.this,MainActivity.class);
                startActivity(intent);
                finish();


            }
        });

    }
}