package com.aqua.copy_url;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by teimeiu on 2017/05/20.
 */

public class MyAdapter extends BaseAdapter {

    private List<Project> stuList;
    private LayoutInflater inflater;

    public MyAdapter() {
    }
    private int selectedPosition = 0;// 选中的位置
    public void setSelectedPosition(int position) {
        selectedPosition = position;
//        if (selectedPosition == position) {
//            itemlayoutb.setBackgroundColor(Color.parseColor("#ffffff"));
//            textc.setTextColor(Color.parseColor("#ff0000"));
//        } else {
//            itemlayoutb.setBackgroundColor(Color.TRANSPARENT);
//            textc.setTextColor(Color.parseColor("#393939"));
//        }
    }
    public MyAdapter(List<Project> stuList, Context context) {
        this.stuList = stuList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return stuList == null ? 0 : stuList.size();
    }

    @Override
    public Project getItem(int position) {
        return stuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //加载布局为一个视图
        View view = inflater.inflate(R.layout.vlist, null);
        Project pro = getItem(position);
        //在view视图中查找id为image_photo的控件
        ImageView image_photo = (ImageView) view.findViewById(R.id.image_photo);
        TextView tv_name = (TextView) view.findViewById(R.id.title);
        TextView tv_age = (TextView) view.findViewById(R.id.url);
        image_photo.setImageResource(pro.getPhoto());
        tv_name.setText(pro.getTitle());
        tv_age.setText(String.valueOf(pro.getUrl()));
        return view;
    }
}