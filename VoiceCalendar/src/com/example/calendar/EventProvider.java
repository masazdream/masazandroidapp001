package com.example.calendar;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class EventProvider extends ContentProvider {
	private EventDatabaseHelper mEventDatabaseHelper = null;
	//private static final int CURRENT_DATABASE_VERSION = 1;
	//private static final int CURRENT_DATABASE_VERSION = 2;
	private static final int CURRENT_DATABASE_VERSION = 3;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		int numDeleted = db.delete(EventInfo.DB_NAME, selection, selectionArgs);
		return numDeleted;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		long newId = db.insert(EventInfo.DB_NAME, null, values);
		Uri newUri = Uri.parse(uri + "/" + newId);
		return newUri;
	}

	@Override
	public boolean onCreate() {
		// EventDatabaseHelperインスタンスを作成する（データベースにアクセスするためのSQLiteOpenHelperというクラスを継承したもの）
		mEventDatabaseHelper = new EventDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		Cursor c = db.query(EventInfo.DB_NAME, projection, selection,
				selectionArgs, null, null, sortOrder);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mEventDatabaseHelper.getWritableDatabase();
		int numUpdated = db.update(EventInfo.DB_NAME, values, selection,
				selectionArgs);
		return numUpdated;
	}

	public class EventDatabaseHelper extends SQLiteOpenHelper {

		public EventDatabaseHelper(Context context) {
			super(context, EventInfo.DB_NAME, null, CURRENT_DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE " + EventInfo.DB_NAME + "("
					+ EventInfo.ID + " INTEGER PRIMARY KEY," + EventInfo.TITLE
					+ " TEXT," + EventInfo.CONTENT + " TEXt," + EventInfo.WHERE
					+ " TEXT," + EventInfo.START_TIME + " TEXT,"
					+ EventInfo.END_TIME + " TEXT,"
					+ EventInfo.RECORDING_FILE + " TEXT" + ");";
			db.execSQL(sql);
		}

		/**
		 * アプリのバージョンがあがって、データベースのレコードに変更があった場合などDatabaseVersionが異なる 場合に呼び出される
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// テーブルをいったん削除
			db.execSQL("DROP TABLE IF EXISTS " + EventInfo.DB_NAME);
			// 再作成
			onCreate(db);
		}

	}

}
