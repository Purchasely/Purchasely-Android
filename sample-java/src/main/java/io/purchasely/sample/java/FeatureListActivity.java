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
import io.purchasely.ext.PLYProductViewResult;
import io.purchasely.ext.ProductViewResultListener;
import io.purchasely.ext.Purchasely;
import io.purchasely.models.PLYPlan;
import io.purchasely.sample.java.R;

public class FeatureListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_list);

        //TODO set the product id you want to display
        Fragment fragment = Purchasely.productFragment("YOUR_PRODUCT_ID", "default", new ProductViewResultListener() {
            @Override
            public void onResult(@NotNull PLYProductViewResult plyProductViewResult, @Nullable PLYPlan plyPlan) {
                Snackbar.make(
                        getWindow().getDecorView(),
                        "Purchased result is $result with plan ${plan?.vendorId}",
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.inappFragment, fragment, "InAppFragment")
                .commitAllowingStateLoss();

        findViewById(R.id.progressBar).setVisibility(View.GONE);

        //Use LiveData to be notified when a purchase is made
        Purchasely.livePurchase().observe(this, product -> {
            Log.d("Purchasely", "User purchased " + product);
            Snackbar.make(getWindow().getDecorView(), "Purchased " + product.getVendorId(), Snackbar.LENGTH_SHORT).show();
        });
    }
}