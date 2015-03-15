package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.ui.MainActivity;
import com.washappkorea.corp.cleanbasket.ui.OrderFragment;
import com.washappkorea.corp.cleanbasket.ui.OrderInfoFragment;
import com.washappkorea.corp.cleanbasket.ui.OrderStatusFragment;
import com.washappkorea.corp.cleanbasket.ui.widget.OrderItemAdapterHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemListDialog extends DialogFragment implements View.OnClickListener {
    private static final String TAG = ItemListDialog.class.getSimpleName();

    private GridView mOrderItemGridView;
    private TextView mNumberTextView;
    private TextView mTotalTextView;
    private Button mAcceptButton;
    private Button mAcceptBigButton;
    private Button mModifyButton;
    private Button mCancelButton;

    private ArrayList<OrderItem> orderItems;
    private ItemListAdapter mItemListAdapter;

    public static ItemListDialog newInstance(ArrayList<OrderItem> orderItems) {
        ItemListDialog ild = new ItemListDialog();
        ild.initialize(orderItems);

        return ild;
    }

    public void initialize(ArrayList<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

//        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
//        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
//        getDialog().getWindow().setLayout(width, height);

        View rootView = inflater.inflate(R.layout.dialog_item_list, container, false);

        mOrderItemGridView = (GridView) rootView.findViewById(R.id.gridview_item_list);
        mNumberTextView = (TextView) rootView.findViewById(R.id.textview_item_number_list);
        mTotalTextView = (TextView) rootView.findViewById(R.id.textview_item_total_list);
        mAcceptButton = (Button) rootView.findViewById(R.id.button_accept_list);
        mAcceptBigButton = (Button) rootView.findViewById(R.id.button_accept_list_big);
        mModifyButton = (Button) rootView.findViewById(R.id.button_modify_list);
        mCancelButton = (Button) rootView.findViewById(R.id.button_cancel_list);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(orderItems != null) {
            mItemListAdapter = new ItemListAdapter(getActivity(), 0, orderItems);
            mOrderItemGridView.setNumColumns(2);
            mOrderItemGridView.setAdapter(mItemListAdapter);

            mNumberTextView.setText(
                    mItemListAdapter.getItemNumber() +
                    getString(R.string.item_unit));
            mTotalTextView.setText(
                    getString(R.string.label_total) +
                    " " +
                    mItemListAdapter.getItemTotal() +
                    getString(R.string.monetary_unit));
        }

        if (getTag().equals(OrderFragment.ITEM_LIST_DIALOG_TAG) || getTag().equals(OrderFragment.MODIFY_ITEM_LIST_DIALOG_TAG)) {
            mAcceptBigButton.setVisibility(View.GONE);
            mModifyButton.setVisibility(View.GONE);
        }
        else if (getTag().equals(OrderInfoFragment.ITEM_LIST_DIALOG_TAG_INFO)) {
            mAcceptBigButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);
        }
        else if (getTag().equals(OrderStatusFragment.ITEM_LIST_DIALOG_TAG)) {
            mAcceptButton.setVisibility(View.GONE);
            mModifyButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);
        }

        mAcceptButton.setOnClickListener(this);
        mAcceptBigButton.setOnClickListener(this);
        mModifyButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_accept_list:
            case R.id.button_accept_list_big:
                dismiss();

                if (getTag().equals(OrderFragment.MODIFY_ITEM_LIST_DIALOG_TAG)) {
                    if (getActivity().getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG_MODIFY) == null)
                        addFragment(R.id.layout_order_state_fragment, new OrderInfoFragment());
                    else {
                        ((OrderInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG_MODIFY)).setCalculationInfo();
                        showFragment(getActivity().getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG_MODIFY));
                    }
                }
                else {
                    if (getActivity().getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG) == null)
                        addFragment(R.id.layout_order_fragment, new OrderInfoFragment());
                    else {
                        ((OrderInfoFragment) getActivity().getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG)).setCalculationInfo();
                        showFragment(getActivity().getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG));
                    }
                }
                break;

            case R.id.button_modify_list:
                dismiss();

                changeFragment(
                        getActivity().getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG),
                        getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0"));
                break;

            case R.id.button_cancel_list:
                dismiss();
                break;
        }
    }

    private void addFragment(int containerViewId, Fragment fragment) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(containerViewId, fragment, OrderInfoFragment.TAG);
        ft.addToBackStack(MainActivity.NEW_INFO_FRAGMENT);
        ft.commit();
    }

    private void changeFragment(Fragment hideFragment, Fragment showFragment) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.hide(hideFragment);
        ft.show(showFragment);
        ft.addToBackStack(MainActivity.CHANGE_TO_ORDER_FRAGMENT);
        ft.commit();
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.show(fragment);
        ft.addToBackStack(MainActivity.CHANGE_TO_INFO_FRAGMENT);
        ft.commit();
    }

    private void removeFragment(Fragment fragment) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    class ItemListAdapter extends OrderItemAdapterHelper {
        private LayoutInflater mLayoutInflater;

        ItemListAdapter(Context context, int resource, List<OrderItem> objects) {
            super(context, resource, objects);

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            OrderItemListViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_orderitem_list, parent, false);
                holder = new OrderItemListViewHolder();
                holder.textViewOrderItem = (TextView) convertView.findViewById(R.id.textview_orderitem_list);
                holder.textViewOrderItemPrice = (TextView) convertView.findViewById(R.id.textview_orderitem_price_list);
                holder.textViewOrderItemCount = (TextView) convertView.findViewById(R.id.textview_orderitem_count_list);
                holder.textViewOrderItemTotalPrice = (TextView) convertView.findViewById(R.id.textview_orderitem_total_list);
                convertView.setTag(holder);
            } else
                holder = (OrderItemListViewHolder) convertView.getTag();

            holder.textViewOrderItem.setText(CleanBasketApplication.getInstance().getStringByString(getItem(position).descr));
            holder.textViewOrderItemPrice.setText(String.valueOf(getItem(position).price) + getContext().getString(R.string.monetary_unit));
            holder.textViewOrderItemCount.setText(getItem(position).count + getString(R.string.item_unit));
            holder.textViewOrderItemTotalPrice.setText(String.valueOf(getItem(position).price * getItem(position).count) + getContext().getString(R.string.monetary_unit));

            return convertView;
        }

        protected class OrderItemListViewHolder {
            public TextView textViewOrderItem;
            public TextView textViewOrderItemPrice;
            public TextView textViewOrderItemCount;
            public TextView textViewOrderItemTotalPrice;
        }
    }
}