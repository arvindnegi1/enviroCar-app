/**
 * Copyright (C) 2013 - 2019 the enviroCar community
 * <p>
 * This file is part of the enviroCar app.
 * <p>
 * The enviroCar app is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * The enviroCar app is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with the enviroCar app. If not, see http://www.gnu.org/licenses/.
 */
package org.envirocar.app.views.carselection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.BlockedNumberContract;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding3.appcompat.RxToolbar;
import com.jakewharton.rxbinding3.widget.RxTextView;

import org.envirocar.app.R;
import org.envirocar.app.handler.preferences.CarPreferenceHandler;
import org.envirocar.app.handler.DAOProvider;
import org.envirocar.app.injection.BaseInjectorFragment;
import org.envirocar.app.BaseApplicationComponent;
import org.envirocar.app.views.utils.ECAnimationUtils;
import org.envirocar.core.entity.Car;
import org.envirocar.core.entity.CarImpl;
import org.envirocar.core.entity.CarNew;
import org.envirocar.core.entity.Link;
import org.envirocar.core.entity.Manufacturer;
import org.envirocar.core.entity.ManufacturerCar;
import org.envirocar.core.exception.DataRetrievalFailureException;
import org.envirocar.core.logging.Logger;
import org.envirocar.remote.service.CarServiceNew;
import org.envirocar.remote.service.EnviroCarService;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * TODO JavaDoc
 *
 * @author dewall
 */
public class CarSelectionAddCarFragment extends BaseInjectorFragment {
    private static final Logger LOG = Logger.getLogger(CarSelectionAddCarFragment.class);


    @BindView(R.id.envirocar_toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.activity_car_selection_newcar_content_view)
    protected View contentView;
    @BindView(R.id.activity_car_selection_newcar_download_layout)
    protected View downloadView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Inject
    protected DAOProvider daoProvider;
    @Inject
    protected CarPreferenceHandler carManager;

    private CompositeDisposable disposables = new CompositeDisposable();
    private Scheduler.Worker mainThreadWorker = AndroidSchedulers.mainThread().createWorker();

    private Set<String> mManufacturerNames = new HashSet<>();
    private Map<String,String> hsn = new ConcurrentHashMap<>();
    Bundle manufacturer_args;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.activity_car_selection_newcar_fragment, container, false);
        ButterKnife.bind(this, view);

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.inflateMenu(R.menu.menu_logbook_add_fueling);
        toolbar.setNavigationOnClickListener(v -> {
            hideKeyboard(v);
            closeThisFragment();
        });
        manufacturer_args = new Bundle();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        String items[] = new String[]{"Manufacturer","Model","Year","Fuel","Engine","Power"};
        int Res[] = {R.drawable.ic_manufacturer,
                R.drawable.ic_directions_car_black_24dp,
                R.drawable.ic_date_24dp,
                R.drawable.ic_gas,
                R.drawable.ic_engine,
                R.drawable.power};
        recyclerView.setAdapter(new CustomAdapter(getContext(),items,Res));
        // initially we set the toolbar exp to gone
        toolbar.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        downloadView.setVisibility(View.GONE);

        //add root fragment

        showManufacturer();

        return view;
    }

    private void showManufacturer() {
        disposables.add(daoProvider.getSensorNewDAO()
                .getAllManufactureObservable()
                .toFlowable(BackpressureStrategy.BUFFER)
                .onBackpressureBuffer(10000)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toObservable()
                .subscribeWith(new DisposableObserver<List<Manufacturer>>() {

                    @Override
                    protected void onStart() {
                        LOG.info("onStart() download sensors");
                        downloadView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {

                        mainThreadWorker.schedule(() -> {
                           dispose();
                            downloadView.setVisibility(View.GONE);
                        });
                        FragmentManager fragmentManager = getChildFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        Manufacturer_fragment manufacturer_fragment = new Manufacturer_fragment();
                        ArrayList<String> mname = new ArrayList<>();
                        mname.addAll(mManufacturerNames);
                        manufacturer_args.putStringArrayList("manu",mname);
                        manufacturer_args.putSerializable("hsn_map", (Serializable) hsn);
                        manufacturer_fragment.setArguments(manufacturer_args);
                        fragmentTransaction.replace(R.id.activity_car_selection_fragment,manufacturer_fragment);
                        fragmentTransaction.commit();


                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<Manufacturer> manufacturers) {
                        for (Manufacturer manufacturer : manufacturers) {
                            if (manufacturer != null) {
                            hsn.put(manufacturer.getName(),manufacturer.getHsn());
                                mManufacturerNames.add(manufacturer.getName());
                            }
                        }
                    }
                }));

    }

    @Override
    public void onResume() {
        LOG.info("onResume()");
        super.onResume();
        ECAnimationUtils.animateShowView(getContext(), toolbar,
                R.anim.translate_slide_in_top_fragment);
        ECAnimationUtils.animateShowView(getContext(), contentView,
                R.anim.translate_slide_in_bottom_fragment);
    }

    @Override
    public void onDestroy() {
        LOG.info("onDestroy()");

        // release all disposables.
        disposables.clear();
        super.onDestroy();
    }


    public void closeThisFragment() {
        // ^^
        ECAnimationUtils.animateHideView(getContext(),
                ((CarSelectionActivity) getActivity()).overlayView, R.anim.fade_out);
        ECAnimationUtils.animateHideView(getContext(), contentView, R.anim
                .translate_slide_out_bottom, () -> ((CarSelectionUiListener) getActivity()).onHideAddCarFragment());
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void injectDependencies(BaseApplicationComponent appComponent) {
        appComponent.inject(this);
    }
}
