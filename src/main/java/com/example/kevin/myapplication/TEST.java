package com.example.kevin.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class TEST extends AppCompatActivity {

    private Switch Switch;
    private ToggleButton BTList;
    private ListView Listview;
    private TextView text0;
//    private ArrayList<String> List = new ArrayList<>();
//    private ArrayAdapter ListAdapter;
//    private MenuItem add_item, search_item, revert_item, share_item, delete_item;       // 選單項目物件

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;

    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothSocket mBTSocket;
    public ConnectedThread mConnectedThread;


    private void BTOn(View view){
        if(!BA.isEnabled()){                                                                        /////檢視藍芽是否開始
            Intent turnonBT = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);          /////開啟藍芽
            startActivityForResult(turnonBT, 0);
            Toast.makeText(getApplicationContext(),"Requesting",Toast.LENGTH_LONG).show();
        }
        else{                                                                                        /////藍芽已開啟
            Toast.makeText(getApplicationContext(),"Already on",Toast.LENGTH_LONG).show();
        }
    }

    private void BTOff(View view){
        BA.disable();                                                                              /////關閉藍芽
        Toast.makeText(getApplicationContext(),"Turned off" ,Toast.LENGTH_LONG).show();
    }

    private void BTList(View view){                                                                 /////列出已配對的裝置
        Listview.setVisibility(View.VISIBLE);
        pairedDevices = BA.getBondedDevices();
        final ArrayList<String> list = new ArrayList<>();
        for(BluetoothDevice bt : pairedDevices)                                                     /////列出配對藍芽裝置的名稱
            list.add(bt.getName() + "\n" + bt.getAddress());

        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        Listview.setAdapter(adapter);
    }

    public AdapterView.OnItemClickListener OnItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            /////如果藍芽沒開啟顯示通知
            if(!BA.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on",Toast.LENGTH_SHORT).show();
                return;
            }

            // 獲取裝置的MAC碼
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);

            new Thread(){
                public void run() {
                    boolean fail = false;
                    //取得裝置MAC找到連接的藍芽裝置
                    BluetoothDevice device = BA.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                        //建立藍芽socket
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed",Toast.LENGTH_SHORT).show();
                    }

                    try {
                        mBTSocket.connect();
                    }
                    catch (IOException e){
                        try{
                            fail = true;
                            mBTSocket.close(); //關閉socket
                        }
                        catch(IOException e2){
                            Toast.makeText(getBaseContext(), "Socket creation failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!fail){
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                    }
                }
            }.start();

        }
    };

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws
            IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100);
                        //pause and wait for rest of data
                        bytes = mmInStream.available();
                        // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes);
                        // record how many bytes we actually read
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }
    }

    private OnClickListener click = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == Switch.getId()){
                if(Switch.isChecked())              //呼叫藍芽switchch
                    BTOn(view);

                else
                    BTOff(view);

            }


            if(view.getId() == BTList.getId()) {
                if (BTList.isChecked())
                    BTList(view);  /////呼叫BTLIST

                else
                    Listview.setVisibility(View.GONE);

            }

        }

    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Switch = (Switch) findViewById(R.id.switch3);
        BTList = (ToggleButton)findViewById(R.id.toggle);
        Listview = (ListView)findViewById(R.id.List1);

        Switch.setOnClickListener(click);
        BTList.setOnClickListener(click);

        Listview.setOnItemClickListener(OnItemClickListener);

        BA = BluetoothAdapter.getDefaultAdapter();
    }

    @Override

    //離開應用程式時，把藍芽功能關閉
    protected void onDestroy()
    {
        super.onDestroy();
        BA.disable();
    }

}


