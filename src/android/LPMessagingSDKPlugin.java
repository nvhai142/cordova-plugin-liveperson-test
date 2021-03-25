package com.liveperson.plugin;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.liveperson.api.LivePersonCallbackImpl;
import com.liveperson.api.sdk.LPConversationData;
import com.liveperson.infra.InitLivePersonProperties;
import com.liveperson.infra.MonitoringInitParams;
import com.liveperson.infra.callbacks.InitLivePersonCallBack;
import com.liveperson.messaging.TaskType;
import com.liveperson.messaging.model.AgentData;
import com.liveperson.messaging.sdk.api.LivePerson;
import com.liveperson.messaging.sdk.api.callbacks.LogoutLivePersonCallback;
import com.liveperson.messaging.sdk.api.model.ConsumerProfile;
import com.liveperson.infra.auth.LPAuthenticationParams;
import com.liveperson.infra.auth.LPAuthenticationType;
import com.liveperson.infra.ICallback;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LPMessagingSDKPlugin extends CordovaPlugin {

    private static final String TAG = LPMessagingSDKPlugin.class.getSimpleName();
    public String LP_APP_PACKAGE_NAME;
    //String AppID = "com.outsystemsenterprise.aspirelifestylesdev.CosmoMobileApp";

    private static final String INIT = "lp_sdk_init";
    private static final String START_CONVERSATION = "start_lp_conversation";
    private static final String CLOSE_CONVERSATION_SCREEN = "close_conversation_screen";
    private static final String SET_USER = "set_lp_user_profile";

    private static final String CLEAR_HISTORY_AND_LOGOUT = "lp_clear_history_and_logout";

    private static final String RECONNECT_WITH_NEW_TOKEN = "reconnect_with_new_token";
    public static final String LP_ACCOUNT_ID = "lp_account_id";
    public static final String LP_REGISTER_PUSHER = "register_pusher";

    private static final String LP_REGISTER_GLOBAL_ASYNC_EVENT_CALLBACK = "lp_register_event_callback";

    CallbackContext mCallbackContext;
    CallbackContext mGlobalCallbackContext;
    CallbackContext mRegisterLpPusherCallbackContext;
    
    JSONArray uProfile;
    //String AppIdentifier;
    private String AppInstallationID; 

    private CordovaWebView mainWebView;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mainWebView = webView;
    }

    public static String getAppPackageName(Context context) {
        String currentPackageName = context.getPackageName();
        Log.v(TAG, "LPMessagingSDK.getAppPackageName : currentPackageName" + currentPackageName);
        return currentPackageName;
    }

    /**
     * Liveperson messaging API calls from the JS
     * @param action - supported action: lp_sdk_init, start_lp_conversation
     * @param args - using arg(0) for the account Id
     * @param callbackContext - callback to the JS
     * @return
     * @throws JSONException
     */
    public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        Log.v(TAG, "LPMessagingSDK.execute:" + action);
        LP_APP_PACKAGE_NAME = getAppPackageName(cordova.getActivity());
        Log.v(TAG, "LPMessagingSDK.LP_APP_PACKAGE_NAME:" + LP_APP_PACKAGE_NAME);
        Log.v(TAG, "LPMessagingSDK.VERSION:" + LivePerson.getSDKVersion());


        boolean success = true;


        switch (action){
            case LP_REGISTER_GLOBAL_ASYNC_EVENT_CALLBACK:
                mGlobalCallbackContext = callbackContext;
                JSONObject eventJson = new JSONObject();
                eventJson.put("eventName","lp_register_global_async_event_callback");

                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT, "lp_register_global_async_event_callback");
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
                break;
            case INIT:
                //mCallbackContext = callbackContext;
                // lp_sdk_init - Call this action inorder to do Messaging SDK init
                final String accountId = args.getString(0);
                
                final String AppId = args.getString(1);
                final String AppInstall = args.getString(2);

                AppInstallationID = AppInstall;
                
                Log.d(TAG, "Messaging SDK: init for account Id: " + accountId);
                Log.v(TAG, "Messaging SDK VERSION:" + LivePerson.getSDKVersion());
                initSDK(accountId,AppId,callbackContext);
                break;
            case CLOSE_CONVERSATION_SCREEN:
                mCallbackContext = callbackContext;

                // LivePerson.checkActiveConversation(new ICallback<Boolean, Exception>() {
                //     @Override
                //     public void onSuccess(Boolean aBoolean) {
                //         LivePerson.resolveConversation();
                //     }
        
                //     @Override
                //     public void onError(Exception e) {
                //     }
                // });

                ChatActivity.fa.finish();

                Log.d(TAG, CLOSE_CONVERSATION_SCREEN+ " LPMessagingSDKConversationScreenClosed " + args);
                JSONObject jsonCloseConversation = new JSONObject();
                try {
                    jsonCloseConversation.putOpt("eventName","LPMessagingSDKConversationScreenClosed");
                    mCallbackContext.success(jsonCloseConversation.toString());
                } catch (JSONException e) {
                   
                }
                break;
            case CLEAR_HISTORY_AND_LOGOUT:
                mCallbackContext = callbackContext;
                //final String accountId = args.getString(0);
                LivePerson.logOut(cordova.getActivity(), args.getString(0), LP_APP_PACKAGE_NAME, new LogoutLivePersonCallback() {
                    @Override
                    public void onLogoutSucceed() {
                        JSONObject json = new JSONObject();
                        try {
                            json.putOpt("eventName","LPMessagingSDKClearHistoryAndLogout");
                            mCallbackContext.success(json.toString());
                        } catch (JSONException e) {
                           
                        }

                    }

                    @Override
                    public void onLogoutFailed() {
                        JSONObject json = new JSONObject();
                        try {
                            json.putOpt("eventName","LPMessagingSDKClearHistoryAndLogout");
                            mCallbackContext.error(json.toString());
                        } catch (JSONException e) {
                            
                        }

                    }
                });

                break;
            case START_CONVERSATION:
                mCallbackContext = callbackContext;
                String appIDs = args.getString(0);
                String engagement = "[\n" +
                "        {\n" +
                "        \"type\": \"personal\",\n" +
                "        \"personal\": {\n" +
                "        \"gender\": \""+args.getString(20)+"\",\n" +
                "        \"company\": \""+args.getString(21)+"\",\n" +
                "        \"language\": \""+args.getString(5)+"\",\n" +
                "        \"firstname\": \""+args.getString(12)+"\",\n" +
                "        \"lastname\": \""+args.getString(13)+"\",\n" +
                "        \"age\": {\n" +
                "        \"age\": \""+args.getString(14)+"\",\n" +
                "        \"year\": \""+args.getString(15)+"\",\n" +
                "        \"month\": \""+args.getString(16)+"\",\n" +
                "        \"day\": \""+args.getString(17)+"\"\n" +
                "        },\n" +
                "        \"contacts\": [\n" +
                "        {\n" +
                "        \"email\": \""+args.getString(18)+"\",\n" +
                "        \"phone\": \""+args.getString(19)+"\",\n" +
                "        \"address\": {\n" +
                "        \"country\": \""+args.getString(3)+"\",\n" +
                "        \"region\": \""+args.getString(4)+"\"\n" +
                "        }\n" +
                "        }\n" +
                "        ]\n" +
                "        }\n" +
                "        },\n" +
                "        {\n" +
                "        \"info\": {\n" +
                "        \"storeZipCode\": \""+args.getString(6)+"\",\n" +
                "        \"accountName\": \""+args.getString(7)+"\",\n" +
                "        \"customerId\": \""+args.getString(8)+"\",\n" +
                "        \"storeNumber\": \""+args.getString(10)+"\",\n" +
                "        \"ctype\": \""+args.getString(9)+"\",\n" +
                "        \"userName\": \""+args.getString(22)+"\"\n" +
                "        },\n" +
                "        \"type\": \"ctmrinfo\"\n" +
                "        }\n" +
                "        ]";
                
                String entrypoint = "[\"http://www.liveperson-test.com\",\n" +
                    "                   \""+args.getString(11)+"\",\n" +
                    "                   \"lang://Eng\"]";

                if(!args.isNull(1)) {
                    Log.d(TAG, "Messaging SDK:  startAuthenticatedConversation");
                    String jwt = args.getString(1);
                    String partyID = args.getString(2);
                    String AppIdentifier = args.getString(23);

                    String WelcomeMsg = args.getString(24);
                    String ChatTitleHeader = args.getString(25);
                    String ClearConversationMsg = args.getString(26);
                    String ClearConfirmMsg = args.getString(27);
                    String ChooseMsg = args.getString(28);
                    String RevolvedTileMsg = args.getString(29);
                    String ResolvedConfirmMsg = args.getString(30);
                    String ClearTitleMsg = args.getString(31);
                    String YesMsg = args.getString(32);
                    String CancelMsg = args.getString(33);
                    String ClearMsg = args.getString(34);
                    String MenuMsg = args.getString(35);

                    String ButtonOpt1Msg = args.getString(36);
                    String ButtonOpt1Value = args.getString(37);
                    String ButtonOpt2Msg = args.getString(38);
                    String ButtonOpt2Value = args.getString(39);

                    String languageApp = args.getString(40);

                    startAuthenticatedConversation(appIDs,jwt,partyID,engagement,entrypoint,AppIdentifier,WelcomeMsg,ChatTitleHeader,ClearConversationMsg,ClearConfirmMsg,ChooseMsg,RevolvedTileMsg,ResolvedConfirmMsg,ClearTitleMsg,YesMsg,CancelMsg,ClearMsg,MenuMsg,ButtonOpt1Msg,ButtonOpt1Value,ButtonOpt2Msg,ButtonOpt2Value,languageApp);
                } else {
                    Log.d(TAG, "Messaging SDK: Start conversation");
                    String partyID = args.getString(2);
                    startConversation(appIDs,partyID,engagement,entrypoint);
                }

                break;
            case RECONNECT_WITH_NEW_TOKEN:
                mCallbackContext = callbackContext;
                Log.d(TAG, "Messaging SDK: RECONNECT_WITH_NEW_TOKEN"+args.getString(0));
                reconnect(args.getString(0));
                break;
            case SET_USER:
                mCallbackContext = callbackContext;
                Log.d(TAG, "Messaging SDK: Set User, args:" + args);
                setProfile(callbackContext, args);
                break;
            case LP_REGISTER_PUSHER:

                mRegisterLpPusherCallbackContext = callbackContext;
                final String account = args.getString(0);
                final String token = args.getString(1);
                Log.d(TAG, "@@@ Android ...LPMessaging SDK: register_pusher for  account: " + account +", token: " + token + " LP_APP_PACKAGE_NAME : "+LP_APP_PACKAGE_NAME);
                LivePerson.registerLPPusher(account, LP_APP_PACKAGE_NAME, token);
                JSONObject json = new JSONObject();
                try {
                    json.put("eventName","LPMessagingSDKRegisterLpPusher");
                    json.put("deviceToken",token);

                } catch (JSONException e) {
                    
                }
                PluginResult resultLpPusher = new PluginResult(PluginResult.Status.OK, json.toString());
                resultLpPusher.setKeepCallback(true);
                mRegisterLpPusherCallbackContext.sendPluginResult(resultLpPusher);

                break;
            default:
                mCallbackContext = callbackContext;
                Log.d(TAG,"LPMessagingSDK - lpMessagingSDK." + action + " is not a supported function.");
                callbackContext.error("LivePerson." + action + " is not a supported function.");
                success = false;
                break;
        }

        return success;
    }

    /**
     *
     * @param accountId
     */
    private void initSDK(final String accountId,final String AppID,org.apache.cordova.CallbackContext cb) {
            final org.apache.cordova.CallbackContext callbackContext = cb;

//            cordova.getActivity().setTitle("CHAT");
                LivePerson.shutDown();
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    MonitoringInitParams monitoringParams = new MonitoringInitParams(AppInstallationID);
                    LivePerson.initialize(cordova.getActivity(), new InitLivePersonProperties(accountId, AppID, monitoringParams, new InitLivePersonCallBack() {
                        @Override
                        public void onInitSucceed() {
                            Log.i(TAG, "@@@ android ... SDK initialize completed successfully");
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity());
                            sharedPreferences.edit().putString(LP_ACCOUNT_ID, accountId).apply();

                            final JSONObject json = new JSONObject();
                            try {
                                json.put("eventName","LPMessagingSDKInit");
                            } catch (JSONException e1) {
                               
                            }

                            cordova.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PluginResult result = new PluginResult(PluginResult.Status.OK, json.toString());
                                    result.setKeepCallback(true);
                                    callbackContext.sendPluginResult(result);
                                    setCallBack();
                                }
                            });
                        }

                        @Override
                        public void onInitFailed(Exception e) {
                            Log.i(TAG, "@@@ Android ... SDK initialize completed with error");

                            final JSONObject json = new JSONObject();
                            try {
                                json.put("eventName","LPMessagingSDKInit");
                            } catch (JSONException e1) {
                               
                            }

                            cordova.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, json.toString());
                                    result.setKeepCallback(true);
                                    callbackContext.sendPluginResult(result);
                                }
                            });
                        }
                    }));

                }
            });
    }

    private void reconnect(String jwt) {
        final JSONObject json = new JSONObject();
        try {
            json.put("eventName","LPMessagingSDKReconnectWithNewToken");
            json.put("token",jwt);
        } catch (JSONException e1) {
            
        }
        LivePerson.reconnect(new LPAuthenticationParams().setHostAppJWT(jwt));
        PluginResult result = new PluginResult(PluginResult.Status.OK, json.toString());
        result.setKeepCallback(true);
        mCallbackContext.sendPluginResult(result);
    }

    /**
     *
     */
    private void startConversation(final String appID, final String partyID, final String engagement, final String entrypoint) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject json = new JSONObject();
                    try {
                        json.put("eventName","LPMessagingSDKStartConversation");
                        json.put("type","unauthenticated");
                    } catch (JSONException e1) {
                        
                    }
                    try {
                        Context context = cordova.getActivity().getApplicationContext();
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("EXTRA_APPID", appID);
                        intent.putExtra("EXTRA_PARTYID", partyID);
                        intent.putExtra("EXTRA_ENGAGEMENT", engagement);
                        intent.putExtra("EXTRA_ENTRYPOINT", entrypoint);
                        intent.putExtra("EXTRA_PROFILE", uProfile.toString());
                        intent.putExtra("EXTRA_AppInstallationID", AppInstallationID);
                        cordova.getActivity().startActivity(intent);

                       // LivePerson.showConversation(cordova.getActivity());
                        PluginResult result = new PluginResult(PluginResult.Status.OK, json.toString());
                        result.setKeepCallback(true);
                        mCallbackContext.sendPluginResult(result);
                        setCallBack();
                        
                    } catch (Exception e2) {
                        PluginResult result = new PluginResult(PluginResult.Status.ERROR, json.toString());
                        result.setKeepCallback(true);
                        mCallbackContext.sendPluginResult(result);
                        setCallBack();
                    }

                }
            });
        }

    private void startAuthenticatedConversation(final String appID, final String token, final String partyID, final String engagement, final String entrypoint, final String AppIdentifier, final String WelcomeMsg, final String ChatTitleHeader, final String ClearConversationMsg, final String ClearConfirmMsg, final String ChooseMsg, final String RevolvedTileMsg, final String ResolvedConfirmMsg, final String ClearTitleMsg, final String YesMsg, final String CancelMsg, final String ClearMsg, final String MenuMsg, final String ButtonOpt1Msg, final String ButtonOpt1Value, final String ButtonOpt2Msg, final String ButtonOpt2Value, final String languageApp) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final JSONObject json = new JSONObject();
                try {
                    json.put("eventName","LPMessagingSDKStartConversation");
                    json.put("type","authenticated");
                        
                } catch (JSONException e1) {
                    
                }

                try {
                    Context context = cordova.getActivity().getApplicationContext();    
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("EXTRA_AUTHENTICATE", token);
                    intent.putExtra("EXTRA_APPID", appID);
                    intent.putExtra("EXTRA_PARTYID", partyID);
                    intent.putExtra("EXTRA_ENGAGEMENT", engagement);
                    intent.putExtra("EXTRA_ENTRYPOINT", entrypoint);
                    intent.putExtra("EXTRA_APPIDENTIFIER", AppIdentifier);
                    intent.putExtra("EXTRA_LanguageApp", languageApp);
                    intent.putExtra("EXTRA_AppInstallationID", AppInstallationID);

                    intent.putExtra("EXTRA_WelcomeMsg", WelcomeMsg);
                    intent.putExtra("EXTRA_ChatTitleHeader", ChatTitleHeader);
                    intent.putExtra("EXTRA_ClearConversationMsg", ClearConversationMsg);
                    intent.putExtra("EXTRA_ClearConfirmMsg", ClearConfirmMsg);
                    intent.putExtra("EXTRA_ChooseMsg", ChooseMsg);
                    intent.putExtra("EXTRA_RevolvedTileMsg", RevolvedTileMsg);
                    intent.putExtra("EXTRA_ResolvedConfirmMsg", ResolvedConfirmMsg);
                    intent.putExtra("EXTRA_ClearTitleMsg", ClearTitleMsg);
                    intent.putExtra("EXTRA_YesMsg", YesMsg);
                    intent.putExtra("EXTRA_CancelMsg", CancelMsg);
                    intent.putExtra("EXTRA_ClearMsg", ClearMsg);
                    intent.putExtra("EXTRA_MenuMsg", MenuMsg);

                    intent.putExtra("EXTRA_ButtonOpt1Msg", ButtonOpt1Msg);
                    intent.putExtra("EXTRA_ButtonOpt1Value", ButtonOpt1Value);
                    intent.putExtra("EXTRA_ButtonOpt2Msg", ButtonOpt2Msg);
                    intent.putExtra("EXTRA_ButtonOpt2Value", ButtonOpt2Value);
                    
                    intent.putExtra("EXTRA_PROFILE", uProfile.toString());
                    cordova.getActivity().startActivity(intent);

                   // LivePerson.showConversation(cordova.getActivity(),token);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, json.toString());
                    result.setKeepCallback(true);
                    mCallbackContext.sendPluginResult(result);
                    setCallBack();
                } catch (Exception e2) {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, json.toString());
                    result.setKeepCallback(true);
                    mCallbackContext.sendPluginResult(result);
                    setCallBack();
                }
            }
        });
    }

    /**
     *
     * @param callbackContext
     * @param args
     * @throws JSONException
     */
    private void setProfile(final CallbackContext callbackContext, JSONArray args) throws JSONException {
        uProfile = args;
        final String appId = LP_APP_PACKAGE_NAME;
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


//        callbackContext.success();
        JSONObject json = new JSONObject();
        try {
            json.putOpt("eventName","LPMessagingSDKSetUserProfile");
        } catch (JSONException e) {
            
            callbackContext.error(e.toString());
        }
        try {
            LivePerson.setUserProfile(consumerProfile);
            PluginResult result = new PluginResult(PluginResult.Status.OK, json.toString());
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        } catch (Exception e) {
            try {
                json.putOpt("error",e.toString());
            } catch (JSONException e1) {
                
                callbackContext.error(e1.toString());
            }
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, json.toString());
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        }


    }


    /**
     * Callbacks that call when the conversation activity running
     */
    private void setCallBack() {
        LivePerson.setCallback(new LivePersonCallbackImpl() {
            @Override
            public void onError(TaskType type, String message) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKError");
                    eventJson.put("error",message);
                } catch (JSONException e1) {
                   
                }

                PluginResult result = new PluginResult(PluginResult.Status.ERROR, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onTokenExpired() {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKTokenExpired");

                } catch (JSONException e1) {
                   
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);

            }

            @Override
            public void onConversationStarted(LPConversationData convData) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKConversationStarted");
                    eventJson.put("conversationID",convData.getId());
                } catch (JSONException e1) {
                   
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);

            }

            @Override
            public void onConversationResolved(LPConversationData convData) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKConversationEnded");
                    eventJson.put("conversationID",convData.getId());
                    eventJson.put("closeReason",convData.getCloseReason());
                } catch (JSONException e1) {
                   
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);

            }

            @Override
            public void onConnectionChanged(boolean isConnected) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKConnectionStateChanged");
                    eventJson.putOpt("connectionState",isConnected);
                } catch (JSONException e1) {
                    
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);


            }

            @Override
            public void onAgentTyping(boolean isTyping) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKAgentIsTypingStateChanged");
                    eventJson.put("isTyping",isTyping);
                } catch (JSONException e1) {
                    
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onAgentDetailsChanged(AgentData agentData) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKAgentDetails");
                    eventJson.put("agent",agentData.toString());
                } catch (JSONException e1) {
                   
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onCsatDismissed() {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKCsatDismissed");

                } catch (JSONException e1) {
                    
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onConversationFragmentClosed() {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKConversationClosed");

                } catch (JSONException e1) {
                    
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }
            @Override
            public void onCsatSubmitted(String conversationId) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKConversationCSATDismissedOnSubmission");
                    eventJson.put("conversationId",conversationId);
                } catch (JSONException e1) {
                   
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onConversationMarkedAsUrgent() {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKConversationMarkedAsUrgent");

                } catch (JSONException e1) {
                  
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onConversationMarkedAsNormal() {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKConversationMarkedAsNormal");

                } catch (JSONException e1) {
                    
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onOfflineHoursChanges(boolean isOfflineHoursOn) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKOffHoursStateChanged");
                    eventJson.put("isOffHours",isOfflineHoursOn);
                } catch (JSONException e1) {
                    
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);
            }

            @Override
            public void onAgentAvatarTapped(AgentData agentData) {
                final JSONObject eventJson = new JSONObject();
                try {
                    eventJson.put("eventName","LPMessagingSDKAgentAvatarTapped");
                    eventJson.put("agent",agentData.toString());
                } catch (JSONException e1) {
                   
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, eventJson.toString());
                result.setKeepCallback(true);
                mGlobalCallbackContext.sendPluginResult(result);

            }
        });
    }

}
