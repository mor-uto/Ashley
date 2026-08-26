package lol.moruto.ashley.feature.impl;

import android.content.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.*;

import java.util.concurrent.*;

import lol.moruto.ashley.*;
import lol.moruto.ashley.feature.Feature;
import lol.moruto.ashley.util.HttpUtil;

public class SendHttpRequestFeature extends Feature {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SendHttpRequestFeature(MainActivity activity) {
        super(activity);
    }

    @Override
    public void execute() {

        int p = dp(20);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, p, p, p);

        TextInputEditText endpoint = field("https://", "Endpoint");
        TextInputEditText body = field("", "Body");

        MaterialAutoCompleteTextView method = new MaterialAutoCompleteTextView(activity);
        method.setAdapter(new ArrayAdapter<>(
                activity,
                android.R.layout.simple_list_item_1,
                new String[]{"GET","POST","PUT","PATCH","DELETE"}
        ));
        method.setText("GET", false);

        MaterialSwitch pretty = new MaterialSwitch(activity);
        pretty.setText("Pretty JSON");
        pretty.setChecked(true);

        root.addView(wrap("Endpoint", endpoint));
        root.addView(wrap("Method", method));
        root.addView(wrap("Body", body));
        root.addView(pretty);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("HTTP Request")
                .setView(root)
                .setPositiveButton("Send", (d, w) -> send(
                        endpoint.getText().toString(),
                        method.getText().toString(),
                        body.getText().toString(),
                        pretty.isChecked()
                ))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void send(String url, String method, String body, boolean pretty) {

        executor.execute(() -> {

            try {
                HttpUtil.HttpResponse r =
                        HttpUtil.send(url, method, body);

                activity.runOnUiThread(() ->
                        show(r, pretty));

            } catch (Exception e) {

                activity.runOnUiThread(() ->
                        show(new HttpUtil.HttpResponse(
                                -1,
                                "FAILED",
                                e.getMessage()
                        ), pretty));
            }
        });
    }

    private void show(HttpUtil.HttpResponse r, boolean pretty) {
        ScrollView s = new ScrollView(activity);

        TextView t = new TextView(activity);
        int p = dp(18);

        t.setPadding(p, p, p, p);
        t.setTypeface(Typeface.MONOSPACE);
        t.setTextIsSelectable(true);

        String body = r.getBody();
        if (pretty) body = HttpUtil.prettyJson(body);

        t.setText("HTTP " + r.getCode() + "\n\n" + r.getMessage() + "\n\n" + (body == null ? "<empty>" : body));

        s.addView(t);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Response")
                .setView(s)
                .setPositiveButton("Close", null)
                .setNeutralButton("Copy", (d, w) -> copyToClipboard("http", t.getText().toString()))
                .show();
    }

    private TextInputEditText field(String def, String hint) {
        TextInputEditText e = new TextInputEditText(activity);
        e.setHint(hint);
        e.setText(def);
        return e;
    }

    private TextInputLayout wrap(String hint, View v) {
        TextInputLayout l = new TextInputLayout(activity);
        l.setHint(hint);
        l.setLayoutParams(lp());
        l.addView(v);
        return l;
    }

    private LinearLayout.LayoutParams lp() {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        p.bottomMargin = dp(14);
        return p;
    }

    @Override public String getTitle() { return "HTTP Request"; }
    @Override public String getDescription() { return "Send requests"; }
    @Override public String getSubtitle() { return "OkHttp powered"; }
    @Override public int getIcon() { return R.drawable.http; }
}