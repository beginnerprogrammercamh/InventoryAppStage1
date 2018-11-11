package com.example.android.inventoryappstage1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage1.data.InventoryContract;

import java.text.NumberFormat;

//Sets up an adapter for a list or grid view that uses a {@link Cursor} of
// //inventory data as its data source. This adapter knows how to create list items
// for each row of inventory data in the {@link Cursor}.
public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    // Makes a new blank list item view. No data is set (or bound) to the views yet.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //This method binds the data (in the current row pointed to by cursor) to the given
    // list item layout.
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        final TextView quantityTextView = view.findViewById(R.id.quantity);
        final Button saleButton = view.findViewById(R.id.sale_button);
        final TextView saleTextView = view.findViewById(R.id.sale_view);

        // Find the columns of attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);

        // Read the attributes from the Cursor for the current item
        String bookName = cursor.getString(nameColumnIndex);
        Float bookPrice = cursor.getFloat(priceColumnIndex);
        Integer bookQuantity = cursor.getInt(quantityColumnIndex);
        int id = cursor.getInt(idColumnIndex);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(bookName);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        priceTextView.setText(formatter.format(bookPrice));
        if (bookQuantity > 0) {
            quantityTextView.setText(Integer.toString(bookQuantity));
        } else {
            quantityTextView.setText(context.getString(R.string.Out_of_Stock));
        }
        saleTextView.setText(Integer.toString(id));


        //Set OnClick Listener to the button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get current quantity from TextView
                int updateQuantity = Integer.parseInt(quantityTextView.getText().toString().trim());
                if (updateQuantity ==0) {
                    Toast.makeText(context, R.string.sale_fail, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    updateQuantity -= 1;
                    quantityTextView.setText(Integer.toString(updateQuantity));
                    //get id from view
                    long id_number = Integer.parseInt(saleTextView.getText().toString());
                    Uri bookSelected = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id_number);
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantityTextView.getText().toString());
                    //update database
                    int rowsAffected = context.getContentResolver().update(bookSelected, values, null, null);
                    //confirm whether quantity was updated or not
                    if (rowsAffected == 0) {
                        Toast.makeText(context, R.string.quantity_update_error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.quantity_update_success, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}