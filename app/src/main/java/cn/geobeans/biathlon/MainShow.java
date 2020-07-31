package cn.geobeans.biathlon;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.geobeans.biathlon.utils.ToastUtils;

import static cn.geobeans.biathlon.utils.FileUtil.addTxtToFileBuffered;

public class MainShow extends BaseActivity {
    private float FLT_EPSILON = 0.00001f;
    private SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd");

    private Long tempSeq = -1L;
    private DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private List<PanelView> mPanels = new ArrayList<>();

    private TextView mAver_Qiwen;
    private TextView mAver_Qiya;
    private Float m_fQiwen = 0.0f;
    private Float m_fQiya = 0.0f;

    private float m_angleTst = 0.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_show);
        mActivityList.add(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        PanelView panel1 = findViewById(R.id.panel_1);
        mPanels.add(panel1);
        PanelView panel2 = findViewById(R.id.panel_2);
        mPanels.add(panel2);
        PanelView panel3 = findViewById(R.id.panel_3);
        mPanels.add(panel3);
        PanelView panel4 = findViewById(R.id.panel_4);
        mPanels.add(panel4);
        PanelView panel5 = findViewById(R.id.panel_5);
        mPanels.add(panel5);

        mAver_Qiwen = findViewById(R.id.average_temp);
        mAver_Qiya = findViewById(R.id.average_pressure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.app_setup:
//                mPanels.get(0).setAngle(m_angleTst);
//                mPanels.get(0).invalidate();
//                m_angleTst += 10.0f;
                break;
            case R.id.app_exit:
                break;
            default:
        }
        return true;
    }

    @Override
    public void result(final String[] data) {
        if (Long.parseLong(data[5], 16) == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parseFS_FX(data);
                }
            });
        }
    }

    private String getNum(double a) {
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        String p = decimalFormat.format(a);
        return p;
    }

    private boolean fillWind(String[] data,StringBuffer stringBuffer,int data_index,int panel_index)
    {
        //风速
        float x11 = Float.intBitsToFloat(new BigInteger(data[data_index+3] + data[data_index+2] + data[data_index+1] + data[data_index], 16).intValue());
        //风向
        float x12 = Float.intBitsToFloat(new BigInteger(data[data_index+7] + data[data_index+6] + data[data_index+5] + data[data_index+4], 16).intValue());
        //温度
        float x13 = Float.intBitsToFloat(new BigInteger(data[data_index+11] + data[data_index+10] + data[data_index+9] + data[data_index+8], 16).intValue());
        //气压
        float x14 = Float.intBitsToFloat(new BigInteger(data[data_index+15] + data[data_index+14] + data[data_index+13] + data[data_index+12], 16).intValue());
        //湿度
        float x15 = Float.intBitsToFloat(new BigInteger(data[data_index+19] + data[data_index+18] + data[data_index+17] + data[data_index+16], 16).intValue());

        String fs = getNum(x11);
        String fx = getNum(x12);
        stringBuffer.append( fs + ";" + fx + ";");
        mPanels.get(panel_index).setWindSpeedString(fs);
        mPanels.get(panel_index).setAngle(x12);
        mPanels.get(panel_index).invalidate();

        if(Math.abs(x13 - 0.0f) < FLT_EPSILON && Math.abs(x14 - 0.0f) < FLT_EPSILON) {
            return  false;
        }else {
            m_fQiwen += x13;
            m_fQiya += x14;
            return true;
        }
    }

    private void parseFS_FX(String[] data) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(dateformat1.format(System.currentTimeMillis()) + " ");//日期
            stringBuffer.append(dateformat.format(System.currentTimeMillis()) + ";");//时间

            long targetId = Long.parseLong(data[0], 16);
            int sourceId = Integer.parseInt(data[1], 16);
            long sequence = Long.parseLong(data[2], 16);
            //Log.e("dongaohui: ", data[0]+data[1]+data[2]);

            long length = Long.parseLong(data[3], 16);
            long batteryLevel = Long.parseLong(data[4], 16);
            //过滤重复数据
            if (tempSeq == sequence) {
                return;
            }
            tempSeq = sequence;

            long cmd = Long.parseLong(data[5], 16);
            long totalSensorNum = Long.parseLong(data[6], 16);
            if (totalSensorNum == 0||totalSensorNum==1||totalSensorNum==2)
                return;
            if (totalSensorNum == 5) {
                m_fQiwen = 0.0f;
                m_fQiya = 0.0f;
                int iNum = 0;
                if(fillWind(data,stringBuffer,7,0))
                    iNum++;
                if(fillWind(data,stringBuffer,27,1))
                    iNum++;
                if(fillWind(data,stringBuffer,47,2))
                    iNum++;
                if(fillWind(data,stringBuffer,67,3))
                    iNum++;
                if(fillWind(data,stringBuffer,87,4))
                    iNum++;

                if(iNum!=0) {
                    m_fQiwen /= iNum;
                    m_fQiya /= iNum;
                }else{
                    m_fQiwen = 0.0f;
                    m_fQiya = 0.0f;
                }
                String strQiwen = getNum(m_fQiwen);
                String strQiya = getNum(m_fQiya);
                stringBuffer.append( strQiwen + ";" + strQiya + ";");
                mAver_Qiwen.setText("气温: "+strQiwen+"°C");
                mAver_Qiya.setText("气压: "+strQiya+"帕");
//                //风速
//                float x11 = Float.intBitsToFloat(new BigInteger(data[10] + data[9] + data[8] + data[7], 16).intValue());
//                //风向
//                float x12 = Float.intBitsToFloat(new BigInteger(data[14] + data[13] + data[12] + data[11], 16).intValue());
//                //温度
//                float x13 = Float.intBitsToFloat(new BigInteger(data[18] + data[17] + data[16] + data[15], 16).intValue());
//                //气压
//                float x14 = Float.intBitsToFloat(new BigInteger(data[22] + data[21] + data[20] + data[19], 16).intValue());
//                //湿度
//                float x15 = Float.intBitsToFloat(new BigInteger(data[26] + data[25] + data[24] + data[23], 16).intValue());


            }
            addTxtToFileBuffered(stringBuffer.toString());
        }catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast("数据解析出错");
        }

    }
}