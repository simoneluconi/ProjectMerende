package com.simoneluconi.projectmerende;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.orm.SugarApp;
import com.orm.SugarContext;
import com.orm.SugarDb;
import com.orm.SugarRecord;
import com.simoneluconi.projectmerende.API.APIInterface;
import com.simoneluconi.projectmerende.API.Merenda;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://www.simoneluconi.com/merende/";

    TextView txtTotale;
    public List<Merenda> Merende;
    MerendeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SugarContext.init(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final RecyclerView mRecyclerView = findViewById(R.id.rv);
        txtTotale = findViewById(R.id.totale);
        txtTotale.setText(String.format(Locale.getDefault(), "Totale: %.2f€", 0.00));

        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        APIInterface service = retrofit.create(APIInterface.class);

        Call<List<Merenda>> merende = service.getLista();

        merende.enqueue(new Callback<List<Merenda>>() {
            @Override
            public void onResponse(Call<List<Merenda>> call, Response<List<Merenda>> response) {
                if(response.isSuccessful()) {
                    adapter = new MerendeAdapter(response.body());
                    mRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Merenda>> call, Throwable t) {
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

            UpdateTotale(Merende);

            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }


    public class MerendeAdapter extends RecyclerView.Adapter<MerendeAdapter.PersonViewHolder> {

        MerendeAdapter(List<Merenda> merende) {
            Merende = merende;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.merende_adapter, parent, false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(final PersonViewHolder holder, final int position) {
            holder.titolo.setText(Merende.get(position).getNome());

            Merenda m = SugarRecord.findById(Merenda.class, Merende.get(position).getId());
            if (m != null) {
                holder.currQty = m.getCurrentQty();
                holder.qty.setText(String.valueOf(holder.currQty));
            }


            holder.plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.currQty < Merende.get(position).getQmax()) {
                        holder.currQty++;
                        holder.qty.setText(String.valueOf(holder.currQty));

                        Merende.get(position).setCurrentQty(holder.currQty);

                        UpdateTotale(Merende);
                    }
                }
            });

            holder.min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.currQty > 0) {
                        holder.currQty--;
                        holder.qty.setText(String.valueOf(holder.currQty));
                        Merende.get(position).setCurrentQty(holder.currQty);
                        UpdateTotale(Merende);
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

            int currQty = 0;

            PersonViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.cv);
                qty = itemView.findViewById(R.id.qty);
                titolo = itemView.findViewById(R.id.nomemerenda);
                plus = itemView.findViewById(R.id.increment);
                min = itemView.findViewById(R.id.decrement);

                qty.setText(String.valueOf(currQty));
            }
        }

    }

    public void UpdateTotale(List<Merenda> merende) {
        double totale = 0;

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

            }

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

        txtTotale.setText(String.format(Locale.getDefault(), "Totale: %.2f€", totale));
    }
}

