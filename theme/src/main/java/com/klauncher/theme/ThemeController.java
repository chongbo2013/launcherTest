package com.klauncher.theme;

import com.klauncher.theme.apktheme.ApkTheme;
import com.klauncher.theme.ziptheme.ZipTheme;
import com.klauncher.theme.settings.SettingsProvider;
import com.klauncher.theme.util.ThemeLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class ThemeController extends BroadcastReceiver{
	public static final String LOGTAG = "ThemeController";
	public static final String ACTION_LAUNCHER_THEME_FORCE_RELOAD_LAUNCHER = "action_themecenter_themechange_lelauncher_force_reload_launcher";
	public static final String ACTION_LAUNCHER_THEME_ZIP = "action_themecenter_themechange_lelauncher_zip";
	public static final String ACTION_LAUNCHER_THEME_APK = "action_themecenter_themechange_lelauncher_apk";
	public static final String ACTION_LAUNCHER_THEME_TYPE = "ThemeType";
    public static final String ACTION_LAUNCHER_THEME_PATH = "ThemePath";
    public static final String ACTION_LAUNCHER_THEME_PACKAGE = "ThemePackage";
    public static final String EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK = "EnableThemeMask";
    public static final String EXTRA_THEME_TYPE_ZIP = "ThemeTypeZip";
    public static final String EXTRA_THEME_TYPE_APK = "ThemeTypeApk";
    public static final String EXTRA_THEME_APP_ICON_SIZE = "AppIconSize";
    public static final String EXTRA_THEME_APP_ICON_TEXTURE_SIZE = "AppIconTextureSize";

	//public static final String DEFAULT_APK_THEME = "com.klauncher.theme.colorful";
	public static final String DEFAULT_ZIP_THEME_FOLDER = Environment.getExternalStorageDirectory() + File.separator + "ktheme";
	public static final String DEFAULT_ZIP_THEME = DEFAULT_ZIP_THEME_FOLDER + File.separator + "default.ktm";
	public static final String UI_ZIP_THEME = DEFAULT_ZIP_THEME_FOLDER + File.separator + "theme.ktm";
	
    private ITheme mTheme = null;
    private Context mContext = null;
	private boolean mUseUiTheme = false;
    
    public ITheme getTheme(){
    	return mTheme;
    }
    
	public ThemeController(Context context, boolean useUiTheme) {
		super();
		mContext = context.getApplicationContext();
		mUseUiTheme = useUiTheme;
		
		// zip theme
		IntentFilter filter = new IntentFilter(ACTION_LAUNCHER_THEME_ZIP);
		mContext.registerReceiver(this, filter);
		
		// apk theme
		filter = new IntentFilter(ACTION_LAUNCHER_THEME_APK);
		mContext.registerReceiver(this, filter);

		try {
			//mTheme = new ApkTheme(mContext, DEFAULT_APK_THEME);
			if (useUiTheme) {
				mTheme = new ZipTheme(mContext, UI_ZIP_THEME);
			} else {
				mTheme = new ZipTheme(mContext, DEFAULT_ZIP_THEME);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
        ThemeLog.d(LOGTAG, "onReceive intent=" + action);
        String themeInfo = "";
        boolean enableThemeMask = true;
        try{
	        //add for receive zip theme apply broadcast start
	        if(ACTION_LAUNCHER_THEME_ZIP.equals(action)){
	        	/* adb shell am broadcast -a action_themecenter_themechange_lelauncher_zip --es ThemePath data/data/com.lenovo.xlauncher/res.zip */
	        	themeInfo = intent.getStringExtra(ACTION_LAUNCHER_THEME_PATH);
	        	enableThemeMask = intent.getBooleanExtra(EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK, true);
				if (mUseUiTheme) {
					mTheme = new ZipTheme(mContext, UI_ZIP_THEME);
				} else {
					mTheme = new ZipTheme(mContext, DEFAULT_ZIP_THEME);
				}
	        }else if(ACTION_LAUNCHER_THEME_APK.equals(action)){
	        	/* adb shell am broadcast -a action_themecenter_themechange_lelauncher_apk --es ThemePackage com.lenovo.launcher.theme.lovecorner */
	        	themeInfo = intent.getStringExtra(ACTION_LAUNCHER_THEME_PACKAGE);
	        	enableThemeMask = intent.getBooleanExtra(EXTRA_LAUNCHER_THEME_ENABLE_THEME_MASK, true);
	        	mTheme = new ApkTheme(mContext, themeInfo);
	        }
        } catch (Exception e) {
        	ThemeLog.i(LOGTAG, "Error happened in init Theme!");
        	mTheme = null;
        }
        String currentTheme = SettingsProvider.getStringCustomDefault(mContext, SettingsProvider.SETTINGS_CURRENT_THEME, "");
        ThemeLog.i(LOGTAG, "currentTheme:" + currentTheme + ",themeInfo:" + themeInfo + ", enableThemeMask:" + enableThemeMask);
        if(null != mTheme){
        	// true表明已经加载过此主题,仅仅需要重新retart Launcher即可
        	boolean justRestartLauncher = currentTheme.equals(themeInfo);
        	if(!justRestartLauncher){
            	// false表明第一次加载此主题,需要记录当前主题信息,同时需要重启Launcher
            	SettingsProvider.putString(mContext, SettingsProvider.SETTINGS_CURRENT_THEME, themeInfo);
        	}
            mTheme.handleTheme(justRestartLauncher, enableThemeMask);
        }
	}

}
