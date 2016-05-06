/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.klauncher.kinflow.common.task;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.search.model.SearchEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class SearchAsynchronousGet {

    public static final int CONNECTION_ERROR = -1;//没有连接到服务器：网络错误||服务器维护
    public static final int SUCCESS = 0;//获取数据成功
    public static final int PARSE_ERROR = 1;//解析数据失败
    public static final int OBTAIN_RESULT_NULL = 2;//获取到服务器端的响应数据，但是数据为空
    public static final int RESPONSE_FAIL = 3;//服务器端有响应，但是出现错误：如404,500-----服务器响应失败，请稍后重试

//    private final OkHttpClient client = new OkHttpClient();
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build();
    private final Gson gson = new GsonBuilder().create();
    private Handler handler;

    private Message msg;

    public SearchAsynchronousGet(Handler handler, int messageWhat) {
        this.handler = handler;
        msg = MessageFactory.createMessage(messageWhat);
    }

    public void run(final SearchEnum searchEnum) throws Exception {
        Request request = new Request.Builder()
                .url(searchEnum.getUrl_obtainHotword())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                msg.arg1 = CONNECTION_ERROR;
                handler.sendMessage(msg);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    msg.arg1 = RESPONSE_FAIL;
                    handler.sendMessage(msg);
                    throw new IOException("Unexpected code " + response);
                }
                switch (msg.what) {
                    case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                        switch (searchEnum) {
                            case BAIDU:
                                parseBaiduHotWord(response.body().string());
                                break;
                            case SHENMA:
                                break;
                        }
                        break;
                }
                response.body().close();
            }
        });
    }


    /**
     * 解析百度热词
     *
     * @param responseBody
     */
    void parseBaiduHotWord(String responseBody) {
        Log.i("AsynchronousGet", ("百度热词响应体:  " + responseBody));
        List<HotWord> hotWordList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONObject jsonHot = jsonObject.getJSONObject("hot");

            int jsonHotLength = jsonHot.length();

            if (jsonObject.isNull("hot") || jsonHot.length() == 0) {//没有获取到热词列表
                msg.arg1 = OBTAIN_RESULT_NULL;
            } else {
                for (int i = 1; i <= jsonHotLength; i++) {
                    JSONObject jsonHotWord = jsonHot.getJSONObject(String.valueOf(i));
                    hotWordList.add(new HotWord(String.valueOf(i), jsonHotWord.getString("word"), jsonHotWord.getString("url")));
                }
                msg.arg1 = SUCCESS;
                msg.obj = hotWordList;
            }
        } catch (JSONException e) {
            msg.arg1 = PARSE_ERROR;
        } finally {
            handler.sendMessage(msg);
        }
    }


}
