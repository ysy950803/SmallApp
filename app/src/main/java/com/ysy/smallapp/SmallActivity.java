package com.ysy.smallapp;

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
    }
}
