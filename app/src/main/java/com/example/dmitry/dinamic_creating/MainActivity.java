package com.example.dmitry.dinamic_creating;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import at.markushi.ui.CircleButton;


public class MainActivity extends AppCompatActivity implements Serializable, View.OnClickListener, View.OnTouchListener, CompoundButton.OnCheckedChangeListener {
    private static final String FILENAME = "file.txt";
    RelativeLayout rlmain;
    ArrayList<DrawLine> lines; // массив со связями
    ArrayList<View> vershiny; // массив со всеми вершинами
    boolean[][] smejVerBool, incidVerBool;
    float dp;
    int buttonId = 0, lineId = 0, btnSide, width = 0, height = 0; // подсчёт кнопок
    private float dX, dY;
    View aWhile;
    Switch switchMove, switchAdd;
    int blue = R.color.rebroColor,
            red = R.color.dugaColor,
            btnMainColor = Color.DKGRAY,
            bgColor = R.color.BGColor;
    boolean lineCreating = false,
            whatColor,
            dataSaved = false;
    final int MENU_DEL_VER = 1002,
            MENU_LINE = 1004,
            DIALOG_EXIT = 1;

    final String TAG = "GraDes";
    Random random = new Random();

    String gName;
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    long rowID;
    ContentValues cv;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        rlmain = findViewById(R.id.rlmain);
        rlmain.setBackgroundColor(getResources().getColor(bgColor));
        rlmain.setOnTouchListener(this);
        vershiny = new ArrayList<>();
        lines = new ArrayList<>();
        dp = getResources().getDisplayMetrics().density;
        btnSide = (int) (42 * dp);
        switchMove = findViewById(R.id.switchMove);
        switchAdd = findViewById(R.id.switchAdd);
        if (switchMove != null) switchMove.setOnCheckedChangeListener(this);

        mDBHelper = new DatabaseHelper(this);

        gName = (String) getIntent().getSerializableExtra("gName");
        Log.d("geg", gName);
    }

    @Override
    public void onClick(View view) {
        if (vershiny.contains(view) && vershiny.contains(aWhile)) {
            if (lineCreating) {
                DrawLine mDrawLine = new DrawLine(this);
                mDrawLine.setCoords(aWhile, view);
                mDrawLine.setId(lineId);
                mDrawLine.number = Integer.toString(++lineId);
                mDrawLine.draw();
                if (whatColor) {
                    mDrawLine.setColor(getResources().getColor(red));
                } else {
                    mDrawLine.setColor(getResources().getColor(red));
                }
                lineCreating = false;
                lines.add(mDrawLine);
                rlmain.addView(mDrawLine);
                view.bringToFront(); // чтобы вершины были впереди линий
                aWhile.bringToFront();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    void createVer(float x, float y){
        CircleButton vershNew = new CircleButton(this);
        TextDrawable text = new TextDrawable(this);
        RelativeLayout.LayoutParams rel_lay = new RelativeLayout.LayoutParams(btnSide, btnSide); // ширина и высота создаваемой кнопки
        rel_lay.leftMargin = (int)x; // отступ от левой границы
        rel_lay.topMargin = (int)y; // отступ от верхней границы
        vershNew.setLayoutParams(rel_lay); // задание кнопке указанных параметров
        vershNew.setId(buttonId); // задание кнопке id
        text.setText(Integer.toString(++buttonId)); // задание кнопке текста
        text.setTextColor(Color.WHITE); // цвет текста
        vershNew.setColor(getResources().getColor(blue)); // цвет вершины
        vershNew.setImageDrawable(text);
        vershiny.add(vershNew);
        rlmain.addView(vershNew); // добавление кнопки на главный экран
        registerForContextMenu(vershNew); // для дальнейшего вызова контекстного меню
        vershNew.setOnClickListener(this); // навешивание слушателя
        if (switchMove.isChecked()) vershNew.setOnTouchListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_info):
                break;
            case (R.id.action_matrix):
                createMatrix();
                Intent intent = new Intent(MainActivity.this, MatrixActivity.class);
                // TODO сделать загрузку не через экстры, а при помощи БД
                intent.putExtra("smej", smejVerBool);
                intent.putExtra("incid", incidVerBool);
                startActivity(intent);
                break;

            case (R.id.action_save_graph):
                saveData();
                break;

            case (R.id.action_load_graph):
                break;

            case (android.R.id.home):
                showDialog(DIALOG_EXIT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // метод для вызова контекстного меню
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        aWhile = v;
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, MENU_DEL_VER, Menu.NONE, getString(R.string.cont_delete));
        SubMenu subMenu = menu.addSubMenu(getString(R.string.cont_sub_del_rebra)); // подменю
        for (int i = 0; i < lines.size(); i++) {
            if (aWhile == lines.get(i).firstBtn || aWhile == lines.get(i).secondBtn) {
                subMenu.add(Menu.NONE, i, Menu.NONE, "№ ребра: " + (i + 1));
            }
        }
        menu.add(Menu.NONE, MENU_LINE, Menu.NONE, getString(R.string.cont_rebro));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        StringBuilder message;
        switch (item.getItemId()) {
            // удаление вершины
            case MENU_DEL_VER:
                ArrayList<DrawLine> linesToDel = new ArrayList<>();
                //ArrayList<View> verToDel = new ArrayList<>();
                message = new StringBuilder("Вершина удалена"); // хз почему именно стринг баффер, надо почитать в инете

                for (int j = 0; j < vershiny.size(); j++) {
                    if (vershiny.get(j) == aWhile) {
                        vershiny.get(j).setVisibility(View.GONE);
                        for (int i = 0; i < lines.size(); i++) {
                            if (lines.get(i).firstBtn == vershiny.get(j) || lines.get(i).secondBtn == vershiny.get(j)) {
                                lines.get(i).setVisibility(View.GONE);
                                linesToDel.add(lines.get(i)); // массив для дальнейшего удаления
                            }
                        }
                    }
                }
                lines.removeAll(linesToDel); // вся эта хрень выше -- для нормального удаления связей и вершин
                linesToDel.clear(); // освобождение памяти
                break;
                // создание линии
            case MENU_LINE:
                message = new StringBuilder("Нажмите на вершину");
                lineCreating = true;
                whatColor = true;
                break;
            default:
                return super.onContextItemSelected(item);
        }
        Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show();
        return false;
    }

    // функция для создания перетаскивания вершин
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            // ACTION_DOWN срабатывает при прикосновении к экрану,
            // здесь определяется начальное стартовое положение объекта:
            case MotionEvent.ACTION_DOWN:
                // создание
                if (switchAdd.isChecked()) {
                    createVer(event.getX(), event.getY());
                    // TODO убрать этот костыль с сохранением
                    dataSaved = false;
                }
                else {
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                }
                break;
            // ACTION_MOVE обрабатывает случившиеся в процессе прикосновения изменения, здесь
            // содержится информация о последней точке, где находится объект после окончания действия прикосновения ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // перетаскивание
                if (vershiny.contains(v)) {
                    v.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();

                    // чтобы линии тоже отрисовывались вместе с движением вершины
                    if (!lines.isEmpty())
                        for (DrawLine line : lines) {
                            for (View smejBtn : vershiny) {
                                if (smejBtn == line.secondBtn)
                                    line.setCoords(line.firstBtn, smejBtn);
                                if (smejBtn == line.firstBtn)
                                    line.setCoords(smejBtn, line.secondBtn);
                            }
                            line.draw();
                        }
                }
                dataSaved = false;
                break;
        }
        return true;
    }

    // метод для навешивания слушателя всем вершинам
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) for (View ver : vershiny) ver.setOnTouchListener(this);
        else for (View ver : vershiny) ver.setOnTouchListener(null);
    }

    // кнопка сохранения графа, мб также вписать в меню
    public void onClickBtnSave(View view) {

    }

    // кнопка загрузки графа, мб вписать в меню
    public void onClickBtnLoad(View view) {
        mDb = mDBHelper.getWritableDatabase();
        mDb.delete("graphs", null, null);
        mDb.close();
    }

    public void onClickBtnList(View view) {

    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_EXIT) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle("Выход");
            // сообщение
            adb.setMessage("Сохранить данные?");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);
            // кнопка положительного ответа
            adb.setPositiveButton("Да", myClickListener);
            // кнопка отрицательного ответа
            adb.setNegativeButton("Нет", myClickListener);
            // кнопка нейтрального ответа
            adb.setNeutralButton("Отмена", myClickListener);
            // создаем диалог
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    saveData();
                    finAct();
                    //finish();
                    break;
                // негативная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    finAct();
                    //finish();
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };

    private void finAct() {
        Intent intentList = new Intent(MainActivity.this, GraphListActivity.class);
        startActivity(intentList);
    }

    void saveData() {
        int rr = 0;
        StringBuilder vers = new StringBuilder();
        for (View ver:
                vershiny) {
            Log.d(Integer.toString(rr++), "x: " + ver.getX() + ", y: " + ver.getY());
            vers.append(ver.getX()).append(" ").append(ver.getY()).append("\n");
        }

        mDb = mDBHelper.getWritableDatabase();

        createMatrix();
        // строка матрицы смежности для записи в БД
        StringBuilder smejMatBool = new StringBuilder();
        if (smejVerBool.length != 0) {
            for (int j = 1; j <= smejVerBool.length; j++) {
                for (int i = 0; i < j; i++) {
                    if (smejVerBool[j-1][i]) smejMatBool.append("1 ");
                    else smejMatBool.append("0 ");
                }
                smejMatBool.append("\n");
            }
        }

        ArrayList<String> product1 = new ArrayList<>();
        cv = new ContentValues();
        cv.clear();
        cv.put("gVers", vers.toString());
        cv.put("gName", gName);
        cv.put("gMatS", smejMatBool.toString());

        // просто выборка имён графов
        Cursor cursor1 = mDb.rawQuery("SELECT gName FROM graphs", null);
        int idColName1 = cursor1.getColumnIndex("gName");
        cursor1.moveToFirst();
        // и проходится по всем строкам, выбирая нужные колонки
        while (!cursor1.isAfterLast()) {
            product1.add(cursor1.getString(idColName1));
            cursor1.moveToNext();
        }
        cursor1.close();

        if (!product1.contains(gName)) {
            rowID = mDb.insert("graphs", null, cv);
        } else {
            mDb.update("graphs", cv, "gName = ?", new String[] {gName});
        }
        Log.d("dood", String.valueOf(rowID));

        mDb.close();
        Toast.makeText(this, "Граф " + gName + " сохранён", Toast.LENGTH_SHORT).show();

        dataSaved = true;
    }

    void createMatrix(){
        smejVerBool = new boolean[vershiny.size()][vershiny.size()];
        incidVerBool = new boolean[vershiny.size()][lines.size()];
        for (int i = 0; i < vershiny.size(); i++) {
            for (int j = 0; j < lines.size(); j++) {
                if (vershiny.get(i) == lines.get(j).secondBtn) {
                    smejVerBool[vershiny.get(i).getId()][vershiny.indexOf(lines.get(j).firstBtn)] = true;
                }/*
                        if (vershiny.get(i) == lines.get(j).firstBtn) {
                            smejVerBool[vershiny.get(i).getId()][vershiny.indexOf(lines.get(j).secondBtn)] = true;
                        }*/
                if (vershiny.get(i) == lines.get(j).secondBtn || vershiny.get(i) == lines.get(j).firstBtn) {
                    incidVerBool[vershiny.get(i).getId()][j] = true;
                }
            }
        }
    }
}
