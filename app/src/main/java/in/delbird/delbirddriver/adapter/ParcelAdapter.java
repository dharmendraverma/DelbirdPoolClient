package in.delbird.delbirddriver.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.controller.Messages;
import in.delbird.delbirddriver.enums.PackageStatus;
import in.delbird.delbirddriver.model.ParcelModel;
import in.delbird.delbirddriver.utils.Utils;

import java.util.ArrayList;


/**
 * Created by Dharmendra on 1/27/16.
 */
public class ParcelAdapter extends BaseAdapter {
    Context context;
    ArrayList<ParcelModel> parcelModelArrayList;
    SharedPreferences preferences;

    public ParcelAdapter(Context context, ArrayList<ParcelModel> parcelModelArrayList) {
        this.context = context;
        this.parcelModelArrayList = parcelModelArrayList;
    }

    @Override
    public int getCount() {
        return parcelModelArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return parcelModelArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        preferences = parent.getContext().getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_view_parcel, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ParcelModel parcelModel = parcelModelArrayList.get(position);
        holder.name.setText(parcelModel.getReceiverName());
        holder.destinationAddres.setText(parcelModel.getFlatNumber() + ", " + parcelModel.getCity() + ",\n" +
                parcelModel.getState() + ", " + parcelModel.getPinCode());
        setPackageStatus(parcelModel.getStatus(), holder);

        holder.contactNumber.setText(parcelModel.getReceiverPhone());
        holder.contactNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.makeCall(context, parcelModel.getReceiverPhone());
            }
        });




        return convertView;
    }
    private void setPackageStatus(String status, ViewHolder holder) {
        if (status.equalsIgnoreCase(PackageStatus.INRIDE.toString())) {
            holder.status.setText(Messages.IN_RIDE);
            holder.status.setTextColor(context.getResources().getColor(R.color.blueColor));
        } else if (status.equalsIgnoreCase(PackageStatus.ONTRIP.toString())) {
            holder.status.setText(Messages.ON_TRIP);
            holder.status.setTextColor(context.getResources().getColor(R.color.blueColor));
        } else if (status.equalsIgnoreCase(PackageStatus.DELIVERED.toString())) {
            holder.status.setText(Messages.DELIVERED);
            holder.status.setTextColor(context.getResources().getColor(R.color.greenColor));
        } else if (status.equalsIgnoreCase(PackageStatus.CANCELLED.toString())) {
            holder.status.setText(Messages.CANCELLED);
            holder.status.setTextColor(context.getResources().getColor(R.color.redColor));
        }

    }
    public class ViewHolder {
        TextView name;
        TextView destinationAddres;
        TextView contactNumber;
        TextView status;
        ImageView options;

        public ViewHolder(View convertView) {
            name = (TextView) convertView.findViewById(R.id.name);
            destinationAddres = (TextView) convertView.findViewById(R.id.destination_address);
            contactNumber = (TextView) convertView.findViewById(R.id.phone_number);
            status = (TextView) convertView.findViewById(R.id.status);

        }
    }
}
