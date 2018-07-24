package in.delbird.delbirddriver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.enums.PaymentMode;
import in.delbird.delbirddriver.model.HistoryModel;
import in.delbird.delbirddriver.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Dharmendra on 27/1/16.
 */
public class HistoryAdapter extends BaseAdapter {
    ArrayList<HistoryModel> data;
    Context mcontext;

    public HistoryAdapter(Context context, ArrayList<HistoryModel> data) {
        mcontext = context;
        this.data = data;
    }

    public void changeData(ArrayList<HistoryModel> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflate = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflate.inflate(R.layout.history_items, parent, false);
            HistoryModel getdata = data.get(position);
            ViewHolder viewholder = new ViewHolder(convertView);
            viewholder.history_time.setText(Utils.convertLongTimeToStringDate(getdata.getCreationTime()));
            viewholder.history_details.setText(getdata.getRideType());
            viewholder.history_price.setText(getdata.getCost() + "");
            viewholder.history_status.setText(getdata.getStatus());
            if (getdata.getPaymentMode().equalsIgnoreCase(PaymentMode.CASH.toString())) {
                viewholder.paymnetMode.setText(PaymentMode.CASH.toString());
            } else if (getdata.getPaymentMode().equalsIgnoreCase(PaymentMode.Delbird_WALLET.toString())) {
                viewholder.paymnetMode.setText("Wallet");

            }
            if (getdata.getPicUrl() != null && getdata.getPicUrl().length() > 0) {
                Picasso.with(mcontext).load(getdata.getPicUrl()).placeholder(R.drawable.progress_animation).into(viewholder.circular_image);
            } else {
                viewholder.circular_image.setImageResource(R.drawable.pro_pic);
            }
            // viewholder.circular_image.setImageResource(Integer.parseInt(getdata.getCircular_image().toString()));
        }
        return convertView;
    }

    public class ViewHolder {
        TextView history_time, history_details, history_price, history_status,paymnetMode;
        ImageView circular_image, rupee_icon;

        ViewHolder(View view) {
            history_time = (TextView) view.findViewById(R.id.history_time);
            history_details = (TextView) view.findViewById(R.id.history_details);
            history_price = (TextView) view.findViewById(R.id.history_price);
            history_status = (TextView) view.findViewById(R.id.history_status);
            circular_image = (ImageView) view.findViewById(R.id.circular_image);
            rupee_icon = (ImageView) view.findViewById(R.id.history_rupeeicon);
            paymnetMode = (TextView) view.findViewById(R.id.payment_mode);

        }

    }
}
