package com.example.claudinei.spheroconnection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.async.CollisionDetectedAsyncData;
import com.orbotix.command.RotationRateCommand;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;
import com.orbotix.common.RobotChangedStateListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by claudinei on 03/09/16.
 */
public class Connection extends Activity implements RobotChangedStateListener, SensorEventListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    private ConvenienceRobot mRobot;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float bateria;
    private static float VELOCIDADE_SPHERO = 0.9f;
    private boolean bottomPressed = false;

    float direction = 90.0f;

    private String sentido = "up";

    Float x = new Float(0);
    Float y = new Float(0);
    Float z = new Float(0);

    // Gravity force on x, y, z axis
    private float gravity[] = new float[3];

    private static final int THRESHOLD = 7;
    private static final float ALPHA = 0.8f;

    private float orientation = 90.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.floating_button);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.e("Sphero", "Location permission has not already been granted");
                Toast.makeText(Connection.this, "Sphero - Location permission has not already been granted", Toast.LENGTH_SHORT).show();
                List<String> permissions = new ArrayList<String>();
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                Log.d("Sphero", "Location permission already granted");
            }
        }

        findViewById(R.id.bt_connect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Connection.this, "Connecting . . . ", Toast.LENGTH_SHORT).show();

                DualStackDiscoveryAgent.getInstance().addRobotStateListener(Connection.this);
                startDiscovery();
            }
        });

        findViewById(R.id.bt_disconnect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Connection.this, "Disconecting . . . ", Toast.LENGTH_SHORT).show();
                stopDiscovery();
            }
        });


        findViewById(R.id.bt_sleep).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mRobot != null) {
                    mRobot.sleep();
                    Toast.makeText(getApplicationContext(), "Sleeping . . . ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Connect to a Sphero first!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        findViewById(R.id.btn_movim).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    bottomPressed = true;
                    Toast.makeText(getApplicationContext(), "Pronto para o Strike ?! Faça o movimento então . . .", Toast.LENGTH_SHORT).show();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    bottomPressed = false;
                }
                return true;
            }
        });

        findViewById(R.id.btn_Calibrate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRobot != null) {
                    direction = direction + 10.0f;
                    mRobot.rotate(direction);
                }
            }
        });

        findViewById(R.id.btn_up).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRobot != null) {
                    mRobot.rotate(direction);
                    Toast.makeText(getApplicationContext(), "Frente", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_down).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRobot != null) {
                    if(sentido != "down"){
                        direction = Math.abs(direction + 180.0f);
                        mRobot.rotate(direction);
                        Toast.makeText(getApplicationContext(), "Trás", Toast.LENGTH_SHORT).show();
                        sentido = "down";
                    }
                }
            }
        });

        findViewById(R.id.btn_left).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRobot != null) {
                    if(sentido != "left"){
                        direction = Math.abs(direction + 270.0f);
                        mRobot.rotate(direction);
                        Toast.makeText(getApplicationContext(), "Esquerda", Toast.LENGTH_SHORT).show();
                        sentido = "left";
                    }
                }
            }
        });

        findViewById(R.id.btn_right).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRobot != null) {
                    if(sentido != "right"){
                        direction = Math.abs(direction + 90.0f);
                        mRobot.rotate(direction);
                        Toast.makeText(getApplicationContext(), "Direita", Toast.LENGTH_SHORT).show();
                        sentido = "right";
                    }
                }
            }
        });
    }


    @Override
    protected void onStop() {
        //If the DiscoveryAgent is in discovery mode, stop it.
        if (DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if (mRobot != null) {
            mRobot.disconnect();
            mRobot = null;

            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_container));

            TextView text = (TextView) layout.findViewById(R.id.text);
            text.setText("Sphero disconnected !!!");

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
    }

    private void stopDiscovery() {
        //If the DiscoveryAgent is in discovery mode, stop it.
        if (DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if (mRobot != null) {
            mRobot.disconnect();
            mRobot = null;
        }
    }

    private void startDiscovery() {
        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if (!DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery(this);
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
                Toast.makeText(Connection.this, "Sphero - DiscoveryException: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        startDiscovery();
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d("Permissions", "Permission Denied: " + permissions[i]);
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);
                direction = mRobot.getLastHeading();
                mRobot.setBackLedBrightness(1.0f);

                mRobot.enableCollisions(true);
                mRobot.addResponseListener(new ResponseListener() {
                    @Override
                    public void handleResponse(DeviceResponse deviceResponse, Robot robot) {

                    }

                    @Override
                    public void handleStringResponse(String s, Robot robot) {

                    }

                    @Override
                    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {
                        if (asyncMessage instanceof CollisionDetectedAsyncData) {
                            for (int i = 0; i < 10; i++) {
                                mRobot.setLed(255, 0, 0);
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        mRobot.setLed(0, 0, 255);
                                    }
                                }, 200);

                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        mRobot.setLed(0, 255, 0);
                                    }
                                }, 200);
                            }
                        }
                    }
                });

                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast,
                        (ViewGroup) findViewById(R.id.custom_toast_container));

                TextView text = (TextView) layout.findViewById(R.id.text);
                text.setText("Sphero connected !!!");

                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();

                break;
            }
        }
    }

    private void locomover(final float direcao, final float velocidade) {
        if (mRobot != null) {
            // Sphero ira para a direcao requerida na velocidade que for passada como parametro
            mRobot.drive(direcao, velocidade);

            // ira se locomover na direcao por 1 segundo
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    //Para e executa o proximo movimento da lista recursivamente.
                    mRobot.stop();
                }
            }, 5000);

        }
    }

    //    Esse metodo manipula o sensor de acelerometro do celular
    @Override
    public void onSensorChanged(SensorEvent event) {
        Float xx = event.values[0];
        Float yy = event.values[1];
        Float zz = event.values[2];

         /*
        Os valores ocilam de -10 a 10.
        Quanto maior o valor de X mais ele ta caindo para a esquerda - Positivo Esqueda
        Quanto menor o valor de X mais ele ta caindo para a direita  - Negativo Direita
        Se o valor de  X for 0 então o celular ta em pé - Nem Direita Nem Esquerda
        Se o valor de Y for 0 então o cel ta "deitado"
         Se o valor de Y for negativo então ta de cabeça pra baixo, então quanto menor y mais ele ta inclinando pra ir pra baixo
        Se o valor de Z for 0 então o dispositivo esta reto na horizontal.
        Quanto maior o o valor de Z Mais ele esta inclinado para frente
        Quanto menor o valor de Z Mais ele esta inclinado para traz.
        */

        if (z == 0) {
            x = xx;
            y = yy;
            z = zz;
        }
        if (bottomPressed) {
            if (y < 0 && yy > 0) {
                float acceleration = maxAcceleration(event);
                Sensor mySensor = event.sensor;

                locomover(direction, VELOCIDADE_SPHERO);
                TextView tv = (TextView) findViewById(R.id.tvDirecao);
                tv.setText("Frente");
            } else if (yy < 0 && zz < 3 && zz < z) {
                float currentDirection = Math.abs(direction - 180.0f);
                locomover(currentDirection, VELOCIDADE_SPHERO);
                TextView tv = (TextView) findViewById(R.id.tvDirecao);
                tv.setText("Trás");
            }
        }

        x = xx;
        y = yy;
        z = zz;


    }

    private float maxAcceleration(SensorEvent event) {
        // Low-pass filter
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0]; // x axis
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1]; // y axis
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2]; // z axis

        // High-pass filter
        float linear_acceleration[] = new float[3];
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        float max = Math.max(Math.max(linear_acceleration[0], linear_acceleration[1])
                , linear_acceleration[2]);

        if (max == linear_acceleration[1]) {
            return linear_acceleration[1];
        } else {
            return 0.0f;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Retorna a bateria disponivel do Sphero
        bateria = sensor.getPower();
        TextView tv = (TextView) findViewById(R.id.tvBateria);
        float batteryShow = bateria * 100;
        String show = batteryShow + " %";
        tv.setText(show);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

}
