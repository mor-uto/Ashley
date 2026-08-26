package lol.moruto.ashley.feature;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import lol.moruto.ashley.MainActivity;

public abstract class Feature {
    protected final MainActivity activity;

    protected Feature(MainActivity activity) {
        this.activity = activity;
    }

    public abstract String getTitle();
    public String getDescription() { return ""; }

    public String getSubtitle() { return ""; }

    public int dp(int v) {
        return (int) (v * activity.getResources().getDisplayMetrics().density);
    }

    public void copyToClipboard(String label, String input) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);

        clipboard.setPrimaryClip(ClipData.newPlainText(label, input));
        Toast.makeText(activity, "Copied!", Toast.LENGTH_SHORT).show();
    }

    @DrawableRes
    public abstract int getIcon();

    public abstract void execute();
}