package com.example.claudinei.spheroconnection;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;
import com.orbotix.common.RobotChangedStateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudinei on 03/09/16.
 */
public class FloatingButton extends Activity implements RobotChangedStateListener{

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    private ConvenienceRobot mRobot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.floating_button);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int hasLocationPermission = checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION );
            if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                Log.e( "Sphero", "Location permission has not already been granted" );
                Toast.makeText(FloatingButton.this, "Sphero - Location permission has not already been granted", Toast.LENGTH_SHORT).show();
                List<String> permissions = new ArrayList<String>();
                permissions.add( Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()] ), REQUEST_CODE_LOCATION_PERMISSION );
            } else {
                Log.d( "Sphero", "Location permission already granted" );
            }
        }



        findViewById(R.id.pink_icon).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FloatingButton.this, "Clicked pink Floating Action Button", Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.bt_connect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FloatingButton.this, "Connecting . . . ", Toast.LENGTH_SHORT).show();

                DualStackDiscoveryAgent.getInstance().addRobotStateListener( FloatingButton.this );
                startDiscovery();
            }
        });

        findViewById(R.id.bt_disconnect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FloatingButton.this, "Disconecting . . . ", Toast.LENGTH_SHORT).show();
                stopDiscovery();
            }
        });
    }



    @Override
    protected void onStop() {
        //If the DiscoveryAgent is in discovery mode, stop it.
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if( mRobot != null ) {
            mRobot.disconnect();
            mRobot = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener( null );
    }

    private void stopDiscovery() {
        //If the DiscoveryAgent is in discovery mode, stop it.
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if( mRobot != null ) {
            mRobot.disconnect();
            mRobot = null;
        }
    }

    private void startDiscovery() {
        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery( this );
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
                Toast.makeText(FloatingButton.this, "Sphero - DiscoveryException: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        startDiscovery();
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
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

}
