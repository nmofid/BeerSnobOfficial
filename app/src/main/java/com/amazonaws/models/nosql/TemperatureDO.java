package com.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "iotminifridge-mobilehub-456530050-Temperature")

public class TemperatureDO {
    private String _time;
    private String _temp;

    @DynamoDBHashKey(attributeName = "Time")
    @DynamoDBAttribute(attributeName = "Time")
    public String getTime() {
        return _time;
    }

    public void setTime(final String _time) {
        this._time = _time;
    }
    @DynamoDBRangeKey(attributeName = "Temp")
    @DynamoDBAttribute(attributeName = "Temp")
    public String getTemp() {
        return _temp;
    }

    public void setTemp(final String _temp) {
        this._temp = _temp;
    }

}
