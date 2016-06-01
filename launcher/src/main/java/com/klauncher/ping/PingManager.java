package com.klauncher.ping;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.dl.statisticalanalysis.MobileStatistics;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by yanni on 16/3/27.
 */
public class PingManager {

    public static final String KEY_PING_TYPE = "pty";
    public static final String KEY_PING_TIMESTAMP = "ptm";

    //public static final String USER_ACTION_CLICK = "1";
    public static final String USER_ACTION_CLICK = "klauncher_action_clickl";
   // public static final String USER_ACTION_UNINSTALL = "2";
    public static final String USER_ACTION_UNINSTALL = "klauncher_action_uninstall";
    //public static final String USER_ACTION_INSTALL = "3";
    public static final String USER_ACTION_INSTALL = "klauncher_action_install";

    private static final int MAX_BUFFER_SIZE = 10;
    private static final String SP_PING = "ping";
    private static final String KEY_PING_APPLIST_LAST_REPORT = "lst_rpt_app";
    private static final int MIN_REPORT_INTERVAL = 12 * 3600 * 1000; //12 hours

    private static PingManager sPingManager = new PingManager();
    public static PingManager getInstance() {
        return sPingManager;
    }

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private PingHandler.PingFailCallback mPingFailCallback = new PingHandler.PingFailCallback() {
        @Override
        public void onFailed(final List<Map<String, String>> dataMap) {
            synchronized (mPingDataList) {
                mPingDataList.addAll(dataMap);
            }
        }
    };

    private List<Map<String, String>> mPingDataList = new ArrayList<>();

    private PingManager() {}

    public void init(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(SP_PING, Context.MODE_PRIVATE);
    }

    public void ping(int action, Map<String, String> value) {
        if (value == null) {
            value = new HashMap<>();
        }
        value.put(KEY_PING_TYPE, String.valueOf(action));
        value.put(KEY_PING_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        synchronized (mPingDataList) {
            mPingDataList.add(value);
            if (needSend() && isNetworkAvailable()) {
                sendPingRequest();
            }
        }
    }

    private boolean needSend() {
        return mPingDataList.size() > MAX_BUFFER_SIZE;
    }

    private void sendPingRequest() {
        PingHandler handler = new PingHandler(mContext, mPingFailCallback);
        handler.requestPing(mPingDataList);
        mPingDataList.clear();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager= (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        // report under wifi & mobile
        NetworkInfo networkInfo= manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    private String getApkFileSFCrc32(String sourceDir) {
        long crc = 0xffffffff;

        try {
            File f = new File(sourceDir);
            ZipFile z = new ZipFile(f);
            Enumeration<? extends ZipEntry> zList = z.entries();
            ZipEntry ze;
            while (zList.hasMoreElements()) {
                ze = zList.nextElement();
                if (ze.isDirectory()
                        || ze.getName().toString().indexOf("META-INF") == -1
                        || ze.getName().toString().indexOf(".SF") == -1) {
                    continue;
                } else {
                    crc = ze.getCrc();
                    break;
                }
            }
            z.close();
        } catch (Exception e) {
        }
        return Long.toHexString(crc);
    }

    public static String getFileMD5(String sourceDir) {
        File file = new File(sourceDir);
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public void reportUserAction4App(String action, String packageName) {
        Map<String, String> pingMap = new HashMap<>();
        pingMap.put("action", action);
        pingMap.put("source", "1");
        pingMap.put("pkg_name", packageName);
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);
            pingMap.put("version_code", String.valueOf(packageInfo.versionCode));
            pingMap.put("crc", getApkFileSFCrc32(applicationInfo.sourceDir));
            pingMap.put("md5", getFileMD5(applicationInfo.sourceDir));
            // ping(1, pingMap);
            //sdk 埋点变更
            MobileStatistics.onEvent(action, pingMap);

        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    public void reportLauncherAppList(String xml) {
        Map<String, String> pingMap = new HashMap<>();
        pingMap.put("launcher_info", xml);
        ping(2, pingMap);
        mSharedPreferences.edit().putLong(KEY_PING_APPLIST_LAST_REPORT, System.currentTimeMillis()).commit();
    }

    public boolean needReportLauncherAppList() {
        return System.currentTimeMillis() - mSharedPreferences.getLong(
                KEY_PING_APPLIST_LAST_REPORT, 0) > MIN_REPORT_INTERVAL;
    }

    //------------

    public static final String USER_ACTION_CLICK_HOTWORD = "click_hotword";
    public static final String USER_ACTION_CLICK_BANNER = "click_banner";
    public static final String USER_ACTION_CLICK_CARD_MORE = "click_card_more";
    public static final String USER_ACTION_CLICK_CARD_CHANGE = "click_card_change";
    public static final String USER_ACTION_CLICK_SEARCHBOX = "click_search_icon";
    public static final String USER_ACTION_CLICK_CARD_NEWS_OPEN = "click_open_card_news";
    public static final String USER_ACTION_CLICK_CARD_NAVIGATION= "click_navigation";

    public static final String KEY_HOTWORD_CONTENT_FROM= "hotWordFrom";
    public static final String KEY_PULL_UP_APP_PACKAGENAME= "pullUpAppPackageName";
    public static final String KEY_PULL_UP_APP_CRC32= "pullUpAppCRC32";
    public static final String KEY_CARD_CONTENT_FROM= "cardContentFrom";
    public static final String KEY_CARD_SECOND_TYPE_ID= "cardSecondTypeId";
    public static final String KEY_NAVIGATION_URL= "navigationUrl";
    public static final String KEY_BANNER_FROM= "bannerFrom";
    public static final String VALUE_HOTWORD_CONTENT_FROM_BAIDU= "baidu";
    public static final String VALUE_HOTWORD_CONTENT_FROM_SHENMA= "shenma";
    public static final String VALUE_CARD_CONTENT_FROM_YIDIANZIXUN= "yiDianZiXun";
    public static final String VALUE_CARD_CONTENT_FROM_JINRITOUTIAO= "jinRiTouTiao";

    static boolean kinflowIsUsed;

    /**
     * 上报热词被点击
     * 热词来源,调起包名,crc32
     */
    public Map<String, String> reportUserAction4HotWord(HotWord hotWord,String pullUpAppPackageName){
        kinflowIsUsed = true;
        Map<String, String> pingMap = new HashMap<>();
        //添加action
//        pingMap.put("action", USER_ACTION_CLICK_HOTWORD);
        //添加来源
        int from = hotWord.getType();
        String fromName = from==1?VALUE_HOTWORD_CONTENT_FROM_BAIDU:VALUE_HOTWORD_CONTENT_FROM_SHENMA;
        pingMap.put(KEY_HOTWORD_CONTENT_FROM,fromName);
        //添加包名
        pingMap.put(KEY_PULL_UP_APP_PACKAGENAME,pullUpAppPackageName);
        //添加src32
        String src32 = getApkFilesCRC32FromPackageName(pullUpAppPackageName);
        pingMap.put(KEY_PULL_UP_APP_CRC32, src32);
//        toLog("热词被点击上报信息:", pingMap);
        MobileStatistics.onEvent(USER_ACTION_CLICK_HOTWORD,pingMap);
        return  pingMap;
    }

    /**
     * @param openType 打开方式新闻客户端||浏览器||内置浏览器
     * @param pullUpAppPackageName:目标客户端包名
     * @param fromName 来源标识:adview||今日头条||一点咨询
     * @return
     */
    public Map<String, String> reportUserAction4Banner(String pullUpAppPackageName,String fromName){
        kinflowIsUsed = true;
        Map<String, String> pingMap = new HashMap<>();
        //添加action
//        pingMap.put("action", USER_ACTION_CLICK_BANNER);
        //添加打开方式
//        pingMap.put("BannerOpenType",openType);
        //添加目标客户端包名
        pingMap.put(KEY_PULL_UP_APP_PACKAGENAME,pullUpAppPackageName);
        //添加目标客户端crc32
        String crc32 = getApkFilesCRC32FromPackageName(pullUpAppPackageName);
        pingMap.put(KEY_PULL_UP_APP_CRC32, crc32);
        //添加来源标识
        pingMap.put(KEY_BANNER_FROM,fromName);
//        toLog("Banner被点击上报信息:",pingMap);
        MobileStatistics.onEvent(USER_ACTION_CLICK_BANNER, pingMap);
        return  pingMap;
    }

    /**
     * 上报用户点击
     * @param pullUpAppPackageName
     * @param fromName
     * @return
     */
    public Map<String, String> reportUserAction4CardMore(String pullUpAppPackageName,String fromName){
        kinflowIsUsed = true;
        Map<String, String> pingMap = new HashMap<>();
        //添加action
//        pingMap.put("action", USER_ACTION_CLICK_CARD_MORE);
        //添加目标客户端包名
        pingMap.put(KEY_PULL_UP_APP_PACKAGENAME,pullUpAppPackageName);
        //添加目标客户端crc32
        String crc32 = getApkFilesCRC32FromPackageName(pullUpAppPackageName);
        pingMap.put(KEY_PULL_UP_APP_CRC32, crc32);
        //添加来源标识
        pingMap.put(KEY_CARD_CONTENT_FROM,fromName);
//        toLog("CardMore被点击上报信息:",pingMap);
        MobileStatistics.onEvent(USER_ACTION_CLICK_CARD_MORE,pingMap);
        return  pingMap;
    }

    public Map<String, String> reportUserAction4Changes(String fromName,String cardSecondTypeId){
        kinflowIsUsed = true;
        Map<String, String> pingMap = new HashMap<>();
        //添加action
//        pingMap.put("action", USER_ACTION_CLICK_CARD_CHANGE);
        //添加cardType
        pingMap.put(KEY_CARD_SECOND_TYPE_ID,cardSecondTypeId);
        //添加来源标识
        pingMap.put(KEY_CARD_CONTENT_FROM,fromName);
//        toLog("CardChange被点击上报信息:",pingMap);
        MobileStatistics.onEvent(USER_ACTION_CLICK_CARD_CHANGE, pingMap);
        return  pingMap;
    }

    public Map<String, String> reportUserAction4SearchBox(HotWord hotWord,String pullUpAppPackageName){
        kinflowIsUsed = true;
        Map<String, String> pingMap = new HashMap<>();
        //添加action
//        pingMap.put("action", USER_ACTION_CLICK_SEARCHBOX);
        //添加来源
        int from = hotWord.getType();
        String fromName = from==1?VALUE_HOTWORD_CONTENT_FROM_BAIDU:VALUE_HOTWORD_CONTENT_FROM_SHENMA;
        pingMap.put(KEY_HOTWORD_CONTENT_FROM,fromName);
        //添加包名
        pingMap.put(KEY_PULL_UP_APP_PACKAGENAME,pullUpAppPackageName);
        //添加src32
        String crc32 = getApkFilesCRC32FromPackageName(pullUpAppPackageName);
        pingMap.put(KEY_PULL_UP_APP_CRC32, crc32);
//        toLog("搜索框被点击上报信息:",pingMap);
        MobileStatistics.onEvent(USER_ACTION_CLICK_SEARCHBOX,pingMap);
        return  pingMap;
    }

    public Map<String, String> reportUserAction4cardNewsOpen(String fromName,String cardSecondTypeId){
        kinflowIsUsed = true;
        Map<String, String> pingMap = new HashMap<>();
        //添加action
//        pingMap.put("action", USER_ACTION_CLICK_CARD_NEWS_OPEN);
        //添加cardType
        pingMap.put(KEY_CARD_SECOND_TYPE_ID,cardSecondTypeId);
        //添加来源标识
        pingMap.put(KEY_CARD_CONTENT_FROM, fromName);
//        toLog("新闻被点击上报信息:",pingMap);
        MobileStatistics.onEvent(USER_ACTION_CLICK_CARD_NEWS_OPEN,pingMap);
        return  pingMap;
    }

    public Map<String, String> reportUserAction4Navigation(Navigation navigation){
        kinflowIsUsed = true;
        Map<String, String> pingMap = new HashMap<>();
        //添加action
//        pingMap.put("action", USER_ACTION_CLICK_CARD_NAVIGATION);
        //添加来源标识
        pingMap.put(KEY_NAVIGATION_URL, navigation.getNavUrl());
//        toLog("新闻被点击上报信息:",pingMap);
        MobileStatistics.onEvent(USER_ACTION_CLICK_CARD_NAVIGATION, pingMap);
        return  pingMap;
    }

    public void toLog(String actionName,Map<String, String> pingMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : pingMap.entrySet()) {
            sb.append(entry.getKey()).append(" : "+entry.getValue()).append("\n");
        }
        Log.i("MyInfo",actionName+sb.toString());
    }

    public String getApkFilesCRC32FromPackageName (String pullUpAppPackageName) {
        String src32 = "";
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(
                    pullUpAppPackageName, PackageManager.GET_META_DATA);
            src32 = getApkFileSFCrc32(applicationInfo.sourceDir);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return src32;
    }
}
