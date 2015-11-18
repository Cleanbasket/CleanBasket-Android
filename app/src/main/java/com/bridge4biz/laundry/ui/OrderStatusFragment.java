package com.bridge4biz.laundry.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.ui.dialog.CalculationDialog;
import com.bridge4biz.laundry.ui.dialog.ItemListDialog;
import com.bridge4biz.laundry.ui.dialog.ModifyDateTimeDialog;
import com.bridge4biz.laundry.ui.dialog.ModifyDialog;
import com.bridge4biz.laundry.ui.widget.OrderStateAdapter;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.AlarmManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.DateTimeFactory;
import com.bridge4biz.laundry.util.ImageManager;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

public class OrderStatusFragment extends Fragment implements View.OnClickListener, ModifyDialog.OnMenuSelectedListener, ModifyDateTimeDialog.OnDialogDismissListener, Response.Listener<JSONObject> {
    private static final String TAG = OrderStatusFragment.class.getSimpleName();
    public static final String ITEM_LIST_DIALOG_TAG = "ITEM_LIST_STATUS_DIALOG";
    public static final String TOTAL_DIALOG_TAG = "TOTAL_DIALOG";
    public static final String MODIFY_DIALOG_TAG = "MODIFY_DIALOG";
    public static final String MODIFY_DIALOG_TAG_AFTER_PICK_UP = "MODIFY_AFTER_PICK_UP_DIALOG";
    public static final String MODIFY_DATETIME_DIALOG_TAG = "MODIFY_DATETIME_DIALOG";

    public static final int MODIFY_ITEM = 0;
    public static final int MODIFY_DATETIME = 1;
    public static final int MODIFY_COUPON_MILEAGE = 2;
    public static final int CANCEL_ORDER = 3;

    public static final int PICK_UP_WAIT = 0;
    public static final int PICK_UP_MAN_SELECTED = 1;
    public static final int PICK_UP_FINISH = 2;
    public static final int DELIVERY_MAN_SELECTED = 3;
    public static final int DELIVERY_FINISH = 4;

    private LinearLayout noInfoPane;
    private RelativeLayout infoPane;
    private ImageView imageViewPdFace;
    private Button buttonCallPD;
    private ImageView imageViewStatusBar;
    private View lineBottom;
    private TextView textViewEmpty;
    private TextView textViewPDName;
    private TextView textViewPickUpDateTime;
    private TextView textViewDropOffDateTime;
    private Button buttonOrderItem;
    private Button buttonTotalGross;
    private Button buttonModifyOrder;
    private Button buttonFeedback;
    private Button buttonOrderHistory;

    private Order mOrder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.custom_order_status, container, false);

        noInfoPane = (LinearLayout) rootView.findViewById(R.id.layout_info);
        infoPane = (RelativeLayout) rootView.findViewById(R.id.layout_order_info);
        imageViewPdFace = (ImageView) rootView.findViewById(R.id.imageview_pd_face);
        buttonCallPD = (Button) rootView.findViewById(R.id.button_call_pd);
        imageViewStatusBar = (ImageView) rootView.findViewById(R.id.imageview_status_bar);
        lineBottom = (View) rootView.findViewById(R.id.line_bottom);
        textViewEmpty = (TextView) rootView.findViewById(R.id.textview_no_item);
        textViewPDName = (TextView) rootView.findViewById(R.id.textview_pd_name);
        textViewPickUpDateTime = (TextView) rootView.findViewById(R.id.textview_pick_up_time);
        textViewDropOffDateTime = (TextView) rootView.findViewById(R.id.textview_drop_off_time);
        buttonOrderItem = (Button) rootView.findViewById(R.id.button_order_item_child);
        buttonTotalGross = (Button) rootView.findViewById(R.id.button_total);
        buttonModifyOrder = (Button) rootView.findViewById(R.id.button_modify_order);
        buttonFeedback = (Button) rootView.findViewById(R.id.button_feedback);
        buttonOrderHistory = (Button) rootView.findViewById(R.id.button_order_history);

        buttonOrderHistory.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getOrderStatus();
    }

    private void getOrderStatus() {
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_RECENT_ORDER);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        ArrayList<Order> orders = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<Order>>() {
                        }.getType());

                        if (orders.size() == 0) showNoInfoPane();
                        else drawInfo(orders.get(0));
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void showNoInfoPane() {
        noInfoPane.setVisibility(View.VISIBLE);
        infoPane.setVisibility(View.GONE);
    }

    private void showInfoPane() {
        noInfoPane.setVisibility(View.GONE);
        infoPane.setVisibility(View.VISIBLE);
    }

    private void drawInfo(final Order order) {
        showInfoPane();

        this.mOrder = order;

        int state = order.state;

        buttonFeedback.setVisibility(View.GONE);
        textViewPDName.setText(getString(R.string.pd_name_default));
        imageViewPdFace.setImageResource(R.drawable.ic_launcher);
        // lineBottom.setVisibility(View.INVISIBLE);

        // state에 따라 컴포넌트를 출력합니다
        switch (state) {
            case OrderStateAdapter.PICK_UP_WAIT:
                imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline1);
                buttonModifyOrder.setVisibility(View.VISIBLE);
                lineBottom.setVisibility(View.VISIBLE);
                buttonCallPD.setVisibility(View.GONE);
                break;

            case OrderStateAdapter.PICK_UP_MAN_SELECTED:
                imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline2);
                buttonModifyOrder.setVisibility(View.VISIBLE);
                lineBottom.setVisibility(View.VISIBLE);
                if (order.pickupInfo != null) {
                    imageViewPdFace.setImageResource(0);
                    setPDFaceImage(imageViewPdFace, order.pickupInfo.img);
                    textViewPDName.setText(order.pickupInfo.name);
                    buttonCallPD.setVisibility(View.VISIBLE);
                    buttonCallPD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + order.pickupInfo.phone));
                            startActivity(callIntent);
                        }
                    });
                }
                break;

            case OrderStateAdapter.DELIVERY_MAN_SELECTED:
                imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline3);
                if (order.dropoffInfo != null) {
                    imageViewPdFace.setImageResource(0);
                    setPDFaceImage(imageViewPdFace, order.dropoffInfo.img);
                    textViewPDName.setText(order.dropoffInfo.name);
                    buttonCallPD.setVisibility(View.VISIBLE);
                    buttonCallPD.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + order.pickupInfo.phone));
                            startActivity(callIntent);
                        }
                    });
                }
                break;

            case OrderStateAdapter.DELIVERY_FINISH:
                imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline4);
                buttonFeedback.setVisibility(View.VISIBLE);
                break;

            default:
                imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline2);
                buttonCallPD.setVisibility(View.GONE);
                break;
        }

        textViewPickUpDateTime.setText(
                DateTimeFactory.getInstance().getDate(getActivity(), order.pickup_date) +
                        DateTimeFactory.getInstance().getNewLine() +
                        DateTimeFactory.getInstance().getTime(getActivity(), order.pickup_date) + getString(R.string.time_tilde) +
                        DateTimeFactory.getInstance().getPlusOneTime(getActivity(), order.pickup_date));
        textViewDropOffDateTime.setText(
                DateTimeFactory.getInstance().getDate(getActivity(), order.dropoff_date) +
                        DateTimeFactory.getInstance().getNewLine() +
                        DateTimeFactory.getInstance().getTime(getActivity(), order.dropoff_date)+ getString(R.string.time_tilde) +
                        DateTimeFactory.getInstance().getPlusOneTime(getActivity(), order.dropoff_date));
        buttonOrderItem.setText(
                getString(R.string.label_item) + " " + order.getTotalNumberFromOrder() + getString(R.string.item_unit));
        buttonOrderItem.setTag(order);

            /* 총계를 구해 버튼에 새깁니다 */
//            buttonTotalGross.setText(
//                    getString(R.string.label_total) + " " +
//                            mFormatKRW.format(getTotalFromOrder(position) + order.dropoff_price - order.mileage - getCouponTotal(order)) +
//                            getString(R.string.monetary_unit));
        buttonTotalGross.setText(
                getString(R.string.label_total) + " " +
                        CleanBasketApplication.mFormatKRW.format(order.price) +
                        getString(R.string.monetary_unit));
        buttonTotalGross.setTag(order);
        buttonModifyOrder.setTag(order);
        buttonOrderItem.setOnClickListener(this);
        buttonTotalGross.setOnClickListener(this);
        buttonModifyOrder.setOnClickListener(this);
        buttonFeedback.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_order_item_child:
                ItemListDialog itemListDialog =
                        ItemListDialog.newInstance(mOrder.item, null);

                itemListDialog.show(
                        getActivity().getSupportFragmentManager(),
                        ITEM_LIST_DIALOG_TAG);
                break;

            case R.id.button_total:
                CalculationDialog calculationDialog =
                        CalculationDialog.newInstance(mOrder, null);

                calculationDialog.show(
                        getActivity().getSupportFragmentManager(),
                        TOTAL_DIALOG_TAG);
                break;

            case R.id.button_modify_order:
                ModifyDialog md = ModifyDialog.newInstance(this, mOrder.oid);

                if (mOrder.state >= PICK_UP_FINISH)
                    md.show(getActivity().getSupportFragmentManager(), MODIFY_DIALOG_TAG_AFTER_PICK_UP);
                else
                    md.show(getActivity().getSupportFragmentManager(), MODIFY_DIALOG_TAG);
                break;

            case R.id.button_order_history:
                Intent intent = new Intent();
                intent.setAction("com.bridge4biz.laundry.ui.OrderHistoryActivity");
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onDialogDismiss() {
        getOrderStatus();
    }

    @Override
    public void onMenuSelected(int mode, int oid) {
        switch (mode) {
            case MODIFY_ITEM:
                OrderFragment f = new OrderFragment();
                f.setOrderInfo(mOrder);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.add(R.id.layout_order_state_fragment, f, OrderFragment.TAG);
                ft.addToBackStack(MainActivity.NEW_ORDER_FRAGMENT);
                ft.commit();
                break;

            case MODIFY_DATETIME:
                ModifyDateTimeDialog modifyDateTimeDialog = ModifyDateTimeDialog.newInstance(this, oid);

                modifyDateTimeDialog.show(getActivity().getSupportFragmentManager(), MODIFY_DATETIME_DIALOG_TAG);
                break;

            case CANCEL_ORDER:
                cancelOrder(oid);
                AlarmManager.getInstance(getActivity()).cancelAlarm(mOrder);
                AlarmManager.getInstance(getActivity()).deleteAlarmFromDB(mOrder);
                break;
        }
    }

    private void cancelOrder(int oid) {
        getOrderStatus();

        PostRequest postRequest = new PostRequest(getActivity());
        postRequest.setUrl(AddressManager.DEL_ORDER);
        postRequest.setParams("oid", String.valueOf(oid));
        postRequest.setListener(this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), getString(R.string.toast_error), Toast.LENGTH_SHORT);
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest());
    }

    @Override
    public void onResponse(JSONObject response) {
        JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
        switch (jsonData.constant) {
            case Constants.IMPOSSIBLE:
                CleanBasketApplication.getInstance().showToast(getString(R.string.delete_impossible));
                break;

            case Constants.SUCCESS:
                CleanBasketApplication.getInstance().showToast(getString(R.string.order_delete_success));
                AlarmManager.getInstance(getActivity()).deleteAlarmFromDB(mOrder);
                AlarmManager.getInstance(getActivity()).cancelAlarm(mOrder);
                AlarmManager.getInstance(getActivity()).setAlarm();
                break;
        }
    }

    private void setPDFaceImage(ImageView imageView, String imageInfo) {
        Picasso.with(getActivity()).load(Config.SERVER_ADDRESS + imageInfo).transform(ImageManager.getCircleTransformation()).into(imageView);
    }
}