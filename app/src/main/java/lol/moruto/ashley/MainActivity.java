package lol.moruto.ashley;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import lol.moruto.ashley.feature.FeatureManager;
import lol.moruto.ashley.ui.*;
import lol.moruto.ashley.util.CryptoUtil;

public class MainActivity extends AppCompatActivity {
    private FeatureManager featureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_main);

        if (!"dc61eab9a1be6375db2c8b9debdb5486912651d6fca004887fe54f4e8869f4eb".equals(CryptoUtil.sha256(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)))) {
            Toast.makeText(this, "You're not allowed to use this Application.", Toast.LENGTH_LONG).show();
            finish();
        }

        featureManager = new FeatureManager(this);

        EdgeToEdge.enable(this);

        BottomNavigationView navigation = findViewById(R.id.bottomNavigation);

        if (savedInstanceState == null) loadFragment(new HomeFragment());

        navigation.setOnItemSelectedListener(item -> {

            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_hrt) {
                fragment = new HRTFragment();
            }
            loadFragment(fragment);

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }
}
