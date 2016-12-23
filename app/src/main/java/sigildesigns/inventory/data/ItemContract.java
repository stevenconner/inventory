package sigildesigns.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */

public class ItemContract {
    // To prevent someone from accidentally instantiating the contract class, give it an empty
    // constructor.
    private ItemContract() {
    }

    // Content Authority for the app
    public static final String CONTENT_AUTHORITY = "sigildesigns.inventory";

    // Use CONTENT_AUTHORITY to create the base of all URIs which apps will use to contact the
    // content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible path (appended to base content URI for possible URIs)
    public static final String PATH_ITEMS = "items";

    // Inner class that defines constant values for the items database table.
    // Each entry in the table represents a single item.
    public static final class ItemEntry implements BaseColumns {
        // The content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        // The MIME type of the {@link #CONTENT_URI} for a list of items
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // The MIME type of the {@link #CONTENT_URI} for a single item
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // Name of the database table for items
        public static final String TABLE_NAME = "items";

        // Unique ID number for the item (only for use in the database table). Type: INTEGER
        public static final String _ID = BaseColumns._ID;

        // Name of the item. Type: TEXT
        public static final String COLUMN_ITEM_NAME = "name";

        // Description of the item. Type TEXT
        public static final String COLUMN_ITEM_DESCRIPTION = "description";

        // Price of the item. Type REAL.
        public static final String COLUMN_ITEM_PRICE = "price";

        // Quantity of the item. Type INTEGER.
        public static final String COLUMN_ITEM_QTY = "quantity";

        // Picture of the item. Type BLOB.
        public static final String COLUMN_ITEM_PICTURE = "picture";
    }
}
