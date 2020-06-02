package org.envirocar.app.services;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.envirocar.core.dao.CarNewDAO;
import org.envirocar.core.entity.Manufacturers;
import org.envirocar.core.entity.PowerSource;
import org.envirocar.core.entity.Vehicles;

@Database(entities = {Manufacturers.class, PowerSource.class, Vehicles.class}, version = 1)
public abstract class CarDataBase extends RoomDatabase {
    private static CarDataBase instance;

    public static CarDataBase getDatabase(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context,CarDataBase.class,"Sample.db")
                    .createFromAsset("databases/test2.db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
    public abstract CarNewDAO carNewDAO();
}
