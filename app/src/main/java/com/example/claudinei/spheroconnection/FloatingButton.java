package com.example.claudinei.spheroconnection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.orbotix.ConvenienceRobot;

/**
 * Created by claudinei on 03/09/16.
 */
public class FloatingButton extends Activity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    private ConvenienceRobot mRobot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.floating_button);

        Intent intent = new Intent(FloatingButton.this, Connection.class);
        startActivity(intent);
    }

}
