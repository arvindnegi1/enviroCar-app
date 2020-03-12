package org.envirocar.app.views.carselection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import org.envirocar.app.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Manufacturer_fragment extends Fragment {
@BindView(R.id.fragment_manufacturer_grid)
    GridView gridView;
@BindView(R.id.fragment_manufacturer_list)
    ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manufacturer_fragment, container, false);
        ButterKnife.bind(this,view);
        int Res[]={R.drawable.bmw_logo,R.drawable.alpina_logo,R.drawable.audi_logo,
                R.drawable.merce_logo,R.drawable.keinath_logo,R.drawable.gumpert_logo,R.drawable.volkswagen_logo,R.drawable.opel_logo,R.drawable.porshce_logo};
        String items[] = new String[]{"BMW","ALPINA","AUDI","MERCEDES","KEINATH","GUMPERT","VOLKSWAGEN","OPEL","PORSHCE"};
        CustomGridAdapter customGridAdapter = new CustomGridAdapter(getContext(),items,Res);
        ArrayList<String> mmanufacturernames = new ArrayList<>();
        if (getArguments() != null) {
            mmanufacturernames = getArguments().getStringArrayList("manu");
            Collections.sort(mmanufacturernames);
        }
        listView.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,mmanufacturernames));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedManufacturer = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getContext(),""+selectedManufacturer,Toast.LENGTH_SHORT).show();
            }
        });
        gridView.setAdapter(customGridAdapter);
        return view;
    }

}
