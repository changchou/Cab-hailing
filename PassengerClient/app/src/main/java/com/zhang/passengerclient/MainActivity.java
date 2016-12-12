package com.zhang.passengerclient;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etTel, etName;
    private TextView tvCallTaxi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTel = (EditText) findViewById(R.id.etTel);
        etName = (EditText) findViewById(R.id.etName);
        tvCallTaxi = (TextView) findViewById(R.id.tvCallTaxi);

        tvCallTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etTel.getText()) && !TextUtils.isEmpty(etName.getText())) {

                    Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                    intent.putExtra("telNum", etTel.getText().toString().trim());
                    intent.putExtra("name", etName.getText().toString().trim());
                    startActivity(intent);

                } else {
                    Toast.makeText(MainActivity.this, "请输入手机号和姓名", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
