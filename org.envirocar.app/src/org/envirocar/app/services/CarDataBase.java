package org.envirocar.app.services;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.envirocar.core.dao.CarNewDAO;
import org.envirocar.core.entity.Manufacturers;
import org.envirocar.core.entity.PowerSource;
import org.envirocar.core.entity.Vehicles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Database(entities = {Manufacturers.class, PowerSource.class, Vehicles.class}, version = 1)
public abstract class CarDataBase extends RoomDatabase {
    private static CarDataBase instance;

    public abstract CarNewDAO carNewDAO();

    public static CarDataBase getDatabase(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context,CarDataBase.class,"Samew.db")
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executor executor = Executors.newSingleThreadExecutor();
                            executor.execute(()->{
                                AssetManager assetManager = context.getAssets();
        InputStream is = null;
        try {
            is = assetManager.open("databases/vehicles.csv");
//            Toast.makeText(context,"negi", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        StringTokenizer st = null;
        List<Manufacturers> manufacturers = new ArrayList<>();
        try {

            while ((line = reader.readLine()) != null) {
                st = new StringTokenizer(line, ",");
                st.nextToken();
                Manufacturers manufacturers1 = new Manufacturers();
                manufacturers1.setId(st.nextToken());
                manufacturers1.setName(st.nextToken());
                manufacturers.add(manufacturers1);
             //   Toast.makeText(context,"negi"+st.nextToken(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            //Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        instance.carNewDAO().inseretAll(manufacturers);
                            });
                        }
                    })
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

}
