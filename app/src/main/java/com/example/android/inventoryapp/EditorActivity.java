package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.GoodsEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int GOODS_LOADER_ID = 0;
    /**
     * Identifier for image data loader
     */
    public static final int IMAGE_GALLERY_REQUEST = 10;
    /**
     * Identifier for image URI loader
     */
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";

    /** Bitmap value of the image fetched from the Uri */
    private Bitmap imageValue;
    /** Image Path of the record fetched from the Uri*/
    private String imagePath;

    final Context imgContext = this;

    /** image uri*/
    private Uri imageUri;

    private EditText mNameEditTxt;
    private EditText mPriceEditTxt;
    private EditText mQuantityEditTxt;
    private EditText mSupplierEditTxt;
    private ImageView mImage;

    private Button mMinusButton;
    private Button mPlusButton;
    private Button mOrderButton;
    private Button mAddImage;

    // Content Uri for existing data (null if new)
    private Uri mCurrentGoodsUri;

    // Keeps track of whether the product has been edited or not
    private boolean mGoodsChanged = false;
    // If touched change mGoodsCHanged to true
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGoodsChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get data from intent which launched that activity
        Intent intent = getIntent();
        mCurrentGoodsUri = intent.getData();

        // Find views and set onTouchListeners on them
        mNameEditTxt = (EditText) findViewById(R.id.edit_name);
        mPriceEditTxt = (EditText) findViewById(R.id.edit_price);
        mSupplierEditTxt = (EditText) findViewById(R.id.edit_email);
        mQuantityEditTxt = (EditText) findViewById(R.id.edit_quantity);
        mImage = (ImageView) findViewById(R.id.image_editor);

        mMinusButton = (Button) findViewById(R.id.minus_button);
        mPlusButton = (Button) findViewById(R.id.plus_button);
        mOrderButton = (Button) findViewById(R.id.order_button);
        mAddImage = (Button) findViewById(R.id.add_image_button);

        // On touch listeners for checking if user changed something
        mNameEditTxt.setOnTouchListener(mTouchListener);
        mPriceEditTxt.setOnTouchListener(mTouchListener);
        mSupplierEditTxt.setOnTouchListener(mTouchListener);
        mQuantityEditTxt.setOnTouchListener(mTouchListener);
        mAddImage.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);

        if (mCurrentGoodsUri == null) {
            // New product
            setTitle(getString(R.string.new_editor_activity_title));

            // Hide delete from menu
            invalidateOptionsMenu();

            // Set quantity to 0
            mQuantityEditTxt.setText("0");

            // Hide order button
            mOrderButton.setVisibility(View.GONE);
        } else {
            // Editing product
            setTitle(getString(R.string.editor_activity_title));

            // Initialize loader to read data form db
            getLoaderManager().initLoader(GOODS_LOADER_ID, null, this);
        }

        // Decrease quantity button
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayedQuantity = mQuantityEditTxt.getText().toString().trim();
                int quant = Integer.parseInt(displayedQuantity);

                if(quant > 0) {
                    quant --;
                    mQuantityEditTxt.setText(Integer.toString(quant));
                } else {
                    Toast.makeText(EditorActivity.this, R.string.quantity0, Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Increase quantity button
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayedQuantity = mQuantityEditTxt.getText().toString().trim();
                int quant = Integer.parseInt(displayedQuantity);

                if(quant < 2147483647) {
                    quant ++;
                    mQuantityEditTxt.setText(Integer.toString(quant));
                } else {
                    Toast.makeText(EditorActivity.this, R.string.quantity_higher, Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Order from supplier button
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SENDTO);

                // Read product name and supplier email
                String contact = mSupplierEditTxt.getText().toString().trim();
                String product = mNameEditTxt.getText().toString().trim();
                String subject = getString(R.string.order) + product;
                String msg = getString(R.string.order_message);

                // Prepare message
                i.setData(Uri.parse("mailto:" + contact));
                i.putExtra(Intent.EXTRA_SUBJECT, subject);
                i.putExtra(Intent.EXTRA_TEXT, msg);

                // Send intent
                startActivity(i);
                // Leave activity
                finish();
            }
        });

        // Browse for image
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent;
                // Invoke an implicit intent to open the photo gallery
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                //Get path to image directory
                String pictureDirectoryPath = pictureDirectory.getPath();

                Uri data = Uri.parse(pictureDirectoryPath);
                intent.setDataAndType(data, "image/*");

                startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mGoodsChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (imageUri != null)
            outState.putString(STATE_IMAGE_URI, imageUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            imageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = mImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImage.setImageBitmap(getBitmapFromUri(imageUri, mImage, imgContext ));
                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // If getting image from gallery was successful
        if (requestCode == IMAGE_GALLERY_REQUEST && (resultCode == RESULT_OK)) {
            try {
                imageUri = data.getData();
                int takeFlags = data.getFlags();
                // Persist permissions for using image
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                imagePath = imageUri.toString();

                // Declare InputStream and pass uri to content resolver
                InputStream inputStream;
                inputStream = getContentResolver().openInputStream(imageUri);
                imageValue = BitmapFactory.decodeStream(inputStream);
                // display image
                mImage.setImageBitmap(imageValue);
                imagePath = imageUri.toString();
                try {
                    // Check for the freshest data.
                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                mImage.setImageBitmap(getBitmapFromUri(imageUri, mImage, imgContext));

            } catch (Exception e) {
                e.printStackTrace();
                // if loading image failed display message with toast
                Toast.makeText(EditorActivity.this, R.string.load_image_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri, ImageView imageView, Context imgContext) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e("EditorActivity", "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e("EditorActivity", "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate proper menu
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentGoodsUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:

                saveProduct();

                return true;

            case R.id.action_delete:

                showDeleteConfirmationDialog();

                return true;

            case android.R.id.home:

                // If product hasnt changed (onTouchListener) navigate to parent activity
                if (!mGoodsChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveProduct() {

        // Read from input fields
        String nameString = mNameEditTxt.getText().toString().trim();
        String priceString = mPriceEditTxt.getText().toString().trim();
        String quantityString = mQuantityEditTxt.getText().toString().trim();
        String supplierString = mSupplierEditTxt.getText().toString().trim();

        if ((!TextUtils.isEmpty(nameString))
                && (!TextUtils.isEmpty(priceString))
                && (!TextUtils.isEmpty(quantityString))
                && (!TextUtils.isEmpty(supplierString))) {

            if (imageUri == null) {
                return;
            }
            imagePath = imageUri.toString();

            // return if nothing was changed
            if (mCurrentGoodsUri == null && TextUtils.isEmpty(nameString)
                    && TextUtils.isEmpty(priceString)
                    && TextUtils.isEmpty(quantityString)
                    && TextUtils.isEmpty(supplierString)) {

                return;
            }

            // Create ContentValue object
            ContentValues values = new ContentValues();
            values.put(GoodsEntry.COLUMN_GOODS_NAME, nameString);
            values.put(GoodsEntry.COLUMN_GOODS_SUPPLIER, supplierString);
            values.put(GoodsEntry.COLUMN_GOODS_IMAGE, imagePath);

            int quantity = Integer.parseInt(quantityString);
            values.put(GoodsEntry.COLUMN_GOODS_QUANTITY, quantity);

            float price = Float.parseFloat(priceString);
            values.put(GoodsEntry.COLUMN_GOODS_PRICE, price);

            // Check if this is new or existing product
            if (mCurrentGoodsUri == null) {
                // new product
                Uri newUri = getContentResolver().insert(GoodsEntry.CONTENT_URI, values);

                if (newUri == null) {
                    Log.e("New product", " save product error");

                } else {

                    Toast.makeText(this,(getString(R.string.saved_product)),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                // existing product
                int rowsAffected = getContentResolver().update(mCurrentGoodsUri, values, null, null);

                if (rowsAffected == 0) {
                    Log.e("Error with updating", "");
                } else {
                    Log.e("update ", "successful");

                    Toast.makeText(this, "Update successful",
                            Toast.LENGTH_SHORT).show();

                }
            }

            // Leave activity only if all fields are filled
            finish();
        } else {
            // Stay in activity and inform user that all inputs have to be filed
            if ((TextUtils.isEmpty(nameString)) || (TextUtils.isEmpty(priceString))
                    || (TextUtils.isEmpty(supplierString)) || (TextUtils.isEmpty(quantityString))
                    || (TextUtils.isEmpty(imagePath))){
                Toast.makeText(getApplicationContext(), "Please fill all empty fields and add image", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.del_product_q);
        builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        if(mCurrentGoodsUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentGoodsUri, null, null);

            if(rowsDeleted == 0) {
                Toast.makeText(this, R.string.error_deleting, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // SELECT for loading data from db
        String[] projection = {
                GoodsEntry._ID,
                GoodsEntry.COLUMN_GOODS_NAME,
                GoodsEntry.COLUMN_GOODS_PRICE,
                GoodsEntry.COLUMN_GOODS_QUANTITY,
                GoodsEntry.COLUMN_GOODS_IMAGE,
                GoodsEntry.COLUMN_GOODS_SUPPLIER };

        return new CursorLoader(this, mCurrentGoodsUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1 ) {
            return;
        }

        //
        ViewTreeObserver viewTreeObserver = mImage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mImage.setImageBitmap(getBitmapFromUri(imageUri,mImage, imgContext));
            }
        });

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_NAME);
            int priceColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_SUPPLIER);
            int imageColumnIndex = cursor.getColumnIndex(GoodsEntry.COLUMN_GOODS_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            final String img = cursor.getString(imageColumnIndex);

            // update views with data
            mNameEditTxt.setText(name);
            mSupplierEditTxt.setText(supplier);
            mQuantityEditTxt.setText(Integer.toString(quantity));
            mPriceEditTxt.setText(Float.toString(price));
            imageUri = Uri.parse(img);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // reset views
        mNameEditTxt.setText("");
        mQuantityEditTxt.setText("0");
        mSupplierEditTxt.setText("");
        mPriceEditTxt.setText("0");
    }

}