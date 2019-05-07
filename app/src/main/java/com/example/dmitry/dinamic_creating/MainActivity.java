package com.example.dmitry.dinamic_creating;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
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
    boolean lineCreating = false, whatColor;
    final int MENU_DEL_VER = 1002, MENU_LINE = 1004;

    final String TAG = "GraDes";
    Random random = new Random();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        rel_lay.leftMargin = (int)x;//random.nextInt(rlmain.getWidth() - btnSide); // рандомный отступ от левой границы
        rel_lay.topMargin = (int)y;//random.nextInt(rlmain.getHeight() - btnSide); // рандомный отступ от верхней границы
        vershNew.setLayoutParams(rel_lay); // задание кнопке указанных параметров
        vershNew.setId(buttonId);
        text.setText(Integer.toString(++buttonId));
        text.setTextColor(Color.WHITE);
        vershNew.setColor(getResources().getColor(blue));
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
                Intent intent = new Intent(MainActivity.this, MatrixActivity.class);
                intent.putExtra("smej", smejVerBool);
                intent.putExtra("incid", incidVerBool);
                startActivity(intent);
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
        int rr = 0;
        for (View ver:
             vershiny) {
            Log.d(Integer.toString(rr++), "x: " + ver.getX() + ", y: " + ver.getY());
        }
    }

    // кнопка загрузки графа, мб вписать в меню
    public void onClickBtnLoad(View view) {
        rlmain.removeAllViews();
        vershiny.clear();
        buttonId = 0;
        lines.clear();
        lineId = 0;


        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(FILENAME)));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                Log.d("sees ", str);
                Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
