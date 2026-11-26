package id.nexora.youtubelitex;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerActivity extends Activity {
    private WebView player;
    private FrameLayout root;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private int previousOrientation;

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("‹   " + getIntent().getStringExtra("video_title"));
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, dp(8), 0);
        page.addView(title, new LinearLayout.LayoutParams(-1, dp(44)));
        title.setOnClickListener(v -> finish());

        player = new WebView(this);
        WebSettings settings = player.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        player.setBackgroundColor(Color.BLACK);
        player.setWebChromeClient(new PlayerChromeClient());
        player.setWebViewClient(new WebViewClient());
        page.addView(player, new LinearLayout.LayoutParams(-1, 0, 1));

        root.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        String videoId = getIntent().getStringExtra("video_id");
        if (videoId == null) videoId = "";
        videoId = videoId.replaceAll("[^A-Za-z0-9_-]", "");

        // Use a real HTTPS document base and an explicit iframe with a referrer policy.
        // This is intended to provide the Referer/API client identity expected by YouTube.
        String html = "<!doctype html><html><head>"
                + "<meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'>"
                + "<meta name='referrer' content='strict-origin-when-cross-origin'>"
                + "<style>html,body{margin:0;padding:0;width:100%;height:100%;background:#000;}"
                + "iframe{display:block;border:0;width:100%;height:100%;}</style>"
                + "</head><body>"
                + "<iframe id='ytplayer' "
                + "src='https://www.youtube.com/embed/" + videoId
                + "?playsinline=1&rel=0&enablejsapi=1&origin=https%3A%2F%2Fwww.youtube.com' "
                + "title='YouTube video player' "
                + "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' "
                + "allowfullscreen "
                + "referrerpolicy='strict-origin-when-cross-origin'></iframe>"
                + "</body></html>";

        player.loadDataWithBaseURL("https://www.youtube.com/", html, "text/html", "UTF-8", null);
    }

    private class PlayerChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            customView = view;
            customViewCallback = callback;
            previousOrientation = getRequestedOrientation();
            player.setVisibility(View.GONE);
            root.addView(customView, new FrameLayout.LayoutParams(-1, -1));
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        @Override
        public void onHideCustomView() {
            hideCustomView();
        }
    }

    private void hideCustomView() {
        if (customView == null) return;
        root.removeView(customView);
        customView = null;
        player.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustomView();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stopLoading();
            player.loadUrl("about:blank");
            player.destroy();
            player = null;
        }
        super.onDestroy();
    }
}
