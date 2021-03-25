package com.liveperson.plugin;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.liveperson.infra.ICallback;
import com.liveperson.infra.auth.LPAuthenticationParams;
import com.liveperson.messaging.sdk.api.LivePerson;


/**
 * Created by nirni on 11/20/16.
 */

public class FirebaseRegistrationIntentService extends IntentService {

    public static final String TAG = FirebaseRegistrationIntentService.class.getSimpleName();

    public FirebaseRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String token = FirebaseInstanceId.getInstance().getToken();
        String account = ChatActivity.getBrandID();
        String appID = ChatActivity.getAppID();
        LivePerson.registerLPPusher(account, appID, token, new LPAuthenticationParams(), new ICallback<Void, Exception>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onHandleIntent onSuccess: ");
            }

            @Override
            public void onError(Exception error) {
//                Log.d(TAG, "onHandleIntent onError: " + error.getMessage());
            }
        });
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        // Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        // LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);

    }
}
