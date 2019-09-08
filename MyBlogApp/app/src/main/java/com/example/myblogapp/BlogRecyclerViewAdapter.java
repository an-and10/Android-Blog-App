package com.example.myblogapp;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogRecyclerViewAdapter extends RecyclerView.Adapter<BlogRecyclerViewAdapter.ViewHolder> {

    private List<BlogPosts> blog_post_list;
    private Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public  BlogRecyclerViewAdapter(List<BlogPosts> blog_lists)
    {
        this.blog_post_list = blog_lists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_items,viewGroup, false);
        firebaseFirestore =FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final String blogPostId = blog_post_list.get(i).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();



        String desc_data = blog_post_list.get(i).getPost_desc();
        viewHolder.setDesc(desc_data);

        String imageDownloadUri = blog_post_list.get(i).getPost_url();
        //Toast.makeText(context, "Url post Image: "+ imageDownloadUri, Toast.LENGTH_LONG).show();
        viewHolder.setBlogImage(imageDownloadUri);


        final String user_id = blog_post_list.get(i).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                   // Toast.makeText(context, "Enter into Task", Toast.LENGTH_SHORT).show();
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                   // Toast.makeText(context, "User Name:"+userName, Toast.LENGTH_SHORT).show();
                   // Toast.makeText(context, "Profile User Image Url: "+userImage, Toast.LENGTH_SHORT).show();
                    viewHolder.setUserData(userName, userImage);

                } else {

                    //Firebase Exception

                }



            }
        });



//            long millisecond = blog_post_list.get(i).gettime().getTime();
//            Toast.makeText(context, "Time: "+millisecond, Toast.LENGTH_SHORT).show();
//            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
//            viewHolder.setTime(dateString);
//        } catch (Exception e) {
//
//            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
//
//        }

        // Likes Button Action//

        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    viewHolder.updateLikesCount(count);

                } else {

                    viewHolder.updateLikesCount(0);

                }

            }
        });


        //Get Likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.likesicons));

                } else {

                    viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.unlikesicons));

                }

            }
        });

        //Likes Feature
        viewHolder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();

                        }

                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return blog_post_list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView desc;
        private ImageView blogImageView;

        private TextView blogDate;

        private TextView blogUserName;
        private ImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
           // blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);

        }
        public  void setDesc(String textView)
        {
            desc = mView.findViewById(R.id.blog_post_descriptions);
            desc.setText(textView);

        }
        public  void setBlogImage(String downloadUri)
        {
            blogImageView = mView.findViewById(R.id.blog_image_post);
           // Toast.makeText(context, "Setting Post Image", Toast.LENGTH_SHORT).show();
            Glide.with(context).load(downloadUri).into(blogImageView);

        }

        public void setUserData(String userName, String userImage) {


                blogUserImage = mView.findViewById(R.id.user_profile);
                blogUserName = mView.findViewById(R.id.blog_username);


                 blogUserName.setText(userName);
          //  Picasso.with(context).load(userImage).fit().into(blogImageView);
            //Toast.makeText(context, "Uri: "+Uri.parse(userImage), Toast.LENGTH_SHORT).show();
            Glide.with(context).load(userImage).into(blogUserImage);

//                RequestOptions placeholderOption = new RequestOptions();
//                placeholderOption.placeholder(R.drawable.profile_placeholder);



            }


        public void setTime(String date) {

            blogDate = mView.findViewById(R.id.blog_postdate);
            blogDate.setText(date);

        }

        public void updateLikesCount(int count){

            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");

        }

    }


}
