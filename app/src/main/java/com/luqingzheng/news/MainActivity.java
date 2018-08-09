package com.luqingzheng.news;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.luqingzheng.news.gson.News;
import com.luqingzheng.news.gson.NewsList;
import com.luqingzheng.news.util.HttpUtil;
import com.luqingzheng.news.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int  ITEM_SOCIETY= 1;
    private static final int  ITEM_COUNTY= 2;
    private static final int  ITEM_INTERNATION= 3;
    private static final int  ITEM_FUN= 4;
    private static final int  ITEM_SPORT= 5;

    private List<Title> titleList = new ArrayList<Title>();
    private ListView listView;
    private TitleAdapter adapter;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("社会新闻");

        refreshLayout = findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        listView = findViewById(R.id.list_view);
        adapter = new TitleAdapter(this,R.layout.list_view_item,titleList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Intent intent = new Intent(MainActivity.this,ContentActivity.class);
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Title title = titleList.get(position);
                intent.putExtra("title",actionBar.getTitle());
                intent.putExtra("uri",title.getUri());
                startActivity(intent);
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_society);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId()){
                    case R.id.nav_society:
                        handleCurrentPage("社会新闻",ITEM_SOCIETY);
                        break;
                    case R.id.nav_county:
                        handleCurrentPage("国内新闻",ITEM_COUNTY);
                        break;
                    case R.id.nav_internation:
                        handleCurrentPage("国际新闻",ITEM_INTERNATION);
                        break;
                    case R.id.nav_fun:
                        handleCurrentPage("娱乐新闻",ITEM_FUN);
                        break;
                    case R.id.nav_sport:
                        handleCurrentPage("体育新闻",ITEM_SPORT);
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                refreshLayout.setRefreshing(true);
                int itemName = parseString((String)actionBar.getTitle());
                requestNew(itemName);
            }
        });
        requestNew(ITEM_SOCIETY);
    }

    private void handleCurrentPage(String text,int item){
        ActionBar actionBar = getSupportActionBar();
        if (!text.equals(actionBar.getTitle().toString())){
            actionBar.setTitle(text);
            requestNew(item);
            refreshLayout.setRefreshing(true);
        }
    }

    public void requestNew(int itemName){
        String address = response(itemName);
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        Toast.makeText(MainActivity.this,"新闻加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseText = response.body().string();
                final NewsList newsList = Utility.parseJsonWithGson(responseText);
                final int code = newsList.code;
                final String msg = newsList.msg;
                if (code == 200){
                    titleList.clear();
                    for (News news : newsList.newsList){
                        Title title = new Title(news.title,news.description,news.picUrl,news.url);
                        titleList.add(title);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            adapter.notifyDataSetChanged();
                            listView.setSelection(0);
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            Toast.makeText(MainActivity.this,"数据返回错误",Toast.LENGTH_SHORT).show();
                            refreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private String response(int itemName)
    {
        String address = "https://api.tianapi.com/social/?key=d521f90f7fe0636fdd92a48716b3d95c&num=50&rand=1";
        switch (itemName) {
            case ITEM_SOCIETY:
                break;
            case ITEM_COUNTY:
                address = address.replaceAll("social", "guonei");
                break;
            case ITEM_INTERNATION:
                address = address.replaceAll("social", "world");
                break;
            case ITEM_FUN:
                address = address.replaceAll("social", "huabian");
                break;
            case ITEM_SPORT:
                address = address.replaceAll("social", "tiyu");
                break;
        }
        return address;
    }

    private int parseString(String text){
        if (text.equals("社会新闻")){
            return ITEM_SOCIETY;
        }
        if (text.equals("国内新闻")){
            return ITEM_COUNTY;
        }
        if (text.equals("国际新闻")){
            return ITEM_INTERNATION;
        }
        if (text.equals("娱乐新闻")){
            return ITEM_FUN;
        }
        if (text.equals("体育新闻")){
            return ITEM_SPORT;
        }
        return ITEM_SOCIETY;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        }else {
            finish();
        }
    }
}




















