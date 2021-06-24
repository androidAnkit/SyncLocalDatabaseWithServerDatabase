package com.example.woco.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.woco.wocoNames;

import java.util.jar.Attributes;

import static com.example.woco.data.Contract.NamesEntry.COLUMN_NAME;
import static com.example.woco.data.Contract.NamesEntry.SYNC_STATUS;
import static com.example.woco.data.Contract.NamesEntry.TABLE_NAME;

public class dbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = dbHelper.class.getSimpleName();

    /* Name of the database */
    public static final String DATABASE_NAME = "woco.db";
    /* Version of the database */
    public static final int DATABASE_VERSION = 1;
    /* The Text datatype */
    public static final String TEXT_TYPE = " TEXT";

    public SQLiteDatabase database;


    private static final String SQL_CREATE_WOCO_TABLE =  "CREATE TABLE " + Contract.NamesEntry.TABLE_NAME + " ("
            + Contract.NamesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL, " + SYNC_STATUS + " INTEGER NOT NULL);";

    public dbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /* Executing the SQL statement */
        db.execSQL(SQL_CREATE_WOCO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contract.NamesEntry.TABLE_NAME);
    }

//    public void close() {
//        database.close();
//    }

    public wocoNames findName(String name){
        String query = "select " + COLUMN_NAME + " from " + Contract.NamesEntry.TABLE_NAME + " where " + COLUMN_NAME + "= \"" + name + "\"";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor=db.rawQuery(query,null);

        wocoNames wocoNames=new wocoNames();

        if (cursor.moveToFirst()) {
            wocoNames.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            cursor.close();
        }else{
            wocoNames=null;
        }
        db.close();

        return wocoNames;
    }

    public Cursor view_all_names(){
        SQLiteDatabase database = this.getReadableDatabase();
        String [] projection = {COLUMN_NAME, SYNC_STATUS};
        //String query="Select * from "+ Contract.NamesEntry.TABLE_NAME;
       // Cursor data= database.rawQuery(query,null);
        return database.query(TABLE_NAME,projection,null,null,null,null,null);
     //   return data;
    }

    public wocoNames updateDatabase(String name, int sync_status){
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_NAME,name);
//        values.put(SYNC_STATUS,sync_status);
        String query = "Update " + TABLE_NAME + " set " + SYNC_STATUS + "="+Contract.SYNC_STATUS_SUCCESS+ " where " + COLUMN_NAME + "= \"" + name + "\""  + " AND " + SYNC_STATUS + "= \"" + Contract.SYNC_STATUS_FAILED + "\"" ;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor=db.rawQuery(query,null);

        wocoNames wocoNames = new wocoNames();

        if (cursor.moveToFirst()) {
            wocoNames.setSyncStatus(cursor.getInt(Integer.parseInt(cursor.getColumnName(Contract.SYNC_STATUS_SUCCESS))));
            cursor.close();
        }else{
            wocoNames=null;
        }
        db.close();


//        String selection = COLUMN_NAME + "=?";
//        String[] selection_args= {name};
//        database.update(TABLE_NAME,values,selection,selection_args);
        return wocoNames;
    }

    public void removeSelectedName(String name) {
        //Open the database
        SQLiteDatabase database = this.getWritableDatabase();
        //Execute sql query to remove from database
        //NOTE: When removing by String in SQL, value must be enclosed with ''
        database.execSQL("DELETE FROM " + Contract.NamesEntry.TABLE_NAME + " WHERE " + COLUMN_NAME + "= \"" + name + "\"");
        //Close the database
        database.close();
    }

    public boolean addName(String name, int sync_status){
        // Getting the database in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Putting values
        values.put(Contract.NamesEntry.COLUMN_NAME,name);
        values.put(SYNC_STATUS,sync_status);
        long newRowId = db.insert(Contract.NamesEntry.TABLE_NAME, null, values);
        //Uri newUri = getContentResolver().insert(Contract.NamesEntry.CONTENT_URI,values);
        db.close();
        if(newRowId==-1){
            return false;
        }else
            return true;
    }

}
