package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.washappkorea.corp.cleanbasket.R;

import java.util.ArrayList;
import java.util.List;

public class ClassDialog extends DialogFragment implements View.OnClickListener {
    private static final String TAG = ClassDialog.class.getSimpleName();

    private ListView mClassListView;
    private Button mCancelButton;
    private View mClassFormView;

    private ClassAdapter mClassAdapter;

    public static ClassDialog newInstance() {
        ClassDialog ild = new ClassDialog();
        ild.initialize();

        return ild;
    }

    public void initialize() {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View rootView = inflater.inflate(R.layout.dialog_class, container, false);

        mClassListView = (ListView) rootView.findViewById(R.id.listview_class_list);
        mCancelButton = (Button) rootView.findViewById(R.id.button_cancel_class_list);
        mCancelButton.setOnClickListener(this);

        ArrayList<ClassInfo> list = new ArrayList<ClassInfo>();

        list.add(new ClassInfo(null,"class_label", "class_benefit", getString(R.string.class_total)));
        list.add(new ClassInfo("clean", "bronze_basket", "bronze_info", "0"));
        list.add(new ClassInfo("silver", "silver_basket", "silver_info", "100000"));
        list.add(new ClassInfo("gold", "gold_basket", "gold_info", "300000"));
        list.add(new ClassInfo("love", "love_basket", "love_info", "500000"));

        mClassAdapter = new ClassAdapter(getActivity(), 0, list);
        mClassListView.setAdapter(mClassAdapter);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel_class_list:
                dismiss();
        }
    }

    class ClassInfo {
        ClassInfo(String img, String name, String benefit, String total) {
            this.img = img;
            this.name = name;
            this.benefit = benefit;
            this.total = total;
        }

        String img;
        String name;
        String benefit;
        String total;
    }

    class ClassAdapter extends ArrayAdapter<ClassInfo> {
        private LayoutInflater mLayoutInflater;

        ClassAdapter(Context context, int resource, List<ClassInfo> objects) {
            super(context, resource, objects);

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CouponListViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_class, parent, false);
                holder = new CouponListViewHolder();
                holder.imageViewClassInfo = (ImageView) convertView.findViewById(R.id.imageview_class);
                holder.textViewClassName = (TextView) convertView.findViewById(R.id.textview_class_name);
                holder.textViewClassBenefit = (TextView) convertView.findViewById(R.id.textview_class_benefit);
                holder.textViewClassTotal = (TextView) convertView.findViewById(R.id.textview_class_total);
                convertView.setTag(holder);
            } else
                holder = (CouponListViewHolder) convertView.getTag();

            holder.imageViewClassInfo.setImageResource(getDrawableByString(getItem(position).img));
            holder.textViewClassName.setText(getStringByString(getItem(position).name));
            holder.textViewClassBenefit.setText(getStringByString(getItem(position).benefit));
            if (position > 0)
                holder.textViewClassTotal.setText(getItem(position).total + getString(R.string.monetary_unit));
            else
                holder.textViewClassTotal.setText(getItem(position).total);

            return convertView;
        }

        /* 이름으로 아이콘을 가져옵니다 */
        public int getDrawableByString(String name) {
            return getContext().getResources().getIdentifier("ic_class_" + name, "drawable", getContext().getPackageName());
        }

        /* 이름으로 문자열을 가져옵니다 */
        public String getStringByString(String name) {
            int resourceId = getActivity().getResources().getIdentifier(name, "string", getActivity().getPackageName());

            String result = "";

            try {
                result = getActivity().getString(resourceId);
            } catch (Exception e) {
                return getActivity().getString(R.string.default_name);
            }

            return result;
        }

        protected class CouponListViewHolder {
            public ImageView imageViewClassInfo;
            public TextView textViewClassName;
            public TextView textViewClassBenefit;
            public TextView textViewClassTotal;
        }
    }
}