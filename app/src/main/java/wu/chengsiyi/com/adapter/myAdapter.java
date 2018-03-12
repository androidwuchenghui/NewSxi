package wu.chengsiyi.com.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import wu.chengsiyi.com.newsxi.R;

/**
 * Created by ${Wu} on 2018/2/28.
 */

public class myAdapter extends BaseAdapter {
    private ArrayList<String> data;

    public myAdapter(ArrayList<String> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vHolder;
        if(convertView==null){
            //初始化item布局
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);

            //创建记事本类
            vHolder = new ViewHolder();

            //查找控件，保存控件的引用
            vHolder.tv = ((TextView) convertView.findViewById(R.id.item_tv));

            //将当前viewHolder与converView绑定
            convertView.setTag(vHolder);
        }else{
            //如果不为空，获取
            vHolder = (ViewHolder) convertView.getTag();
        }
        vHolder.tv.setText(data.get(position));

        return convertView;
    }

    class ViewHolder{
        TextView tv;
    }
}
