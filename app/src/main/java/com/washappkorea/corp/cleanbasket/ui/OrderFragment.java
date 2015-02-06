package com.washappkorea.corp.cleanbasket.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.OrderService;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.widget.OrderItemAdapter;
import com.washappkorea.corp.cleanbasket.widget.OrderItemsView;

import java.util.ArrayList;

public class OrderFragment extends Fragment implements AdapterView.OnItemClickListener {
    private OrderItemsView mOrderItemsView;
    private OrderItemAdapter mOrderItemAdapter;
    private OrderService mOrderService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);

        mOrderItemsView = (OrderItemsView) rootView.findViewById(R.id.gridview_item);

        mOrderItemAdapter = new OrderItemAdapter(getActivity(), R.layout.item_orderitem);
        Button mOrderButton = (Button) rootView.findViewById(R.id.button_order);

        mOrderItemsView.setNumColumns(3);
        mOrderItemsView.setAreHeadersSticky(true);
        mOrderItemsView.setAdapter(mOrderItemAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // OrderCategory는 서버에 요청해서 받아옵니다
        ArrayList<OrderCategory> orderCategoryList = new ArrayList<OrderCategory>();
        orderCategoryList.add(new OrderCategory(0, "추천"));
        orderCategoryList.add(new OrderCategory(1, "비즈니스"));
        orderCategoryList.add(new OrderCategory(2, "상의"));
        mOrderItemAdapter.setOrderCategoryList(orderCategoryList);

        // 아이템들은 서버에 요청해서 받아옵니다
        mOrderItemAdapter.add(new OrderItem("티셔츠", 2000, 0, "ic_t_shirt"));
        mOrderItemAdapter.add(new OrderItem("와이셔츠", 2000, 1, "ic_shirt"));
        mOrderItemAdapter.add(new OrderItem("티셔츠", 2000, 2, "ic_t_shirt"));
        mOrderItemAdapter.add(new OrderItem("와이셔츠", 2000, 2, "ic_shirt"));
        mOrderItemAdapter.add(new OrderItem("티셔츠", 2000, 2, "ic_t_shirt"));
        mOrderItemAdapter.add(new OrderItem("와이셔츠", 2000, 2, "ic_shirt"));
        mOrderItemAdapter.add(new OrderItem("티셔츠", 2000, 3, "ic_t_shirt"));
        mOrderItemAdapter.add(new OrderItem("와이셔츠", 2000, 3, "ic_shirt"));

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mOrderItemAdapter.getItem(position) != null) {
            mOrderItemAdapter.getItem(position).count++;
        }

        mOrderItemAdapter.notifyDataSetChanged();
    }

    private OrderService getOrderService() {
        if (mOrderService == null) {
            mOrderService = ((MainActivity) getActivity()).getRestAdapter().create(OrderService.class);
        }

        return mOrderService;
    }
}