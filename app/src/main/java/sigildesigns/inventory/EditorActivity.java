package sigildesigns.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.ByteArrayOutputStream;

import sigildesigns.inventory.data.ItemContract;

/**
 * Allows user to create a new item for the inventory or to modify an existing one.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // Identifier for the item data loader
    private static final int EXISTING_ITEM_LOADER = 0;

    // Content URI for the existing item (null if its a new item)
    private Uri mCurrentItemUri;

    // EditText field to enter the item's name
    private EditText mNameEditText;

    // EditText field to enter the item's price
    private EditText mPriceEditText;

    // EditText field to enter the item's description
    private EditText mDescriptionEditText;

    // EditText field to enter the item's quantity
    private EditText mQtyEditText;

    // ImageView field to display the item's picture
    private ImageView mItemImage;

    // EditText field to enter item's contact number
    private EditText mContactNumber;

    // Boolean flag that keeps track of whether the item has been edited (true) or not (false)
    private boolean mItemHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mItemHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.editor_toolbar);
        setSupportActionBar(toolbar);

        // Examine the intent that was used to launch this activity, in order to figure out if we're
        // creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain an item content URI, then we know that we are
        // creating a new item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say "Add an Item"
            setTitle(R.string.add_new_item);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(R.string.edit_item);

            // Initialize a loader to read the item data from the database and display
            // the current values in the editor.
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.editor_view_name_et);
        mDescriptionEditText = (EditText) findViewById(R.id.editor_view_description_et);
        mPriceEditText = (EditText) findViewById(R.id.editor_view_price_et);
        mQtyEditText = (EditText) findViewById(R.id.editor_view_qty_et);
        mItemImage = (ImageView) findViewById(R.id.editor_view_image);
        mContactNumber = (EditText) findViewById(R.id.editor_view_contactnumber_et);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user has
        // touched or modified them. This will let us know if there are unsaved changes or not,
        // if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQtyEditText.setOnTouchListener(mTouchListener);
        mContactNumber.setOnTouchListener(mTouchListener);

        // Set up click listener for imageview to take a picture
        mItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }

    // Get user input from editor and save item into database
    private void saveItem() {
        // Read from input fields, use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String qtyString = mQtyEditText.getText().toString().trim();
        String contactNumberString = mContactNumber.getText().toString().trim();

        // If the price is not provided by the user, don't try to parse the string into a float,
        // instead use 0 as default.
        float priceFloat = 0;
        if (!TextUtils.isEmpty(priceString)) {
            priceFloat = Float.valueOf(priceString);
        }

        // If the qty is not provided by the user, don't try to parse the string into an integer,
        // instead use 0 as a default.
        int qtyInt = 0;
        if (!TextUtils.isEmpty(qtyString)) {
            qtyInt = Integer.valueOf(qtyString);
        }

        byte[] imageByte = convertImage();


        // Check if this is supposed to be a new item and check if fields are blank.
        if (mCurrentItemUri == null && TextUtils.isEmpty(nameString) || TextUtils.isEmpty
                (descriptionString)
                || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(qtyString)) {
            // Since no fields were modified, we can return early without creating a new item.
            Toast.makeText(this, R.string.item_not_saved_need_more_input, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys, and item attributes
        // values.
        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DESCRIPTION, descriptionString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PICTURE, imageByte);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, priceFloat);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QTY, qtyInt);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_CONTACTNUMBER, contactNumberString);

        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a new item, so insert a new item into the provider, returning the content URI
            // for the new item.
            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.error_saving_item, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an existing item, so update the item with content URI:
            // mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selectionArgs
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.error_saving_item, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise the item update was successful and we can display a toast.
                Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu

        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option.
            case R.id.action_save:
                // Save item to database
                saveItem();
                // Exit activity
                finish();
                return true;
            case R.id.action_cancel:
                // Show a dialog that notifies the user they have unsaved changes.
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // This method is called when the back button is pressed.


    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private byte[] convertImage() {
        Bitmap picture = ((BitmapDrawable) mItemImage.getDrawable()).getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bArray = bos.toByteArray();
        return bArray;
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains all
        // columns from the item table
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_QTY,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_DESCRIPTION,
                ItemContract.ItemEntry.COLUMN_ITEM_PICTURE,
                ItemContract.ItemEntry.COLUMN_ITEM_CONTACTNUMBER};
        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,        // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int qtyColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QTY);
            int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
            int descriptionColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry
                    .COLUMN_ITEM_DESCRIPTION);
            int pictureColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry
                    .COLUMN_ITEM_PICTURE);
            int contactColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry
                    .COLUMN_ITEM_CONTACTNUMBER);

            // Extract out the value from the Cursor for the given column index.
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(qtyColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            byte[] bb = cursor.getBlob(pictureColumnIndex);
            Bitmap bmp = BitmapFactory.decodeByteArray(bb, 0, bb.length);
            mItemImage.setImageBitmap(bmp);
            String contactNumber = cursor.getString(contactColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQtyEditText.setText(String.valueOf(quantity));
            mPriceEditText.setText(String.valueOf(price));
            mDescriptionEditText.setText(description);
            mContactNumber.setText(contactNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQtyEditText.setText("");
        mPriceEditText.setText("");
        mDescriptionEditText.setText("");
        mItemImage.setImageResource(R.drawable.placeholder);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost if they
     * continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when the user confirms
     *                                   they want to discard their changes.
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners for the
        // positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_discard_changes);
        builder.setPositiveButton(R.string.alert_dialog_positive_button,
                discardButtonClickListener);
        builder.setNegativeButton(R.string.alert_dialog_negative_button, new DialogInterface
                .OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog and continue
                // editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mItemImage.setImageBitmap(imageBitmap);
        }
    }
}
