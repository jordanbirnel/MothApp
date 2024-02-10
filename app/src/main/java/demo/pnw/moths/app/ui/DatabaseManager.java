package demo.pnw.moths.app.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseManager extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_IDKEY = "idKey";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String LOC = "location";
    private static final String PIC1 = "pic1";
    private static final String PIC2 = "pic2";
    private static final String PIC3 = "pic3";
    private static final String PIC4 = "pic4";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String UPLOADED = "uploaded";

    public DatabaseManager(Context context ) {
        super( context, "idKey_DB", null, DATABASE_VERSION );
    }
    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     * @param db The database.
     */
    public void onCreate(SQLiteDatabase db) {
        // Build sql create statement
        String sqlCreate = "create table " + TABLE_IDKEY + "( " + ID;
        sqlCreate += " integer primary key autoincrement, " + NAME;
        sqlCreate += " text, " + LOC + " text, " + PIC1 + " text, " + PIC2 + " text, "
                + PIC3 + " text, " + PIC4 + " text, "+ LATITUDE + " double, "+LONGITUDE+ " double, "+UPLOADED+" bool )";
        db.execSQL(sqlCreate);
    }

    /**
     * Called when the database needs to be upgraded. Drops the old table and creates a new db
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop old table if it exist
        db.execSQL( "drop table if exists " + TABLE_IDKEY);
        // Re-create tables
        onCreate(db);
    }
    /**
     * Inserts a new MothObservation record into the database.
     * @param mothMoment The MothObservation object to insert.
     */
    public void insert( MothObservation mothMoment){
        SQLiteDatabase db = this.getWritableDatabase();
        String sqlInsert = "insert into " + TABLE_IDKEY;
        sqlInsert += " values( null, '" + mothMoment.getMothName();
        sqlInsert += "', '" + mothMoment.getMothLocation();
        sqlInsert += "', '" + mothMoment.getCameraImage1();
        sqlInsert += "', '" + mothMoment.getCameraImage2();
        sqlInsert += "', '" + mothMoment.getCameraImage3();
        sqlInsert += "', '" + mothMoment.getCameraImage4();
        sqlInsert += "', '" + mothMoment.getLatitudeString();
        sqlInsert += "', '" + mothMoment.getLongitudeString();
        sqlInsert += "', 'false'" + " )";

        // Insert the data to the db and close to prevent leaks
        db.execSQL(sqlInsert);
        db.close();
    }
    /**
     * Deletes a MothObservation record from the database by ID.
     * @param id The ID of the record to delete.
     */
    public void deleteById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String sqlDelete = "delete from " + TABLE_IDKEY;
        sqlDelete += " where " + ID + " = " + id;
        db.execSQL(sqlDelete);
        db.close();
    }

    /**
     * Selects all MothObservation records from the database.
     * @return An ArrayList of MothObservation objects.
     */
    public ArrayList<MothObservation> selectAll( ) {
        String sqlQuery = "select * from " + TABLE_IDKEY;

        SQLiteDatabase db = this.getWritableDatabase( );
        Cursor cursor = db.rawQuery( sqlQuery, null );

        ArrayList<MothObservation> moments = new ArrayList<MothObservation>( );
        while( cursor.moveToNext( ) ) {
            MothObservation currentMoment
                    = new MothObservation( Integer.parseInt( cursor.getString( 0 ) ), cursor.getString( 1 ),
                    cursor.getString( 2 ), cursor.getString(3), cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), Double.parseDouble(cursor.getString( 7 )),
                    Double.parseDouble(cursor.getString( 8 )), (1==(cursor.getInt( 9 ))));
            moments.add( currentMoment );
        }
        db.close( );
        return moments;
    }
    /**
     * Selects a single MothObservation record from the database by ID.
     * @param id The ID of the record to select.
     * @return A MothObservation object or null if not found.
     */
    public MothObservation selectById(int id ) {
        String sqlQuery = "select * from " + TABLE_IDKEY;
        sqlQuery += " where " + ID + " = " + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery( sqlQuery, null );

        MothObservation moment = null;
        if( cursor.moveToFirst( ) )
            moment = new MothObservation( Integer.parseInt( cursor.getString( 0 ) ), cursor.getString( 1 ),
                    cursor.getString( 2 ), cursor.getString(3), cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), Double.parseDouble(cursor.getString( 7 )),
                    Double.parseDouble(cursor.getString( 8 )), Boolean.parseBoolean(cursor.getString( 9 )));
        return moment;
    }

    /**
     * Selects MothObservation records from the database by moth type (name).
     * @param type The type (name) of moth to filter by.
     * @return An ArrayList of MothObservation objects.
     */
    public ArrayList<MothObservation> selectByMothType(String type){
        String sqlQuery = "select * from " + TABLE_IDKEY;
        sqlQuery += " where " + NAME + " = " + "'"+ type+ "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, null);

        ArrayList<MothObservation> moments = new ArrayList<MothObservation>( );
        while( cursor.moveToNext( ) ) {
            MothObservation currentMoment
                    = new MothObservation( Integer.parseInt( cursor.getString( 0 ) ), cursor.getString( 1 ),
                    cursor.getString( 2 ), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getString(6), Double.parseDouble(cursor.getString( 7 )),
                    Double.parseDouble(cursor.getString( 8 )), Boolean.parseBoolean(cursor.getString( 9 )));
            moments.add( currentMoment );
        }

        db.close( );
        return moments;
    }
    /**
     * Updates the 'uploaded' status of a MothObservation record by ID.
     * @param id The ID of the record to update.
     */
    public void setUploaded(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String sqlQuery = "update " + TABLE_IDKEY;
        sqlQuery += " set " +UPLOADED+" = TRUE";
        sqlQuery += " where " +ID +" = "+ id;
        //Log.e("query",sqlQuery);
        try {
            db.execSQL(sqlQuery);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        db.close();
    }
}
