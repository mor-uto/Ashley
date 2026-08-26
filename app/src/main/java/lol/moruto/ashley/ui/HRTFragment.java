package lol.moruto.ashley.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

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

        ((TextView) v.findViewById(R.id.days_number)).setText(
                String.valueOf(ChronoUnit.DAYS.between(
                        LocalDate.of(2026, 1, 17), LocalDate.now()
                ))
        );

        section(v, R.id.blood_history_button, R.id.blood_history_container, "Blood Test History");
        section(v, R.id.regimen_history_button, R.id.regimen_history_container, "Regimen Changes History");
        section(v, R.id.trans_resources_button, R.id.trans_resources_container, "Trans Resources");
        section(v, R.id.trans_timeline_button, R.id.trans_timeline_container, "Trans Timeline");

        card(v, R.id.regimen_history_card, "Regimen Changes",
                "14/7/2026: 80mg Estradiol Undecylate IM\n\n" +
                        "7/7/2026: 2mg Estradiol benzoate + 20mg Progesterone IM\n\n" +
                        "24/6/2026: Estradiol dose increased → 4mg → 6mg\n\n" +
                        "7/5/2026: Zoladex 10.8mg (Goserelin) Injected SubQ\n\n" +
                        "6/7/2026: Spironolactone stopped\n\n" +
                        "4/25/2026: Spironolactone increased → 100mg → 200mg\n\n" +
                        "Starting dose: 4mg Estradiol Valerate + 100mg Spironolactone",
                "");

        card(v, R.id.trans_resources_card, "Trans Resources",
                "<a href=\"https://valerie.vg\">valerie.vg</a> - All-In-One Trans Resources Hub\n\n" +
                        "<h1>Estrogen Graph Visualizers</h1>\n\n" +
                        "<a href=\"https://GAHTPlotter.com\">GAHTPlotter.com</a>\n\n" +
                        "<a href=\"https://sim.transfemscience.org\">sim.transfemscience.org</a>",
                "");

        card(v, R.id.blood_card_1, "Blood Report #1",
                "Estradiol: 134 pg/mL\n\n" +
                        "Total Testosterone: 1359 ng/dL\n\n" +
                        "FSH: 19.8 mIU/mL\n\n" +
                        "LH: 33.2 mIU/mL\n\n" +
                        "Prolactin: 9.1 ng/mL",
                "17/4/2026");

        card(v, R.id.blood_card_2, "Blood Report #2",
                "Estradiol: 87.9 pg/mL\n\n" +
                        "Total Testosterone: 13 ng/ml\n\n" +
                        "FSH: 5.4 mIU/mL\n\n" +
                        "LH: 4.2 mIU/mL\n\n" +
                        "Prolactin: 6.0 ng/mL",
                "10/06/2026");

        card(v, R.id.blood_card_3, "Blood Report #3",
                "Estradiol: 72 pg/mL\n\n" +
                        "Total Testosterone: 6.9 ng/ml\n\n" +
                        "FSH: 5.61 mIU/mL\n\n" +
                        "LH: 4.7 mIU/mL",
                "6/7/2026");
    }

    private void section(View v, int buttonId, int containerId, String title) {
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
        description.setText(Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY));
        description.setMovementMethod(LinkMovementMethod.getInstance());

        ((TextView) card.findViewById(R.id.card_subtitle)).setText(date);
    }
}