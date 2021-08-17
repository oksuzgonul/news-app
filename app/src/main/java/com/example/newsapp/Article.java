package com.example.newsapp;

public class Article {
    private String INITIAL_STRING = "";

    private String mTitle;
    private String mSectionName;
    private String mAuthor = INITIAL_STRING;
    private String mDatePublished = INITIAL_STRING;
    private String mUrl;

    public Article(String title, String sectionName,
                   String author, String datePublished, String url) {
        mTitle = title;
        mSectionName = sectionName;
        mAuthor = author;
        mDatePublished = datePublished;
        mUrl = url;
    }

    public Article(String title, String sectionName, String url) {
        mTitle = title;
        mSectionName = sectionName;
        mUrl = url;
    }


    public String getTitle() {return mTitle;}
    public String getSectionName() {return mSectionName;}
    public String getAuthor() {return mAuthor;}
    public String getDatePublished() {return mDatePublished;}
    public String getUrl() {return mUrl;}
    public Boolean hasAuthor() {return !mAuthor.equals(INITIAL_STRING);}
    public Boolean hasDate() {return !mDatePublished.equals(INITIAL_STRING);}

}