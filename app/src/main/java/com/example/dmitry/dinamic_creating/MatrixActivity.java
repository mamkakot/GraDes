package com.example.dmitry.dinamic_creating;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
                for (int i = 1; i <= smejVer.length; i++) {

                    if (i < 10) {
                        stringSmej.append("    ");
                        stringIncid.append("    ");
                    }
                    if (i >= 10 && i < 100) {
                        stringSmej.append("  ");
                        stringIncid.append("  ");
                    }
                    stringSmej.append(i).append(": |");
                    stringIncid.append(i).append(": |");
                    for (int j = 0; j < i; j++) {
                        if (smejVer[i - 1][j]) {
                            text = "1 | ";
                        } else {
                            text = "0 | ";
                        }
                        stringSmej.append(text);
                    }
                    for (int t = 0; t < incidVer[i - 1].length; t++) {
                        if (incidVer[i - 1][t]) text = "1 | ";
                        else text = "0 | ";
                        stringIncid.append(text);
                    }
                    stringSmej.append("\n");
                    stringIncid.append("\n");
                }
                matrixSmej.setText(stringSmej);
                if (incidVer[0].length != 0) matrixIncid.setText(stringIncid);
        }
    }
}
