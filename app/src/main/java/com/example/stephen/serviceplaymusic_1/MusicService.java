package com.example.stephen.serviceplaymusic_1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private String filePath= Environment.getExternalStorageDirectory()+"/music.mp3";
    private int position=0;
    private int musicLength=0;
    private MyReceiver myReceiver;
    private boolean onPrepareed=false;

    //notification
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    public static final String PLAY_MUSIC="com.example.stephen.action.PLAY_MUSIC";
    public static final String CHANGE_SEEKBAR="com.example.stephen.action.CHANGE_SEEKBAR";
    public static final String SET_SB_MAX="com.example.stephen.action.SET_SB_MAX";
    public static final String CLOSE_ACTIVITY="com.example.stephen.action.CLOSE_ACTIVITY";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MusicService","onCreate !!!!!!!!!");
        //初始化notification
        initNF();
        //注冊廣播接收器
        myReceiver=new MyReceiver();
        IntentFilter intentFilter=new IntentFilter();
        //intentFilter.addAction(STOP_MUSIC);
        intentFilter.addAction(PLAY_MUSIC);
        registerReceiver(myReceiver,intentFilter);
    }

    public void initNF(){
        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent closeAvtivityIntent=new Intent(CLOSE_ACTIVITY);
        PendingIntent closeAvtivityPendingIntent=PendingIntent.getBroadcast(this,0,closeAvtivityIntent,0);
        Intent playMusicIntent=new Intent(PLAY_MUSIC);
        PendingIntent playMusicPendingIntent=PendingIntent.getBroadcast(this,0,playMusicIntent,0);

        RemoteViews views=new RemoteViews(getPackageName(),R.layout.notification_layout);
        views.setTextViewText(R.id.notification_voice_name,"一個人要像一支队伍");
        views.setTextViewText(R.id.notification_voice_author,"作者：流而");
        views.setProgressBar(R.id.progressbar,100,0,false);
        views.setOnClickPendingIntent(R.id.notification_close,closeAvtivityPendingIntent);
        views.setOnClickPendingIntent(R.id.notification_play,playMusicPendingIntent);

        Intent restartBroadcastIntent=new Intent(MusicService.this,MainActivity.class);
        //PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent=PendingIntent.getActivity(MusicService.this,1,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder=new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_voice_play)
                .setContentIntent(pendingIntent)
                .setContent(views)
                .setOngoing(true);
        notificationManager.notify(1,mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        File file=new File(filePath);
        if (file.exists())Log.d("MusicService","File exist!!!!");
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("MusicService","Play Error!!!!!!!!!");
                return false;
            }
        });
        try {
            mediaPlayer.setDataSource(file.getPath());
        }catch (IOException e){
            e.printStackTrace();
            Log.d("MusicService","SetDataSource Error!!!!!!!!!");
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                onPrepareed=true;
                Intent setSeekbarMaxIntent =new Intent(SET_SB_MAX);
                setSeekbarMaxIntent.putExtra("max_seekbar",mediaPlayer.getDuration());
                sendBroadcast(setSeekbarMaxIntent);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mediaPlayer.isPlaying()){
                if (position!=0){
                    mediaPlayer.seekTo(position);
                }
                mediaPlayer.start();
                notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Intent closeAvtivityIntent=new Intent(CLOSE_ACTIVITY);
                PendingIntent closeAvtivityPendingIntent=PendingIntent.getBroadcast(MusicService.this,0,closeAvtivityIntent,0);
                Intent playMusicIntent=new Intent(PLAY_MUSIC);
                PendingIntent playMusicPendingIntent=PendingIntent.getBroadcast(MusicService.this,0,playMusicIntent,0);

                RemoteViews views=new RemoteViews(getPackageName(),R.layout.notification_layout);
                views.setTextViewText(R.id.notification_voice_name,"一個人要像一支队伍");
                views.setTextViewText(R.id.notification_voice_author,"作者：流而");
                views.setProgressBar(R.id.progressbar,100,0,false);
                views.setOnClickPendingIntent(R.id.notification_close,closeAvtivityPendingIntent);
                views.setOnClickPendingIntent(R.id.notification_play,playMusicPendingIntent);
                views.setImageViewResource(R.id.notification_play,R.drawable.notification_voice_pause);
                views.setProgressBar(R.id.progressbar,mediaPlayer.getDuration(),mediaPlayer.getCurrentPosition(),false);

                Intent restartBroadcastIntent=new Intent(MusicService.this,MainActivity.class);
                //PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntent=PendingIntent.getActivity(MusicService.this,1,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder=new NotificationCompat.Builder(MusicService.this);
                mBuilder.setSmallIcon(R.drawable.ic_voice_play)
                        .setContentIntent(pendingIntent)
                        .setContent(views)
                        .setOngoing(true);
                notificationManager.notify(1,mBuilder.build());
                Message message=new Message();
                message.what=1;
                handler.sendMessage(message);
            }else if (mediaPlayer.isPlaying()){
                position=mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
                notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Intent closeAvtivityIntent=new Intent(CLOSE_ACTIVITY);
                PendingIntent closeAvtivityPendingIntent=PendingIntent.getBroadcast(MusicService.this,0,closeAvtivityIntent,0);
                Intent playMusicIntent=new Intent(PLAY_MUSIC);
                PendingIntent playMusicPendingIntent=PendingIntent.getBroadcast(MusicService.this,0,playMusicIntent,0);

                RemoteViews views=new RemoteViews(getPackageName(),R.layout.notification_layout);
                views.setTextViewText(R.id.notification_voice_name,"一個人要像一支队伍");
                views.setTextViewText(R.id.notification_voice_author,"作者：流而");
                views.setProgressBar(R.id.progressbar,100,0,false);
                views.setOnClickPendingIntent(R.id.notification_close,closeAvtivityPendingIntent);
                views.setOnClickPendingIntent(R.id.notification_play,playMusicPendingIntent);
                views.setImageViewResource(R.id.notification_play,R.drawable.notification_voice_play);
                views.setProgressBar(R.id.progressbar,mediaPlayer.getDuration(),mediaPlayer.getCurrentPosition(),false);

                Intent restartBroadcastIntent=new Intent(MusicService.this,MainActivity.class);
                //PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pendingIntent=PendingIntent.getActivity(MusicService.this,1,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder=new NotificationCompat.Builder(MusicService.this);
                mBuilder.setSmallIcon(R.drawable.ic_voice_play)
                        .setContentIntent(pendingIntent)
                        .setContent(views)
                        .setOngoing(true);
                notificationManager.notify(1,mBuilder.build());
                Log.d("MusicService","clicked stop button!!!!!!!!!");
            }
        }
    }

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1){
                //发送广播更新activity中的seekbar进度
                Intent changeSeekbarIntent =new Intent(CHANGE_SEEKBAR);
                position=mediaPlayer.getCurrentPosition();
                changeSeekbarIntent.putExtra("position",position);
                sendBroadcast(changeSeekbarIntent);
                if (mediaPlayer.isPlaying()){
                    handler.sendEmptyMessageDelayed(1,1000);
                    refreshNFProcessBar();
                }
                Log.d("MusicService","position-"+position);
            }
        }
    };

    public void refreshNFProcessBar(){
        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent closeAvtivityIntent=new Intent(CLOSE_ACTIVITY);
        PendingIntent closeAvtivityPendingIntent=PendingIntent.getBroadcast(MusicService.this,0,closeAvtivityIntent,0);
        Intent playMusicIntent=new Intent(PLAY_MUSIC);
        PendingIntent playMusicPendingIntent=PendingIntent.getBroadcast(MusicService.this,0,playMusicIntent,0);

        RemoteViews views=new RemoteViews(getPackageName(),R.layout.notification_layout);
        views.setTextViewText(R.id.notification_voice_name,"一個人要像一支队伍");
        views.setTextViewText(R.id.notification_voice_author,"作者：流而");
        views.setProgressBar(R.id.progressbar,100,0,false);
        views.setOnClickPendingIntent(R.id.notification_close,closeAvtivityPendingIntent);
        views.setOnClickPendingIntent(R.id.notification_play,playMusicPendingIntent);
        views.setImageViewResource(R.id.notification_play,R.drawable.notification_voice_pause);
        views.setProgressBar(R.id.progressbar,mediaPlayer.getDuration(),mediaPlayer.getCurrentPosition(),false);

        Intent restartBroadcastIntent=new Intent(MusicService.this,MainActivity.class);
        //PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent=PendingIntent.getActivity(MusicService.this,1,restartBroadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder=new NotificationCompat.Builder(MusicService.this);
        mBuilder.setSmallIcon(R.drawable.ic_voice_play)
                .setContentIntent(pendingIntent)
                .setContent(views)
                .setOngoing(true);
        notificationManager.notify(1,mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        notificationManager.cancel(1);
        unregisterReceiver(myReceiver);
    }

}
