package com.example.myblogapp;




public class BlogPosts extends BlogPostId {
    public  String post_desc,user_id,post_url;

    public BlogPosts()
    {

    }

    public BlogPosts(String post_desc, String user_id, String post_url) {
        this.post_desc = post_desc;
        this.user_id = user_id;
        this.post_url = post_url;


    }



    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPost_url() {
        return post_url;
    }

    public void setPost_url(String post_url) {
        this.post_url = post_url;

    }



}
