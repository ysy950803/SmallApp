package com.ysy.smallapp;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SmallActivity extends AppCompatActivity {

    public static class Small0 extends SmallActivity {
    }

    public static class Small1 extends SmallActivity {
    }

    public static class Small2 extends SmallActivity {
    }

    public static class Small3 extends SmallActivity {
    }

    public static class Small4 extends SmallActivity {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_small);

        // 动态地给小程序Activity设置名称和图标，下面代码只是举例，实际信息肯定是动态获取的
        // 由于iconRes这个构造参数的API 28才加入的，所以建议区分版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int iconRes = 0; // 这里应该是小程序图标的资源索引
            setTaskDescription(new ActivityManager.TaskDescription("小程序名", iconRes));
        } else {
            Bitmap iconBmp = null; // 这里应该是小程序图标的bitmap
            setTaskDescription(new ActivityManager.TaskDescription("小程序名", iconBmp));
        }
    }
}
