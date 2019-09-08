package com.example.myblogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button LoginBtn,LoginRegBtn;
    private EditText LoginEmail,LoginPassword;
    private FirebaseAuth mAuth;
    private ProgressBar LoginProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        LoginBtn = findViewById(R.id.regCreate);
        LoginRegBtn = findViewById(R.id.regLogin);
        LoginEmail = findViewById(R.id.regEmail);
        LoginPassword  = findViewById(R.id.regPassword);
        LoginProgress = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = LoginEmail.getText().toString();
                String loginPassword = LoginPassword.getText().toString();
                if( !TextUtils.isEmpty(loginEmail)  && !TextUtils.isEmpty(loginPassword))
                {
                    LoginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                              Intent setupIntent = new Intent(LoginActivity.this,AccountSetUpActivity.class);
                              startActivity(setupIntent);
                              finish();

                            }else
                            {
                                String e = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: "+e, Toast.LENGTH_SHORT).show();
                            }
                            LoginProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }



            }
        });

        LoginRegBtn.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Determine is Weather User is Login or Not!
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
          sendToMain();
        }

    }

    private void sendToMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);

    }

    @Override
    public void onClick(View v) {
        Intent mainIntent = new Intent(this, RegisterAccountActivity.class);
        startActivity(mainIntent);
    }
}
