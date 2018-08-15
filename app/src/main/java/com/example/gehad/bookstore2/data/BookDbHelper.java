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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* BookEntry Inner Class imported to be used directly instead of saying every time "BookContract.BookEntry",
 * so it will be eliminated to"BookEntry" directly.
 */
import com.example.gehad.bookstore2.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    public final static String LOG_TAG = BookDbHelper.class.getSimpleName();

    /** Name of the database file */
    public final static String DATABASE_NAME = "BookStore.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    public final static int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link BookDbHelper}.
     *
     * @param context of the app
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        /*
         Create a String that contains the SQL statement to create the books table
         * All fields are mandatory.
         */
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_PRICE + " REAL NOT NULL, "
                + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_BOOK_SUPP_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_SUPP_NUM + " TEXT NOT NULL"
                + ");";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // The database is still at version 1, so there's nothing to be done here.
    }
}

