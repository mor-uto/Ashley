package lol.moruto.ashley.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lol.moruto.ashley.R;
import lol.moruto.ashley.feature.Feature;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    public interface OnFeatureClickListener {
        void onFeatureClick(Feature feature);
    }

    private final List<Feature> features;
    private final OnFeatureClickListener listener;

    public HomeAdapter(List<Feature> features, OnFeatureClickListener listener) {
        this.features = features;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Feature feature = features.get(position);

        holder.title.setText(feature.getTitle());
        holder.description.setText(feature.getDescription());
        holder.subtitle.setText(feature.getSubtitle());
        holder.icon.setImageResource(feature.getIcon());

        holder.card.setOnClickListener(v -> listener.onFeatureClick(feature));
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView card;
        ImageView icon;
        TextView title, description, subtitle;

        ViewHolder(View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            icon = itemView.findViewById(R.id.card_icon);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            subtitle = itemView.findViewById(R.id.card_subtitle);
        }
    }
}