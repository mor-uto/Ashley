package lol.moruto.ashley.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lol.moruto.ashley.MainActivity;
import lol.moruto.ashley.R;
import lol.moruto.ashley.adapter.HomeAdapter;
import lol.moruto.ashley.feature.Feature;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, @Nullable Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) requireActivity();

        RecyclerView recycler = view.findViewById(R.id.recyclerView);

        recycler.setLayoutManager(new GridLayoutManager(activity, 2));

        HomeAdapter adapter = new HomeAdapter(activity.getFeatureManager().getFeatures(), Feature::execute);

        recycler.setAdapter(adapter);
    }
}