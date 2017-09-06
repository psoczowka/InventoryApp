package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.GoodsEntry;

import static android.R.attr.id;

/**
 * Created by kiwi on 2017-07-23.
 */

public class GoodsProvider extends ContentProvider {

    /** URI matcher code for the content URI for the goods table */
    private static final int GOODS = 100;

    /** URI matcher code for the content URI for a single item in the goods table */
    private static final int GOODS_ID = 101;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        // URI for whole table
        mUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_GOODS, GOODS);

        // URI for single row, where "#" stands for an integer
        mUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_GOODS + "/#", GOODS_ID);
    }


    /** Database helper object */
    private GoodsDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new GoodsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // db read mode
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = mUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                cursor = database.query(GoodsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case GOODS_ID:
                selection = GoodsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(GoodsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("cannot query uri " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                return  GoodsEntry.CONTENT_LIST_TYPE;

            case GOODS_ID:
                return GoodsEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalStateException("unknown uri " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = mUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                return insertGoods(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertGoods(Uri uri, ContentValues values) {

        //SANITY CHECKS
        // Check that the name is not null
        String name = values.getAsString(GoodsEntry.COLUMN_GOODS_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String supplier = values.getAsString(GoodsEntry.COLUMN_GOODS_SUPPLIER);
        if (supplier == null ) {
            throw new IllegalArgumentException("Product requires a supplier");
        }

        String price = values.getAsString(GoodsEntry.COLUMN_GOODS_PRICE);
        if (price == null ) {
            throw new IllegalArgumentException("Product requires a price");
        }

        String quantity = values.getAsString(GoodsEntry.COLUMN_GOODS_QUANTITY);
        if (quantity == null ) {
            throw new IllegalArgumentException("Product requires quantity");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(GoodsEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e("Goods Provider ", "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the goods content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = mUriMatcher.match(uri);
        switch (match) {
            case GOODS:
                rowsDeleted = database.delete(GoodsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case GOODS_ID:
                selection = GoodsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(GoodsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("deletion not suported for " + uri);

        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = mUriMatcher.match(uri);
        switch (match) {

            case GOODS:
                return updateGoods(uri, contentValues, selection, selectionArgs);

            case GOODS_ID:
                selection = GoodsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                return updateGoods(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateGoods(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(GoodsEntry.COLUMN_GOODS_NAME)) {
            String name = values.getAsString(GoodsEntry.COLUMN_GOODS_NAME);
            if (name == null) {

                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(GoodsEntry.COLUMN_GOODS_SUPPLIER)) {
            String name = values.getAsString(GoodsEntry.COLUMN_GOODS_SUPPLIER);
            if (name == null) {

                throw new IllegalArgumentException("Product requires a supplier");
            }
        }

        if (values.containsKey(GoodsEntry.COLUMN_GOODS_QUANTITY)) {
            String name = values.getAsString(GoodsEntry.COLUMN_GOODS_QUANTITY);
            if (name == null) {

                throw new IllegalArgumentException("Product requires quantity");
            }
        }

        if (values.containsKey(GoodsEntry.COLUMN_GOODS_PRICE)) {
            String name = values.getAsString(GoodsEntry.COLUMN_GOODS_PRICE);
            if (name == null) {

                throw new IllegalArgumentException("Product requires price");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(GoodsEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}