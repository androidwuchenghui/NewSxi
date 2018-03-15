package wu.chengsiyi.com.newsxi;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.chaychan.library.BottomBarLayout;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import wu.chengsiyi.com.adapter.BaseFragmentAdapter;
import wu.chengsiyi.com.fragment.HomeFragment;
import wu.chengsiyi.com.fragment.ThreeFragment;
import wu.chengsiyi.com.fragment.TwoFragment;
import wu.chengsiyi.com.service.BluetoothLeService;

public class MainActivity extends FragmentActivity {
    //    private BottomTabBar mBottomTabBar;

    private ViewPager mVpContent;
    private BottomBarLayout mBottomBarLayout;

    private List<Fragment> fragmentList = new ArrayList<>();
    private HomeFragment homeFragment;
    private TwoFragment twoFragment;
    private ThreeFragment threeFragment;

    //ble服务
    private BluetoothLeService mBluetoothLeService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen(this);
        setContentView(R.layout.activity_main);

        checkPermissions();//权限检查

        judgeBluetooth(); // 打开蓝牙

        initView();

        initFragment(savedInstanceState);

        initListener();

        bindBleService();//绑定服务

    }

    private void bindBleService() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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

    private void checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermissions = new RxPermissions(this);

            boolean granted1 = rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION);
            boolean granted2 = rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            boolean granted3 = rxPermissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
            Log.d("rxPermissions", "checkPermissions:  位置： "+granted1+"  写： "+granted2+"  读：  "+granted3);
            rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) .subscribe(granted -> {
                if (granted) { // Always true pre-M
                    // I can control the camera now
                    Log.d("rxPermissions", "granted:   OK   ");
                } else {
                    // Oups permission denied
                    Log.d("rxPermissions", "granted:   NO  ");
                }
            });
        }
    }

    private void judgeBluetooth() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();

            // finish();
            return;
        }
        //            mBluetoothAdapter.enable();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

    }

    private void initFragment(Bundle savedInstanceState) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState != null) {
            homeFragment = (HomeFragment) getSupportFragmentManager().getFragment(savedInstanceState, "homeFragment");
            twoFragment = (TwoFragment) getSupportFragmentManager().getFragment(savedInstanceState, "twoFragment");
            threeFragment = (ThreeFragment) getSupportFragmentManager().getFragment(savedInstanceState, "threeFragment");

        } else {
            homeFragment = new HomeFragment();
            twoFragment = new TwoFragment();
            threeFragment = new ThreeFragment();

        }

        fragmentList.add(homeFragment);
        fragmentList.add(twoFragment);
        fragmentList.add(threeFragment);

    }

    private void initListener() {
        mVpContent.setAdapter(new BaseFragmentAdapter(getSupportFragmentManager(),fragmentList));

        mVpContent.setOffscreenPageLimit(4);
        mBottomBarLayout.setViewPager(mVpContent);

    }


    private void initView() {
        mVpContent = (ViewPager) findViewById(R.id.vp_content);
        mBottomBarLayout = (BottomBarLayout) findViewById(R.id.bbl);

    }



    private void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                Window window = activity.getWindow();
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                //导航栏颜色也可以正常设置
                //                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = activity.getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                attributes.flags |= flagTranslucentStatus;
                //                attributes.flags |= flagTranslucentNavigation;
                window.setAttributes(attributes);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        FragmentManager manager = getSupportFragmentManager();
        if (homeFragment.isAdded()) {
            manager.putFragment(outState, "homeFragment", homeFragment);
        }
        if (twoFragment.isAdded()) {
            manager.putFragment(outState, "twoFragment", twoFragment);
        }
        if (threeFragment.isAdded()) {
            manager.putFragment(outState, "threeFragment", threeFragment);
        }

    }
}
