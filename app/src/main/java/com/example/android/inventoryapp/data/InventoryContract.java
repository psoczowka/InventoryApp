package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kiwi on 2017-07-22.
 */

public class InventoryContract {

    // Empty constructor o prevent from instantiating the contract class
    private InventoryContract() {}

    /**
     *  Name for content provider
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_GOODS = "goods";


    public static final class GoodsEntry implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GOODS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOODS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOODS;


        public static final String TABLE_NAME = "goods";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_GOODS_NAME ="name";

        public final static String COLUMN_GOODS_QUANTITY ="quantity";

        public final static String COLUMN_GOODS_PRICE ="price";

        public final static String COLUMN_GOODS_SUPPLIER ="supplier";

        public final static String COLUMN_GOODS_IMAGE ="image";
    }
}