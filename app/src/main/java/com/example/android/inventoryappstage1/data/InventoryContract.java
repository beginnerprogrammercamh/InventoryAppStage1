package com.example.android.inventoryappstage1.data;
import android.provider.BaseColumns;

public final class InventoryContract {

    private InventoryContract() {}

    //Inner class for the table that is created
    public static final class InventoryEntry implements BaseColumns {

        //Name of the database table
        public static final String TABLE_NAME = "Books";

        //Setting up constants for each heading
        public static final String COLUMN_PRODUCT_NAME = "Product_Name";
        public static final String COLUMN_PRICE = "Price";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "Supplier_Name";
        public static final String COLUMN_SUPPLIER_PHONE = "Supplier_Phone_Number";
    }
}
