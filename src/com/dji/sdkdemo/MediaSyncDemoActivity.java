
package com.dji.sdkdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalActivity;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef.CameraMode;
import dji.sdk.api.Camera.DJICameraSystemState;
import dji.sdk.api.media.DJIMedia;
import dji.sdk.interfaces.DJICameraSystemStateCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIMediaFetchCallBack;
import dji.sdk.interfaces.DJIReceivedFileDataCallBack;
import android.R.anim;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
public class MediaSyncDemoActivity extends Activity {
    
    private static final String TAG = "MediaSyncDemoActivity";
    private static String ALBUM_NAME = "/DJI_SDK_DCIM";
    private static final String MNT = android.os.Environment.getExternalStorageDirectory().getPath();
    private RandomAccessFile mSyncFile;
    
    private FileListAdapter mListAdapter;
    private ProgressDialog mDialog;
    private ProgressDialog mDownloadDialog;
    
    private List<DJIMedia> mDJIMediaList = null;
    private DJICameraSystemStateCallBack mCameraSystemStateCallBack = null;
    
    private final int SHOWTOAST = 1;
    private final int SHOW_PROGRESS_DIALOG = 2;
    private final int HIDE_PROGRESS_DIALOG = 3;
    private final int SHOW_DOWNLOAD_PROGRESS_DIALOG = 4;
    private final int HIDE_DOWNLOAD_PROGRESS_DIALOG = 5;
    private final int FETCH_FILE_LIST = 6;
    private final int NEED_REFRESH_FILE_LIST = 7;
    
    private Handler handler = new Handler(new Handler.Callback() {
        
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    setResultToToast((String)msg.obj); 
                    break;
                case SHOW_PROGRESS_DIALOG:
                    ShowProgressDialog();
                    break;  
                case HIDE_PROGRESS_DIALOG:
                    HideProgressDialog();
                    break;                     
                case SHOW_DOWNLOAD_PROGRESS_DIALOG:
                    ShowDownloadProgressDialog();
                    break;  
                case HIDE_DOWNLOAD_PROGRESS_DIALOG:
                    HideDownloadProgressDialog();
                    break;                     
                case FETCH_FILE_LIST:
                    getFileList();
                    break;     
                case NEED_REFRESH_FILE_LIST:
                    mListAdapter.notifyDataSetChanged();
                    break; 
                    
                default:
                    break;
            }
            return false;
        }
    });
    
    private void setResultToToast(String result){
        Toast.makeText(MediaSyncDemoActivity.this, result, Toast.LENGTH_SHORT).show();
    }
    
    private void CreateProgressDialog() {
        
        mDialog = new ProgressDialog(MediaSyncDemoActivity.this);
        mDialog.setMessage(MediaSyncDemoActivity.this.getResources().getString(
                R.string.Message_Waiting));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        
        mDownloadDialog = new ProgressDialog(MediaSyncDemoActivity.this);
        mDownloadDialog.setTitle(R.string.sync_file_title);
        mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(false);        
    }
    
    private void ShowProgressDialog() {
        if(mDialog != null){
            mDialog.show();
        }
    }

    private void HideProgressDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    
    private void ShowDownloadProgressDialog() {
        if(mDownloadDialog != null){
            mDownloadDialog.show();
        }
    }

    private void HideDownloadProgressDialog() {
        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
            mDownloadDialog.dismiss();
        }
    }
    
    private long getAvailableStore(String filePath)
    {
        StatFs statFs = new StatFs(filePath);
        long blockSize = statFs.getBlockSize();
        
        long totalBlocks = statFs.getBlockCount();
        
        long availableSpareBlocks = statFs.getAvailableBlocks();
        
        return availableSpareBlocks*blockSize;
    }
    
    private void getFileList(){
        new Thread(){
            public void run() {
                DJIDrone.getDjiCamera().fetchMediaList(new DJIMediaFetchCallBack(){

                    @Override
                    public void onResult(List<DJIMedia> mList, DJIError mError) {
                        // TODO Auto-generated method stub
                        handler.sendMessage(handler.obtainMessage(HIDE_PROGRESS_DIALOG, null));
                        
                        if(mError.errorCode == DJIError.RESULT_OK ){
                            if(mDJIMediaList != null){
                                mDJIMediaList.clear();
                            }
                            Log.d(TAG, "fetchMediaList success");
                            mDJIMediaList.addAll(mList);
                            handler.sendMessage(handler.obtainMessage(NEED_REFRESH_FILE_LIST, null));
                        }
                        else{
                            Log.e(TAG, "fetchMediaList failed,errorCode = "+ mError.errorCode);
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, DJIError.getErrorDescriptionByErrcode(mError.errorCode)));
                        }
                    }
                    
                });
            }
        }.start();
    }
    
    private void syncOneFileByIndex(final int index){
        
        String state = android.os.Environment.getExternalStorageState();
        if(!android.os.Environment.MEDIA_MOUNTED.equals(state))
        {
            Log.e(TAG,"!android.os.Environment.MEDIA_MOUNTED.equals(state)");
            return ;        
        }
        
        if(mDJIMediaList == null){
            return;
        }
        
        if(index >= mDJIMediaList.size()){
            return;
        }
        
        File destDir = new File(MNT + ALBUM_NAME);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        final DJIMedia mMedia = mDJIMediaList.get(index);
        long size = mMedia.fileSize;        
        long available = getAvailableStore(MNT);
        Log.d(TAG,"syncOneFileByIndex size = "+size);
        Log.d(TAG,"syncOneFileByIndex available = "+available);
        
        if(size > available)
        {
            Log.e(TAG,"syncOneFileByIndex size > available");
            return ;                
        }
        
        
        if (size <= 0)
        {   
            Log.e(TAG,"syncOneFileByIndex size <= 0 return ");
            return;
        }
        
        final String filePath = MNT + ALBUM_NAME + "/" + mMedia.fileName;  
        
        //if sync fail exist or not ,delete the file 
        try {
            File destCheck = new File(filePath);
            if (destCheck.exists()) 
            {
                destCheck.delete();
            }
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        
        try {
            mSyncFile = new RandomAccessFile(filePath, "rw");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            mSyncFile = null;
            e.printStackTrace();
        }
        
        if(mSyncFile == null){
            return;
        }
        
        handler.sendMessage(handler.obtainMessage(SHOW_DOWNLOAD_PROGRESS_DIALOG, null));
        
        new Thread(){
            public void run() {
                DJIDrone.getDjiCamera().fetchMediaData(mMedia,new DJIReceivedFileDataCallBack(){

                    @Override
                    public void onResult(byte[] buffer, int size, int progress, DJIError mErr) {
                        // TODO Auto-generated method stub
                        if(mErr.errorCode == DJIError.RESULT_OK ){
                            Log.d(TAG, "syncOneFileByIndex fetchMediaData success,progress = "+ progress);
                            try {
                                mSyncFile.write(buffer, 0, size);
                                mDownloadDialog.setProgress(progress);
                                if(progress == 100){
                                    mSyncFile.close();
                                    handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                                    handler.sendMessage(handler.obtainMessage(SHOWTOAST, getString(R.string.sync_success)));
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        else{
                            handler.sendMessage(handler.obtainMessage(HIDE_DOWNLOAD_PROGRESS_DIALOG, null));
                            if(mSyncFile!=null)
                            {
                                try {
                                    mSyncFile.close();
                                } catch (IOException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                
                                //if sync fail ,delete the file 
                                File destDir = new File(filePath);
                                if (destDir.exists()) 
                                {
                                    destDir.delete();
                                }
                            }
                            
                            Log.e(TAG, "syncOneFileByIndex fetchMediaData failed,errorCode = "+ mErr.errorCode);
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, getString(R.string.sync_failed)+","+DJIError.getErrorDescriptionByErrcode(mErr.errorCode)));
                        }
                    }
                    
                });  
            }
        }.start();
    }
    private void getThumbnailByIndex(int index){
       
        if(mDJIMediaList == null){
            return;
        }
        
        if(index >= mDJIMediaList.size()){
            return;
        }
        
        final DJIMedia mMedia = mDJIMediaList.get(index);
        
        //reset icon to ic_dialog_info 
        MediaSyncDemoActivity.this.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
            }
        });
        
        
        DJIDrone.getDjiCamera().fetchMediaThumbnail(mMedia,new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr) {
                // TODO Auto-generated method stub
                if(mErr.errorCode == DJIError.RESULT_OK){
                    Log.d(TAG, "getThumbnailByIndex success");
                    
                    MediaSyncDemoActivity.this.runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if(mDownloadDialog != null && mDownloadDialog.isShowing()){
                                @SuppressWarnings("deprecation")
                                Drawable mDrawable = new BitmapDrawable(mMedia.thumbnail);
                                mDownloadDialog.setIcon(mDrawable);
                            }
                        }
                    });

                }
                else{
                    Log.e(TAG, "getThumbnailByIndex failed,errorCode = "+ mErr.errorCode);
                }
            }
            
        });
        
    }
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_sync_demo);
        
        ListView mListView = (ListView)findViewById(R.id.filelistView); 
        
        mListAdapter = new FileListAdapter();
        mListView.setOnItemClickListener(new OnItemClickListener() {  
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {  
                Log.d(TAG,"Index = "+ index);
                getThumbnailByIndex(index);
                syncOneFileByIndex(index);
            }  
        });  
        mListView.setAdapter(mListAdapter);
        
        CreateProgressDialog();
        
        mCameraSystemStateCallBack = new DJICameraSystemStateCallBack(){

            @Override
            public void onResult(DJICameraSystemState mState) {
                // TODO Auto-generated method stub
                
            }
            
        };
        DJIDrone.getDjiCamera().setDjiCameraSystemStateCallBack(mCameraSystemStateCallBack);
        
        handler.sendMessage(handler.obtainMessage(SHOW_PROGRESS_DIALOG, null));
        mDJIMediaList = new ArrayList<DJIMedia>();
        handler.sendMessageDelayed(handler.obtainMessage(FETCH_FILE_LIST, null), 500);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onResume");
        
        DJIDrone.getDjiCamera().startUpdateTimer(1000);
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        
        DJIDrone.getDjiCamera().stopUpdateTimer();
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
        
        DJIDrone.getDjiCamera().setCameraMode(CameraMode.Camera_Camera_Mode,new DJIExecuteResultCallback(){

            @Override
            public void onResult(DJIError mErr) {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        if(mDJIMediaList != null){
            mDJIMediaList.clear();
        }
        
        super.onDestroy();
    }
    
    private  class FileListAdapter extends BaseAdapter {
        
        class ItemHolder {
            //ImageView thumbnail_img;
            TextView  file_name;
        }
        
        public FileListAdapter() {
            super();
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            
            final ItemHolder mItemHolder;
            
            if (convertView == null) {
                convertView = LayoutInflater.from(MediaSyncDemoActivity.this).inflate(
                        R.layout.media_info_item, null);
                
                mItemHolder = new ItemHolder();
                //mItemHolder.thumbnail_img = (ImageView) convertView.findViewById(R.id.item_image);
                mItemHolder.file_name = (TextView) convertView.findViewById(R.id.filename);
                convertView.setTag(mItemHolder);                
            }
            else {
                mItemHolder = (ItemHolder)convertView.getTag();
            }

            if(mDJIMediaList != null){
                mItemHolder.file_name.setText(mDJIMediaList.get(index).fileName);
            }
            else{
                mItemHolder.file_name.setText("");
            }

            return convertView;
        }
        @Override
        public int getCount() {
            if(mDJIMediaList != null){
                return mDJIMediaList.size();
            }
            return 0;
        }
        @Override
        public Object getItem(int index) {
            if(mDJIMediaList != null){
                return mDJIMediaList.get(index);
            }
            return  null;
        }

        @Override
        public long getItemId(int id) {
            return id;
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
