package com.epic.localmusic.WebSocket;

import com.epic.localmusic.activity.PlayActivity;
import com.epic.localmusic.receiver.PlayerManagerReceiver;
import com.epic.localmusic.service.MusicPlayerService;
import com.epic.localmusic.util.EpicParams;
import com.epic.localmusic.util.EpicParams.ConnectStatus;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.epic.localmusic.util.MusicConstant;
import com.epic.localmusic.util.MyApplication;
import com.epic.localmusic.util.MyMusicUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.ByteString;


public class WebSocketHandler extends WebSocketListener {
    private static final String TAG = "WebSocketHandler ";

    private WebSocket webSocket;

    private ConnectStatus status;

    private String webSocketURL;

    private OkHttpClient client = new OkHttpClient.Builder()
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            //.pingInterval(20, TimeUnit.SECONDS)
            .build();

    private static WebSocketHandler Instance;

    private AudioManager mAudioManager;

    MockWebServer mockWebServer;

    //控制音乐播放的本地广播
    private LocalBroadcastManager playMusicBroadcastManager;

    public WebSocketHandler() {
        this.webSocketURL = EpicParams.WS_SERVER_URL;
        // 获取系统音量管理器
        mAudioManager = (AudioManager) MyApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public WebSocketHandler(String webSocketURL) {
        this.webSocketURL = webSocketURL;
        mAudioManager = (AudioManager) MyApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public static WebSocketHandler getInstance() {
        if (Instance == null) {
            synchronized (WebSocketHandler.class) {
                Instance = new WebSocketHandler();
            }
        }
        return Instance;
    }

    public void setUrl(String webSocketURL) {
        this.webSocketURL = webSocketURL;
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public void testConnect() {
        mockWebServer = new MockWebServer();
        //Handle message async.
        mockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.i(TAG, "server onOpen");
                Log.i(TAG, "server request header:" + response.request().headers());
                Log.i(TAG, "server response header:" + response.headers());
                Log.i(TAG, "server response:" + response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String string) {
                //Log.i(TAG,"web socket server message:" + string);
                //webSocket.send("response-" + string);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.i(TAG, "server onClosing");
                Log.i(TAG, "code:" + code + " reason:" + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.i(TAG, "server onClosed");
                Log.i(TAG, "code:" + code + " reason:" + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.i(TAG, "server onFailure");
                Log.i(TAG, "throwable:" + t);
                Log.i(TAG, "response:" + response);
            }

        }));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort();
                setUrl(url);
                connect();
            }
        }).start();
    }

    public void connect() {
        //构造request对象
        if (webSocket != null) {
            webSocket.cancel();
            status = ConnectStatus.Canceled;
        }
        Request request = new Request.Builder()
                .url(webSocketURL)
                .build();
        webSocket = client.newWebSocket(request, this);
        status = ConnectStatus.Connecting;
    }

    public void reConnect() {
        if (webSocket != null) {
            webSocket.cancel();
            status = ConnectStatus.Canceled;
            Request request = new Request.Builder()
                    .url(webSocketURL)
                    .build();
            webSocket = client.newWebSocket(request, this);
            status = ConnectStatus.Connecting;
        } else {
            Log.e(TAG, "reConnect: Has been initialized");
        }
    }

    public void send(String text) {
        if (webSocket != null) {
            //Log.i(TAG, "send: " + text);
            webSocket.send(text);
        }
    }

    public void send(byte[] array) {
        if (webSocket != null) {
            Log.i(TAG, "send byte array: length " + array.length);
            webSocket.send(new ByteString(array));
        }
    }

    public void send(ByteString bytes) {
        webSocket.send(bytes);
    }

    public void cancel() {
        if (webSocket != null) {
            webSocket.cancel();
            status = ConnectStatus.Canceled;
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, null);
            status = ConnectStatus.Closed;
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.i(TAG, "onOpen: ");
        MyMusicUtil.showToast("服务器连接成功");
        this.status = ConnectStatus.Open;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.i(TAG, "onMessage: " + text);

        // TODO 处理服务器返回的操控命令
        //数据格式 {'actindex': 4, 'actname': 'QP'}

        // 解析json数据
        JSONObject json = JSONObject.parseObject(text);
        int actIndex = json.getInteger("actindex");
        switch (actIndex) {
            case 2:
                // pinch2
                Log.i(TAG, "onMessage: 音量加");
                MyMusicUtil.showToast("音量增大");
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                break;
            case 1:
                // pinch1
                Log.i(TAG, "onMessage: 音量减");
                MyMusicUtil.showToast("音量减小");
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                break;
            case 0:
                // crab
                Log.i(TAG, "onMessage: 播放暂停");
                MyMusicUtil.showToast("播放/暂停");
                MyApplication.getInstance().play();
                break;
            case 3:
                // pinch3
                Log.i(TAG, "onMessage: 切歌");
                MyMusicUtil.showToast("下一首");
                MyMusicUtil.playNextMusic(MyApplication.getContext());
                break;
            case 4:
                // TODO 播放列表模式
                Log.i(TAG, "onMessage: 播放列表模式");
                MyMusicUtil.showToast("切换播放模式");
                int playMode = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_MODE);
                if (playMode == MusicConstant.PLAYMODE_RANDOM)
                    MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_SINGLE_REPEAT);
                else if (playMode == MusicConstant.PLAYMODE_SINGLE_REPEAT)
                    MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_SEQUENCE);
                else
                    MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_RANDOM);
                MyApplication.getInstance().sendBroadCast(MusicConstant.UPDATE_PLAY_MODE);
                break;
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        this.status = ConnectStatus.Closing;
        Log.i(TAG, "onClosing: ");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.i(TAG, "onClosed: ");
        this.status = ConnectStatus.Closed;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        Log.i(TAG, "onFailure: " + t.toString());
        t.printStackTrace();
        this.status = ConnectStatus.Canceled;
        MyMusicUtil.showToast("服务器连接失败");
        try {
            //断线重连
            Thread.sleep(5000);
            reConnect();
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Log.e(TAG, "onFailure");
        }
    }
}