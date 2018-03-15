package wu.chengsiyi.com.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Administrator on 2018/3/13 0013.
 */
@Entity
public class DeviceInfo {

    @Id(autoincrement = true)
    private Long id;
    //string 属性
    private String deviceName;
    @Unique
    private String deviceAddress;

    private String password;
    private String realName ;
    private String deviceID;
    private String softVision;
    //布尔属性
    private boolean lastConnect = false;
    private boolean isFirst = true;
    private boolean isConnected = false;
    @Generated(hash = 880125492)
    public DeviceInfo(Long id, String deviceName, String deviceAddress,
            String password, String realName, String deviceID, String softVision,
            boolean lastConnect, boolean isFirst, boolean isConnected) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.password = password;
        this.realName = realName;
        this.deviceID = deviceID;
        this.softVision = softVision;
        this.lastConnect = lastConnect;
        this.isFirst = isFirst;
        this.isConnected = isConnected;
    }
    @Generated(hash = 2125166935)
    public DeviceInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDeviceName() {
        return this.deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getDeviceAddress() {
        return this.deviceAddress;
    }
    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRealName() {
        return this.realName;
    }
    public void setRealName(String realName) {
        this.realName = realName;
    }
    public String getDeviceID() {
        return this.deviceID;
    }
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
    public String getSoftVision() {
        return this.softVision;
    }
    public void setSoftVision(String softVision) {
        this.softVision = softVision;
    }
    public boolean getLastConnect() {
        return this.lastConnect;
    }
    public void setLastConnect(boolean lastConnect) {
        this.lastConnect = lastConnect;
    }
    public boolean getIsFirst() {
        return this.isFirst;
    }
    public void setIsFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }
    public boolean getIsConnected() {
        return this.isConnected;
    }
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

}
