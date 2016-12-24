package sigildesigns.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import sigildesigns.inventory.data.ItemContract;

public class CatalogActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER = 0;

    ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find the ListView which will be populated with item data
        ListView itemListView = (ListView) findViewById(R.id.list_view_items);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_title_text);
        itemListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row or item data in the Cursor.
        // There is no item data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        // Setup item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create a new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                /**
                 * Form the content URI that represents the specific item that was clicked on,
                 * by appending the "id" (passed as input to this method) onto the
                 * {@link ItemEntry#CONTENT_URI}.
                 * For example, the URI would be "content://sigildesigns.inventory/items/2" if the
                 * item with the ID 2 was clicked on.
                 */
                Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry
                        .CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);
                // Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // Helper method to insert hardcoded item data into the database. For debugging purposes only.
    private void insertItem() {
        // Create a ContentValues object where column names are the keys, and item attributes are
        // values.
        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, "Kaladesh Booster Pack");
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QTY, 10);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, 3.99);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_DESCRIPTION, "15 card booster pack for the " +
                "MTG set Kaladesh.");
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PICTURE, R.drawable.kaladeshboosters);

        // Insert the dummy data into the database
        Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);
    }

    // Helper method to delete all items in the database
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemContract.ItemEntry.CONTENT_URI, null,
                null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from item database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_QTY,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,               // Parent activity context
                ItemContract.ItemEntry.CONTENT_URI, // Provider content URI to query
                projection,                         // Columns to include in the resulting Cursor
                null,                               // No selection clause
                null,                               // No selection arguments
                null);                              // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link ItemCursorAdapter} with this new cursor containing updated item data
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
