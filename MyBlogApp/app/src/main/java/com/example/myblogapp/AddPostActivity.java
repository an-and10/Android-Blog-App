package com.example.myblogapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {
    private static final int MAX_LENGTH =30 ;
    private Toolbar toolbarposts;
    private ImageView newPostImage;
    private EditText add_description;
    private Button add_post_btn;
    private Uri postImageUri =null;
    private ProgressBar progressBaraddpost;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private Bitmap compressedImageFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        toolbarposts  = findViewById(R.id.tollbarforpost);
        setSupportActionBar(toolbarposts);
        getSupportActionBar().setTitle("Add New Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBaraddpost = findViewById(R.id.add_post_progress);
        firebaseAuth = FirebaseAuth.getInstance();

        storageReference =  FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();


        current_user_id = firebaseAuth.getCurrentUser().getUid();
        newPostImage = findViewById(R.id.post_image);
        add_description = findViewById(R.id.add_descritptions);
        add_post_btn = findViewById(R.id.add_post_btn);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(AddPostActivity.this);


            }
        });

        add_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc = add_description.getText().toString();
                if(!TextUtils.isEmpty(desc) && postImageUri!=null)
                {
                    progressBaraddpost.setVisibility(View.VISIBLE);
                    String randomName = UUID.randomUUID().toString();
                   final StorageReference filepath = storageReference.child("PostImages").child(randomName+".jpg");
                   filepath.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                           if(task.isSuccessful())
                           {

                               Date c = Calendar.getInstance().getTime();
                               Toast.makeText(AddPostActivity.this, "Current Add Post Time: "+c, Toast.LENGTH_SHORT).show();

                               SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                               String formattedDate = df.format(c);
                               Toast.makeText(AddPostActivity.this, "Formated Date: "+formattedDate, Toast.LENGTH_SHORT).show();
                               Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
                               while (!urlTask.isSuccessful());
                               Uri downloadUrl = urlTask.getResult();
                               final String downloadUri = String.valueOf(downloadUrl);
                              //String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();
                               Toast.makeText(AddPostActivity.this, "Download Url :"+downloadUri, Toast.LENGTH_LONG).show();

                               Map<String, Object>PostMap = new HashMap<>();
                               PostMap.put("post_url",downloadUri);
                               PostMap.put("post_desc",desc);
                               PostMap.put("user_id",current_user_id);
                               PostMap.put("Time",formattedDate);

       firebaseFirestore.collection("Posts").add(PostMap).
               addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                   @Override
                   public void onComplete(@NonNull Task<DocumentReference> task) {

                       if(task.isSuccessful())
                       {
                           Intent newtoMain = new Intent(AddPostActivity.this,MainActivity.class);
                           startActivity(newtoMain);

                           Toast.makeText(AddPostActivity.this, "New Post Added", Toast.LENGTH_SHORT).show();

                       }else
                       {
                           Toast.makeText(AddPostActivity.this, "error in FireBase Collection", Toast.LENGTH_SHORT).show();
                       }
                       progressBaraddpost.setVisibility(View.INVISIBLE);

                   }
               });




                           }else
                           {
                               progressBaraddpost.setVisibility(View.INVISIBLE);


                           }
                       }
                   });

                }


            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri=  result.getUri();
                newPostImage.setImageURI(postImageUri);



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


}
