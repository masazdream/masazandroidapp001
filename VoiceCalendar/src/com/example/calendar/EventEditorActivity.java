package com.example.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * イベントの詳細編集画面
 * 
 * 1.OnClickメソッドを処理するためにEventEditorActivityにOnClickListenerをインプリメント</br>
 * 2.リソースからViewを作成</br> 3.TextEditなどのビューの要素を取得</br>
 * 4.OnClickListenerにEventEditorActivityをセットする</br>
 * 5.インテントのExtraからデータのIDを取得する</br> 6.インテントのExtraから日付を取得する</br>
 * 7.IDが0なら新規作成なので、タップされた日付で今の時刻からスケジュールとしてデータを作成する</br>
 * 8.IDが0でなければ、DBからデータを取得、内容を編集エリアに設定する</br>
 * 9.Discardボタンがタップされたら何もせずアクティビティを終了する</br>
 * 10.Saveボタンがタップされたら編集中のデータをデータベースに保存する。新規ならinserメソッド、更新ならupdateメソッドを使用する</br>
 * 11.保存が完了したらアクティビティを終了する
 * 
 * @author masahiro
 * 
 */
public class EventEditorActivity extends Activity implements OnClickListener {
	// Viewのインスタンス
	private EditText mTitleEditText = null;
	private EditText mWhereEditText = null;
	private EditText mContentEditText = null;

	private TextView mStartDateTextView = null;
	private TextView mStartTimeTextView = null;
	private TextView mEndDateTextView = null;
	private TextView mEndTimeTextView = null;
	private Button mDiscardButton = null;
	private Button mSaveButton = null;
	private CheckBox mAllDayCheckBox = null;

	private Button mRecodingButton = null;
	private boolean isRecoding = false;

	// IntentでもらったデータベースID
	private long mId = 0;
	// 日付文字列
	private String mDateString = null;

	// レコーディングファイル文字列
	private String mRecoringFileName = null;

	// レコーディング用オブジェクト
	private PlayAndRecord mPlayAndRecord = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventeditor);
		mTitleEditText = (EditText) findViewById(R.id.title);
		mWhereEditText = (EditText) findViewById(R.id.where);
		mContentEditText = (EditText) findViewById(R.id.content);
		mStartDateTextView = (TextView) findViewById(R.id.startDate);
		mStartTimeTextView = (TextView) findViewById(R.id.startTime);
		mEndDateTextView = (TextView) findViewById(R.id.endDate);
		mEndTimeTextView = (TextView) findViewById(R.id.endTime);
		mDiscardButton = (Button) findViewById(R.id.discard);
		mSaveButton = (Button) findViewById(R.id.save);
		mAllDayCheckBox = (CheckBox) findViewById(R.id.allDay);
		mRecodingButton = (Button) findViewById(R.id.recording);

		// OnClickListnerにEventEditorActivityをセットする
		mDiscardButton.setOnClickListener(this);
		mSaveButton.setOnClickListener(this);
		Intent intent = getIntent();

		mId = intent.getLongExtra(EventInfo.ID, 0);
		mDateString = intent.getStringExtra("date");

		// 新規or編集によって日付情報の取得を場合わけ
		if (mId == 0) {// 新規
			// タップした日付で今の時刻からのスケジュールとしてデータを作成する
			Calendar targetCal = EventInfo.toCalendar(mDateString);
			Calendar nowCal = new GregorianCalendar();

			// 開始日はタップした日付
			mStartDateTextView.setText(EventInfo.dateFormat.format(targetCal
					.getTime()));
			// 開始時間は現在の時間
			mStartTimeTextView.setText(EventInfo.timeFormat.format(nowCal
					.getTime()));

			// 時間を1時間加算
			nowCal.add(Calendar.HOUR, 1);

			// 終了日は開始日と同じ
			mEndDateTextView.setText(EventInfo.dateFormat.format(targetCal
					.getTime()));

			// 終了時間は開始から一時間後
			mEndTimeTextView.setText(EventInfo.timeFormat.format(nowCal
					.getTime()));

			String date = EventInfo.recordingDateFormat.format(targetCal
					.getTime());
			String time = EventInfo.recordingFileFormat
					.format(nowCal.getTime());

			mRecoringFileName = date + time + EventInfo.ext;
		} else { // 編集
					// データベースからデータを取得。ほしいデータはIDが一致する行。取得できたら編集エリアのviewに設定する
			ContentResolver contentResolver = getContentResolver();
			String selection = EventInfo.ID + " = " + mId;
			Cursor c = contentResolver.query(
					EventCalendarActivity.mResolverUri, null, selection, null,
					null);

			if (c.moveToNext()) {
				mTitleEditText.setText(c.getString(c
						.getColumnIndex(EventInfo.TITLE)));
				mWhereEditText.setText(c.getString(c
						.getColumnIndex(EventInfo.WHERE)));
				mContentEditText.setText(c.getString(c
						.getColumnIndex(EventInfo.CONTENT)));
				String startTime = c.getString(c
						.getColumnIndex(EventInfo.START_TIME));
				Calendar startCal = EventInfo.toCalendar(startTime);
				mStartDateTextView.setText(EventInfo.dateFormat.format(startCal
						.getTime()));
				mStartTimeTextView.setText(EventInfo.timeFormat.format(startCal
						.getTime()));

				String endTime = c.getString(c
						.getColumnIndex(EventInfo.END_TIME));
				Calendar endCal = EventInfo.toCalendar(endTime);

				mEndDateTextView.setText(EventInfo.dateFormat.format(endCal
						.getTime()));
				mEndTimeTextView.setText(EventInfo.timeFormat.format(endCal
						.getTime()));
				// AllDayとするかどうか
				if (startCal.get(Calendar.HOUR_OF_DAY) == 0
						&& startCal.get(Calendar.MINUTE) == 0) {
					startCal.add(Calendar.DAY_OF_MONTH, 1);
					if (startCal.equals(endCal)) {
						mStartTimeTextView.setVisibility(View.INVISIBLE);
						mEndDateTextView.setVisibility(View.INVISIBLE);
						mEndTimeTextView.setVisibility(View.INVISIBLE);
						mAllDayCheckBox.setChecked(true);
					}
				}
				mRecoringFileName = c.getString(c
						.getColumnIndex(EventInfo.RECORDING_FILE));
			}
			c.close();
		}

		mStartDateTextView.setOnClickListener(new DateOnClickListener(this));
		mEndDateTextView.setOnClickListener(new DateOnClickListener(this));
		mStartTimeTextView.setOnClickListener(new TimeOnClickListener(this));
		mEndTimeTextView.setOnClickListener(new TimeOnClickListener(this));

		mAllDayCheckBox.setOnClickListener(new AllDayOnClickListener());
		mRecodingButton.setOnClickListener(new RecordingOnClickListener());

		mPlayAndRecord = new PlayAndRecord();
	}

	/**
	 * どのボタンがタップされたかで場合わけする処理
	 */
	@Override
	public void onClick(View v) {
		if (v == mDiscardButton) {
			// 破棄
			Log.d("CALENDAR", "DISCARD");
			finish();
		} else if (v == mSaveButton) {
			// 保存
			ContentResolver contentResolver = getContentResolver();
			ContentValues values = new ContentValues();

			values.put(EventInfo.TITLE, mTitleEditText.getText().toString());
			values.put(EventInfo.WHERE, mWhereEditText.getText().toString());
			values.put(EventInfo.CONTENT, mContentEditText.getText().toString());
			if (mAllDayCheckBox.isChecked()) {
				GregorianCalendar c = EventInfo.toCalendar(EventInfo
						.toDBDateString(
								mStartDateTextView.getText().toString(),
								"00:00"));
				values.put(EventInfo.START_TIME, EventInfo.toDBDateString(c));
				c.add(Calendar.DAY_OF_MONTH, 1);
				values.put(EventInfo.END_TIME, EventInfo.toDBDateString(c));
			} else {
				values.put(EventInfo.START_TIME, EventInfo.toDBDateString(
						mStartDateTextView.getText().toString(),
						mStartTimeTextView.getText().toString()));
				values.put(EventInfo.END_TIME, EventInfo.toDBDateString(
						mEndDateTextView.getText().toString(), mEndTimeTextView
								.getText().toString()));

			}
			values.put(EventInfo.RECORDING_FILE, mRecoringFileName);
			if (mId == 0) {// 新規レコード登録
				contentResolver.insert(EventCalendarActivity.mResolverUri,
						values);
				Log.d("CALENDAR", "Insert: " + mId);
			} else {
				String where = EventInfo.ID + " = " + mId;
				contentResolver.update(EventCalendarActivity.mResolverUri,
						values, where, null);
				Log.d("CALENDAR", "Update: " + mId);
			}

			Intent intent = new Intent();
			intent.putExtra(EventCalendarActivity.CHANGED, true);
			setResult(RESULT_OK, intent);
			// 完了
			finish();
		}
	}

	/**
	 * 日付の文字列をセットされるリスナー
	 * 
	 * @author masahiro
	 * 
	 */
	private class DateOnClickListener implements OnClickListener {
		private Context mContext = null;

		// Contextが必要なので、コンストラクタで渡す
		public DateOnClickListener(Context c) {
			mContext = c;
		}

		/**
		 * クリックされた時に呼び出される
		 * 
		 * @param View
		 *            クリックされたビュー
		 */
		@Override
		public void onClick(View v) {
			GregorianCalendar c = null;
			if (v == mStartDateTextView) {
				// タップされた開始日時の文字列を取得し、カレンダーにする
				c = EventInfo.toCalendar(EventInfo.toDBDateString(
						mStartDateTextView.getText().toString(),
						mStartTimeTextView.getText().toString()));
			} else if (v == mEndDateTextView) {
				// タップされた終了日時の文字列を取得し、カレンダーにする
				c = EventInfo.toCalendar(EventInfo.toDBDateString(
						mEndDateTextView.getText().toString(), mEndTimeTextView
								.getText().toString()));
			} else {
				return;
			}

			// DatePickerDialogを作成し表示する
			DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
					new DateSetListener(v), c.get(Calendar.YEAR),
					c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.show();
		}
	}

	/**
	 * DatePickerDialogの設定を押下したときに呼び出されるメソッド
	 * 
	 * @author masahiro
	 * 
	 */
	private class DateSetListener implements OnDateSetListener {
		private View mView = null;

		// コンストラクタ
		public DateSetListener(View v) {
			mView = v;
		}

		@Override
		public void onDateSet(DatePicker picker, int y, int m, int d) {
			GregorianCalendar c = new GregorianCalendar();
			c.set(y, m, d);

			if (mView == mStartDateTextView) {
				mStartDateTextView.setText(EventInfo.dateFormat.format(c
						.getTime()));
			} else if (mView == mEndDateTextView) {
				mEndDateTextView.setText(EventInfo.dateFormat.format(c
						.getTime()));
			}
		}
	}

	/**
	 * 日付の文字列をセットされるリスナー
	 * 
	 * @author masahiro
	 * 
	 */
	private class TimeOnClickListener implements OnClickListener {
		private Context mContext = null;

		// Contextが必要なので、コンストラクタで渡す
		public TimeOnClickListener(Context c) {
			mContext = c;
		}

		/**
		 * クリックされた時に呼び出される
		 * 
		 * @param View
		 *            クリックされたビュー
		 */
		@Override
		public void onClick(View v) {
			GregorianCalendar c = null;
			if (v == mStartTimeTextView) {
				// タップされた開始日時の文字列を取得し、カレンダーにする
				c = EventInfo.toCalendar(EventInfo.toDBDateString(
						mStartDateTextView.getText().toString(),
						mStartTimeTextView.getText().toString()));
			} else if (v == mEndTimeTextView) {
				// タップされた終了日時の文字列を取得し、カレンダーにする
				c = EventInfo.toCalendar(EventInfo.toDBDateString(
						mEndDateTextView.getText().toString(), mEndTimeTextView
								.getText().toString()));
			} else {
				return;
			}

			// TimePickerDialogを作成し表示する
			TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
					new TimeSetListener(v), c.get(Calendar.HOUR_OF_DAY),
					c.get(Calendar.MINUTE), true);
			timePickerDialog.show();
		}
	}

	/**
	 * TimePickerDialogの設定を押下したときに呼び出されるメソッド
	 * 
	 * @author masahiro
	 * 
	 */
	private class TimeSetListener implements OnTimeSetListener {
		private View mView = null;

		// コンストラクタ
		public TimeSetListener(View v) {
			mView = v;
		}

		@Override
		public void onTimeSet(TimePicker picker, int h, int m) {
			GregorianCalendar c = new GregorianCalendar();
			c.set(Calendar.HOUR_OF_DAY, h);
			c.set(Calendar.MINUTE, m);

			if (mView == mStartTimeTextView) {
				mStartTimeTextView.setText(EventInfo.timeFormat.format(c
						.getTime()));
			} else if (mView == mEndTimeTextView) {
				mEndTimeTextView.setText(EventInfo.timeFormat.format(c
						.getTime()));
			}
		}
	}

	/**
	 * AllDayチェックボックスのリスナー
	 * 
	 * @author masahiro
	 * 
	 */
	private class AllDayOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (((CheckBox) v).isChecked()) {
				// AlldayがON
				mStartTimeTextView.setVisibility(View.INVISIBLE);
				mEndDateTextView.setVisibility(View.INVISIBLE);
				mEndTimeTextView.setVisibility(View.INVISIBLE);
			} else {
				mStartTimeTextView.setVisibility(View.VISIBLE);
				mEndDateTextView.setVisibility(View.VISIBLE);
				mEndTimeTextView.setVisibility(View.VISIBLE);
			}

		}

	}

	/**
	 * レコーディングボタンが押されたときのリスナークラス
	 */
	private class RecordingOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (isRecoding) {
				((Button) v).setText(PlayAndRecord.recording);
				mPlayAndRecord.stopRecorder();
				message("stop recording");
				isRecoding = false;
			} else {
				((Button) v).setText(PlayAndRecord.nowRecofing);
				message("start recording");
				mPlayAndRecord.startRecorder(mRecoringFileName);
				isRecoding = true;
			}
		}
	}

	/**
	 * メッセージ表示用メソッド
	 * 
	 * @param text
	 */
	private void message(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
}