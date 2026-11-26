package id.nexora.youtubelitex;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.Gravity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerActivity extends Activity {
    private WebView player;

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        TextView title = new TextView(this);
        title.setText("‹   " + getIntent().getStringExtra("video_title"));
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, dp(8), 0);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));
        title.setOnClickListener(v -> finish());

        player = new WebView(this);
        WebSettings settings = player.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        player.setWebChromeClient(new WebChromeClient());
        player.setWebViewClient(new WebViewClient());
        root.addView(player, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        String videoId = getIntent().getStringExtra("video_id");
        if (videoId == null) {
            videoId = "";
        }
        videoId = videoId.replaceAll("[^A-Za-z0-9_-]", "");

        String html = "<!doctype html><html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>html,body,#player{margin:0;width:100%;height:100%;background:#000;}</style>"
                + "</head><body><div id='player'></div>"
                + "<script src='https://www.youtube.com/iframe_api'></script>"
                + "<script>function onYouTubeIframeAPIReady(){new YT.Player('player',{"
                + "width:'100%',height:'100%',videoId:'" + videoId + "',"
                + "playerVars:{playsinline:0,rel:0},"
                + "events:{onReady:function(event){event.target.playVideo();}}"
                + "});}</script></body></html>";

        player.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stopLoading();
            player.destroy();
            player = null;
        }
        super.onDestroy();
    }
}
