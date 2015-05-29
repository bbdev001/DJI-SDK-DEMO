
package com.dji.sdkdemo;

import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.api.DJIDrone;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.api.MainController.DJIMainControllerTypeDef.DJIMcErrorType;
import dji.sdk.interfaces.DJIMcuErrorCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainControllerDemoActivity extends Activity {
    private static final String TAG = "MainControllerDemoActivity";
    
    private TextView mConnectStateTextView;
    private TextView mMainControllerStateTextView;
    private Button mMainControllerErrorBtn;
    
    
    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack  = null;    
    private DJIMcuUpdateStateCallBack mMcuUpdateStateCallBack = null;
    private DJIMcuErrorCallBack mMcuErrorCallBack = null;
    
    private Timer mTimer;
    private String McStateString = "";    
    private String McErrorString = "";
    
    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run() 
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState(); 
        }

    };
    
    private void checkConnectState(){
        
        MainControllerDemoActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run() 
            {   
                boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                if(bConnectState){
                    mConnectStateTextView.setText(R.string.camera_connection_ok);
                }
                else{
                    mConnectStateTextView.setText(R.string.camera_connection_break);
                }
            }
        });
        
    }
    
    private String getErrorDescriptionByErrorCode(DJIMcErrorType errCode){
        String result = "";
        
        if(errCode == DJIMcErrorType.Mc_No_Error ){
            result = getString(R.string.MCU_NO_ERROR);
        }
        else if(errCode == DJIMcErrorType.Mc_Config_Error ){
            result = getString(R.string.MCU_CONFIG_ERROR);
        }
        else if(errCode == DJIMcErrorType.Mc_SerialNum_Error ){
            result = getString(R.string.MCU_SERIALNUM_ERROR);
        }
        else if(errCode == DJIMcErrorType.Mc_Imu_Error ){
            result = getString(R.string.MCU_IMU_ERROR);
        }
        else if(errCode == DJIMcErrorType.Mc_X1_Error ){
            result = getString(R.string.MCU_X1_ERROR);
        }       
        else if(errCode == DJIMcErrorType.Mc_X2_Error ){
            result = getString(R.string.MCU_X2_ERROR);
        }  
        else if(errCode == DJIMcErrorType.Mc_Pmu_Error ){
            result = getString(R.string.MCU_PMU_ERROR);
        }  
        else if(errCode == DJIMcErrorType.Mc_Transmitter_Error ){
            result = getString(R.string.MCU_TRANSMITTER_ERROR);
        }
        else if(errCode == DJIMcErrorType.Mc_Sensor_Error ){
            result = getString(R.string.MCU_SENSOR_ERROR);
        }        
        else if(errCode == DJIMcErrorType.Mc_Compass_Error ){
            result = getString(R.string.MCU_COMPASS_ERROR);
        }  
        else if(errCode == DJIMcErrorType.Mc_Imu_Calibration_Error ){
            result = getString(R.string.MCU_IMU_CALIBRATION_ERROR);
        }         
        else if(errCode == DJIMcErrorType.Mc_Compass_Calibration_Error ){
            result = getString(R.string.MCU_COMPASS_CALIBRATION_ERROR);
        }   
        else if(errCode == DJIMcErrorType.Mc_Transmitter_Calibration_Error ){
            result = getString(R.string.MCU_TRANSMITTER_CALIBRATION_ERROR);
        }           
        else if(errCode == DJIMcErrorType.Mc_Invalid_Battery_Error ){
            result = getString(R.string.MCU_INVALID_BATTERY_ERROR);
        }    
        else if(errCode == DJIMcErrorType.Mc_Invalid_Battery_Communication_Error ){
            result = getString(R.string.MCU_INVALID_BATTERY_COMMUNICATION_ERROR);
        }    
        else if(errCode == DJIMcErrorType.Mc_Unkown_Error ){
            result = getString(R.string.MCU_UNKOWN_ERROR);
        }          
        return result;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_controller_demo);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_MC);
        mMainControllerStateTextView = (TextView)findViewById(R.id.MainControllerStateTV);
        mMainControllerErrorBtn = (Button)findViewById(R.id.MainControllerErrorButton);        
        mConnectStateTextView = (TextView)findViewById(R.id.ConnectStateMCTextView);
        
        //mMainControllerStateBtn.setEnabled(false);
        //mMainControllerStateBtn.setClickable(false);
        mMainControllerErrorBtn.setEnabled(false);
        mMainControllerErrorBtn.setClickable(false);
        
        mDjiGLSurfaceView.start();
        
        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }

            
        };
        
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);
        
        
        mMcuUpdateStateCallBack = new DJIMcuUpdateStateCallBack(){

            @Override
            public void onResult(DJIMainControllerSystemState state) {
                // TODO Auto-generated method stub
                
                StringBuffer sb = new StringBuffer();   
                sb.append(getString(R.string.main_controller_state)).append("\n");
                sb.append("satelliteCount=").append(state.satelliteCount).append("\n");
                sb.append("homeLocationLatitude=").append(state.homeLocationLatitude).append("\n");
                sb.append("homeLocationLongitude=").append(state.homeLocationLongitude).append("\n");
                sb.append("droneLocationLatitude=").append(state.droneLocationLatitude).append("\n");
                sb.append("droneLocationLongitude=").append(state.droneLocationLongitude).append("\n");
                sb.append("velocityX=").append(state.velocityX).append("\n");
                sb.append("velocityY=").append(state.velocityY).append("\n");
                sb.append("velocityZ=").append(state.velocityZ).append("\n");
                sb.append("speed=").append(state.speed).append("\n");      
                sb.append("altitude=").append(state.altitude).append("\n");
                sb.append("pitch=").append(state.pitch).append("\n");
                sb.append("roll=").append(state.roll).append("\n");
                sb.append("yaw=").append(state.yaw).append("\n");
                sb.append("remainPower=").append(state.remainPower).append("\n");
                sb.append("remainFlyTime=").append(state.remainFlyTime).append("\n");
                sb.append("powerLevel=").append(state.powerLevel).append("\n");
                sb.append("isFlying=").append(state.isFlying).append("\n");
                sb.append("noFlyStatus=").append(state.noFlyStatus).append("\n");
                sb.append("noFlyZoneCenterLatitude=").append(state.noFlyZoneCenterLatitude).append("\n");
                sb.append("noFlyZoneCenterLongitude=").append(state.noFlyZoneCenterLongitude).append("\n");
                sb.append("noFlyZoneRadius=").append(state.noFlyZoneRadius);  

                McStateString = sb.toString();
  
                MainControllerDemoActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run() 
                    {   
                        mMainControllerStateTextView.setText(McStateString);
                    }
                });
            }
            
        };
        
        mMcuErrorCallBack = new DJIMcuErrorCallBack(){

            @Override
            public void onError(DJIMcErrorType error) {
                // TODO Auto-generated method stub
                StringBuffer sb = new StringBuffer();   
                sb.append(getString(R.string.main_controller_error)).append("\n");
                sb.append(getErrorDescriptionByErrorCode(error));

                McErrorString = sb.toString();
  
                MainControllerDemoActivity.this.runOnUiThread(new Runnable(){

                    @Override
                    public void run() 
                    {   
                        mMainControllerErrorBtn.setText(McErrorString);
                    }
                });
            }
            
        };
        
        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(mMcuUpdateStateCallBack);
        DJIDrone.getDjiMC().setMcuErrorCallBack(mMcuErrorCallBack);
               
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);        
        
        DJIDrone.getDjiMC().startUpdateTimer(1000);
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        
        if(mTimer!=null) {            
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        
        DJIDrone.getDjiMC().stopUpdateTimer();
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        
        mDjiGLSurfaceView.destroy();
        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        
        super.onDestroy();
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
