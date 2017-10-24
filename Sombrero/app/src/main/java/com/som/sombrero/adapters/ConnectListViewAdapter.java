package com.som.sombrero.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.som.sombrero.R;

import java.util.ArrayList;
import java.util.Collection;

public class ConnectListViewAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mDataSet;

    public ConnectListViewAdapter() {
        this.mDataSet = new ArrayList<>();
    }

    public ConnectListViewAdapter(@NonNull ArrayList<BluetoothDevice> mDataSet) {
        this.mDataSet = mDataSet;
    }

    @Override
    public int getCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.view_connect_list_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.view_connect_list_item_name))
                .setText(getItem(position).getName());

        ((TextView) convertView.findViewById(R.id.view_connect_list_item_address))
                .setText(getItem(position).getAddress());

        return convertView;
    }

    public void addToDataSet(BluetoothDevice device) {
        this.mDataSet.add(device);
        notifyDataSetChanged();
    }

    public void addAllToDataSet(Collection<BluetoothDevice> mDataSet) {
        this.mDataSet.addAll(mDataSet);
        notifyDataSetChanged();
    }

    public void setDataSet(ArrayList<BluetoothDevice> mDataSet) {
        this.mDataSet = mDataSet;
        notifyDataSetChanged();
    }

    public ArrayList<BluetoothDevice> getDataSet() {
        return mDataSet;
    }
}
