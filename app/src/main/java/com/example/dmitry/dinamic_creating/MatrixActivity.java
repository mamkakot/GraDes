package com.example.dmitry.dinamic_creating;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

public class MatrixActivity extends AppCompatActivity {
    boolean[][] smejVer, incidVer;
    TextView textSmej;
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
        smejVer = (boolean[][]) getIntent().getSerializableExtra("smej");
        incidVer = (boolean[][]) getIntent().getSerializableExtra("incid");
        if (smejVer.length >= 228) {
            Toast.makeText(this, "Ну вот добрался ты сюда, а что дальше? Что ты от этого получил?", Toast.LENGTH_LONG).show();
            return;
        }
        if (smejVer.length != 0) {
            // TODO Сделать нормальную оптимизацию, чтобы он каждый раз заново эти таблицы не создавал и не грузил долго
            TableLayout tableLayoutSmej = findViewById(R.id.tableLayoutSmej);
            TableLayout tableLayoutIncid = findViewById(R.id.tableLayoutIncid);
            // TODO Сделать-таки подписи к осям (ну то есть номера вершин и рёбер, да)
            for (int i = 0; i < smejVer.length; i++) {
                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                for (int j = 0; j <= i; j++) {
                    TextView textView = new TextView(this);
                    if (smejVer[i][j]) text = "1";
                    else text = "0";
                    textView.setText(text);
                    tableRow.addView(textView, j);
                }
                tableLayoutSmej.addView(tableRow, i);
            }

            for (int i = 0; i < smejVer.length; i++) {
                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                for (int t = 0; t < incidVer[i].length; t++) {
                    TextView textView = new TextView(this);
                    if (incidVer[i][t]) text = "1";
                    else text = "0";
                    textView.setText(text);
                    tableRow.addView(textView, t);
                }
                tableLayoutIncid.addView(tableRow, i);
            }
        }
    }
}

