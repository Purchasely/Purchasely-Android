package io.purchasely.sample.java;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.purchasely.ext.LogLevel;
import io.purchasely.ext.PLYUI;
import io.purchasely.ext.ProductsListener;
import io.purchasely.ext.Purchasely;
import io.purchasely.ext.UIListener;
import io.purchasely.models.PLYPlan;
import io.purchasely.models.PLYProduct;
import io.purchasely.sample.java.R;

public class MainActivity extends AppCompatActivity {

    private Adapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));

        adapter = new Adapter();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        Purchasely.start(getApplicationContext(), "YOUR_API_KEY", "YOUR_USER_ID", null, null);

        //set your user id to bind the purchase or to restore it
        //Purchasely.setUserId("");

        //set log level for debugging
        //Purchasely.setLogLevel(LogLevel.DEBUG);

        /*
        Implement UI Listener to handle UI event that may appear to user (success and error dialog)
        Purchasely.setUiListener(new UIListener() {
            @Override
            public void onAlert(@NotNull PLYUI ui) {
                if(ui instanceof PLYUI.InAppSuccess) {
                    //TODO display success view
                }
            }
        });
        */

        //Use LiveData to be notified when a purchase is made
        Purchasely.livePurchase().observe(this, product -> {
            Log.d("Purchasely", "User purchased " + product);
            Snackbar.make(findViewById(R.id.recyclerView), "Purchased " + product.getVendorId(), Snackbar.LENGTH_SHORT).show();
        });

        findViewById(R.id.buttonDisplayFeatureList).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), FeatureListActivity.class)));

        Purchasely.getProducts(new ProductsListener() {
            @Override
            public void onSuccess(@NotNull List<PLYProduct> list) {
                List<PLYPlan> plans = new ArrayList<>();
                for(int i = 0; i < list.size(); i++) {
                    plans.addAll(list.get(i).getPlans());
                }
                adapter.list.addAll(plans);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NotNull Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Error " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    static class Adapter extends RecyclerView.Adapter<Holder> {

        List<PLYPlan> list = new ArrayList<>();

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(generateTextView(parent));
        }

        private View generateTextView(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setPaddingRelative(60, 30, 60, 0);
            return view;
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.bind(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    static class Holder extends ViewHolder {

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(PLYPlan plan) {
            String productId = "";
            if(plan.getStoreInformation().getStore_product_id() != null) {
                productId = plan.getStoreInformation().getStore_product_id();
            }
            StringBuilder content = new StringBuilder();
            content.append(productId);
            content.append("\n");
            content.append(String.format("Full Price: %s", plan.localizedFullPrice()));
            content.append("\n");
            content.append(String.format("Price: %s", plan.localizedPrice()));
            content.append("\n");
            content.append(String.format("Period: %s", plan.localizedPeriod()));
            content.append("\n");
            content.append(String.format("Full introductory price: %s", plan.localizedFullIntroductoryPrice()));
            content.append("\n");
            content.append(String.format("Introductory Price: %s", plan.localizedIntroductoryPrice()));
            content.append("\n");
            content.append(String.format("Introductory Period: %s", plan.localizedIntroductoryPeriod()));
            content.append("\n");
            content.append(String.format("Introductory Duration: %s", plan.localizedIntroductoryDuration()));
            content.append("\n");
            content.append(String.format("Trial Period: %s", plan.localizedTrialDuration()));
            content.append("\n");
            content.append(String.format("Numeric Price: %s", plan.getPrice()));
            content.append("\n");
            content.append(String.format("Price Currency Code: %s", plan.getPriceCurrencyCode()));
            content.append("\n");
            content.append(String.format("Daily Equivalent: %s", plan.dailyEquivalentPrice()));
            content.append("\n");
            content.append(String.format("Weekly Equivalent: %s", plan.weeklyEquivalentPrice()));
            content.append("\n");
            content.append(String.format("Monthly Equivalent: %s", plan.monthlyEquivalentPrice()));
            content.append("\n");
            content.append(String.format("Yearly Equivalent: %s", plan.yearlyEquivalentPrice()));
            ((TextView) itemView).setText(content);
        }
    }

}
