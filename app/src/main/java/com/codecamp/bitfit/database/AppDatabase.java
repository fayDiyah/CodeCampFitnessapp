package com.codecamp.bitfit.database;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by androidcodecamp on 13.02.2018.
 */

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase extends BaseModel {

    public static final String NAME = "AppDatabase";

    public static final int VERSION = 2;
}