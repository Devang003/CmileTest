package com.example.testcmile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rilixtech.CountryCodePicker;

public class SignUpActivity extends AppCompatActivity {
    private CountryCodePicker mCountryCodePicker;
    private Button mButtonSend;
    private EditText mMobileNumberEditText;
    private FirebaseUser mFirebaseUSer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle("SingUp");
        getSupportActionBar().setIcon(R.drawable.cmile);



        mFirebaseUSer = FirebaseAuth.getInstance().getCurrentUser();

        if(mFirebaseUSer != null){
            Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }


        mCountryCodePicker = findViewById(R.id.ccp);
        mButtonSend = findViewById(R.id.buttonSend);
        mMobileNumberEditText =findViewById(R.id.PhoneNumberEdt);

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mobile = mMobileNumberEditText.getText().toString().trim();

                if(mobile.isEmpty() || mobile.length() < 10){
                    mMobileNumberEditText.setError("Enter a valid mobile");
                    mMobileNumberEditText.requestFocus();
                    return;
                }

                Intent intent = new Intent(SignUpActivity.this, PhoneNumberVerification.class);
                intent.putExtra("mobile", mobile);
                startActivity(intent);
            }
        });
    }
}

