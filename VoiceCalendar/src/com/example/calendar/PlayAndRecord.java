package com.example.calendar;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.widget.Button;
import android.widget.Toast;

/**
 * 音声の再生と録音を担当するクラス
 * 
 * @author masahiro
 * 
 */
public class PlayAndRecord {
	public static final String play = "play";
	public static final String stop = "stop";
	public static final String recording = "recording";
	public static final String nowRecofing = "stop recording";

	public static final String sdcardpath = "sdcard";

	private final MediaPlayer player = new MediaPlayer();
	private final MediaRecorder recorder = new MediaRecorder();

	private boolean nowPlaying = false;

	/**
	 * 指定された音声ファイルを再生する
	 * 
	 * @param path
	 *            　音声ファイル
	 * 
	 */
	public void play(String path) {
		if (!nowPlaying) {
			try {
				player.setDataSource(path);
				player.prepare();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			nowPlaying = true;
			player.start();
		}
	}

	public void stop(String path) {
		if (nowPlaying) {
			player.stop();
			nowPlaying = false;
			player.reset();
		}
	}

	public boolean getStates() {
		return nowPlaying;
	}

	public void startRecorder(String fileName) {
		try {
			String path = File.separator + sdcardpath + File.separator
					+ fileName;
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(path);
			recorder.prepare();
			recorder.start(); // Recording is now started
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopRecorder() {
		recorder.stop();
	}
}
