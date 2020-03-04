package org.envirocar.remote.dao;

import org.envirocar.core.dao.CarNewDAO;
import org.envirocar.core.entity.Car;
import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.exception.DataRetrievalFailureException;
import org.envirocar.remote.service.CarServiceNew;
import org.envirocar.remote.service.EnviroCarService;
import org.envirocar.remote.util.EnvirocarServiceUtils;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import okhttp3.Cache;
import retrofit2.Call;
import retrofit2.Response;

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
}
