package io.purchasely.sample.java;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import io.purchasely.ext.DisplayProductListener;
import io.purchasely.ext.Purchasely;
import io.purchasely.sample.java.R;

public class FeatureListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_list);

        Purchasely.displayProduct("YOUR_PRODUCT_ID", new DisplayProductListener() {
            @Override
            public void onFailure(@NotNull Throwable throwable) {
                Log.e("SinglePlan", "Error", throwable);
                String message = "error";
                if(throwable.getMessage() != null) {
                    message = throwable.getMessage();
                }
                Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@NotNull Fragment fragment) {
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.inappFragment, fragment, "InAppFragment")
                        .commitAllowingStateLoss();

                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }
}