package in.delbird.delbirddriver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import in.delbird.delbirddriver.R;


/**
 * Created by Dharmendra on 5/25/15.
 */
public class MenuAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    String[] menu_name = {"History", "Help", "About us"};
    private int selectedPosition = 0;

    public MenuAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return menu_name.length;
    }

    @Override
    public Object getItem(int position) {
        return menu_name[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_single_row, null);
        TextView textView = (TextView) convertView.findViewById(R.id.menu_text);
        textView.setText(menu_name[position]);


        return convertView;
    }


}
