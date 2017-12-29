package com.example.a52466.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;

import static java.security.AccessController.getContext;

public class MainActivity extends Activity implements SurfaceHolder.Callback,SensorEventListener {
    private Button start;// 开始录制按钮
    private MediaRecorder mediarecorder;// 录制视频的类
    private SurfaceView surfaceview;// 显示视频的控件
    // 用来显示视频的一个接口，我靠不用还不行，也就是说用mediarecorder录制视频还得给个界面看
    // 想偷偷录视频的同学可以考虑别的办法。。嗯需要实现这个接口的Callback接口
    private SurfaceHolder surfaceHolder;
    private boolean isStart=false;
    private Camera camera;
    private Chronometer timer;
    private float x;
    private float y;
    private float z;
    private float px;
    private float py;
    private float pz;
    private float angle;
    private double average;
    private SensorManager sensorManager1;
    private SensorManager sensorManager2;
    private long delay=50;
    private long delay2=100;
    private int count;
    private int index;
    private Handler h;
    public static final String mComma = ",";
    private static StringBuilder mStringBuilder = null;
    private static String mFileName = null;

    TextView xCoor; // declare X axis object
    TextView yCoor; // declare Y axis object
    TextView zCoor; // declare Z axis object
    TextView Angle; // declare steering angle object
    TextView Tuoluo; // declare Tuoluo object

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        // 设置横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);
        File dirFirstFolder = new File("/sdcard/DrivingData");
        if(!dirFirstFolder.exists()){
            dirFirstFolder.mkdirs();
        }
        init();
        //initial value
        x=9.8F;
        y=0;
        z=0;
        px=0;
        py=0;
        pz=0;
        angle=0;
        count=0;
        index=0;
        average=0;
        //connect elements
        xCoor=(TextView)findViewById(R.id.xcoor); // create X axis object
        yCoor=(TextView)findViewById(R.id.ycoor); // create Y axis object
        zCoor=(TextView)findViewById(R.id.zcoor); // create Z axis object
        Angle=(TextView)findViewById(R.id.SteerAngle); // create Z axis object
        Tuoluo=(TextView)findViewById(R.id.Tuoluo); // create Z axis object

        //set sensor
        sensorManager1=(SensorManager)getSystemService(SENSOR_SERVICE);
        // add listener. The listener will be MyActivity (this) class
        sensorManager1.registerListener(this,
                sensorManager1.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager2=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensorManager2.registerListener(this,
                sensorManager2.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
        //ticker
        h = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Message m = h.obtainMessage(0);
                h.sendMessageDelayed(m, delay);
                count=count+5;
                index++;
                if (count>=100){
                    count=count-100;
                }
                float dx=x-px;
                float dy=y-py;
                float dz=z-pz;
                //float angle=(float)Math.toDegrees(Math.cos((px*x+py*y+pz*z)/((float)Math.sqrt(px*px+py*py+pz*pz)*(float)Math.sqrt(x*x+y*y+z*z))));
                //float angle=(float)Math.toDegrees(Math.cos((dx*0+dy*0+dz*pz)/((float)Math.sqrt(dx*dx+dy*dy+dz*dz)*(float)Math.sqrt(0+0+pz*pz))));
                //float angle=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);

                xCoor.setText("X: "+String.format("%.2f",x)+"("+String.format("%.2f", px)+")");
                yCoor.setText("Y: "+String.format("%.2f",y)+"("+String.format("%.2f", py)+")");
                zCoor.setText("Z: "+String.format("%.2f",z)+"("+String.format("%.2f", pz)+")");
                double show=(angle)*180/Math.PI;
                Tuoluo.setText("X: "+String.format("%.2f",angle));
                angle=0;
                average=show;//average+(show * 0.1);


                Angle.setText("Angle: "+String.format("%.2f", average)+"°");
                if (isStart==true) {
                    mStringBuilder.append(index);
                    mStringBuilder.append(mComma);
                    if (count<10){
                        mStringBuilder.append(timer.getText()+":0"+count);
                    }
                    else
                        mStringBuilder.append(timer.getText()+":"+count);
                    mStringBuilder.append(mComma);
                    mStringBuilder.append(String.format("%.2f", average));
                    mStringBuilder.append("\n");
                }
                average=0;
                return false;
            }
        });
    }



    private void init() {
        start = (Button) this.findViewById(R.id.start);
        start.setOnClickListener(new TestVideoListener());
        surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
        SurfaceHolder holder = surfaceview.getHolder();// 取得holder
        holder.addCallback(this); // holder加入回调接口
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        timer = (Chronometer)findViewById(R.id.Time);
    }

    class TestVideoListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (isStart==false) {
                count=0;
                mediarecorder = new MediaRecorder();// 创建mediarecorder对象
                // 设置录制视频源为Camera(相机)
                mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
                mediarecorder
                        .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                // 设置录制的视频编码h263 h264
                mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
                mediarecorder.setVideoSize(720, 480);
                // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
                mediarecorder.setVideoEncodingBitRate(5*1024*1024);
                mediarecorder.setVideoFrameRate(40);
                mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
                // 设置视频文件输出的路径
                SimpleDateFormat DateFormat=new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
                String date=DateFormat.format(new java.util.Date());
                mediarecorder.setOutputFile("/sdcard/DrivingData/"+date+".mp4");
                try {
                    // 准备录制
                    mediarecorder.prepare();
                    // 开始录制
                    mediarecorder.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //initial csv
                String folderName = null;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    folderName ="/sdcard/DrivingData/CSV/";
                }
                File fileRobo = new File(folderName);
                if(!fileRobo.exists()){
                    fileRobo.mkdir();
                }
                mFileName = folderName + date+".csv";
                mStringBuilder = new StringBuilder();
                mStringBuilder.append("Index");
                mStringBuilder.append(mComma);
                mStringBuilder.append("time");
                mStringBuilder.append(mComma);
                mStringBuilder.append("angle");
                mStringBuilder.append("\n");


                //start timer
                timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
                timer.setFormat("0"+String.valueOf(hour)+":%s");
                timer.start();
                index=0;
                start.setText("Stop");
                Message m = h.obtainMessage(0);
                h.sendMessageDelayed(m, delay);
                isStart=true;
            }
            else if (isStart==true) {
                if (mediarecorder != null) {
                    // 停止录制
                    mediarecorder.stop();
                    // 释放资源
                    mediarecorder.release();
                    mediarecorder = null;
                }
                //stop csv
                if (mFileName != null) {
                    try {
                        File file = new File(mFileName);
                        FileOutputStream fos = new FileOutputStream(file, false);
                        fos.write(mStringBuilder.toString().getBytes());
                        fos.flush();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new RuntimeException("You should call open() before flush()");
                }

                timer.stop();
                start.setText("Start");
                isStart=false;
                Toast.makeText(getApplicationContext(), "Saved Success!", Toast.LENGTH_LONG).show();
                count=0;
                h.removeMessages(0);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        surfaceHolder = holder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        surfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surfaceDestroyed的时候同时对象设置为null
        surfaceview = null;
        surfaceHolder = null;
        mediarecorder = null;
    }

    // 当精度发生变化时调用
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    // 当sensor事件发生时候调用
    public void onSensorChanged(SensorEvent event){

        // check sensor type
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

            px=x;
            py=y;
            pz=z;
            // assign directions
            x=event.values[0];
            y=event.values[1];
            z=event.values[2];
        }
        if (event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            angle=event.values[0];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeMessages(0);
    }

    @Override
    protected void onDestroy() {
        sensorManager1.unregisterListener(this);
        sensorManager2.unregisterListener(this);
        super.onDestroy();
    }
}