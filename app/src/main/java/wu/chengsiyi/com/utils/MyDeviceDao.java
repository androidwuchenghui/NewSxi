package wu.chengsiyi.com.utils;

import java.util.List;

import wu.chengsiyi.com.base.BaseApplication;
import wu.chengsiyi.com.database.DeviceInfo;
import wu.chengsiyi.com.database.DeviceInfoDao;

/**
 * Created by Administrator on 2018/3/13 0013.
 */

public class MyDeviceDao {

    // 插入 device.db 数据库
    public static void insertDevice(DeviceInfo deviceInfo) {
        BaseApplication.getDao().insertOrReplace(deviceInfo);
    }
    // 查询 device.db 数据库
    public static List<DeviceInfo> queryDevice(String mac) {
        return BaseApplication.getDao().queryBuilder().where(DeviceInfoDao.Properties.DeviceAddress.eq(mac)).list();
    }
    // 修改 数据库
    public static void updateDevice(DeviceInfo deviceInfo) {
        BaseApplication.getDao().update(deviceInfo);
    }
    // 删除 数据库
    public static void deleteDevice(String mac) {
        List<DeviceInfo> list = queryDevice(mac);
        for (DeviceInfo deviceInfo : list) {
            BaseApplication.getDao().delete(deviceInfo);
        }
    }
}
