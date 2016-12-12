package com.zhang.passengerclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseLocActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvBack;
    private EditText etLoc;
    private Button btnFrom;
    private ListView locListView;
    private String address;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_loc);

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        tvBack = (TextView) findViewById(R.id.tvBack);
        etLoc = (EditText) findViewById(R.id.etFrom);
        btnFrom = (Button) findViewById(R.id.btnFrom);
        locListView = (ListView) findViewById(R.id.locListView);

        address = getIntent().getStringExtra("address");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        locListView.setAdapter(adapter);

        adapter.add(address);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etLoc.getText())) {
                    Intent i = new Intent();
                    i.putExtra("from", etLoc.getText().toString().trim());
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Toast.makeText(ChooseLocActivity.this, "请输入出发地", Toast.LENGTH_SHORT).show();
                }
            }
        });

        locListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = adapter.getItem(position);

                Intent i = new Intent();
                i.putExtra("from", item);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }
}
