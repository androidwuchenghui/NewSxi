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
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import wu.chengsiyi.com.constant.MessageEvent;
import wu.chengsiyi.com.database.DeviceInfo;
import wu.chengsiyi.com.utils.MyDeviceDao;

import static wu.chengsiyi.com.base.BaseApplication.getBluetoothClient;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_CONNECTED;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_CONNECTED_OK;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_DISCONNECT;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_DISCONNECT_TIP;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Device_CommBaudRate;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Device_CustomerID;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Device_Name;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Module_Information;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Module_Soft_Version;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Password_C1;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_Password_Notify;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_ReadDataFromDevice;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Character_SendDataToDevice;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Service_DeviceConfig;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Service_Password;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Service_ReadDataFromDevice;
import static wu.chengsiyi.com.constant.Constants.C_UUID_Service_SendDataToDevice;
import static wu.chengsiyi.com.constant.Constants.REQUEST_SUCCESS;
import static wu.chengsiyi.com.constant.Constants.STATUS_CONNECTED;
import static wu.chengsiyi.com.constant.Constants.STATUS_DISCONNECTED;
import static wu.chengsiyi.com.utils.MyDeviceDao.deleteDevice;
import static wu.chengsiyi.com.utils.MyUtils.BinaryToHexString;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@SuppressWarnings("unused")
public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    ExecutorService pool = Executors.newFixedThreadPool(3);
    private BleGattProfile mProfile;
    private Thread serviceThread = new Thread() {
        @Override
        public void run() {
            displayServices(mProfile);
            super.run();
        }
    };

    private BluetoothClient mClient;

    private final IBinder mBinder = new LocalBinder();

    private boolean isConnected;

    // service 数量
    private int size;

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
    // 第几次提交密码
    private int commit_amount = 97;
    // 默认密码 和 产品密码
    private static final String DEFAULT_PASSWORD = "000000";
    private static final String YIHI_DEFAULT_PASSWORD = "135246";
    private StringBuilder createPassword;
    private boolean newVersion;

    // 提交私人密码的次数
    private int reChange = 0;
    private int priPass = 0;
    private byte[] mPrivatePassword;


    public void setDevice(BluetoothDevice device) {
        this.device = device;
        mClient.registerConnectStatusListener(device.getAddress(), mBleConnectStatusListener);
        mClient.connect(device.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                Log.d(TAG, "onResponse: " + code + "   profile:  " + profile);
                if (code == REQUEST_SUCCESS) {
                    Log.d(TAG, "onResponse:  成功 --> 遍历service  " + profile);
                    mProfile = profile;
                    pool.execute(serviceThread);
                }
            }
        });

    }

    private void displayServices(BleGattProfile profile) {
        List<BleGattService> services = profile.getServices();
        size = services.size();
        for (BleGattService service : services) {

            List<BleGattCharacter> characters = service.getCharacters();
            for (BleGattCharacter character : characters) {

                if (C_UUID_Character_SendDataToDevice.equals(character.getUuid())) {
                    g_Character_TX = character;
//                                Log.d(TAG, "onResponse: " + g_Character_TX);
                }
                if (C_UUID_Character_ReadDataFromDevice.equals(character.getUuid())) {

                    g_Character_RX = character;
//                                Log.d(TAG, "onResponse: " + g_Character_RX);
                }
                if (C_UUID_Character_Device_Name.equals(character.getUuid())) {
                    g_Character_DeviceName = character;
//                                Log.d(TAG, "onResponse: " + g_Character_DeviceName);
                }
                if (C_UUID_Character_Device_CustomerID.equals(character.getUuid())) {
                    g_Character_CustomerID = character;
//                                Log.d(TAG, "onResponse: " + g_Character_CustomerID);
                }
                if (C_UUID_Character_Device_CommBaudRate.equals(character.getUuid())) {
                    g_Character_Baud_Rate = character;
//                                Log.d(TAG, "onResponse: " + g_Character_Baud_Rate);
                }
                if (C_UUID_Character_Password_C1.equals(character.getUuid())) {
                    g_Character_Password = character;
//                                Log.d(TAG, "onResponse: " + g_Character_Password);
                }
                if (C_UUID_Character_Password_Notify.equals(character.getUuid())) {
                    g_Character_Password_Notify = character;
//                                Log.d(TAG, "onResponse: " + g_Character_Password_Notify);
                }
                if (C_UUID_Character_Module_Information.equals(character.getUuid())) {
                    g_Character_Module_Information = character;
                    newVersion = true;
//                                Log.d(TAG, "onResponse: " + g_Character_Module_Information);
                }
                if (C_UUID_Character_Module_Soft_Version.equals(character.getUuid())) {
                    g_Character_Module_Soft_Information = character;
//                                Log.d(TAG, "onResponse: " + g_Character_Module_Soft_Information);
                }
            }
        }
        if (size > 6) {
            newVersion = false;
        }
        Log.d(TAG, "onResponse:  size " + size + "  ");
        // 打开 读信息 通知
        if (g_Character_RX != null) {
            Log.d(TAG, "onResponse: "+"  打开读信息的通道----");
            mClient.notify(device.getAddress(), C_UUID_Service_ReadDataFromDevice, g_Character_RX.getUuid(), readDataBleNotifyResponse);
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (g_Character_Password_Notify != null) {
            Log.d(TAG, "onResponse: "+"  打开密码通道----");
            mClient.notify(device.getAddress(), C_UUID_Service_Password, g_Character_Password_Notify.getUuid(), passwordBleNotifyResponse);
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 提交密码
        List<DeviceInfo> deviceInfos = MyDeviceDao.queryDevice(device.getAddress());

        if (deviceInfos.size() < 1) {
            //  数据库中不存在
            Log.d(TAG, "onResponse: " + "  数据库中不存在  ");

            // 初次提交默认密码
            String defaultPassword = DEFAULT_PASSWORD + DEFAULT_PASSWORD;
            byte[] passwordBytes = defaultPassword.getBytes();
            commit_amount = 0;
            Log.d(TAG, "onResponse: " + "  提交 000000 密码 ！ ");
            mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, passwordBytes, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == REQUEST_SUCCESS) {

                    }
                }
            });
        } else {
            //  数据库中查询到有连接记录
            commit_amount = 0;
            Log.d(TAG, "onResponse: " + "  连接过 -> 存在  ");
            DeviceInfo deviceInfo = deviceInfos.get(0);
            String password = deviceInfo.getPassword();
            String pw = password + password;
            byte[] pwb = pw.getBytes();
            mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, pwb, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == REQUEST_SUCCESS) {
                        Log.d(TAG, "onResponse: " + "  提交了保存的密码 ！ ");
                    }
                }
            });
        }
    }

    //  接收 读取数据的 Notify
    BleNotifyResponse readDataBleNotifyResponse = new BleNotifyResponse() {
        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                Log.d(TAG, "onResponse: " + " 打开   信息 通知成功 ~~~  ");

            }
        }

        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            String reply = BinaryToHexString(value);
            Log.d(TAG, "onResponse  读到信息 onNotify: " + reply);
        }
    };
    // 接收 设密码的 Notify
    BleNotifyResponse passwordBleNotifyResponse = new BleNotifyResponse() {
        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                Log.d(TAG, "onResponse: " + "打开   密码 通知成功 *** " + " 准备查询数据库，提交密码 > ");
            }
        }
        /**
         * 提交密码后的回调 ( 00:密码正确 )
         * @param service
         * @param character
         * @param value
         */
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            String reply = BinaryToHexString(value);

            Log.d(TAG, "onResponse  密码 onNotify: " + reply);
            //对提交密码返回进行处理
            handlePasswordCallbacks(reply);

        }
    };

    /**
     * 处理密码返回
     * commit_amount
     * 0 : 原始 000000    密码
     * 1 : 产品 135246    密码
     * 2 ：修改           密码
     * 3 ：再次提交000000  密码
     * 4 ：提交修后的密码
     */
    private void handlePasswordCallbacks(String reply) {
        switch (reply) {
            case "00":

                //以下用来跳过设密码
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !newVersion) {
                    Log.e(TAG, "handlePasswordCallbacks:  " + "  -->  跳过设置密码  ");
                    //直接使用 密码 000000 连接
                    DeviceInfo newDeviceInfo = new DeviceInfo();
                    newDeviceInfo.setDeviceAddress(device.getAddress());
                    newDeviceInfo.setDeviceName(device.getName());
                    newDeviceInfo.setIsFirst(true);

                    newDeviceInfo.setPassword(DEFAULT_PASSWORD);
                    newDeviceInfo.setIsConnected(true);
                    MyDeviceDao.insertDevice(newDeviceInfo);

                    EventBus.getDefault().post(new MessageEvent(ACTION_STATUS_CONNECTED_OK));
                    Log.d(TAG, "onResponse :  成功   -->   正常连接  开始查询数据 ");
                    byte[] getRealName = new byte[]{0x55, (byte) 0xFF, 0x02, 0x01, 0x01};
                    mClient.write(device.getAddress(), C_UUID_Service_SendDataToDevice, C_UUID_Character_SendDataToDevice, getRealName, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {

                        }
                    });

                    break;
                }

                if (commit_amount == 0) {
                    ifFirst();
                } else if (commit_amount == 1) {
                    Log.d(TAG, "onResponse: " + "    产品密码返回正确  ");
                    ifFirst();
                } else if (commit_amount == 3) {
                    ifFirst();
                } else if (commit_amount == 2) {
                    //不会执行到这里
                } else if (commit_amount == 4) {
                    // 保存设备信息   执行正常通讯
                    DeviceInfo newDeviceInfo = new DeviceInfo();
                    newDeviceInfo.setDeviceAddress(device.getAddress());
                    newDeviceInfo.setDeviceName(device.getName());
                    newDeviceInfo.setIsFirst(true);

                    newDeviceInfo.setPassword(createPassword.toString());
                    newDeviceInfo.setIsConnected(true);
                    MyDeviceDao.insertDevice(newDeviceInfo);
                    EventBus.getDefault().post(new MessageEvent(ACTION_STATUS_CONNECTED_OK));
                    Log.d(TAG, "onResponse :  成功   -->   正常连接  开始查询数据 ");
                    byte[] getRealName = new byte[]{0x55, (byte) 0xFF, 0x02, 0x01, 0x01};
                    mClient.write(device.getAddress(), C_UUID_Service_SendDataToDevice, C_UUID_Character_SendDataToDevice, getRealName, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {

                        }
                    });
                }

                break;

            case "01":

                if (commit_amount == 0) {
                    // 初次密码 000000 错误 提交产品密码  135246
                    Log.d(TAG, "handlePasswordCallbacks: " + " 初次密码 000000 错误  提交产品密码  135246");
                    commit_amount = 1;
                    String dpw = YIHI_DEFAULT_PASSWORD + YIHI_DEFAULT_PASSWORD;
                    byte[] dpw_byte = dpw.getBytes();
                    Log.d(TAG, "onResponse: " + "提交了产品密码 135246");
                    mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, dpw_byte, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {

                        }
                    });
                } else if (commit_amount == 1) {
                    Log.d(TAG, "handlePasswordCallbacks: " + " 产品密码 135246  错误  接下来判断是否保存过 ");
                    List<DeviceInfo> checkDeviceInfo = MyDeviceDao.queryDevice(device.getAddress());
                    if (checkDeviceInfo.size() < 1) {
                        Log.d(TAG, "handlePasswordCallbacks: " + " 没保存过 ");
                        //  提示用户退出App  结束   步骤8
                        gameOver();
                    } else {

                        Log.d(TAG, "handlePasswordCallbacks: " + " - 保存过 - ");
                        //  删除保存的记录
                        deleteDevice(device.getAddress());
                        commit_amount = 3;
                        String defaultPassword = DEFAULT_PASSWORD + DEFAULT_PASSWORD;
                        byte[] passwordBytes = defaultPassword.getBytes();
                        mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, passwordBytes, new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if (code == REQUEST_SUCCESS) {
                                    Log.d(TAG, "onResponse: " + "  提交了密码 ！ " + DEFAULT_PASSWORD);
                                }
                            }
                        });
                    }
                } else if (commit_amount == 3) {
                    //  再次提交000000错误   退出App  结束   步骤8
                    gameOver();
                } else if (commit_amount == 2) {
                    if (reChange == 3) {
                        reChange = 0;
                        // 结束
                        Log.d(TAG, " onResponse  私人密码修改3次 失败:   断开—— ");
                        if (isConnected) {
                            mClient.disconnect(device.getAddress());
                        }

                    } else {

                        commit_amount = 2;
                        reChange++;
                        mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, mPrivatePassword, new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if (code == REQUEST_SUCCESS) {
                                    Log.d(TAG, "onResponse: " + "  提交了私人密码 ！ ");
                                }
                            }
                        });
                    }

                } else if (commit_amount == 4) {
                    if (priPass == 3) {
                        priPass = 0;
                        //结束
                        mClient.disconnect(device.getAddress());
                        Log.d(TAG, " onResponse  私人密码登录3次 失败:   断开—— ");
                    } else {

                        priPass++;
                        commit_amount = 4;
                        String newPassword = createPassword.toString() + createPassword.toString();
                        mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, newPassword.getBytes(), new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if (code == REQUEST_SUCCESS) {
                                    Log.d(TAG, "onResponse: " + "  使用私人密码登录  ！ ");
                                }
                            }
                        });
                    }
                }

                break;
            case "02":  // 修改密码成功

                if (commit_amount == 2) {
                    // 提交修改后的 新密码
                    priPass++;
                    commit_amount = 4;

                    String newPassword = createPassword.toString() + createPassword.toString();
                    mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, newPassword.getBytes(), new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {
                            if (code == REQUEST_SUCCESS) {
                                Log.d(TAG, "onResponse: " + "  使用私人密码登录  ！ ");
                            }
                        }
                    });
                }
                break;
        }
    }

    //  提示用户退出App  结束   步骤8
    private void gameOver() {
        Log.d(TAG, "onResponse  gameOver: ");
        if (isConnected) {
            mClient.disconnect(device.getAddress());
        }
        EventBus.getDefault().post(new MessageEvent(ACTION_STATUS_DISCONNECT_TIP));
    }

    private void ifFirst() {
        List<DeviceInfo> deviceInfos = MyDeviceDao.queryDevice(device.getAddress());

        if (deviceInfos.size() < 1) {
            //  是第一次连接
            mClient.read(device.getAddress(), C_UUID_Service_DeviceConfig, C_UUID_Character_Device_CustomerID, new BleReadResponse() {
                @Override
                public void onResponse(int code, byte[] data) {
                    String recogniseCode = BinaryToHexString(data);
                    if (code == REQUEST_SUCCESS) {
                        Log.d(TAG, "onResponse:  readCharacter  成功！  " + recogniseCode);
                        if (recogniseCode.equals("0601")) {
                            createPassword = new StringBuilder();
                            //  制造新密码
                            for (int i = 0; i < 6; i++) {
                                int random = (int) (Math.random() * 100);
                                if (random % 3 == 0) {
                                    //number
                                    int n = (int) (Math.random() * 10);
                                    createPassword.append(n);
                                } else if (random % 3 == 1) {
                                    //big letter
                                    int bl = (int) (Math.random() * 26) + 65;
                                    createPassword.append((char) bl);
                                } else {
                                    // small letter
                                    int sl = (int) (Math.random() * 26) + 97;
                                    createPassword.append((char) sl);
                                }
                            }
                            String newPassword = DEFAULT_PASSWORD + createPassword;
                            mPrivatePassword = newPassword.getBytes();
                            //  提交私人密码
                            commit_amount = 2;
                            reChange++;
                            mClient.write(device.getAddress(), C_UUID_Service_Password, C_UUID_Character_Password_C1, mPrivatePassword, new BleWriteResponse() {
                                @Override
                                public void onResponse(int code) {
                                    if (code == REQUEST_SUCCESS) {
                                        Log.d(TAG, "onResponse: " + "  提交了私人密码 ！ ");
                                    }
                                }
                            });

                        } else {
                            //  不是本公司产品断开
                            Log.d(TAG, "onResponse: " + "  断开 ");
                            if (isConnected) {
                                mClient.disconnect(device.getAddress());
                            }

                        }

                    }
                }
            });
        } else {
            //  不是第一次登录，执行正常通讯  getRealName
            EventBus.getDefault().post(new MessageEvent(ACTION_STATUS_CONNECTED_OK));
            Log.d(TAG, "onResponse :  成功   -->   正常连接  开始查询数据 ");
            byte[] getRealName = new byte[]{0x55, (byte) 0xFF, 0x02, 0x01, 0x01};
            mClient.write(device.getAddress(), C_UUID_Service_SendDataToDevice, C_UUID_Character_SendDataToDevice, getRealName, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {

                }
            });
        }
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
        Log.d(TAG, "onCreateS:  蓝牙基础服务创建  + " + "   蓝牙初始化 --> " + initialize + "   " + mClient);


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
                .setConnectRetry(2)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(15000)
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
            Log.d(TAG, "onConnectStatusChanged: " + status + "   mac : " + mac);
            //mConnect 判断连接~
            isConnected = (status == Constants.STATUS_CONNECTED);
            if (status == STATUS_CONNECTED) {
                EventBus.getDefault().post(new MessageEvent(ACTION_STATUS_CONNECTED));
            } else if (status == STATUS_DISCONNECTED) {
                EventBus.getDefault().post(new MessageEvent(ACTION_STATUS_DISCONNECT));
            }
        }
    };

}
