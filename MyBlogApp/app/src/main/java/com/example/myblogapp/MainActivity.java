package com.example.myblogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    private Toolbar maintool;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton floatingActionButton;
    private String current_user_id;
    private Fragment homeFragment;
    private Fragment notificationFragment;
    private  Fragment accountFragment;
    private BottomNavigationView bottomNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth  = FirebaseAuth.getInstance();
        Toast.makeText(this, "Home Page", Toast.LENGTH_SHORT).show();


        if(mAuth.getCurrentUser()!=null) {


            maintool = findViewById(R.id.main_toolbar);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);

            floatingActionButton = findViewById(R.id.add_post);

            setSupportActionBar(maintool);
            getSupportActionBar().setTitle("My Blog ");
            firebaseFirestore = FirebaseFirestore.getInstance();
            homeFragment = new FragementHome();
            notificationFragment = new FragmentNotification();
            accountFragment = new FragmentAccount();
            initializeFragment();

            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent AddPost = new Intent(MainActivity.this, AddPostActivity.class);
                    startActivity(AddPost);


                }
            });

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame);

                    switch (menuItem.getItemId()) {

                        case R.id.home:

                            replaceFragment(homeFragment, currentFragment);
                            return true;

                        case R.id.settings:

                            replaceFragment(accountFragment, currentFragment);
                            return true;

                        case R.id.notifications:

                            replaceFragment(notificationFragment, currentFragment);
                            return true;

                        default:
                            return false;


                    }

                }

            });


        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null)
        {
            sendToLogin();

        }else
        {
            current_user_id= mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            Intent sendToSetup = new Intent(MainActivity.this,AccountSetUpActivity.class);
                            startActivity(sendToSetup);
                            finish();
                        }
                    }else
                    {
                        Toast.makeText(MainActivity.this, "Error in Main Activity", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

        private void initializeFragment() {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.add(R.id.frame, homeFragment);
            fragmentTransaction.add(R.id.frame, notificationFragment);
            fragmentTransaction.add(R.id.frame, accountFragment);

            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(accountFragment);

            fragmentTransaction.commit();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.logout:
                mAuth.signOut();
                sendToLogin();
                return true;

            case R.id.account_settings:
                Intent i = new Intent(MainActivity.this,AccountSetUpActivity.class);
                startActivity(i);
                return true;

            default  : return false;
        }


    }
    private void sendToLogin() {
        Intent LoginIntent = new Intent(this,LoginActivity.class);
        startActivity(LoginIntent);
        finish();
    }

    private void  replaceFragment(Fragment fragment,Fragment currentFragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if(fragment == homeFragment){

            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if(fragment == accountFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);

        }

        if(fragment == notificationFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);

        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }

}
