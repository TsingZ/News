package com.luqingzheng.news.util;

import com.google.gson.Gson;
import com.luqingzheng.news.gson.NewsList;

public class Utility {
    public static NewsList parseJsonWithGson(final String requestText){
        Gson gson = new Gson();
        return gson.fromJson(requestText,NewsList.class);
    }
}
