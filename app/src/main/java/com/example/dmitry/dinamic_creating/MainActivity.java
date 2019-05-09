package com.example.dmitry.dinamic_creating;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements Serializable, View.OnClickListener, View.OnTouchListener, CompoundButton.OnCheckedChangeListener {
    RelativeLayout rlmain;
    ArrayList<DrawLine> lines; // массив со связями
    ArrayList<View> vershiny; // массив со всеми вершинами
    boolean[][] smejVerBool, incidVerBool;
    float dp;
    int buttonId = 0, lineId = 0, btnSide, width = 0, height = 0; // подсчёт кнопок
    private float dX, dY;
    View aWhile;
    Switch switchMove, switchAdd;
    int red = R.color.dugaColor,
            bgColor = R.color.BGColor;
    boolean lineCreating = false,
            whatColor,
            dataSaved = true;
    final int MENU_DEL_VER = 1002,
            MENU_LINE = 1004;

    String gName;
    DatabaseHelper mDBHelper;
    SQLiteDatabase mDb;
    ContentValues cv;

    AlertDialog.Builder adExit;

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
        btnSide = (int) (35 * dp);
        switchMove = findViewById(R.id.switchMove);
        switchAdd = findViewById(R.id.switchAdd);
        if (switchMove != null) switchMove.setOnCheckedChangeListener(this);

        mDBHelper = new DatabaseHelper(this);
        mDb = mDBHelper.getReadableDatabase();

        gName = (String) getIntent().getSerializableExtra("gName");
        Log.d("geg", gName);

        ArrayList<String> product1 = new ArrayList<>();
        Cursor cursor1 = mDb.rawQuery("SELECT gName FROM graphs", null);
        int idColName1 = cursor1.getColumnIndex("gName");
        cursor1.moveToFirst();
        // и проходится по всем строкам, выбирая нужные колонки
        while (!cursor1.isAfterLast()) {
            product1.add(cursor1.getString(idColName1));
            cursor1.moveToNext();
        }
        cursor1.close();
        if (product1.contains(gName)) {
            loadGraph(gName);
            loadLines(gName);
            Log.d("ssaka X", vershiny.get(0).getX() + " ");
        } else {
            //mDb.update("graphs", cv, "gName = ?", new String[] {gName});
        }
    }

    private void loadGraph(String gName1) {
        String versCoordsFromDB = "";
        String matIncidFromDB = "";

        Cursor cursor1 = mDb.query("graphs",
                new String[] {"gVers", "gMatI"},
                "gName = ?",
                new String[] {gName1},
                null,
                null,
                null);
        //int idColName1 = cursor1.getColumnIndex("gName");
        int idColVers = cursor1.getColumnIndex("gVers");
        int idColMatI = cursor1.getColumnIndex("gMatI");
        cursor1.moveToFirst();
        // и проходится по всем строкам, выбирая нужные колонки
        while (!cursor1.isAfterLast()) {
            versCoordsFromDB = (cursor1.getString(idColVers));
            matIncidFromDB = (cursor1.getString(idColMatI));
            cursor1.moveToNext();
        }
        cursor1.close();

        String[] versCoordsRaw = {};
        Float[][] versCoords = {{}};
        if (!versCoordsFromDB.isEmpty()) {
            versCoordsRaw = versCoordsFromDB.split("\n");
            versCoords = new Float[versCoordsRaw.length][3];
        }

        String [] matIncidRaw = {};
        String [][] matIncidRaw2 = {{}};
        if (!matIncidFromDB.isEmpty()) {
            matIncidRaw = matIncidFromDB.split("\n");
            matIncidRaw2 = new String[matIncidRaw.length][matIncidRaw[0].length() / 2];
        }

        for (int i = 0; i < versCoordsRaw.length; i++) {
            for (int j = 0; j < versCoordsRaw[i].split(" ").length; j++) {
                versCoords[i][j] = Float.valueOf(versCoordsRaw[i].split(" ")[j]);
            }

            System.arraycopy(matIncidRaw[i].split(" "), 0, matIncidRaw2[i], 0, matIncidRaw[i].split(" ").length);
        }

        for (Float[] so : versCoords) {
            createVer(so[0], so[1]);
        }

        String [][] matIncidFin = new String[matIncidRaw2[0].length][matIncidRaw2.length];
        int retArrI = 0;
        int retArrJ = 0;
        /*for (int i = 0; i < matIncidRaw2.length; i++) {
            for (int j = 0; j < matIncidRaw2[i].length; j++) {
                matIncidFin[j][i] = matIncidRaw2[i][j];
            }
        }*/
        for (String [] srI: matIncidRaw2) {
            for (String srJ: srI) {
                matIncidFin[retArrI++][retArrJ] = (srJ);
            }
            retArrI = 0;
            retArrJ++;
            //Log.d("retArrIndex'ы", retArrI + " " + retArrJ);
        }


        for (int i = 0; i < matIncidFin.length; i++) {
                Log.d("fuka", TextUtils.join("", matIncidFin[i]) + " ");
        }


        for (int i = 0; i < matIncidFin.length; i++) {
            int fB, sB;
            fB = TextUtils.join("", matIncidFin[i]).indexOf("1");
            sB = TextUtils.join("", matIncidFin[i]).indexOf("1", fB+1);
            if (fB != -1) {
                if (sB == -1) {
                    createLine(vershiny.get(fB), vershiny.get(fB));
                } else {
                    createLine(vershiny.get(sB), vershiny.get(fB));
                }
                Log.d("index's", fB + " " + sB);
            }
        }
        for (int i = 0; i < matIncidFin.length; i++) {
            //Log.d("row", i + " " + matIncidFin[i] + " " + matIncidFin[i].toString().indexOf("1"));
        }
        //createLine(vershiny.get(0), vershiny.get(1));
        //rlmain.requestLayout();
        //Log.d("array check", matIncidRaw2[0][0]);
    }

    void loadLines(String gName2){
        //String versCoords = "";
        String matI = "";
        Cursor cursor1 = mDb.query("graphs",
                new String[] {"gMatI"},
                "gName = ?",
                new String[] {gName2},
                null,
                null,
                null);
        //int idColName1 = cursor1.getColumnIndex("gName");
        //int idColVers = cursor1.getColumnIndex("gVers");
        int idColMatI = cursor1.getColumnIndex("gMatI");
        cursor1.moveToFirst();
        // и проходится по всем строкам, выбирая нужные колонки
        while (!cursor1.isAfterLast()) {
            matI = (cursor1.getString(idColMatI));
            cursor1.moveToNext();
        }
        cursor1.close();

        /*String [] matI1 = matI.split("\n");
        String [][] matI2 = new String[matI1.length][vershiny.size()];

        for (int i = 0; i < matI1.length; i++) {
            matI2[i] = matI1[i].split(" ");
        }*/
        /*for (int i = 0; i < sis.length; i++) {
            String [] sus = sas[i].split(" ");
            for (int j = 0; j < sus.length; j++) {
                sos[i][j] = Float.valueOf(sus[j]);
            }

            sqs [i] = sis[i].split(" ");
        }*/
        /*
        for (int r = 0; r < sqs.length; r++) {
            for (int w = 0; w < r; w++) {
                if (sqs[r][w].equals("1")) {
                    createLine(vershiny.get(w), vershiny.get(r));
                }
                Log.d("suka", sqs[1][0].equals("1") + " " + gName2 + vershiny.get(w).getY() + vershiny.get(r).getY());
            }
        }*/

        //Log.d("sukaLines", matI + " " + gName2);
    }

    // слушатель, который навешивается на каждую создаваемую вершину
    @Override
    public void onClick(View view) {
        if (lineCreating) {
            createLine(view, aWhile);
        }
        Log.d("Координаты вершины:", view.getY() + " " + view.getX());
    }

    private void createLine(View ver1, View ver2) {
        if (vershiny.contains(ver1) && vershiny.contains(ver2)) {
            DrawLine mDrawLine = new DrawLine(this);
            mDrawLine.setCoords(ver2, ver1, btnSide);
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
            ver1.bringToFront(); // чтобы вершины были впереди линий
            ver2.bringToFront();
        }
        //Log.d("Line creating", ver1.getX() + " " + ver1.getY() + " " + ver1.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    void createVer(float x, float y){

        FloatingActionButton vershNew = new FloatingActionButton(this);
        vershNew.setX(x);
        vershNew.setY(y);
        vershNew.setCustomSize(btnSide);

        TextDrawable text = new TextDrawable(this);
        vershNew.setId(buttonId); // задание кнопке id
        text.setText(Integer.toString(++buttonId)); // задание кнопке текста
        text.setTextColor(Color.WHITE); // цвет текста
        // цвет кнопки, ибо через ресурсы нормально не задашь
        vershNew.getBackground().setColorFilter(Color.parseColor("#5581a6"), PorterDuff.Mode.MULTIPLY);
        vershNew.setImageDrawable(text); // навешивание текста
        vershiny.add(vershNew); // добавление в список вершин
        rlmain.addView(vershNew); // добавление кнопки на главный экран
        registerForContextMenu(vershNew); // для дальнейшего вызова контекстного меню
        vershNew.setOnClickListener(this); // навешивание слушателя
        // навешивание слушателя для перетаскивания
        if (switchMove.isChecked()) vershNew.setOnTouchListener(this);

        //Log.d("Ver coords", vershNew.getX() + " " + vershNew.getY());
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

            case (android.R.id.home):
                onBackPressed();
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
        SubMenu subMenu = menu.addSubMenu(getString(R.string.cont_sub_del_rebra)); // подменю для удаления рёбер
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
                //aWhile.setVisibility(View.GONE);
                //vershiny.remove(aWhile);
                dataSaved = false;
                break;
                // создание линии
            case MENU_LINE:
                message = new StringBuilder("Нажмите на вершину");
                lineCreating = true;
                whatColor = true;
                dataSaved = false;
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
                                    line.setCoords(line.firstBtn, smejBtn, btnSide);
                                if (smejBtn == line.firstBtn)
                                    line.setCoords(smejBtn, line.secondBtn, btnSide);
                            }
                            line.draw();
                        }
                    dataSaved = false;
                }
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
    // TODO поудалять к хуям все эти методы кнопочные
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

    private void finAct() {
        Intent intentList = new Intent(MainActivity.this, GraphListActivity.class);
        startActivity(intentList);
    }

    void saveData() {
        int rr = 0;
        StringBuilder vers = new StringBuilder();
        for (View ver:
                vershiny) {
            //Log.d(Integer.toString(rr++), "x: " + ver.getX() + ", y: " + ver.getY());
            vers.append(ver.getX()).append(" ").append(ver.getY()).append("\n");
        }

        mDb = mDBHelper.getWritableDatabase();

        createMatrix();
        // строка матрицы смежности для записи в БД
        StringBuilder smejMatBool = new StringBuilder();
        StringBuilder incidMatBool = new StringBuilder();
        if (smejVerBool.length != 0) {
            for (int j = 1; j <= smejVerBool.length; j++) {
                for (int i = 0; i < j; i++) {
                    if (smejVerBool[j-1][i]) smejMatBool.append("1 ");
                    else smejMatBool.append("0 ");
                }
                smejMatBool.append("\n");

                for (int t = 0; t < incidVerBool[j - 1].length; t++) {
                    if (incidVerBool[j - 1][t]) incidMatBool.append("1 ");
                    else incidMatBool.append("0 ");
                }
                incidMatBool.append("\n");
            }
        }

        ArrayList<String> product1 = new ArrayList<>();

        // cv -- переменная для нормальной вставки записей в таблицу
        cv = new ContentValues();
        cv.clear();
        cv.put("gVers", vers.toString());
        cv.put("gName", gName);
        cv.put("gMatS", smejMatBool.toString());
        cv.put("gMatI", incidMatBool.toString());

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
        // если в БД нет графа с указанным именем -- просто вставка
        if (!product1.contains(gName)) {
            mDb.insert("graphs", null, cv);
            // иначе -- просто обновление
        } else {
            mDb.update("graphs", cv, "gName = ?", new String[] {gName});
        }

        mDb.close();
        Toast.makeText(this, "Граф " + gName + " сохранён", Toast.LENGTH_SHORT).show();

        dataSaved = true;

        //finAct();
    }

    // метод создания матриц
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


    // метод, срабатывающий при нажатии кнопки "назад"
    public void onBackPressed() {
        if (!dataSaved) {
            openQuitDialog();
        } else {
            finAct();
        }
        //Log.d("datasaved", String.valueOf(dataSaved));
    }

    // метод создания предупреждающего диалога
    void openQuitDialog(){
        adExit = new AlertDialog.Builder(this);
        adExit.setTitle("Выход");  // заголовок
        adExit.setMessage("Сохранить данные?"); // сообщение
        adExit.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                saveData();
                finAct();
            }
        });
        adExit.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                finAct();
            }
        });
        adExit.setCancelable(true);
        adExit.show();
    }
}
