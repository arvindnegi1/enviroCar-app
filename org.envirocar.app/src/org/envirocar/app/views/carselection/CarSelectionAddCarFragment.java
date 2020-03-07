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
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * TODO JavaDoc
 *
 * @author dewall
 */
public class CarSelectionAddCarFragment extends BaseInjectorFragment {
    private static final Logger LOG = Logger.getLogger(CarSelectionAddCarFragment.class);

    private static final int ERROR_DEBOUNCE_TIME = 750;
    private static final int CONSTRUCTION_YEAR_MIN = 1990;
    private static final int CONSTRUCTION_YEAR_MAX = Calendar.getInstance().get(Calendar.YEAR);
    private static final int ENGINE_DISPLACEMENT_MIN = 500;
    private static final int ENGINE_DISPLACEMENT_MAX = 5000;

    @BindView(R.id.envirocar_toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.activity_car_selection_newcar_toolbar_exp)
    protected View toolbarExp;
    @BindView(R.id.activity_car_selection_newcar_content_view)
    protected View contentView;
    @BindView(R.id.activity_car_selection_newcar_download_layout)
    protected View downloadView;

    @BindView(R.id.activity_car_selection_newcar_layout_manufacturer)
    protected TextInputLayout manufacturerLayout;
    @BindView(R.id.activity_car_selection_newcar_input_manufacturer)
    protected AutoCompleteTextView manufacturerText;
    @BindView(R.id.activity_car_selection_newcar_input_model)
    protected AutoCompleteTextView modelText;
    @BindView(R.id.activity_car_selection_newcar_input_constructionyear)
    protected AutoCompleteTextView yearText;
    @BindView(R.id.activity_car_selection_newcar_input_fueltype)
    protected AutoCompleteTextView fueltypeText;
    @BindView(R.id.activity_car_selection_newcar_layout_engine)
    protected TextInputLayout engineLayout;
    @BindView(R.id.activity_car_selection_newcar_input_engine)
    protected AutoCompleteTextView engineText;
    @BindView(R.id.activity_car_selection_newcar_input_power)
    protected AutoCompleteTextView powerText;
    @BindView(R.id.activity_car_selection_layout_fullDetail)
    protected Button full;

    @Inject
    protected DAOProvider daoProvider;
    @Inject
    protected CarPreferenceHandler carManager;

    private CompositeDisposable disposables = new CompositeDisposable();
    private Scheduler.Worker mainThreadWorker = AndroidSchedulers.mainThread().createWorker();

    private Set<Car> mCars = new HashSet<>();
    private CarNew mCarsNew;
    private Set<String> mManufacturerNames = new HashSet<>();
    private Set<String> mModelSet = new LinkedHashSet<>();
    private Set<String> mYearSet = new HashSet<>();
    private List<String> mModelName = new ArrayList<>();
    private List<String> mYear = new ArrayList<>();
    private Map<String,List<CarNew>> mModelYearFuelToEngine = new HashMap<>();
    private Map<Integer,List<CarNew>> mModelYearFuelEngineToPower = new HashMap<>();
    private Map<Integer,List<CarNew>> mModelPowerToCar = new HashMap<>();
    private Map<String,List<CarNew>> mModelToYearToFuel = new HashMap<>();
    private Map<String,List<String>> mModelYear = new HashMap<>();
    private Map<String,String> hsn = new ConcurrentHashMap<>();
    private Map<String, Set<String>> mCarToModelMap = new ConcurrentHashMap<>();
    private Map<String, Set<String>> mModelToYear = new ConcurrentHashMap<>();
    private Map<Pair<String, String>, Set<String>> mModelToCCM = new ConcurrentHashMap<>();
    CarNew readyCar;
    String s="";
    String manufacturSelected1 = "";


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


        // initially we set the toolbar exp to gone
        toolbar.setVisibility(View.GONE);
        toolbarExp.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        downloadView.setVisibility(View.INVISIBLE);

        RxToolbar.itemClicks(toolbar)
                .filter(continueWhenFormIsCorrect())
                .map(createCarFromForm())
                .filter(continueWhenCarHasCorrectValues())
                .map(checkCarAlreadyExist())
                .subscribeWith(new DisposableObserver<Car>() {
                    @Override
                    public void onComplete() {
                        LOG.info("onCompleted car");
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOG.warn(e.getMessage(), e);
                    }

                    @Override
                    public void onNext(Car car) {
                        LOG.info("car added");
                        ((CarSelectionUiListener) getActivity()).onCarAdded(car);
                        hideKeyboard(getView());
                        closeThisFragment();
                    }
                });


        /*fueltypeText.setAdapter(new FuelTypeAdapter(
                getContext(),
                R.layout.activity_car_selection_newcar_fueltype_item,
                Car.FuelType.values()));*/


        fueltypeText.setKeyListener(null);

        manufacturerText.setOnItemClickListener((parent, view1, position, id) -> requestNextTextfieldFocus(manufacturerText,parent,position));
        modelText.setOnItemClickListener((parent, view12, position, id) -> requestNextTextfieldFocus(modelText,parent,position));
        yearText.setOnItemClickListener((parent, view13, position, id) -> requestNextTextfieldFocus(yearText,parent,position));
        fueltypeText.setOnItemClickListener((parent, view14, position, id) -> requestNextTextfieldFocus(fueltypeText,parent,position));
        engineText.setOnItemClickListener((parent,view15,position,id)-> requestNextTextfieldFocus(engineText,parent,position));
        powerText.setOnItemClickListener((parent,view16,position,id)->requestNextTextfieldFocus(powerText,parent,position));
       // dispatchRemoteSensors();
        showManufacturer();

        //initFocusChangedListener();
        initWatcher();
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"Successful"+s,Toast.LENGTH_SHORT).show();
                            }
                        });

                        mainThreadWorker.schedule(() -> {
                           dispose();
                            manufacturerText.setAdapter(asSortedAdapter(getContext(),mManufacturerNames));
                            downloadView.setVisibility(View.INVISIBLE);
                        });

                    }

                    @Override
                    public void onError(Throwable e) {
                       getActivity().runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                           }
                       });

                    }

                    @Override
                    public void onNext(List<Manufacturer> manufacturers) {
                        for (Manufacturer manufacturer : manufacturers) {
                            if (manufacturer != null) {
                                s += manufacturer.getHsn() + "\n";
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
        ECAnimationUtils.animateShowView(getContext(), toolbarExp,
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

    @OnTextChanged(value = R.id.activity_car_selection_newcar_input_manufacturer, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onManufacturerChanged(CharSequence text) {
        manufacturerText.setError(null);

        modelText.setText("");
        yearText.setText("");
        engineText.setText("");
    }

    @OnTextChanged(value = R.id.activity_car_selection_newcar_input_model, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onModelChanged(CharSequence text) {
        modelText.setError(null);

        yearText.setText("");
        engineText.setText("");
    }

    @OnTextChanged(value = R.id.activity_car_selection_newcar_input_constructionyear, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onConstructionYearChanged(CharSequence text) {
        yearText.setError(null);
        engineText.setText("");
    }

    @OnTextChanged(value = R.id.activity_car_selection_newcar_input_fueltype, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onFuelTypeChanged(CharSequence text) {
        if (text.toString().isEmpty())
            return;

        Car.FuelType fuelType = Car.FuelType.getFuelTybeByTranslatedString(getContext(), text.toString());

        if (Car.FuelType.ELECTRIC.equals(fuelType)) {
            engineLayout.setVisibility(View.GONE);
            engineText.setVisibility(View.GONE);
        } else {
            engineLayout.setVisibility(View.VISIBLE);
            engineText.setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.activity_car_selection_newcar_input_engine)
    protected void onEngineDisplacementChanged(CharSequence text) {
        engineText.setError(null);
    }

    @OnClick(R.id.activity_car_selection_layout_fullDetail)
    protected void seeCarFullDetails() {
        String s=readyCar.getAllotmentDate()+readyCar.getPower()+
                readyCar.getEngineCapacity()+"\naxles"+readyCar.getAxles()+
                "\nmass"+readyCar.getMaximumMass()+"\nseats"+readyCar.getSeats();
        Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
    }

    /**
     * Add car button onClick listener. When clicked, it tries to find out if the car already
     * exists. If this is the case, then it adds the car to the list of selected cars. If not,
     * then it selects
     */
    private Predicate<MenuItem> continueWhenFormIsCorrect() {
        return menuItem -> {
            // First, reset the form
            manufacturerText.setError(null);
            modelText.setError(null);
            yearText.setError(null);
            engineText.setError(null);

            Car.FuelType fuelType = Car.FuelType.getFuelTybeByTranslatedString(
                    getContext(), fueltypeText.getText().toString());

            //First check all input forms for empty strings
            View focusView = null;
            if (fuelType != Car.FuelType.ELECTRIC && engineText.getText().length() == 0) {
                engineText.setError(getString(R.string.car_selection_error_empty_input));
                focusView = engineText;
            }
            if (fueltypeText.getText().length() == 0) {
                fueltypeText.setError(getString(R.string.car_selection_error_empty_input));
                focusView = fueltypeText;
            }
            if (yearText.getText().length() == 0) {
                yearText.setError(getString(R.string.car_selection_error_empty_input));
                focusView = yearText;
            }
            if (modelText.getText().length() == 0) {
                modelText.setError(getString(R.string.car_selection_error_empty_input));
                focusView = modelText;
            }
            if (manufacturerText.getText().length() == 0) {
                manufacturerText.setError(getString(R.string.car_selection_error_empty_input));
                focusView = manufacturerText;
            }

            // if any of the input forms contained empty values, then set the focus to the
            // last one set.
            if (focusView != null) {
                LOG.info("Some input fields were empty");
                focusView.requestFocus();
                return false;
            } else {
                return true;
            }
        };
    }

    private <T> Function<T, Car> createCarFromForm() {
        return t -> {
            // Get the values
            String manufacturer = manufacturerText.getText().toString();
            String model = modelText.getText().toString();
            String yearString = yearText.getText().toString();
            String engineString = engineText.getText().toString();

            FuelTypeAdapter fueltypeAdapter = (FuelTypeAdapter) fueltypeText.getAdapter();
            Car.FuelType fueltype = Car.FuelType.getFuelTybeByTranslatedString(getContext(),
                    fueltypeText.getText().toString());

            // create the car
            int year = Integer.parseInt(yearString);
            if (fueltype != Car.FuelType.ELECTRIC) {
                try {
                    int engine = Integer.parseInt(engineString);
                    return new CarImpl(manufacturer, model, fueltype, year, engine);
                } catch (Exception e) {
                    LOG.error(String.format("Unable to parse engine [%s]", engineString), e);
                }
            }
            return new CarImpl(manufacturer, model, fueltype, year);
        };
    }

    private Predicate<Car> continueWhenCarHasCorrectValues() {
        return car -> {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            View focusView = null;

            // Check the values of engine and year for validity.
            if (car.getFuelType() != Car.FuelType.ELECTRIC &&
                    (car.getEngineDisplacement() < 500 || car.getEngineDisplacement() > 5000)) {
                engineText.setError(getString(R.string.car_selection_error_invalid_input));
                focusView = engineText;
            }
            if (car.getConstructionYear() < 1990 || car.getConstructionYear() > currentYear) {
                yearText.setError(getString(R.string.car_selection_error_invalid_input));
                focusView = yearText;
            }

            // if tengine or year have invalid values, then request the focus.
            if (focusView != null) {
                focusView.requestFocus();
                return false;
            }

            return true;
        };
    }

    private Function<Car, Car> checkCarAlreadyExist() {
        return car -> {
            String manu = car.getManufacturer();
            String model = car.getModel();
            String year = "" + car.getConstructionYear();
            String engine = "" + car.getEngineDisplacement();
            Pair<String, String> modelYear = new Pair<>(model, year);

            Car selectedCar = null;
            if (mManufacturerNames.contains(manu)
                    && mCarToModelMap.get(manu) != null
                    && mCarToModelMap.get(manu).contains(model)
                    && mModelToYear.get(model) != null
                    && mModelToYear.get(model).contains(year)
                    && mModelToCCM.get(modelYear) != null
                    && mModelToCCM.get(modelYear).contains(engine)) {
                for (Car other : mCars) {
                    if (other.getManufacturer().equals(manu)
                            && other.getModel().equals(model)
                            && other.getConstructionYear() == car.getConstructionYear()
                            && other.getEngineDisplacement() == car.getEngineDisplacement()
                            && other.getFuelType() == car.getFuelType()) {
                        selectedCar = other;
                        break;
                    }
                }
            }

            if (selectedCar == null) {
                LOG.info("New Car type. Register car at server.");
                carManager.registerCarAtServer(car);
                return car;
            } else {
                LOG.info(String.format("Car already existed -> [%s]", selectedCar.getId()));
                return selectedCar;
            }
        };
    }

    private void getAllCarNew(String manufid,String carid) {
        disposables.add(daoProvider.getSensorNewDAO()
                .getAllCarNewObservable(manufid,carid)
                .toFlowable(BackpressureStrategy.BUFFER)
                .onBackpressureBuffer(10000)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toObservable()
                .subscribeWith(new DisposableObserver<CarNew>() {

                    @Override
                    protected void onStart() {
                        LOG.info("onStart() download Manufacturer Car");
                        downloadView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        mainThreadWorker.schedule(() -> {
                            Toast.makeText(getContext(),""+mCarsNew.getAllotmentDate(),Toast.LENGTH_SHORT).show();
                            dispose();
                            downloadView.setVisibility(View.INVISIBLE);
                        });

                    }

                    @Override
                    public void onNext(CarNew carNews) {
                       mCarsNew = carNews;
                       mYear.add(carNews.getAllotmentDate());
                        if (!mModelToYearToFuel.containsKey(carNews.getAllotmentDate()))
                            mModelToYearToFuel.put(carNews.getAllotmentDate(), new ArrayList<>());
                        mModelToYearToFuel.get(carNews.getAllotmentDate()).add(carNews);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }));
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
                        LOG.info("onStart() download Manufacturer Car");
                        downloadView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        mainThreadWorker.schedule(() -> {
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,mModelName);
                            modelText.setAdapter(arrayAdapter);
                            dispose();
                            downloadView.setVisibility(View.INVISIBLE);
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }));

    }
    private void dispatchRemoteSensors() {
        disposables.add(daoProvider.getSensorDAO()
                .getAllCarsObservable()
                .toFlowable(BackpressureStrategy.BUFFER)
                .onBackpressureBuffer(10000)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toObservable()
                .subscribeWith(new DisposableObserver<List<Car>>() {

                    @Override
                    protected void onStart() {
                        LOG.info("onStart() download sensors");
                        downloadView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        LOG.info("onCompleted(): cars successfully downloaded.");

                        mainThreadWorker.schedule(() -> {
                            // Update the manufactuerers in
//                            updateSpinner(mManufacturerNames, manufacturerSpinner);
                            updateManufacturerViews();

                            dispose();

                            downloadView.setVisibility(View.INVISIBLE);
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOG.error(e.getMessage(), e);
                        mainThreadWorker.schedule(() -> {
                            downloadView.setVisibility(View.INVISIBLE);
                        });
                    }

                    @Override
                    public void onNext(List<Car> cars) {
                        for (Car car : cars) {
                            if (car != null)
                                addCarToAutocompleteList(car);
                        }
                    }
                }));
    }

    private void initFocusChangedListener() {
        manufacturerText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String manufacturer = manufacturerText.getText().toString();
                updateModelViews(manufacturer);
            }
        });

        modelText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String model = modelText.getText().toString();
                updateYearView(model);
            }
        });

        yearText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String year = yearText.getText().toString();
                String model = modelText.getText().toString();
                Pair<String, String> modelYear = new Pair<>(model, year);

                updateEngineView(modelYear);
            }
        });

        engineText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkFuelingType();
            }
        });
    }

    private void checkFuelingType() {
        String manufacturer = manufacturerText.getText().toString();
        String model = modelText.getText().toString();
        String yearString = yearText.getText().toString();
        String engineString = engineText.getText().toString();
        Pair<String, String> modelYear = new Pair<>(model, yearString);

        Car selectedCar = null;
        if (mManufacturerNames.contains(manufacturer)
                && mCarToModelMap.get(manufacturer) != null
                && mCarToModelMap.get(manufacturer).contains(model)
                && mModelToYear.get(model) != null
                && mModelToYear.get(model).contains(yearString)
                && mModelToCCM.get(modelYear) != null
                && mModelToCCM.get(modelYear).contains(engineString)) {
            for (Car other : mCars) {
                if (other.getManufacturer() == null ||
                        other.getModel() == null ||
                        other.getConstructionYear() == 0 ||
                        other.getEngineDisplacement() == 0 ||
                        other.getFuelType() == null) {
                    continue;
                }
                if (other.getManufacturer().equals(manufacturer)
                        && other.getModel().equals(model)
                        && other.getConstructionYear() == Integer.parseInt(yearString)
                        && other.getEngineDisplacement() == Integer.parseInt(engineString)) {
                    selectedCar = other;
                    break;
                }
            }
        }

//        if (selectedCar != null && selectedCar.getFuelType() != null) {
//            fueltypeText.setText(selectedCar.getFuelType().toString());
//        }
    }

    private void updateManufacturerViews() {
        if (!mManufacturerNames.isEmpty()) {
            manufacturerText.setAdapter(asSortedAdapter(getContext(), mManufacturerNames));
        } else {
            manufacturerText.setAdapter(null);
        }
    }

    private void updateModelViews(String manufacturer) {
        if (mCarToModelMap.containsKey(manufacturer)) {
            modelText.setAdapter(asSortedAdapter(getContext(), mCarToModelMap.get(manufacturer)));
        } else {
            modelText.setAdapter(null);
        }
    }

    private void updateYearView(String model) {
        if (mModelToYear.containsKey(model)) {
            yearText.setAdapter(asSortedAdapter(getContext(), mModelToYear.get(model)));
        } else {
            yearText.setAdapter(null);
        }
    }

    private void updateEngineView(Pair<String, String> model) {
        if (mModelToCCM.containsKey(model)) {
            engineText.setAdapter(asSortedAdapter(getContext(), mModelToCCM.get(model)));
        } else {
            engineText.setAdapter(null);
        }
    }

    /**
     * Inserts the attributes of the car
     *
     * @param car
     */
    private void addCarToAutocompleteList(Car car) {

        mCars.add(car);
        String manufacturer = car.getManufacturer().trim();
        String model = car.getModel().trim();
        String year = Integer.toString(car.getConstructionYear());

        if (manufacturer.isEmpty() || model.isEmpty() || year.isEmpty())
            return;

        mManufacturerNames.add(manufacturer);

        if (!mCarToModelMap.containsKey(manufacturer))
            mCarToModelMap.put(manufacturer, new HashSet<>());
        mCarToModelMap.get(manufacturer).add(model);

        if (!mModelToYear.containsKey(model))
            mModelToYear.put(model, new HashSet<>());
        mModelToYear.get(model).add(Integer.toString(car.getConstructionYear()));

        Pair<String, String> modelYearPair = new Pair<>(model, year);
        if (!mModelToCCM.containsKey(modelYearPair))
            mModelToCCM.put(modelYearPair, new HashSet<>());
        mModelToCCM.get(modelYearPair).add(Integer.toString(car.getEngineDisplacement()));
    }

    public void closeThisFragment() {
        // ^^
        ECAnimationUtils.animateHideView(getContext(),
                ((CarSelectionActivity) getActivity()).overlayView, R.anim.fade_out);
        ECAnimationUtils.animateHideView(getContext(), R.anim
                .translate_slide_out_top_fragment, toolbar, toolbarExp);
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

    private void initWatcher() {
        disposables.add(RxTextView.afterTextChangeEvents(modelText)
                .debounce(ERROR_DEBOUNCE_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(t -> t.toString())
                .subscribe(model -> {
                    if (model.trim().isEmpty()) {
                        modelText.setError(getString(R.string.car_selection_error_empty_input));
                    }
                }, LOG::error));

        // Year input validity check.
        disposables.add(RxTextView.textChanges(yearText)
                .skipInitialValue()
                .debounce(ERROR_DEBOUNCE_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString)
                .filter(s -> !s.isEmpty())
                .subscribe(yearString -> {
                    try {
                        int year = Integer.parseInt(yearString);
                        if (year < CONSTRUCTION_YEAR_MIN || year > CONSTRUCTION_YEAR_MAX) {
                            yearText.setError(getString(R.string.car_selection_error_invalid_input));
                            yearText.requestFocus();
                        }
                    } catch (Exception e) {
                        LOG.error(String.format("Unable to parse year [%s]", yearString), e);
                        yearText.setError(getString(R.string.car_selection_error_invalid_input));
                        yearText.requestFocus();
                    }
                }, LOG::error));

        // Engine input validity check.
        disposables.add(RxTextView.textChanges(engineText)
                .skipInitialValue()
                .debounce(ERROR_DEBOUNCE_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString)
                .filter(s -> !s.isEmpty())
                .subscribe(engineString -> {
                    if (engineString.isEmpty())
                        return;

                    try {
                        int engine = Integer.parseInt(engineString);
                        if (engine < ENGINE_DISPLACEMENT_MIN || engine > ENGINE_DISPLACEMENT_MAX) {
                            engineText.setError(getString(R.string.car_selection_error_invalid_input));
                            engineText.requestFocus();
                        }
                    } catch (Exception e) {
                        LOG.error(String.format("Unable to parse engine [%s]", engineString), e);
                        engineText.setError(getString(R.string.car_selection_error_invalid_input));
                        engineText.requestFocus();
                    }
                }, LOG::error));
    }

    private void requestNextTextfieldFocus(TextView textField,AdapterView<?> parent,int position) {
        if(textField == manufacturerText) {
            manufacturSelected1 = parent.getItemAtPosition(position).toString();
            Toast.makeText(getContext(),""+hsn.get(manufacturSelected1),Toast.LENGTH_SHORT).show();
            manufacturerCar(hsn.get(manufacturSelected1));
            full.setVisibility(View.GONE);
        }
        else if(textField == modelText) {
            String temp = "";
            String manufactureCarSelected = parent.getItemAtPosition(position).toString();
            List<String> ss = mModelYear.get(manufactureCarSelected);
            mYear.clear();
            //mYearSet.clear();
            mModelToYearToFuel.clear();
            for(String it : ss) {
                getAllCarNew(hsn.get(manufacturSelected1),it);
            }
            //mYear.addAll(mYearSet);
            full.setVisibility(View.GONE);
            yearText.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,mYear));
            Toast.makeText(getContext(),""+temp,Toast.LENGTH_SHORT).show();
        }
        else if(textField == yearText) {
            String yearSelected = parent.getItemAtPosition(position).toString();
            List<CarNew> carNews = mModelToYearToFuel.get(yearSelected);
            mModelYearFuelToEngine.clear();
            Set<String> fuelSet = new LinkedHashSet<>();
            List<String> fuel = new ArrayList<>();
            for(CarNew carNew : carNews) {
                List<Link> allLinks = carNew.getLinks();
                fuelSet.add(allLinks.get(1).getTitle());
                if (!mModelYearFuelToEngine.containsKey(allLinks.get(1).getTitle()))
                    mModelYearFuelToEngine.put(allLinks.get(1).getTitle(), new ArrayList<>());
                mModelYearFuelToEngine.get(allLinks.get(1).getTitle()).add(carNew);
            }
            fuel.addAll(fuelSet);
            full.setVisibility(View.GONE);
            fueltypeText.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,fuel));
        }
        else if(textField == fueltypeText) {
            String fuelSelected = parent.getItemAtPosition(position).toString();
            List<CarNew> carNews = mModelYearFuelToEngine.get(fuelSelected);
            mModelYearFuelEngineToPower.clear();
            Set<Integer> engineSet = new LinkedHashSet<>();
            List<Integer> engine = new ArrayList<>();
            for(CarNew carNew : carNews) {
                engineSet.add(carNew.getEngineCapacity());
                if (!mModelYearFuelEngineToPower.containsKey(carNew.getEngineCapacity()))
                    mModelYearFuelEngineToPower.put(carNew.getEngineCapacity(), new ArrayList<>());
                mModelYearFuelEngineToPower.get(carNew.getEngineCapacity()).add(carNew);
            }
            full.setVisibility(View.GONE);
            engine.addAll(engineSet);
            engineText.setAdapter(new ArrayAdapter<Integer>(getContext(),android.R.layout.simple_dropdown_item_1line,engine));
        }
        else if(textField == engineText) {
            Integer engineSelected = Integer.parseInt(parent.getItemAtPosition(position).toString());
            List<CarNew> carNews = mModelYearFuelEngineToPower.get(engineSelected);
            List<Integer> power = new ArrayList<>();
            mModelPowerToCar.clear();
            for(CarNew carNew : carNews) {
                power.add(carNew.getPower());
                if (!mModelPowerToCar.containsKey(carNew.getPower()))
                    mModelPowerToCar.put(carNew.getPower(), new ArrayList<>());
                mModelPowerToCar.get(carNew.getPower()).add(carNew);
            }
            full.setVisibility(View.GONE);
            powerText.setAdapter(new ArrayAdapter<Integer>(getContext(),android.R.layout.simple_dropdown_item_1line,power));
        }
        else if(textField == powerText) {
            Integer powerSelected = Integer.parseInt(parent.getItemAtPosition(position).toString());
            List<CarNew> carNews = mModelPowerToCar.get(powerSelected);
            for(CarNew carNew :carNews) {
                readyCar = carNew;
            }
        full.setVisibility(View.VISIBLE);
        }
        try {
            TextView nextField = (TextView) textField.focusSearch(View.FOCUS_DOWN);
            nextField.requestFocus();
        } catch (Exception e) {
            LOG.warn("Unable to find next field or to request focus to next field.");
        }
    }

    private static final ArrayAdapter<String> asSortedAdapter(Context context, Set<String> set) {
        String[] strings = set.toArray(new String[set.size()]);
        Arrays.sort(strings);
        return new ArrayAdapter<>(
                context,
                R.layout.activity_car_selection_newcar_fueltype_item,
                strings);
    }

    /**
     * Custom array adapter for translated fueltypes
     */
    private static class FuelTypeAdapter extends ArrayAdapter<String> {
        private final Car.FuelType[] values;

        public FuelTypeAdapter(@NonNull Context context, int resource, Car.FuelType[] values) {
            super(context, resource, new AbstractList<String>() {
                @Override
                public int size() {
                    return values.length;
                }

                @Override
                public String get(int index) {
                    if (index == ArrayAdapter.NO_SELECTION)
                        return null;
                    return context.getString(values[index].getStringResource());
                }
            });
            this.values = values;
        }

        public Car.FuelType getOriginal(int index) {
            if (index == ArrayAdapter.NO_SELECTION)
                return null;
            return values[index];
        }
    }
}
