package org.envirocar.app.views.carselection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.envirocar.app.R;

public class CustomGridAdapter extends BaseAdapter {

    Context mcontext;
    String brandText[];
    int Res[];
    public CustomGridAdapter(Context mcontext,String brandText[],int Res[]) {
        this.mcontext = mcontext;
        this.brandText = brandText;
        this.Res = Res;
    }
    @Override
    public int getCount() {
        return brandText.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View view1 = LayoutInflater.from(mcontext).inflate(R.layout.custom_grid_layout,null);
        ImageView imageView = view1.findViewById(R.id.custom_grid_image);
        TextView textView = view1.findViewById(R.id.custom_grid_text);
        imageView.setImageResource(Res[i]);
        textView.setText(brandText[i]);
        return view1;
    }
}
