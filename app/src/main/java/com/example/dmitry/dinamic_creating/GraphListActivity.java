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
import java.util.Objects;

public class GraphListActivity extends AppCompatActivity {
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;

    ArrayAdapter<String> adapter;
    ListView lvGraphs;
    AlertDialog.Builder ad, adUnauthorized;
    ArrayList<String> gNames;
    Context context;
    int IDacc;
    String accName, accPassword;
    SharedPreferences sPref;

    final String SAVED_USERNAME = "username";
    final String SAVED_PASSWORD = "password";

    String savedUsername, savedPassword;

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
                if (!Objects.equals(savedPassword, "") && !Objects.equals(savedUsername, "")) {
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
                                        public void onClick(DialogInterface dialog, int id) {
                                            String gName = userInput.getText().toString();
                                            if (gNames.contains(gName)) {
                                                Intent intent = new Intent(GraphListActivity.this, MainActivity.class);
                                                intent.putExtra("gName", gName);
                                                intent.putExtra("IDg", getIDg(gName));
                                                startActivity(intent);
                                            } else {
                                                final ContentValues cvLUG = new ContentValues();
                                                Cursor cursor = mDb.rawQuery("SELECT IDacc FROM accs WHERE accs.accName = ? AND accs.accPassword = ?", new String[]{savedUsername, savedPassword});
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

                                                int IDg = 0;
                                                Cursor c = mDb.rawQuery("SELECT IDg FROM graphs WHERE gName = ?", new String[] {gName});

                                                if (c != null) {
                                                    if (c.moveToFirst()) {
                                                        String str;
                                                        do {
                                                            str = "";
                                                            for (String cn : c.getColumnNames()) {
                                                                str = str.concat(cn + " = "
                                                                        + c.getString(c.getColumnIndex(cn)) + "; ");
                                                                IDg = Integer.valueOf(c.getString(c.getColumnIndex(cn)));
                                                            }
                                                        } while (c.moveToNext());
                                                    }
                                                    c.close();
                                                }

                                                cvLUG.put("IDg", IDg);
                                                mDb.insert("lug", null, cvLUG);

                                                saveAccData();
                                                Intent intent = new Intent(GraphListActivity.this, MainActivity.class);
                                                intent.putExtra("gName", gName);
                                                intent.putExtra("IDg", getIDg(gName));
                                                startActivity(intent);
                                            }
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
                } else {
                    final AlertDialog.Builder dialogUnauthorized = new AlertDialog.Builder(this);
                    dialogUnauthorized.setTitle("Вы не вошли в аккаунт");  // заголовок
                    dialogUnauthorized.setMessage("Хотите войти?"); // сообщение
                    dialogUnauthorized.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            showAuthDialog();
                        }
                    });
                    dialogUnauthorized.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.cancel();
                        }
                    });
                    dialogUnauthorized.setCancelable(true);
                    dialogUnauthorized.show();
                }
                break;

            case (R.id.action_auth):
                showAuthDialog();
                break;

            case (R.id.action_info):
                Intent intent = new Intent(GraphListActivity.this, AlgGlossary.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAuthDialog() {
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
                            public void onClick(DialogInterface dialog, int id) {
                                if (!(login.getText().toString().equals("") || pword.getText().toString().equals(""))) {
                                    ArrayList<String> accNames = new ArrayList<>();
                                    ArrayList<String> accPasswords = new ArrayList<>();
                                    String loginStr = login.getText().toString();
                                    String pwordStr = pword.getText().toString();

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
                                        if (!accPasswords.contains(pwordStr))
                                            Toast.makeText(context, "Неправильный пароль!", Toast.LENGTH_SHORT).show();
                                        else {
                                            Toast.makeText(context, "Добро пожаловать, " + loginStr + "!", Toast.LENGTH_LONG).show();
                                            accName = loginStr;
                                            accPassword = pwordStr;
                                            saveAccData();
                                            loadAccData();
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Не оставляйте строки пустыми(", Toast.LENGTH_SHORT).show();
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
    }

    private void loadLUG(final String accName, String accPassword) {
        gNames = new ArrayList<>();

        Cursor cursorLUG = mDb.rawQuery("SELECT graphs.gName AS gName FROM lug " +
                "LEFT JOIN graphs " +
                "ON lug.IDg = graphs.IDg " +
                "LEFT JOIN accs " +
                "ON lug.IDacc = accs.IDacc " +
                "WHERE accs.accName = ? AND accs.accPassword = ?", new String[] {accName, accPassword});
        int idColGName = cursorLUG.getColumnIndex("gName");
        cursorLUG.moveToFirst();
        while (!cursorLUG.isAfterLast()) {
            if (cursorLUG.getString(idColGName) != null)
                gNames.add(cursorLUG.getString(idColGName));
            cursorLUG.moveToNext();
        }
        cursorLUG.close();

        if (adapter != null) adapter.clear();
        if (!gNames.isEmpty()) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gNames);
            // присваиваем адаптер списку
            lvGraphs.setAdapter(adapter);

            // нажатие на элемент списка
            lvGraphs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO передавать по IDg => написать функцию нахождения этого ID и применять при удалении
                    String gName = String.valueOf(lvGraphs.getItemAtPosition(position));

                    Intent intent = new Intent(GraphListActivity.this, MainActivity.class);
                    intent.putExtra("gName", gName);
                    intent.putExtra("IDg", getIDg(gName));
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
                accName = loginStrDialog;
                accPassword = pwordStrDialog;
                saveAccData();
                loadAccData();
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

    int getIDg(String gName) {
        int ID = -1;
        Cursor cGraph = mDb.rawQuery("SELECT graphs.IDg AS IDg FROM lug " +
                "LEFT JOIN graphs " +
                "ON lug.IDg = graphs.IDg " +
                "LEFT JOIN accs ON lug.IDacc = accs.IDacc " +
                "WHERE accs.accName = ? AND graphs.gName = ?", new String[] {savedUsername, gName});
        int idColIDg = cGraph.getColumnIndex("IDg");
        cGraph.moveToFirst();
        while (!cGraph.isAfterLast()) {
            ID = Integer.valueOf(cGraph.getString(idColIDg));
            cGraph.moveToNext();
        }
        cGraph.close();
        return ID;
    }

    void openDeleteDialog(final AdapterView<?> parent, final int position){
        final String gName = String.valueOf(lvGraphs.getItemAtPosition(position));

        // TODO реализовать удаление через IDg
        ad = new AlertDialog.Builder(this);
        ad.setTitle("Удаление");  // заголовок
        ad.setMessage("Вы точно хотите удалить граф?"); // сообщение
        ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                int ID = getIDg(gName);
                mDb.delete("lug", "IDg = ?", new String[]{String.valueOf(ID)});
                mDb.delete("graphs", "IDg = ?", new String[]{String.valueOf(ID)});
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
        if (accName != null && accPassword != null) {
            ed.putString(SAVED_USERNAME, accName);

            ed.putString(SAVED_PASSWORD, accPassword);
            ed.apply();
        }
    }

    void loadAccData() {
        sPref = getPreferences(MODE_PRIVATE);
        savedUsername = sPref.getString(SAVED_USERNAME, "");
        savedPassword = sPref.getString(SAVED_PASSWORD, "");
        loadLUG(savedUsername, savedPassword);
    }
}
