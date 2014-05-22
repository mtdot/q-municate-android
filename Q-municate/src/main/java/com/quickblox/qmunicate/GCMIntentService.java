package com.quickblox.qmunicate;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.gcm.NotificationHelper;
import com.quickblox.qmunicate.model.PushMessage;
import com.quickblox.qmunicate.ui.splash.SplashActivity;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class GCMIntentService extends IntentService {

    public final static int NOTIFICATION_ID = 1;
    public final static long VIBRATOR_DURATION = 300;

    private final static String TAG = GCMIntentService.class.getSimpleName();

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
               /* sendNotification("Deleted messages on server: " +
                        extras.toString());*/
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                parseMessage(extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void parseMessage(Bundle extras) {
        String message = extras.getString(NotificationHelper.MESSAGE, "");
        saveMissedMessageFlag(true);
        sendNotification(message);
    }

    private void saveMissedMessageFlag(boolean isMissedMessage) {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_MISSED_MESSAGE, isMissedMessage);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, SplashActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, Consts.ZERO_VALUE, intent,
                Consts.ZERO_VALUE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.ic_launcher).setContentTitle(getString(R.string.push_title)).setStyle(
                new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg).setVibrate(
                new long[]{Consts.ZERO_VALUE, VIBRATOR_DURATION});

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void sendBroadcast(PushMessage message) {
        Intent intent = new Intent();
        intent.setAction(NotificationHelper.ACTION_VIDEO_CALL);
        QBUser qbUser = new QBUser();
        qbUser.setId(message.getUserId());
        intent.putExtra(Consts.USER, qbUser);
        sendBroadcast(intent);
    }
}