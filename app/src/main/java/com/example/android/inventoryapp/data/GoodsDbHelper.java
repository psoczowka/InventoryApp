package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.GoodsEntry;

/**
 * Created by kiwi on 2017-07-22.
 */

public class GoodsDbHelper  extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;


    public GoodsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a String that contains the SQL statement to create table
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + GoodsEntry.TABLE_NAME + " ("
                + GoodsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GoodsEntry.COLUMN_GOODS_NAME + " TEXT NOT NULL, "
                + GoodsEntry.COLUMN_GOODS_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + GoodsEntry.COLUMN_GOODS_PRICE + " REAL NOT NULL DEFAULT 0, "
                + GoodsEntry.COLUMN_GOODS_IMAGE + " TEXT NOT NULL, "
                + GoodsEntry.COLUMN_GOODS_SUPPLIER + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}