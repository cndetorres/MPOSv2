package com.lemonsquare.distrilitemposv2.Control;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.lemonsquare.distrilitemposv2.Model.ItemList;
import com.lemonsquare.distrilitemposv2.R;
import java.util.List;

public class ItemListControl extends ArrayAdapter<ItemList> {

    private Context context;
    private int layoutResourceId;
    private List<ItemList> lists;
    private TextView listItem;
    //ViewHolder holder;

    public ItemListControl(Context context, int layoutResourceId, List<ItemList> lists) {
        super(context, layoutResourceId, lists);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.lists = lists;
    }

   /* public class ViewHolder
    {
        TextView holderListItem;
    }*/


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //holder = new ViewHolder();
            if (convertView == null)
            {
                LayoutInflater ItemInflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = ItemInflate.inflate(layoutResourceId,parent,false);
                TextView materialName =(TextView) convertView.findViewById(R.id.rowsItem);
                materialName.setText(lists.get(position).getMaterialName());

            }
           /* else
            {
                holder = (ViewHolder) convertView.getTag();
            }*/
        return convertView;
    }

    public String getMaterialNameAtPosition(int position) {
        return lists.get(position).getMaterialName();
    }

    public String getMaterialCodeAtPosition(int position) {
        return lists.get(position).getMaterialCode();
    }
}