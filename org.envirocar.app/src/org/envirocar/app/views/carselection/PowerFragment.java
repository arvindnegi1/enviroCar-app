package org.envirocar.app.views.carselection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import org.envirocar.app.R;
import org.envirocar.core.entity.CarNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PowerFragment extends Fragment {

    @BindView(R.id.autoComplete)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.power_fragment_list)
    ListView listView;

    private Map<Integer,List<CarNew>> mModelPowerToCar = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_power, container, false);
        ButterKnife.bind(this,view);
        if (getArguments() != null) {
            ArrayList<CarNew> carNews = new ArrayList<>();
            carNews = (ArrayList<CarNew>) getArguments().getSerializable("power");
            List<Integer> power = new ArrayList<>();
            mModelPowerToCar.clear();
            for(CarNew carNew : carNews) {
                power.add(carNew.getPower());
                if (!mModelPowerToCar.containsKey(carNew.getPower()))
                    mModelPowerToCar.put(carNew.getPower(), new ArrayList<>());
                mModelPowerToCar.get(carNew.getPower()).add(carNew);
            }
            ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(getContext(),android.R.layout.simple_dropdown_item_1line,power);
            listView.setAdapter(arrayAdapter);
            autoCompleteTextView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Integer powerSelected = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                    jumpToFragment(powerSelected);
                }
            });
        }
        return view;
    }
    void jumpToFragment(Integer powerSelected) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ReadyCar readyCar = new ReadyCar();
        List<CarNew> carNews = mModelPowerToCar.get(powerSelected);
        ArrayList<CarNew> arrayList = new ArrayList<>(carNews.size());
        arrayList.addAll(carNews);
        Bundle args = new Bundle();
        args.putSerializable("readyCar",arrayList);
        readyCar.setArguments(args);
        fragmentTransaction.replace(R.id.activity_car_selection_fragment,readyCar);
        fragmentTransaction.commit();
    }
}
