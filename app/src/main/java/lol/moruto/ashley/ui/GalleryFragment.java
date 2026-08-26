package lol.moruto.ashley.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lol.moruto.ashley.R;
import lol.moruto.ashley.adapter.GalleryAdapter;
import lol.moruto.ashley.util.ImageViewerDialog;

public class GalleryFragment extends Fragment
        implements GalleryAdapter.Listener {

    private GalleryAdapter adapter;
    private MaterialButton deleteButton;

    public GalleryFragment() {
        super(R.layout.fragment_gallery);
    }

    @Override
    public void onViewCreated(
            @NonNull View v,
            @Nullable Bundle state) {

        super.onViewCreated(v, state);

        RecyclerView recycler =
                v.findViewById(R.id.gallery_recycler);

        deleteButton =
                v.findViewById(R.id.gallery_delete);

        recycler.setLayoutManager(
                new GridLayoutManager(
                        requireContext(),
                        3
                )
        );

        adapter = new GalleryAdapter(
                requireContext(),
                loadImages(),
                this
        );

        recycler.setAdapter(adapter);

        deleteButton.setVisibility(View.GONE);

        deleteButton.setOnClickListener(x ->
                confirmDelete()
        );
    }

    private List<File> loadImages() {

        File dir = new File(
                requireContext().getFilesDir(),
                "gallery"
        );

        File[] files = dir.listFiles();

        if (files == null)
            return new ArrayList<>();

        Arrays.sort(
                files,
                (a, b) -> Long.compare(
                        b.lastModified(),
                        a.lastModified()
                )
        );

        return new ArrayList<>(
                Arrays.asList(files)
        );
    }

    @Override
    public void onSelectionChanged(int count) {

        if (count == 0) {
            deleteButton.setVisibility(View.GONE);
            return;
        }

        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setText("Delete (" + count + ")");
    }

    @Override
    public void onImageClicked(File file) {
        if (adapter.isSelectionMode()) {
            adapter.toggleSelection(file);
            return;
        }

        ImageViewerDialog dialog =
                ImageViewerDialog.newInstance(file);

        dialog.show(
                getChildFragmentManager(),
                "image_viewer"
        );
    }

    private void confirmDelete() {

        int count = adapter.getSelectedCount();

        if (count == 0)
            return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete photos?")
                .setMessage(
                        "Delete " + count +
                                " selected photo" +
                                (count == 1 ? "" : "s") +
                                " permanently?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteSelected()
                )
                .show();
    }

    private void deleteSelected() {

        int deleted = adapter.deleteSelected();

        Toast.makeText(
                requireContext(),
                deleted + " photo" +
                        (deleted == 1 ? "" : "s") +
                        " deleted",
                Toast.LENGTH_SHORT
        ).show();

        deleteButton.setVisibility(View.GONE);
    }
}