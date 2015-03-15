package com.washappkorea.corp.cleanbasket.ui;


import android.app.ActionBar;
import android.os.Bundle;

import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.ui.dialog.EmailDialog;

public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        final ActionBar bar = getActionBar();

        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(false);

        setContentView(R.layout.activity_main);

        EmailDialog ed = EmailDialog.newInstance();

        ed.show(getSupportFragmentManager(), "ed");
    }
}
