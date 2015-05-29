package com.dji.sdkdemo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.media.DJIMedia;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraPreviewResolustionType;
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.interfaces.DJIMediaFetchCallBack;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
    private static final String TAG = "MainActivity";
    private int type = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ListView mListView = (ListView)findViewById(R.id.listView); 

        Intent intent= getIntent();  
        type = intent.getIntExtra("DroneType", 0);
        Log.v("type","Type" + type);
        
        if (type == 1) {
            mListView.setAdapter(new DemoListAdapter());
        } else {
            mListView.setAdapter(new p2vDemoListAdapter());
        }
        mListView.setOnItemClickListener(new OnItemClickListener() {  
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {  
                onListItemClick(index);
            }  
        }); 
        
        //Log.e(TAG, "type = "+type);
        onInitSDK(type);
        
        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGerneralListener() {
                        
                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onGetPermissionResult = "+result);
                            Log.e(TAG, "onGetPermissionResultDescription = "+DJIError.getCheckPermissionErrorDescription(result));
                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();


    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
       
    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        onUnInitSDK();
        super.onDestroy();
    }
    
    private void onInitSDK(int type){
        switch(type){
            case 0 : {
                DJIDrone.initWithType(this.getApplicationContext(),DJIDroneType.DJIDrone_Vision);
                break;
            }
            case 1 : {
                DJIDrone.initWithType(this.getApplicationContext(),DJIDroneType.DJIDrone_Inspire1);
                break;
            }
            default : {
                break;
            }
        }
    	
        DJIDrone.connectToDrone();
    }
    
    private void onUnInitSDK(){
        DJIDrone.disconnectToDrone();
    }
    
    private void onListItemClick(int index) {
        Intent intent = null;
        switch (type) {
            case 0 : {
                intent = new Intent(MainActivity.this,p2vDemos[index].demoClass);
                break;
            }
            
            case 1 : {
                intent = new Intent(MainActivity.this, demos[index].demoClass);
                break;
            }
            
            default : {
                break;
            }
        }
        this.startActivity(intent);
    }
    
    private static final DemoInfo[] demos = {
        new DemoInfo(R.string.demo_title_preview,R.string.demo_desc_preview, PreviewDemoActivity.class),
        new DemoInfo(R.string.demo_title_camera_protocol,R.string.demo_desc_camera_protocol, CameraProtocolDemoActivity.class),
        new DemoInfo(R.string.demo_title_main_controller_protocol,R.string.demo_desc_main_controller_protocol, MainControllerDemoActivity.class),
        new DemoInfo(R.string.demo_title_battery_protocol,R.string.demo_desc_battery_protocol, BatteryInfoDemoActivity.class),
        new DemoInfo(R.string.demo_title_gimbal_protocol,R.string.demo_desc_gimbal_protocol, GimbalDemoActivity.class),
        new DemoInfo(R.string.demo_title_gs_protocol,R.string.demo_desc_gs_protocol, GsProtocolDemoActivity.class),
        new DemoInfo(R.string.demo_title_gs_protocol_joystick,R.string.demo_desc_gs_protocol_joystick, GsProtocolJoystickDemoActivity.class),
        new DemoInfo(R.string.demo_title_gs_protocol_hotpoint,R.string.demo_title_gs_protocol_hotpoint, GsProtocolHotPointDemoActivity.class),
        new DemoInfo(R.string.demo_title_remote_control_protocol,R.string.demo_desc_remote_control_protocol,RemoteControlDemoActivity.class),
        new DemoInfo(R.string.demo_title_image_transmitter_protocol,R.string.demo_desc_image_transmitter_protocol,ImageTransmitterDemoActivity.class),
    };
    
    private static final DemoInfo[] p2vDemos = {
        new DemoInfo(R.string.demo_title_preview,R.string.demo_desc_preview, PreviewDemoActivity.class),
        new DemoInfo(R.string.demo_title_camera_protocol,R.string.demo_desc_camera_protocol, CameraProtocolDemoActivity.class),
        new DemoInfo(R.string.demo_title_main_controller_protocol,R.string.demo_desc_main_controller_protocol, MainControllerDemoActivity.class),
        new DemoInfo(R.string.demo_title_battery_protocol,R.string.demo_desc_battery_protocol, BatteryInfoDemoActivity.class),
        new DemoInfo(R.string.demo_title_gimbal_protocol,R.string.demo_desc_gimbal_protocol, GimbalDemoActivity.class),
        new DemoInfo(R.string.demo_title_gs_protocol,R.string.demo_desc_gs_protocol, GsProtocolDemoActivity.class),
        new DemoInfo(R.string.demo_title_gs_protocol_joystick,R.string.demo_desc_gs_protocol_joystick, GsProtocolJoystickDemoActivity.class),
        new DemoInfo(R.string.demo_title_range_extender,R.string.demo_desc_range_extender, RangeExtenderDemoActivity.class),
        new DemoInfo(R.string.demo_title_media_sync,R.string.demo_desc_media_sync, MediaSyncDemoActivity.class)
    };

    private  class DemoListAdapter extends BaseAdapter {
        public DemoListAdapter() {
            super();
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(MainActivity.this, R.layout.demo_info_item, null);
            TextView title = (TextView)convertView.findViewById(R.id.title);
            TextView desc = (TextView)convertView.findViewById(R.id.desc);

            title.setText(demos[index].title);
            desc.setText(demos[index].desc);
            return convertView;
        }
        @Override
        public int getCount() {
            return demos.length;
        }
        @Override
        public Object getItem(int index) {
            return  demos[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }
    
    private  class p2vDemoListAdapter extends BaseAdapter {
        public p2vDemoListAdapter() {
            super();
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            convertView = View.inflate(MainActivity.this, R.layout.demo_info_item, null);
            TextView title = (TextView)convertView.findViewById(R.id.title);
            TextView desc = (TextView)convertView.findViewById(R.id.desc);

            title.setText(p2vDemos[index].title);
            desc.setText(p2vDemos[index].desc);
            return convertView;
        }
        @Override
        public int getCount() {
            return p2vDemos.length;
        }
        @Override
        public Object getItem(int index) {
            return  p2vDemos[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }
    
   private static class DemoInfo{
        private final int title;
        private final int desc;
        private final Class<? extends android.app.Activity> demoClass;

        public DemoInfo(int title , int desc,Class<? extends android.app.Activity> demoClass) {
            this.title = title;
            this.desc  = desc;
            this.demoClass = demoClass;
        }
    }
   
   /** 
    * @Description : RETURN BTN RESPONSE FUNCTION
    * @author      : andy.zhao
    * @param view 
    * @return      : void
    */
   public void onReturn(View view){
       Log.d(TAG ,"onReturn");  
       this.finish();
   }
   
}
