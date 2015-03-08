package com.washappkorea.corp.cleanbasket.ui;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.db.DBHelper;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.listener.NetworkErrorListener;
import com.washappkorea.corp.cleanbasket.io.model.AppInfo;
import com.washappkorea.corp.cleanbasket.io.model.ItemInfo;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.ui.dialog.ItemListDialog;
import com.washappkorea.corp.cleanbasket.ui.widget.OrderItemAdapter;
import com.washappkorea.corp.cleanbasket.ui.widget.OrderItemsView;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;

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

        getOrderItemFromDB();

        getAppInfo();
    }

    private void getOrderItem() {
        Log.i(TAG, "get from Server");

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
                        ItemInfo itemInfo;

                        try {
                            itemInfo = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ItemInfo>(){}.getType());
                            final ArrayList<OrderCategory> categories = itemInfo.categories;
                            final ArrayList<OrderItem> orderItems = itemInfo.orderItems;

                            getDBHelper().getOrderCategoryDao().callBatchTasks(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    for (OrderCategory oc : categories)
                                        getDBHelper().getOrderCategoryDao().createOrUpdate(oc);

                                    return null;
                                }
                            });

                            getDBHelper().getOrderItemDao().callBatchTasks(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    for (OrderItem oi : orderItems)
                                        getDBHelper().getOrderItemDao().createOrUpdate(oi);

                                    return null;
                                }
                            });

                            insertCategory(categories);
                            insertOrderItem(orderItems);
                        } catch (JsonSyntaxException e) {

                        } catch (NullPointerException e) {

                        }

                        break;
                }
            }
        }, new NetworkErrorListener(getActivity()));
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void getOrderItemFromDB() {
        Log.i(TAG, "get from DB");

        insertCategory((ArrayList<OrderCategory>) getDBHelper().getOrderCategoryDao().queryForAll());
        insertOrderItem((ArrayList<OrderItem>) getDBHelper().getOrderItemDao().queryForAll());
    }

    private void insertCategory(ArrayList<OrderCategory> orderCategories) {
        if (mOrderItemAdapter != null)
            mOrderItemAdapter.setOrderCategoryList(orderCategories);
    }

    private void insertOrderItem(ArrayList<OrderItem> orderItems) {
        if (orderItems.size() == 0)
            return;

        Collections.sort(orderItems, new Comparator<OrderItem>() {
            @Override
            public int compare(OrderItem lhs, OrderItem rhs) {
                return lhs.category - rhs.category;
            }
        });

        if (mOrderItemAdapter != null) {
            mOrderItemAdapter.addAll(orderItems);
            mOrderItemAdapter.setFixedOrderItem(orderItems);
        }
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

    private void getAppInfo() {
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_APP_INFO);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = getGson().fromJson(response, JsonData.class);
                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        AppInfo appInfo = getGson().fromJson(jsonData.data, AppInfo.class);
                        AppInfo localAppInfo = null;

                        try {
                            localAppInfo = getDBHelper().getAppInfoDao().queryBuilder().orderBy(AppInfo.ID, false).limit(1L).queryForFirst();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        if (localAppInfo == null || appInfo.order_item_ver > localAppInfo.order_item_ver)
                            getOrderItem();

                        if (localAppInfo != null && checkAndroidLatest(appInfo.android_app_ver, localAppInfo.android_app_ver))
                            showUpdateAlert();

                        getDBHelper().getAppInfoDao().createOrUpdate(appInfo);

                    Log.i(TAG, "Downloaded app info successfully");
                    break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private boolean checkAndroidLatest(String newAppInfo, String localAppInfo) {
        String[] newAppInfoString = newAppInfo.split("\\.");
        String[] localAppInfoString = localAppInfo.split("\\.");

        try {
            int firstDotNewInfo = Integer.parseInt(newAppInfoString[0]);
            int secondDotNewInfo = Integer.parseInt(newAppInfoString[1]);

            int firstDotLocalInfo = Integer.parseInt(localAppInfoString[0]);
            int secondDotLocalInfo = Integer.parseInt(localAppInfoString[1]);

            if (firstDotNewInfo > firstDotLocalInfo) {
                return true;
            }

            if (secondDotNewInfo > secondDotLocalInfo) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.i(TAG, e.toString());
        }

        return false;
    }

    private void showUpdateAlert() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.update_alert_title))
                .setMessage(getString(R.string.update_alert_message))
                .setInverseBackgroundForced(true)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.update_alert_accept), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.PLAY_STORE_URL));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.update_alert_exit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .create().show();
    }

    private DBHelper getDBHelper() {
        return ((MainActivity) getActivity()).getDBHelper();
    }

    private Gson getGson() {
        return CleanBasketApplication.getInstance().getGson();
    }
}