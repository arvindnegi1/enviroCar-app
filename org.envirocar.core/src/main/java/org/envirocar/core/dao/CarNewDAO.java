package org.envirocar.core.dao;

import org.envirocar.core.entity.Car;
import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.exception.DataRetrievalFailureException;

import java.util.List;

import io.reactivex.Observable;

public interface CarNewDAO {
   public  List<Manufacturer> getAllManufacturer() throws DataRetrievalFailureException;
   public Observable<List<Manufacturer>> getAllManufactureObservable();
}
