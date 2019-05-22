package com.example.dmitry.dinamic_creating;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class AlgGlossary extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    DatabaseHelper mDbHelper;
    SQLiteDatabase mDb;
    ToggleButton toggleBtn;
    ListView lvAlgs;
    AlertDialog.Builder adBuilder;
    Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glossary);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Алгоритмы на графах");

        toggleBtn = findViewById(R.id.toggleButton);

        lvAlgs = findViewById(R.id.lvAlgs);

        context = this;

        loadAlgs(toggleBtn.isChecked());

        toggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadAlgs(isChecked);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onToggleBtnClick(View view) {
        loadAlgs(toggleBtn.isChecked());
    }

    private void loadAlgs(boolean isChecked) {
        String IDt;
        if (isChecked) {
            toggleBtn.setText("Ориентированные");
            IDt = "1";
        } else {
            toggleBtn.setText("Неориентированные");
            IDt = "2";
        }

        mDbHelper = new DatabaseHelper(context);
        mDb = mDbHelper.getReadableDatabase();

        toggleBtn = findViewById(R.id.toggleButton);

        ArrayList<String> algNames = new ArrayList<>();
        Cursor c = mDb.rawQuery("SELECT algs.algName AS algName, IDt FROM atl "+
                "LEFT JOIN algs "+
                "ON atl.IDalg = algs.IDalg "+
                "WHERE IDt = ?", new String[]{IDt});
        if (c != null) {
            int idColAlgName = c.getColumnIndex("algName");
            c.moveToFirst();
            while (!c.isAfterLast()) {
                algNames.add(c.getString(idColAlgName));
                c.moveToNext();
            }
            c.close();
        }

        if (!algNames.isEmpty()) {
            adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, algNames);
            lvAlgs.setAdapter(adapter);

            lvAlgs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String algName = String.valueOf(lvAlgs.getItemAtPosition(position));
                    String algDesc = "";
                    Cursor c = mDb.rawQuery("SELECT algDescription FROM algs "+
                            "WHERE algName = ?", new String[]{algName});
                    if (c != null) {
                        int idColAlgDesc = c.getColumnIndex("algDescription");
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            algDesc = c.getString(idColAlgDesc);

                            c.moveToNext();
                        }
                        c.close();
                    }
                    adBuilder = new AlertDialog.Builder(context);
                    adBuilder.setTitle(algName);
                    adBuilder.setMessage(algDesc);
                    adBuilder.setCancelable(true);
                    adBuilder.show();
                }
            });
        }
    }
}
