package byob.beersnob6;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

/**
 * Created by carissakane on 1/15/18.
 */

@Dao
public interface TempHumidityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertInfo(TempHumidity info);

    @Update
    public int updateInfo(TempHumidity info);

    @Delete
    public void deleteInfo(TempHumidity info);

    @Query("SELECT * FROM tempHumidity")
    public TempHumidity[] getAllInfo();
}
