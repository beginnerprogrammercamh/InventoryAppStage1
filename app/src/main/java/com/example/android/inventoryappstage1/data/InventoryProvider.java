package com.example.android.inventoryappstage1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class InventoryProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    // URI matcher code for the content URI for the Books table
    private static final int BOOKS = 100;
    // URI matcher code for the content URI for a single book in the books table
    private static final int BOOK_ID = 101;
    // UriMatcher object to match a content URI to a corresponding code.
    // The input passed into the constructor represents the code to return for the root URI.
    // It's common to use NO_MATCH as the input for this case.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    //Database helper object.
    private InventoryDbHelper mInventoryDbHelper;

    @Override
    public boolean onCreate() {
        // Create and initialize a InventoryDbHelper object to gain access to the inventory database.
        mInventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mInventoryDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // For the BOOKS code, query the books table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the books table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the books table where the _id equals the number to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor
        return cursor;
    }

    // Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS: //ONLY this one is allowed. We don't want BOOKS_ID because to insert we need whole table
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    //Insert method to be called in the above switch case BOOKS.
    //Inserts a new book and returns new CONTENT URI for the specific row
    private Uri insertBook(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("The book needs a name.");
        }
        //Check that the price is valid and not null
        Float price = values.getAsFloat(InventoryContract.InventoryEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Price must be entered.");
        } else if (price != null && price < 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }

        //Check if the quantity is valid and not null
        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity must be entered.");
        } else if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantity can not be negative.");
        }

        // Check that the supplier name is not null
        String supp_name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supp_name == null) {
            throw new IllegalArgumentException("The supplier needs a name.");
        }

        // Check that the supplier phone number is not null
        String supp_phone = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);
        if (supp_phone == null) {
            throw new IllegalArgumentException("The supplier phone number needs to be added.");
        }

        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase(); //Needs to be writeable this time

        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        //Return with the new ID of the row
        return ContentUris.withAppendedId(uri, id);
    }

    // Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // Update books in the database with the given content values.
    // Return the number of rows that were successfully updated.
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //If the COLUMN_PRODUCT_NAME key is present, check it is not null
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("The book needs a name.");
            }
        }
        //If the COLUMN_PRICE key is present, check that the price is valid and not null
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRICE)) {
            Float price = values.getAsFloat(InventoryContract.InventoryEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Price must be entered.");
            } else if (price != null && price < 0) {
                throw new IllegalArgumentException("Price must be greater than 0.");
            }
        }

        //If COLUMN_QUANTITY key is present, check if the quantity is valid and not null
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Quantity must be entered.");
            } else if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Quantity can not be negative.");
            }
        }

        // If the COLUMN_SUPPLIER_NAME key is present, check that the supplier name is not null
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME)) {
            String supp_name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supp_name == null) {
                throw new IllegalArgumentException("The supplier needs a name.");
            }
        }

        // If the COLUMN_SUPPLIER_PHONE key is present, check that the supplier phone number is not null
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE)) {
            String supp_phone = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);
            if (supp_phone == null) {
                throw new IllegalArgumentException("The supplier phone number needs to be added.");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }


    // Delete the data at the given selection and selection arguments
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    // Returns the MIME type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}