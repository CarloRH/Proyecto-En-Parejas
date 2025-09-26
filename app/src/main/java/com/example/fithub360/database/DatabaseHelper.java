package com.example.fithub360.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FitHub360.db";
    private static final int DATABASE_VERSION = 2; // nuevo esquema con muscle_group_id

    // Tabla usuarios
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Tabla ejercicios completados
    public static final String TABLE_COMPLETED_EXERCISES = "completed_exercises";
    public static final String COLUMN_EXERCISE_NAME = "exercise_name";
    public static final String COLUMN_CATEGORY = "category"; // legible
    public static final String COLUMN_MUSCLE_GROUP_ID = "muscle_group_id"; // id WGER
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Crear tabla usuarios
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COLUMN_PASSWORD + " TEXT NOT NULL);";

    // Crear tabla ejercicios completados
    private static final String CREATE_COMPLETED_EXERCISES_TABLE = "CREATE TABLE " + TABLE_COMPLETED_EXERCISES + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_EXERCISE_NAME + " TEXT NOT NULL, " +
            COLUMN_MUSCLE_GROUP_ID + " INTEGER NOT NULL, " +
            COLUMN_CATEGORY + " TEXT, " +
            COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_COMPLETED_EXERCISES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Estrategia simple: recrear tablas (para entornos de desarrollo)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLETED_EXERCISES);
        onCreate(db);
    }

    // Usuarios
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Ejercicios completados
    public boolean insertCompletedExercise(String exerciseName, int muscleGroupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXERCISE_NAME, exerciseName);
        values.put(COLUMN_MUSCLE_GROUP_ID, muscleGroupId);
        values.put(COLUMN_CATEGORY, mapCategory(muscleGroupId));
        long result = db.insert(TABLE_COMPLETED_EXERCISES, null, values);
        return result != -1;
    }

    // Compatibilidad con lógica existente basada en categoría legible
    public boolean addCompletedExercise(String exerciseName, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXERCISE_NAME, exerciseName);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_MUSCLE_GROUP_ID, reverseMapCategory(category));
        long result = db.insert(TABLE_COMPLETED_EXERCISES, null, values);
        return result != -1;
    }

    public int getCompletedExercisesCount(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_CATEGORY + "=?";
        String[] selectionArgs = {category};
        Cursor cursor = db.query(TABLE_COMPLETED_EXERCISES, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getCountByMuscleGroup(int muscleGroupId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_MUSCLE_GROUP_ID + "=?";
        String[] selectionArgs = {String.valueOf(muscleGroupId)};
        Cursor cursor = db.query(TABLE_COMPLETED_EXERCISES, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private String mapCategory(int muscleGroupId) {
        switch (muscleGroupId) {
            case 1: return "Pecho";
            case 2: return "Espalda";
            case 3: return "Piernas";
            case 4: return "Hombros";
            case 5: return "Bíceps";
            case 6: return "Tríceps";
            case 7: return "Abdominales";
            case 8: return "Antebrazos";
            default: return "Otro";
        }
    }

    private int reverseMapCategory(String category) {
        if (category == null) return 0;
        switch (category) {
            case "Pecho": return 1;
            case "Espalda": return 2;
            case "Piernas": return 3;
            case "Hombros": return 4;
            case "Bíceps": return 5;
            case "Tríceps": return 6;
            case "Abdominales": return 7;
            case "Antebrazos": return 8;
            default: return 0;
        }
    }
}
