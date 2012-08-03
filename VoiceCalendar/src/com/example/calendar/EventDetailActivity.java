package com.example.calendar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;
import android.database.Cursor;

/**
 * 一日分のイベント詳細を表示するクラス
 * 
 * @author masahiro
 * 
 */
public class EventDetailActivity extends Activity implements OnItemLongClickListener{
	private String mDateString = null;

	// private ArrayList<Long> mEventIds = null;

	// EventListViewのインスタンス
	private ListView mEventListView = null;

	// event editor　を起動するためのrequest code
	protected static final int EVENT_EDITOR = 3;

	// 新規イベント追加メニュー用ID
	private static final int NEW_EVENT_MENU_ID = 1;

	// 削除Id
	private long mDeleteId = 0;
	
	/**
	 * タイトルの日付を表示
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventdetail);

		Intent intent = getIntent();

		mDateString = intent.getStringExtra("date");

		TextView dateView = (TextView) findViewById(R.id.detailDate);
		dateView.setText(mDateString);

		mEventListView = (ListView) findViewById(R.id.eventList);
		setListAdapter();
		mEventListView.setOnItemLongClickListener(this);

		// eventListViewのアイテムをクリックされた時の処理をセット

		/*
		mEventListView
				.setOnItemLongClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						
					}
					*/

					/*
					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						Intent intent = new Intent(EventDetailActivity.this,
								EventEditorActivity.class);

						// ArrayAdapterの型をEventInfoクラスにまとめる EventInfo event =
						// (EventInfo)
						parent.getAdapter().getItem(position);

						//intent.putExtra(EventInfo.ID, mEventIds.get(position));
						//intent.putExtra(EventInfo.ID, event.getId());

						intent.putExtra("date", mDateString);
						startActivityForResult(intent, EVENT_EDITOR);
					}

				});
				*/

	}

	private void setListAdapter() {
		// TODO ArrayAdapterを継承したクラスを作り、ボタンを設定することで、ListViewにボタンが出てくる
		/*
		 * mEventListView.setAdapter(new ArrayAdapter<EventInfo>(this,
		 * android.R.layout.simple_list_item_1, getEventDetail(mDateString)));
		 */
		mEventListView.setAdapter(new ListAdapter(this,
				android.R.layout.simple_list_item_1,
				getEventDetail(mDateString)));
	}

	/**
	 * このメソッドは、ArrayAdapter継承クラスの中で使える
	 */
	private ArrayList<EventInfo> getEventDetail(String date) {
		// イベントの詳細
		ArrayList<EventInfo> events = new ArrayList<EventInfo>();

		// イベントのID
		/*
		 * mEventIds = new ArrayList<Long>();
		 */
		// コンテントリゾルバの初期化
		ContentResolver contentResolver = getContentResolver();
		String selection = EventInfo.START_TIME + " LIKE ?";
		String[] selectionArgs = { mDateString + "%" };
		String sortOrder = EventInfo.START_TIME;

		Cursor c = contentResolver.query(EventCalendarActivity.mResolverUri,
				null, selection, selectionArgs, sortOrder);

		while (c.moveToNext()) {
			EventInfo event = new EventInfo();
			event.setId(c.getLong(c.getColumnIndex(EventInfo.ID)));
			event.setTitle(c.getString(c.getColumnIndex(EventInfo.TITLE)));
			event.setStart(c.getString(c.getColumnIndex(EventInfo.START_TIME)));
			event.setEnd(c.getString(c.getColumnIndex(EventInfo.END_TIME)));
			event.setWhere(c.getString(c.getColumnIndex(EventInfo.WHERE)));
			event.setContent(c.getString(c.getColumnIndex(EventInfo.CONTENT)));
			event.setRecordingFile(c.getString(c
					.getColumnIndex(EventInfo.RECORDING_FILE)));
			events.add(event);
		}
		c.close();
		return events;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EVENT_EDITOR && resultCode == RESULT_OK) {
			if (data.getBooleanExtra(EventCalendarActivity.CHANGED, false)) {
				setListAdapter();
				Intent intent = new Intent();
				intent.putExtra(EventCalendarActivity.CHANGED, true);
				setResult(RESULT_OK, intent);
			}
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, NEW_EVENT_MENU_ID, Menu.NONE, R.string.newEvent);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == NEW_EVENT_MENU_ID) {
			Intent intent = new Intent(EventDetailActivity.this,
					EventEditorActivity.class);
			// 新規
			intent.putExtra(EventInfo.ID, 0);
			intent.putExtra("date", mDateString);
			startActivityForResult(intent, EVENT_EDITOR);
		}
		return true;
	}

	/**
	 * リストの要素にボタンを配置するためのクラス
	 * 
	 */
	public class ListAdapter extends ArrayAdapter<EventInfo> {
		private LayoutInflater mInflater;
		private TextView mTitle;
		private Button mEditButton;
		private Button mPlayButton;
		private List<EventInfo> mEvents;

		public ListAdapter(Context context, int textViewResourceId,
				List<EventInfo> events) {
			super(context, textViewResourceId, events);
			mInflater = getLayoutInflater();
			mEvents = events;
		}

		// @Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.detaillist, null);
			}
			final EventInfo item = this.getItem(position);

			// return super.getView(position, convertView, parent);
			mTitle = (TextView) convertView.findViewById(R.id.nameText);
			mTitle.setText(item.getTitle());
			mEditButton = (Button) convertView.findViewById(R.id.editButton);
			mEditButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(EventDetailActivity.this,
							EventEditorActivity.class);
					intent.putExtra(EventInfo.ID, item.getId());

					intent.putExtra("date", mDateString);
					startActivityForResult(intent, EVENT_EDITOR);
				}
			});

			mPlayButton = (Button) convertView.findViewById(R.id.playButton);
			mPlayButton.setText(PlayAndRecord.play);
			final PlayAndRecord mPlayAndRecord = new PlayAndRecord();
			mPlayButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/*
					 * Intent intent = new Intent(EventDetailActivity.this,
					 * EventEditorActivity.class); intent.putExtra(EventInfo.ID,
					 * item.getId());
					 * 
					 * intent.putExtra("date", mDateString);
					 * startActivityForResult(intent, EVENT_EDITOR);
					 */
					// テスト用のファイルパス
					String sdcardpath = "sdcard";
					String fileName = item.getRecordingFile();
					String testPath = File.separator + sdcardpath
							+ File.separator + fileName;
					if (!mPlayAndRecord.getStates()) {
						mPlayAndRecord.play(testPath);
						message("Start Play");
						((Button) v).setText(PlayAndRecord.stop);
					} else {
						mPlayAndRecord.stop(testPath);
						message("Stop Play");
						((Button) v).setText(PlayAndRecord.play);
					}

				}
			});
			return convertView;
		}
	}

	private void message(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		EventInfo item = (EventInfo)parent.getAdapter().getItem(position);
		mDeleteId = item.getId();
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		
		alertDialogBuilder.setTitle(R.string.deleteConfirm);
		
		alertDialogBuilder.setPositiveButton(R.string.deleteOK, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContentResolver contentResolver = getContentResolver();
				String selection = EventInfo.ID+" = "+mDeleteId;
				Cursor c = contentResolver.query(
						EventCalendarActivity.mResolverUri, null, selection, null,
						null);

				if (c.moveToNext()) {
					String fileName = c.getString(c.getColumnIndex(EventInfo.RECORDING_FILE));
					String path = File.separator + PlayAndRecord.sdcardpath + File.separator
							+ fileName;
					
					File deleteFile = new File(path);
					deleteFile.delete();
				}
				
				contentResolver.delete(EventCalendarActivity.mResolverUri, selection, null);
				setListAdapter();
				
				Intent intent = new Intent();
				intent.putExtra(EventCalendarActivity.CHANGED, true);
				setResult(RESULT_OK, intent);
			}
		});
		
		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Cancelの場合は何もしない
			}
		});

		alertDialogBuilder.setCancelable(true);
		
		AlertDialog alertDialog = alertDialogBuilder.create();
		
		alertDialog.show();
		return true;
	}
	
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

	private void actionMove(MotionEvent event){
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
