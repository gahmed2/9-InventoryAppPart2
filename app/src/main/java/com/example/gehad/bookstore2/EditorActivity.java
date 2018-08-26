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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gehad.bookstore2.data.BookContract.BookEntry;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the book data loader
    private static final int EXISTING_BOOK_LOADER = 0;

    // Content URI for the existing book (null if it's a new book)
    private Uri mCurrentBookUri;

    // EditText field to enter the book's name
    private EditText mNameEditText;

    // EditText field to enter the book's price
    private EditText mPriceEditText;

    // Decrease Quantity Button
    private Button mDecreaseQtyButton;

    // EditText field to enter the book's quantity
    private EditText mQuantityEditText;

    // Increase Quantity Button
    private Button mIncreaseQtyButton;

    // EditText field to enter the book's supplier name
    private EditText mSupplierEditText;

    // EditText field to enter the book's supplier phone
    private EditText mSuppPhoneEditText;

    // Order Button
    private Button mOrderBook;

    // Will be assigned True, if all Mandatory fields are filled
    private boolean isFilled;

    // Boolean flag that keeps track of whether the book has been edited (true) or not (false)
    private boolean mBookHasChanged = false;

    // Get Quantity value for decrement and increment buttons
    int Qty;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility") //To suppress override performClick warning on EditText fields
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        /*
         * Examine the intent that was used to launch this activity,
         * in order to figure out if we're creating a new book or editing an existing one.
         */
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        /*
         * If the intent DOES NOT contain a book content URI, then we know that we are
         * creating a new book.
         */
        if (mCurrentBookUri == null){
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.editor_activity_title_new_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        }else {
            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from.
        mNameEditText = findViewById(R.id.bookNameEdit);
        mPriceEditText = findViewById(R.id.bookPriceEdit);
        mDecreaseQtyButton = findViewById(R.id.decreaseQtyButton);
        mQuantityEditText = findViewById(R.id.bookQuantityEdit);
        mIncreaseQtyButton = findViewById(R.id.increaseQtyButton);
        mSupplierEditText = findViewById(R.id.bookSuppEdit);
        mSuppPhoneEditText = findViewById(R.id.bookSuppPhoneEdit);
        mOrderBook = findViewById(R.id.bookOrderButton);

        /*
         * Setup OnTouchListeners on all the input fields, so we can determine if the user
         * has touched or modified them. This will let us know if there are unsaved changes
         * or not, if the user tries to leave the editor without saving.
         */
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSuppPhoneEditText.setOnTouchListener(mTouchListener);

        mDecreaseQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Quantity value from EditText field
                // If the EditText field is blank (null), assign Qty to equal zero.
                if (TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())){
                    Qty = 0;
                } else{
                    Qty = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                }

                // Decrement Quantity by 1 if larger than 0
                if (Qty > 0){
                    Qty -= 1;
                    mQuantityEditText.setText(String.valueOf(Qty));
                } else {
                    Toast.makeText(view.getContext(), getString(R.string.decrement_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mIncreaseQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Quantity value from EditText field
                // If the EditText field is blank (null), assign Qty to equal zero.
                if (TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())){
                    Qty = 0;
                } else{
                    Qty = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                }

                // Increment Quantity by 1
                 Qty += 1;
                mQuantityEditText.setText(String.valueOf(Qty));
            }
        });

        // When Order button pressed, it will open the dial screen with the supplier phone number
        mOrderBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mSuppPhoneEditText.getText().toString().trim())){
                    Toast.makeText(view.getContext(), getString(R.string.provide_tel_number),
                            Toast.LENGTH_SHORT).show();
                }else {
                    String uri = "tel:" + mSuppPhoneEditText.getText().toString().trim();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
                    //intent.setData(Uri.parse(uri));
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Get user input from editor and save new book into database.
     */
    private void saveBook(){
        isFilled = false;

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierEditText.getText().toString().trim();
        String supplierPhoneString = mSuppPhoneEditText.getText().toString().trim();

        // Check if this is supposed to be a new book
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString)){
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(this, getString(R.string.missing_data), Toast.LENGTH_SHORT).show();
        } else if (mCurrentBookUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierNameString) ||
                TextUtils.isEmpty(supplierPhoneString)){
            //Check first that all mandatory fields are not null.
            // If it's a new book and there are some fields blank, a toast message will remind
            // the user to fill in all the required fields.
            Toast.makeText(this, getString(R.string.missing_data), Toast.LENGTH_SHORT).show();
        } else {
            /* Create a ContentValues object where column names are the keys,
             * and book attributes from the editor are the values.
             */
            ContentValues values = new ContentValues();

            // Converting price and quantity from String to float and integer respectively
            float priceValue = Float.parseFloat(priceString);
            int quantityValue = Integer.parseInt(quantityString);

            values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
            values.put(BookEntry.COLUMN_BOOK_PRICE, priceValue);
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantityValue);
            values.put(BookEntry.COLUMN_BOOK_SUPP_NAME, supplierNameString);
            values.put(BookEntry.COLUMN_BOOK_SUPP_NUM, supplierPhoneString);

            // All required fields are filled.
            isFilled = true;

            // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
            if (mCurrentBookUri == null) {
                // This is a NEW book, so insert a new book into the provider,
                // returning the content URI for the new book.
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentBookUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentBookUri, values,
                        null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Defining Menu to appear in the app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    // Defining Menu items (save button)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save book to database
                saveBook();
                // Exit activity
                if(isFilled)finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't been changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                /*
                 * Otherwise if there are unsaved changes, setup a dialog to warn the user.
                 * Create a click listener to handle the user confirming that
                 * changes should be discarded.
                 */
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // This method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // If the book hasn't been changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPP_NAME,
                BookEntry.COLUMN_BOOK_SUPP_NUM};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,                // Query the content URI for the current book
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPP_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPP_NUM);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(String.valueOf(price));
            mQuantityEditText.setText(String.valueOf(quantity));
            mSupplierEditText.setText(supplierName);
            mSuppPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mSuppPhoneEditText.setText("");
    }

    //Prompt the user to confirm that they want to delete this book.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete this book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing this book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete this book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}