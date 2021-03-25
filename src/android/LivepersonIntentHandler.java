package com.liveperson.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.liveperson.api.LivePersonIntents;
import com.liveperson.api.sdk.LPConversationData;
import com.liveperson.api.sdk.PermissionType;
import com.liveperson.infra.ICallback;
//import com.liveperson.infra.log.LPMobileLog;
import com.liveperson.messaging.TaskType;
import com.liveperson.messaging.model.AgentData;
import com.liveperson.messaging.sdk.api.LivePerson;

/**
 * Created by nishant.singh on 05-04-2018.
 */

public class LivepersonIntentHandler {

    private BroadcastReceiver mLivePersonReceiver;
    private static final String TAG = LivepersonIntentHandler.class.getSimpleName();
    private boolean isCsatLaunched = false;
    private boolean isConversationUrgent = false;
    private boolean isConversationActive = false;
    ChatActivityCallback mChatActivityCallback;


    public LivepersonIntentHandler(ChatActivity mChatActivity) {
        mChatActivityCallback = (ChatActivityCallback) mChatActivity;
        registerToLivePersonEvents();
        checkStatus();
    }

    public boolean getIsConversationUrgent() {
        return isConversationUrgent;
    }

    public void setIsConversationUrgent(Boolean isUrgent) {
        isConversationUrgent = isUrgent;
    }

    public boolean getIsConversationActive() {
        return isConversationActive;
    }

    public boolean getIsCsatLaunched() {
        return isCsatLaunched;
    }

    public void registerToLivePersonEvents() {
        createLivePersonReceiver();
        LocalBroadcastManager.getInstance((Context) mChatActivityCallback)
                .registerReceiver(mLivePersonReceiver, LivePersonIntents.getIntentFilterForAllEvents());
    }

    private void checkStatus() {
        LivePerson.checkActiveConversation(new ICallback<Boolean, Exception>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Log.d("checkActiveConversation", "onSuccess: " +aBoolean);
                isConversationActive = aBoolean;
            }

            @Override
            public void onError(Exception e) {
                Log.d("checkActiveConversation", "onError: " + e.getMessage());
            }
        });

        LivePerson.checkConversationIsMarkedAsUrgent(new ICallback<Boolean, Exception>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                isConversationUrgent = aBoolean;
            }

            @Override
            public void onError(Exception e) {
//                Log.e(TAG, e.getMessage());
            }
        });

    }


    private void createLivePersonReceiver() {
        if (mLivePersonReceiver != null) {
            return;
        }
        mLivePersonReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()) {
                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_AGENT_AVATAR_TAPPED_INTENT_ACTION:
                        onAgentAvatarTapped(LivePersonIntents.getAgentData(intent));
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_AGENT_DETAILS_CHANGED_INTENT_ACTION:
                        AgentData agentData = LivePersonIntents.getAgentData(intent);
                        onAgentDetailsChanged(agentData);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_AGENT_TYPING_INTENT_ACTION:
                        boolean isTyping = LivePersonIntents.getAgentTypingValue(intent);
                        onAgentTyping(isTyping);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CONNECTION_CHANGED_INTENT_ACTION:
                        boolean isConnected = LivePersonIntents.getConnectedValue(intent);
                        onConnectionChanged(isConnected);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CONVERSATION_MARKED_AS_NORMAL_INTENT_ACTION:
                        onConversationMarkedAsNormal();
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CONVERSATION_MARKED_AS_URGENT_INTENT_ACTION:
                        onConversationMarkedAsUrgent();

                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CONVERSATION_RESOLVED_INTENT_ACTION:
                        LPConversationData lpConversationData = LivePersonIntents.getLPConversationData(intent);
                        onConversationResolved(lpConversationData);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CONVERSATION_STARTED_INTENT_ACTION:
                        LPConversationData lpConversationData1 = LivePersonIntents.getLPConversationData(intent);
                        onConversationStarted(lpConversationData1);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CSAT_LAUNCHED_INTENT_ACTION:
                        onCsatLaunched();
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CSAT_DISMISSED_INTENT_ACTION:
                        onCsatDismissed();
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CSAT_SKIPPED_INTENT_ACTION:
                        onCsatSkipped();
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_CSAT_SUBMITTED_INTENT_ACTION:
                        String conversationId = LivePersonIntents.getConversationID(intent);
                        onCsatSubmitted(conversationId);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_ERROR_INTENT_ACTION:
                        TaskType type = LivePersonIntents.getOnErrorTaskType(intent);
                        String message = LivePersonIntents.getOnErrorMessage(intent);
                        onError(type, message);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_OFFLINE_HOURS_CHANGES_INTENT_ACTION:
                        onOfflineHoursChanges(LivePersonIntents.getOfflineHoursOn(intent));
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_TOKEN_EXPIRED_INTENT_ACTION:
                        // handle Token expired
                        Log.d(TAG, "onReceive: LP_ON_TOKEN_EXPIRED_INTENT_ACTION");
                        onTokenExpired();
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_USER_DENIED_PERMISSION:
                        PermissionType deniedPermissionType = LivePersonIntents.getPermissionType(intent);
                        boolean doNotShowAgainMarked = LivePersonIntents.getPermissionDoNotShowAgainMarked(intent);
                        onUserDeniedPermission(deniedPermissionType, doNotShowAgainMarked);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_USER_ACTION_ON_PREVENTED_PERMISSION:
                        PermissionType preventedPermissionType = LivePersonIntents.getPermissionType(intent);
                        onUserActionOnPreventedPermission(preventedPermissionType);
                        break;

                    case LivePersonIntents.ILivePersonIntentAction.LP_ON_STRUCTURED_CONTENT_LINK_CLICKED:
                        String uri = LivePersonIntents.getLinkUri(intent);
                        onStructuredContentLinkClicked(uri);
                        break;
                }

            }
        };

    }

    private void showToast(String message) {
       // LPMobileLog.d(TAG + "_CALLBACK", message);
    }

    private void onAgentAvatarTapped(AgentData agentData) {
        showToast("on Agent Avatar Tapped - " + agentData.mFirstName + " " + agentData.mLastName);
    }


    private void onOfflineHoursChanges(boolean isOfflineHoursOn) {
        showToast("on Offline Hours Changes - " + isOfflineHoursOn);
    }

    private void onConversationMarkedAsNormal() {
        showToast("Conversation Marked As Normal");
        isConversationUrgent = false;
    }

    private void onConversationMarkedAsUrgent() {
        showToast("Conversation Marked As Urgent");
        isConversationUrgent = true;
    }

    private void onCsatSubmitted(String conversationId) {
        showToast("on CSAT Submitted. ConversationID = " + conversationId);
        if (mChatActivityCallback != null)
            mChatActivityCallback.finishChatScreen();
    }

    private void onCsatLaunched() {
        showToast("on CSAT Launched");
        isCsatLaunched = true;
        if (mChatActivityCallback != null)
            mChatActivityCallback.closeOptionMenu();
    }

    private void onCsatDismissed() {
        showToast("on CSAT Dismissed");
    }

    private void onCsatSkipped() {
        showToast("on CSAT Skipped");
    }

    private void onAgentDetailsChanged(AgentData agentData) {
        Log.e(TAG, agentData.mNickName);
        if (mChatActivityCallback != null)
            mChatActivityCallback.setAgentName(agentData.mNickName);
    }

    private void onAgentTyping(boolean isTyping) {
        showToast("isTyping " + isTyping);
    }

    private void onConnectionChanged(boolean isConnected) {
        showToast("onConnectionChanged " + isConnected);
    }

    private void onConversationResolved(LPConversationData convData) {
        showToast("Conversation resolved " + convData.getId()
                + " reason " + convData.getCloseReason());
        if (mChatActivityCallback != null)
            mChatActivityCallback.closeOptionMenu();
        isConversationActive = false;
        isConversationUrgent = false;

    }

    private void onConversationStarted(LPConversationData convData) {
        showToast("Conversation started " + convData.getId()
                + " reason " + convData.getCloseReason());
        isConversationActive = true;
    }

    private void onTokenExpired() {
        showToast("onTokenExpired ");
        mChatActivityCallback.reconectChat();
    }

    private void onError(TaskType type, String message) {
        showToast(" problem " + type.name());
    }

    private void onUserDeniedPermission(PermissionType permissionType, boolean doNotShowAgainMarked) {
        showToast("onUserDeniedPermission " + permissionType.name() + " doNotShowAgainMarked = " + doNotShowAgainMarked);
    }

    private void onUserActionOnPreventedPermission(PermissionType permissionType) {
        showToast("onUserActionOnPreventedPermission " + permissionType.name());
    }

    private void onStructuredContentLinkClicked(String uri) {
        showToast("onStructuredContentLinkClicked. Uri: " + uri);
    }

    interface ChatActivityCallback {
        void finishChatScreen();

        void reconectChat();

        void setAgentName(String agentName);

        void closeOptionMenu();
    }

}
