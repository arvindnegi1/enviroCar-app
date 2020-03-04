package org.envirocar.remote.dao;

import android.util.Log;
import android.widget.Toast;

import org.envirocar.core.dao.CarNewDAO;
import org.envirocar.core.entity.Car;
import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.exception.DataRetrievalFailureException;
import org.envirocar.remote.service.CarService;
import org.envirocar.remote.service.CarServiceNew;
import org.envirocar.remote.service.EnviroCarService;
import org.envirocar.remote.util.EnvirocarServiceUtils;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import okhttp3.Cache;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RemoteCarNewDAO extends BaseRemoteDAO<CacheCarDAO, CarServiceNew> implements CarNewDAO {

    @Inject
    public RemoteCarNewDAO(CacheCarDAO cacheDao, CarServiceNew remoteService) {
        super(cacheDao, remoteService);
    }

    public List<Manufacturer> getAllManufacturer() throws DataRetrievalFailureException {
        remoteService.newCarServiceUrl(EnviroCarService.BASE_URL_NEW);
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
    public Observable<List<Manufacturer>> getAllManufactureObservable() {
        final CarServiceNew carServiceNew = EnviroCarService.getCarServiceNew();
         carServiceNew.newCarServiceUrl(EnviroCarService.BASE_URL_NEW);
        Call<List<Manufacturer>> carsCall = carServiceNew.getAllManufacturer();
        return Observable.just(carsCall)
                .concatMap(listCall -> {
                    try {
                        // Execute the call.
                        Response<List<Manufacturer>> response = listCall.execute();

                        Observable<List<Manufacturer>> res = Observable.just(response.body());

                        return res;
                    } catch (IOException e) {
                        // Return an error observable that invokes the observers onError method.
                        return Observable.error(new DataRetrievalFailureException(e));
                    }
                });
    }
}
