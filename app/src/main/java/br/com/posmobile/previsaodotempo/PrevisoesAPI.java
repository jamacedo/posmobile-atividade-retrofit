package br.com.posmobile.previsaodotempo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by alexandre on 07/08/16.
 */

public interface PrevisoesAPI {
    //todo Inclua um parametro para passar dinamicamente o APPID como String
    @GET("daily?mode=json&lang=pt&units=metric&cnt=14")
    Call<Previsoes> getPrevisoes(@Query("q") String cidade_pais,@Query("APPID") String api_key);
}
