/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 *    Author: Gehad Ahmed
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.gehad.bookstore2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Books app.
 */
/* final class won't be extended or implemented again by another classes, as it contains
 * constants for our database
 */
public final class BookContract {

    /* To prevent someone from accidentally instantiating the contract class,
     * give it an empty private constructor.
     */
    private BookContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.gehad.bookstore2";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.gehad.bookstore2/Books/ is a valid path for
     * looking at book data. content://com.example.gehad.bookstore2/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_BOOKS = "Books";

    /**
     * Inner class that defines constant values for the books database table.
     * Each entry in the table represents a single book.
     */
    public static final class BookEntry implements BaseColumns {

        /** The content URI to access the book data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of books.
         * CURSOR_DIR_BASE_TYPE = "vnd.android.cursor.dir" as constant String value
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single book.
         * CURSOR_ITEM_BASE_TYPE = "vnd.android.cursor.item" as constant String value
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /** Name of database table for books */
        public final static String TABLE_NAME = "Books";

        /**
         * BaseColumns._ID = "_id" is a constant String value.
         * Unique ID number for the book (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the book.
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_NAME = "Book_Name";

        /**
         * Price of the book.
         * Type: REAL
         */
        public final static String COLUMN_BOOK_PRICE = "Price_USD";

        /**
         * Quantity of the book.
         * Type: INTEGER
         */
        public final static String COLUMN_BOOK_QUANTITY = "Quantity";

        /**
         * Supplier Name of the book.
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_SUPP_NAME = "Supplier_Name";

        /**
         * Supplier Phone Number of the book.
         * Type: TEXT
         */
        public final static String COLUMN_BOOK_SUPP_NUM = "Supplier_Phone_Number";
    }
}