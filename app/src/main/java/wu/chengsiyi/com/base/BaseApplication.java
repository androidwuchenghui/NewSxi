package wu.chengsiyi.com.base;

import android.app.Application;
import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.BluetoothContext;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.utils.HttpLog;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * APPLICATION
 */
public class BaseApplication extends Application {

    private static Application app = null;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        EasyHttp.init(this);

        //这里涉及到安全我把url去掉了，demo都是调试通的
        String Url = "https://www.yihisxminiid.com/";
        EasyHttp.getInstance().debug("EasyHttp", true);
        BluetoothContext.set(this);

         mClient = new BluetoothClient(this);

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
}
