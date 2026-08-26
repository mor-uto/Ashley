package lol.moruto.ashley.feature.impl;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import lol.moruto.ashley.MainActivity;
import lol.moruto.ashley.R;
import lol.moruto.ashley.feature.Feature;

public class FileShredderFeature extends Feature {

    private final ActivityResultLauncher<String[]> pickerLauncher;

    public FileShredderFeature(MainActivity activity) {
        super(activity);

        pickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::shredFile
        );
    }

    @Override
    public void execute() {
        pickerLauncher.launch(new String[]{"*/*"});
    }

    private void shredFile(Uri uri) {
        if (uri == null) return;

        DocumentFile file = DocumentFile.fromSingleUri(activity, uri);
        String name = file.getName() != null ? file.getName() : "this file";

        new AlertDialog.Builder(activity)
                .setTitle("Shred file?")
                .setMessage(
                        "Are you sure you want to securely delete:\n\n" +
                                name +
                                "\n\nThis cannot be undone."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Shred", (dialog, which) ->
                        shredFileConfirmed(uri)
                )
                .show();
    }

    private void shredFileConfirmed(Uri uri) {
        try (ParcelFileDescriptor pfd =
                     activity.getContentResolver().openFileDescriptor(uri, "rw")) {

            if (pfd == null) {
                Toast.makeText(
                        activity,
                        "Couldn't open file.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            long size = pfd.getStatSize();
            if (size <= 0) size = 1024;

            byte[] buffer = new byte[8192];
            Random random = new Random();

            for (int pass = 0; pass < 7; pass++) {
                try (FileOutputStream out =
                             new FileOutputStream(pfd.getFileDescriptor())) {

                    switch (pass) {
                        case 0:
                            Arrays.fill(buffer, (byte) 0x00);
                            break;
                        case 1:
                            Arrays.fill(buffer, (byte) 0xFF);
                            break;
                        case 2:
                            Arrays.fill(buffer, (byte) 0xAA);
                            break;
                        case 3:
                            Arrays.fill(buffer, (byte) 0x55);
                            break;
                        case 4:
                        case 6:
                            random.nextBytes(buffer);
                            break;
                        case 5:
                            random.nextBytes(buffer);
                            for (int i = 0; i < buffer.length; i++)
                                buffer[i] = (byte) ~buffer[i];
                            break;
                    }

                    long remaining = size;

                    while (remaining > 0) {
                        int len = (int) Math.min(buffer.length, remaining);
                        out.write(buffer, 0, len);
                        remaining -= len;
                    }

                    out.getFD().sync();
                }
            }

            Toast.makeText(
                    activity,
                    fileDeleted(uri),
                    Toast.LENGTH_LONG
            ).show();

        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(
                    activity,
                    "Failed to shred file.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String fileDeleted(Uri uri) {
        DocumentFile file = DocumentFile.fromSingleUri(activity, uri);

        return file.delete() ? "Shredded (best effort) + deleted" : "Shredded but delete failed";
    }

    @Override
    public String getTitle() {
        return "File Shredder";
    }

    @Override
    public String getDescription() {
        return "Securely delete files.";
    }

    @Override
    public String getSubtitle() {
        return "+ 2/7/2026";
    }

    @Override
    public int getIcon() {
        return R.drawable.shredder;
    }
}