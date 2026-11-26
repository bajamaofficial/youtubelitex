package id.nexora.youtubelitex;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class MainActivity extends Activity {
    private LinearLayout videoList;
    private EditText searchInput;
    private TextView statusText;

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(16, 16, 16));
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 16, 16));

        TextView header = new TextView(this);
        header.setText("▶  YouTube Lite X");
        header.setTextColor(Color.WHITE);
        header.setTextSize(21);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), 0, dp(10), 0);
        header.setBackgroundColor(Color.rgb(25, 25, 25));
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(56)));

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setPadding(dp(9), dp(8), dp(9), dp(5));

        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setHint("Cari video YouTube");
        searchInput.setTextColor(Color.WHITE);
        searchInput.setHintTextColor(Color.LTGRAY);
        searchInput.setBackgroundColor(Color.rgb(42, 42, 42));
        searchInput.setPadding(dp(10), 0, dp(10), 0);
        searchInput.setImeOptions(3);
        searchRow.addView(searchInput, new LinearLayout.LayoutParams(0, dp(46), 1));

        Button searchButton = new Button(this);
        searchButton.setText("CARI");
        searchRow.addView(searchButton, new LinearLayout.LayoutParams(dp(78), dp(46)));
        root.addView(searchRow);

        LinearLayout tabs = new LinearLayout(this);
        addTab(tabs, "Beranda", "Indonesia");
        addTab(tabs, "Populer", "popular Indonesia");
        Button historyButton = new Button(this);
        historyButton.setText("Riwayat");
        historyButton.setTextSize(11);
        tabs.addView(historyButton, new LinearLayout.LayoutParams(0, dp(42), 1));
        historyButton.setOnClickListener(v -> showHistory());
        root.addView(tabs);

        statusText = new TextView(this);
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(12);
        statusText.setPadding(dp(12), dp(5), 0, dp(5));
        statusText.setText("Beranda video");
        root.addView(statusText, new LinearLayout.LayoutParams(-1, dp(28)));

        ScrollView scrollView = new ScrollView(this);
        videoList = new LinearLayout(this);
        videoList.setOrientation(LinearLayout.VERTICAL);
        videoList.setPadding(dp(9), dp(5), dp(9), dp(15));
        scrollView.addView(videoList);
        root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        searchButton.setOnClickListener(v -> performSearch());
        searchInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == 3) {
                performSearch();
                return true;
            }
            return false;
        });

        if (hasApiKey()) {
            searchVideos("Indonesia");
        } else {
            showMessage("Antarmuka Native siap. Tambahkan YOUTUBE_API_KEY di GitHub Actions Secrets untuk mengaktifkan pencarian.");
        }
    }

    private void addTab(LinearLayout tabs, String label, String term) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(11);
        tabs.addView(button, new LinearLayout.LayoutParams(0, dp(42), 1));
        button.setOnClickListener(v -> searchVideos(term));
    }

    private boolean hasApiKey() {
        return BuildConfig.YOUTUBE_API_KEY != null
                && !BuildConfig.YOUTUBE_API_KEY.trim().isEmpty()
                && !BuildConfig.YOUTUBE_API_KEY.equals("PASTE_YOUTUBE_DATA_API_KEY_HERE");
    }

    private void performSearch() {
        String term = searchInput.getText().toString().trim();
        if (term.isEmpty()) {
            Toast.makeText(this, "Masukkan kata pencarian", Toast.LENGTH_SHORT).show();
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
        searchVideos(term);
    }

    private void searchVideos(String term) {
        if (!hasApiKey()) {
            showMessage("API key belum dipasang. Ikuti README, lalu build ulang.");
            return;
        }

        statusText.setText("Mencari video...");
        videoList.removeAllViews();

        new AsyncTask<String, Void, SearchResult>() {
            @Override
            protected SearchResult doInBackground(String... terms) {
                SearchResult result = new SearchResult();
                HttpURLConnection connection = null;
                try {
                    String urlText = "https://www.googleapis.com/youtube/v3/search"
                            + "?part=snippet&type=video&maxResults=25&q="
                            + URLEncoder.encode(terms[0], "UTF-8")
                            + "&key=" + URLEncoder.encode(BuildConfig.YOUTUBE_API_KEY, "UTF-8");

                    connection = (HttpURLConnection) new URL(urlText).openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);

                    int responseCode = connection.getResponseCode();
                    InputStream input = responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream() : connection.getErrorStream();

                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        output.write(buffer, 0, count);
                    }
                    input.close();

                    JSONObject root = new JSONObject(output.toString("UTF-8"));
                    if (responseCode < 200 || responseCode >= 300) {
                        result.error = root.optJSONObject("error") != null
                                ? root.getJSONObject("error").optString("message", "HTTP " + responseCode)
                                : "HTTP " + responseCode;
                        return result;
                    }

                    JSONArray items = root.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        JSONObject snippet = item.getJSONObject("snippet");
                        JSONObject idObject = item.getJSONObject("id");
                        JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                        JSONObject thumb = thumbnails.has("high")
                                ? thumbnails.getJSONObject("high")
                                : thumbnails.getJSONObject("default");

                        result.items.add(new VideoItem(
                                idObject.getString("videoId"),
                                snippet.optString("title"),
                                snippet.optString("channelTitle"),
                                thumb.optString("url")
                        ));
                    }
                } catch (Exception e) {
                    result.error = e.getMessage() == null ? "Kesalahan jaringan" : e.getMessage();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(SearchResult result) {
                if (result.error != null) {
                    statusText.setText("Pencarian gagal: " + result.error);
                    return;
                }
                statusText.setText(result.items.size() + " video ditemukan");
                for (VideoItem item : result.items) {
                    addVideoCard(item);
                }
                if (result.items.isEmpty()) {
                    showMessage("Tidak ada hasil.");
                }
            }
        }.execute(term);
    }

    private void addVideoCard(VideoItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(7), dp(7), dp(7), dp(9));
        card.setBackgroundColor(Color.rgb(29, 29, 29));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(9);

        ImageView thumbnail = new ImageView(this);
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.addView(thumbnail, new LinearLayout.LayoutParams(-1, dp(190)));

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(dp(4), dp(7), dp(4), dp(2));
        card.addView(title);

        TextView channel = new TextView(this);
        channel.setText(item.channel);
        channel.setTextColor(Color.LTGRAY);
        channel.setTextSize(12);
        card.addView(channel);

        card.setOnClickListener(v -> {
            getSharedPreferences("history", MODE_PRIVATE).edit()
                    .putString(item.id, item.title + "|" + item.channel + "|" + item.thumbnail)
                    .apply();
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            intent.putExtra("video_id", item.id);
            intent.putExtra("video_title", item.title);
            startActivity(intent);
        });

        videoList.addView(card, cardParams);

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... ignored) {
                try {
                    return BitmapFactory.decodeStream(new URL(item.thumbnail).openStream());
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    thumbnail.setImageBitmap(bitmap);
                }
            }
        }.execute();
    }

    private void showHistory() {
        videoList.removeAllViews();
        statusText.setText("Riwayat tontonan lokal");
        Map<String, ?> history = getSharedPreferences("history", MODE_PRIVATE).getAll();
        if (history.isEmpty()) {
            showMessage("Belum ada riwayat tontonan.");
            return;
        }
        for (Map.Entry<String, ?> entry : history.entrySet()) {
            String[] data = String.valueOf(entry.getValue()).split("\\|", 3);
            if (data.length == 3) {
                addVideoCard(new VideoItem(entry.getKey(), data[0], data[1], data[2]));
            }
        }
    }

    private void showMessage(String message) {
        videoList.removeAllViews();
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(Color.WHITE);
        text.setTextSize(15);
        text.setPadding(dp(15), dp(20), dp(15), dp(20));
        videoList.addView(text);
    }

    private static final class SearchResult {
        final java.util.ArrayList<VideoItem> items = new java.util.ArrayList<>();
        String error;
    }
}
