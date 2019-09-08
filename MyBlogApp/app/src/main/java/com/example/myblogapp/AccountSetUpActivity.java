package com.example.myblogapp;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


public class AccountSetUpActivity extends AppCompatActivity {
    Toolbar setupToolbar;
   ImageView profile;
   private   String user_id;
    private Uri mainImageUri=null;
   private EditText username;
   private Button update;
   private StorageReference storagereference;
   private FirebaseAuth firebaseAuth;
   ProgressBar setupprogress;
   private boolean isChanged =false;

   private FirebaseFirestore firebaseFirestore;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_set_up);
        setupToolbar = findViewById(R.id.setuptoolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Settings");
        firebaseFirestore = FirebaseFirestore.getInstance();



        profile =findViewById(R.id.setupimage);
        username = findViewById(R.id.user_name);
        update = findViewById(R.id.updat_btn);
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();

        setupprogress = findViewById(R.id.setupprogress);

        storagereference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        Toast.makeText(AccountSetUpActivity.this, "Data Exists", Toast.LENGTH_SHORT).show();
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageUri = Uri.parse(image);
                        username.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.profile);
                        Toast.makeText(AccountSetUpActivity.this, "Image Url"+image, Toast.LENGTH_SHORT).show();

                        Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).load(image).into(profile);



                    }
                }else
                {
                    Toast.makeText(AccountSetUpActivity.this, "FireBase Retrive Data Error:", Toast.LENGTH_SHORT).show();
                }
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_name = username.getText().toString();
                if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {

                    if (isChanged) {


                        setupprogress.setVisibility(View.VISIBLE);
                        user_id = firebaseAuth.getCurrentUser().getUid();
                        Toast.makeText(AccountSetUpActivity.this, "Current User Id:" + user_id, Toast.LENGTH_SHORT).show();
                        StorageReference image_path = storagereference.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeFireStore(task, user_name);
                                } else {
                                    String e = task.getException().getMessage();
                                    Toast.makeText(AccountSetUpActivity.this, "Image Error:" + e, Toast.LENGTH_SHORT).show();
                                }
                                setupprogress.setVisibility(View.INVISIBLE);


                            }
                        });
                    }
                    else {
                        storeFireStore(null, user_name);
                    }
                }
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
                {
                    if(ContextCompat.checkSelfPermission(AccountSetUpActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

                    {
                        Toast.makeText(AccountSetUpActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(AccountSetUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else
                    {
                        BringImagePicker();
                    }

                }else
                {
                    BringImagePicker();
                }

            }
        });

    }

    private void storeFireStore(Task<UploadTask.TaskSnapshot> task,String user_name) {
        Uri download_uri;
        if(task !=null)
        {
//           download_uri= task.getResult().getUploadSessionUri();
            Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
            while (!urlTask.isSuccessful());
             download_uri = urlTask.getResult();

        }else
        {
            download_uri = mainImageUri;
        }
        //Toast.makeText(MyProfile.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();


        Map<String,String>UserMap = new HashMap<>();
        UserMap.put("name",user_name);
        UserMap.put("image",download_uri.toString());
        firebaseFirestore.collection("Users").document(user_id).set(UserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(AccountSetUpActivity.this, "Account Updatred", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(AccountSetUpActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }else
                {
                    String e = task.getException().getMessage();
                    Toast.makeText(AccountSetUpActivity.this, "FireStore Error:"+e, Toast.LENGTH_SHORT).show();
                }
            }
        });




        Toast.makeText(AccountSetUpActivity.this, "Image is Uploaded", Toast.LENGTH_SHORT).show();
        // Toast.makeText(AccountSetUpActivity.this, "", Toast.LENGTH_SHORT).show();
        // Toast.makeText(AccountSetUpActivity.this, "Image And Data Uploaded Successfully", Toast.LENGTH_SHORT).show();

    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(AccountSetUpActivity.this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                profile.setImageURI(mainImageUri);
                isChanged=  true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
