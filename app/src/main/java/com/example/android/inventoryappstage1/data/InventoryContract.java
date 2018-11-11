package com.example.android.inventoryappstage1.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    //Empty constructor
    private InventoryContract() {
    }

    //Sets up the constant for the content authority which is the package name
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage1";

    //the content authority will be used to create the base of all the URI's which apps will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Setting up possible paths fort the content URI to take
    public static final String PATH_BOOKS = "Books";

    //Inner class for the table that is created
    public static final class InventoryEntry implements BaseColumns {

        //The content URI to access the table in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // The MIME type of the {@link #CONTENT_URI} for a list of books.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        // The MIME type of the {@link #CONTENT_URI} for a single BOOK.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //Name of the database table
        public static final String TABLE_NAME = "Books";

        //Unique ID number for the book (only for use in the database table).
        public final static String _ID = BaseColumns._ID;
        //Setting up constants for each heading
        public static final String COLUMN_PRODUCT_NAME = "Product_Name";
        public static final String COLUMN_PRICE = "Price";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "Supplier_Name";
        public static final String COLUMN_SUPPLIER_PHONE = "Supplier_Phone_Number";
    }
}
