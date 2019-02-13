package com.example.dmitry.dinamic_creating;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import at.markushi.ui.CircleButton;

public class MainActivity extends AppCompatActivity implements Serializable, View.OnClickListener, View.OnTouchListener, CompoundButton.OnCheckedChangeListener {
    RelativeLayout rlmain;
    ArrayList<DrawLine> lines; // массив со связями
    ArrayList<View> vershiny; // массив со всеми вершинами

    boolean[][] smejVerBool;
    int[][] incidVerCount;
    float dp;
    int buttonId = 0, btnSide, width=0, height=0; // подсчёт кнопок
    private float dX, dY;
    View aWhile;
    Switch switchMove;
    int blue = R.color.rebroColor,
            red = R.color.dugaColor,
            btnMainColor = Color.DKGRAY,
            bgColor = R.color.BGColor;
    boolean lineCreating = false, whatColor;
    final int MENU_DEL_VER = 1002,
            MENU_DEL_LINE = 1003,
            MENU_LINE = 1004;

    final String TAG = "GraDes";
    Random random = new Random();
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
        vershiny = new ArrayList<>();
        lines = new ArrayList<>();
        dp = getResources().getDisplayMetrics().density;
        btnSide = (int) (42*dp);
        switchMove = findViewById(R.id.switchMove);
        if (switchMove != null) switchMove.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (vershiny.contains(view) && vershiny.contains(aWhile)) {
            if (lineCreating) {
                DrawLine mDrawLine = new DrawLine(this);
                mDrawLine.setCoords(aWhile, view);
                mDrawLine.draw();
                if (whatColor) {
                    mDrawLine.setColor(getResources().getColor(blue));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_create):
                CircleButton vershNew = new CircleButton(this);
                TextDrawable text = new TextDrawable(this);
                RelativeLayout.LayoutParams rel_lay = new RelativeLayout.LayoutParams(btnSide, btnSide); // ширина и высота создаваемой кнопки
                rel_lay.leftMargin = random.nextInt(rlmain.getWidth() - btnSide); // рандомный отступ от левой границы
                rel_lay.topMargin = random.nextInt(rlmain.getHeight() - btnSide); // рандомный отступ от верхней границы
                vershNew.setLayoutParams(rel_lay); // задание кнопке указанных параметров
                vershNew.setId(buttonId);
                text.setText(Integer.toString(++buttonId));
                text.setTextColor(Color.WHITE);
                vershNew.setColor(btnMainColor);
                vershNew.setImageDrawable(text);
                vershiny.add(vershNew);
                rlmain.addView(vershNew); // добавление кнопки на главный экран
                registerForContextMenu(vershNew); // для дальнейшего вызова контекстного меню
                vershNew.setOnClickListener(this); // навешивание слушателя
                if (switchMove.isChecked()) vershNew.setOnTouchListener(this);
                break;
            case (R.id.action_info):
                break;
            case (R.id.action_matrix):
                smejVerBool = new boolean[vershiny.size()][vershiny.size()];
                incidVerCount = new int[vershiny.size()][lines.size()];
                int[] versList = new int[vershiny.size()];
                int[] linesList = new int[lines.size()];
                for (int i = 0; i < vershiny.size(); i++) {
                    versList[i] = vershiny.get(i).getId();
                }
                // TODO сюда же встроить подсчёт кол-ва рёбер для матрицы индидентности
                for (int i = 0; i < vershiny.size(); i++) {
                    for (int j = 0; j < lines.size(); j++) {
                        if (vershiny.get(i) == lines.get(j).secondBtn) {
                            smejVerBool[vershiny.get(i).getId()][vershiny.indexOf(lines.get(j).firstBtn)] = true;
                            incidVerCount[vershiny.get(i).getId()][j]++;
                            // TODO переделать эту обоссанную ошибку с индексом, который больше длины
                        }
                    }
                }
                Intent intent = new Intent(MainActivity.this, MatrixActivity.class);
                intent.putExtra("smej", smejVerBool);
                intent.putExtra("incid", incidVerCount);
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
                subMenu.add(Menu.NONE, i, Menu.NONE, "№ ребра: " + Integer.toString(i+1));
            }
        }
        menu.add(Menu.NONE, MENU_LINE, Menu.NONE, getString(R.string.cont_rebro));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        StringBuilder message;
        switch (item.getItemId()) {
            case MENU_DEL_VER:
                ArrayList<DrawLine> linesToDel = new ArrayList<>();
                //ArrayList<View> verToDel = new ArrayList<>();
                message = new StringBuilder("Вершина удалена"); // хз почему именно стринг баффер, надо почитать в инете

                for (int j = 0; j < vershiny.size(); j++) {
                    if (vershiny.get(j) == aWhile) {
                        vershiny.get(j).setVisibility(View.GONE);
                        for (int i = 0; i < lines.size(); i++) {
                            if (lines.get(i).firstBtn == vershiny.get(j) || lines.get(i).secondBtn == vershiny.get(j)) {
                                message.append(Integer.toString(i)).append(" ");
                                lines.get(i).setVisibility(View.GONE);
                                linesToDel.add(lines.get(i)); // массив для дальнейшего удаления
                            }
                        }
                        //verToDel.add(vershiny.get(j)); // вот эта хрень
                    }
                }
                Log.i(TAG, "Lines size1: " + Integer.toString(lines.size()));
                lines.removeAll(linesToDel); // вся эта хрень выше -- для нормального удаления связей и вершин
                Log.i(TAG, "Lines size2: " + Integer.toString(lines.size()) + "\nVer size 1: " + Integer.toString(vershiny.size()));
                //vershiny.removeAll(verToDel); // трогай, плиз
                Log.i(TAG, "Ver size2: " + Integer.toString(vershiny.size()));
                linesToDel.clear(); // освобождение памяти
                //verToDel.clear();
                break;
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

    // метод для перемещения вершины по экрану
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // ACTION_DOWN срабатывает при прикосновении к экрану,
            // здесь определяется начальное стартовое положение объекта:
            case MotionEvent.ACTION_DOWN:
                dX = v.getX() - event.getRawX();
                dY = v.getY() - event.getRawY();
                break;
            // ACTION_MOVE обрабатывает случившиеся в процессе прикосновения изменения, здесь
            // содержится информация о последней точке, где находится объект после окончания действия прикосновения ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                v.animate()
                        .x(event.getRawX() + dX)
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();

                if (!lines.isEmpty())
                    for (DrawLine line : lines) {
                        for (View smejBtn : vershiny) {
                            if (smejBtn == line.secondBtn) line.setCoords(line.firstBtn, smejBtn);
                            if (smejBtn == line.firstBtn) line.setCoords(smejBtn, line.secondBtn);
                        }
                        line.draw();
                    }
                break;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) for (View ver : vershiny) ver.setOnTouchListener(this);
        else for (View ver : vershiny) ver.setOnTouchListener(null);
    }
}
