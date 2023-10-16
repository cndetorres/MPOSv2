package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomMainListActivity extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] text;
    private final Integer[] icon;

    public CustomMainListActivity(Activity context,
                                  String[] text, Integer[] icon) {
        super(context, R.layout.item_main, text);
        this.context = context;
        this.text = text;
        this.icon = icon;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.item_main, null, true);
        ImageView ivICon = (ImageView) rowView.findViewById(R.id.ivIcon);
        TextView txText = (TextView) rowView.findViewById(R.id.rowsText);
        txText.setText(text[position]);

        ivICon.setImageResource(icon[position]);
        return rowView;
    }
}
