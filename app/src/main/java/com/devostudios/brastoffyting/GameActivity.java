
package com.devostudios.brastoffyting;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class GameActivity extends Activity {
    private GameRenderer renderer;
    private float lastX, lastY;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FrameLayout root = new FrameLayout(this);
        GLSurfaceView surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(2);
        renderer = new GameRenderer();
        surface.setRenderer(renderer);
        surface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surface.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastX = event.getX(); lastY = event.getY(); return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = event.getX() - lastX;
                float dy = event.getY() - lastY;
                lastX = event.getX(); lastY = event.getY();
                renderer.look(dx, dy);
                return true;
            }
            return true;
        });
        root.addView(surface);
        TextView hud = new TextView(this);
        hud.setText("TRAINING RANGE  •  DRAG TO LOOK\nPROTOTYPE BUILD");
        hud.setTextColor(0xffffffff);
        hud.setTextSize(12);
        hud.setPadding(24, 18, 24, 18);
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT);
        root.addView(hud, hp);
        TextView exit = new TextView(this);
        exit.setText("EXIT");
        exit.setTextColor(0xffffffff);
        exit.setTextSize(14);
        exit.setPadding(28, 18, 28, 18);
        exit.setBackgroundColor(0xaa9a4828);
        exit.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams ep = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.RIGHT);
        root.addView(exit, ep);
        setContentView(root);
    }
}
