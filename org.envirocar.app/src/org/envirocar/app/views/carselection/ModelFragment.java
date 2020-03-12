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

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ModelFragment extends Fragment {
 @BindView(R.id.show)
    TextView textView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_model, container, false);
        ButterKnife.bind(this,view);
        if (getArguments() != null) {
            String selected = getArguments().getString("selected");
            textView.setText(selected);
        }
        return view;
    }
}
