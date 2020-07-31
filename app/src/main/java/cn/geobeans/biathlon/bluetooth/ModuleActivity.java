package cn.geobeans.biathlon.bluetooth;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.minew.modulekit.MTModule;
import com.minew.modulekit.MTModuleManager;
import com.minew.modulekit.ModuleException;
import com.minew.modulekit.enums.ConnectionState;
import com.minew.modulekit.interfaces.MTModuleListener;
import com.minew.modulekit.interfaces.ModuleChangeConnection;
import com.minew.modulekit.interfaces.WriteCallback;

import cn.geobeans.biathlon.R;
import cn.geobeans.biathlon.utils.Util;


public class ModuleActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private MTModuleManager mtModuleManager;
    private MTModule mtModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        initView();
        initManager();
        initListener();
    }

    private void initView(){
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = findViewById(R.id.module_edit);
        button = findViewById(R.id.module_btn);

    }

    private void initManager(){
        mtModuleManager = MTModuleManager.getInstance(this);
        mtModule= MTOperate.getInstance().getMtModule();
    }

    private void initListener(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mtModule!=null) {
                    String text=editText.getText().toString();
                    byte[] data = text.getBytes();
                    mtModule.writeData(data, new WriteCallback() {
                        @Override
                        public void write(final boolean b, ModuleException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (b){
                                        Toast.makeText(ModuleActivity.this,"Write success!", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(ModuleActivity.this,"Write fail!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                }
            }
        });

        if (mtModule!=null){
            mtModule.setMTModuleListener(new MTModuleListener() {
                @Override
                public void didReceiveData(final byte[] bytes) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data=new String(bytes);

                            Long l = Util.parseLong("5decb73d", 16);
                            Float f = Float.intBitsToFloat(l.intValue());
                            Log.e("----",data+"----"+f);
//                            Toast.makeText(ModuleActivity.this,"Receive data="+f,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }


        mtModuleManager.setModuleChangeConnection(new ModuleChangeConnection() {
            @Override
            public void onDeviceChangeStatus(final MTModule device, ConnectionState status) {
                switch (status) {
                    case DeviceLinkStatus_Connected:
                        break;
                    case DeviceLinkStatus_ConnectFailed:
                    case DeviceLinkStatus_Disconnect:
                        //Log.i("module_tag","status="+status.name());
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void finish() {
        super.finish();
        mtModuleManager.disconnect(mtModule);
    }



}
