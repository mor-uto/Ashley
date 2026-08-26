package lol.moruto.ashley;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import lol.moruto.ashley.feature.FeatureManager;
import lol.moruto.ashley.ui.*;

public class MainActivity extends AppCompatActivity {
    private FeatureManager featureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_main);

        if (!"dc61eab9a1be6375db2c8b9debdb5486912651d6fca004887fe54f4e8869f4eb".equals(sha256(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)))) {
            Toast.makeText(this, "Wrong phone.", Toast.LENGTH_LONG).show();
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

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }
}
