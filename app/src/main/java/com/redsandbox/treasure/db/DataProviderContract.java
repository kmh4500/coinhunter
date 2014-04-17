package com.redsandbox.treasure.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 *
 * Defines constants for accessing the content provider defined in DataProvider. A content provider
 * contract assists in accessing the provider's available content URIs, column names, MIME types,
 * and so forth, without having to know the actual values.
 */
public final class DataProviderContract implements BaseColumns {

    private DataProviderContract() { }
        
        // The URI scheme used for content URIs
        public static final String SCHEME = "content";

        // The provider's authority
        public static final String AUTHORITY = "com.redsandbox";

        /**
         * The DataProvider content URI
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

        /**
         *  The MIME type for a content URI that would return multiple rows
         *  <P>Type: TEXT</P>
         */
        public static final String MIME_TYPE_ROWS =
                "vnd.android.cursor.dir/vnd.com.redsandbox";

        /**
         * The MIME type for a content URI that would return a single row
         *  <P>Type: TEXT</P>
         *
         */
        public static final String MIME_TYPE_SINGLE_ROW =
                "vnd.android.cursor.item/vnd.com.redsandbox";

        /**
         * Picture table primary key column name
         */
        public static final String ROW_ID = BaseColumns._ID;
        
        /**
         * Point table
         * {"Text":"hu","Y":287,"X":187,"PostID":13},
         * 
         * {"Link":"http://ted-zine.appspot.com",
         * "Favicon":"http://ted-zine.appspot.com/favicon.ico",
         * "Y":437,"X":778,"Title":"TEDZINE","PostID":45},
         * 
         * {"Images":"[{\"title\":\"english_cover.jpg\",\"url\":\"UploadServlet?getfile=26022106-9f2d-4f01-b9f2-c0d308be8225.jpg\"}]","Y":470,"X":556,"PostID":51},
         */
        public static final String POINT_TABLE_NAME = "PointData";
        public static final Uri POINT_TABLE_CONTENTURI =
                Uri.withAppendedPath(CONTENT_URI, POINT_TABLE_NAME);
        public static final String COLUMN_TEXT = "Text";
        public static final String COLUMN_X = "X";
        public static final String COLUMN_Y = "Y";
        public static final String COLUMN_COLOR = "Color";
        public static final String COLUMN_POST_ID = "PostID";
        public static final String COLUMN_SPACE_ID = "SpaceID";

        public static final String SPACE_TABLE_NAME = "SpaceData";
        public static final Uri SPACE_TABLE_CONTENTURI =
                Uri.withAppendedPath(CONTENT_URI, SPACE_TABLE_NAME);
        public static final String COLUMN_SPACE_SPACE_ID = "SpaceID";
        public static final String COLUMN_SPACE_SPACE_NAME = "SpaceName";
        public static final String COLUMN_SPACE_COLOR = "Color";

        // The content provider database name
        public static final String DATABASE_NAME = "PointDataDB";
        
        // The starting version of the database
        public static final int DATABASE_VERSION = 1;
}
