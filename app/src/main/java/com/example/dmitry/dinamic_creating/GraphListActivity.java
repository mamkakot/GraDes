package com.example.dmitry.dinamic_creating;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class GraphListActivity extends AppCompatActivity {
    private static final String ACC_NAME = "username";
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    ArrayAdapter<String> adapter;
    ListView lvGraphs;
    AlertDialog.Builder ad, adUnauthorized;
    ArrayList<String> gNames;
    Context context;
    int IDacc, IDg;
    String accName, accPassword;
    SharedPreferences sPref;

    final String SAVED_USERNAME = "username";
    final String SAVED_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_list);

        lvGraphs = findViewById(R.id.lvGraphs);

        mDBHelper = new DatabaseHelper(this);
        mDb = mDBHelper.getWritableDatabase();
        context = this;

        loadAccData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph_act_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_create):
                //Получаем вид с файла prompt.xml, который применим для диалогового окна:
                LayoutInflater liCreate = LayoutInflater.from(this);
                final View promptsViewCreate = liCreate.inflate(R.layout.prompt, null);
                //Создаем AlertDialog
                AlertDialog.Builder mDialogBuilderCreate = new AlertDialog.Builder(this);
                //Настраиваем prompt.xml для нашего AlertDialog:
                mDialogBuilderCreate.setView(promptsViewCreate);
                //Настраиваем отображение поля для ввода текста в открытом диалоге:
                final EditText userInput = promptsViewCreate.findViewById(R.id.userInput);
                //Настраиваем сообщение в диалоговом окне:
                mDialogBuilderCreate
                        .setCancelable(true)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        String gName = userInput.getText().toString();
                                        Log.d("faf", gName);

                                        if (gNames.contains(gName)) {
                                            gName += "_1";
                                        }
                                        final ContentValues cvLUG = new ContentValues();
                                        Cursor cursor = mDb.rawQuery("SELECT IDacc FROM accs WHERE accs.accName = ? AND accs.accPassword = ?", new String[] {accName, accPassword});
                                        int idColAccID = cursor.getColumnIndex("IDacc");
                                        cursor.moveToFirst();
                                        // и проходится по всем строкам, выбирая нужные колонки
                                        while (!cursor.isAfterLast()) {
                                            IDacc = Integer.valueOf(cursor.getString(idColAccID));
                                            cursor.moveToNext();
                                        }
                                        cursor.close();
                                        cvLUG.put("IDacc", IDacc);

                                        ContentValues cvGraph = new ContentValues();
                                        cvGraph.put("gName", gName);
                                        mDb.insert("graphs", null, cvGraph);
                                        Log.d("GLA", "graph inserted " + gName);
                                        Cursor cursorG = mDb.rawQuery("SELECT IDg FROM graphs WHERE gName = ?", new String[] {gName});
                                        int idColGraphID = cursorG.getColumnIndex("IDg");
                                        cursorG.moveToFirst();
                                        // и проходится по всем строкам, выбирая нужные колонки
                                        while (!cursorG.isAfterLast()) {
                                            IDg = Integer.valueOf(cursorG.getString(idColGraphID));
                                            cursorG.moveToNext();
                                        }
                                        cursorG.close();
                                        cvLUG.put("IDg", IDg);
                                        mDb.insert("lug", null, cvLUG);
                                        Log.d("GLA", "lug inserted " + IDacc + " " + IDg);

                                        saveAccData();

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
                AlertDialog alertDialogCreate = mDialogBuilderCreate.create();
                //и отображаем его:
                alertDialogCreate.show();
                break;

            case (R.id.action_auth):
                LayoutInflater li = LayoutInflater.from(this);
                final View promptsView = li.inflate(R.layout.prompt_auth, null);
                final AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);
                mDialogBuilder.setView(promptsView);
                final EditText login = promptsView.findViewById(R.id.login);
                final EditText pword = promptsView.findViewById(R.id.pword);
                mDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        ArrayList<String> accNames = new ArrayList<>();
                                        ArrayList<String> accPasswords = new ArrayList<>();
                                        String loginStr = login.getText().toString();
                                        String pwordStr = pword.getText().toString();
                                        Log.d("Auth_data", loginStr + " " + pwordStr);

                                        Cursor cursor = mDb.rawQuery("SELECT accName, accPassword FROM accs", null);
                                        int idColAccName = cursor.getColumnIndex("accName");
                                        int idColAccPassword = cursor.getColumnIndex("accPassword");
                                        cursor.moveToFirst();
                                        while (!cursor.isAfterLast()) {
                                            accNames.add(cursor.getString(idColAccName));
                                            accPasswords.add(cursor.getString(idColAccPassword));
                                            cursor.moveToNext();
                                        }
                                        cursor.close();

                                        if (!accNames.contains(loginStr)) {
                                            openUnauthorizedDialog(loginStr, pwordStr);
                                            dialog.cancel();
                                        } else {
                                            if (!accPasswords.contains(pwordStr)) {
                                                Toast.makeText(context, "Неправильный пароль!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, "Добро пожаловать, " + loginStr + "!", Toast.LENGTH_LONG).show();
                                                accName = loginStr; accPassword = pwordStr;
                                                loadLUG(loginStr, pwordStr);
                                            }
                                        }
                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = mDialogBuilder.create();
                alertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadLUG(final String accName, String accPassword) {
        gNames = new ArrayList<>();
        IDacc = -1; IDg = -1;

        Cursor cursorLUG = mDb.rawQuery("SELECT graphs.gName AS gName, accs.accName AS accName FROM lug\n" +
                "LEFT JOIN graphs\n" +
                "ON lug.IDg = graphs.IDg\n" +
                "LEFT JOIN accs\n" +
                "ON lug.IDacc = accs.IDacc\n" +
                "WHERE accs.accName = ? AND accs.accPassword = ?", new String[] {accName, accPassword});
        int idColGName = cursorLUG.getColumnIndex("gName");
        cursorLUG.moveToFirst();
        while (!cursorLUG.isAfterLast()) {
            gNames.add(cursorLUG.getString(idColGName));
            cursorLUG.moveToNext();
        }
        cursorLUG.close();

        if (!gNames.isEmpty()) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gNames);

            // присваиваем адаптер списку
            lvGraphs.setAdapter(adapter);

            // нажатие на элемент списка
            lvGraphs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String gName = String.valueOf(lvGraphs.getItemAtPosition(position));
                    Log.d("faf", gName);

                    Intent intent = new Intent(GraphListActivity.this, MainActivity.class);
                    intent.putExtra("gName", gName);
                    intent.putExtra("IDacc", IDacc);
                    intent.putExtra("IDg", IDg);
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
    }

    void openUnauthorizedDialog(final String loginStrDialog, final String pwordStrDialog){
        adUnauthorized = new AlertDialog.Builder(this);
        adUnauthorized.setTitle("Нет пользователя с таким именем");  // заголовок
        adUnauthorized.setMessage("Зарегистрировать нового пользователя?"); // сообщение
        adUnauthorized.setPositiveButton("Зарегистрировать", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                ContentValues cv = new ContentValues();
                cv.put("accName", loginStrDialog);
                cv.put("accPassword", pwordStrDialog);
                mDb.insert("accs", null, cv);
            }
        });
        adUnauthorized.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        adUnauthorized.setCancelable(true);
        adUnauthorized.show();
    }

    void openDeleteDialog(final AdapterView<?> parent, final int position){
        final String gName = String.valueOf(lvGraphs.getItemAtPosition(position));

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

    void saveAccData() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_USERNAME, accName);
        ed.putString(SAVED_PASSWORD, accPassword);
        ed.apply();
        Toast.makeText(this, "Text saved", Toast.LENGTH_SHORT).show();
    }

    void loadAccData() {
        sPref = getPreferences(MODE_PRIVATE);
        String savedUsername = sPref.getString(SAVED_USERNAME, "");
        String savedPassword = sPref.getString(SAVED_PASSWORD, "");
        loadLUG(savedUsername, savedPassword);
        Toast.makeText(this, "Text loaded", Toast.LENGTH_SHORT).show();
    }
}
