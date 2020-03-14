package org.envirocar.app.views.carselection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.envirocar.app.R;
import org.envirocar.core.entity.CarNew;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReadyCar extends Fragment {
   @BindView(R.id.namedata)
    TextView name;
   @BindView(R.id.allotmentdata)
           TextView allotment;
   @BindView(R.id.powerdata)
           TextView power;
   @BindView(R.id.enginedata)
           TextView engine;
   @BindView(R.id.axlesdata)
           TextView axles;
   @BindView(R.id.massdata)
           TextView mass;
   @BindView(R.id.seatsdata)
           TextView seats;

    CarNew readyCar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ready_car, container, false);
        ButterKnife.bind(this,view);
        if (getArguments() != null) {
            ArrayList<CarNew> carNews = new ArrayList<>();
            carNews = (ArrayList<CarNew>) getArguments().getSerializable("readyCar");
            for(CarNew carNew :carNews) {
                readyCar = carNew;
            }
        }
        String s="Name: \t\t\t"+readyCar.getCommercialName()+"\n\nAllotment date: \t\t\t"+readyCar.getAllotmentDate()
                +"\n\nPower:\t\t\t "+readyCar.getPower()+
                "\n\nEngine: \t\t\t"+readyCar.getEngineCapacity()+"\n\nAxles:\t\t\t "+readyCar.getAxles()+
                "\n\nMass:\t\t\t "+readyCar.getMaximumMass()+"\n\nSeats:\t\t\t "+readyCar.getSeats();
        name.setText(readyCar.getCommercialName());
        allotment.setText(readyCar.getAllotmentDate());
        power.setText(readyCar.getPower().toString());
        engine.setText(readyCar.getEngineCapacity().toString());
        axles.setText(readyCar.getAxles().toString());
        mass.setText(readyCar.getMaximumMass().toString());
        seats.setText(readyCar.getSeats().toString());

        return view;
    }
}
