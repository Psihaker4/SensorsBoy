package com.psychomath.sensorsboy;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "sensor322";

    private boolean searching = false;

    private List<List<Float>> floatsA = new ArrayList<>();
    private ArrayAdapter<String> adapterA;

    private Sensor sensorA;
    private SensorManager sensorManager;

    private int time = 1;

    private long passedTime;
    private long delay = 1000/13;
    private Handler handler;

    private List<Float> value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorA = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ListView accList = (ListView) findViewById(R.id.acc_list);

        adapterA = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1,new ArrayList<String>());

        accList.setAdapter(adapterA);

        handler = new Handler();

    }

    Runnable r = new Runnable() {
        @Override
        public void run() {

            if (value == null) {
                handler.postDelayed(r, delay);
                return;
            }

            floatsA.add(value);
            adapterA.add(String.format(Locale.ENGLISH, "X=%.4f Y=%.4f X=%.4f", value.get(0), value.get(1), value.get(2)));

            if (searching)
                if (floatsA.size() == time * 13) {
                    Log.d(TAG, "run: Stop");
                    searching = false;
                    if (sensorA != null)
                        sensorManager.unregisterListener(sensorAListener);

                    passedTime = System.currentTimeMillis() - passedTime;
                    Toast.makeText(getBaseContext(), "Operation ended in: " + String.valueOf((float) passedTime / 1000) + "s.", Toast.LENGTH_SHORT).show();

                    invalidateOptionsMenu();

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child(String.valueOf(time)).push()
                            .setValue(floatsA);

                } else
                    handler.postDelayed(r, delay);

        }
    };

    SensorEventListener sensorAListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            List<Float> f = new ArrayList<>();
            for(int i =0; i < 3; i++) {
                f.add(sensorEvent.values[i]);
            }
            value = f;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        int drawId;
        if(searching){
            drawId = R.drawable.ic_stop;
        } else {
            drawId = R.drawable.ic_start;
        }

        menu.findItem(R.id.timing).setTitle(String.valueOf(time)+"s");
        menu.findItem(R.id.search).setIcon(getResources().getDrawable(drawId));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        int id = item.getItemId();

        if(id==R.id.timing) {
            if(!searching) {
                int t = Character.getNumericValue(item.getTitle().charAt(0));
                t = t + (t < 9 ? 1 : -8);
                item.setTitle(t + "s");
                time = t;
            }
        } else if(id == R.id.search) {

            int drawId;
            if (searching) {
                searching = false;
                if (sensorA != null)
                    sensorManager.unregisterListener(sensorAListener);
                drawId = R.drawable.ic_start;
            } else {
                Log.d(TAG, "onOptionsItemSelected: startSearch");
                searching = true;
                adapterA.clear();
                floatsA = new ArrayList<>();
                if (sensorA != null) {
                    sensorManager.registerListener(sensorAListener, sensorA, SensorManager.SENSOR_DELAY_UI);
                    passedTime = System.currentTimeMillis();
                }
                drawId = R.drawable.ic_stop;
                handler.postDelayed(r,delay);
            }
            item.setIcon(getResources().getDrawable(drawId));
        }

        return true;
    }

}
