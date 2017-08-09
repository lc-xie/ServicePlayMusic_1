package com.example.stephen.serviceplaymusic_1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private Button start,stop;
    private SeekBar seekBar;
    private int position=0;
    private int musicLength=0;
    private MainActivityReceiver mainActivityReceiver;


    public static final String PLAY_MUSIC="com.example.stephen.action.PLAY_MUSIC";
    //public static final String STOP_MUSIC="com.example.stephen.action.STOP_MUSIC";
    public static final String CHANGE_SEEKBAR="com.example.stephen.action.CHANGE_SEEKBAR";
    public static final String SET_SB_MAX="com.example.stephen.action.SET_SB_MAX";
    public static final String CLOSE_ACTIVITY="com.example.stephen.action.CLOSE_ACTIVITY";
    public static final String RESTART_ACTIVITY="com.example.stephen.action.RESTART_ACTIVITY";

    /*private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService=((MusicService.MyBinder)service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MainActivity","Service Disconnected !!!!!!!!!");
        }
    };
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //musicService=new MusicService();
        start=(Button)findViewById(R.id.start);
        stop=(Button)findViewById(R.id.stop);
        start.setOnClickListener(startPlay);
        //stop.setOnClickListener(stopPlay);
        seekBar=(SeekBar)findViewById(R.id.seekbar);

        Intent bindIntent=new Intent(MainActivity.this,MusicService.class);
        startService(bindIntent);
    }

    private View.OnClickListener startPlay=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(PLAY_MUSIC);
            sendBroadcast(intent);
        }
    };
    /*private View.OnClickListener stopPlay=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //stopService(new Intent(MainActivity.this,MusicService.class));
            Intent intent=new Intent(STOP_MUSIC);
            sendBroadcast(intent);
        }
    };*/

    class MainActivityReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()==CHANGE_SEEKBAR){
                position=intent.getIntExtra("position",0);
                seekBar.setProgress(position);
                Log.d("MainActivity","    "+position);
            }else if (intent.getAction()==SET_SB_MAX){
                musicLength=intent.getIntExtra("max_seekbar",0);
                seekBar.setMax(musicLength);
                Log.d("MainActivity","    "+musicLength);
            }else if (intent.getAction()==CLOSE_ACTIVITY){
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        mainActivityReceiver=new MainActivityReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(SET_SB_MAX);
        intentFilter.addAction(CHANGE_SEEKBAR);
        intentFilter.addAction(CLOSE_ACTIVITY);
        intentFilter.addAction(RESTART_ACTIVITY);
        registerReceiver(mainActivityReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this,MusicService.class));
        unregisterReceiver(mainActivityReceiver);
        super.onDestroy();
    }
}
