package org.envirocar.core.dao;

import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.exception.DataRetrievalFailureException;

import java.util.List;

public interface CarNewDAO {
    List<Manufacturer> getAllManufacturer() throws DataRetrievalFailureException;
}
