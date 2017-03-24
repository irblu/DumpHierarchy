package com.zzlys.dumphierarchy.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.zzlys.dumphierarchy.R;

import static android.app.Notification.FLAG_ONGOING_EVENT;

/**
 * Created by ziliang.z on 2017/3/2.
 */

public class MainService extends Service {
    private final String TAG = this.getClass().toString();
    private Notification _notification;
    private Context _context;
    private NotificationManager _notiManager;
    private BroadcastReceiver _broadcastReceiver = null;
    private RemoteViews mRemoteViews;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        _context = getApplicationContext();
        _notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(_broadcastReceiver == null) {
            _broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                }
            };
        }
        createNoti();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _notiManager.cancelAll();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNoti() {
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.layout_noti);

        //实例化一个指向MusicService的intent
        Intent intent = new Intent();
        intent.setAction("com.zzlys.dumphierarchy.service.DumpService");

        //设置Toggle按钮的点击事件
        intent.putExtra("BUTTON_INDEX", 1111);
        PendingIntent pendingIntent = PendingIntent.getService(_context, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.button_noti_toggle, pendingIntent);


        _notification = new Notification.Builder(_context)
                .setSmallIcon(R.drawable.ic_stat_dh)
                .build();
        _notification.contentView = mRemoteViews;
        _notification.flags |= FLAG_ONGOING_EVENT;

        _notiManager.notify(0,_notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

}
