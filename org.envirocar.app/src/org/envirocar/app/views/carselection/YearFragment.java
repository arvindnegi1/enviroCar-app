package org.envirocar.app.views.carselection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import org.envirocar.app.BaseApplicationComponent;
import org.envirocar.app.R;
import org.envirocar.app.handler.DAOProvider;
import org.envirocar.app.injection.BaseInjectorFragment;
import org.envirocar.core.entity.CarNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class YearFragment extends BaseInjectorFragment {
    @BindView(R.id.year_fragment_download)
    View downloadView;
    @BindView(R.id.year_fragment_data)
    View showData;
    @BindView(R.id.autoComplete)
    AutoCompleteTextView autoCompleteTextView;
    @BindView(R.id.year_fragment_list)
    ListView listView;

    @Inject
    protected DAOProvider daoProvider;

    private Scheduler.Worker mainThreadWorker = AndroidSchedulers.mainThread().createWorker();
    private CompositeDisposable disposables = new CompositeDisposable();
    private CarNew mCarsNew;
    private List<String> mYear = new ArrayList<>();
    private Map<String,List<CarNew>> mModelToYearToFuel = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_year, container, false);
         ButterKnife.bind(this,view);
        if (getArguments() != null) {
            ArrayList<String> selected = new ArrayList<>();
            selected = getArguments().getStringArrayList("manufacturerCarYear");
            //String temp="";
            String manufid = getArguments().getString("manufid");
            for(String it : selected) {
              // temp+=it;
                getAllCarNew(manufid,it);
            }
            //Toast.makeText(getContext(),""+temp+manufid,Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    protected void injectDependencies(BaseApplicationComponent baseApplicationComponent) {
        baseApplicationComponent.inject(this);
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
                        downloadView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        mainThreadWorker.schedule(() -> {
                            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,mYear);
                            listView.setAdapter(arrayAdapter);
                            autoCompleteTextView.setAdapter(arrayAdapter);
                            showData.setVisibility(View.VISIBLE);
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
                    mainThreadWorker.schedule(()->{
                        Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();});
                    }
                }));
    }
}
