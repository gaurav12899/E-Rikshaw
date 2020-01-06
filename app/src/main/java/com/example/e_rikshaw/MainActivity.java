package com.example.e_rikshaw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button mDriver,mCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mCustomer=(Button) findViewById(R.id.customer);
        mDriver =  (Button) findViewById(R.id.driver);


        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this,DriverLoginActivity2.class);
                startActivity(intent);
                finish();
                return;

            }
        });


        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this,Customer_Login_2.class);
                startActivity(intent);
                finish();
                return;

            }
        });

    }
}
