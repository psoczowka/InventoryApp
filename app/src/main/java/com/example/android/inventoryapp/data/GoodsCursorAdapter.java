package com.example.android.inventoryapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.GoodsEntry;

import static android.R.attr.id;
import static android.R.attr.name;
import static com.example.android.inventoryapp.R.id.quantity;

/**
 * Created by kiwi on 2017-07-23.
 */

public class GoodsCursorAdapter extends CursorAdapter {

    private TextView mNameTextView;
    private TextView mPriceTextView;
    private TextView mCurrentQuantityTextView;

    private Button saleButton;

    /**
     * Constructs a new {@link GoodsCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public GoodsCursorAdapter(Context context, Cursor c)   {
        super(context, c, 0);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);

    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        mNameTextView = (TextView) view.findViewById(R.id.name);
        mPriceTextView = (TextView) view.findViewById(R.id.price);
        mCurrentQuantityTextView = (TextView) view.findViewById(R.id.quantity_value);

        int nameColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_NAME);
        int priceColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(GoodsEntry._ID);

        String goodsName = cursor.getString(nameColumnIndex);
        Float goodsPrice = cursor.getFloat(priceColumnIndex);
        final int goodsQuantity = cursor.getInt(quantityColumnIndex);
        final int goodsId = cursor.getInt(idColumnIndex);

        String price = goodsPrice.toString();
        String quantity = Integer.toString(goodsQuantity);

        mNameTextView.setText(goodsName);
        mPriceTextView.setText(price);
        mCurrentQuantityTextView.setText(quantity);

        // Sale button logic
        saleButton = (Button) view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (goodsQuantity > 0) {

                    int saleQuantity =  goodsQuantity - 1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(GoodsEntry.COLUMN_GOODS_QUANTITY, saleQuantity);

                    Uri goodsUri = ContentUris.withAppendedId(GoodsEntry.CONTENT_URI, goodsId);

                    int numRowsUpdated = context.getContentResolver().update(goodsUri, contentValues, null, null);

                    Log.e("Sale of products uri: ", "" + goodsUri + " rows updated " + numRowsUpdated);

                    if (numRowsUpdated == 0) {
                        Log.e("Error with updating", " sale button value");
                    }

                } else {
                    Toast.makeText(context, (R.string.quantity0), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}