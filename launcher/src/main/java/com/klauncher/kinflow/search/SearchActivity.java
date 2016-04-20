package com.klauncher.kinflow.search;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klauncher.launcher.R;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.task.AsynchronousGet;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.search.adapter.HotWordsRecyclerViewAdapter;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.search.model.HotWordItemDecoration;
import com.klauncher.kinflow.weather.listener.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xixionghui on 2016/3/15.
 */
public class SearchActivity extends Activity {


    TextView searchOrCancle;
    EditText editSearch;
    RelativeLayout searchHeader;
    RecyclerView hotwordRecycler;

    public static final String HITN_HOT_WORD_KEY = "hint_hot_word_ley";
    HotWord hintHotWord;
    List<HotWord> recyclerViewHotWordList;

    private Handler handler;
    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.arg1) {
                case AsynchronousGet.CONNECTION_ERROR:
                    Toast.makeText(SearchActivity.this, getResources().getString(R.string.kinflow_string_connection_error), Toast.LENGTH_SHORT).show();
                    break;
                case AsynchronousGet.RESPONSE_FAIL:
                    Toast.makeText(SearchActivity
                            .this, getResources().getString(R.string.kinflow_string_response_fail), Toast.LENGTH_SHORT).show();
                    break;
                case AsynchronousGet.OBTAIN_RESULT_NULL:
                    Toast.makeText(SearchActivity
                            .this, getResources().getString(R.string.kinflow_string_obtain_result_null), Toast.LENGTH_SHORT).show();
                    break;
                case AsynchronousGet.PARSE_ERROR:
                    Toast.makeText(SearchActivity
                            .this, getResources().getString(R.string.kinflow_string_parse_error), Toast.LENGTH_SHORT).show();
                    break;
                case AsynchronousGet.SUCCESS:

                    switch (msg.what) {
                        case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD://获取百度热词
                            ArrayList<HotWord> hotWordArrayList = (ArrayList<HotWord>) msg.obj;
//                            hintHotWord = hotWordArrayList.get(0);
                            recyclerViewHotWordList =  hotWordArrayList.subList(0, 12);
                            editSearch.setHint(hintHotWord.getWord());
                            hotwordRecycler.setAdapter(new HotWordsRecyclerViewAdapter(SearchActivity.this, recyclerViewHotWordList));
                            break;

                    }
                    break;
            }

            switch (msg.arg1) {
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        hintHotWord = getIntent().getParcelableExtra(HITN_HOT_WORD_KEY);
        editSearch.setHint(hintHotWord.getWord());
        editSearch.setFocusable(true);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int hintLength = editSearch.getHint().length();
                int editLength = editSearch.getText().length();
                if (hintLength > 0 || editLength>0) {
                    editSearch.setHint("");
                    searchOrCancle.setTag("search");
                    searchOrCancle.setText("搜索");
                } else {
                    searchOrCancle.setTag("cancle");
                    searchOrCancle.setText("取消");
                }
            }
        });
        //HotWordItemDecoration(int hSpacing, int vSpacing, boolean includeEdge)
        hotwordRecycler.addItemDecoration(new HotWordItemDecoration(16, 16, false));
        hotwordRecycler.setLayoutManager(new GridLayoutManager(SearchActivity.this, 2));
        hotwordRecycler.addOnItemTouchListener(new RecyclerItemClickListener(SearchActivity.this,hotwordRecycler,new RecyclerItemClickListener.OnItemClickListener(){

            @Override
            public void onItemClick(View view, int position) {
                HotWord hotWord = recyclerViewHotWordList.get(position);
                CommonUtils.getInstance().openHotWord(SearchActivity.this,hotWord.getUrl());
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(SearchActivity.this, (position + "  onItemLongClick"), Toast.LENGTH_SHORT).show();
            }
        }));
        handler = new Handler(callback);
        asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
    }

    private void initView() {
        searchOrCancle = (TextView) findViewById(R.id.search_or_cancle);
        editSearch = (EditText) findViewById(R.id.edit_search);
        searchHeader = (RelativeLayout) findViewById(R.id.search_header);
        hotwordRecycler = (RecyclerView) findViewById(R.id.hotword_recycler);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.search_or_cancle:
                if (searchOrCancle.getTag().equals("search")) {
                    String searchContext = editSearch.getText().toString();
                    String hintContext = (String) editSearch.getHint();
                    String context = TextUtils.isEmpty(searchContext)?hintContext:searchContext;
                    if (TextUtils.isEmpty(context)) {
                        Toast.makeText(this, "请输入搜索词", Toast.LENGTH_SHORT).show();
                    } else {
                        CommonUtils.getInstance().openHotWord(this,Const.URL_SEARCH_WITH_BAIDU + context);
                    }
                } else if (searchOrCancle.getTag().equals("cancle")) {
                    finish();
                }
                break;
        }
    }


    /**
     * 可请求一个或者多个
     *
     * @param msgWhats
     */
    public void asynchronousRequest(int... msgWhats) {
        for (int what : msgWhats) {
            try {
                switch (what) {
                    case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                        new AsynchronousGet(handler, MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD).run(Const.URL_HOT_WORD);
                        break;
                    case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION:
                        new AsynchronousGet(handler, MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION).run(Const.NAVIGATION_GET);
                        break;
                    case MessageFactory.MESSAGE_WHAT_OBTAION_CARD:
                        new AsynchronousGet(handler, MessageFactory.MESSAGE_WHAT_OBTAION_CARD).run(Const.CARD_GET);
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
