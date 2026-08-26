package lol.moruto.ashley.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lol.moruto.ashley.R;
import lol.moruto.ashley.util.ImageCrypto;

public class GalleryAdapter
        extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    public interface Listener {
        void onSelectionChanged(int count);
        void onImageClicked(File file);
    }

    private final Context context;
    private final List<File> images;
    private final Listener listener;

    private final Set<File> selected =
            new HashSet<>();

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    public GalleryAdapter(
            Context context,
            List<File> images,
            Listener listener) {

        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_gallery,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        File file = images.get(position);

        holder.image.setImageDrawable(null);

        holder.check.setVisibility(
                selected.contains(file)
                        ? View.VISIBLE
                        : View.GONE
        );

        executor.execute(() -> {

            Bitmap bitmap =
                    decryptImage(file);

            holder.image.post(() -> {

                if (bitmap != null)
                    holder.image.setImageBitmap(bitmap);
            });
        });

        holder.itemView.setOnClickListener(v ->
                listener.onImageClicked(file)
        );

        holder.itemView.setOnLongClickListener(v -> {

            toggleSelection(file);

            return true;
        });
    }

    public void toggleSelection(File file) {

        if (selected.contains(file))
            selected.remove(file);
        else
            selected.add(file);

        notifyItemChanged(
                images.indexOf(file)
        );

        listener.onSelectionChanged(
                selected.size()
        );
    }

    public boolean isSelectionMode() {
        return !selected.isEmpty();
    }

    public int getSelectedCount() {
        return selected.size();
    }

    public int deleteSelected() {

        int deleted = 0;

        for (File file :
                new ArrayList<>(selected)) {

            if (file.delete()) {
                images.remove(file);
                deleted++;
            }
        }

        selected.clear();

        notifyDataSetChanged();

        listener.onSelectionChanged(0);

        return deleted;
    }

    private Bitmap decryptImage(File file) {

        try {

            byte[] encrypted =
                    Files.readAllBytes(
                            file.toPath()
                    );

            byte[] decrypted =
                    ImageCrypto.decrypt(encrypted);

            return BitmapFactory.decodeByteArray(
                    decrypted,
                    0,
                    decrypted.length
            );

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onViewRecycled(
            @NonNull ViewHolder holder) {

        holder.image.setImageDrawable(null);

        super.onViewRecycled(holder);
    }

    @Override
    public void onDetachedFromRecyclerView(
            @NonNull RecyclerView recyclerView) {

        executor.shutdown();

        super.onDetachedFromRecyclerView(
                recyclerView
        );
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView image;
        ImageView check;

        ViewHolder(View view) {
            super(view);

            image = view.findViewById(
                    R.id.gallery_image
            );

            check = view.findViewById(
                    R.id.gallery_check
            );
        }
    }
}