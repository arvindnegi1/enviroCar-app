package org.envirocar.remote.service;

import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.entity.ManufacturerCar;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CarServiceNew {
    @GET("manufacturers")
    Call<List<Manufacturer>> getAllManufacturer();

    @GET("manufacturers/{manfuid}/vehicles")
    Call<List<ManufacturerCar>> getAllManufacturerCar(@Path("manfuid")String manufuid);
}
