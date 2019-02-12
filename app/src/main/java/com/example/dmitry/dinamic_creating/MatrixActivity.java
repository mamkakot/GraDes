package com.example.dmitry.dinamic_creating;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

public class MatrixActivity extends AppCompatActivity {
    boolean[][] smejVer;
    int[][] incidVer;
    TextView textSmej;
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
        String simbolSpace = "   ";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        textSmej = findViewById(R.id.textViewSmej);
        smejVer = (boolean[][]) getIntent().getSerializableExtra("smej");
        incidVer = (int[][]) getIntent().getSerializableExtra("incid");
        if (smejVer.length >= 228) {
            Toast.makeText(this, "Ну вот добрался ты сюда, а что дальше? Что ты от этого получил?", Toast.LENGTH_LONG).show();
            return;
        }
        if (smejVer.length != 0) {
            textSmej.setText("Матрица смежности: ");
            TableLayout tableLayout = findViewById(R.id.tableLayoutSmej);
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
                tableLayout.addView(tableRow, i);
            }
        }
    }
}

