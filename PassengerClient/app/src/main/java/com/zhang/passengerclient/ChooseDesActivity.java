package com.zhang.passengerclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ChooseDesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvBack;
    private EditText etDes;
    private Button btnDes;
    private ListView desListView;
    private String city;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_des);

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        tvBack = (TextView) findViewById(R.id.tvBack);
        etDes = (EditText) findViewById(R.id.etDes);
        btnDes = (Button) findViewById(R.id.btnDes);
        desListView = (ListView) findViewById(R.id.desListView);

        city = getIntent().getStringExtra("city");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        desListView.setAdapter(adapter);

        switch (city) {
            case "荆州市":
                adapter.add("荆州火车站");
                adapter.add("荆州长途汽车站");
                adapter.add("荆州东门");
                adapter.add("中山公园");
                adapter.add("荆州万达");
                adapter.add("沙市北京路");
                adapter.add("沙市女人街");
                break;
            case "北京市":
                adapter.add("北京站");
                adapter.add("北京东站");
                adapter.add("北京西站");
                adapter.add("首都机场");
                adapter.add("三里屯");
                adapter.add("天安门");
                adapter.add("颐和园");
                break;
            case "上海市":
                adapter.add("上海站");
                adapter.add("上海民航龙华机场");
                adapter.add("上海虹桥站");
                adapter.add("中山公园");
                adapter.add("上海博物馆");
                adapter.add("外滩");
                adapter.add("迪斯尼乐园");
                break;
            case "广州市":
                adapter.add("广州站");
                adapter.add("白云国际机场");
                adapter.add("广州南站");
                adapter.add("沙面");
                adapter.add("长隆欢乐世界");
                adapter.add("中山大学");
                adapter.add("广州塔");
                break;
            case "武汉市":
                adapter.add("武汉站");
                adapter.add("天河国际机场");
                adapter.add("汉口站");
                adapter.add("武昌站");
                adapter.add("付家坡长途汽车站");
                adapter.add("金家墩长途汽车站");
                adapter.add("武汉大学");
                break;
            case "南京市":
                adapter.add("南京站");
                adapter.add("禄口国际机场");
                adapter.add("中山陵");
                adapter.add("夫子庙");
                adapter.add("总统府");
                adapter.add("秦淮河");
                adapter.add("南京南站");
                break;
        }


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

        btnDes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(etDes.getText())) {
                    Intent i = new Intent();
                    i.putExtra("destination", etDes.getText().toString().trim());
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Toast.makeText(ChooseDesActivity.this, "请输入目的地", Toast.LENGTH_SHORT).show();
                }
            }
        });

        desListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = adapter.getItem(position);

                Intent i = new Intent();
                i.putExtra("destination", item);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }
}
