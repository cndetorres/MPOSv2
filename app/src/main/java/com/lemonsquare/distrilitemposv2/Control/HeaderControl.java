package com.lemonsquare.distrilitemposv2.Control;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lemonsquare.distrilitemposv2.Model.HeaderList;
import com.lemonsquare.distrilitemposv2.R;
import java.util.List;

public class HeaderControl extends ArrayAdapter<HeaderList> {

    private Context context;
    private int layoutResourceId;
    private List<HeaderList> lists;

    public HeaderControl(Context context, int layoutResourceId, List<HeaderList> lists) {
        super(context, layoutResourceId, lists);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.lists = lists;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            LayoutInflater ItemInflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = ItemInflate.inflate(layoutResourceId,parent,false);
            TextView firstColumn =(TextView) convertView.findViewById(R.id.rowsItem);
            TextView secondColumn =(TextView) convertView.findViewById(R.id.rowsQty);
            TextView thirdColumn =(TextView) convertView.findViewById(R.id.rowsUnit);
            firstColumn.setText(lists.get(position).getFirstColumn());
            secondColumn.setText(lists.get(position).getSecondColumn());
            thirdColumn.setText(lists.get(position).getThirdColumn());

            firstColumn.setTextColor(Color.WHITE);
            secondColumn.setTextColor(Color.WHITE);
            thirdColumn.setTextColor(Color.WHITE);
            firstColumn.setTypeface(null, Typeface.BOLD);
            secondColumn.setTypeface(null, Typeface.BOLD);
            thirdColumn.setTypeface(null, Typeface.BOLD);
            convertView.setBackgroundColor(Color.parseColor("#FF9800"));

        }
        return convertView;
    }

}
