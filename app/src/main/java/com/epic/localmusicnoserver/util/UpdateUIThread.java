package com.epic.localmusicnoserver.util;

import android.content.Context;
import android.content.Intent;

import com.epic.localmusicnoserver.fragment.PlayBarFragment;
import com.epic.localmusicnoserver.receiver.PlayerManagerReceiver;


/**
 * 循环发送广播，更改歌曲的播放进度
 */
public class UpdateUIThread extends Thread {

	private int threadNumber;

	private Context context;

	private PlayerManagerReceiver playerManagerReceiver;

	private int duration;

	private int currentPosition;

	private int oldCurrent;


	public UpdateUIThread(PlayerManagerReceiver playerManagerReceiver, Context context, int threadNumber) {
		this.playerManagerReceiver = playerManagerReceiver;
		this.context = context;
		this.threadNumber = threadNumber;
	}


	@Override
	public void run() {
		try {
			while (playerManagerReceiver.getThreadNumber() == this.threadNumber) {
				if (playerManagerReceiver.status == MusicConstant.STATUS_STOP) {
					break;
				}
				if (playerManagerReceiver.status == MusicConstant.STATUS_PLAY || playerManagerReceiver.status == MusicConstant.STATUS_PAUSE) {
					if (!playerManagerReceiver.getMediaPlayer().isPlaying()) {
						break;
					}
					duration = playerManagerReceiver.getMediaPlayer().getDuration();
					currentPosition = playerManagerReceiver.getMediaPlayer().getCurrentPosition();
					Intent intent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlAYBAR);
					intent.putExtra(MusicConstant.STATUS, MusicConstant.STATUS_RUN);  //状态
					intent.putExtra(MusicConstant.KEY_DURATION, duration);  //播放长度
					intent.putExtra(MusicConstant.KEY_CURRENT, currentPosition);  //播放进度
					context.sendBroadcast(intent);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}

