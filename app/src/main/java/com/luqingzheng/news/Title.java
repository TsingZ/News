package com.luqingzheng.news;

public class Title {
    private String title;
    private String descr;
    private String imageUrl;
    private String uri;

    public Title(String title,String descr,String imageUrl,String uri){
        this.title = title;
        this.descr = descr;
        this.imageUrl = imageUrl;
        this.uri = uri;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescr()
    {
        return descr;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public String getUri()
    {
        return uri;
    }
}
