package com.example.dmitry.dinamic_creating;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MatrixActivity extends AppCompatActivity {
    TextView textSmej, matrixSmej, matrixIncid;
    LinearLayout matrixLayout;

    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    int IDg;

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

        IDg = (int) getIntent().getSerializableExtra("IDg");

        mDBHelper = new DatabaseHelper(this);
        mDb = mDBHelper.getReadableDatabase();
        Cursor c = mDb.rawQuery("SELECT gMatS, gMatI FROM graphs WHERE IDg = ?", new String[] {String.valueOf(IDg)});
        int idColMatS = c.getColumnIndex("gMatS");
        int idColMatI = c.getColumnIndex("gMatI");

        String gMatS = "", gMatI = "";

        c.moveToFirst();
        while (!c.isAfterLast()) {
            gMatS = c.getString(idColMatS);
            gMatI = c.getString(idColMatI);
            c.moveToNext();
        }
        c.close();

        matrixSmej.setText(gMatS);
        matrixIncid.setText(gMatI);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
