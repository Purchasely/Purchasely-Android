package io.purchasely.sample.java;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.purchasely.billing.Store;
import io.purchasely.ext.LogLevel;
import io.purchasely.ext.PLYRunningMode;
import io.purchasely.ext.Purchasely;
import io.purchasely.google.GoogleStore;
import io.purchasely.models.PLYPlan;
import io.purchasely.sample.R;

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

        List<Store> stores = new ArrayList<>();
        stores.add(new GoogleStore());

        new Purchasely.Builder(getApplicationContext())
                //TODO set your api key
                .apiKey("afa96c76-1d8e-4e3c-a48f-204a3cd93a15")
                .logLevel(LogLevel.DEBUG)
                .isReadyToPurchase(true)
                .runningMode(PLYRunningMode.Full.INSTANCE)
                .stores(stores)
                .build();

        Purchasely.start();

        findViewById(R.id.buttonDisplayFeatureList).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), FeatureListActivity.class)));

        /*Purchasely.allProducts(new ProductsListener() {
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
        });*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Purchasely.close();
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

    static class Holder extends RecyclerView.ViewHolder {

        Holder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(PLYPlan plan) {
            StringBuilder content = new StringBuilder();
            content.append(plan.getStore_product_id());
            content.append("\n");
            content.append(String.format("Full Price: %s", plan.localizedFullPrice()));
            content.append("\n");
            content.append(String.format("Price: %s", plan.localizedPrice()));
            content.append("\n");
            content.append(String.format("Period: %s", plan.localizedPeriod()));
            content.append("\n");
            content.append(String.format("Full introductory price: %s", plan.localizedFullIntroductoryPrice()));
            content.append("\n");
            content.append(String.format("Introductory Price: %s", plan.localizedIntroductoryPrice(false)));
            content.append("\n");
            content.append(String.format("Introductory Period: %s", plan.localizedIntroductoryPeriod()));
            content.append("\n");
            content.append(String.format("Introductory Duration: %s", plan.localizedIntroductoryDuration()));
            content.append("\n");
            content.append(String.format("Trial Period: %s", plan.localizedTrialDuration()));
            content.append("\n");
            content.append(String.format("Numeric Price: %s", plan.price()));
            content.append("\n");
            content.append(String.format("Currency Symbol: %s", plan.currencySymbol()));
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
