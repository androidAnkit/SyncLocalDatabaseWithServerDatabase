package com.example.woco.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract {

    private Contract(){}

    public static final int SYNC_STATUS_FAILED=0;
    public static final int SYNC_STATUS_SUCCESS=1;
    public static final String SERVER_BASE_URL="http://10.0.2.2/woco_connect.php";
    public static final String UI_UPDATE_BROADCAST="com.example.woco.data.uiupdatebroadcast";

    public static final String CURSOR_DIR_BASE_TYPE = "vnd.android.cursor.dir";     // For whole list
    public static final String CURSOR_ITEM_BASE_TYPE = "vnd.android.cursor.item";  // For single item
    public static final String CONTENT_AUTHORITY = "com.example.woco";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NAMES = "names";

    public static final class NamesEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_NAMES);
        public static final String CONTENT_LIST_TYPE = CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NAMES;
        public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NAMES;

        /** Name of database table for woco */
        public final static String TABLE_NAME = "names";

        /**
         * Unique ID number for the names (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name column
         *
         * Type: TEXT
         */
        public final static String COLUMN_NAME ="name";

        /**
         * sync_status column
         *
         * Type: INTEGER
         */
        public final static String SYNC_STATUS ="sync_status";
    }
}
