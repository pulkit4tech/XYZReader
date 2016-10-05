package com.example.xyzreader.ui;

import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by Sneha Khadatare : 587823
 * on 8/29/2016.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
