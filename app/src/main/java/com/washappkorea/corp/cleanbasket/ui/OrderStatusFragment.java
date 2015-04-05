package com.washappkorea.corp.cleanbasket.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.Coupon;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.model.Order;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.io.request.PostRequest;
import com.washappkorea.corp.cleanbasket.ui.dialog.CalculationDialog;
import com.washappkorea.corp.cleanbasket.ui.dialog.ItemListDialog;
import com.washappkorea.corp.cleanbasket.ui.dialog.ModifyDateTimeDialog;
import com.washappkorea.corp.cleanbasket.ui.dialog.ModifyDialog;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;
import com.washappkorea.corp.cleanbasket.util.DateTimeFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderStatusFragment extends Fragment {
    private static final String TAG = OrderStatusFragment.class.getSimpleName();
    public static final String ITEM_LIST_DIALOG_TAG = "ITEM_LIST_STATUS_DIALOG";
    public static final String TOTAL_DIALOG_TAG = "TOTAL_DIALOG";
    public static final String MODIFY_DIALOG_TAG = "MODIFY_DIALOG";
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

    private static int previousChild = -1;

    private LayoutInflater mLayoutInflater;

    private TextView mTextViewEmpty;
    private ExpandableListView mOrderStateListView;
    private View mProgressView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayoutInflater = inflater;

        View rootView = inflater.inflate(R.layout.fragment_order_status, container, false);

        mOrderStateListView = (ExpandableListView) rootView.findViewById(R.id.listview_order_state);
        mTextViewEmpty = (TextView) rootView.findViewById(R.id.textview_no_item);
        mProgressView = rootView.findViewById(R.id.loading_progress);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mOrderStateListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (previousChild == groupPosition) {

                } else if (previousChild >= 0) {
                    mOrderStateListView.collapseGroup(previousChild);
                }

                previousChild = groupPosition;
            }
        });
    }

    private void getOrderStatus() {
        showProgress(true);

        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_ORDER);
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
                        showProgress(false);
                        ArrayList<Order> orders = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<Order>>(){}.getType());
                        insertOrderStatus(orders);
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), getString(R.string.toast_error), Toast.LENGTH_SHORT);
                showProgress(false);
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    /**
     * 받아온 주문 정보를 리스트뷰에 넣습니다. 이때 알람을 띄울 수 있도록 DB에 저장합니다.
     * @param orders
     */
    private void insertOrderStatus(ArrayList<Order> orders) {
        if(orders != null && orders.size() > 0) {
            Collections.sort(orders, new Comparator<Order>() {
                @Override
                public int compare(Order lhs, Order rhs) {
                    return rhs.oid - lhs.oid;
                }
            });

            OrderStateAdapter orderStatusAdapter = new OrderStateAdapter(orders, mLayoutInflater);
            mOrderStateListView.setAdapter(orderStatusAdapter);
            mOrderStateListView.expandGroup(0);
            mTextViewEmpty.setVisibility(View.GONE);
        }
        else
            mTextViewEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        getOrderStatus();
    }

    protected class OrderStateAdapter extends BaseExpandableListAdapter implements View.OnClickListener, ModifyDialog.OnMenuSelectedListener, Response.Listener<JSONObject>, ModifyDateTimeDialog.OnDialogDismissListener {
        private ArrayList<Order> orders;
        private LayoutInflater mLayoutInflater;
        private int objectOrderId;

        public OrderStateAdapter(ArrayList<Order> orders, LayoutInflater mLayoutInflater) {
            this.orders = orders;
            this.mLayoutInflater = mLayoutInflater;
        }

        @Override
        public int getGroupCount() {
            return orders.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Order getGroup(int position) {
            return orders.get(position);
        }

        @Override
        public Order getChild(int groupPosition, int childPosition) {
            return orders.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
            OrderStatusViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_order_status, parent, false);
                holder = new OrderStatusViewHolder();
                holder.imageViewOrderStatus = (ImageView) convertView.findViewById(R.id.imageview_order_status);
                holder.textViewOrderStatus = (TextView) convertView.findViewById(R.id.textview_order_status);
                holder.textViewOrderNumber = (TextView) convertView.findViewById(R.id.textview_order_number_status);
                holder.textViewPickUpDateTime = (TextView) convertView.findViewById(R.id.textview_pick_up_time_status);
                holder.textViewDropOffDateTime = (TextView) convertView.findViewById(R.id.textview_drop_off_time_status);
                holder.textViewTotal = (TextView) convertView.findViewById(R.id.textview_total_status);
                holder.textViewTotalNumber = (TextView) convertView.findViewById(R.id.textview_item_number_status);
                convertView.setTag(holder);
            } else
                holder = (OrderStatusViewHolder) convertView.getTag();

//            holder.imageViewOrderStatus.setImageResource(getDrawableByStatus(getGroup(position).state));
            holder.textViewOrderStatus.setText(getStringByState(getGroup(position).state));
            holder.textViewOrderNumber.setText(getGroup(position).order_number);
            holder.textViewPickUpDateTime.setText(getGroup(position).pickup_date);
            holder.textViewDropOffDateTime.setText(getGroup(position).dropoff_date);
            holder.textViewTotal.setText(getTotalFromOrder(position) + getString(R.string.monetary_unit));
            holder.textViewTotalNumber.setText(getTotalNumberFromOrder(position) + getString(R.string.item_unit));

            return convertView;
        }

        @Override
        public View getChildView(int position, int groupPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            OrderStatusDetailViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.custom_order_status, parent, false);
                holder = new OrderStatusDetailViewHolder();
                holder.imageViewPdFace = (ImageView) convertView.findViewById(R.id.imageview_pd_face);
                holder.imageViewStatusBar = (ImageView) convertView.findViewById(R.id.imageview_status_bar);
                holder.textViewPDName = (TextView) convertView.findViewById(R.id.textview_pd_name);
                holder.textViewPickUpDateTime = (TextView) convertView.findViewById(R.id.textview_pick_up_time);
                holder.textViewDropOffDateTime = (TextView) convertView.findViewById(R.id.textview_drop_off_time);
                holder.buttonOrderItem = (Button) convertView.findViewById(R.id.button_order_item_child);
                holder.buttonTotalGross = (Button) convertView.findViewById(R.id.button_total);
                holder.buttonModifyOrder = (Button) convertView.findViewById(R.id.button_modify_order);
                holder.buttonFeedback = (Button) convertView.findViewById(R.id.button_feedback);
                convertView.setTag(holder);
            } else
                holder = (OrderStatusDetailViewHolder) convertView.getTag();

            int state = getGroup(position).state;

            holder.buttonModifyOrder.setVisibility(View.GONE);
            holder.buttonFeedback.setVisibility(View.GONE);

            // state에 따라 컴포넌트를 출력합니다
            switch (state) {
                case PICK_UP_WAIT:
                    holder.imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline1);
                    holder.buttonModifyOrder.setVisibility(View.VISIBLE);
                    break;

                case PICK_UP_MAN_SELECTED:
                    holder.imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline2);
                    holder.buttonModifyOrder.setVisibility(View.VISIBLE);
                    if (getGroup(position).pickupInfo != null) {
                        setPDFaceImage(holder.imageViewPdFace, getGroup(position).pickupInfo.img);
                        holder.textViewPDName.setText(getGroup(position).pickupInfo.name + " " + getString(R.string.pd_name_default));
                    }
                    break;

                case DELIVERY_MAN_SELECTED:
                    holder.imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline3);
                    if (getGroup(position).dropoffInfo != null) {
                            setPDFaceImage(holder.imageViewPdFace, getGroup(position).dropoffInfo.img);
                            holder.textViewPDName.setText(getGroup(position).dropoffInfo.name + " " + getString(R.string.pd_name_default));
                    }
                    break;

                case DELIVERY_FINISH:
                    holder.imageViewStatusBar.setImageResource(R.drawable.ic_order_status_timeline4);
                    holder.buttonFeedback.setVisibility(View.VISIBLE);

                default:
                    holder.imageViewPdFace.setImageResource(R.drawable.ic_sale);
                    holder.textViewPDName.setText(getString(R.string.pd_name_default));
                    break;
            }

            holder.textViewPickUpDateTime.setText(
                    DateTimeFactory.getInstance().getDate(getActivity(), getGroup(position).pickup_date) +
                    DateTimeFactory.getInstance().getNewLine() +
                    DateTimeFactory.getInstance().getTime(getActivity(), getGroup(position).pickup_date));
            holder.textViewDropOffDateTime.setText(
                    DateTimeFactory.getInstance().getDate(getActivity(), getGroup(position).dropoff_date) +
                    DateTimeFactory.getInstance().getNewLine() +
                    DateTimeFactory.getInstance().getTime(getActivity(), getGroup(position).dropoff_date));
            holder.textViewPickUpDateTime.setText(getString(R.string.label_item) + " " + DateTimeFactory.getInstance().getPrettyTime(getGroup(position).pickup_date));
            holder.textViewDropOffDateTime.setText(DateTimeFactory.getInstance().getPrettyTime(getGroup(position).dropoff_date));
            holder.buttonOrderItem.setText(getTotalNumberFromOrder(position) + getString(R.string.item_unit));
            holder.buttonOrderItem.setTag(getGroup(position));
            /* 총계를 구해 버튼에 새깁니다 */
            holder.buttonTotalGross.setText(getString(R.string.label_total) +
                    " " +
                    (getTotalFromOrder(position) + getGroup(position).dropoff_price - getGroup(position).mileage + getCouponTotal(getGroup(position))) +
                    getString(R.string.monetary_unit));
            holder.buttonTotalGross.setTag(getGroup(position));
            holder.buttonModifyOrder.setTag(getGroup(position));
            holder.buttonOrderItem.setOnClickListener(this);
            holder.buttonTotalGross.setOnClickListener(this);
            holder.buttonModifyOrder.setOnClickListener(this);
            holder.buttonFeedback.setOnClickListener(this);

            return convertView;
        }

        private int getTotalFromOrder(int position) {
            int total = 0;

            ArrayList<OrderItem> items = getGroup(position).item;
            for (OrderItem item : items) {
                total = total + item.price * item.count;
            }

            return total;
        }

        private int getCouponTotal(Order order) {
            if (order.coupon == null)
                return 0;

            int total = 0;

            for (Coupon coupon : order.coupon) {
                total = total + coupon.value;
            }

            return total;
        }

        private int getTotalNumberFromOrder(int position) {
            int total = 0;

            ArrayList<OrderItem> items = getGroup(position).item;
            for (OrderItem item : items) {
                total = total + item.count;
            }

            return total;
        }

        private void setPDFaceImage(ImageView imageView, String imageInfo) {
            ImageLoader imageLoader = RequestQueue.getInstance(getActivity()).getImageLoader();
            imageLoader.get(Config.SERVER_ADDRESS + imageInfo,
                    ImageLoader.getImageListener(
                            imageView, R.drawable.ic_sale, R.drawable.ic_sale
                    ));
        }

        private String getStringByState(int state) {
            String stateString = "";

            switch (state) {
                case PICK_UP_WAIT:
                    stateString = getString(R.string.pick_up_wait);
                    break;
                case PICK_UP_MAN_SELECTED:
                    stateString = getString(R.string.pick_up_man_selected);
                    break;
                case PICK_UP_FINISH:
                    stateString = getString(R.string.pick_up_finish);
                    break;
                case DELIVERY_MAN_SELECTED:
                    stateString = getString(R.string.deliverer_selected);
                    break;
                case DELIVERY_FINISH:
                    stateString = getString(R.string.deliverer_finish);
                    break;
            }

            return stateString;
        }

        protected class OrderStatusDetailViewHolder {
            private ImageView imageViewPdFace;
            private ImageView imageViewStatusBar;
            public TextView textViewPDName;
            public TextView textViewPickUpDateTime;
            public TextView textViewDropOffDateTime;
            public Button buttonOrderItem;
            public Button buttonTotalGross;
            public Button buttonModifyOrder;
            public Button buttonFeedback;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        protected class OrderStatusViewHolder {
            private ImageView imageViewOrderStatus;
            public TextView textViewOrderStatus;
            public TextView textViewOrderNumber;
            public TextView textViewPickUpDateTime;
            public TextView textViewDropOffDateTime;
            public TextView textViewTotal;
            public TextView textViewTotalNumber;
        }

        @Override
        public void onClick(View v) {
            Order order = (Order) v.getTag();

            if (order == null)
                return;

            switch (v.getId()) {
                case R.id.button_order_item_child:
                    ItemListDialog itemListDialog =
                            ItemListDialog.newInstance(order.item);

                    itemListDialog.show(
                            getActivity().getSupportFragmentManager(),
                            ITEM_LIST_DIALOG_TAG);
                    break;

                case R.id.button_total:
                    CalculationDialog calculationDialog =
                            CalculationDialog.newInstance(order);

                    calculationDialog.show(
                            getActivity().getSupportFragmentManager(),
                            TOTAL_DIALOG_TAG);
                    break;

                case R.id.button_modify_order:
                    ModifyDialog md = ModifyDialog.newInstance(this, order.oid);
                    md.show(getActivity().getSupportFragmentManager(), MODIFY_DIALOG_TAG);
                    break;

                case R.id.button_feedback:
                    break;
            }
        }

        @Override
        public void onMenuSelected(int mode, int oid) {
            switch (mode) {
                case MODIFY_ITEM:
                    OrderFragment f = new OrderFragment();
                    f.setOrderInfo(findOrderById(oid));

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
                    ((MainActivity) getActivity()).deleteAlarmFromDB(findOrderById(oid));
                    break;
            }
        }

        private Order findOrderById(int oid) {
            int position = 0;

            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).oid == oid)
                    position = i;
            }

            return orders.get(position);
        }

        /**
         * 주문을 삭제합니다
         * @param oid 삭제할 주문 번호
         */
        private void removeByOrderId(int oid) {
            int position = 0;

            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).oid == oid)
                    position = i;
            }

            orders.remove(position);
            notifyDataSetChanged();
        }

        private void cancelOrder(int oid) {
            showProgress(true);

            this.objectOrderId = oid;

            PostRequest postRequest = new PostRequest(getActivity());
            postRequest.setUrl(AddressManager.DEL_ORDER);
            postRequest.setParams("oid", String.valueOf(oid));
            postRequest.setListener(this, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), getString(R.string.toast_error), Toast.LENGTH_SHORT);
                    showProgress(false);
                }
            });
            RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest());
        }

        @Override
        public void onResponse(JSONObject response) {
            JsonData jsonData = getGson().fromJson(response.toString(), JsonData.class);
            switch (jsonData.constant) {
                case Constants.IMPOSSIBLE:
                    CleanBasketApplication.getInstance().showToast(getString(R.string.delete_impossible));
                    break;

                case Constants.SUCCESS:
                    CleanBasketApplication.getInstance().showToast(getString(R.string.order_delete_success));

                    ((MainActivity) getActivity()).deleteAlarmFromDB(findOrderById(objectOrderId));
                    ((MainActivity) getActivity()).cancelAlarm(findOrderById(objectOrderId));
                    ((MainActivity) getActivity()).setAlarm();
                    removeByOrderId(objectOrderId);
                    showProgress(false);
                    break;
            }
        }

        @Override
        public void onDialogDismiss() {
            getOrderStatus();
        }
    }

    private Gson getGson() {
        return CleanBasketApplication.getInstance().getGson();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mOrderStateListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mOrderStateListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOrderStateListView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}