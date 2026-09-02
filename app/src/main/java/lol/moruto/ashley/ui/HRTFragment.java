package lol.moruto.ashley.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import lol.moruto.ashley.R;

public class HRTFragment extends Fragment {
    private String HRT_URL;

    private static final String CACHE_FILE = "hrt.json";
    private static final String HASH_FILE = "hrt.sha256";
    private static final String LAST_UPDATED_FILE = "hrt.last_updated";

    public HRTFragment() {
        super(R.layout.fragment_hrt);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle state) {
        super.onViewCreated(v, state);
        HRT_URL = getString(R.string.hrt_json_url);

        try {
            String jsonText = readCachedJson();

            if (jsonText != null) {
                loadHRT(v, new JSONObject(jsonText));
                showLastUpdated(v);
            } else {
                Toast.makeText(requireContext(), "No cached HRT data", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to load cached HRT data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        new Thread(() -> {

            if (!hasInternetConnection()) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(
                                requireContext(),
                                "No internet connection",
                                Toast.LENGTH_LONG
                        ).show()
                );
                return;
            }

            try {
                updateFromServer();

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    try {
                        String jsonText = readCachedJson();

                        if (jsonText != null) {
                            loadHRT(v, new JSONObject(jsonText));
                            showLastUpdated(v);
                        }

                    } catch (Exception e) {
                        if (isAdded()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load updated HRT data",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
            } catch (Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    requireContext(),
                                    "HRT update failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }
            }
        }).start();
    }

    private boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) requireContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            return false;
        }

        Network network = cm.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                cm.getNetworkCapabilities(network);

        if (capabilities == null) {
            return false;
        }

        return capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
        );
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private void loadHRT(View v, JSONObject json) throws Exception {
        LocalDate start = parseDate(json.getString("start_date"));

        updateInjectionSchedule(v, json);

        ((TextView) v.findViewById(R.id.days_number)).setText(String.valueOf(ChronoUnit.DAYS.between(start, LocalDate.now())));

        section(v, R.id.blood_history_button, R.id.blood_history_container, "Blood Test History");
        section(v, R.id.regimen_history_button, R.id.regimen_history_container, "Regimen Changes History");
        section(v, R.id.trans_resources_button, R.id.trans_resources_container, "Trans Resources");
        section(v, R.id.trans_timeline_button, R.id.trans_timeline_container, "Trans Timeline");

        loadBloodTests(v, json);
        loadRegimen(v, json);
        loadResources(v, json);
    }

    private void updateInjectionSchedule(View v, JSONObject json) {
        TextView schedule = v.findViewById(R.id.injection_schedule);

        int frequency = json.optInt("injection_frequency_in_days", 0);

        String firstDateString = json.optString("injection_frequency_firstdate", ""         );

        if (frequency <= 0 || firstDateString.isEmpty()) {
            schedule.setText("");
            return;
        }

        LocalDate firstDate = parseDate(firstDateString);
        LocalDate today = LocalDate.now();

        long days = ChronoUnit.DAYS.between(firstDate, today);

        long remainder = Math.floorMod(days, frequency);

        if (remainder == 0) {
            schedule.setText("Injection today");
        } else {
            long remaining = frequency - remainder;
            schedule.setText(remaining + "d remaining");
        }
    }

    private void loadRegimen(View v, JSONObject json) throws Exception {
        JSONObject regimen = json.getJSONObject("regimen_history");
        JSONArray entries = regimen.getJSONArray("entries");

        StringBuilder body = new StringBuilder();

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);

            String date = entry.optString("date", "");
            String text = entry.optString("text", "");

            if (!date.isEmpty()) {
                body.append("<b>")
                        .append(date)
                        .append("</b>");
            }

            if (!text.isEmpty()) {
                if (!date.isEmpty()) {
                    body.append("<br><br>");
                }

                body.append(text);
            }

            if (i < entries.length() - 1) {
                body.append("<br><br><br>");
            }
        }

        card(
                v,
                R.id.regimen_history_card,
                regimen.getString("title"),
                body.toString(),
                ""
        );
    }


    private void loadResources(View v, JSONObject json) throws Exception {
        JSONObject resources = json.getJSONObject("trans_resources");
        JSONArray entries = resources.getJSONArray("entries");

        StringBuilder body = new StringBuilder();

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);

            String name = entry.optString("name", "");
            String description = entry.optString("description", "");
            String url = entry.optString("url", "");

            if (!name.isEmpty()) {
                body.append("<b>")
                        .append(name)
                        .append("</b>");
            }

            if (!description.isEmpty()) {
                body.append("<br><br>")
                        .append(description);
            }

            if (!url.isEmpty()) {
                body.append("<br><br>")
                        .append("<a href=\"")
                        .append(url)
                        .append("\">Open Resource</a>");
            }

            if (i < entries.length() - 1) {
                body.append("<br><br><br>");
            }
        }

        card(
                v,
                R.id.trans_resources_card,
                resources.getString("title"),
                body.toString(),
                ""
        );
    }

    private void loadBloodTests(View v, JSONObject json) throws Exception {
        LinearLayout container = v.findViewById(R.id.blood_history_container);

        JSONArray tests = json.getJSONArray("blood_tests");

        container.removeAllViews();

        for (int i = 0; i < tests.length(); i++) {
            JSONObject test = tests.getJSONObject(i);

            View card = getLayoutInflater().inflate(R.layout.item_feature, container, false);

            TextView title = card.findViewById(R.id.card_title);
            TextView description = card.findViewById(R.id.card_description);
            TextView subtitle = card.findViewById(R.id.card_subtitle);

            title.setText(test.getString("title"));
            subtitle.setText(test.getString("date"));

            JSONObject results = test.getJSONObject("results");
            StringBuilder body = new StringBuilder();
            JSONArray names = results.names();

            if (names != null) {
                for (int j = 0; j < names.length(); j++) {
                    String name = names.getString(j);

                    body.append(name).append(": ").append(results.getString(name));

                    if (j < names.length() - 1) {
                        body.append("\n\n");
                    }
                }
            }

            description.setText(body.toString());
            container.addView(card);
        }
    }

    private void section(View v,int buttonId, int containerId, String title) {
        MaterialButton button = v.findViewById(buttonId);
        View container = v.findViewById(containerId);

        button.setOnClickListener(x -> {

            boolean show = container.getVisibility() != View.VISIBLE;

            container.setVisibility(show ? View.VISIBLE : View.GONE);
            button.setText(show ? "Hide " + title : title);
        });
    }

    private void card(View v, int id, String title, String body, String date) {
        View card = v.findViewById(id);

        ((TextView) card.findViewById(R.id.card_title)).setText(title);

        TextView description = card.findViewById(R.id.card_description);

        description.setText(
                Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY)
        );

        description.setLineSpacing(8f, 1.0f);
        description.setMovementMethod(LinkMovementMethod.getInstance());

        ((TextView) card.findViewById(R.id.card_subtitle)).setText(date);
    }

    private String readCachedJson() throws Exception {
        File file = new File(requireContext().getFilesDir(), CACHE_FILE);

        if (!file.exists()) return null;

        FileInputStream in = new FileInputStream(file);

        byte[] data = readAll(in);

        in.close();

        return new String(data, StandardCharsets.UTF_8);
    }

    private String readCachedHash() throws Exception {
        File file = new File(requireContext().getFilesDir(), HASH_FILE);

        if (!file.exists()) return null;

        FileInputStream in = new FileInputStream(file);

        byte[] data = readAll(in);

        in.close();

        return new String(data, StandardCharsets.UTF_8).trim();
    }

    private void updateFromServer() throws Exception {
        byte[] downloaded = downloadFile();

        String downloadedJson = new String(downloaded, StandardCharsets.UTF_8);

        JSONObject json = new JSONObject(downloadedJson);

        parseDate(json.getString("start_date"));

        int frequency = json.optInt("injection_frequency_in_days", 0);
        String firstDate = json.optString("injection_frequency_firstdate", "");

        if (frequency > 0 && !firstDate.isEmpty()) {
            parseDate(firstDate);
        }

        String newHash = sha256(downloaded);
        String oldHash = readCachedHash();

        if (newHash.equalsIgnoreCase(oldHash)) return;

        File jsonFile = new File(requireContext().getFilesDir(), CACHE_FILE);

        try (FileOutputStream out = new FileOutputStream(jsonFile)) {
            out.write(downloaded);
            out.flush();
        }

        File hashFile = new File(requireContext().getFilesDir(), HASH_FILE);

        try (FileOutputStream out = new FileOutputStream(hashFile)) {
            out.write(newHash.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        File updatedFile = new File(requireContext().getFilesDir(), LAST_UPDATED_FILE);

        try (FileOutputStream out = new FileOutputStream(updatedFile)) {
            out.write(
                    String.valueOf(System.currentTimeMillis())
                            .getBytes(StandardCharsets.UTF_8)
            );
            out.flush();
        }
    }

    private byte[] downloadFile() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(HRT_URL).openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setUseCaches(false);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 " + "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        InputStream in;

        if (responseCode >= 200 && responseCode < 300) {
            in = connection.getInputStream();
        } else {
            in = connection.getErrorStream();

            String error = "";

            if (in != null) {
                error = new String(
                        readAll(in),
                        StandardCharsets.UTF_8
                );
            }

            connection.disconnect();

            throw new Exception("HTTP " + responseCode + ": " + error);
        }

        byte[] data = readAll(in);

        in.close();
        connection.disconnect();

        return data;
    }

    public static String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(data);

        StringBuilder result = new StringBuilder();

        for (byte b : hash) {
            result.append(String.format("%02x", b & 0xff));
        }

        return result.toString();
    }

    private byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] temp = new byte[8192];

        int length;

        while ((length = in.read(temp)) != -1) {
            buffer.write(temp, 0, length);
        }

        return buffer.toByteArray();
    }

    private void showLastUpdated(View v) {
        TextView lastUpdated = v.findViewById(R.id.last_updated);

        if (lastUpdated == null) return;

        try {
            File file = new File(requireContext().getFilesDir(), LAST_UPDATED_FILE);

            if (!file.exists()) {
                lastUpdated.setText("Last updated: Never");
                return;
            }

            FileInputStream in = new FileInputStream(file);

            byte[] data = readAll(in);

            in.close();

            long timestamp = Long.parseLong(new String(data, StandardCharsets.UTF_8).trim());

            java.text.DateFormat format = java.text.DateFormat.getDateTimeInstance();

            lastUpdated.setText("Last updated: " + format.format(new java.util.Date(timestamp)));
        } catch (Exception e) {
            lastUpdated.setText("Last updated: Unknown");
        }
    }
}
