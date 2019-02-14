package com.example.dmitry.dinamic_creating;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MatrixActivity extends AppCompatActivity {
    boolean[][] smejVer, incidVer;
    TextView textSmej, matrixSmej, matrixIncid;
    LinearLayout matrixLayout;
    String text = "";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        matrixLayout = findViewById(R.id.matrix_layout);
        textSmej = findViewById(R.id.textViewSmej);
        matrixSmej = findViewById(R.id.matrixSmej);
        matrixSmej.setMovementMethod(new ScrollingMovementMethod());
        matrixIncid = findViewById(R.id.matrixIncid);
        smejVer = (boolean[][]) getIntent().getSerializableExtra("smej");
        incidVer = (boolean[][]) getIntent().getSerializableExtra("incid");
        if (smejVer.length >= 228) {
            Toast.makeText(this, "Ну вот добрался ты сюда, а что дальше? Что ты от этого получил?", Toast.LENGTH_LONG).show();
            return;
        }
        if (smejVer.length != 0) {
            StringBuilder stringSmej = new StringBuilder(), stringIncid = new StringBuilder();
            // TODO Сделать-таки подписи к осям (ну то есть номера вершин и рёбер, да)
            for (int i = 0; i < smejVer.length; i++) {
                for (int j = 0; j <= i; j++) {
                    if (smejVer[i][j]) text = "1 | ";
                    else text = "0 | ";
                    stringSmej.append(text);
                }
                stringSmej.append("\n");
            }

            for (int i = 0; i < smejVer.length; i++) {
                for (int t = 0; t < incidVer[i].length; t++) {
                    if (incidVer[i][t]) text = "1 ";
                    else text = "0 ";
                    stringIncid.append(text);
                }
                stringIncid.append("\n");
            }
            matrixSmej.setText(stringSmej);
            matrixIncid.setText(stringIncid);
        }
    }
}

