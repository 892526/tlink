/* Copyright (C) 2018 VNC Automotive Ltd.  All Rights Reserved.
 *
 * This is a sample application intended to demonstrate part of a
 * VNC Automotive SDK. It is not intended as a production-ready
 * component. */

package com.realvnc.androidsampleserver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.realvnc.androidsampleserver.activity.VNCMobileServer;

public abstract class NotificationHelper {
    public static final String CHANNEL_ID = "NOTIFICATION_CHANNEL";

    public abstract static class ServiceUtils {

        public static void startForegroundServiceWithIntent(
                final Context ctx,
                final Intent intent) {

            // On API 26+ we can force a service to run in the foreground by
            // calling startForegroundService.
            // After the system has created the service, the app has five
            // seconds to call the service's startForeground() method to show
            // the new service's user-visible notification. If the app does not
            // call startForeground() within the time limit, the system stops
            // the service and declares the app to be ANR.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent);
            } else {
                ctx.startService(intent);
            }
        }

        public static void startServiceInForeground(
                final Service service,
                final int notiID,
                final String notiTitle,
                final String notiText,
                final int notiIcon) {

            final Context ctx = service.getApplicationContext();

            final Notification.Builder notiBuilder =
                    NotificationHelper.getNotificationBuilder(ctx);

            NotificationHelper.setServerContentIntent(
                    ctx,
                    notiBuilder,
                    SampleIntents.SHOW_UI_INTENT);

            final Notification notification = notiBuilder
                    .setContentTitle(notiTitle)
                    .setContentText(notiText)
                    .setSmallIcon(notiIcon)
                    .getNotification();

            // Start the service in the foreground and show the new service's
            // user-visible notification
            service.startForeground(notiID, notification);
        }
    }

    public abstract static class UniqueIdGenerator {
        private static int sNotiID = 1;

        public static int generate() {
            return sNotiID++;
        }
    }

    @TargetApi(26)
    public static boolean createNotificationChannel(final Context ctx) {
        // The NotificationChannel class is only available API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationManager notificationManager =
                    NotificationHelper.getNotificationManager(ctx);

            final NotificationChannel channel = new NotificationChannel(
                    NotificationHelper.CHANNEL_ID,
                    ctx.getString(R.string.notification_channel_title),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);

            notificationManager.createNotificationChannel(channel);

            return true;
        }
        return false;
    }

    public static Notification.Builder getNotificationBuilder(
            final Context ctx) {

        // On API 26+ create a builder specifying the NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(ctx, CHANNEL_ID);
        }
        return new Notification.Builder(ctx);
    }

    public static NotificationManager getNotificationManager(
            final Context ctx) {

        return (NotificationManager) ctx.getSystemService(
                Context.NOTIFICATION_SERVICE);
    }

    public static void setServerContentIntent(
            final Context ctx,
            final Notification.Builder builder,
            final String intentAction) {

        final Intent notificationIntent = new Intent(
                ctx,
                VNCMobileServer.class);
        notificationIntent.setAction(intentAction);
        notificationIntent.setPackage(ctx.getPackageName());

        // Due to the issue 63236 in Android Open Source Project (ROM 4.4 only),
        // we always update previous cached pending intent before sending
        // the notification.
        builder.setContentIntent(PendingIntent.getActivity(
                ctx,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
