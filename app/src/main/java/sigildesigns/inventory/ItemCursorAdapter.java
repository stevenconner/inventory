package sigildesigns.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import sigildesigns.inventory.data.ItemContract;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view that uses a
 * {@link android.database.Cursor} of item data as its data source. This adapter knows how to create
 * list items for each row of item data in the {@link android.database.Cursor}.
 */

public class ItemCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context the context
     * @param cursor  the cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.content_catalog, viewGroup, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvQty = (TextView) view.findViewById(R.id.quantity);
        TextView tvPrice = (TextView) view.findViewById(R.id.price);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry
                .COLUMN_ITEM_NAME));
        int qty = cursor.getInt(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry
                .COLUMN_ITEM_QTY));
        // Convert qty to a string
        String stringQty = String.valueOf(qty);
        float price = cursor.getFloat(cursor.getColumnIndexOrThrow(ItemContract.ItemEntry
                .COLUMN_ITEM_PRICE));
        // Convert price to a string
        String stringPrice = String.valueOf(price);
        // Populate fields with extracted properties
        tvName.setText(name);
        tvQty.setText(stringQty);
        tvPrice.setText(stringPrice);
    }
}
