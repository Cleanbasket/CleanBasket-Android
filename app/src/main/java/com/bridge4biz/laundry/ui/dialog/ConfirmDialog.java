package com.bridge4biz.laundry.ui.dialog;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.ui.widget.CalculationInfo;
import com.bridge4biz.laundry.util.DateTimeFactory;

import java.text.DecimalFormat;

public class ConfirmDialog extends DialogFragment implements View.OnClickListener {
    private static final String TAG = ConfirmDialog.class.getSimpleName();

    private OnConfirmListener mOnConfirmListener;

    // UI references.
    private int unit;
    private CalculationInfo calculationInfo;

    private TextView mTextViewInfo;
    private Button mButtonOrder;
    private Button mButtonCancel;

    private DecimalFormat mFormatKRW = new DecimalFormat("###,###,###");

    public interface OnConfirmListener {
        void onConfirm(ConfirmDialog dialog);
    }

    public static ConfirmDialog newInstance(OnConfirmListener onConfirmListener, int unit, CalculationInfo calculationInfo) {
        ConfirmDialog md = new ConfirmDialog();
        md.initialize(onConfirmListener, unit, calculationInfo);

        return md;
    }

    public void initialize(OnConfirmListener onConfirmListener, int unit, CalculationInfo calculationInfo) {
        this.unit = unit;
        this.mOnConfirmListener = onConfirmListener;
        this.calculationInfo = calculationInfo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View rootView = inflater.inflate(R.layout.dialog_confirm, container, false);
        mButtonOrder = (Button) rootView.findViewById(R.id.button_accept_confirm);
        mButtonCancel = (Button) rootView.findViewById(R.id.button_cancel_confirm);

        // Set up the login form.
        mTextViewInfo = (TextView) rootView.findViewById(R.id.confirm_info);

        mButtonOrder.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);

        if (calculationInfo.scope > 0)
            mTextViewInfo.setText(getString(R.string.label_item) + " " + unit + getString(R.string.item_unit) + DateTimeFactory.getInstance().getNewLine() +
                    calculationInfo.getPriceTag() + DateTimeFactory.getInstance().getNewLine() +
                    getString(R.string.price_has_scope));
        else
            mTextViewInfo.setText(getString(R.string.label_item) + " " + unit + getString(R.string.item_unit) + DateTimeFactory.getInstance().getNewLine() +
                    calculationInfo.getPriceTag());

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_accept_confirm:
                mOnConfirmListener.onConfirm(this);
                dismiss();
                break;

            case R.id.button_cancel_confirm:
                dismiss();
                break;
        }
    }
}