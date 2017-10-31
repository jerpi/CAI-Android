package com.som.sombrero.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.som.sombrero.R;

import java.util.ArrayList;
import java.util.List;

public class ConnectListViewAdapter extends BaseAdapter {

    private List<BluetoothDevice> mDataSet;

    public ConnectListViewAdapter() {
        this.mDataSet = new ArrayList<>();
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

        BluetoothDevice device = getItem(position);

        ((TextView) convertView.findViewById(R.id.connect_list_item_name))
                .setText(device.getName());

        ((TextView) convertView.findViewById(R.id.connect_list_item_address))
                .setText(device.getAddress());

        ((CheckBox) convertView.findViewById(R.id.connect_list_item_is_paired))
                .setChecked(device.getBondState() == BluetoothDevice.BOND_BONDED);

        return convertView;
    }

    public void addToDataSet(BluetoothDevice device) {
        this.mDataSet.add(device);
        notifyDataSetChanged();
    }

    public List<BluetoothDevice> getDataSet() {
        return mDataSet;
    }
}
