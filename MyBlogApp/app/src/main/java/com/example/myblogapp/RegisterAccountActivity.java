package com.example.myblogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText regEmail, regPassword, regPasswordConfirm;
    private Button regCreate,regLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);
        regEmail = findViewById(R.id.regEmail);
        regPassword  =findViewById(R.id.regPassword);
        regPasswordConfirm = findViewById(R.id.regPasswordConfirm);
        regCreate  =findViewById(R.id.regCreate);
        regLogin = findViewById(R.id.regLogin);
        mAuth = FirebaseAuth.getInstance();

        regCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regEmail.getText().toString();
                String pass = regPassword.getText().toString();
                String c_pass= regPasswordConfirm.getText().toString();
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(c_pass))
                {
                    if(pass.equals(c_pass))
                    {
                    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Intent accountSetup = new Intent(RegisterAccountActivity.this,AccountSetUpActivity.class);
                                startActivity(accountSetup);
                                finish();
                            }
                            else
                            {
                                String e = task.getException().getMessage();
                                Toast.makeText(RegisterAccountActivity.this, "Error:"+e, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    }
                    else
                    {
                        Toast.makeText(RegisterAccountActivity.this, "Password Incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        regLogin.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null)
        {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        Intent loginIntent = new Intent(RegisterAccountActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
