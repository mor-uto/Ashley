package lol.moruto.ashley.ui;

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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lol.moruto.ashley.R;

public class HRTFragment extends Fragment {

    public HRTFragment() {
        super(R.layout.fragment_hrt);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle state) {
        super.onViewCreated(v, state);

        try {
            JSONObject json = new JSONObject(readJson());

            LocalDate start = LocalDate.parse(json.getString("start_date"));
            updateInjectionSchedule(v, json);

            ((TextView) v.findViewById(R.id.days_number)).setText(
                    String.valueOf(
                            ChronoUnit.DAYS.between(start, LocalDate.now())
                    )
            );

            section(
                    v,
                    R.id.blood_history_button,
                    R.id.blood_history_container,
                    "Blood Test History"
            );

            section(
                    v,
                    R.id.regimen_history_button,
                    R.id.regimen_history_container,
                    "Regimen Changes History"
            );

            section(
                    v,
                    R.id.trans_resources_button,
                    R.id.trans_resources_container,
                    "Trans Resources"
            );

            section(
                    v,
                    R.id.trans_timeline_button,
                    R.id.trans_timeline_container,
                    "Trans Timeline"
            );

            loadBloodTests(v, json);
            loadRegimen(v, json);
            loadResources(v, json);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    requireContext(),
                    "Failed to load HRT data",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void updateInjectionSchedule(View v, JSONObject json) {
        TextView schedule = v.findViewById(R.id.injection_schedule);

        int frequency = json.optInt("injection_frequency_in_days", 0);
        String firstDateString = json.optString("injection_frequency_firstdate", "");

        if (frequency <= 0 || firstDateString.isEmpty()) {
            schedule.setText("");
            return;
        }

        LocalDate firstDate = LocalDate.parse(firstDateString);
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

        StringBuilder body = new StringBuilder();

        JSONArray entries = regimen.getJSONArray("entries");

        for (int i = 0; i < entries.length(); i++) {

            JSONObject entry = entries.getJSONObject(i);

            String date = entry.getString("date");
            String text = entry.getString("text");

            if (!date.isEmpty())
                body.append(date).append(": ");

            body.append(text);

            if (i < entries.length() - 1)
                body.append("\n\n");
        }

        card(
                v,
                R.id.regimen_history_card,
                regimen.getString("title"),
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

            View card = getLayoutInflater()
                    .inflate(R.layout.item_feature, container, false);

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

                    body.append(name)
                            .append(": ")
                            .append(results.getString(name));

                    if (j < names.length() - 1)
                        body.append("\n\n");
                }
            }

            description.setText(body.toString());

            container.addView(card);
        }
    }

    private void loadResources(View v, JSONObject json) throws Exception {

        JSONObject resources = json.getJSONObject("trans_resources");

        StringBuilder body = new StringBuilder();

        JSONArray entries = resources.getJSONArray("entries");

        for (int i = 0; i < entries.length(); i++) {

            JSONObject entry = entries.getJSONObject(i);

            body.append("<a href=\"")
                    .append(entry.getString("url"))
                    .append("\">")
                    .append(entry.getString("name"))
                    .append("</a>");

            String description = entry.optString("description");

            if (!description.isEmpty())
                body.append(" - ").append(description);

            if (i < entries.length() - 1)
                body.append("\n\n");
        }

        card(
                v,
                R.id.trans_resources_card,
                resources.getString("title"),
                body.toString(),
                ""
        );
    }

    private void section(View v, int buttonId, int containerId, String title) {
        MaterialButton button = v.findViewById(buttonId);
        View container = v.findViewById(containerId);

        button.setOnClickListener(x -> {

            boolean show = container.getVisibility() != View.VISIBLE;

            container.setVisibility(
                    show ? View.VISIBLE : View.GONE
            );

            button.setText(
                    show ? "Hide " + title : title
            );
        });
    }

    private String readJson() throws Exception {
        InputStream in = getResources().openRawResource(R.raw.hrt);

        byte[] data = new byte[in.available()];
        in.read(data);
        in.close();

        return new String(data, StandardCharsets.UTF_8);
    }

    private void card(View v, int id, String title, String body, String date) {
        View card = v.findViewById(id);

        ((TextView) card.findViewById(R.id.card_title)).setText(title);
        TextView description = card.findViewById(R.id.card_description);

        description.setText(Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY));

        description.setLineSpacing(12f, 1.0f);

        description.setLineSpacing(8f, 1.0f);
        description.setMovementMethod(LinkMovementMethod.getInstance());

        ((TextView) card.findViewById(R.id.card_subtitle)).setText(date);
    }
}