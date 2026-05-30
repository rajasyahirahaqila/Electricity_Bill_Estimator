package com.example.electricitybillestimator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
public class DatabaseHelper extends SQLiteOpenHelper {

    // ─── Database Configuration ─────────────────────────────────
    private static final String DATABASE_NAME    = "electricity_bills.db";
    private static final int    DATABASE_VERSION = 1;

    // ─── Table Name ─────────────────────────────────────────────
    public static final String TABLE_NAME = "bill_records";

    // ─── Column Names ───────────────────────────────────────────
    // These must match exactly what we use everywhere
    public static final String COLUMN_ID            = "id";
    public static final String COLUMN_MONTH         = "month";
    public static final String COLUMN_UNIT          = "unit";
    public static final String COLUMN_REBATE        = "rebate";
    public static final String COLUMN_TOTAL_CHARGES = "total_charges";
    public static final String COLUMN_FINAL_COST    = "final_cost";

    // ─── SQL Statement to CREATE the table ──────────────────────
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_MONTH         + " TEXT NOT NULL, "
                    + COLUMN_UNIT          + " REAL NOT NULL, "
                    + COLUMN_REBATE        + " INTEGER NOT NULL, "
                    + COLUMN_TOTAL_CHARGES + " REAL NOT NULL, "
                    + COLUMN_FINAL_COST    + " REAL NOT NULL"
                    + ")";

    // ─── Constructor ────────────────────────────────────────────
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ─── Called ONCE when app runs for the first time ───────────
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    // ─── Called when DATABASE_VERSION number increases ──────────
    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Drop old table and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ═══════════════════════════════════════════════════════════
    // CREATE — Insert a new bill record into the database
    // Returns the row ID of the new record (-1 if failed)
    // ═══════════════════════════════════════════════════════════
    public long insertRecord(BillRecord record) {
        // Get a writable database connection
        SQLiteDatabase db = this.getWritableDatabase();

        // ContentValues is like a Map — key=column, value=data
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH,         record.getMonth());
        values.put(COLUMN_UNIT,          record.getUnit());
        values.put(COLUMN_REBATE,        record.getRebate());
        values.put(COLUMN_TOTAL_CHARGES, record.getTotalCharges());
        values.put(COLUMN_FINAL_COST,    record.getFinalCost());

        // Insert the row and get its ID
        long id = db.insert(TABLE_NAME, null, values);

        // Always close the database when done
        db.close();

        return id;
    }

    // ═══════════════════════════════════════════════════════════
    // READ ALL — Get every record from the database
    // Returns a List of BillRecord objects
    // ═══════════════════════════════════════════════════════════
    public List<BillRecord> getAllRecords() {
        List<BillRecord> recordList = new ArrayList<>();

        // SQL query to get ALL rows, newest first
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();

        // Cursor is like a pointer that moves through results
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through each row
        if (cursor.moveToFirst()) {
            do {
                // Read each column by its name
                int    id    = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_ID));
                String month = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_MONTH));
                double unit  = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COLUMN_UNIT));
                int rebate   = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_REBATE));
                double total = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COLUMN_TOTAL_CHARGES));
                double final_ = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COLUMN_FINAL_COST));

                // Create a BillRecord object and add to list
                recordList.add(new BillRecord(
                        id, month, unit, rebate, total, final_));

            } while (cursor.moveToNext()); // Move to next row
        }

        // Always close cursor and database
        cursor.close();
        db.close();

        return recordList;
    }

    // ═══════════════════════════════════════════════════════════
    // READ ONE — Get a single record by its ID
    // ═══════════════════════════════════════════════════════════
    public BillRecord getRecordById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query only the row with matching ID
        Cursor cursor = db.query(
                TABLE_NAME,       // table name
                null,             // all columns (null = SELECT *)
                COLUMN_ID + "=?", // WHERE id = ?
                new String[]{String.valueOf(id)}, // value for ?
                null, null, null
        );

        BillRecord record = null;

        if (cursor != null && cursor.moveToFirst()) {
            record = new BillRecord(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MONTH)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_UNIT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REBATE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_CHARGES)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FINAL_COST))
            );
            cursor.close();
        }

        db.close();
        return record;
    }

    // ═══════════════════════════════════════════════════════════
    // UPDATE — Edit an existing record
    // Returns number of rows affected (1 = success, 0 = failed)
    // ═══════════════════════════════════════════════════════════
    public int updateRecord(BillRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH,         record.getMonth());
        values.put(COLUMN_UNIT,          record.getUnit());
        values.put(COLUMN_REBATE,        record.getRebate());
        values.put(COLUMN_TOTAL_CHARGES, record.getTotalCharges());
        values.put(COLUMN_FINAL_COST,    record.getFinalCost());

        // UPDATE WHERE id = record.getId()
        int rowsAffected = db.update(
                TABLE_NAME,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(record.getId())}
        );

        db.close();
        return rowsAffected;
    }

    // ═══════════════════════════════════════════════════════════
    // DELETE — Remove a record by its ID
    // Returns number of rows deleted (1 = success, 0 = failed)
    // ═══════════════════════════════════════════════════════════
    public int deleteRecord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rowsDeleted = db.delete(
                TABLE_NAME,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return rowsDeleted;
    }

    // ═══════════════════════════════════════════════════════════
    // COUNT — Get total number of records
    // ═══════════════════════════════════════════════════════════
    public int getRecordCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NAME, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }
}