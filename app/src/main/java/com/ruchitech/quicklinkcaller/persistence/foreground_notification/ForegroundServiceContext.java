package com.ruchitech.quicklinkcaller.persistence.foreground_notification;

;

import static com.ruchitech.quicklinkcaller.persistence.McsConstants.ZERO;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ruchitech.quicklinkcaller.R;


public class ForegroundServiceContext extends ContextWrapper {
    public static final String EXTRA_FOREGROUND = "foreground";
    private static final String TAG = "ForegroundService";

    public ForegroundServiceContext(Context context) {
        super(context);
    }

    private static Notification buildForegroundNotification(Context context, String str) {
        Intent intent = new Intent();
        intent.setAction("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent activity = PendingIntent.getActivity(context, ZERO, intent, ZERO | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("foreground-service", "Caller ID Running", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setShowBadge(false);
            notificationChannel.setVibrationPattern(new long[]{ZERO});
            notificationChannel.setLockscreenVisibility(ZERO);
            ((NotificationManager) context.getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "foreground-service")
                .setOngoing(true)
                .setContentTitle(context.getResources().getString(R.string.enhance_caller_id))
                //.setContentText(context.getResources().getString(R.string.notitication_content_for_persistence))
                .setSmallIcon(R.drawable.phone)
                .setContentIntent(activity)
                .setChannelId("foreground-service")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("Keep Caller ID in background")
                .bigText(context.getResources().getString(R.string.notitication_content_for_persistence)));

        return builder.build();
    }

    public static void completeForegroundService(Service service, Intent intent, String str) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_FOREGROUND, false)) {
            return;
        }
        String serviceName = getServiceName(service);
        Log.d(str, "Started " + serviceName + " in foreground mode.");
        try {
            Notification buildForegroundNotification = buildForegroundNotification(service, serviceName);
            service.startForeground(serviceName.hashCode(), buildForegroundNotification);
            Log.d(str, "Notification: " + buildForegroundNotification);
        } catch (Exception e5) {
            Log.e("gfkmjkhgh", "completeForegroundService: "+e5);
            Log.w(str, e5);
        }
    }

    private static String getServiceName(Service service) {
        String str = null;
        try {
            ForegroundServiceInfo foregroundServiceInfo = (ForegroundServiceInfo) service.getClass().getAnnotation(ForegroundServiceInfo.class);
            if (foregroundServiceInfo != null) {
                if (foregroundServiceInfo.res() != 0) {
                    try {
                        str = service.getString(foregroundServiceInfo.res());
                    } catch (Exception unused) {
                    }
                }
                if (str == null) {
                    str = foregroundServiceInfo.value();
                }
            }
        } catch (Exception unused2) {
        }
        return str == null ? service.getClass().getSimpleName() : str;
    }

    private boolean isIgnoringBatteryOptimizations() {
        return ((PowerManager) getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName());
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public ComponentName startService(Intent intent) {
        int i5 = Build.VERSION.SDK_INT;
        if (i5 < 23 || isIgnoringBatteryOptimizations()) {
            return super.startService(intent);
        }
        Log.d(TAG, "Starting in foreground mode.");
        intent.putExtra(EXTRA_FOREGROUND, true);
        return i5 >= 26 ? super.startForegroundService(intent) : super.startService(intent);
    }
}