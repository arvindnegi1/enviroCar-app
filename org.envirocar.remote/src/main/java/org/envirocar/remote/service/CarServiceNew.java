package org.envirocar.remote.service;

import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.entity.ManufacturerCar;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface CarServiceNew {

    @GET("manufacturers")
    Call<List<Manufacturer>> getAllManufacturer();

    @GET("manufacturers")
    Call<List<Manufacturer>> getAllManufacturerObservable();

    @GET("manufacturers/{manfuid}/vehicles")
    Call<List<ManufacturerCar>> getAllManufacturerCar(@Path("manfuid")String manufuid);

    @GET("manufacturers/{manfuid}/vehicles")
    Call<List<ManufacturerCar>> getAllManufacturerCarObservable(@Path("manfuid")String manufuid);
}
