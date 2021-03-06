package com.simoneluconi.projectmerende;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.orm.SugarContext;
import com.orm.SugarRecord;
import com.simoneluconi.projectmerende.API.APIInterface;
import com.simoneluconi.projectmerende.API.Merenda;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://www.simoneluconi.com/merende/";
    public List<Merenda> Merende;
    TextView txtTotale;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SugarContext.init(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Merende = new ArrayList<>();

        mRecyclerView = findViewById(R.id.rv);
        txtTotale = findViewById(R.id.totale);
        txtTotale.setText(String.format(Locale.getDefault(), "Totale: %.2f€", 0.00));

        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new MerendeAdapter());

        APIInterface service = retrofit.create(APIInterface.class);

        Call<List<Merenda>> merende = service.getLista();

        merende.enqueue(new Callback<List<Merenda>>() {
            @Override
            public void onResponse(@NonNull Call<List<Merenda>> call, @NonNull Response<List<Merenda>> response) {
                if (response.isSuccessful()) {
                    Merende.clear();
                    if (response.body() != null) {

                        for (Merenda m : response.body()) {
                            Merenda mtmp = SugarRecord.findById(Merenda.class, m.getId());
                            if (mtmp != null) {
                                m.setCurrentQty(mtmp.getCurrentQty());
                            }
                        }

                        Merende.addAll(response.body());
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        UpdateTotale(Merende, false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Merenda>> call, @NonNull Throwable t) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset) {
            for (Merenda m : Merende) {
                m.setCurrentQty(0);
                SugarRecord.save(m);
            }

            UpdateTotale(Merende, true);
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    public void UpdateTotale(List<Merenda> merende, boolean updateDB) {
        double totale = 0;
        int qTtot = 0;

        int pizzeTotali = 0;
        double prezzoSingoloPizza = 0;
        double prezzoDoppioPizza = 0;

        for (Merenda m : merende) {
            if (m.getCurrentQty() > 0) {

                if (m.getNome().toLowerCase().contains("pizza")) {
                    pizzeTotali += m.getCurrentQty();
                    prezzoDoppioPizza = m.getPrezzoDoppio();
                    prezzoSingoloPizza = m.getPrezzo();
                } else
                    totale += m.getPrezzo() * m.getCurrentQty();

                qTtot += m.getCurrentQty();
            }

            if (updateDB)
                SugarRecord.update(m);
        }

        if (pizzeTotali > 0) {

            if (pizzeTotali % 2 == 1) {
                totale += prezzoDoppioPizza * (pizzeTotali - 1);
                totale += prezzoSingoloPizza;
            } else {
                totale += prezzoDoppioPizza * pizzeTotali;
            }

        }

        int qOmgg = qTtot / 5;
        if (qOmgg == 0)
            txtTotale.setText(String.format(Locale.getDefault(), "Totale: %.2f€", totale));
        else
            txtTotale.setText(String.format(Locale.getDefault(), "Totale: %.2f€ (%2$s omaggio)", totale, qOmgg));
    }

    public class MerendeAdapter extends RecyclerView.Adapter<MerendeAdapter.PersonViewHolder> {
        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.merende_adapter, parent, false);
            return new PersonViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final PersonViewHolder holder, final int p) {
            final int position = holder.getAdapterPosition();

            final Merenda m = Merende.get(position);
            holder.titolo.setText(m.getNome());

            holder.qty.setText(String.valueOf(m.getCurrentQty()));

            holder.plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (m.getCurrentQty() < m.getQmax()) {
                        m.setCurrentQty(m.getCurrentQty() + 1);
                        holder.qty.setText(String.valueOf(m.getCurrentQty()));

                        UpdateTotale(Merende, true);
                    }
                }
            });

            holder.min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (m.getCurrentQty() > 0) {
                        m.setCurrentQty(m.getCurrentQty() - 1);
                        holder.qty.setText(String.valueOf(m.getCurrentQty()));

                        UpdateTotale(Merende, true);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return Merende.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public class PersonViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView qty;
            TextView titolo;

            ImageButton plus;
            ImageButton min;

            PersonViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.cv);
                qty = itemView.findViewById(R.id.qty);
                titolo = itemView.findViewById(R.id.nomemerenda);
                plus = itemView.findViewById(R.id.increment);
                min = itemView.findViewById(R.id.decrement);
            }
        }

    }
}

