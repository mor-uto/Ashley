package lol.moruto.ashley.util;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lol.moruto.ashley.MainActivity;
import lol.moruto.ashley.feature.impl.FileShredderFeature;
import lol.moruto.ashley.util.ImageCrypto;

public class ImageViewerDialog extends DialogFragment {

    private static final String ARG_FILE = "file";

    private Bitmap bitmap;
    private ImageView imageView;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static ImageViewerDialog newInstance(File file) {

        ImageViewerDialog dialog =
                new ImageViewerDialog();

        Bundle args = new Bundle();

        args.putString(
                ARG_FILE,
                file.getAbsolutePath()
        );

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setStyle(
                STYLE_NORMAL,
                android.R.style.Theme_Material_NoActionBar_Fullscreen
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(
            @Nullable Bundle state) {

        LinearLayout root =
                new LinearLayout(requireContext());

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(Gravity.CENTER);

        root.setBackgroundColor(
                android.graphics.Color.BLACK
        );

        imageView = new ImageView(
                requireContext()
        );

        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        root.addView(
                imageView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout buttons =
                new LinearLayout(
                        requireContext()
                );

        buttons.setGravity(Gravity.CENTER);

        androidx.appcompat.widget.AppCompatButton
                deleteButton =
                new androidx.appcompat.widget.AppCompatButton(
                        requireContext()
                );

        deleteButton.setText("Delete");

        buttons.addView(deleteButton);

        root.addView(
                buttons,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        deleteButton.setOnClickListener(v ->
                confirmDelete()
        );

        loadImage();

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setView(root)
                        .create();

        return dialog;
    }

    private void loadImage() {

        Bundle args = getArguments();

        if (args == null)
            return;

        String path =
                args.getString(ARG_FILE);

        if (path == null)
            return;

        File file = new File(path);

        executor.execute(() -> {

            try {

                byte[] encrypted =
                        Files.readAllBytes(
                                file.toPath()
                        );

                byte[] decrypted =
                        ImageCrypto.decrypt(
                                encrypted
                        );

                Bitmap result =
                        BitmapFactory.decodeByteArray(
                                decrypted,
                                0,
                                decrypted.length
                        );

                requireActivity().runOnUiThread(() -> {

                    if (result == null)
                        return;

                    bitmap = result;

                    if (imageView != null)
                        imageView.setImageBitmap(
                                result
                        );
                });

            } catch (Exception e) {

                e.printStackTrace();

                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    requireContext(),
                                    "Couldn't decrypt image",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }
            }
        });
    }

    private void confirmDelete() {

        Bundle args = getArguments();

        if (args == null)
            return;

        String path =
                args.getString(ARG_FILE);

        if (path == null)
            return;

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle("Delete photo?")
                .setMessage(
                        "This will permanently delete the encrypted photo."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            File file = new File(path);

                            FileShredderFeature shredder = new FileShredderFeature((lol.moruto.ashley.MainActivity) requireActivity());

                            shredder.shredFile(Uri.fromFile(file));
                            Toast.makeText(requireContext(), "Photo shredded and deleted", Toast.LENGTH_SHORT).show();

                            dismiss();
                        }
                )
                .show();
    }

    @Override
    public void onDestroyView() {

        if (imageView != null)
            imageView.setImageDrawable(null);

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
}