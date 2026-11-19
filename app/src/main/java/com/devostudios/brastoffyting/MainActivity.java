
package com.devostudios.brastoffyting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String SESSION_PREFS = "player_session";
    private static final String REFRESH_TOKEN = "refresh_token";
    private String accessToken;
    private TextView loadingStatus;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        restoreSessionOrShowLogin();
    }

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(196, 92, 37)));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(dp(24), dp(12), dp(24), dp(12));
        l.setBackgroundColor(Color.rgb(15, 19, 22));
        return l;
    }

    private EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setHint(hint);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.LTGRAY);
        e.setInputType(type);
        e.setPadding(dp(14), dp(8), dp(14), dp(8));
        e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(196, 92, 37)));
        return e;
    }

    private void restoreSessionOrShowLogin() {
        String refresh = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE)
                .getString(REFRESH_TOKEN, "");
        if (refresh == null || refresh.isEmpty()) {
            showAuth();
            return;
        }
        LinearLayout page = column();
        TextView title = text("BRAST OF FYTING", 30, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        page.addView(title);
        loadingStatus = text("CONNECTING TO OFFICIAL SERVICES...", 13, Color.LTGRAY);
        page.addView(loadingStatus);
        setContentView(page);
        AuthClient.refresh(refresh, (ok, message, session) -> runOnUiThread(() -> {
            if (ok && session != null) {
                saveSession(session);
                showLobby();
            } else {
                clearSession();
                showAuth();
                toast("Please sign in again to continue.");
            }
        }));
    }

    private void showAuth() {
        LinearLayout page = column();
        TextView title = text("BRAST OF FYTING", 30, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        page.addView(title);
        page.addView(text("A DEVO STUDIOS GAME  •  ONLINE COMBAT", 12, Color.rgb(196, 128, 82)));
        page.addView(text("SIGN IN OR CREATE YOUR PLAYER ACCOUNT", 14, Color.LTGRAY));

        EditText email = field("Email address", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);
        EditText password = field("Password (6+ characters)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        page.addView(email, new LinearLayout.LayoutParams(-1, dp(54)));
        page.addView(password, new LinearLayout.LayoutParams(-1, dp(54)));

        TextView status = text("An online player account is required.", 12, Color.LTGRAY);
        page.addView(status);
        page.addView(button("SIGN IN", () -> auth(email, password, status, false)));
        page.addView(button("CREATE ACCOUNT", () -> auth(email, password, status, true)));
        page.addView(text("Landscape mode  •  Version 1.0.8", 11, Color.GRAY));
        setContentView(page);
    }

    private void auth(EditText email, EditText password, TextView status, boolean signup) {
        String e = email.getText().toString().trim();
        String p = password.getText().toString();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches() || p.length() < 6) {
            status.setText("Enter a valid email and a password with at least 6 characters.");
            return;
        }
        status.setText("Connecting securely...");
        AuthClient.Callback callback = (ok, message, session) -> runOnUiThread(() -> {
            status.setText(message);
            if (ok && session != null) {
                saveSession(session);
                showLobby();
            }
        });
        if (signup) AuthClient.signUp(e, p, callback);
        else AuthClient.signIn(e, p, callback);
    }

    private void saveSession(AuthClient.Session session) {
        accessToken = session.accessToken;
        if (session.refreshToken != null && !session.refreshToken.isEmpty()) {
            getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit()
                    .putString(REFRESH_TOKEN, session.refreshToken).apply();
        }
    }

    private void clearSession() {
        accessToken = null;
        getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit().clear().apply();
    }

    private void showLobby() {
        LinearLayout page = column();
        TextView title = text("BRAST OF FYTING", 30, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        page.addView(title);
        page.addView(text("TACTICAL COMBAT  •  ONLINE", 13, Color.rgb(196, 128, 82)));

        LinearLayout official = new LinearLayout(this);
        official.setOrientation(LinearLayout.VERTICAL);
        official.setGravity(Gravity.CENTER);
        official.setPadding(dp(18), dp(14), dp(18), dp(14));
        official.setBackgroundColor(Color.rgb(31, 39, 42));
        TextView serverName = text("OFFICIAL SERVER", 20, Color.WHITE);
        serverName.setTypeface(Typeface.DEFAULT_BOLD);
        official.addView(serverName);
        official.addView(text("DEFAULT SERVER  •  AUTOMATICALLY SELECTED", 11, Color.rgb(196, 128, 82)));
        official.addView(text("Your account service is connected. Live game-server deployment is still pending.",
                12, Color.LTGRAY));
        page.addView(official, new LinearLayout.LayoutParams(-1, -2));

        page.addView(button("CONNECT TO OFFICIAL SERVER", () ->
                toast("Official Server is selected. Live multiplayer will be enabled when its dedicated game server is deployed.")));
        page.addView(button("PLAY TRAINING", () ->
                startActivity(new Intent(this, GameActivity.class))));
        page.addView(button("LOADOUT  •  COMING SOON", () ->
                toast("Weapon inventory is planned for a future update.")));
        page.addView(button("SIGN OUT", () -> {
            AuthClient.signOut(accessToken);
            clearSession();
            showAuth();
        }));
        page.addView(text("Version 1.0.8  •  Devo Studios", 11, Color.GRAY));
        setContentView(page);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
