/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redsandbox.treasure.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

/**
 *
 * Defines a ContentProvider that stores URLs of Picasa featured pictures
 * The provider also has a table that tracks the last time a picture URL was updated.
 */
public class DataProvider extends ContentProvider {
    // Indicates that the incoming query is for a picture URL
    public static final int POINT_QUERY = 1;
    public static final int SPACE_QUERY = 2;

    // Indicates an invalid content URI
    public static final int INVALID_URI = -1;

    // Constants for building SQLite tables during initialization
    private static final String TEXT_TYPE = "TEXT";
    private static final String PRIMARY_KEY_TYPE = "INTEGER PRIMARY KEY";
    private static final String UNIQUE_TEXT_TYPE = "TEXT UNIQUE";
    private static final String INTEGER_TYPE = "INTEGER";

    private static final String CREATE_POINT_TABLE_SQL = "CREATE TABLE" + " " +
            DataProviderContract.POINT_TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.ROW_ID + " " + PRIMARY_KEY_TYPE + " ," +
            DataProviderContract.COLUMN_TEXT + " " + TEXT_TYPE + " ," +
            DataProviderContract.COLUMN_COLOR + " " + INTEGER_TYPE + " ," +
            DataProviderContract.COLUMN_SPACE_ID + " " + TEXT_TYPE + " ," +
            DataProviderContract.COLUMN_POST_ID + " " + UNIQUE_TEXT_TYPE + " ," +
            DataProviderContract.COLUMN_X + " " + INTEGER_TYPE + " ," +
            DataProviderContract.COLUMN_Y + " " + INTEGER_TYPE  +
            ")";
    
    private static final String CREATE_SPACE_TABLE_SQL = "CREATE TABLE" + " " +
            DataProviderContract.SPACE_TABLE_NAME + " " +
            "(" + " " +
            DataProviderContract.ROW_ID + " " + PRIMARY_KEY_TYPE + " ," +
            DataProviderContract.COLUMN_SPACE_SPACE_ID + " " + TEXT_TYPE + " ," +
            DataProviderContract.COLUMN_SPACE_COLOR + " " + INTEGER_TYPE + " ," +
            DataProviderContract.COLUMN_SPACE_SPACE_NAME + " " + TEXT_TYPE +
            ")";
    
    // Identifies log statements issued by this component
    public static final String LOG_TAG = "DataProvider";

    // Defines an helper object for the backing database
    private SQLiteOpenHelper mHelper;

    // Defines a helper object that matches content URIs to table-specific parameters
    private static final UriMatcher sUriMatcher;

    // Stores the MIME types served by this provider
    private static final SparseArray<String> sMimeTypes;

    // Stores the MIME types served by this provider
    private static final SparseArray<String> tables = new SparseArray<String>();
    
    /*
     * Initializes meta-data used by the content provider:
     * - UriMatcher that maps content URIs to codes
     * - MimeType array that returns the custom MIME type of a table
     */
    static {
        
        // Creates an object that associates content URIs with numeric codes
        sUriMatcher = new UriMatcher(0);

        /*
         * Sets up an array that maps content URIs to MIME types, via a mapping between the
         * URIs and an integer code. These are custom MIME types that apply to tables and rows
         * in this particular provider.
         */
        sMimeTypes = new SparseArray<String>();
        
        tables.put(POINT_QUERY, DataProviderContract.POINT_TABLE_NAME);
        tables.put(SPACE_QUERY, DataProviderContract.SPACE_TABLE_NAME);

        // Adds a URI "match" entry that maps picture URL content URIs to a numeric code
        for (int i = 0; i < tables.size(); ++i) {
            sUriMatcher.addURI(
                    DataProviderContract.AUTHORITY,
                    tables.valueAt(i),
                    tables.keyAt(i));
            sMimeTypes.put(
            		tables.keyAt(i),
                    "vnd.android.cursor.dir/vnd." +
                    DataProviderContract.AUTHORITY + "." +
                    tables.valueAt(i));
        }
    }

    // Closes the SQLite database helper class, to avoid memory leaks
    public void close() {
        mHelper.close();
    }
    
    /**
     * Defines a helper class that opens the SQLite database for this provider when a request is
     * received. If the database doesn't yet exist, the helper creates it.
     */
    private class DataProviderHelper extends SQLiteOpenHelper {
        /**
         * Instantiates a new SQLite database using the supplied database name and version
         *
         * @param context The current context
         */
        DataProviderHelper(Context context) {
            super(context,
                    DataProviderContract.DATABASE_NAME,
                    null,
                    DataProviderContract.DATABASE_VERSION);
        }


        /**
         * Executes the queries to drop all of the tables from the database.
         *
         * @param db A handle to the provider's backing database.
         */
        private void dropTables(SQLiteDatabase db) {
        	for (int i = 0; i < tables.size(); ++i) {
	            // If the table doesn't exist, don't throw an error
	            db.execSQL("DROP TABLE IF EXISTS " + tables.valueAt(i));
        	}
        }

        /**
         * Does setup of the database. The system automatically invokes this method when
         * SQLiteDatabase.getWriteableDatabase() or SQLiteDatabase.getReadableDatabase() are
         * invoked and no db instance is available.
         *
         * @param db the database instance in which to create the tables.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creates the tables in the backing database for this provider
            db.execSQL(CREATE_POINT_TABLE_SQL);
            db.execSQL(CREATE_SPACE_TABLE_SQL);
        }

        /**
         * Handles upgrading the database from a previous version. Drops the old tables and creates
         * new ones.
         *
         * @param db The database to upgrade
         * @param version1 The old database version
         * @param version2 The new database version
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int version1, int version2) {
            Log.w(DataProviderHelper.class.getName(),
                    "Upgrading database from version " + version1 + " to "
                            + version2 + ", which will destroy all the existing data");

            // Drops all the existing tables in the database
            dropTables(db);

            // Invokes the onCreate callback to build new tables
            onCreate(db);
        }
        /**
         * Handles downgrading the database from a new to a previous version. Drops the old tables
         * and creates new ones.
         * @param db The database object to downgrade
         * @param version1 The old database version
         * @param version2 The new database version
         */
        @Override
        public void onDowngrade(SQLiteDatabase db, int version1, int version2) {
            Log.w(DataProviderHelper.class.getName(),
                "Downgrading database from version " + version1 + " to "
                        + version2 + ", which will destroy all the existing data");
    
            // Drops all the existing tables in the database
            dropTables(db);
    
            // Invokes the onCreate callback to build new tables
            onCreate(db);
            
        }
    }
    /**
     * Initializes the content provider. Notice that this method simply creates a
     * the SQLiteOpenHelper instance and returns. You should do most of the initialization of a
     * content provider in its static initialization block or in SQLiteDatabase.onCreate().
     */
    @Override
    public boolean onCreate() {

        // Creates a new database helper object
        mHelper = new DataProviderHelper(getContext());

        return true;
    }
    /**
     * Returns the result of querying the chosen table.
     * @see android.content.ContentProvider#query(android.net.Uri, String[], String, String[], String)
     * @param uri The content URI of the table
     * @param projection The names of the columns to return in the cursor
     * @param selection The selection clause for the query
     * @param selectionArgs An array of Strings containing search criteria
     * @param sortOrder A clause defining the order in which the retrieved rows should be sorted
     * @return The query results, as a {@link android.database.Cursor} of rows and columns
     */
    @Override
    public Cursor query(
        Uri uri,
        String[] projection,
        String selection,
        String[] selectionArgs,
        String sortOrder) {

        SQLiteDatabase db = mHelper.getReadableDatabase();
        // Decodes the content URI and maps it to a code
        int code = sUriMatcher.match(uri);
        switch (code) {

            // If the query is for a picture URL
            case POINT_QUERY:
            case SPACE_QUERY:
                // Does the query against a read-only version of the database
                Cursor returnCursor = db.query(
                    tables.get(code),
                    projection,selection, selectionArgs, null, null, sortOrder);

                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;

            case INVALID_URI:

                throw new IllegalArgumentException("Query -- Invalid URI:" + uri);
        }

        return null;
    }
    
    

    /**
     * Returns the mimeType associated with the Uri (query).
     * @see android.content.ContentProvider#getType(android.net.Uri)
     * @param uri the content URI to be checked
     * @return the corresponding MIMEtype
     */
    @Override
    public String getType(Uri uri) {

        return sMimeTypes.get(sUriMatcher.match(uri));
    }
    /**
     *
     * Insert a single row into a table
     * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
     * @param uri the content URI of the table
     * @param values a {@link android.content.ContentValues} object containing the row to insert
     * @return the content URI of the new row
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
    	int code = sUriMatcher.match(uri);
        switch (code) {
            case POINT_QUERY:
            case SPACE_QUERY:
                mHelper.getWritableDatabase().insertWithOnConflict(tables.get(code), null, values, SQLiteDatabase.CONFLICT_REPLACE);
            	break;
            default:
                throw new IllegalArgumentException("Insert: Invalid URI" + uri);
        }

        return null;
    }
    /**
     * Implements bulk row insertion using
     * {@link android.database.sqlite.SQLiteDatabase#insert(String, String, android.content.ContentValues) SQLiteDatabase.insert()}
     * and SQLite transactions. The method also notifies the current
     * {@link android.content.ContentResolver} that the {@link android.content.ContentProvider} has
     * been changed.
     * @see android.content.ContentProvider#bulkInsert(android.net.Uri, android.content.ContentValues[])
     * @param uri The content URI for the insertion
     * @param insertValuesArray A {@link android.content.ContentValues} array containing the row to
     * insert
     * @return The number of rows inserted.
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] insertValuesArray) {
        // Decodes the content URI and choose which insert to use
        switch (sUriMatcher.match(uri)) {
            case POINT_QUERY:
            	bulkInsertInternal(uri, insertValuesArray, DataProviderContract.POINT_TABLE_NAME, DataProviderContract.COLUMN_TEXT, false);
                return insertValuesArray.length;
            case INVALID_URI:
                // An invalid URI was passed. Throw an exception
                throw new IllegalArgumentException("Bulk insert -- Invalid URI:" + uri);
        }
        return -1;
    }
    
    private void bulkInsertInternal(Uri uri, ContentValues[] insertValuesArray,
			String tableName, String columnHack, boolean deleteHistory) {
    	// Gets a writeable database instance if one is not already cached
        SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

        /*
         * Begins a transaction in "exclusive" mode. No other mutations can occur on the
         * db until this transaction finishes.
         */
        localSQLiteDatabase.beginTransaction();

        if (deleteHistory) {
	        // Deletes all the existing rows in the table
	        localSQLiteDatabase.delete(tableName, null, null);
        }
        // Gets the size of the bulk insert
        int numImages = insertValuesArray.length;

        // Inserts each ContentValues entry in the array as a row in the database
        for (int i = 0; i < numImages; i++) {
            localSQLiteDatabase.insertWithOnConflict(tableName, columnHack, insertValuesArray[i], SQLiteDatabase.CONFLICT_REPLACE);
        }

        // Reports that the transaction was successful and should not be backed out.
        localSQLiteDatabase.setTransactionSuccessful();

        // Ends the transaction and closes the current db instances
        localSQLiteDatabase.endTransaction();
	}
    
	/**
     * Returns an UnsupportedOperationException if delete is called
     * @see android.content.ContentProvider#delete(android.net.Uri, String, String[])
     * @param uri The content URI
     * @param selection The SQL WHERE string. Use "?" to mark places that should be substituted by
     * values in selectionArgs.
     * @param selectionArgs An array of values that are mapped to each "?" in selection. If no "?"
     * are used, set this to NULL.
     *
     * @return the number of rows deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	int code = sUriMatcher.match(uri);
    	switch (code) {
    	case POINT_QUERY:
    		mHelper.getWritableDatabase().delete(tables.get(code), selection, selectionArgs);
    		break;
    	default:
    		throw new IllegalArgumentException("Insert: Invalid URI" + uri);
    	}
      return 0;
    }

    /**
     * Updates one or more rows in a table.
     * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, String, String[])
     * @param uri The content URI for the table
     * @param values The values to use to update the row or rows. You only need to specify column
     * names for the columns you want to change. To clear the contents of a column, specify the
     * column name and NULL for its value.
     * @param selection An SQL WHERE clause (without the WHERE keyword) specifying the rows to
     * update. Use "?" to mark places that should be substituted by values in selectionArgs.
     * @param selectionArgs An array of values that are mapped in order to each "?" in selection.
     * If no "?" are used, set this to NULL.
     *
     * @return int The number of rows updated.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // Decodes the content URI and choose which insert to use
    	int code = sUriMatcher.match(uri);
        switch (code) {
            case POINT_QUERY:
                SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();
                localSQLiteDatabase.update(tables.get(code), values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                throw new IllegalArgumentException("Insert: Invalid URI" + uri);
        }

        return -1;
    }
    
}
