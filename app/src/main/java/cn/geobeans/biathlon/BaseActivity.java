package cn.geobeans.biathlon;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.geobeans.biathlon.utils.ModuleUtil;
import cn.geobeans.biathlon.utils.PreferencesUtil;
import cn.geobeans.biathlon.utils.ToastUtils;
import cn.geobeans.biathlon.utils.Util;

/**
 * @Author: baixm
 * @Date: 2019/12/24
 */
public abstract class BaseActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,ModuleUtil.CallBackInterface {
    public static final List<BaseActivity> mActivityList = new ArrayList<>();
    public PreferencesUtil preferencesUtil;
    public static String INDEX = "index";
    public static String ZZWGZPL = "zzwgzpl";//自组网工作频率
    public static String CGQNUM = "cgqnum";//传感器数量
    public static String SJTGNUM = "sjtgnum";//数据通道数量
    public static String SJTG = "sjtg";//数据通道
    public static String CGQID = "cgqid";//传感器ID
    public static String DQSBID = "DQSBID";//设备ID
    public static String LJLAMC = "ljlamc";//蓝牙
    private byte[] arr;

//    public Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//                    if (setCgqOk == null) {
//                        ToastUtils.showToast("设置传感器数据通道失败");
//                    } else {
//                        setCgqOk = null;
//                    }
//
//                    break;
//
//                case 3:
//                    if (setSbOk == null) {
//                        ToastUtils.showToast("设置当前设备ID失败");
//                    } else {
//                        setSbOk = null;
//                    }
//
//                    break;
//
//                case 5:
//                    if (setIdOk == null) {
//                        ToastUtils.showToast("设置传感器ID失败");
//                    } else {
//                        setIdOk = null;
//                    }
//
//                    break;
//
//                case 7:
//                    if (setLyOk == null) {
//                        ToastUtils.showToast("设置蓝牙名称失败");
//                    } else {
//                        setLyOk = null;
//                    }
//
//                    break;
//
//                case 8:
//                    if (setZzwOk == null) {
//                        ToastUtils.showToast("设置设备自组网工作频率失败");
//                    } else {
//                        setZzwOk = null;
//                    }
//
//                    break;
//
//
//            }
//            super.handleMessage(msg);
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesUtil = PreferencesUtil.getInstance();
        //Log.i("Thread", "onCreate: "+Thread.currentThread().getId());

        ModuleUtil.getInstance().setOnCallBackInterface(this);
    }

    public void init() {
        setCGQTD();
        getSBDYId();
        getSBId();
        getPL();
    }


    @Override

    public void execute(byte[] bytes) {
        String[] data = null;
        if (bytes != null) {
            if (bytes.length == 40) {
                if(arr==null)
                    arr = bytes;
                else{
                    arr = byteMergerAll(arr, bytes);
                }
                return;
            }
            if (arr != null && arr.length == 80 && bytes.length == 29) {
                data = Util.byte2HexStr(byteMergerAll(arr, bytes)).split(" ");
                arr = null;
            } else {
                data = Util.byte2HexStr(bytes).split(" ");
            }

        }

        synchronized (BaseActivity.class) {
            //基本数据6位，状态码1位，验证值2位

            //测试用 start-----------------
//            boolean bCrc = verifyData(data);
//            if(data.length>=6) {
//                m_iCurSequence = Integer.parseInt(data[2], 16);
//                m_bCrc = bCrc;
//                if (!m_bCrc)
//                    m_iCrcFailed++;
//                if (m_bFirst) {
//                    m_iPreSequence = m_iCurSequence;
//                    m_bFirst = false;
//                }
//
//                int iDiff = 0;
//                if(m_iCurSequence!=m_iPreSequence){
//                    if(m_iCurSequence>m_iPreSequence){
//                        iDiff = m_iCurSequence-m_iPreSequence;
//                    }else{
//                        iDiff = m_iCurSequence-m_iPreSequence+256;
//                    }
//                    if(iDiff>1 ){
//                        m_iPakageLost += (iDiff-1);
//                    }
//                    m_iPreSequence = m_iCurSequence;
//                }
//
//            }
            //测试用 end-----------------

            if (data != null && data.length > 6 && verifyData(data)) {
                int cmd = Integer.parseInt(data[5]);
                int result = Integer.parseInt(data[6]);
                if (cmd != 0) {
                    switch (cmd) {
                        case 1://表示设置获取传感器数据通道返回结果
                            if (result != 1) {
//                                setCgqOk = false;
                                ToastUtils.showToast("设置传感器数据通道失败");
                            } else {
//                                setCgqOk = true;
                                setData(cmd);
                                ToastUtils.showToast("设置传感器数据通道成功");
                            }
                            break;
                        case 2://表示获取传感器数据通道结果。Num为总通道数，分别是Channel1, Channel2。
                            if (result != 1 || data.length < 10) {
                                ToastUtils.showToast("获取传感器数据通道失败");
                                break;
                            }
                            int tdNum = Integer.parseInt(data[7], 16);
                            preferencesUtil.saveParam(SJTGNUM, tdNum);
                            if (data.length == tdNum * 2 + 10) {
                                for (int i = 0; i < tdNum; i++) {
                                    preferencesUtil.saveParam(SJTG + i, Integer.parseInt(data[9 + 2 * i] + data[8 + 2 * i], 16));
                                    //setData(cmd);
                                }
                                setData(cmd);
                            }
                            break;
                        case 3://设置Device ID 返回结果
                            if (result == 0) {
//                                setSbOk = false;
                                ToastUtils.showToast("设置当前设备ID失败");
                            } else {
//                                setSbOk = true;
                                setData(cmd);
                                ToastUtils.showToast("设置当前设备ID成功");
                            }
                            break;
                        case 4://用于获取前device ID 编号
                            if (result == 0 || data.length != 10) {
                                ToastUtils.showToast("获取当前设备ID失败");
                                break;
                            }
                            preferencesUtil.saveParam(DQSBID, Integer.parseInt(data[7]));
                            setData(cmd);
                            break;
                        case 5://设置传感器ID返回结果。
                            if (result != 1) {
//                                setIdOk = false;
                                ToastUtils.showToast("设置传感器ID失败");
                            } else {
//                                setIdOk = true;
                                setData(cmd);
                                ToastUtils.showToast("设置传感器ID成功");
                            }
                            break;
                        case 6://获取前设备对应的传感器ID号，Num:传感器总数。默认为3。
                            if (result != 1 || data.length < 10) {
                                ToastUtils.showToast("获取前设备对应的传感器ID失败");
                                break;
                            }
                            int num = Integer.parseInt(data[7], 16);
                            preferencesUtil.saveParam(CGQNUM, num);
                            if (data.length == num + 10) {
                                for (int i = 1; i <= num; i++) {
                                    preferencesUtil.saveParam(CGQID + i, Integer.parseInt(data[8 + i - 1], 16));
                                }
                                setData(cmd);
                            }
                            break;
                        case 7://设置蓝牙名称返回结果
                            if (result != 1) {
//                                setLyOk = false;
                                ToastUtils.showToast("设置蓝牙名称失败");
                            } else {
//                                setLyOk = true;
                                setData(cmd);
                                ToastUtils.showToast("设置蓝牙名称成功");
                            }
                            break;
                        case 8://设置设备自组网工作频率返回结果。
                            if (result != 1) {
//                                setZzwOk = false;
                                ToastUtils.showToast("设置设备自组网工作频率失败");
                            } else {
//                                setZzwOk = true;
                                setData(cmd);
                                ToastUtils.showToast("设置设备自组网工作频率成功");
                            }
                            break;
                        case 9://获取设备自组网工作频率。由高八位和低八位组成。频率范围：420-510Mhz。频率step 为1Mhz。假如频率为433Mhz 时，Frequece_L=0XB1, Frequece_H=0x01。
                            if (result != 1 || data.length != 11) {
                                ToastUtils.showToast("获取设备自组网工作频率失败");
                                break;
                            }
                            preferencesUtil.saveParam(ZZWGZPL, Integer.parseInt(data[8] + data[7], 16));
                            setData(cmd);
                            break;
                        default:
                            ToastUtils.showToast(cmd + "未定义");
                    }
                }
                for (int i = 0; i < mActivityList.size(); i++) {
                    mActivityList.get(i).result(data);
                }

            } else {
//                ToastUtils.showToast("数据错误");
                if (data!=null){
                    //lijp
                    //ToastUtils.showToast(Arrays.toString(data));
                    ToastUtils.showToast("数据错误");
                }else {
                    ToastUtils.showToast("data为null");
                }
            }
        }

    }


//    public void execute(byte[] bytes) {
//        Util.byte2HexStr(bytes);
//        return;
//    }

    public abstract void result(String[] data);

    public void setData(int cmd) {

    }

    public boolean verifyData(String[] data) {
        try {
            List<String> list = Arrays.asList(data);
            int crcCal = rs485crcCal(list, list.size() - 2);
            if (crcCal != Integer.parseInt(list.get(list.size() - 1) + list.get(list.size() - 2), 16)) {
                ToastUtils.showToast("数据CRC验证错误");
                return false;
            }
            return true;
        } catch (Exception e) {
            e.getStackTrace();
            return false;
        }
    }

    /**
     * CMD为2时，Data 为空
     * 获取前传感器数据通道
     */
    public void setCGQTD() {
        setSensorData("02", null);
    }

    /**
     * 3.1.4 CMD为4时，Data 为空：
     * 此命令用于获取前device ID 编号。
     */
    public void getSBId() {
        setSensorData("04", null);
    }

    /**
     * 此命令用于获取前设备对应的传感器ID号。
     */
    public void getSBDYId() {
        setSensorData("06", null);
    }

    /**
     * 3.1.9 CMD为9时，Data 为空：
     * 此命令用于获取前设备的自组网工作频率。
     */
    private void getPL() {
        setSensorData("09", null);
    }

    public void setSensorData(String cmd, List<String> data) {
        List<String> list = new ArrayList<>();

        list.add("BB");
        list.add("99");
        int param = (int) preferencesUtil.getParam(INDEX, 1);
        if (param >= 50) {
            param = 0;
        }
        list.add(format(param + ""));
        preferencesUtil.saveParam(INDEX, param + 1);
        list.add("00");
        list.add("55");
        list.add(cmd);
        if (data != null && data.size() > 0) {
            list.addAll(data);
        }
        list.set(3, format(list.size() + ""));
        String crc = getCrc(Integer.toHexString(rs485crcCal(list, list.size())).toUpperCase());
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            buffer.append(list.get(i));
        }
        buffer.append(crc);
        ModuleUtil.getInstance().writeData(buffer.toString());
    }

    public String getCrc(String hex) {
        String str = "";
        switch (hex.length()) {
            case 1:
                str += "0" + hex + "00";
                break;
            case 2:
                str += hex + "00";
                break;
            case 3:
                str += hex.substring(1) + "0" + hex.substring(0, 1);
                break;
            case 4:
                str += hex.substring(2) + hex.substring(0, 2);
                break;
        }
        return str;
    }

    public String format(String formatData) {
        if (formatData.length() == 1) {
            return "0" + formatData;
        } else {
            String hex = Integer.toHexString(Integer.parseInt(formatData, 10)).toUpperCase();
            if (hex.length() == 1) {
                return "0" + hex;
            }
            return hex;
        }
    }

    public int rs485crcCal(List<String> list, int ubLength) {
        int uwcrc;
        short i;
        uwcrc = 0xffff;
        for (i = 0; i < ubLength; i++) {
            uwcrc = crcCal(uwcrc, Short.parseShort(list.get(i), 16));
        }
        return uwcrc;
    }

    public byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    private int crcCal(int uwcrcbuf, short ubinput) {
        short i;
        int uwx16;
        for (i = 0; i < 8; i++) {
            if (((uwcrcbuf & 0x0001) ^ (ubinput & 0x01)) != 0) {
                uwx16 = 0x8408;
            } else {
                uwx16 = 0x0000;
            }
            uwcrcbuf = uwcrcbuf >> 1;
            uwcrcbuf ^= uwx16;
            ubinput = (short) (ubinput >> 1);
        }
        return uwcrcbuf;
    }

}
