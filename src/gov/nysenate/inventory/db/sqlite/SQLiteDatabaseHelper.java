package gov.nysenate.inventory.db.sqlite;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gov.nysenate.inventory.util.SqlParser;
import gov.nysenate.inventory.util.AssetUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper
{

    private static final String SQL_DIR = "sql";

    private static final String DROPFILE = "drop.sql";

    private static final String CREATEFILE = "create.sql";

    private static final String UPGRADEFILE_PREFIX = "upgrade-";

    private static final String UPGRADEFILE_SUFFIX = ".sql";

    static SQLiteDatabase db = null;

    private Context context;

    public SQLiteDatabaseHelper(Context context, String name,
            CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
        db = this.getWritableDatabase();
    }

    /*
     * BELOW ARE DDL (DATA DEFINITION LANGUAGE) FOR THE DATABASE
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            execSqlFile(CREATEFILE, db);
        } catch (IOException exception) {
            throw new RuntimeException("Database creation failed", exception);
        }
    }

    public void resetDatabase(SQLiteDatabase db) {
        try {
            this.db = db;
            execSqlFile(DROPFILE, db);
        } catch (IOException exception) {
            throw new RuntimeException("Database drop failed", exception);
        }
        onCreate(db);
        onUpgrade(db, 0, 999); // Run any db upgrades (from 0 to 999)
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            for (String sqlFile : AssetUtils.list(SQL_DIR,
                    this.context.getAssets())) {
                if (sqlFile.startsWith(UPGRADEFILE_PREFIX)) {
                    int fileVersion = Integer.parseInt(sqlFile.substring(
                            UPGRADEFILE_PREFIX.length(), sqlFile.length()
                                    - UPGRADEFILE_SUFFIX.length()));
                    if (fileVersion > oldVersion && fileVersion <= newVersion) {
                        execSqlFile(sqlFile, db);
                    }
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Database upgrade failed", exception);
        }
    }

    /*
     * BELOW RUNS DATABAS SCRIPT FILES
     */

    protected void execSqlFile(String sqlFile, SQLiteDatabase db)
            throws SQLException, IOException {
        try {
            for (String sqlInstruction : SqlParser.parseSqlFile(SQL_DIR + "/"
                    + sqlFile, this.context.getAssets())) {
                System.out.println("***EXECUTING SQL:" + sqlInstruction);
                try {
                    db.execSQL(sqlInstruction);
                } catch (SQLException e) {
                    if (sqlFile.equals(this.DROPFILE)) {
                        e.printStackTrace();
                    } else {
                        throw e;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    /*
     * BELOW ARE DML (DATA MANIPULATION LANGUAGE) FOR THE DATABASE
     */

    public Long insert(String table, ContentValues insertValues)
            throws Exception {
        long result = 0;
        result = db.insert(table, null, insertValues);
        return result;
    }

    public Long update(String table, ContentValues updateValues,
            String whereClause) {
        return update(table, updateValues, null);
    }

    public Long update(String table, ContentValues updateValues,
            String whereClause, String[] selectionArgs) throws Exception {
        long result = 0;
        result = db.update(table, updateValues, whereClause, selectionArgs);
        return result;
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        Cursor mCursor = null;
        try {
            mCursor = db.rawQuery(sql, selectionArgs);
            if (mCursor != null) {
                mCursor.moveToFirst();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCursor; // iterate to get each value.
    }

    public void truncateTable(String table) {
        delete(table, null);
    }

    public int delete(String table, String whereClause) {
        return delete(table, whereClause, null);
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        int rowsDeleted = db.delete(table, whereClause, whereArgs);
        Cursor cursor = this.rawQuery("SELECT 1 FROM " + table + " LIMIT 1",
                new String[] {});
        boolean recExists = (cursor.getCount() > 0);
        if (!recExists) {
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + table
                    + "'");
        }
        return rowsDeleted;
    }

    public void execSQL(String sql) {
        db.execSQL(sql);
    }

    public void resetDB(SQLiteDatabase db) {
        resetDatabase(db);
    }

    /*
     * 
     * Other helper methods
     */

    public String getNow() {
        return getNow("yyyy-MM-dd hh:mm:ss.SSS a");
    }

    public String getNow(String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat,
                Locale.US);
        Date date = new Date();
        String currentDate = simpleDateFormat.format(date);
        return currentDate;
    }

    public Date getDate(String dateValue) throws ParseException {
        return getDate(dateValue, "yyyy-MM-dd hh:mm:ss.SSS a");
    }

    public Date getDate(String dateValue, String dateFormat)
            throws ParseException {
        Date currendDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat,
                Locale.US);
        currendDate = simpleDateFormat.parse(dateValue);
        return currendDate;
    }

}