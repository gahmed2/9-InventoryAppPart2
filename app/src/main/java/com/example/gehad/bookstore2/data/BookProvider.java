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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/* BookEntry Inner Class imported to be used directly instead of saying every time "BookContract.BookEntry",
 * so it will be eliminated to"BookEntry" directly.
 */
import com.example.gehad.bookstore2.data.BookContract.BookEntry;

/**
 * {@link ContentProvider} for Book Store app.
 */
public class BookProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the Books table */
    private static final int BOOKS = 100;

    /** URI matcher code for the content URI for a single book in the Books table */
    private static final int BOOK_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize. All paths added to the UriMatcher have a corresponding code to return
         * when a match is found.
         */
        /* The content URI of the form "content://com.example.gehad.bookstore2/Books/" will map to the
         * integer code {@link #BOOKS}. This URI is used to provide access to MULTIPLE rows
         * of the Books table.
         */
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);

        /*
         * The content URI of the form "content://com.example.gehad.bookstore2/Books/#" will map to the
         * integer code {@link #BOOK_ID}. This URI is used to provide access to ONE single row
         * of the Books table.
         */
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        /*
         * For example, "content://com.example.gehad.bookstore2/Books/3" matches, but
         * "content://com.example.gehad.bookstore2/Books/" (without a number at the end) doesn't match.
         */
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /** Database helper object */
    private BookDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case BOOKS:
                /*
                 * For the BOOKS code, query the Books table directly with the given
                 * projection, selection, selection arguments, and sort order. The cursor
                 * could contain multiple rows of the Books table.
                 */
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                /*
                 * For the BOOK_ID code, extract out the ID from the URI.
                 * For an example URI such as "content://com.example.gehad.bookstore2/Books/3",
                 * the selection will be "_id=?" and the selection argument will be a
                 * String array containing the actual ID of 3 in this case.
                 */
                /*
                 * For every "?" in the selection, we need to have an element in the selection
                 * arguments that will fill in the "?". Since we have 1 question mark in the
                 * selection, we have 1 String in the selection arguments' String array.
                 */
                 //We use "=?" in the query statement to avoid SQL Data Injection into our database.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the Books table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }

        /*
         * Set notification URI on the Cursor,
         * so we know what content URI the Cursor was created for.
         * If the data at this URI changes, then we know we need to update the Cursor.
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book requires a name");
        }

        // Check that the price is not null and greater than 0 USD
        Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
        if ((price == null) || (price != null && price <= 0)) {
            throw new IllegalArgumentException("Book requires valid price");
        }

        // Check that the Qty is not null and greater than or equal to 0
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if ((quantity == null) || (quantity != null && quantity < 0)) {
            throw new IllegalArgumentException("Book requires valid quantity");
        }

        // Check that the supplier name is not null
        String supplierName = values.getAsString(BookEntry.COLUMN_BOOK_SUPP_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Book requires a supplier name");
        }

        // Check that the supplier phone number is not null
        String supplierPhone = values.getAsString(BookEntry.COLUMN_BOOK_SUPP_NUM);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Book requires a supplier phone number");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the book content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                /*
                 * For the BOOK_ID code, extract out the ID from the URI,
                 * so we know which row to update. Selection will be "_id=?" and selection
                 * arguments will be a String array containing the actual ID.
                 */
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update Books in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more Books).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link BookEntry#COLUMN_BOOK_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            // Check that the price is not null and greater than 0 USD
            Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
            if ((price == null) || (price != null && price <= 0)) {
                throw new IllegalArgumentException("Book requires valid price");
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_QUANTITY} key is present,
        // check that the Qty value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            // Check that the Qty is not null and greater than or equal to 0
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if ((quantity == null) || (quantity != null && quantity < 0)) {
                throw new IllegalArgumentException("Book requires valid quantity");
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_SUPP_NAME} key is present,
        // check that the supplier name value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPP_NAME)) {
            // Check that the supplier name is not null
            String supplierName = values.getAsString(BookEntry.COLUMN_BOOK_SUPP_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Book requires a supplier name");
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_SUPP_NUM} key is present,
        // check that the supplier phone number value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPP_NUM)) {
            // Check that the supplier phone number is not null
            String supplierPhone = values.getAsString(BookEntry.COLUMN_BOOK_SUPP_NUM);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Book requires a supplier phone number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
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

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
