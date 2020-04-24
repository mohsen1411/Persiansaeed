package com.develope.persiancalendar.view.activity;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.develope.persiancalendar.CalendarTool;
import com.develope.persiancalendar.Constants;
import com.develope.persiancalendar.R;
import com.develope.persiancalendar.adapter.DrawerAdapter;
import com.develope.persiancalendar.service.ApplicationService;
import com.develope.persiancalendar.util.Utils;
import com.develope.persiancalendar.view.fragment.ApplicationPreferenceFragment;
import com.develope.persiancalendar.view.fragment.CalendarFragment;
import com.develope.persiancalendar.view.fragment.CompassFragment;
import com.develope.persiancalendar.view.fragment.ConverterFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.mohsen.persiancalendar.util.UpdateUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends AppCompatActivity {
    private Utils utils;
    private UpdateUtils updateUtils;

    private DrawerLayout drawerLayout;
    private DrawerAdapter adapter;

    CalendarTool calendarTool = new CalendarTool();


    private Class<?>[] fragments = {
            null,
            CalendarFragment.class,
            ConverterFragment.class,
            CompassFragment.class,
            ApplicationPreferenceFragment.class,
         //   AboutFragment.class
    };

    private static final int CALENDAR = 1;
    private static final int CONVERTER = 2;
    private static final int COMPASS = 3;
    private static final int PREFERENCE = 4;
    private static final int ABOUT = 5;
    private static final int EXIT = 6;

    // Default selected fragment
    private static final int DEFAULT = CALENDAR;

    private int menuPosition = 0; // it should be zero otherwise #selectItem won't be called

    private String lastLocale;
    private String lastTheme;




    //-----------------------------------------------------------------------------------------------
    private AdView adView;
    private InterstitialAd interstitialAd;
    private RequestQueue requestQueue;
    private static final String URL  ="https://www.fonisha.com/taghvim.json";
//--------------------------------------------------------------------------------------------------



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        utils = Utils.getInstance(getApplicationContext());
        utils.setTheme(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        utils.changeAppLanguage(this);
        utils.loadLanguageResource();
        lastLocale = utils.getAppLanguage();
        lastTheme = utils.getTheme();
        updateUtils = UpdateUtils.getInstance(getApplicationContext());

        if (!Utils.getInstance(this).isServiceRunning(ApplicationService.class)) {
            startService(new Intent(getBaseContext(), ApplicationService.class));
        }

        updateUtils.update(true);

        setContentView(R.layout.activity_main);


        //-------------------------------------------------------------------------------------------
        initVolley();
//---------------------------------------------------------------------------------------------------------



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            toolbar.setPadding(0, 0, 0, 0);
        }


//        Pushe.initialize(this,true);
        RecyclerView navigation = (RecyclerView) findViewById(R.id.navigation_view);
        navigation.setHasFixedSize(true);
        adapter = new DrawerAdapter(this);
        navigation.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        navigation.setLayoutManager(layoutManager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        final View appMainView = findViewById(R.id.app_main_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            int slidingDirection = +1;

            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (isRTL())
                        slidingDirection = -1;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    slidingAnimation(drawerView, slideOffset);
                }
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            private void slidingAnimation(View drawerView, float slideOffset) {
                appMainView.setTranslationX(slideOffset * drawerView.getWidth() * slidingDirection);
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        selectItem(DEFAULT);

        LocalBroadcastManager.getInstance(this).registerReceiver(dayPassedReceiver,
                new IntentFilter(Constants.LOCAL_INTENT_DAY_PASSED));
        FrameLayout n = (FrameLayout)findViewById(R.id.fragment_holder);
        switch (calendarTool.getIranianMonth()){


            case 1:
            case 2:
            case 3:
                n.setBackgroundResource(R.drawable.spring1);
                break;
            case 4:
            case 5:
            case 6:
                n.setBackgroundResource(R.drawable.summer1);
                break;
            case 7:
            case 8:
            case 9:
                n.setBackgroundResource(R.drawable.autumn1);
                break;
            case 10:
            case 11:
            case 12:
                n.setBackgroundResource(R.drawable.winter1);
                break;


        }

    }

    //-----------------------------------------------------------------------------------------------------
    private void initVolley() {
        requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, URL, null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    int key = response.getInt("key");
                    String appId = response.getString("appid");
                    String bannerId = response.getString("bannerid");
                    String interId = response.getString("interid");

                    initAdmob(key,appId,bannerId,interId);
                    requestQueue.stop();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                requestQueue.stop();

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void initAdmob(int key, String appId, String bannerId, String interId) {

        if(key == 1){

            MobileAds.initialize(getApplicationContext(),appId);

            /*for banner*/
            adView = new AdView(getApplicationContext());
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(bannerId);
            createBanner();
            adView.loadAd(new AdRequest.Builder().build());

            /*for interstitial*/
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(interId);
            interstitialAd.loadAd(new AdRequest.Builder().build());

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(interstitialAd.isLoaded()){
                        interstitialAd.show();
                    }
                }
            }, 5000);
        }

    }

    private void createBanner() {

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        adView.setLayoutParams(params);

        RelativeLayout container = findViewById(R.id.container);
        container.addView(adView);

//        ScrollView about_layoyt = findViewById(R.id.about_layout);
//        about_layoyt.addView(adView);

    }


    //---------------------------------------------------------------------------------------------------------------------





    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isRTL() {
        return getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        utils.changeAppLanguage(this);
        View v = findViewById(R.id.drawer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            v.setLayoutDirection(isRTL() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    public boolean dayIsPassed = false;

    private BroadcastReceiver dayPassedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dayIsPassed = true;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (dayIsPassed) {
            dayIsPassed = false;
            restartActivity();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dayPassedReceiver);
        super.onDestroy();
    }

    public void onClickItem(int position) {
        selectItem(position);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if (menuPosition != DEFAULT) {
            selectItem(DEFAULT);
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Checking for the "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void beforeMenuChange(int position) {
        if (position != menuPosition) {
            // reset app lang on menu changes, ugly hack but it seems is needed
            utils.changeAppLanguage(this);
        }

        // only if we are returning from preferences
        if (menuPosition != PREFERENCE)
            return;

        utils.updateStoredPreference();
        updateUtils.update(true);

        boolean needsActivityRestart = false;

        String locale = utils.getAppLanguage();
        if (!locale.equals(lastLocale)) {
            lastLocale = locale;
            utils.changeAppLanguage(this);
            utils.loadLanguageResource();
            needsActivityRestart = true;
        }

        if (!lastTheme.equals(utils.getTheme())) {
            needsActivityRestart = true;
            lastTheme = utils.getTheme();
        }

        if (needsActivityRestart)
            restartActivity();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void selectItem(int item) {
        if (item == EXIT) {
            finish();
            return;
        }

        if(item == ABOUT){

//            Intent intent = new Intent(Intent.ACTION_EDIT);
//            intent.setData(Uri.parse("bazaar://details?id=" + "com.mohsen.persiancalendar"));
//            intent.setPackage("com.farsitel.bazaar");
//            startActivity(intent);



            try {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }

        }

        beforeMenuChange(item);
        if (menuPosition != item) {
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_holder,
                                (Fragment) fragments[item].newInstance(),
                                fragments[item].getName()
                        ).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            menuPosition = item;
        }

        adapter.setSelectedItem(item);

        drawerLayout.closeDrawers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE)
            LocalBroadcastManager.getInstance(this).sendBroadcast(
                    new Intent(Constants.LOCATION_PERMISSION_RESULT));
    }
}
