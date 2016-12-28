package sigildesigns.inventory;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import sigildesigns.inventory.data.ItemContract;

/**
 * Allows user to view the details of an item in the inventory.
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    // Identifier for the item data loader
    private static final int EXISTING_ITEM_LOADER = 0;

    // Content URI for the existing item
    private Uri mCurrentItemUri;

    private TextView mNameTextView;
    private TextView mPriceTextView;
    private TextView mDescriptionTextView;
    private TextView mQtyTextView;
    private ImageView mItemImage;
    private Button mOrderButton;
    private String contactNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);

        // Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mNameTextView = (TextView) findViewById(R.id.details_view_name_tv);
        mDescriptionTextView = (TextView) findViewById(R.id.details_view_description_tv);
        mPriceTextView = (TextView) findViewById(R.id.details_view_price_tv);
        mQtyTextView = (TextView) findViewById(R.id.details_view_qty_tv);
        mItemImage = (ImageView) findViewById(R.id.details_view_image);
        mOrderButton = (Button) findViewById(R.id.details_view_order_button);

        // Setup order button to call the number from the database
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactNumber));
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.details_action_edit:
                // Create a new intent to go to {@link EditorActivity}
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                // Set the URI on the data field of the intent
                intent.setData(mCurrentItemUri);
                // Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
                return true;
            case R.id.details_action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Prompt the user to confirm they want to delete this item.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message and click listeners for the positive
        // and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_delete_item_prompt);
        builder.setPositiveButton(R.string.alert_dialog_delete, new DialogInterface
                .OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface
                .OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog and continue editing.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Perform the deletion of the item in the database
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, R.string.error_deleting_item, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise the delete was successful and we can display a toast.
                Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the detail view shows all the item attributes, define a projection that contains
        // all columns from the item table.
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
        // Return early if hte cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
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
            contactNumber = cursor.getString(contactColumnIndex);


            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mQtyTextView.setText(String.valueOf(quantity));
            mPriceTextView.setText(String.valueOf(price));
            mDescriptionTextView.setText(description);
        }
    }

    private void callContact() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(contactNumber));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameTextView.setText("");
        mQtyTextView.setText("");
        mPriceTextView.setText("");
        mDescriptionTextView.setText("");
        mItemImage.setImageResource(R.drawable.placeholder);
    }
}
