package org.envirocar.app.views.carselection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.envirocar.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    Context mcontext;
    String items[];
    public CustomAdapter(Context mcontext,String items[]) {
        this.mcontext = mcontext;
        this.items = items;
    }
    @NonNull
    @Override
    public CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.top_recy,null);
        CustomViewHolder customViewHolder = new CustomViewHolder(view);
        return customViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.CustomViewHolder holder, int position) {
        holder.textView.setText(items[position]);
        holder.floatingActionButton.setOnClickListener(view ->{ holder.fabProgressCircle.setVisibility(View.VISIBLE);
            holder.fabProgressCircle.show();
            Toast.makeText(mcontext,""+items[position],Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.brand)
        FloatingActionButton floatingActionButton;
        @BindView(R.id.laytext)
        TextView textView;
        @BindView(R.id.prog)
        FABProgressCircle fabProgressCircle;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
