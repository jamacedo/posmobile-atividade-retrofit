package br.com.posmobile.previsaodotempo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PrincipalActivity extends AppCompatActivity {

    ListView listaPrevisoes;
    List<Previsao> previsoes = new ArrayList<Previsao>();

    TextView tvTemperaturaHoje;
    TextView tvPeriodoHoje;
    TextView tvCidade;
    ImageView ivIconeHoje;

    Retrofit retrofit;
    PrevisoesAPI previsoesAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);

        listaPrevisoes      = (ListView) findViewById(R.id.previsoesListView);
        tvTemperaturaHoje   = (TextView) findViewById(R.id.textViewTempHoje);
        tvPeriodoHoje       = (TextView) findViewById(R.id.textViewPeriodoHoje);
        tvCidade            = (TextView) findViewById(R.id.textViewCidade);
        ivIconeHoje         = (ImageView)findViewById(R.id.imageViewIconeHoje);

        listaPrevisoes.setAdapter(new ListaPrevisaoAdapter(this, previsoes));



    }

    private void atualizaPrevisoes(List<Previsao> previsoes, Cidade cidade) {
        this.previsoes.clear();
        Previsao previsaoDestaque = previsoes.remove(0);
        tvTemperaturaHoje.setText(previsaoDestaque.getTemperatura());
        tvPeriodoHoje.setText(previsaoDestaque.getPeriodo());
        tvCidade.setText(cidade.getName());

        Glide.with(PrincipalActivity.this).
                load(String.format(Utils.URL_ICONE, previsaoDestaque.getIcone())).
                into(ivIconeHoje);

        this.previsoes.addAll(previsoes);
        ((ArrayAdapter) PrincipalActivity.this.listaPrevisoes.getAdapter()).notifyDataSetChanged();
    }

    //Inflando o menu de configurações
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuconfig, menu);
        return true;
    }

    //Ao clicar nas opções do menu, chama as respectivas ações
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.config:
                Intent i = new Intent(this, ConfigActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Testando valores das configurações
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String prefcidade = settings.getString("edtCidade", "sem dados");

        GsonBuilder gsonBldr = new GsonBuilder();
        gsonBldr.registerTypeAdapter(Previsao.class, new PrevisaoDeserializer());

        retrofit = new Retrofit.Builder()
                .baseUrl(Utils.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create(gsonBldr.create()))
                .build();

        previsoesAPI = retrofit.create(PrevisoesAPI.class);

        Call<Previsoes> callbackPrevisoes;
        callbackPrevisoes = previsoesAPI.getPrevisoes(prefcidade, Utils.API_KEY);

        callbackPrevisoes.enqueue(new Callback<Previsoes>() {
            @Override
            public void onResponse(Call<Previsoes> call, retrofit2.Response<Previsoes> response) {
                List<Previsao> previsoes = new ArrayList<>();
                previsoes.addAll(response.body().previsaoList);

                Cidade cidade = response.body().city;

                atualizaPrevisoes(previsoes, cidade);
            }

            @Override
            public void onFailure(Call<Previsoes> call, Throwable t) {

            }
        });

        //Toast.makeText(PrincipalActivity.this, "Preferências: Cidade["+prefcidade+"], atualização automática["+prefatualiza+"]", Toast.LENGTH_LONG).show();
    }

}
