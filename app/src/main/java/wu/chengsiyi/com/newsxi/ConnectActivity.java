package wu.chengsiyi.com.newsxi;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import wu.chengsiyi.com.constant.MessageEvent;
import wu.chengsiyi.com.service.BluetoothLeService;
import wu.chengsiyi.com.widget.refresh_listview.XupListView;

import static wu.chengsiyi.com.base.BaseApplication.getBluetoothClient;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_CONNECTED;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_CONNECTED_OK;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_DISCONNECT;
import static wu.chengsiyi.com.constant.Constants.ACTION_STATUS_DISCONNECT_TIP;

/**
 * Created by ${Wu} on 2018/3/5.
 */

public class ConnectActivity extends AppCompatActivity implements XupListView.IXListViewListener {
    private static final String TAG = "ConnectActivity";
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean isScanning;

    @BindView(R.id.device_lv)
    XupListView xlv;
    private BluetoothClient mClient;
    //dialog
    private ProgressDialog dialog;

    //ble服务
    private BluetoothLeService mBluetoothLeService;

    private Handler mHandler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        //  eventBus 注册
        EventBus.getDefault().register(this);

        init();

    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("service", "Unable to initialize Bluetooth");
                finish();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void init() {
        mHandler = new Handler();
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置进度条的样式

        dialog.setCanceledOnTouchOutside(false);
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        xlv.setAdapter(mLeDeviceListAdapter);

        // 先扫BLE设备3次，每次3s

        mClient = getBluetoothClient();

        xlv.setPullRefreshEnable(true);
        xlv.setPullLoadEnable(true);
        xlv.setAutoLoadEnable(true);
        xlv.setXListViewListener(this);
        scanLeDevice(true);
    }
    private String getTime() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(new Date());
    }
    @Override
    protected void onResume() {
        super.onResume();

    }
    //下拉刷新
    @Override
    public void onRefresh() {

        scanLeDevice(true);
        Log.d("rxPermissions", "onRefresh: ");
    }

    @Override
    public void onLoadMore() {

    }


    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = ConnectActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();

            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
                viewHolder.deviceAddress.setText(device.getAddress());
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
                viewHolder.deviceAddress.setText(device.getAddress());
            }

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    dialog.setMessage(ConnectActivity.this.getString(R.string.connecting));
                    dialog.show();
                    mBluetoothLeService.setDevice(device);
                }
            });

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mClient.stopSearch();
    }

    private void scanLeDevice(final boolean enable) {
        xlv.setRefreshTime(getTime());
        if(enable){
            mClient.search( new SearchRequest.Builder()
                    .searchBluetoothLeDevice(4000)   // 先扫BLE设备3次，每次3s
                    .build(), new SearchResponse() {
                @Override
                public void onSearchStarted() {
                    mLeDeviceListAdapter.clear();
                    dialog.setMessage(ConnectActivity.this.getString(R.string.searching));
                    dialog.show();
                    Log.d("rxPermissions", "onSearchStarted: ");
                }

                @Override
                public void onDeviceFounded(SearchResult device) {

                    Beacon beacon = new Beacon(device.scanRecord);
                    BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
                    mLeDeviceListAdapter.addDevice(device.device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    Log.d("rxPermissions", "onDeviceFounded: "+device.toString());
                }

                @Override
                public void onSearchStopped() {
                    Log.d("rxPermissions", "onSearchStopped: ");
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    xlv.stopRefresh();
                }

                @Override
                public void onSearchCanceled() {
                    Log.d("rxPermissions", "onSearchCanceled: ");
                }
            });
        }else {
            mClient.stopSearch();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.stopSearch();
        EventBus.getDefault().unregister(this);
        unbindService(mServiceConnection);
    }
    // 接收EventBus
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onMoonEvent(MessageEvent messageEvent){
        Log.d(TAG, "onMoonEvent: "+messageEvent.getMessage()+"    ms:  "+messageEvent);
        switch (messageEvent.getMessage()){
            case ACTION_STATUS_CONNECTED:

                if(!dialog.isShowing()){
                    dialog.setMessage(ConnectActivity.this.getString(R.string.connecting));
                    dialog.show();
                }
            break;
            case ACTION_STATUS_DISCONNECT_TIP:

                if(dialog.isShowing()){
                    dialog.dismiss();
                }

                new AlertDialog.Builder(ConnectActivity.this)
                        .setIcon(R.mipmap.bt_fankui)
                        .setTitle(R.string.point_out_title)
                        .setMessage(ConnectActivity.this.getString(R.string.point_out_information_2))
                        .setCancelable(false)
                        .setNegativeButton(R.string.close_the_tip, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ConnectActivity.this.finish();
                            }
                        })
                        .create().show();

                break;
            case ACTION_STATUS_DISCONNECT:

                if(dialog.isShowing()){
                    dialog.setMessage(ConnectActivity.this.getString(R.string.disConnect));
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(dialog.isShowing()){

                                dialog.dismiss();
                            }
                        }
                    },1000);
                }
                break;
            case ACTION_STATUS_CONNECTED_OK:

                if(dialog.isShowing()){
                    dialog.setMessage(ConnectActivity.this.getString(R.string.conSuccess));
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(dialog.isShowing()){
                                dialog.dismiss();
                                ConnectActivity.this.finish();
                            }
                        }
                    },1500);
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
}
