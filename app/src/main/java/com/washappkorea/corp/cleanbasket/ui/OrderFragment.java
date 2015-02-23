package com.washappkorea.corp.cleanbasket.ui;


import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.OrderService;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.ui.dialog.ItemListDialog;
import com.washappkorea.corp.cleanbasket.ui.widget.OrderItemAdapter;
import com.washappkorea.corp.cleanbasket.ui.widget.OrderItemsView;

import java.util.ArrayList;

public class OrderFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {
    private static final String TAG = OrderFragment.class.getSimpleName();
    private static final String ITEM_LIST_DIALOG_TAG = "ITEM_LIST_DIALOG";

    private OrderItemsView mOrderItemsView;
    private OrderItemAdapter mOrderItemAdapter;
    private SearchView mSearchView;
    private RelativeLayout mOrderButton;
    private OrderService mOrderService;

    private TextView mTextViewItemNumber;
    private TextView mTextViewItemTotal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);

        mOrderItemsView = (OrderItemsView) rootView.findViewById(R.id.gridview_item);
        mOrderItemAdapter = new OrderItemAdapter(getActivity(), R.layout.item_orderitem);
        mSearchView = (SearchView) rootView.findViewById(R.id.searchview_item);
        mOrderButton = (RelativeLayout) rootView.findViewById(R.id.button_order);
        mTextViewItemNumber = (TextView) rootView.findViewById(R.id.textview_item_number);
        mTextViewItemTotal = (TextView) rootView.findViewById(R.id.textview_item_total);

        mOrderItemsView.setNumColumns(3);
        mOrderItemsView.setAreHeadersSticky(true);
        mOrderItemsView.setAdapter(mOrderItemAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSearchView.setOnClickListener(this);
        mSearchView.setOnQueryTextListener(this);
        mOrderButton.setOnClickListener(this);

        mOrderItemAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                updateOrderInfo();
            }
        });

        // OrderCategory는 서버에 요청해서 받아옵니다
        ArrayList<OrderCategory> orderCategoryList = new ArrayList<OrderCategory>();
        orderCategoryList.add(new OrderCategory(0, "offers"));
        orderCategoryList.add(new OrderCategory(1, "business"));
        orderCategoryList.add(new OrderCategory(2, "top"));
        orderCategoryList.add(new OrderCategory(3, "bottom"));
        orderCategoryList.add(new OrderCategory(4, "overcoat"));
        orderCategoryList.add(new OrderCategory(5, "etc"));
        mOrderItemAdapter.setOrderCategoryList(orderCategoryList);

        // 아이템들은 서버에 요청해서 받아옵니다
        mOrderItemAdapter.add(new OrderItem(1, "t_shirt", 2000, 0, "ic_item_t_shirt"));
        mOrderItemAdapter.add(new OrderItem(2, "y_shirt", 2000, 0, "ic_item_y_shirt"));
        mOrderItemAdapter.add(new OrderItem(3, "trousers", 3000, 0, "ic_item_trousers"));
        mOrderItemAdapter.add(new OrderItem(4, "coat", 8000, 0, "ic_item_coat"));
        mOrderItemAdapter.add(new OrderItem(5, "jacket", 4000, 0, "ic_item_jacket"));
        mOrderItemAdapter.add(new OrderItem(1, "t_shirt", 2000, 1, "ic_item_t_shirt"));
        mOrderItemAdapter.add(new OrderItem(2, "y_shirt", 2000, 2, "ic_item_y_shirt"));
        mOrderItemAdapter.add(new OrderItem(3, "trousers", 3000, 3, "ic_item_trousers"));
        mOrderItemAdapter.add(new OrderItem(4, "coat", 8000, 4, "ic_item_coat"));
        mOrderItemAdapter.add(new OrderItem(5, "jacket", 4000, 4, "ic_item_jacket"));

//        getOrderService().getItemCodes(new Callback<ArrayList<OrderItem>>() {
//            @Override
//            public void success(ArrayList<OrderItem> orderItems, Response response) {
//                mOrderItemsView.insertOrderItem(orderItems);
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
    }

    private void updateOrderInfo() {
        if (mTextViewItemNumber != null && mTextViewItemTotal != null) {
            mTextViewItemNumber.setText(
                    getResources().getString(R.string.label_item) +
                            " " +
                            mOrderItemAdapter.getItemNumber() +
                            getResources().getString(R.string.item_unit));

            mTextViewItemTotal.setText(
                    getResources().getString(R.string.label_total) +
                            " " +
                            mOrderItemAdapter.getItemTotal() +
                            getResources().getString(R.string.monetary_unit));
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchview_item:
                mSearchView.setIconified(false);
                break;

            case R.id.button_order:
                popItemListDialog(mOrderItemAdapter.getSelectedItems());
                break;
        }
    }

    private void popItemListDialog(ArrayList<OrderItem> orderItems) {
        ItemListDialog itemListDialog =
                ItemListDialog.newInstance(orderItems);

        itemListDialog.show(
                getActivity().getSupportFragmentManager(),
                ITEM_LIST_DIALOG_TAG);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mOrderItemAdapter.getFilter().filter(newText);

        return true;
    }

    private OrderService getOrderService() {
        if (mOrderService == null) {
            mOrderService = ((MainActivity) getActivity()).getRestAdapter().create(OrderService.class);
        }

        return mOrderService;
    }
}