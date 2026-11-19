
package com.devostudios.brastoffyting;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

final class GameRenderer implements GLSurfaceView.Renderer {
    private final float[] projection = new float[16];
    private final float[] view = new float[16];
    private final float[] model = new float[16];
    private final float[] mvp = new float[16];
    private int program, positionHandle, colorHandle, mvpHandle;
    private FloatBuffer cubeBuffer;
    private float yaw = 0f, pitch = 0f;

    private static final float[] CUBE = {
        -1,-1,-1,  1,-1,-1,  1, 1,-1, -1, 1,-1,
        -1,-1, 1,  1,-1, 1,  1, 1, 1, -1, 1, 1
    };
    private static final short[] INDICES = {
        0,1,2, 0,2,3, 4,6,5, 4,7,6,
        0,4,5, 0,5,1, 3,2,6, 3,6,7,
        1,5,6, 1,6,2, 0,3,7, 0,7,4
    };
    private java.nio.ShortBuffer indexBuffer;

    void look(float dx, float dy) {
        yaw += dx * 0.25f;
        pitch = Math.max(-35f, Math.min(35f, pitch + dy * 0.18f));
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.38f, 0.48f, 0.52f, 1f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        String vertex =
            "attribute vec3 aPosition;" +
            "uniform mat4 uMvp;" +
            "void main(){ gl_Position = uMvp * vec4(aPosition,1.0); }";
        String fragment =
            "precision mediump float;" +
            "uniform vec4 uColor;" +
            "void main(){ gl_FragColor = uColor; }";
        program = createProgram(vertex, fragment);
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        colorHandle = GLES20.glGetUniformLocation(program, "uColor");
        mvpHandle = GLES20.glGetUniformLocation(program, "uMvp");

        ByteBuffer vb = ByteBuffer.allocateDirect(CUBE.length * 4).order(ByteOrder.nativeOrder());
        cubeBuffer = vb.asFloatBuffer(); cubeBuffer.put(CUBE).position(0);
        ByteBuffer ib = ByteBuffer.allocateDirect(INDICES.length * 2).order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer(); indexBuffer.put(INDICES).position(0);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.perspectiveM(projection, 0, 62f, (float) width / Math.max(1, height), 0.1f, 100f);
    }

    @Override public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        float angle = (float)Math.toRadians(yaw);
        float px = (float)Math.sin(angle) * 6f;
        float pz = (float)Math.cos(angle) * 6f;
        Matrix.setLookAtM(view, 0, px, 2.7f + pitch * 0.025f, pz,
                0f, 0.8f, 0f, 0f, 1f, 0f);

        drawBox(0f, -0.25f, 0f, 18f, 0.25f, 18f, 0.25f, 0.29f, 0.27f, 1f);
        drawBox(0f, 1.0f, -8f, 18f, 2f, 0.25f, 0.34f, 0.37f, 0.35f, 1f);
        drawBox(-8f, 1.0f, 0f, 0.25f, 2f, 18f, 0.30f, 0.34f, 0.32f, 1f);
        drawBox(8f, 1.0f, 0f, 0.25f, 2f, 18f, 0.30f, 0.34f, 0.32f, 1f);
        drawBox(-2.8f, 0.55f, -2.5f, 0.9f, 0.55f, 0.9f, 0.43f, 0.30f, 0.20f, 1f);
        drawBox(2.5f, 0.75f, -4.2f, 1.1f, 0.75f, 0.8f, 0.38f, 0.39f, 0.34f, 1f);
        drawBox(0.0f, 0.45f, 2.8f, 1.4f, 0.45f, 0.65f, 0.40f, 0.31f, 0.22f, 1f);
        drawBox(-4.8f, 0.45f, 4.0f, 0.8f, 0.45f, 0.8f, 0.36f, 0.39f, 0.34f, 1f);
        drawBox(4.5f, 0.45f, 4.5f, 0.8f, 0.45f, 0.8f, 0.36f, 0.39f, 0.34f, 1f);
        // Simple weapon silhouette in the lower-right foreground.
        drawBox(0.85f, -0.55f, -1.5f, 0.16f, 0.12f, 0.62f, 0.10f, 0.12f, 0.12f, 1f);
        drawBox(0.83f, -0.72f, -1.25f, 0.11f, 0.22f, 0.16f, 0.08f, 0.09f, 0.09f, 1f);
    }

    private void drawBox(float x, float y, float z, float sx, float sy, float sz,
                         float r, float g, float b, float a) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.scaleM(model, 0, sx, sy, sz);
        float[] mv = new float[16];
        Matrix.multiplyMM(mv, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, projection, 0, mv, 0);
        GLES20.glUseProgram(program);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0);
        GLES20.glUniform4f(colorHandle, r, g, b, a);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDICES.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    private int createProgram(String vertex, String fragment) {
        int vs = compile(GLES20.GL_VERTEX_SHADER, vertex);
        int fs = compile(GLES20.GL_FRAGMENT_SHADER, fragment);
        int p = GLES20.glCreateProgram();
        GLES20.glAttachShader(p, vs); GLES20.glAttachShader(p, fs);
        GLES20.glLinkProgram(p);
        int[] status = new int[1];
        GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) throw new RuntimeException("Shader link failed: " + GLES20.glGetProgramInfoLog(p));
        return p;
    }

    private int compile(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) throw new RuntimeException("Shader compile failed: " + GLES20.glGetShaderInfoLog(shader));
        return shader;
    }
}
