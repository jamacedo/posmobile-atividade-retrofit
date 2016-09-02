package br.com.posmobile.previsaodotempo;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    ImageView ivIconeHoje;


    Retrofit retrofit;
    PrevisoesAPI previsoesAPI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);
        listaPrevisoes = (ListView) findViewById(R.id.previsoesListView);
        tvTemperaturaHoje = (TextView) findViewById(R.id.textViewTempHoje);
        tvPeriodoHoje = (TextView) findViewById(R.id.textViewPeriodoHoje);
        ivIconeHoje = (ImageView) findViewById(R.id.imageViewIconeHoje);

        listaPrevisoes.setAdapter(new ListaPrevisaoAdapter(this, previsoes));

        //Precisamos registrar esse deserializador para ajustar o Json da API com nosso objeto com atributos resumidos...
        GsonBuilder gsonBldr = new GsonBuilder();
        gsonBldr.registerTypeAdapter(Previsao.class, new PrevisaoDeserializer());

        retrofit = new Retrofit.Builder()
                //done Inclua a url base no construtor do Retrofit
                .baseUrl(Utils.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create(gsonBldr.create()))
                .build();

        //done Inicialize a variável previsoesAPI utilizando o método create do objeto retrofit
        PrevisoesAPI previsoesAPI = retrofit.create(PrevisoesAPI.class);

        Call<Previsoes> callbackPrevisoes;
        callbackPrevisoes = previsoesAPI.getPrevisoes("vitoria,brazil", Utils.API_KEY);
        //done Chame o método enqueue (do objeto callbackPrevisoes) passando como parametro um novo Callback
        callbackPrevisoes.enqueue(new Callback<Previsoes>() {
            //done Complete o método onResponse (do novo Callback) 1. buscando as previsões em response.body() 2. Chamando o método atualizaPrevisoes
            @Override
            public void onResponse(Call<Previsoes> call, retrofit2.Response<Previsoes> response) {
                Previsoes prev = response.body();
                atualizaPrevisoes(prev.previsaoList);
            }

            @Override
            public void onFailure(Call<Previsoes> call, Throwable t) {

            }
        });

    }

    private void atualizaPrevisoes(List<Previsao> previsoes) {
        this.previsoes.clear();
        Previsao previsaoDestaque = previsoes.remove(0);
        tvTemperaturaHoje.setText(previsaoDestaque.getTemperatura());
        tvPeriodoHoje.setText(previsaoDestaque.getPeriodo());

        Glide.with(PrincipalActivity.this).
                load(String.format(Utils.URL_ICONE, previsaoDestaque.getIcone())).
                into(ivIconeHoje);

        this.previsoes.addAll(previsoes);
        ((ArrayAdapter) PrincipalActivity.this.listaPrevisoes.getAdapter()).notifyDataSetChanged();
    }

}
