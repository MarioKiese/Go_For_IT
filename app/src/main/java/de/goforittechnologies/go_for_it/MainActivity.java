package de.goforittechnologies.go_for_it;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Widgets
    TextView tvSensorValue;
    Button btUpdate;


    private BroadcastReceiver receiver = new BroadcastReceiver() {



        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int steps = 0;
                steps = bundle.getInt("steps");

                    tvSensorValue.setText(String.valueOf(steps));

            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSensorValue = findViewById(R.id.tvSensorValue);
        btUpdate = findViewById(R.id.btUpdate);

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), StepCounterService.class);
                startService(intent);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                StepCounterService.NOTIFICATION));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);


    }



}
