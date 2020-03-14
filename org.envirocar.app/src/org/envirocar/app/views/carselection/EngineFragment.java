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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EngineFragment extends Fragment {

    @BindView(R.id.autoComplete)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.engine_fragment_list)
    ListView listView;

    private Map<Integer,List<CarNew>> mModelYearFuelEngineToPower = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_engine, container, false);
        ButterKnife.bind(this,view);
        if (getArguments() != null) {
            ArrayList<CarNew> carNews = new ArrayList<>();
            carNews = (ArrayList<CarNew>) getArguments().getSerializable("engine");
            mModelYearFuelEngineToPower.clear();
            Set<Integer> engineSet = new LinkedHashSet<>();
            List<Integer> engine = new ArrayList<>();
            for(CarNew carNew : carNews) {
                engineSet.add(carNew.getEngineCapacity());
                if (!mModelYearFuelEngineToPower.containsKey(carNew.getEngineCapacity()))
                    mModelYearFuelEngineToPower.put(carNew.getEngineCapacity(), new ArrayList<>());
                mModelYearFuelEngineToPower.get(carNew.getEngineCapacity()).add(carNew);
            }
            engine.addAll(engineSet);
            ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(getContext(),android.R.layout.simple_dropdown_item_1line,engine);
            listView.setAdapter(arrayAdapter);
            autoCompleteTextView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Integer engineSelected = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                    jumpToFragment(engineSelected);
                }
            });
        }
        return view;
    }

    void jumpToFragment(Integer engineSelected) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PowerFragment powerFragment = new PowerFragment();
        List<CarNew> carNews = mModelYearFuelEngineToPower.get(engineSelected);
        ArrayList<CarNew> arrayList = new ArrayList<>(carNews.size());
        arrayList.addAll(carNews);
        Bundle args = new Bundle();
        args.putSerializable("power",arrayList);
        powerFragment.setArguments(args);
        fragmentTransaction.replace(R.id.activity_car_selection_fragment,powerFragment);
        fragmentTransaction.commit();
    }
}
