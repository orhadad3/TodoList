package com.example.todolistproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Sql class extends SQLiteOpenHelper to manage database creation and version management.
 */
public class Sql extends SQLiteOpenHelper {
    // Database name and version constants
    public static final String DB_NAME = "tasks.db";
    public static final int DB_VERSION = 1;

    /**
     * Constructor for Sql class.
     *
     * @param context The context of the application.
     */
    public Sql(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This method creates the tasks table.
     *
     * @param db The database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // SQL statement to create the tasks table
            String CREATE_TASKS_TABLE = "CREATE TABLE tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date TEXT NOT NULL," +
                    "description TEXT NOT NULL," +
                    "urgency INTEGER NOT NULL," +
                    "status INTEGER NOT NULL," +
                    "post_to TEXT," +
                    "user_uid TEXT NOT NULL" +
                    ");";
            // Execute the SQL statement
            db.execSQL(CREATE_TASKS_TABLE);
        } catch (Exception e) {
            Log.e("Sql", "Error creating database: " + e.getMessage());
        }
    }

    /**
     * Called when the database needs to be upgraded.
     * This implementation simply drops the existing table and creates a new one.
     *
     * @param db         The database instance.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the existing tasks table if it exists
        db.execSQL("DROP TABLE IF EXISTS tasks");
        // Recreate the database
        onCreate(db);
    }
}