package com.bridge4biz.laundry.ui;


import android.app.AlertDialog;
import android.content.Context;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.db.DBHelper;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.listener.NetworkErrorListener;
import com.bridge4biz.laundry.io.model.AppInfo;
import com.bridge4biz.laundry.io.model.District;
import com.bridge4biz.laundry.io.model.ItemInfo;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.model.OrderCategory;
import com.bridge4biz.laundry.io.model.OrderItem;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.ui.dialog.CalculationDialog;
import com.bridge4biz.laundry.ui.dialog.ItemListDialog;
import com.bridge4biz.laundry.ui.widget.OrderItemAdapter;
import com.bridge4biz.laundry.ui.widget.OrderItemsView;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class OrderFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener, ItemListDialog.ItemListListener, CalculationDialog.CalculationListener {
    public static final String TAG = OrderFragment.class.getSimpleName();
    public static final String ITEM_LIST_DIALOG_TAG = "ITEM_LIST_DIALOG";
    public static final String MODIFY_ITEM_LIST_DIALOG_TAG = "MODIFY_ITEM_LIST_DIALOG";

    private Boolean isModifyOrder = false;

    private Order mOrder;
    private OrderItemsView mOrderItemsView;
    private OrderItemAdapter mOrderItemAdapter;
    private SearchView mSearchView;
    private RelativeLayout mOrderButton;

    private ImageView mImageViewSearchIcon;
    private ImageView mUndoButton;

    private TextView mTextViewItemNumber;
    private TextView mTextViewItemTotal;
    private TextView mTextViewOrderButton;

    private DecimalFormat mFormatKRW = new DecimalFormat("###,###,###");

    private ArrayList<OrderItem> mOrderItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);

        mOrderItemsView = (OrderItemsView) rootView.findViewById(R.id.gridview_item);
        mOrderItemAdapter = new OrderItemAdapter(getActivity(), R.layout.item_orderitem);
        mSearchView = (SearchView) rootView.findViewById(R.id.searchview_item);
        mImageViewSearchIcon = (ImageView) rootView.findViewById(R.id.search_icon);
        mOrderButton = (RelativeLayout) rootView.findViewById(R.id.button_order);
        mUndoButton = (ImageView) rootView.findViewById(R.id.imageview_undo);
        mTextViewOrderButton = (TextView) rootView.findViewById(R.id.textview_order_button_label);
        mTextViewItemNumber = (TextView) rootView.findViewById(R.id.textview_item_number);
        mTextViewItemTotal = (TextView) rootView.findViewById(R.id.textview_item_total);

        mOrderItemsView.setNumColumns(3);
        mOrderItemsView.setAreHeadersSticky(true);
        mOrderItemsView.setAdapter(mOrderItemAdapter);

        int searchCloseButtonId = mSearchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) mSearchView.findViewById(searchCloseButtonId);

        int searchIconId = mSearchView.getContext().getResources()
                .getIdentifier("android:id/search_button", null, null);
        ImageView searchIcon = (ImageView) mSearchView.findViewById(searchIconId);
        searchIcon.setImageResource(R.drawable.ic_menu_search);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setQuery("", true);
                mSearchView.setVisibility(View.GONE);
                mImageViewSearchIcon.setVisibility(View.VISIBLE);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isModifyOrder)
            mTextViewOrderButton.setText(R.string.label_order_modify);

        mSearchView.setOnClickListener(this);
        mSearchView.setOnQueryTextListener(this);
        mImageViewSearchIcon.setOnClickListener(this);
        mOrderButton.setOnClickListener(this);
        mUndoButton.setOnClickListener(this);

        getOrderItemAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                if (getOrderItemAdapter().getSelectedItems().size() > 0) {
                    if (mUndoButton.getVisibility() == View.GONE)
                        popUndoButton(mUndoButton);
                }
                else {
                    if (mUndoButton.getVisibility() == View.VISIBLE)
                        hideUndoButton(mUndoButton);
                }

                updateOrderInfo();
            }
        });

        getOrderItemFromDB();

        getAppInfo();
    }

    public void setOrderInfo(Order order) {
        isModifyOrder = true;

        mOrder = order;
        mOrderItem = order.item;
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

    private void getDistricts() {
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_DISTRICT);
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
                        try {
                            final ArrayList<District> districts = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<District>>(){}.getType());

                            getDBHelper().getDistrictDao().callBatchTasks(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    for (District district : districts) {
                                        getDBHelper().getDistrictDao().createOrUpdate(district);
                                    }

                                    return null;
                                }
                            });
                        } catch (JsonSyntaxException e) {
                            Log.e(TAG, e.toString());
                        } catch (NullPointerException e) {
                            Log.e(TAG, e.toString());
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

        Log.i(TAG, "insertOrderItem");

        Collections.sort(orderItems, new Comparator<OrderItem>() {
            @Override
            public int compare(OrderItem lhs, OrderItem rhs) {
                return lhs.category - rhs.category;
            }
        });

        if (mOrderItem != null) {
            HashMap<Integer, OrderItem> map = new HashMap<Integer, OrderItem>();

            for (OrderItem orderItem : mOrderItem) {
                map.put(orderItem.item_code, orderItem);
            }

            for (OrderItem oi : orderItems) {
                if (map.containsKey(oi.item_code)) {
                    oi.count = map.get(oi.item_code).count;
                }
            }
        }

        if (mOrderItemAdapter != null) {
            mOrderItemAdapter.clear();
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
                            mFormatKRW.format((double) getOrderItemAdapter().getItemTotal()) +
                            getResources().getString(R.string.monetary_unit));
        };
    }

    private void popUndoButton(View v) {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.undo_button_pop);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mUndoButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(animation);
    }

    private void hideUndoButton(View v) {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.undo_button_hide);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mUndoButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_icon:
                mSearchView.requestFocus();
                showSoftKeyboard();
                mSearchView.setVisibility(View.VISIBLE);
                mImageViewSearchIcon.setVisibility(View.GONE);
                break;

            case R.id.button_order:
                if (getOrderItemAdapter().getItemTotal() < OrderInfoFragment.MINIMUM_ORDER) {
                    CleanBasketApplication.getInstance().showToast(getString(R.string.minimum_total));
                    return;
                }

                if (isModifyOrder)
                    popItemListDialog(getOrderItemAdapter().getSelectedItems(), MODIFY_ITEM_LIST_DIALOG_TAG);
                else
                    popItemListDialog(getOrderItemAdapter().getSelectedItems(), ITEM_LIST_DIALOG_TAG);
                break;

            case R.id.imageview_undo:
                getOrderItemAdapter().clearItems();
                break;
        }
    }

    @Override
    public void onItemListFinish(ItemListDialog dialog) {
        if (isModifyOrder) {
            popCalculationDialog(CalculationDialog.MODIFY_TAG);
        }
    }

    @Override
    public void onCalculationFinish(CalculationDialog dialog) {
        getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + 1).onResume();
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void popItemListDialog(ArrayList<OrderItem> orderItems, String tag) {
        ItemListDialog itemListDialog =
                ItemListDialog.newInstance(orderItems, this);

        itemListDialog.show(
                getActivity().getSupportFragmentManager(),
                tag);
    }

    private void popCalculationDialog(String tag) {
        mOrder.item = getOrderItemAdapter().getSelectedItems();
        mOrder.price = getOrderItemAdapter().getItemTotal();

        if (getOrderItemAdapter().getItemTotal() >= OrderInfoFragment.FREE_PICK_UP_PRICE)
            mOrder.dropoff_price = 0;
        else
            mOrder.dropoff_price = 2000;

        CalculationDialog calculationDialog =
                CalculationDialog.newInstance(mOrder, this);

        calculationDialog.show(
                getActivity().getSupportFragmentManager(),
                tag);
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

                        if (localAppInfo == null || appInfo.district_ver > localAppInfo.district_ver)
                            getDistricts();

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

    private void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private DBHelper getDBHelper() {
        return ((MainActivity) getActivity()).getDBHelper();
    }

    private Gson getGson() {
        return CleanBasketApplication.getInstance().getGson();
    }
}