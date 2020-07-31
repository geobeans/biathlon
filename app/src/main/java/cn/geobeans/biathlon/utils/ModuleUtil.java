package cn.geobeans.biathlon.utils;

import android.util.Log;

import com.minew.modulekit.MTModule;
import com.minew.modulekit.MTModuleManager;
import com.minew.modulekit.ModuleException;
import com.minew.modulekit.enums.ConnectionState;
import com.minew.modulekit.interfaces.MTModuleListener;
import com.minew.modulekit.interfaces.ModuleChangeConnection;
import com.minew.modulekit.interfaces.WriteCallback;
import cn.geobeans.biathlon.bluetooth.MTOperate;

import cn.geobeans.biathlon.App;

/**
 * @Author: baixm
 * @Date: 2019/12/24
 */
public class ModuleUtil {
    // Aa 01 55 46 64 00 03 01 f5 02 08 00 91 01 16 f5 20 0b 3e 08 00 f5 01 16 9f ee 4e 43 02 f4 02 08 00 91 01 16 f5 20 0b 3e 08 00 F5 01 16 9f ee 4e 43 03 ed 02 08 00 91 01 16 c4 20 f9 3c 08 00 f5 01 16 98 19 b4 42 7e d8

    private volatile static ModuleUtil instance = null;
    private MTModuleManager mtModuleManager;
    private MTModule mtModule;
    private CallBackInterface callBack;

    private ModuleUtil() {
        mtModuleManager = MTModuleManager.getInstance(App.mContext);
        mtModule = MTOperate.getInstance().getMtModule();
        initListener();
    }

    public static ModuleUtil getInstance() {
        if (instance == null) {
            synchronized (ModuleUtil.class) {
                if (instance == null) {
                    instance = new ModuleUtil();
                }
            }
        }
        return instance;
    }

    private void initListener() {
        mtModuleManager.setModuleChangeConnection(new ModuleChangeConnection() {
            @Override
            public void onDeviceChangeStatus(final MTModule device, ConnectionState status) {
                switch (status) {
                    case DeviceLinkStatus_Connected:
                        break;
                    case DeviceLinkStatus_ConnectFailed:
                    case DeviceLinkStatus_Disconnect:
                        disconnect();
                        break;
                }
            }
        });
        if (mtModule != null) {
            mtModule.setMTModuleListener(new MTModuleListener() {
                @Override
                public void didReceiveData(final byte[] bytes) {
                    //Log.i("Thread", "Recieve Thread: "+Thread.currentThread().getId());
                    callBack.execute(bytes);
                }
            });
        }
    }

    public void disconnect() {
        if (mtModuleManager != null) {
            mtModuleManager.disconnect(mtModule);
        }
    }

    /**
     * 字符串转换为16进制字符串
     *
     * @param s
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * 16进制表示的字符串转换为字节数组
     *
     * @param s 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }

        return b;
    }

    public synchronized void writeData(final String data) {
        byte[] bytes = hexStringToByteArray(data);
        Log.e("write", "write: 成功" + data);
//        String hexStr = Util.byte2HexStr(bytes);
//        System.out.println("##### "+hexStr);
//        String str1 = Util.hexStr2Str(hexStr);
//        System.out.println(str1);
        mtModule.writeData(bytes, new WriteCallback() {
            @Override
            public void write(final boolean b, ModuleException e) {
                if (b) {
                    Log.e("write", "write: 成功" + data);
                } else
                    Log.e("write", "write: 失败" + data);
            }
        });
    }

    public void setOnCallBackInterface(CallBackInterface callBack) {
        this.callBack = callBack;
    }

    public interface CallBackInterface {
        void execute(byte[] bytes);
    }
}