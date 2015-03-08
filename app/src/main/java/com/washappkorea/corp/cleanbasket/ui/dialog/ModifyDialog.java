package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.ui.OrderStatusFragment;

public class ModifyDialog extends DialogFragment {
    private static final String TAG = ModifyDialog.class.getSimpleName();

    private OnMenuSelectedListener onMenuSelectedListener;
    private int orderNumber;

    public interface OnMenuSelectedListener {
        void onMenuSelected(int orderNumber, int mode);
    }

    public static ModifyDialog newInstance(OnMenuSelectedListener onMenuSelectedListener, int orderNumber) {
        ModifyDialog md = new ModifyDialog();
        md.initialize(onMenuSelectedListener, orderNumber);

        return md;
    }

    public void initialize(OnMenuSelectedListener onMenuSelectedListener, int orderNumber) {
        this.onMenuSelectedListener = onMenuSelectedListener;
        this.orderNumber = orderNumber;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.label_order_modify);
        builder.setNegativeButton(R.string.label_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        builder.setItems(R.array.order_modify_menu, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        onMenuSelectedListener.onMenuSelected(OrderStatusFragment.MODIFY_ITEM, orderNumber);
                        break;
                    case 1:
                        onMenuSelectedListener.onMenuSelected(OrderStatusFragment.MODIFY_DATETIME, orderNumber);
                        break;
                    case 2:
                        onMenuSelectedListener.onMenuSelected(OrderStatusFragment.MODIFY_COUPON_MILEAGE, orderNumber);
                        break;
                    case 3:
                        popOrderCancelConfirm();
                        break;
                }
            }
        });

        return builder.create();
    }

    private void popOrderCancelConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.order_cancel_confirm);
        builder.setPositiveButton(R.string.label_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onMenuSelectedListener.onMenuSelected(OrderStatusFragment.CANCEL_ORDER, orderNumber);
            }
        });
        builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}