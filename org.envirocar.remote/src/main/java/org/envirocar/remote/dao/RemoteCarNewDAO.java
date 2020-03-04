package org.envirocar.remote.dao;


import org.envirocar.core.dao.CarNewDAO;
import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.entity.ManufacturerCar;
import org.envirocar.core.exception.DataRetrievalFailureException;
import org.envirocar.remote.service.CarServiceNew;
import org.envirocar.remote.service.EnviroCarService;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;

public class RemoteCarNewDAO extends BaseRemoteDAO<CacheCarDAO, CarServiceNew> implements CarNewDAO {

    @Inject
    public RemoteCarNewDAO(CacheCarDAO cacheDao, CarServiceNew remoteService) {
        super(cacheDao, remoteService);
    }

    public List<Manufacturer> getAllManufacturer() throws DataRetrievalFailureException {
        Call<List<Manufacturer>> manufacturerCall = remoteService.getAllManufacturer();

        try {
            Response<List<Manufacturer>> carsResponse = manufacturerCall.execute();
            List<Manufacturer> result = carsResponse.body();
            return result;
        } catch (IOException e) {
            throw new DataRetrievalFailureException(e);
        }

    }

    @Override
    public List<ManufacturerCar> getAllManufacturerCar(String manufid) throws DataRetrievalFailureException {
        Call<List<ManufacturerCar>> manufactureCarCall = remoteService.getAllManufacturerCar(manufid);

        try {
            Response<List<ManufacturerCar>> manufactureResponse = manufactureCarCall.execute();
            List<ManufacturerCar> result = manufactureResponse.body();
            return result;
        } catch (IOException e) {
            throw new DataRetrievalFailureException(e);
        }
    }

    @Override
    public Observable<List<Manufacturer>> getAllManufactureObservable() {
        final CarServiceNew carServiceNew = EnviroCarService.getCarServiceNew();
        Call<List<Manufacturer>> carsCall = carServiceNew.getAllManufacturer();
        return Observable.just(carsCall)
                .concatMap(listCall -> {
                    try {
                        Response<List<Manufacturer>> response = listCall.execute();

                        Observable<List<Manufacturer>> res = Observable.just(response.body());

                        return res;
                    } catch (IOException e) {
                        return Observable.error(new DataRetrievalFailureException(e));
                    }
                });
    }

    @Override
    public Observable<List<ManufacturerCar>> getAllManufacturerCarObservable(String manufid) {
        final CarServiceNew carServiceNew = EnviroCarService.getCarServiceNew();
        Call<List<ManufacturerCar>> manufacturerCarsCall = carServiceNew.getAllManufacturerCar(manufid);
        return Observable.just(manufacturerCarsCall)
                .concatMap(listCall -> {
                    try {
                        Response<List<ManufacturerCar>> response = listCall.execute();

                        Observable<List<ManufacturerCar>> res = Observable.just(response.body());

                        return res;
                    } catch (IOException e) {
                        return Observable.error(new DataRetrievalFailureException(e));
                    }
                });
    }
}
