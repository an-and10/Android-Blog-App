package com.example.myblogapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragementHome extends Fragment {
    private RecyclerView blog_list_view;
    private List<BlogPosts> blog_lists;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerViewAdapter blogRecyclerViewAdapter;
    private FirebaseAuth firebaseAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageLoaded = true;



    public FragementHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view  = inflater.inflate(R.layout.fragment_fragement_home, container, false);
        blog_lists = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        blogRecyclerViewAdapter = new BlogRecyclerViewAdapter(blog_lists);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerViewAdapter);
        firebaseAuth  =FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null) {

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom =  !recyclerView.canScrollVertically(1);
                    if(reachedBottom)
                    {
                        String post_desc = lastVisible.getString("post_desc");

                        Toast.makeText(container.getContext(), "Reached Bottom :"+post_desc, Toast.LENGTH_SHORT).show();
                        loadMorePost();
                    }
                }
            });

            firebaseFirestore = FirebaseFirestore.getInstance();

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("Time", Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        if (isFirstPageLoaded) {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            blog_lists.clear();

                        }

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {


                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                BlogPosts blogPost = doc.getDocument().toObject(BlogPosts.class).withId(blogPostId);

                                if (isFirstPageLoaded) {

                                    blog_lists.add(blogPost);

                                } else {

                                    blog_lists.add(0, blogPost);

                                }


                                blogRecyclerViewAdapter.notifyDataSetChanged();

                            }
                        }

                        isFirstPageLoaded= false;

                    }

                }

            });

        }



        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost() {
        if (firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("Time", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);
             nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                 @Override
                 public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){

                     lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                     for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                         if (doc.getType() == DocumentChange.Type.ADDED) {

                             String BlogPostId = doc.getDocument().getId();
                             BlogPosts bp = doc.getDocument().toObject(BlogPosts.class).withId(BlogPostId);

                             blog_lists.add(bp);
                             blogRecyclerViewAdapter.notifyDataSetChanged();

                         }
                     }
                 }
             }
            });
        }
    }

}
