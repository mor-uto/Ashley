package lol.moruto.ashley.feature.impl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import lol.moruto.ashley.MainActivity;
import lol.moruto.ashley.R;
import lol.moruto.ashley.feature.Feature;
import lol.moruto.ashley.util.CryptoUtil;

public class CryptoFeature extends Feature {
    @Override public String getTitle() { return "Crypto"; }
    @Override public String getDescription() { return "Cryptography Utilities"; }
    @Override public String getSubtitle() { return "Coming Soon!"; }
    @Override public int getIcon() { return R.drawable.lock; }

    MainActivity activity;

    public CryptoFeature(MainActivity activity) {
        super(activity);
        this.activity = activity;
    }

    private final ActivityResultLauncher<Intent> hashFileLauncher =
            activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != Activity.RESULT_OK) {
                            return;
                        }

                        Intent data = result.getData();

                        if (data == null || data.getData() == null) {
                            return;
                        }

                        Uri uri = data.getData();

                        try {
                            String hash = CryptoUtil.hashFile(
                                    activity,
                                    uri,
                                    "SHA-256"
                            );

                            showResult(
                                    "File SHA-256",
                                    hash
                            );

                        } catch (Exception e) {
                            showResult(
                                    "File Hash Error",
                                    e.getMessage()
                            );
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> encryptFileLauncher =
            activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != Activity.RESULT_OK) return;

                        Intent data = result.getData();

                        if (data == null || data.getData() == null) return;

                        Uri uri = data.getData();

                        EditText password = new EditText(activity);
                        password.setHint("Password");
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        new MaterialAlertDialogBuilder(activity)
                                .setTitle("Encrypt File")
                                .setView(password)
                                .setPositiveButton("Encrypt", (dialog, which) -> {

                                    try {

                                        Uri output = CryptoUtil.encryptFile(
                                                activity,
                                                uri,
                                                password.getText().toString()
                                        );

                                        showResult(
                                                "File Encrypted",
                                                output.toString()
                                        );

                                    } catch (Exception e) {
                                        showResult("Encryption Error", e.getMessage());
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
            );

    private void addCategory(LinearLayout parent, String title, Tool... tools) {
        TextView header = new TextView(activity);

        header.setText("▶ " + title);
        header.setTextSize(18);
        header.setTypeface(Typeface.DEFAULT_BOLD);

        int p = dp(12);
        header.setPadding(p, p, p, p);

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setVisibility(View.GONE);

        for (Tool tool : tools) {
            MaterialButton button = new MaterialButton(activity, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);

            button.setText(tool.name);
            button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            lp.topMargin = dp(6);

            button.setLayoutParams(lp);

            button.setOnClickListener(v -> tool.action.run());

            container.addView(button);
        }

        header.setOnClickListener(v -> {
            boolean expanded = container.getVisibility() == View.VISIBLE;
            container.setVisibility(expanded ? View.GONE : View.VISIBLE);
            header.setText((expanded ? "▶ " : "▼ ") + title);
        });

        parent.addView(header);
        parent.addView(container);
    }

    private static class Tool {
        final String name;
        final Runnable action;

        Tool(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }
    }

    private void showResult(String title, String result) {
        ScrollView scroll = new ScrollView(activity);

        TextView text = new TextView(activity);

        int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);

        text.setPadding(pad, pad, pad, pad);
        text.setTypeface(Typeface.MONOSPACE);
        text.setTextIsSelectable(true);
        text.setText(result);

        scroll.addView(text);

        new MaterialAlertDialogBuilder(activity).setTitle(title).setView(scroll).setPositiveButton("Close", null).setNeutralButton("Copy", (d, w) -> copyToClipboard(title, result)).show();
    }

    @Override
    public void execute() {

        ScrollView scroll = new ScrollView(activity);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);

        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);

        scroll.addView(root);

        addCategory(root, "🔐 Hashing",
                new Tool("SHA-256", this::hashText),
                new Tool("Verify Hash", this::verifyHash),
                new Tool("Hash File", this::hashFile));

        addCategory(root, "📄 Encoding",
                new Tool("Base64 Encode", this::base64Encode),
                new Tool("Base64 Decode", this::base64Decode),
                new Tool("Hex Encode", this::hexEncode),
                new Tool("Hex Decode", this::hexDecode),
                new Tool("Rot13", this::rot13));

        addCategory(root, "🎲 Random",
                new Tool("Password Generator", this::generatePassword));

        addCategory(root, "🔒 Encryption",
                new Tool("Encrypt Text", this::encryptText),
                new Tool("Decrypt Text", this::decryptText),
                new Tool("Generate AES Key", this::generateAesKey));

        addCategory(root, "📁 File Tools",
                new Tool("Encrypt File", this::encryptFile),
                new Tool("Decrypt File", this::decryptFile),
                new Tool("Hash File", this::hashFile));

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Crypto Toolbox")
                .setView(scroll)
                .setPositiveButton("Close", null)
                .show();
    }

    private void encryptFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        encryptFileLauncher.launch(intent);
    }

    private void rot13() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);

        EditText input = new EditText(activity);
        input.setHint("Text");
        input.setGravity(Gravity.TOP);
        input.setMinLines(4);

        EditText rotInput = new EditText(activity);
        rotInput.setHint("ROT value (default: 13)");
        rotInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        Spinner modeSpinner = new Spinner(activity);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        activity,
                        android.R.layout.simple_spinner_dropdown_item,
                        new String[]{
                                "Encode",
                                "Decode"
                        }
                );

        modeSpinner.setAdapter(adapter);

        layout.addView(input);
        layout.addView(rotInput);
        layout.addView(modeSpinner);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("ROT Cipher")
                .setView(layout)
                .setPositiveButton("Apply", (dialog, which) -> {

                    String text = input.getText().toString();

                    int rot = 13;

                    String rotText =
                            rotInput.getText().toString().trim();

                    if (!rotText.isEmpty()) {
                        try {
                            rot = Integer.parseInt(rotText);
                        } catch (NumberFormatException e) {
                            showResult(
                                    "ROT Cipher",
                                    "Invalid ROT value"
                            );
                            return;
                        }
                    }

                    rot = ((rot % 26) + 26) % 26;

                    boolean decode = modeSpinner.getSelectedItemPosition() == 1;

                    if (decode) {
                        rot = (26 - rot) % 26;
                    }

                    String result = rot13(text, rot);

                    showResult(decode ? "ROT Decode" : "ROT Encode", result);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String rot13(String text, int rot) {
        StringBuilder result = new StringBuilder(text.length());

        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                c = (char) ('A' + (c - 'A' + rot) % 26);
            } else if (c >= 'a' && c <= 'z') {
                c = (char) ('a' + (c - 'a' + rot) % 26);
            }

            result.append(c);
        }

        return result.toString();
    }

    private void encryptText() {

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);

        EditText input = new EditText(activity);
        input.setHint("Text");
        input.setGravity(Gravity.TOP);
        input.setMinLines(5);

        EditText password = new EditText(activity);
        password.setHint("Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        layout.addView(input);
        layout.addView(password);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Encrypt Text")
                .setView(layout)
                .setPositiveButton("Encrypt", (dialog, which) -> {

                    String text = input.getText().toString();
                    String pass = password.getText().toString();

                    if (pass.isEmpty()) {
                        showResult("Encryption", "Password cannot be empty");
                        return;
                    }

                    try {
                        byte[] encrypted = CryptoUtil.encrypt(
                                text.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                                pass
                        );

                        showResult(
                                "Encrypted",
                                android.util.Base64.encodeToString(
                                        encrypted,
                                        android.util.Base64.NO_WRAP
                                )
                        );

                    } catch (Exception e) {
                        showResult(
                                "Encryption Error",
                                e.getMessage()
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void decryptText() {

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);

        EditText input = new EditText(activity);
        input.setHint("Base64 encrypted text");
        input.setGravity(Gravity.TOP);
        input.setMinLines(5);

        EditText password = new EditText(activity);
        password.setHint("Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        layout.addView(input);
        layout.addView(password);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Decrypt Text")
                .setView(layout)
                .setPositiveButton("Decrypt", (dialog, which) -> {

                    String encoded = input.getText().toString().trim();
                    String pass = password.getText().toString();

                    if (pass.isEmpty()) {
                        showResult("Decryption", "Password cannot be empty");
                        return;
                    }

                    try {
                        byte[] encrypted =
                                android.util.Base64.decode(
                                        encoded,
                                        android.util.Base64.DEFAULT
                                );

                        byte[] decrypted = CryptoUtil.decrypt(
                                encrypted,
                                pass
                        );

                        showResult(
                                "Decrypted",
                                new String(
                                        decrypted,
                                        java.nio.charset.StandardCharsets.UTF_8
                                )
                        );

                    } catch (Exception e) {
                        showResult(
                                "Decryption Error",
                                "Invalid password or corrupted data"
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void hexEncode() {

        EditText input = new EditText(activity);
        input.setHint("Text");
        input.setGravity(Gravity.TOP);
        input.setMinLines(5);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Hex Encode")
                .setView(input)
                .setPositiveButton("Encode", (dialog, which) -> {

                    String result = CryptoUtil.bytesToHex(
                            input.getText()
                                    .toString()
                                    .getBytes(
                                            java.nio.charset.StandardCharsets.UTF_8
                                    )
                    );

                    showResult("Hex Encoded", result);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void hexDecode() {

        EditText input = new EditText(activity);
        input.setHint("Hex");
        input.setGravity(Gravity.TOP);
        input.setMinLines(5);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Hex Decode")
                .setView(input)
                .setPositiveButton("Decode", (dialog, which) -> {

                    try {

                        byte[] bytes = CryptoUtil.hexToBytes(
                                input.getText().toString()
                        );

                        String result = new String(
                                bytes,
                                java.nio.charset.StandardCharsets.UTF_8
                        );

                        showResult("Hex Decoded", result);

                    } catch (Exception e) {
                        showResult(
                                "Hex Decode Error",
                                "Invalid hexadecimal input"
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void hashFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        hashFileLauncher.launch(intent);
    }


    private void generateAesKey() {}

    private void decryptFile() {}

    private void hashText() {
        EditText input = new EditText(activity);
        input.setHint("Enter text...");

        new MaterialAlertDialogBuilder(activity)
                .setTitle("SHA-256")
                .setView(input)
                .setPositiveButton("Hash", (dialog, which) -> {
                    String hash = CryptoUtil.sha256(input.getText().toString());

                    TextView text = new TextView(activity);

                    int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);

                    text.setPadding(pad, pad, pad, pad);
                    text.setTypeface(Typeface.MONOSPACE);
                    text.setTextIsSelectable(true);
                    text.setText(hash);

                    ScrollView scroll = new ScrollView(activity);
                    scroll.addView(text);

                    new MaterialAlertDialogBuilder(activity).setTitle("SHA-256").setView(scroll).setPositiveButton("Close", null).setNeutralButton("Copy", (d, w) -> copyToClipboard("SHA-256", hash)).show();
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    private void verifyHash() {
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);

        EditText text = new EditText(activity);
        text.setHint("Text");

        EditText hash = new EditText(activity);
        hash.setHint("Expected SHA-256");

        root.addView(text);
        root.addView(hash);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Verify SHA-256")
                .setView(root)
                .setPositiveButton("Verify", (dialog, which) -> {
                    boolean ok = CryptoUtil.verifySha256(text.getText().toString(), hash.getText().toString());

                    String actualHash = CryptoUtil.sha256(text.getText().toString());
                    new MaterialAlertDialogBuilder(activity).setTitle("Verification Result").setMessage((ok ? "✅ Hash matches!\n\n" : "❌ Hash does NOT match!\n\n") + "Computed SHA-256\n" + "────────────────────────────\n" + actualHash).setPositiveButton("Close", null).setNeutralButton("Copy", (d, w) -> copyToClipboard("SHA-256", actualHash)).show();
                }).setNegativeButton("Cancel", null).show();

    }

    private void generatePassword() {
        int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        EditText length = new EditText(activity);
        length.setHint("Length");
        length.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        length.setText("24");

        CheckBox upper = new CheckBox(activity);
        upper.setText("Uppercase");
        upper.setChecked(true);

        CheckBox lower = new CheckBox(activity);
        lower.setText("Lowercase");
        lower.setChecked(true);

        CheckBox numbers = new CheckBox(activity);
        numbers.setText("Numbers");
        numbers.setChecked(true);

        CheckBox symbols = new CheckBox(activity);
        symbols.setText("Symbols");
        symbols.setChecked(true);

        root.addView(length);
        root.addView(upper);
        root.addView(lower);
        root.addView(numbers);
        root.addView(symbols);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Password Generator")
                .setView(root)
                .setPositiveButton("Generate", (dialog, which) -> {
                    try {
                        showResult("Generated Password", CryptoUtil.generatePassword(Integer.parseInt(length.getText().toString()), upper.isChecked(), lower.isChecked(), numbers.isChecked(), symbols.isChecked()));
                    } catch (Exception e) {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    private void base64Encode() {

        EditText input = new EditText(activity);

        input.setHint("Text to encode...");
        input.setMinLines(5);

        new MaterialAlertDialogBuilder(activity).setTitle("Base64 Encode").setView(input).setPositiveButton("Encode", (dialog, which) -> showResult("Base64 Encoded", CryptoUtil.base64Encode(input.getText().toString()))).setNegativeButton("Cancel", null).show();
    }

    private void base64Decode() {
        EditText input = new EditText(activity);

        input.setHint("Base64...");
        input.setMinLines(5);

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Base64 Decode")
                .setView(input)
                .setPositiveButton("Decode", (dialog, which) -> {

                    try {
                        showResult("Decoded Text", CryptoUtil.base64Decode(input.getText().toString()));
                    } catch (RuntimeException e) {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
