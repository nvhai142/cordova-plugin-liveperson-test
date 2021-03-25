package com.liveperson.plugin;

import android.content.Context;
import android.content.Intent;

/**
 * Created by bhavya.mehta on 11-05-2018.
 */

public class FCMUtils {
    /**
     * Call to the {@link FirebaseRegistrationIntentService} class which was taken from Google's
     * sample app for GCM integration
     */
    public static void handleGCMRegistration(Context ctx) {
        Intent intent = new Intent(ctx, FirebaseRegistrationIntentService.class);
        ctx.startService(intent);
    }
}
