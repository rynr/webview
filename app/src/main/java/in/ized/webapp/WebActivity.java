package in.ized.webapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private WebViewClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        setupBrowser();
        disableScreensaver();
        enableFullscreen();
        getPreferences().registerOnSharedPreferenceChangeListener(this);
        registerForContextMenu(findViewById(R.id.webview));
    }

    @Override
    protected void onResume() {
        super.onResume();
        disableScreensaver();
        enableFullscreen();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_web, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(getClass().getName(), "Configuration changed: " + key);
        if (getString(R.string.pref_web_url_key).equals(key)) {
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadUrl(getUrl());
        } else if (getString(R.string.pref_web_js_key).equals(key)) {
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.getSettings().setJavaScriptEnabled(isEnableJavaScript());
            myWebView.reload();
        }
    }

    private void disableScreensaver() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void enableFullscreen() {
        View decorView = getWindow().getDecorView();


        // Hide the status bar and the navigation bar.
        int uiOptions = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY : View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
    }


    private void setupBrowser() {
        Log.d(getClass().getName(), "Setup browser");
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(isEnableJavaScript());
        myWebView.setWebViewClient(getWebClient());

        myWebView.loadUrl(getUrl() + "?id=" + Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID));
    }

    private synchronized WebViewClient getWebClient() {
        if (client == null) {
            client = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (Uri.parse(url).getHost().equals(getUrlHost())) {
                        return false;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                }
            };
        }
        return client;
    }

    private boolean isEnableJavaScript() {
        return getBooleanPreference(getString(R.string.pref_web_js_key), false);
    }

    private String getUrlHost() {
        return Uri.parse(getUrl()).getHost();
    }

    private String getUrl() {
        return getStringPreference(getString(R.string.pref_web_url_key), getString(R.string.pref_web_url_def));
    }

    private String getStringPreference(String name, String def) {
        return getPreferences().getString(name, def);
    }

    private boolean getBooleanPreference(String name, boolean def) {
        return getPreferences().getBoolean(name, def);
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}