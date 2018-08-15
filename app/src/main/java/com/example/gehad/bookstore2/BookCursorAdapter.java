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

package com.example.gehad.bookstore2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/* BookEntry Inner Class imported to be used directly instead of saying every time "BookContract.BookEntry",
 * so it will be eliminated to"BookEntry" directly.
 */
import com.example.gehad.bookstore2.data.BookContract.BookEntry;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in book_list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the book list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.bookNameItem);
        TextView priceTextView = view.findViewById(R.id.bookPriceItem);
        final TextView quantityTextView = view.findViewById(R.id.bookQtyItem);

        // Find Sale Button View
        Button saleButton = view.findViewById(R.id.bookSaleItem);

        // Find the columns of book attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        // Read the book attributes from the Cursor for the current book
        final int bookID = cursor.getInt(idColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        float bookPrice = cursor.getFloat(priceColumnIndex);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current book
        nameTextView.setText(bookName);
        priceTextView.setText(view.getContext().getString(R.string.bookPrice_TextView,
                String.valueOf(bookPrice)));
        quantityTextView.setText(view.getContext().getString(R.string.bookQty_TextView,
                String.valueOf(bookQuantity)));

        //OnClickListener for Sale Button, which will decrease the Qty by 1, when pressed, if Qty > 0
        saleButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(bookQuantity > 0){
                    int saledBookQty = bookQuantity - 1;

                    Uri mUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookID);
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, saledBookQty);
                    int updatedRows = v.getContext().getContentResolver().update(mUri, values,
                            (BookEntry._ID + "=?"), new String[]{String.valueOf(bookID)});

                    if( updatedRows == 0) {
                        Toast.makeText(v.getContext(), R.string.errorSale_Toast, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), R.string.successfulSale_Toast, Toast.LENGTH_SHORT).show();
                        quantityTextView.setText(view.getContext().getString(R.string.bookQty_TextView,
                                String.valueOf(bookQuantity)));
                    }
                }else {
                    Toast.makeText(v.getContext(), R.string.nothingToSell_Toast, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup the item click listener
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Form the content URI that represents the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link BookEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.gehad.bookstore2/Books/2"
                // if the book with ID 2 was clicked on.
                /*
                 * Since ListView item ID starts from 0, and SQL database ID starts from 1,
                 * So, we will add 1 on the item position to match the ID in the database.
                 */
                int id = cursor.getPosition() + 1;
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                Log.v( "ID", Integer.toString(id) );

                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(view.getContext(), EditorActivity.class);

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                // Launch the {@link EditorActivity} to display the data for the current book.
                view.getContext().startActivity(intent);
            }
        });
    }
}
