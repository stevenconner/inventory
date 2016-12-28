package sigildesigns.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static sigildesigns.inventory.data.ItemProvider.LOG_TAG;

/**
 * Helper methods to create the database.
 */

public class ItemDbHelper extends SQLiteOpenHelper {
    // Name of the database.
    private static final String DATABASE_NAME = "inventory.db";
    // Database version. If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // CREATE TABLE items (id INTEGER PRIMARY KEY, name TEXT NOT NULL, quantity INTEGER NOT NULL
        // DEFAULT 0, price REAL NOT NULL DEFAULT 0, description TEXT, picture BLOB);
        // Create a String that contains the SQL statement to create the items table.
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemContract.ItemEntry.TABLE_NAME + " ("
                + ItemContract.ItemEntry._ID + " INTEGER PRIMARY KEY, "
                + ItemContract.ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemContract.ItemEntry.COLUMN_ITEM_QTY + " INTEGER NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_DESCRIPTION + " TEXT, "
                + ItemContract.ItemEntry.COLUMN_ITEM_PICTURE + " BLOB, "
                + ItemContract.ItemEntry.COLUMN_ITEM_CONTACTNUMBER + " TEXT NOT NULL);";
        Log.v(LOG_TAG, SQL_CREATE_ITEMS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
