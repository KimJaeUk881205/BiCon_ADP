package com.example.bicon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;

import com.minew.beaconset.BluetoothState;
import com.minew.beaconset.MinewBeaconManager;
import com.minew.beaconset.MinewBeacon;
import com.minew.beaconset.MinewBeaconManagerListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private MinewBeaconManager mMinewBeaconManager;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1000;
    private TextView mStart_scan;
    private TextView m_list;
    private TextView m_time;
    private TextView m_point;

    private boolean isScanning;

    private List<MinewBeacon> mMinewBeacons = new ArrayList<>();
    private MinewBeacon mMinewBeacon;

    private int POINT = 0;
    private boolean isSaved = false;
    private long nowTime =  System.currentTimeMillis();
    private long saveTime = System.currentTimeMillis();
    private long saveConfig = 180000;
    private String m_listName = "";

    private EditText biconNm;
    private EditText biconsaveTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initManager();
        checkBluetooth();
        checkLocationPermition();

        mStart_scan = (TextView) findViewById(R.id.textView2);
        m_list = (TextView) findViewById(R.id.biconlist);
        m_time = (TextView) findViewById(R.id.timett);
        m_point = (TextView) findViewById(R.id.nowpoint);

        biconNm = (EditText) findViewById(R.id.biconNm);
        biconsaveTime = (EditText) findViewById(R.id.savetimeset);

        Button button1 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"버튼동작",Toast.LENGTH_LONG).show();

                if(mMinewBeaconManager != null){
                    checkBluetooth();
                }

                if(isScanning){
                    isScanning = false;
                    mStart_scan.setText("STOP");
                    if(mMinewBeaconManager != null){
                        mMinewBeaconManager.stopScan();
                    }
                }else{
                    isScanning = true;
                    mStart_scan.setText("START");
                    try{
                        mMinewBeaconManager.startScan();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }

        });

        mMinewBeaconManager.setMinewbeaconManagerListener(new MinewBeaconManagerListener() {
            @Override
            public void onUpdateBluetoothState(BluetoothState bluetoothState) {

            }

            @Override
            public void onRangeBeacons(final List<MinewBeacon> list) {
                runOnUiThread(new Runnable() {
                    int i=0;
                    int bconCnt = 0;
                    TextView mDevice_mac;
                    TextView mDevice_temphumi;
                    TextView mDevice_name;
                    TextView mDevice_uuid;
                    TextView mDevice_other;
                    String settingbiconNm="";
                    long saveTimeSEt=0;

                    @Override
                    public void run() {
                        //설정된 비콘명
                        settingbiconNm = biconNm.getText().toString() != null && !"".equals(biconNm.getText().toString()) ? biconNm.getText().toString() : "MiniBeacon_45226";

                        saveTimeSEt = biconsaveTime.getText().toString() != null && !"".equals(biconsaveTime.getText().toString()) ? Long.valueOf(biconsaveTime.getText().toString()).longValue() : saveConfig;

                        mMinewBeacons = list;
                        bconCnt = mMinewBeacons.size();


                        for(i=0; i<bconCnt;i++){
                            mMinewBeacon = mMinewBeacons.get(i);

                            //장치목록
                            if(!m_listName.contains(mMinewBeacon.getName())){
                                if("".equals(m_listName)){
                                    m_listName += mMinewBeacon.getName();
                                }else{
                                    m_listName += "\n"+mMinewBeacon.getName();
                                }

                                m_list.setText(m_listName);
                            }

                            //Toast.makeText(getApplicationContext(),"탐색기기명/현재적립금:"+mMinewBeacon.getName()+"/"+POINT,Toast.LENGTH_SHORT).show();

                            //비콘 정보 비교
                            if(settingbiconNm.equals(mMinewBeacon.getName())){
                                if(isSaved){
                                    nowTime =  System.currentTimeMillis();
                                    if((nowTime-saveTime) > saveTimeSEt){
                                        Toast.makeText(getApplicationContext(),"시간후적립:10",Toast.LENGTH_SHORT).show();
                                        POINT += 10;
                                        saveTime = System.currentTimeMillis();
                                        isSaved = true;
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"바로적립:10",Toast.LENGTH_SHORT).show();
                                    POINT += 10;
                                    saveTime = System.currentTimeMillis();
                                    isSaved = true;
                                }

                                m_point.setText("현재적립포인트:"+POINT);
                                m_time.setText("경과시간(초):"+((nowTime-saveTime)/1000));
                                //Toast.makeText(getApplicationContext(),"경과시간(초):"+((nowTime-saveTime)/1000),Toast.LENGTH_SHORT).show();

                                //break;
                            }else{
                                nowTime =  System.currentTimeMillis();
                            }

                        }

                    }
                });
            }

            @Override
            public void onAppearBeacons(List<MinewBeacon> list) {

            }

            @Override
            public void onDisappearBeacons(List<MinewBeacon> list) {

            }
        });
    }

    private void initManager(){
        mMinewBeaconManager = MinewBeaconManager.getInstance(this);
    }

    private void checkBluetooth(){
        BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
        switch(bluetoothState){
            case BluetoothStateNotSupported:
                Toast.makeText(getApplicationContext(),"블루투스 지원 안함",Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BluetoothStatePowerOff:
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                break;
            case BluetoothStatePowerOn:
                break;
        }
    }

    private void checkLocationPermition(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                //위치정보 사용 권한없음
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_ACCESS_FINE_LOCATION);
            }else{
                //권한 잇음
            }
        }else{
            //Marshmallow 이전일 경우 권한체크 안함
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        //stop scan
        if (isScanning) {
            mMinewBeaconManager.stopScan();
        }
    }


}