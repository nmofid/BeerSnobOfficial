package byob.beersnob6;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by carissakane on 1/15/18.
 */

@Database(entities = {TempHumidity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TempHumidityDao tempDao();
}
