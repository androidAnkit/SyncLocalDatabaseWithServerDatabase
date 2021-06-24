package com.example.woco.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NamesProvider extends ContentProvider {

    public static final String LOG_TAG = NamesProvider.class.getSimpleName();
    private dbHelper mDbHelper;

    private static final int NAMES = 100;
    private static final int NAME = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,Contract.PATH_NAMES,NAMES);
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY,Contract.PATH_NAMES + "/#",NAME);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NAMES:
                cursor = database.query(Contract.NamesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case NAME:
                selection = Contract.NamesEntry.COLUMN_NAME + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(Contract.NamesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NAMES:
                return Contract.NamesEntry.CONTENT_LIST_TYPE;
            case NAME:
                return Contract.NamesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case NAMES:
                return insertName(uri, values);
            default:
                throw new IllegalArgumentException();
        }
    }

    private Uri insertName(Uri uri, ContentValues values){

        String name = values.getAsString(Contract.NamesEntry.COLUMN_NAME);
        if(name == null || name.length() == 0){
            throw new IllegalArgumentException("Name is required");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(Contract.NamesEntry.TABLE_NAME,null,values);

        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri,id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowDeleted;
        final int match = sUriMatcher.match(uri);
        switch(match){
            case NAMES:
                rowDeleted = db.delete(Contract.NamesEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case NAME:
                selection = Contract.NamesEntry.COLUMN_NAME + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowDeleted =  db.delete(Contract.NamesEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if(rowDeleted!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case NAMES:
                return updateName(uri,values,selection,selectionArgs);
            case NAME:
                selection = Contract.NamesEntry.COLUMN_NAME + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateName(uri,values,selection,selectionArgs);
            default:throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int updateName( Uri uri, ContentValues values, String selection, String[] selectionArgs){

        if(values.containsKey(Contract.NamesEntry.COLUMN_NAME)) {
            String name = values.getAsString(Contract.NamesEntry.COLUMN_NAME);
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if(values.size()==0){
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowupdated = db.update(Contract.NamesEntry.TABLE_NAME,values,selection,selectionArgs);

        if(rowupdated!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowupdated;
    }

}
