package ir.matin.application.weather.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ir.matin.application.weather.Domains.Hourly;
import ir.matin.application.weather.R;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.viewHolder> {
    ArrayList<Hourly> items ;
    Context context ;
    @NonNull
    @Override
    public HourlyAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);
        this.context = parent.getContext();
        return new viewHolder(view);
    }

    public HourlyAdapter(ArrayList<Hourly> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HourlyAdapter.viewHolder holder, int position) {
        Hourly hourly = items.get(position);
        holder.tempText.setText(hourly.getTemperature()+"°");
        holder.hourText.setText(hourly.getHour());

        if (! hourly.getPicPath().startsWith("https")) {

            int drawableResId = holder.itemView.getResources().getIdentifier(hourly.getPicPath(), "drawable", holder.itemView.getContext().getPackageName());

            Glide.with(context)
                    .load(drawableResId)
                    .into(holder.picStatus);
        }
        else {
            Glide.with(holder.itemView.getContext())
                    .load(hourly.getPicPath())
                    .into(holder.picStatus);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView hourText , tempText;
        ImageView picStatus ;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            hourText = itemView.findViewById(R.id.hourTxt);
            tempText = itemView.findViewById(R.id.tempText);
            picStatus=itemView.findViewById(R.id.picStatusHourly);

        }
    }
}
