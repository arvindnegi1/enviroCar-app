package org.envirocar.app.views.carselection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.envirocar.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Manufacturer_fragment extends Fragment {
@BindView(R.id.fragment_manufacturer_grid)
    GridView gridView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manufacturer_fragment, container, false);
        ButterKnife.bind(this,view);
        int Res[]={R.drawable.img_envirocar_logo,R.drawable.img_envirocar_logo,R.drawable.img_envirocar_logo,
                R.drawable.img_envirocar_logo,R.drawable.img_envirocar_logo};
        String items[] = new String[]{"AUDI","BMW","JAGUAR","HYUNDAI","MARUTI SUZUKI"};
        CustomGridAdapter customGridAdapter = new CustomGridAdapter(getContext(),items,Res);
        gridView.setAdapter(customGridAdapter);
        return view;
    }

}
