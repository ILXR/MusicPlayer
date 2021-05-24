package com.epic.localmusic.WebSocket;

import com.epic.localmusic.BlueTooth.Params;
import com.epic.localmusic.util.Constant;
import com.epic.localmusic.util.Constant.ConnectStatus;

import android.support.annotation.Nullable;
import android.util.Log;

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
            .pingInterval(20, TimeUnit.SECONDS)
            .build();

    private static WebSocketHandler Instance;

    MockWebServer mockWebServer;

    public WebSocketHandler() {
        this.webSocketURL = Constant.WebSocketUrl;
    }

    public WebSocketHandler(String webSocketURL) {
        this.webSocketURL = webSocketURL;
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
            Log.i(TAG, "send: " + text);
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
        this.status = ConnectStatus.Open;
        send("测试WebSocket！");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.i(TAG, "onMessage: " + text);
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
        try {
            //断线重连
            Thread.sleep(3000);
            reConnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}