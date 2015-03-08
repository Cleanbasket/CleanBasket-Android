package com.washappkorea.corp.cleanbasket.ui;


import android.os.Bundle;

import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.ui.dialog.EmailDialog;

public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EmailDialog ed = EmailDialog.newInstance();

        ed.show(getSupportFragmentManager(), "ed");
    }
}
