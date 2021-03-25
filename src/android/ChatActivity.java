package com.liveperson.plugin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.os.CountDownTimer;

import com.liveperson.infra.BadArgumentException;
import com.liveperson.infra.CampaignInfo;
import com.liveperson.infra.ConversationViewParams;
import com.liveperson.infra.ICallback;
import com.liveperson.infra.InitLivePersonProperties;
import com.liveperson.infra.auth.LPAuthenticationParams;
import com.liveperson.infra.auth.LPAuthenticationType;
import com.liveperson.infra.callbacks.InitLivePersonCallBack;
import com.liveperson.infra.messaging_ui.fragment.ConversationFragment;
import com.liveperson.messaging.sdk.api.LivePerson;
import com.liveperson.messaging.sdk.api.model.ConsumerProfile;
import com.liveperson.messaging.sdk.api.callbacks.LogoutLivePersonCallback;

import com.liveperson.monitoring.model.EngagementDetails;
import com.liveperson.monitoring.model.LPMonitoringIdentity;
import com.liveperson.monitoring.sdk.MonitoringParams;
import com.liveperson.infra.MonitoringInitParams;
import com.liveperson.infra.model.LPWelcomeMessage;
import com.liveperson.infra.model.MessageOption;
import com.liveperson.monitoring.sdk.api.LivepersonMonitoring;
import com.liveperson.monitoring.sdk.callbacks.EngagementCallback;
import com.liveperson.monitoring.sdk.callbacks.MonitoringErrorType;
import com.liveperson.monitoring.sdk.responses.LPEngagementResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Created by han.nguyen on 20-03-2018.
 * Used as a LivePerson Fragment container.
 */

public class ChatActivity extends AppCompatActivity implements SwipeBackLayout.SwipeBackListener, LivepersonIntentHandler.ChatActivityCallback {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final String LIVEPERSON_FRAGMENT = "liveperson_fragment";
    private ConversationFragment mConversationFragment;
    private static String BrandID = "";
    private static String AppID = "";

    private Menu mMenu;
    String package_name ;
    private DialogHelper mDialogHelper;

    private final SwipeBackLayout.DragEdge DEFAULT_DRAG_EDGE = SwipeBackLayout.DragEdge.LEFT;

    protected AppBarLayout appBar;
    protected Toolbar toolbar;
    protected AppCompatTextView title;
    //Using this field to create swipe right to close child activity
    private SwipeBackLayout swipeBackLayout;
    protected boolean isSwipeBack = true;
    // Intent Handler
    private LivepersonIntentHandler mIntentsHandler;

    CampaignInfo campaign ;
    String partyID;

    public static Activity fa;
    private long startTime = 15 * 60 * 1000; // 15 MINS IDLE TIME
    private final long interval = 1 * 1000;
    private CountDownTimer countDownTimer;

    public static String getBrandID(){
        return BrandID;
    }
    public static String getAppID(){
        return AppID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fa =  this;

        mDialogHelper = new DialogHelper(this);
        package_name = getApplicationContext().getPackageName();
        appBar = findViewById (getApplication().getResources().getIdentifier("appBar", "id", package_name));
        toolbar = findViewById (getApplication().getResources().getIdentifier("toolbar", "id", package_name));
        title = findViewById (getApplication().getResources().getIdentifier("title", "id", package_name));
        int layoutResID = getApplication().getResources().getIdentifier("activity_custom", "layout", package_name);
        setContentView(layoutResID);

        mIntentsHandler = new LivepersonIntentHandler(ChatActivity.this);
        String ChatTitleHeader = "";
        String languageApp = "en-UK";

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            ChatTitleHeader= extras.getString("EXTRA_ChatTitleHeader");
            languageApp= extras.getString("EXTRA_LanguageApp");
            setTitle(ChatTitleHeader);
        } 
        String[] languages= getLanguage(languageApp);
        String lang = languages[0];
        String country = languages[1];


        if (TextUtils.isEmpty(lang)) {
            Log.i(TAG, "createLocale: taking custom locale from edit text.. ");
            createLocale(lang, country);
        } else {
            Log.i(TAG, "createLocale: " + lang + "-null");
            createLocale(lang, null);
        }

        initLivePerson();

        countDownTimer = new CountDownTimer(startTime, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                // TODO: restart counter
                finishChatScreen();
                // LivePerson.checkActiveConversation(new ICallback<Boolean, Exception>() {
                //     @Override
                //     public void onSuccess(Boolean aBoolean) {
                //         if(!aBoolean){
                //             finishChatScreen();
                //         }
                //     }
        
                //     @Override
                //     public void onError(Exception e) {
                //         finishChatScreen();
                //     }
                // });
            }
        };
    }
    @Override
    public void onPause() {
        super.onPause();
        countDownTimer.cancel();            
        countDownTimer.start();
    }
    public void showProgressDialog() {
        mDialogHelper.showProgress();
    }

    public void dismissProgressDialog() { //CreateNewConciergeCase.View::
        mDialogHelper.dismissProgress();
    }


    @Override
    protected void onStart(){
        super.onStart();

       // setUserProfile();
    }


    private void setUserProfile() {
        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            String jsonArray= extras.getString("EXTRA_PROFILE");
            try {
                JSONArray args = new JSONArray(jsonArray);
                final String firstName  = !args.isNull(1) ? args.getString(1) : "";
                final String lastName   = !args.isNull(2) ? args.getString(2) : "";
                final String nickname   = !args.isNull(3) ? args.getString(3) : "";
                final String profileImageUrl   = !args.isNull(4) ? args.getString(4) : "";
                final String phone      = !args.isNull(5) ? args.getString(5) : "";
                final String uid   = !args.isNull(6) ? args.getString(6) : "";
                final String employeeId   = !args.isNull(7) ? args.getString(7) : "";

                ConsumerProfile consumerProfile = new ConsumerProfile.Builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setPhoneNumber(phone)
                .setNickname(nickname)
                .build();

                LivePerson.setUserProfile(consumerProfile);
            } catch (JSONException e) {
              
            }
            
        }
        
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initFragment(CampaignInfo campaignInfo) {
        dismissProgressDialog();
      //  setUserProfile();
        mConversationFragment = (ConversationFragment)getSupportFragmentManager().findFragmentByTag(LIVEPERSON_FRAGMENT);
        Log.d(TAG, "initFragment. mConversationFragment = " + mConversationFragment);
        if (mConversationFragment == null) {

            String authCode = "";
            String publicKey = "";
            String WelcomeMsg = "How can I help you today?";
            String ButtonOpt1Msg = "";
            String ButtonOpt1Value = "";
            String ButtonOpt2Msg = "";
            String ButtonOpt2Value = "";

            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                authCode= extras.getString("EXTRA_AUTHENTICATE");
                WelcomeMsg= extras.getString("EXTRA_WelcomeMsg");
                ButtonOpt1Msg= extras.getString("EXTRA_ButtonOpt1Msg");
                ButtonOpt1Value= extras.getString("EXTRA_ButtonOpt1Value");
                ButtonOpt2Msg= extras.getString("EXTRA_ButtonOpt2Msg");
                ButtonOpt2Value= extras.getString("EXTRA_ButtonOpt2Value");
            }
            Log.d(TAG, "initFragment. authCode = " + authCode);
            LPAuthenticationParams authParams = new LPAuthenticationParams();
            // add new
            ConversationViewParams conversationViewParams = new ConversationViewParams(false);

            Log.d(TAG, "initFragment. publicKey = " + campaignInfo);

            LPWelcomeMessage lpWelcomeMessage = new LPWelcomeMessage(WelcomeMsg);
            
            lpWelcomeMessage.setMessageFrequency(LPWelcomeMessage.MessageFrequency.EVERY_CONVERSATION);
            conversationViewParams.setLpWelcomeMessage(lpWelcomeMessage);

            if(campaignInfo!=null){
                conversationViewParams.setCampaignInfo(campaignInfo);
            }
            
            authParams.setHostAppJWT(authCode);
            //authParams.addCertificatePinningKey(publicKey);
            //LivePerson.showConversation(this, authParams,conversationViewParams);
            mConversationFragment = (ConversationFragment) LivePerson.getConversationFragment(authParams, conversationViewParams);

            if (isValidState()) {

               FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                if (mConversationFragment != null) {
                    ft.add(getResources().getIdentifier("custom_fragment_container", "id", getPackageName()), mConversationFragment,
                            LIVEPERSON_FRAGMENT).commitAllowingStateLoss();
                } else {

                }

            }
        } else {
            attachFragment();
        }
    }

    public void initLivePerson() {
        Log.d("HAN_NGUYEN", "initLivePerson: ");
        showProgressDialog();
        Bundle extras = getIntent().getExtras();
        String newAPP;
        String newID;
        String AppInstall;
        if(extras != null) {
            newAPP= extras.getString("EXTRA_APPID");
            BrandID = newAPP;
            newID = extras.getString("EXTRA_APPIDENTIFIER");
            AppInstall = extras.getString("EXTRA_AppInstallationID");
            if(newID != null){
                 AppID = newID;
            }

            MonitoringInitParams monitoringParams = new MonitoringInitParams(AppInstall);
            LivePerson.initialize(getApplicationContext(), new InitLivePersonProperties(BrandID, AppID, monitoringParams, new InitLivePersonCallBack() {

                @Override
                public void onInitSucceed() {
                    Log.i("HAN_NGUYEN", "Liverperson SDK Initialized" + LivePerson.getSDKVersion());
                    setUserProfile();
                    FCMUtils.handleGCMRegistration(ChatActivity.this);
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initEngagementAttributes();
                        }
                    });
                }

                @Override
                public void onInitFailed(Exception e) {
                    Log.e("HAN_NGUYEN", "Liverperson SDK Initialization Failed : " + e.getMessage());
                }
            }));
        }
    }
    public void initEngagementAttributes(){
        

        Bundle extras = getIntent().getExtras();
        String engagementAtt = "";
        String entrypoint = "";
        if(extras != null) {
            engagementAtt= extras.getString("EXTRA_ENGAGEMENT");
            entrypoint= extras.getString("EXTRA_ENTRYPOINT");
        }   
         
        JSONArray entryPoints = null;
        try {
            entryPoints = new JSONArray(entrypoint);
        } catch (JSONException e) {
            Log.e(TAG, "Error Creating Entry Points :: " + e);
        }

        JSONArray engagementAttributes = null;
        try {
            engagementAttributes = new JSONArray(engagementAtt);
        } catch (JSONException e) {
            Log.e(TAG, "Error Creating Engagement Attr :: " + e);
        }
        MonitoringParams params = new MonitoringParams("PageId", entryPoints, engagementAttributes);
        LPMonitoringIdentity identity = new  LPMonitoringIdentity(null,"");

        // Get Engagement
        LivepersonMonitoring.getEngagement(getApplicationContext(), Arrays.asList(identity), params, new EngagementCallback() {
            @Override
            public void onSuccess(LPEngagementResponse lpEngagementResponse) {
                List<EngagementDetails> engagementList = lpEngagementResponse.getEngagementDetailsList();
                // Check if User qualifies for an Engagement
                if (engagementList != null && !engagementList.isEmpty()) {
                    // Set Campaign ID
                    String currentCampaignId = engagementList.get(0).getCampaignId();
                    // Set Engagement ID
                    String currentEngagementId = engagementList.get(0).getEngagementId();
                    // Set Engagement Context Id
                    String currentEngagementContextId = engagementList.get(0).getContextId();
                    // Set Session ID
                    String currentSessionId = lpEngagementResponse.getSessionId();
                    // Set Visitor ID
                    String currentVisitorId = lpEngagementResponse.getVisitorId();
                    // Try-Catch Block
                    try {
                        // Create Campaign Object
                        CampaignInfo campaign = new CampaignInfo(Long.valueOf(currentCampaignId), Long.valueOf(currentEngagementId),
                                currentEngagementContextId, currentSessionId, currentVisitorId);
                        initFragment(campaign);
                    } catch (Exception  e){
                        initFragment(null);
                    }
                } else {
                    // Log Error
                    initFragment(null);
                }
            }

            @Override
            public void onError(MonitoringErrorType monitoringErrorType, Exception e) {
                initFragment(null);
            }
        });
    }

    private boolean isValidState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !isFinishing() && !isDestroyed();
        } else {
            return !isFinishing();
        }
    }

    private void attachFragment() {
        if (mConversationFragment.isDetached()) {
            Log.d(TAG, "initFragment. attaching fragment");
            if (isValidState()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.attach(mConversationFragment).commitAllowingStateLoss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConversationFragment != null) {
            attachFragment();
        }
    }


    @Override
    public void onBackPressed() {
        if (mConversationFragment == null || !mConversationFragment.onBackPressed()) {

            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String RevolvedTileMsg = "";
        String ClearTitleMsg = "";

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            RevolvedTileMsg= extras.getString("EXTRA_RevolvedTileMsg");
            ClearTitleMsg= extras.getString("EXTRA_ClearTitleMsg");
        }
        getMenuInflater().inflate(getApplication().getResources().getIdentifier("menu_chat", "menu", package_name), menu);
        MenuItem menuItem1 = menu.getItem(0);
        menuItem1.setTitle(RevolvedTileMsg);
        MenuItem menuItem2 = menu.getItem(1);
        menuItem2.setTitle(ClearTitleMsg);
        mMenu = menu;
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIntentsHandler.getIsConversationActive()) {
            menu.setGroupEnabled(getResources().getIdentifier("grp_urgent", "id", getPackageName()), true);
        } else {
            menu.setGroupEnabled(getResources().getIdentifier("grp_urgent", "id", getPackageName()), false);
        }


        if (mIntentsHandler.getIsCsatLaunched()) {
            menu.setGroupEnabled(getResources().getIdentifier("grp_clear", "id", getPackageName()), false);
        }
        return true;
    }


    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {

    }

    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (isSwipeBack) {
            super.setContentView(getContainer());
            View view = LayoutInflater.from(this).inflate(layoutResID, null);
            swipeBackLayout.addView(view);
        } else {
            super.setContentView(layoutResID);
        }
        setup();
    }

    private View getContainer() {
        RelativeLayout container = new RelativeLayout(this);
        swipeBackLayout = new SwipeBackLayout(this);
        swipeBackLayout.setDragEdge(DEFAULT_DRAG_EDGE);
        swipeBackLayout.setOnSwipeBackListener(this);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                        , RelativeLayout.LayoutParams.MATCH_PARENT);
        container.addView(swipeBackLayout);
        return container;
    }


    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setup();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setup();
    }

    void setup() {
        if (toolbar == null) {
            toolbar = findViewById (getApplication().getResources().getIdentifier("toolbar", "id", package_name));
        }
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);
        toolbar.setContentInsetEndWithActions(0);
        if (appBar == null) {
            appBar = findViewById (getApplication().getResources().getIdentifier("appBar", "id", package_name));
        }
        if (title == null) {
            title = findViewById (getApplication().getResources().getIdentifier("title", "id", package_name));
        }
        setSupportActionBar(toolbar);
        Drawable icBack = ContextCompat.getDrawable(this, getApplication().getResources().getIdentifier("ic_baseline_close", "drawable", package_name));
        menuTintColors(this, icBack);
        getSupportActionBar().setHomeAsUpIndicator(icBack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void menuTintColors(Context context, Drawable... drawables) {
        ColorStateList colorStateList = ContextCompat.getColorStateList(context,
                getApplication().getResources().getIdentifier("ic_menu_tint_color", "color", package_name));
        for (Drawable icMenu : drawables) {
            DrawableCompat.setTintList(icMenu, colorStateList);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        title.setText(getString(titleId));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String ClearConversationMsg = "";
        String ClearConfirmMsg = "";
        String ChooseMsg = "";
        String RevolvedTileMsg = "";
        String ResolvedConfirmMsg = "";
        String ClearTitleMsg = "";
        String YesMsg = "";
        String CancelMsg = "";
        String ClearMsg = "";

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            ClearConversationMsg= extras.getString("EXTRA_ClearConversationMsg");
            ClearConfirmMsg= extras.getString("EXTRA_ClearConfirmMsg");
            ChooseMsg= extras.getString("EXTRA_ChooseMsg");
            RevolvedTileMsg= extras.getString("EXTRA_RevolvedTileMsg");
            ResolvedConfirmMsg= extras.getString("EXTRA_ResolvedConfirmMsg");
            ClearTitleMsg= extras.getString("EXTRA_ClearTitleMsg");
            YesMsg= extras.getString("EXTRA_YesMsg");
            CancelMsg= extras.getString("EXTRA_CancelMsg");
            ClearMsg= extras.getString("EXTRA_ClearMsg");
        }
        final String clearm = ClearTitleMsg;
        final String clearc = ClearConfirmMsg;
        if(id == getApplication().getResources().getIdentifier("clear_history", "id", package_name)) {
            // check if the history is resolved,if not skip the clear command and notify the user.
            mDialogHelper.action(ClearTitleMsg,
            ClearConversationMsg,
            ClearMsg, CancelMsg,
                    (dialog, which) -> {
                        LivePerson.checkActiveConversation(new ICallback<Boolean, Exception>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                if (!aBoolean) {
                                    //clear history only from device
                                    LivePerson.clearHistory();
                                } else {
                                    mDialogHelper.alert(clearm, clearc);
                                }

                            }

                            @Override
                            public void onError(Exception e) {
//                                    Log.e(TAG, e.getMessage());
                            }
                        });

                    });
        } else if(id == getApplication().getResources().getIdentifier("mark_as_resolved", "id", package_name)){
            mDialogHelper.action(RevolvedTileMsg,
            ResolvedConfirmMsg,
            YesMsg, CancelMsg,
                    (dialog, which) -> {
                        LivePerson.resolveConversation();
                    });
        } else {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finishChatScreen() {
        finish();
    }

    @Override
    public void reconectChat() {
        String authCode = "";

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            authCode= extras.getString("EXTRA_AUTHENTICATE");
            //LivePerson.reconnect(new LPAuthenticationParams().setHostAppJWT(authCode));
        }
        
    }

    @Override
    public void setAgentName(String agentName) {
        setTitle(agentName);
    }

    @Override
    public void closeOptionMenu() {
        if (mMenu != null)
            mMenu.close();
    }
    private String[] getLanguage(String key){
        Map<String, String[]> map = new HashMap<>();
         map.put("en-UK", new String[]{"en","English"});
         map.put("ko-KR", new String[]{"ko","Korean"});
         map.put("zh-TW", new String[]{"zh-TW","Taiwan"});
         map.put("ja-JP", new String[]{"ja","Japanese"});
         map.put("zh-HK", new String[]{"zh-HK","Hong kong"});
        String[] result = map.get(key);
        return result;
     }
     protected void createLocale(String language, @Nullable String country) {
        Resources resources = getBaseContext().getResources();
        Configuration configuration = resources.getConfiguration();
        Locale customLocale;

        if (TextUtils.isEmpty(language)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                language = resources.getConfiguration().getLocales().get(0).getCountry();
            } else {
                language = resources.getConfiguration().locale.getCountry();
            }
        }

        if (TextUtils.isEmpty(country)) {
            String[] separated = language.split("-");
            if(separated.length>1){
                customLocale = new Locale(separated[0],separated[1]);
            }else{
                customLocale = new Locale(language);
            }
            
        } else {
            customLocale = new Locale(language, country);
        }
        Locale.setDefault(customLocale);

        configuration.setLocale(customLocale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        Locale locale = getLocale();
        Log.d(TAG, "country = " + locale.getCountry() + ", language = " + locale.getLanguage());
    }

    private Locale getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getBaseContext().getResources().getConfiguration().getLocales().get(0);
        } else {
            return getBaseContext().getResources().getConfiguration().locale;
        }
    }
    @Override
    public void onUserInteraction(){
        super.onUserInteraction();

        //Reset the timer on user interaction...
        countDownTimer.cancel();            
        countDownTimer.start();
    }   
    
}
