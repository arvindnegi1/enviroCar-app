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
import android.widget.Toast;

import org.envirocar.app.BaseApplicationComponent;
import org.envirocar.app.R;
import org.envirocar.app.handler.DAOProvider;
import org.envirocar.app.injection.BaseInjectorFragment;
import org.envirocar.core.entity.Car;
import org.envirocar.core.entity.CarNew;
import org.envirocar.core.entity.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FuelFragment extends BaseInjectorFragment {

    @BindView(R.id.autoComplete)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.fuel_fragment_list)
    ListView listView;

    @Inject
    protected DAOProvider daoProvider;

    private Map<String,List<CarNew>> mModelYearFuelToEngine = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fuel, container, false);
        ButterKnife.bind(this,view);
        if (getArguments() != null) {
            ArrayList<CarNew> selected = new ArrayList<>();
            selected = (ArrayList<CarNew>) getArguments().getSerializable("yearSelected");
            mModelYearFuelToEngine.clear();
            Set<String> fuelSet = new LinkedHashSet<>();
            List<String> fuel = new ArrayList<>();
            for(CarNew carNew : selected) {
                List<Link> allLinks = carNew.getLinks();
                fuelSet.add(allLinks.get(1).getTitle());
                if (!mModelYearFuelToEngine.containsKey(allLinks.get(1).getTitle()))
                    mModelYearFuelToEngine.put(allLinks.get(1).getTitle(), new ArrayList<>());
                mModelYearFuelToEngine.get(allLinks.get(1).getTitle()).add(carNew);
            }
            fuel.addAll(fuelSet);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,fuel);
            listView.setAdapter(arrayAdapter);
            autoCompleteTextView.setAdapter(arrayAdapter);

        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String seelcted = adapterView.getItemAtPosition(i).toString();
                jumpToFragment(seelcted);
            }
        });
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String seelcted = adapterView.getItemAtPosition(i).toString();
                jumpToFragment(seelcted);
            }
        });
        return view;
    }

    @Override
    protected void injectDependencies(BaseApplicationComponent baseApplicationComponent) {
        baseApplicationComponent.inject(this);
    }

    void jumpToFragment(String fuelSelected) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        EngineFragment engineFragment = new EngineFragment();
        List<CarNew> carNews = mModelYearFuelToEngine.get(fuelSelected);
        ArrayList<CarNew> arrayList = new ArrayList<>(carNews.size());
        arrayList.addAll(carNews);
        Bundle args = new Bundle();
        args.putSerializable("engine",arrayList);
        engineFragment.setArguments(args);
        fragmentTransaction.replace(R.id.activity_car_selection_fragment,engineFragment);
        fragmentTransaction.commit();
    }
}
