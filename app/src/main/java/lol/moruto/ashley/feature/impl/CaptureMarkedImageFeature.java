package lol.moruto.ashley.feature.impl;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lol.moruto.ashley.MainActivity;
import lol.moruto.ashley.R;
import lol.moruto.ashley.feature.Feature;

public class CaptureMarkedImageFeature extends Feature {
    private String currentPhotoPath;

    private final ActivityResultLauncher<String> permissionLauncher;
    private final ActivityResultLauncher<Intent> cameraLauncher;

    public CaptureMarkedImageFeature(MainActivity activity) {
        super(activity);

        permissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {if (granted) launchCamera();});

        cameraLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> handleCameraResult(result.getResultCode()));
    }

    @Override
    public void execute() {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
            return;
        }

        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void handleCameraResult(int resultCode) {
        if (resultCode != Activity.RESULT_OK || currentPhotoPath == null) return;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

        if (bitmap == null) return;

        Uri uri = saveImageToGallery(addWatermark(bitmap, new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date())));

        if (uri != null) showShareDialog(uri);
    }

    private void launchCamera() {
        try {
            File file = createImageFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file));

            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(activity, "Couldn't open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("IMAGE_" + timestamp, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap addWatermark(Bitmap src, String text) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(5f, 3f, 3f, Color.BLACK);
        paint.setTextSize(result.getWidth() * 0.05f);

        float x = result.getWidth() - paint.measureText(text) - 40;
        float y = result.getHeight() - 60;

        canvas.drawText(text, x, y, paint);
        return result;
    }

    private Uri saveImageToGallery(Bitmap bitmap) {
        try {
            String filename = "IMG_" + System.currentTimeMillis() + ".jpg";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Ashley");

            Uri uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;

            try (OutputStream out = activity.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            }

            Toast.makeText(activity, "Saved to gallery!", Toast.LENGTH_SHORT).show();
            return uri;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showShareDialog(Uri uri) {
        new AlertDialog.Builder(activity).setTitle("Photo Saved").setMessage("Your photo was saved to the gallery.").setPositiveButton("Share", (dialog, which) -> shareImage(uri)).setNegativeButton("Close", (dialog, which) -> dialog.dismiss()).show();
    }

    private void shareImage(Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    @Override
    public String getTitle() {
        return "Snap Photo";
    }

    @Override
    public String getDescription() {
        return "Snap photos with a watermark.";
    }

    @Override
    public String getSubtitle() {
        return "+ 2/7/2026";
    }

    @Override
    public int getIcon() {
        return R.drawable.camera;
    }
}