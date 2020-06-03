package org.envirocar.core.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.envirocar.core.entity.Manufacturers;
import org.envirocar.core.entity.Vehicles;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface CarNewDAO {
    @Query("SELECT * FROM manufacturers")
    Single<List<Manufacturers>> getAll();

    @Query("SELECT * FROM vehicles")
    Single<List<Vehicles>> getAllv();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inseretAll(List<Manufacturers>manufacturers);
}
