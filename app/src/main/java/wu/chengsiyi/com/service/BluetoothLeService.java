/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wu.chengsiyi.com.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;

import java.util.List;
import java.util.UUID;

import static wu.chengsiyi.com.base.BaseApplication.getBluetoothClient;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Device_CommBaudRate;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Device_CustomerID;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Device_Name;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Module_Information;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Module_Soft_Version;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Password_C1;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Password_Notify;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_ReadDataFromDevice;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_SendDataToDevice;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Service_SendDataToDevice;
import static wu.chengsiyi.com.constant.Constants.REQUEST_SUCCESS;
import static wu.chengsiyi.com.constant.Constants.STATUS_CONNECTED;
import static wu.chengsiyi.com.constant.Constants.STATUS_DISCONNECTED;
/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@SuppressWarnings("unused")
public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothClient mClient;

    private final IBinder mBinder = new LocalBinder();

    private boolean mConnected;

    //连接的设备
    private BluetoothDevice device;
    private BleConnectOptions options;
    //  特征值： character
    private BleGattCharacter g_Character_TX; //  uuid  :  0000ffe9-0000-1000-8000-00805f9b34fb
    private BleGattCharacter g_Character_RX; //  uuid  :  0000ffe4-0000-1000-8000-00805f9b34fb
    private BleGattCharacter g_Character_DeviceName; //  uuid  :
    private BleGattCharacter g_Character_CustomerID; //  uuid  :
    private BleGattCharacter g_Character_Baud_Rate; //  uuid  :
    private BleGattCharacter g_Character_Password; //  uuid  :
    private BleGattCharacter g_Character_Password_Notify; //  uuid  :
    private BleGattCharacter g_Character_Module_Information; //  uuid  :
    private BleGattCharacter g_Character_Module_Soft_Information; //  uuid  :

    public void setDevice(BluetoothDevice device) {
        this.device = device;
        mClient.registerConnectStatusListener(device.getAddress(), mBleConnectStatusListener);
        mClient.connect(device.getAddress(), options,new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                Log.d(TAG, "onResponse: "+code+"   data:  "+profile);

                boolean have_Service_SendDataToDevice=false;

                if (code == REQUEST_SUCCESS) {
                    List<BleGattService> services = profile.getServices();
                    for (BleGattService service : services) {
                        if(C_UUID_Service_SendDataToDevice.equals(service.getUUID())){
                            have_Service_SendDataToDevice=true;
                        }
                        List<BleGattCharacter> characters = service.getCharacters();
                        for (BleGattCharacter character : characters) {

                            if(C_UUID_Character_SendDataToDevice.equals(character.getUuid())){
                                g_Character_TX = character;
                                Log.d(TAG, "onResponse: "+g_Character_TX);
                            }
                            if(C_UUID_Character_ReadDataFromDevice.equals(character.getUuid())){
                                g_Character_RX = character;
                                Log.d(TAG, "onResponse: "+g_Character_RX);
                            }
                            if(C_UUID_Character_Device_Name.equals(character.getUuid())){
                                g_Character_DeviceName = character;
                                Log.d(TAG, "onResponse: "+g_Character_DeviceName);
                            }
                            if(C_UUID_Character_Device_CustomerID.equals(character.getUuid())){
                                g_Character_CustomerID = character;
                                Log.d(TAG, "onResponse: "+g_Character_CustomerID);
                            }
                            if(C_UUID_Character_Device_CommBaudRate.equals(character.getUuid())){
                                g_Character_Baud_Rate = character;
                                Log.d(TAG, "onResponse: "+g_Character_Baud_Rate);
                            }
                            if(C_UUID_Character_Password_C1.equals(character.getUuid())){
                                g_Character_Password = character;
                                Log.d(TAG, "onResponse: "+g_Character_Password);
                            }
                            if(C_UUID_Character_Password_Notify.equals(character.getUuid())){
                                g_Character_Password_Notify = character;
                                Log.d(TAG, "onResponse: "+g_Character_Password_Notify);
                            }
                            if(C_UUID_Character_Module_Information.equals(character.getUuid())){
                                g_Character_Module_Information = character;
                                Log.d(TAG, "onResponse: "+g_Character_Module_Information);
                            }
                            if(C_UUID_Character_Module_Soft_Version.equals(character.getUuid())){
                                g_Character_Module_Soft_Information = character;
                                Log.d(TAG, "onResponse: "+g_Character_Module_Soft_Information);
                            }
                        }
                    }
                    Log.d(TAG, "onResponse: "+"   去打开通知~~~ "+have_Service_SendDataToDevice+"   ");
                    if(g_Character_RX!=null && have_Service_SendDataToDevice){
                        Log.d(TAG, "onResponse: "+"打开中");
                            mClient.notify(device.getAddress(), C_UUID_Service_SendDataToDevice, g_Character_RX.getUuid(), new BleNotifyResponse() {
                                @Override
                                public void onNotify(UUID service, UUID character, byte[] value) {

                                }

                                @Override
                                public void onResponse(int code) {
                                    if (code == REQUEST_SUCCESS) {
                                        Log.d(TAG, "onResponse: "+"   打开通知成功~~~ ");
                                    }
                                }
                            });

                    }

                }
            }
        });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "   onBind:   跟服务绑定了！  BindService ");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        boolean initialize = initialize();
        Log.d(TAG, "onCreateS:  蓝牙基础服务创建  + " + "   蓝牙初始化 --> " + initialize+"   "+mClient);


        //      -监听设备配对状态变化-
//        mClient.registerBluetoothBondListener(mBluetoothBondListener);
//        mClient.unregisterBluetoothBondListener(mBluetoothBondListener);

    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * 初始化
     */

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    public boolean initialize() {
        mClient = getBluetoothClient();
        // 连接如果失败重试3次
        // 连接超时30s
        // 发现服务如果失败重试3次
        // 发现服务超时20s
        options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();

        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }


    /**
     * 连接状态监听
     */
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            Log.d(TAG, "onConnectStatusChanged: "+status+"   mac : "+mac    );
            //mConnect 判断连接~
            mConnected = (status == Constants.STATUS_CONNECTED);
            if (status == STATUS_CONNECTED) {
//                EventBus.getDefault().post(new MessageEvent(ACTION_STATUS_DISCONNECT));
            } else if (status == STATUS_DISCONNECTED) {

            }
        }
    };

}
