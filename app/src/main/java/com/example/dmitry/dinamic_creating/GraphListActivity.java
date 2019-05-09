package com.example.dmitry.dinamic_creating;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GraphListActivity extends AppCompatActivity {
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    ArrayAdapter<String> adapter;

    ListView lvGraphs;
    AlertDialog.Builder ad;

    ArrayList<String> product;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_list);

        lvGraphs = findViewById(R.id.lvGraphs);

        mDBHelper = new DatabaseHelper(this);
        mDb = mDBHelper.getWritableDatabase();

        product = new ArrayList<>();

        // вот эта строчка делает выборку из БД (на языке самого sqlite)
        Cursor cursor = mDb.rawQuery("SELECT gName FROM graphs", null);
        int idColName = cursor.getColumnIndex("gName");
        cursor.moveToFirst();
        // и проходится по всем строкам, выбирая нужные колонки
        while (!cursor.isAfterLast()) {
            product.add(cursor.getString(idColName));
            cursor.moveToNext();
        }
        cursor.close();
        Log.d("sus", product.toString());

        //mDb.close();

        //mDBHelper.close();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, product);

        // присваиваем адаптер списку
        lvGraphs.setAdapter(adapter);

        // нажатие на элемент списка
        lvGraphs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("Click", "itemClick: position = " + position + ", id = "
                        + id);
                //String gName = userInput.getText().toString();
                String gName = String.valueOf(lvGraphs.getItemAtPosition(position));
                Log.d("faf", gName);
                Intent intent = new Intent(GraphListActivity.this, MainActivity.class);
                intent.putExtra("gName", gName);
                startActivity(intent);
            }
        });

        // долгое нажатие с последующим удалением
        lvGraphs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                openDeleteDialog(parent, position);
                return true;
            }
        });
    }

    public void onClickBtnAddGraph(View view) {
        //Получаем вид с файла prompt.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(this);
        final View promptsView = li.inflate(R.layout.prompt, null);

        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

        //Настраиваем prompt.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        final EditText userInput = promptsView.findViewById(R.id.input_text);

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String gName = userInput.getText().toString();
                                Log.d("faf", gName);
                                if (product.contains(gName)) {
                                    gName += "_1";
                                }
                                Intent intent = new Intent(GraphListActivity.this, MainActivity.class);
                                intent.putExtra("gName", gName);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();

        //и отображаем его:
        alertDialog.show();
    }

    void openDeleteDialog(final AdapterView<?> parent, final int position){
        final String gName = String.valueOf(lvGraphs.getItemAtPosition(position));

        //mDBHelper = new DatabaseHelper(this);
        //mDb = mDBHelper.getWritableDatabase();

        ad = new AlertDialog.Builder(this);
        ad.setTitle("Удаление");  // заголовок
        ad.setMessage("Вы точно хотите удалить граф?"); // сообщение
        ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                mDb.delete("graphs", "gName = ?", new String[]{gName});
                String selectedItem = parent.getItemAtPosition(position).toString();
                adapter.remove(selectedItem);
                adapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(),
                        "Граф " + selectedItem + " удалён.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        ad.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        ad.setCancelable(true);
        ad.show();
    }
}

