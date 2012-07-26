package com.example.calendar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.provider.BaseColumns;

/**
 * 定数クラス gdはgmailの接頭辞。
 * 
 * @author masahiro
 * 
 */
public class EventInfo {
	public static final String DB_NAME = "events";
	public static final String ID = BaseColumns._ID;
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String WHERE = "gd_where";
	public static final String END_TIME = "gd_when_endTime";
	public static final String START_TIME = "gd_when_startTime";
	public static final String RECORDING_FILE = "gd_recording_file";

	public static final int HOUR_BY_MINUTES = 60;
	public static final int MINUTE_BY_SECONDS = 60;
	public static final int SECOND_BY_MILLI = 1000;
	public static final int MINUTE_BY_MILLI = MINUTE_BY_SECONDS
			* SECOND_BY_MILLI;

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd", Locale.JAPAN);
	public static final SimpleDateFormat recordingDateFormat = new SimpleDateFormat(
			"yyyy_MM_dd", Locale.JAPAN);

	public static final SimpleDateFormat timeFormat = new SimpleDateFormat(
			"HH:mm", Locale.JAPAN);
	public static final SimpleDateFormat recordingFileFormat = new SimpleDateFormat(
			"_HH_mm_ss", Locale.JAPAN);

	public static final SimpleDateFormat RFC822MilliDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.JAPAN);

	public static final String ext = ".3gp";

	// DBフィールド用メンバ変数
	private long mId;
	private String mTitle;
	private String mWhere;
	private String mContent;
	private GregorianCalendar mStart;
	private GregorianCalendar mEnd;
	private String mRecordingFile;

	/**
	 * 日付、時刻の文字列から、DBに保存するための文字列への変換を行うユーティリティメソッド
	 * 
	 * @param date
	 *            日付文字列
	 * @param time
	 *            時刻文字列
	 * @return DB保存用文字列
	 */
	public static String toDBDateString(String date, String time) {
		StringBuilder sb = new StringBuilder();
		sb.append(date);
		sb.append("T");
		sb.append(time);
		sb.append(":00.000");
		sb.append(timeZoneToString(TimeZone.getDefault()));
		return sb.toString();
	}

	/**
	 * タイムゾーンの文字列を生成する
	 * 
	 * @param tz
	 * @return
	 */
	public static String timeZoneToString(TimeZone tz) {
		Calendar cal = Calendar.getInstance();
		String dir = null;

		// TimeZoneからミリ秒単位のUTCからのずれを取得
		int offset = tz.getRawOffset();
		if (offset < 0) {
			offset = -offset;
			dir = "-";
		} else if (offset > 0) {
			dir = "-";
		} else if (offset == 0) {
			return "Z";
		}

		// 時刻情報へ変換する
		int offsetMin = offset / MINUTE_BY_MILLI;
		int offsetHour = offsetMin / HOUR_BY_MINUTES;
		offsetMin = offsetMin % 60;
		cal.set(Calendar.HOUR_OF_DAY, offsetHour);
		cal.set(Calendar.MINUTE, offsetMin);

		// 正負の符号を追加した文字列を返す
		return dir + timeFormat.format(cal.getTime());
	}

	/**
	 * カレンダーからDBに保存する文字列を作成する
	 * 
	 * @param cal
	 * @return
	 */
	public static String toDBDateString(Calendar cal) {
		// RFC 822形式で文字列を生成
		String dateStr = RFC822MilliDateFormat.format(cal.getTime());

		// タイムゾーン部分を処理
		if (dateStr.matches(".+[+-][0-9]{4}$")) {
			dateStr = dateStr.replaceAll("([+-][0-9]{2})([0-9]{2})", "$1:$2");
		}
		return dateStr;
	}

	/**
	 * 日時文字列からカレンダーへの変換
	 * 
	 * @param startTime
	 * @return　Calendar
	 */
	public static GregorianCalendar toCalendar(String startTime) {
		GregorianCalendar calendar = new GregorianCalendar();

		if (startTime == null) {
			return calendar;
		}

		// 数字以外の文字で文字列を区切る
		String[] strs = startTime.split("[^0-9]");
		TimeZone timeZone = TimeZone.getDefault();

		if (startTime.matches("^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$")) {
			// 日付のみの文字列（時刻を00:00に設定）
			calendar.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			calendar.set(Calendar.MONTH, Integer.valueOf(strs[1]) - 1); // calendarでは、月を0-11で表現する
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(strs[2]));
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.setTimeZone(timeZone);
		} else {
			calendar.set(Calendar.YEAR, Integer.valueOf(strs[0]));
			calendar.set(Calendar.MONTH, Integer.valueOf(strs[1]) - 1); // calendarでは、月を0-11で表現する
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(strs[2]));
			calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(strs[3]));
			calendar.set(Calendar.MINUTE, Integer.valueOf(strs[4]));
			calendar.set(Calendar.SECOND, Integer.valueOf(strs[5]));
			calendar.set(Calendar.MILLISECOND, Integer.valueOf(strs[6]));

			// タイムゾーンのパターンによる処理
			if (startTime.matches(".+Z$")) {
				// UTC
				timeZone.setRawOffset(0);
			} else if (startTime.matches(".+\\+[0-9][0-9]:[0-9][0-9]$")) {
				// オフセットがプラス
				timeZone.setRawOffset((Integer.valueOf(strs[7])
						* HOUR_BY_MINUTES + Integer.valueOf(strs[8]))
						* MINUTE_BY_MILLI);
			} else if (startTime.matches(".+-[0-9][0-9]:[0-9][0-9]$")) {
				// オフセットがマイナス
				timeZone.setRawOffset(-(Integer.valueOf(strs[7])
						* HOUR_BY_MINUTES + Integer.valueOf(strs[8]))
						* MINUTE_BY_MILLI);
			}
			calendar.setTimeZone(timeZone);
		}
		return calendar;
	}

	/**
	 * データベースに含まれる情報を1つの文字列として出力
	 * 
	 * @return 文字列
	 */
	public String toString() {
		if (mStart.get(Calendar.HOUR_OF_DAY) == 0
				&& mStart.get(Calendar.MINUTE) == 0) {
			mStart.add(Calendar.DAY_OF_MONTH, 1);
			if (mStart.equals(mEnd)) {
				return getTitle() + "\n" + getStartDateString() + "\n"
						+ getWhere() + "\n" + getContent();
			}
		}

		return getTitle() + "\n" + getStartDateString() + " "
				+ getStartTimeString() + "\n" + getEndDateString() + " "
				+ getEndTimeString() + "\n" + getWhere() + "\n" + getContent();
	}

	/*
	 * setter and getter
	 */

	public long getId() {
		return mId;
	}

	public void setId(long mId) {
		this.mId = mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getWhere() {
		return mWhere;
	}

	public void setWhere(String mWhere) {
		this.mWhere = mWhere;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String mContent) {
		this.mContent = mContent;
	}

	public GregorianCalendar getStart() {
		return mStart;
	}

	public String getStartString() {
		return toDBDateString(mStart);
	}

	public String getStartDateString() {
		return dateFormat.format(mStart.getTime());
	}

	public String getStartTimeString() {
		return timeFormat.format(mStart.getTime());
	}

	public void setStart(GregorianCalendar mStart) {
		this.mStart = mStart;
	}

	public void setStart(String dateString) {
		this.mStart = toCalendar(dateString);
	}

	public GregorianCalendar getEnd() {
		return mEnd;
	}

	public String getEndString() {
		return toDBDateString(mEnd);
	}

	public String getEndDateString() {
		return dateFormat.format(mEnd.getTime());
	}

	public String getEndTimeString() {
		return timeFormat.format(mEnd.getTime());
	}

	public void setEnd(GregorianCalendar mEnd) {
		this.mEnd = mEnd;
	}

	public void setEnd(String dateString) {
		this.mEnd = toCalendar(dateString);
	}

	public String getRecordingFile() {
		return mRecordingFile;
	}

	public void setRecordingFile(String mRecordingFile) {
		this.mRecordingFile = mRecordingFile;
	}
}
