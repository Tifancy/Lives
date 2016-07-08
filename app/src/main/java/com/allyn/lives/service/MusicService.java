package com.allyn.lives.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.allyn.lives.events.MusicBeamEvent;
import com.allyn.lives.manage.PlayMainage;
import com.allyn.lives.utils.Config;
import com.allyn.lives.utils.RxBus;

import java.util.Random;

public class MusicService extends Service {

    private final IBinder mBinder = new MyBinder();

    public static int servicePosition = -1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int startCommand = super.onStartCommand(intent, flags, startId);
        Bundle bundle = intent.getBundleExtra(Config.bunder);
        int position = bundle.getInt(Config.position, -1);
        if (servicePosition == position) {
            if (PlayMainage.mediaPlayer.isPlaying()) {
                PlayMainage.pause();
                RxBus.getDefault().post(new MusicBeamEvent(servicePosition));
            } else {
                PlayMainage.play(position);
                RxBus.getDefault().post(new MusicBeamEvent(servicePosition));
            }
        } else {
            //更新当前播放音乐下标，
            servicePosition = position;
            PlayMainage.stop();

            PlayMainage.play(position).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //这是当前音乐播放结束之后的监听，先判断播放类型，在播放音乐，
                    switch (PlayMainage.Code) {
                        case PlayMainage.ORDER:
                            //判断是不是最后一首歌曲
                            if (servicePosition == PlayMainage.getMusicSize() - 1) {
                                servicePosition = 0;
                            }
                            servicePosition++;
                            break;
                        case PlayMainage.RANDOM:
                            servicePosition = new Random().nextInt(PlayMainage.getMusicSize() - 1);
                            break;
                        case PlayMainage.REPOT:
                            break;
                    }
                    PlayMainage.stop();
                    PlayMainage.play(servicePosition);
                    RxBus.getDefault().post(new MusicBeamEvent(servicePosition));
                }
            });
            //发送当前播放音乐下标，
            RxBus.getDefault().post(new MusicBeamEvent(servicePosition));
        }

        return startCommand;
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}