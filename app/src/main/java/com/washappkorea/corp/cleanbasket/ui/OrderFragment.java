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

import com.android.volley.Response;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.listener.NetworkErrorListener;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.ui.dialog.ItemListDialog;
import com.washappkorea.corp.cleanbasket.ui.widget.OrderItemAdapter;
import com.washappkorea.corp.cleanbasket.ui.widget.OrderItemsView;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

import java.util.ArrayList;

public class OrderFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {
    private static final String TAG = OrderFragment.class.getSimpleName();
    public static final String ITEM_LIST_DIALOG_TAG = "ITEM_LIST_DIALOG";

    private OrderItemsView mOrderItemsView;
    private OrderItemAdapter mOrderItemAdapter;
    private SearchView mSearchView;
    private RelativeLayout mOrderButton;

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

        getOrderItemAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                updateOrderInfo();
            }
        });

        getCategory();
    }

    private void getCategory() {
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_CATEGORY_ITEM);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getOrderItem();

                JsonData jsonData = null;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                }
                catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        ArrayList<OrderCategory> orderCategories = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<OrderCategory>>(){}.getType());
                        insertCategory(orderCategories);
                        break;
                }
            }
        }, new NetworkErrorListener(getActivity()));
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void getOrderItem() {
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_ORDER_ITEM);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = null;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                }
                catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        ArrayList<OrderItem> orderItems = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<OrderItem>>(){}.getType());
                        insertOrderItem(orderItems);
                        break;
                }
            }
        }, new NetworkErrorListener(getActivity()));
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void insertCategory(ArrayList<OrderCategory> orderCategories) {
        if (mOrderItemAdapter != null)
            mOrderItemAdapter.setOrderCategoryList(orderCategories);
    }

    private void insertOrderItem(ArrayList<OrderItem> orderItems) {
        if (mOrderItemAdapter != null)
            mOrderItemAdapter.addAll(orderItems);
    }

    private void updateOrderInfo() {
        if (mTextViewItemNumber != null && mTextViewItemTotal != null) {
            mTextViewItemNumber.setText(
                    getResources().getString(R.string.label_item) +
                            " " +
                            getOrderItemAdapter().getItemNumber() +
                            getResources().getString(R.string.item_unit));

            mTextViewItemTotal.setText(
                    getResources().getString(R.string.label_total) +
                            " " +
                            getOrderItemAdapter().getItemTotal() +
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
                popItemListDialog(getOrderItemAdapter().getSelectedItems());
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
        getOrderItemAdapter().getFilter().filter(newText);

        return true;
    }

    public OrderItemAdapter getOrderItemAdapter() {
        return mOrderItemAdapter;
    }
}