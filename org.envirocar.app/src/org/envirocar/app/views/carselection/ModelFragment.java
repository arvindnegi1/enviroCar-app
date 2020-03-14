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
import android.widget.TextView;

import org.envirocar.app.BaseApplicationComponent;
import org.envirocar.app.R;
import org.envirocar.app.handler.DAOProvider;
import org.envirocar.app.injection.BaseInjectorFragment;
import org.envirocar.core.entity.ManufacturerCar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ModelFragment extends BaseInjectorFragment {
    @BindView(R.id.model_fragment_download)
    View downloadView;
    @BindView(R.id.model_fragment_data)
    View showData;
    @BindView(R.id.autoComplete)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.mode_fragment_list)
    ListView listView;

    @Inject
    protected DAOProvider daoProvider;

    private Scheduler.Worker mainThreadWorker = AndroidSchedulers.mainThread().createWorker();
    private CompositeDisposable disposables = new CompositeDisposable();
    private List<String> mModelName = new ArrayList<>();
    private Map<String,List<String>> mModelYear = new HashMap<>();
    private Set<String> mModelSet = new LinkedHashSet<>();
    String manufid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_model, container, false);
        ButterKnife.bind(this,view);
        if (getArguments() != null) {
             manufid = getArguments().getString("manufid");
            manufacturerCar(manufid);
        }
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                jumpToFragment(adapterView.getItemAtPosition(i).toString());
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                jumpToFragment(adapterView.getItemAtPosition(i).toString());
            }
        });
        return view;
    }
    private void manufacturerCar(String carId){
        disposables.add(daoProvider.getSensorNewDAO()
                .getAllManufacturerCarObservable(carId)
                .toFlowable(BackpressureStrategy.BUFFER)
                .onBackpressureBuffer(10000)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toObservable()
                .subscribeWith(new DisposableObserver<List<ManufacturerCar>>() {

                    @Override
                    protected void onStart() {
                        downloadView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        mainThreadWorker.schedule(() -> {
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,mModelName);
                            showData.setVisibility(View.VISIBLE);
                            autoCompleteTextView.setAdapter(arrayAdapter);
                            listView.setAdapter(arrayAdapter);
                            downloadView.setVisibility(View.INVISIBLE);
                            dispose();
                        });

                    }

                    @Override
                    public void onNext(List<ManufacturerCar> manufacturerCars) {
                        mModelName.clear();
                        mModelYear.clear();
                        mModelSet.clear();
                        for(ManufacturerCar manufacturerCar : manufacturerCars) {
                            if (!mModelYear.containsKey(manufacturerCar.getCommercialName()))
                                mModelYear.put(manufacturerCar.getCommercialName(), new ArrayList<>());
                            mModelYear.get(manufacturerCar.getCommercialName()).add(manufacturerCar.getTsn());
                            mModelSet.add(manufacturerCar.getCommercialName());
                        }
                        mModelName.addAll(mModelSet);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));

    }

    @Override
    protected void injectDependencies(BaseApplicationComponent baseApplicationComponent) {
        baseApplicationComponent.inject(this);
    }
    void jumpToFragment(String selected) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
       YearFragment yearFragment = new YearFragment();
        Bundle args = new Bundle();
        List<String> ss = mModelYear.get(selected);
        ArrayList<String> ls = new ArrayList<>(ss.size());
        ls.addAll(ss);
        args.putStringArrayList("manufacturerCarYear",ls);
        args.putString("manufid",manufid);
        yearFragment.setArguments(args);
        fragmentTransaction.replace(R.id.activity_car_selection_fragment,yearFragment);
        fragmentTransaction.commit();
    }
}
