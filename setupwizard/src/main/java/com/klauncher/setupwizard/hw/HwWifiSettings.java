package com.klauncher.setupwizard.hw;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.klauncher.setupwizard.BaseActivity;
import com.klauncher.setupwizard.R;
import com.klauncher.setupwizard.common.WifiInfoManager;

import java.util.List;

/**
 * Created by hw on 17-3-21.
 */

public class HwWifiSettings extends BaseActivity implements View.OnClickListener, WifiInfoManager.OnWifiListener {

    private RecyclerView recyclerView;
    private MyRecyclerAdapter recycleAdapter;
    //    private WifiManager wifiManager;
//    private List<ScanResult> list;

    private CheckSwitchButton switchButton;

    private TextView backTv;
    private TextView nextTv;
    private WifiInfoManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hw_wifi_layout);
//        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        list = wifiManager.getScanResults();
        manager = new WifiInfoManager(this);
        manager.init();
        manager.setOnWifiListener(this);
        initUi();
    }

    private void initUi() {
        backTv = (TextView) findViewById(R.id.hw_wifi_back);
        nextTv = (TextView) findViewById(R.id.hw_wifi_next);
        backTv.setOnClickListener(this);
        nextTv.setOnClickListener(this);
        switchButton = (CheckSwitchButton) findViewById(R.id.hw_wifi_switch);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    wifiManager.setWifiEnabled(true);
                    manager.openWifi();
                } else {
//                    wifiManager.setWifiEnabled(false);
                    manager.closeWifi();
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
//        if (wifiManager.isWifiEnabled()) {
        if (manager.isWifiEnabled()) {
            switchButton.setChecked(true);
        } else {
            switchButton.setChecked(false);
        }
        recyclerView = (RecyclerView) findViewById(R.id.hw_wifi_list);
//        recycleAdapter = new MyRecyclerAdapter(HwWifiSettings.this, list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
//        recyclerView.setAdapter(recycleAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.hw_wifi_back) {
            finish();
        } else if (i == R.id.hw_wifi_next) {
            Intent intent = new Intent();
            intent.setClass(this, HwAgreement.class);
            startActivity(intent);
        }
    }

    @Override
    public void onWifiEnabled() {

    }

    @Override
    public void onWifiDisabling() {

    }

    @Override
    public void onWifiDisEnabled() {

    }

    @Override
    public void onWifiEnabling() {

    }

    @Override
    public void onWifiUnknown() {

    }

    @Override
    public void onWifiConnected(WifiInfo info) {

    }

    @Override
    public void onWifiDisconnected() {

    }

    @Override
    public void onWifiScanSuccess(List<ScanResult> results) {
        if (recyclerView.getVisibility() != View.VISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
        }
        recycleAdapter = new MyRecyclerAdapter(HwWifiSettings.this, results);
        recyclerView.setAdapter(recycleAdapter);
    }

    @Override
    public void onWifiScanNone() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != manager) {
            manager.onDestory();
        }
    }

    private class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {

        private List<ScanResult> mDatas;
        private Context mContext;
        private LayoutInflater inflater;

        public MyRecyclerAdapter(Context context, List<ScanResult> datas) {
            this.mContext = context;
            this.mDatas = datas;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getItemCount() {

            return mDatas.size();
        }

        //填充onCreateViewHolder方法返回的holder中的控件
        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            ScanResult scanResult = mDatas.get(position);
            Log.d("hw", scanResult.toString());
            holder.nameTv.setText(scanResult.SSID);
//            holder.protectTv.setText(scanResult.capabilities);
//            holder.nameTv.setText();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            //判断信号强度，显示对应的指示图标
            int sigLevel = WifiManager.calculateSignalLevel(
                    scanResult.level, 100);
            if (sigLevel > 90) {
                holder.signalTv.setImageDrawable(getResources().getDrawable(R.drawable.hw_stat_sys_wifi_signal_4));
            } else if (sigLevel > 80) {
                holder.signalTv.setImageDrawable(getResources().getDrawable(R.drawable.hw_stat_sys_wifi_signal_3));
            } else if (sigLevel > 60) {
                holder.signalTv.setImageDrawable(getResources().getDrawable(R.drawable.hw_stat_sys_wifi_signal_2));
            } else if (sigLevel > 40) {
                holder.signalTv.setImageDrawable(getResources().getDrawable(R.drawable.hw_stat_sys_wifi_signal_1));
            } else
                holder.signalTv.setImageDrawable(getResources().getDrawable(R.drawable.hw_stat_sys_wifi_signal_0));
        }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.hw_wifi_item_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        //            TextView protectTv;
        ImageView signalTv;

        public MyViewHolder(View view) {
            super(view);
            nameTv = (TextView) view.findViewById(R.id.hw_wifi_item_name);
//                protectTv=(TextView) view.findViewById(R.id.hw_wifi_item_protect);
            signalTv = (ImageView) view.findViewById(R.id.hw_wifi_item_signal);
        }

    }
}
}
