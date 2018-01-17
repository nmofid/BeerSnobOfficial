package byob.beersnob6;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by carissakane on 1/15/18.
 */

@Entity
public class TempHumidity {
    @PrimaryKey
    @NonNull
    public String dateTime;

    @ColumnInfo(name = "temperature")
    public double temp;

    @ColumnInfo(name = "humidity")
    public double humid;

}
