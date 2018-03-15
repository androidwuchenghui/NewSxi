package wu.chengsiyi.com.base;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.BluetoothContext;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.utils.HttpLog;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import wu.chengsiyi.com.database.DaoMaster;
import wu.chengsiyi.com.database.DaoSession;
import wu.chengsiyi.com.database.DeviceInfoDao;

/**
 * APPLICATION
 */
public class BaseApplication extends Application {

    private static Application app = null;

    private static DeviceInfoDao mDeviceInfoDao;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        EasyHttp.init(this);

        String Url = "https://www.yihisxminiid.com/";
        EasyHttp.getInstance().debug("EasyHttp", true);
        BluetoothContext.set(this);

         mClient = new BluetoothClient(this);

        /**初始化数据库*/
        setupDatabase();

    }

    private void setupDatabase() {
        //创建数据库 device.db
        DaoMaster.DevOpenHelper mHelper = new DaoMaster.
                DevOpenHelper(getAppContext(), "device.db", null);
        //获取可写数据库
        SQLiteDatabase db = mHelper.getWritableDatabase();
        //获取数据库对象
        DaoMaster master = new DaoMaster(db);
        //获取Dao对象管理者
        DaoSession  daoSession = master.newSession();

        mDeviceInfoDao = daoSession.getDeviceInfoDao();
    }


    public class UnSafeHostnameVerifier implements HostnameVerifier {
        private String host;

        public UnSafeHostnameVerifier(String host) {
            this.host = host;
            HttpLog.i("###############　UnSafeHostnameVerifier " + host);
        }

        @Override
        public boolean verify(String hostname, SSLSession session) {
            HttpLog.i("############### verify " + hostname + " " + this.host);
            if (this.host == null || "".equals(this.host) || !this.host.contains(hostname))
                return false;
            return true;
        }
    }

    /**
     * 获取Application的Context
     **/
    public static Context getAppContext() {
        if (app == null)
            return null;
        return app.getApplicationContext();
    }

    private static volatile  BluetoothClient mClient=null;

    public static  BluetoothClient getBluetoothClient(){
        if(mClient==null){
            synchronized(BluetoothClient.class){
                if(mClient==null){
                    mClient=new BluetoothClient (getAppContext());
                }
            }
        }
        return mClient;
    }

    public static DeviceInfoDao getDao(){
        return mDeviceInfoDao;
    }

}
