package cn.geobeans.biathlon.bluetooth;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minew.modulekit.MTModule;
import com.minew.modulekit.MTModuleManager;
import com.minew.modulekit.enums.BluetoothState;
import com.minew.modulekit.enums.ConnectionState;
import com.minew.modulekit.interfaces.ModuleChangeConnection;
import com.minew.modulekit.interfaces.ScanMTModuleCallback;

import java.util.LinkedList;
import java.util.List;

import cn.geobeans.biathlon.MainShow;
import cn.geobeans.biathlon.R;
import cn.geobeans.biathlon.utils.PreferencesUtil;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_COARSE_LOCATION = 122;
    private final int REQUEST_ENABLE_BT = 123;
    private RecyclerView mRecyclerView;
    private RecycleAdapter mAdapter;
    private TextView mStart_scan;
    private EditText mFilterEdit;
    private Button mFilterBtn;
    private ProgressDialog progressDialog;
    private MTModuleManager mtModuleManager;
    private boolean isScanning;
    private String mFilterText = "";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (!ensureBleExists())
            finish();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }

        initView();

        initManager();
        initPermission();

        initListener();

    }

    private void initView() {

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        mStart_scan = findViewById(R.id.start_scan);


        mRecyclerView = findViewById(R.id.recycle);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager
                .HORIZONTAL));

//        mFilterBtn = findViewById(R.id.filter_btn);
//        mFilterEdit = findViewById(R.id.filter_edit);


        dialogshow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initManager();
    }


    private void initManager() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                Log.i("Thread", "initManager: "+Thread.currentThread().getId());
//
//                Looper.loop();
//            }
//        }).start();

        mtModuleManager = MTModuleManager.getInstance(MainActivity.this);
        MTModuleManager.getInstance(this).setModuleChangeConnection(new ModuleChangeConnection() {
            @Override
            public void onDeviceChangeStatus(final MTModule device, ConnectionState status) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                switch (status) {
                    case DeviceLinkStatus_Connected:
                        Intent intent = new Intent(MainActivity.this, MainShow.class);
                        //Intent intent = new Intent(MainActivity.this, TestActiviy.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                        MTOperate.getInstance().setMtModule(device);
                        PreferencesUtil.getInstance().saveParam("deviceName", device.getName());
                        break;
                    case DeviceLinkStatus_ConnectFailed:
                    case DeviceLinkStatus_Disconnect:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Connect fail!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        });

        BluetoothState bluetoothState = MTModuleManager.getInstance(this).checkBluetoothState();
        switch (bluetoothState) {
            case BluetoothStateNotSupported:
                break;
            case BluetoothStatePowerOff:
                break;
            case BluetoothStatePowerOn:
//                mtModuleManager.startScan(scanMTModuleCallback);
                break;
        }
//        mFilterBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isScanning) {
//                    isScanning = false;
//                    mtModuleManager.stopScan();
//                }
//            }
//        });
    }

    ScanMTModuleCallback scanMTModuleCallback = new ScanMTModuleCallback() {
        @Override
        public void onScannedMTModule(LinkedList<MTModule> linkedList) {
            List<MTModule> list = new LinkedList<>();

            for (MTModule mtModule : linkedList) {
                if (mtModule.getMacAddress().contains(mFilterText)) {
                    list.add(mtModule);
                }
            }

//            list.addAll(linkedList);
            mAdapter.setData(list);
        }
    };

    private void initListener() {
        mAdapter.setOnItemClickListener(new RecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                progressDialog.setMessage(getString(R.string.connecting)
                        + mAdapter.getData(position).getName());
                progressDialog.show();
                MTModule mtModule = mAdapter.getData(position);
                mtModuleManager.connect(mtModule);

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mStart_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, HomeActiviy.class);
//                startActivity(intent);
                scan();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void scan() {
        if (isScanning) {
            isScanning = false;
            mStart_scan.setText("开始");
            if (mtModuleManager != null) {
                mtModuleManager.stopScan();
            }
        } else {
            verifyStoragePermissions();

        }
    }


    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
        }
    }

    public void verifyStoragePermissions() {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            } else {
                isScanning = true;
                mStart_scan.setText("停止");
                try {
                    mtModuleManager.startScan(scanMTModuleCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_COARSE_LOCATION:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mtModuleManager.startScan(scanMTModuleCallback);
                    }
                }, 1000);

                break;
            case REQUEST_EXTERNAL_STORAGE:
                switch (permissions[0]) {
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            isScanning = true;
                            mStart_scan.setText("停止");
                            try {
                                mtModuleManager.startScan(scanMTModuleCallback);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
                break;
        }
    }

    protected void dialogshow() {
        progressDialog = new ProgressDialog(MainActivity.this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Connecting...");
    }


    private boolean ensureBleExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this, "Phone does not support BLE", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "您当前手机不支持蓝牙！", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
}
