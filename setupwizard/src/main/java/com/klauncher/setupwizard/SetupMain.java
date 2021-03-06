package com.klauncher.setupwizard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.klauncher.setupwizard.hw.HwLanguageSettings;
import com.klauncher.setupwizard.vivo.VivoSetupActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hw on 17-3-21.
 */

public class SetupMain {
    public static final String TAG = "SetupMain";

    public static final String SP_SETUP = "sp_setup";

    public static final String SP_SETUP_START = "sp_setup_start";

    public static final String SP_SETUP_OVER = "sp_setup_over";

    private static final String HUAWEI_FLAVOR = "huawei";
    private static final String VIVO_FLAVOR = "vivo";
    private static final String OPPO_FLAVOR = "oppo";

    public static List<Activity> activityList = new ArrayList<>();

    public static void startSetup(Context context, String flavor) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SP_SETUP, Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean(SP_SETUP_START, false) && !sharedPreferences.getBoolean(SP_SETUP_OVER, false)) {

                if (TextUtils.equals(flavor, HUAWEI_FLAVOR)) {
                    Intent intent = new Intent();
                    intent.setClass(context, HwLanguageSettings.class);
                    context.startActivity(intent);
                } else if (TextUtils.equals(flavor, VIVO_FLAVOR)) {
                    Intent intent = new Intent();
                    intent.setClass(context, VivoSetupActivity.class);
                    context.startActivity(intent);
                } else if (TextUtils.equals(flavor, OPPO_FLAVOR)) {
//                    Intent intent = new Intent();
//                    intent.setClass(context, OppoLanguageSettings.class);
//                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {

        }

    }

}
