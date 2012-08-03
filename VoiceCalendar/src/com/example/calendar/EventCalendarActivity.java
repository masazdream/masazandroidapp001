package com.example.calendar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.example.calendar.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;

/**
 * カレンダー表示用アクティビティクラス
 * 
 * @author masahiro
 * 
 */
public class EventCalendarActivity extends Activity implements OnClickListener {
	// db changed
	public static final String CHANGED = "changed";

	private static final int DAYS_OF_WEEK = 7;
	private GridView mGridView = null;
	private DateCellAdapter mDateCellAdapter = null;

	// カレンダー表示
	private GregorianCalendar mCalendar = null;
	private TextView mYearMonthTextView = null;

	// DBアクセス
	private ContentResolver mContentResolver = null;
	public static final Uri mResolverUri = Uri
			.parse("content://com.example.calendar.eventprovider");

	// 前・次月のボタンを作成
	private Button mPrevMonthButton = null;
	private Button mNextMonthButton = null;

	// EventDetailActivityを呼び出すためのrequestコード
	protected static final int EVENT_DETAIL = 2;

	// X位置の動作判断
	private static float MARGIN_X = 15.0f;
	// Y位置の動作判断
	private static float MARGIN_Y = 15.0f;
	// 触れた瞬間のX位置
	private float mDownX;
	// 触れた瞬間のY位置
	private float mDownY;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// GridViewのカラム数を設定、GridViewにアダプタをセットする。
		mGridView = (GridView) findViewById(R.id.gridView1);
		mGridView.setNumColumns(DAYS_OF_WEEK);
		mDateCellAdapter = new DateCellAdapter(this);
		mGridView.setAdapter(mDateCellAdapter);

		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// カレンダーをコピー
				Calendar cal = (Calendar) mCalendar.clone();
				// positionから日付を計算する
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.add(Calendar.DAY_OF_MONTH,
						position - cal.get(Calendar.DAY_OF_WEEK) + 1);

				String dateString = EventInfo.dateFormat.format(cal.getTime());

				Intent intent = new Intent(EventCalendarActivity.this,
						EventDetailActivity.class);
				intent.putExtra("date", dateString);
				startActivityForResult(intent, EVENT_DETAIL);
			}
		});
		
		mGridView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
			    switch(action){
			    case MotionEvent.ACTION_UP: // 離れた
			    	actionUp(event);
			        break;
			    case MotionEvent.ACTION_DOWN: // 触れた
			    	actionDown(event);
			        break;
			    case MotionEvent.ACTION_MOVE: // 動いた
			        //actionMove(event);
			        break;
			    case MotionEvent.ACTION_CANCEL: // ?よくわかりません
			        break;
			    case MotionEvent.ACTION_OUTSIDE: // 範囲外に移動
			        break;
			    }
			    //return super.onTouchEvent(event);
			    //ここで画面を動かすという判断の場合は、trueを返す。
			    //画面を動かさない場合には、falseを返す。するとonItemClickメソッドへと移る
				return false;
			}
		});

		// 年月を表示するTextViewを取得
		mYearMonthTextView = (TextView) findViewById(R.id.yearMonth);
		mCalendar = new GregorianCalendar();

		int year = mCalendar.get(Calendar.YEAR);
		// 日本では月を数字の1で表すため、基底の0に1を足しておく
		int month = mCalendar.get(Calendar.MONTH) + 1;
		mYearMonthTextView.setText(year + "/" + month);
		mYearMonthTextView.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));

		// コンテントリゾルバの初期化
		mContentResolver = getContentResolver();

		// ボタンにViewを設定
		mPrevMonthButton = (Button) findViewById(R.id.prevMonth);
		mPrevMonthButton.setOnClickListener(this);
		mNextMonthButton = (Button) findViewById(R.id.nextMonth);
		mNextMonthButton.setOnClickListener(this);
	}

	/**
	 * 内部クラス DateCellAdapter
	 */
	public class DateCellAdapter extends BaseAdapter {
		private static final int NUM_ROWS = 6;
		private static final int NUM_OF_CELLS = DAYS_OF_WEEK * NUM_ROWS;
		private LayoutInflater mLayoutInflater = null;

		/**
		 * コンストラクタ
		 */
		DateCellAdapter(Context context) {
			// LayoutInflater レイアウトビューをプログラム中で作成する際にしようする
			mLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		/**
		 * 1か月分のcell数　NUM_OF_CELLSを返す
		 */
		@Override
		public int getCount() {
			return NUM_OF_CELLS;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		/**
		 * 最も重要なメソッド
		 * 
		 * ビューに表示する内容を決定して、作成する
		 * 
		 * @param position
		 *            セルの位置
		 * @param convertView
		 *            前に使用したView
		 * @param parent
		 *            親ビュー　ここではGridView
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.datecell, null);

			}
			// viewの最小の高さ
			convertView.setMinimumHeight(parent.getHeight() / NUM_ROWS - 1);
			// 日付の欄には、現在のpositionを設定（仮）
			TextView dayOfMonthView = (TextView) convertView
					.findViewById(R.id.dayOfMonth);
			// dayOfMonthView.setText("" + position);

			// カレンダーの日付を割り出す
			// まず、カレンダーの該当月の1日に設定する
			Calendar cal = (Calendar) mCalendar.clone();
			cal.set(Calendar.DAY_OF_MONTH, 1);

			// position - 曜日の値（1～7） + 1 とすると、positionに表示すべきに日付となる
			// positionは0～6を一行、7～13が２行目といったような値。Calendarクラスのaddメソッドで増減をする
			cal.add(Calendar.DAY_OF_MONTH,
					position - cal.get(Calendar.DAY_OF_WEEK) + 1);
			dayOfMonthView.setText("" + cal.get(Calendar.DAY_OF_MONTH));

			// 背景色
			if (position % 7 == 0) {
				dayOfMonthView.setBackgroundColor(R.color.red);
			} else if (position % 7 == 6) {
				dayOfMonthView.setBackgroundColor(R.color.blue);
			} else {
				dayOfMonthView.setBackgroundColor(R.color.gray);
			}

			// スケジュール欄は、schedule+positionを入れておく
			/*
			 * TextView scheduleView = (TextView) convertView
			 * .findViewById(R.id.schedule); scheduleView.setText("Schedule" +
			 * position);
			 */
			TextView scheduleView = (TextView) convertView
					.findViewById(R.id.schedule);
			// 取得するフィールド名のString配列
			String[] projection = { EventInfo.TITLE };

			// selectionはwhere節
			String selection = EventInfo.START_TIME + " LIKE ?";
			String[] selectionArgs = { EventInfo.dateFormat.format(cal
					.getTime()) + "%" };
			String sortOrder = EventInfo.START_TIME;

			Cursor c = mContentResolver.query(mResolverUri, projection,
					selection, selectionArgs, sortOrder);
			StringBuilder sb = new StringBuilder();
			while (c.moveToNext()) {
				sb.append(c.getString(c.getColumnIndex(EventInfo.TITLE)));
				sb.append("\n");
			}
			c.close();

			scheduleView.setText(sb.toString());
			return convertView;
		}
	}

	@Override
	public void onClick(View v) {
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		if (v == mPrevMonthButton) {
			mCalendar.add(Calendar.MONTH, -1);

		} else if (v == mNextMonthButton) {
			mCalendar.add(Calendar.MONTH, 1);
		}

		mYearMonthTextView.setText(mCalendar.get(Calendar.YEAR) + "/"
				+ (mCalendar.get(Calendar.MONTH) + 1));
		mDateCellAdapter.notifyDataSetChanged();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EVENT_DETAIL && resultCode == RESULT_OK) {
			if (data.getBooleanExtra(EventCalendarActivity.CHANGED, false)) {
				mDateCellAdapter.notifyDataSetChanged();
			}
		}
	}
/*
    public boolean onInterceptTouchEvent(MotionEvent event)  {
        // タッチされたらまずonInterceptTouchEventが呼ばれる
        // ここでtrueを返せば親ViewのonTouchEvent
        // ここでfalseを返せば子ViewのonClickやらonLongClickやら
         
        return false;
    }
    */
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    int action = event.getAction();
	    switch(action){
	    case MotionEvent.ACTION_UP: // 離れた
	        break;
	    case MotionEvent.ACTION_DOWN: // 触れた
	        break;
	    case MotionEvent.ACTION_MOVE: // 動いた
	        actionMove(event);
	        break;
	    case MotionEvent.ACTION_CANCEL: // ?よくわかりません
	        break;
	    case MotionEvent.ACTION_OUTSIDE: // 範囲外に移動
	        break;
	    }
	    return super.onTouchEvent(event);
	}

	private void actionDown(MotionEvent event){
	    // 位置を保存
	    mDownX = event.getX();
	    mDownY = event.getY();
	    // テスト用にイベントの情報を画面に表示
	    //TextView tv = (TextView)findViewById(R.id.TextView01);
	    /*
	    StringBuilder sb = new StringBuilder();
	    sb.append("\n");
	    sb.append("action[");
	    sb.append(event.getAction());
	    sb.append("] time[");
	    sb.append(event.getEventTime());
	    sb.append("] (x,y)=(");
	    sb.append(mDownX);
	    sb.append(",");
	    sb.append(mDownY);
	    sb.append(")");
	    //tv.setText(sb.substring(0));
	    Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
	    */
	}    

	private void actionUp(MotionEvent event){
	    // 現在位置と ACTION_DOWN 時の位置を比較
	    float moveX = Math.abs(mDownX - event.getX());
	    float moveY = Math.abs(mDownY - event.getY());
	    // 移動距離が範囲内なら無視
	    if(moveX < MARGIN_X && moveY < MARGIN_Y){
	        return;
	    }
	    // テスト用にイベントの情報を画面に表示
	    StringBuilder sb = new StringBuilder();
	    sb.append("\n");
	    sb.append("action[");
	    sb.append(event.getAction());
	    sb.append("] time[");
	    sb.append(event.getEventTime());
	    sb.append("] (x,y)=(");
	    sb.append(event.getX());
	    sb.append(",");
	    sb.append(event.getY());
	    sb.append(")");
	    
	    Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * DOWNとUP時の座標比較のみで十分なので使用しない
	 * 
	 * @param event
	 */
	private void actionMove(MotionEvent event){
		   // 現在位置と ACTION_DOWN 時の位置を比較
	    float moveX = Math.abs(mDownX - event.getX());
	    float moveY = Math.abs(mDownY - event.getY());

	    // 移動距離が範囲内なら無視
	    if(moveX < MARGIN_X && moveY < MARGIN_Y){
	       return;
	    }

	    // テスト用にイベントの情報を画面に表示
	    //TextView tv = (TextView)findViewById(R.id.TextView01);
	    StringBuilder sb = new StringBuilder();
	    sb.append("\n");
	    sb.append("action[");
	    sb.append(event.getAction());
	    sb.append("] time[");
	    sb.append(event.getEventTime());
	    sb.append("] (x,y)=(");
	    sb.append(event.getX());
	    sb.append(",");
	    sb.append(event.getY());
	    sb.append(")");
	    //tv.setText(sb.substring(0));
	    
	    Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
	}
}