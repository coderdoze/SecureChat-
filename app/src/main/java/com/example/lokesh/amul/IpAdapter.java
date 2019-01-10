package com.example.lokesh.amul;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by lokesh on 27/10/18.
 */

public class IpAdapter extends ArrayAdapter {
    int resource;
    Context context;
    public IpAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.context= context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if(listItemView == null){
           listItemView= LayoutInflater.from(context)
                    .inflate(R.layout.list_item, parent, false);

        }
        Ip ip = (Ip)getItem(position);
        TextView deviceView = listItemView.findViewById(R.id.add);
        deviceView.setText(ip.getDevice());

        TextView ipView = listItemView.findViewById(R.id.ip);
        ipView.setText(ip.getIp());
        return listItemView;
    }
}
