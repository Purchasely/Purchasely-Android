package io.purchasely.sample.java;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;


import io.purchasely.ext.Purchasely;
import io.purchasely.sample.R;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class FeatureListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_list);

        //TODO set the product id you want to display

        /*View view = Purchasely.presentationViewForPlacement(
                this,
                "onboarding",//Presentation Id, may be null for default
                null,
                loaded -> null,
                () -> {
                    ((FrameLayout) findViewById(R.id.container)).removeAllViews();
                    supportFinishAfterTransition();
                    return null;
                },
                (plyProductViewResult, plyPlan) -> {
                    String vendorId = plyPlan != null ? plyPlan.getVendorId() : null;
                    Snackbar.make(
                                    getWindow().getDecorView(),
                                    "Purchased result is $result with plan " + vendorId,
                                    Snackbar.LENGTH_LONG)
                            .show();
                    return null;
                }
        );*/

        //((FrameLayout) findViewById(R.id.container)).addView(view);

        findViewById(R.id.progressBar).setVisibility(View.GONE);

        //Use LiveData to be notified when a purchase is made
        Purchasely.livePurchase().observe(this, product -> {
            Log.d("Purchasely", "User purchased " + product);
        });
    }
}