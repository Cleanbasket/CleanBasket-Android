package com.bridge4biz.laundry.ui.dialog;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bridge4biz.laundry.R;

public class MessageDialog extends DialogFragment {
    private static final String TAG = MessageDialog.class.getSimpleName();

    // UI references.
    private TextView mTextViewInfo;

    private String mMessage;
    private OnDialogDismissListener mOnDialogDismissListener;

    public static interface OnDialogDismissListener {
        public abstract void onDialogDismiss();
    }

    public static MessageDialog newInstance(OnDialogDismissListener onDialogDismissListener, String message) {
        MessageDialog md = new MessageDialog();
        md.initialize(onDialogDismissListener, message);

        return md;
    }

    public void initialize(OnDialogDismissListener onDialogDismissListener, String message) {
        mMessage = message;
        mOnDialogDismissListener = onDialogDismissListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (getShowsDialog()) {
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        }

        getDialog().setTitle(R.string.message_title);

        View rootView = inflater.inflate(R.layout.dialog_message, container, false);

        // Set up the login form.
        mTextViewInfo = (TextView) rootView.findViewById(R.id.textview_message);

        Button buttonUseMileage = (Button) rootView.findViewById(R.id.button_accept);
        buttonUseMileage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnDialogDismissListener != null)
                    mOnDialogDismissListener.onDialogDismiss();

                dismiss();
            }
        });

        if (mMessage != null)
            mTextViewInfo.setText(mMessage);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getDialog().getWindow().getAttributes().height;
        getDialog().getWindow().setLayout(width, height);

        final Drawable d = new ColorDrawable(Color.BLACK);
        d.setAlpha(150);

        getDialog().getWindow().setBackgroundDrawable(d);
    }
}