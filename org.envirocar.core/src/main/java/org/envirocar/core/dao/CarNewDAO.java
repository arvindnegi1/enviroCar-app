package org.envirocar.core.dao;

import org.envirocar.core.entity.Car;
import org.envirocar.core.entity.CarNew;
import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.entity.ManufacturerCar;
import org.envirocar.core.exception.DataRetrievalFailureException;

import java.util.List;

import io.reactivex.Observable;

public interface CarNewDAO {
   public  List<Manufacturer> getAllManufacturer() throws DataRetrievalFailureException;
   public List<ManufacturerCar> getAllManufacturerCar(String manufid) throws DataRetrievalFailureException;
   public CarNew getAllCarNew(String manufid,String carid) throws DataRetrievalFailureException;
   public Observable<List<Manufacturer>> getAllManufactureObservable();
   public Observable<List<ManufacturerCar>> getAllManufacturerCarObservable(String manufid);
   public Observable<CarNew> getAllCarNewObservable(String manufid,String carid);
}
