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

public class MainActivity extends AppCompatActivity implements Serializable, View.OnClickListener, View.OnTouchListener, CompoundButton.OnCheckedChangeListener {
    RelativeLayout rlmain;
    ArrayList<DrawLine> lines; // массив со связями
    ArrayList<FloatingActionButton> vershiny; // массив со всеми вершинами
    boolean[][] smejVerBool, incidVerBool;
    StringBuilder smejMatBool, incidMatBool;
    float dp;
    int buttonId = 0, lineId = 0, btnSide;
    private float dX, dY;
    View aWhile;
    Switch switchMove, switchAdd;
    int red = R.color.dugaColor,
            bgColor = R.color.BGColor;
    boolean lineCreating = false,
            whatColor,
            dataSaved = true;
    final int MENU_DEL_VER = 1002,
            MENU_LINE = 1004,
            MENU_DEL_LINE = 1003;

    int IDg;
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

        gName = (String) getIntent().getSerializableExtra("gName");

        IDg = (int) getIntent().getSerializableExtra("IDg");
        setTitle(gName);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
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
        if (switchAdd != null) switchAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked) {
                    switchMove.setChecked(false);
                    switchMove.setClickable(false);
                } else {
                    switchMove.setClickable(true);
                }
            }
        });

        mDBHelper = new DatabaseHelper(this);
        mDb = mDBHelper.getReadableDatabase();

        ArrayList<String> product1 = new ArrayList<>();
        Cursor cursor1 = mDb.rawQuery("SELECT gName FROM graphs", null);
        int idColName1 = cursor1.getColumnIndex("gName");
        cursor1.moveToFirst();
        while (!cursor1.isAfterLast()) {
            product1.add(cursor1.getString(idColName1));
            cursor1.moveToNext();
        }
        cursor1.close();
        if (product1.contains(gName)) {
            loadGraph();
        }
    }

    // слушатель, который навешивается на каждую создаваемую вершину
    @Override
    public void onClick(View view) {
        if (lineCreating) createLine(view, aWhile);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) for (View ver : vershiny) ver.setOnTouchListener(this);
        else for (View ver : vershiny) ver.setOnTouchListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_matrix):
                saveData();
                Intent intent = new Intent(MainActivity.this, MatrixActivity.class);
                intent.putExtra("IDg", IDg);
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
        aWhile = v; // кстати, крутой костыль. Это самому себе на будущее
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, MENU_DEL_VER, Menu.NONE, getString(R.string.cont_delete));
        for (DrawLine line: lines) {
            if (line.firstBtn == aWhile || line.secondBtn == aWhile) {
                SubMenu subMenu = menu.addSubMenu(Menu.NONE, MENU_DEL_LINE, Menu.NONE, getString(R.string.cont_sub_del_rebra)); // подменю для удаления рёбер
                for (int i = 0; i < lines.size(); i++) {
                    if (aWhile == lines.get(i).firstBtn || aWhile == lines.get(i).secondBtn) {
                        subMenu.add(Menu.NONE, i, Menu.NONE, "№ ребра: " + (i + 1));
                    }
                }
                break;
            }
        }
        menu.add(Menu.NONE, MENU_LINE, Menu.NONE, getString(R.string.cont_rebro));
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String message = "";
        switch (item.getItemId()) {
            // удаление вершины
            case MENU_DEL_VER:
                message = "Вершина удалена"; // хз почему именно стринг баффер, надо почитать в инете

                for (int j = 0; j <  vershiny.size(); j++)
                    if (vershiny.get(j) == aWhile) {
                        vershiny.get(j).setVisibility(View.GONE);
                        for (int i = 0; i < lines.size(); i++)
                            if (lines.get(i).firstBtn == vershiny.get(j) || lines.get(i).secondBtn == vershiny.get(j)) {
                                delLine(i);
                                i--;
                            }
                    }

                for (int i = 0; i < vershiny.size(); i++)
                    if (vershiny.get(i).getId() > aWhile.getId()) {
                        TextDrawable text = new TextDrawable(this);
                        vershiny.get(i).setId(vershiny.get(i).getId() - 1); // задание кнопке id
                        text.setText(Integer.toString((vershiny.get(i).getId()) + 1)); // задание кнопке текста
                        text.setTextColor(Color.WHITE); // цвет текста
                        vershiny.get(i).setImageDrawable(text); // навешивание текста
                    }

                aWhile.setVisibility(View.GONE);
                vershiny.remove(aWhile);

                buttonId--;
                dataSaved = false;
                break;
                // создание линии
            case MENU_LINE:
                message = "Нажмите на вершину";
                lineCreating = true;
                whatColor = true;
                dataSaved = false;
                break;
            case MENU_DEL_LINE: // тоже отличный костыль, спасибо стэк оверфлоу за подсказку
                break;
            default:
                int itemId = item.getItemId();
                delLine(itemId);
                return super.onContextItemSelected(item);
        }
        if (!message.equals(""))
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                if (switchAdd.isChecked()) {
                    createVer(event.getX(), event.getY());
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

    private void finAct() {
        Intent intentList = new Intent(MainActivity.this, GraphListActivity.class);
        startActivity(intentList);
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
        vershNew.getBackground().setColorFilter(Color.parseColor("#5581a6"), PorterDuff.Mode.MULTIPLY);
        vershNew.setImageDrawable(text); // навешивание текста
        vershiny.add(vershNew); // добавление в список вершин
        rlmain.addView(vershNew); // добавление кнопки на главный экран
        registerForContextMenu(vershNew); // для дальнейшего вызова контекстного меню
        vershNew.setOnClickListener(this); // навешивание слушателя
        if (switchMove.isChecked())
            vershNew.setOnTouchListener(this); // навешивание слушателя для перетаскивания
    }

    private void createLine(View ver1, View ver2) {
        if (vershiny.contains(ver1) && vershiny.contains(ver2)) {
            DrawLine mDrawLine = new DrawLine(this);
            mDrawLine.setCoords(ver2, ver1, btnSide);
            mDrawLine.setId(lineId);
            mDrawLine.number = ++lineId;
            mDrawLine.draw();
            if (whatColor) mDrawLine.setColor(getResources().getColor(red));
            else mDrawLine.setColor(getResources().getColor(red));

            lineCreating = false;
            lines.add(mDrawLine);
            rlmain.addView(mDrawLine);
            ver1.bringToFront(); // чтобы вершины были впереди линий
            ver2.bringToFront();
        }
    }

    void delLine(int lineToDel) {
        lines.get(lineToDel).setVisibility(View.GONE);

        for (int i = 0; i < lines.size(); i++)
            if (lines.get(i).number > lineToDel) {
                lines.get(i).number--;
                lines.get(i).draw();
            }
        lines.remove(lineToDel);
        lineId--;
        dataSaved = false;
    }

    void createMatrix(){
        smejMatBool = new StringBuilder();
        incidMatBool = new StringBuilder();
        smejVerBool = new boolean[vershiny.size()][vershiny.size()];
        incidVerBool = new boolean[vershiny.size()][lines.size()];
        for (int i = 0; i < vershiny.size(); i++)
            for (int j = 0; j < lines.size(); j++) {
                if (vershiny.get(i) == lines.get(j).secondBtn)
                    smejVerBool[i][vershiny.indexOf(lines.get(j).firstBtn)] = true;
                if (vershiny.get(i) == lines.get(j).secondBtn || vershiny.get(i) == lines.get(j).firstBtn)
                    incidVerBool[i][j] = true;
            }

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
    }

    private void loadGraph() {
        String versCoordsFromDB = "";
        String matIncidFromDB = "";

        Cursor cursor1 = mDb.query("graphs",
                new String[] {"gVers", "gMatI"},
                "IDg = ?",
                new String[] {String.valueOf(IDg)},
                null,
                null,
                null);
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

        if (versCoordsFromDB != null && !versCoordsFromDB.equals("")) {
            String[] versCoordsRaw = versCoordsFromDB.split("\n");
            Float[][] versCoords = new Float[versCoordsRaw.length][3];

            for (int i = 0; i < versCoordsRaw.length; i++)
                for (int j = 0; j < versCoordsRaw[i].split(" ").length; j++)
                    versCoords[i][j] = Float.valueOf(versCoordsRaw[i].split(" ")[j]);
            for (Float[] so : versCoords)
                createVer(so[0], so[1]);
        }

        if (matIncidFromDB != null && !matIncidFromDB.equals("")) {
            String[] matIncidRaw = matIncidFromDB.split("\n");
            if (matIncidRaw.length > 0) {
                String[][] matIncidRaw2 = new String[matIncidRaw.length][matIncidRaw[0].length() / 2];

                for (int i = 0; i < matIncidRaw.length; i++)
                    System.arraycopy(matIncidRaw[i].split(" "), 0, matIncidRaw2[i], 0, matIncidRaw[i].split(" ").length);

                String[][] matIncidFin = new String[matIncidRaw2[0].length][matIncidRaw2.length];
                int retArrI = 0;
                int retArrJ = 0;

                for (String[] srI : matIncidRaw2) {
                    for (String srJ : srI) matIncidFin[retArrI++][retArrJ] = (srJ);
                    retArrI = 0;
                    retArrJ++;
                }

                for (int i = 0; i < matIncidFin.length; i++) {
                    int fB, sB;
                    fB = TextUtils.join("", matIncidFin[i]).indexOf("1");
                    sB = TextUtils.join("", matIncidFin[i]).indexOf("1", fB + 1);
                    if (fB != -1)
                        if (sB == -1) createLine(vershiny.get(fB), vershiny.get(fB));
                        else createLine(vershiny.get(sB), vershiny.get(fB));
                }
            }
        }
    }

    void saveData() {
        createMatrix();
        StringBuilder vers = new StringBuilder();
        for (View ver: vershiny)
            vers.append(ver.getX()).append(" ").append(ver.getY()).append("\n");
        // cv -- переменная для нормальной вставки записей в таблицу
        cv = new ContentValues();
        cv.clear();
        cv.put("gName", gName);
        cv.put("gVers", vers.toString());
        cv.put("gMatS", smejMatBool.toString());
        cv.put("gMatI", incidMatBool.toString());

        mDb.update("graphs", cv, "IDg = ?", new String[] {String.valueOf(IDg)});

        Toast.makeText(this, "Граф " + gName + " сохранён", Toast.LENGTH_SHORT).show();

        dataSaved = true;
    }

    // метод, срабатывающий при нажатии кнопки "назад"
    public void onBackPressed() {
        if (!dataSaved) openQuitDialog();
        else finAct();
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
