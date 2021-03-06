/*
 * Copyright (c) 2016 咖枯 <kaku201313@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.kapp.knews.helper.measure;


import com.kapp.knews.helper.view.DisplayHelper;

/**
 * @author 咖枯
 * @version 1.0 2016/7/7
 */
public class DimenUtil {
    public static float dp2px(float dp) {
        final float scale = DisplayHelper.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static float sp2px(float sp) {
        final float scale = DisplayHelper.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static int getScreenSize() {
//        return App.getAppContext().getResources().getDisplayMetrics().widthPixels;
        return DisplayHelper.getWidthPixels();
    }
}
