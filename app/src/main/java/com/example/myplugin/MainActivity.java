package com.example.myplugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.prowl.torque.remote.ITorqueService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private final String PLUGIN_NAME = "Opel Insignia DPF";
    private final String PARAM_NAME = "name";
    private final String PARAM_SHORT_NAME = "shortName";
    private final String PARAM_MODE_AND_PID = "modeAndPID";
    private final String PARAM_EQUATION = "equation";
    private final String PARAM_MIN_VALUE = "minValue";
    private final String PARAM_MAX_VALUE = "maxValue";
    private final String PARAM_UNIT = "unit";
    private final String PARAM_HEADER = "header";
    private ITorqueService torqueService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send_button).setOnClickListener(onClickListener);

        Intent intent = new Intent();
        intent.setClassName("org.prowl.torque", "org.prowl.torque.remote.TorqueService");
        boolean successfulBind = bindService(intent, connection, 0);

        if (successfulBind) {


        } else {
            Toast.makeText(getApplicationContext(), "Fail to bind torque service", Toast.LENGTH_SHORT).show();
        }
    }

    private void run() {
        Map<String, ArrayList<String>> textPidParams = getEmptyTextParams();
        Map<String, ArrayList<Float>> floatPidParams = getEmptyFloatParams();
        fillWithTestTextData(textPidParams, floatPidParams);

        float[] minValueTmp = new float[floatPidParams.get(PARAM_MIN_VALUE).size()];
        for (int i = 0; i < floatPidParams.get(PARAM_MIN_VALUE).size(); i++) {
            minValueTmp[i] = floatPidParams.get(PARAM_MIN_VALUE).get(i);
        }

        float[] maxValueTmp = new float[floatPidParams.get(PARAM_MAX_VALUE).size()];
        for (int i = 0; i < floatPidParams.get(PARAM_MAX_VALUE).size(); i++) {
            maxValueTmp[i] = floatPidParams.get(PARAM_MAX_VALUE).get(i);
        }



        try {
            boolean success = torqueService.sendPIDDataV2(
                    PLUGIN_NAME,
                    textPidParams.get(PARAM_NAME).toArray(new String[textPidParams.get(PARAM_NAME).size()]),
                    textPidParams.get(PARAM_SHORT_NAME).toArray(new String[textPidParams.get(PARAM_SHORT_NAME).size()]),
                    textPidParams.get(PARAM_MODE_AND_PID).toArray(new String[textPidParams.get(PARAM_MODE_AND_PID).size()]),
                    textPidParams.get(PARAM_EQUATION).toArray(new String[textPidParams.get(PARAM_EQUATION).size()]),
                    minValueTmp,
                    maxValueTmp,
                    textPidParams.get(PARAM_UNIT).toArray(new String[textPidParams.get(PARAM_UNIT).size()]),
                    textPidParams.get(PARAM_HEADER).toArray(new String[textPidParams.get(PARAM_HEADER).size()]),
                    null,
                    null
            );

            if(success){
                Toast.makeText(getApplicationContext(), "PIDS saved", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
        }
    }

    private Map<String, ArrayList<String>> getEmptyTextParams() {
        Map<String, ArrayList<String>> params = new HashMap<>();
        params.put(PARAM_NAME, new ArrayList<String>());
        params.put(PARAM_SHORT_NAME, new ArrayList<String>());
        params.put(PARAM_MODE_AND_PID, new ArrayList<String>());
        params.put(PARAM_EQUATION, new ArrayList<String>());
        params.put(PARAM_UNIT, new ArrayList<String>());
        params.put(PARAM_HEADER, new ArrayList<String>());
        return params;
    }

    private Map<String, ArrayList<Float>> getEmptyFloatParams() {
        Map<String, ArrayList<Float>> params = new HashMap<>();
        params.put(PARAM_MIN_VALUE, new ArrayList<Float>());
        params.put(PARAM_MAX_VALUE, new ArrayList<Float>());
        return params;
    }

    private void fillWithTestTextData(Map<String, ArrayList<String>> params, Map<String, ArrayList<Float>> floatParams){
        try {
            BufferedReader br = new BufferedReader(new FileReader("data.csv"));
            String line;
            while((line = br.readLine()) != null){
                String[] values = line.split(",");
                params.get(PARAM_NAME).add(values[0]);
                params.get(PARAM_SHORT_NAME).add(values[1]);
                params.get(PARAM_MODE_AND_PID).add(values[2]);
                params.get(PARAM_EQUATION).add(values[3]);
                floatParams.get(PARAM_MIN_VALUE).add(Float.parseFloat(values[4]));
                floatParams.get(PARAM_MAX_VALUE).add(Float.parseFloat(values[5]));
                params.get(PARAM_UNIT).add(values[6]);
                params.get(PARAM_HEADER).add(values[7]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillWithTestFloatData(Map<String, ArrayList<Float>> params){
        params.get(PARAM_MIN_VALUE).add(30f);
        params.get(PARAM_MAX_VALUE).add(100f);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            torqueService = ITorqueService.Stub.asInterface(service);
            try {
                if (torqueService.getVersion() < 19) {
                    return;
                }
            } catch (RemoteException e) {

            }
        }

        public void onServiceDisconnected(ComponentName name) {
            torqueService = null;
        }
    };

    Button.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            run();
        }
    };
}
