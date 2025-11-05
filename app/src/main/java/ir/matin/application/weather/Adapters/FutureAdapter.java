package ir.matin.application.weather.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ir.matin.application.weather.Domains.Future;
import ir.matin.application.weather.R;
public class FutureAdapter extends RecyclerView.Adapter<FutureAdapter.viewHolder> {
    ArrayList<Future> items ;
    Context context ;

    public FutureAdapter(ArrayList<Future> items, Context context) {
        this.items = items;
        this.context = context;
    }


    @NonNull
    @Override
    public FutureAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.future_item_view,parent,false);
        return new viewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FutureAdapter.viewHolder holder, int position) {
        Future future = items.get(position);
        holder.day.setText(future.getDay());
        holder.status.setText(future.getStatus());
        holder.low.setText(future.getLowTemp()+"°");
        holder.high.setText(future.getHighTemp()+"°");

        if (future.getPicPath().startsWith("https")){
            Glide.with(context).
                    load(future.getPicPath())
                    .into(holder.pic);
        }
        else {
            int drawableResId = holder.itemView.getResources().getIdentifier(future.getPicPath(), "drawable", holder.itemView.getContext().getPackageName());

            Glide.with(context)
                    .load(drawableResId)
                    .into(holder.pic);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    @SuppressLint("NotifyDataSetChanged")


    public class viewHolder extends RecyclerView.ViewHolder {
        TextView high,low,status,day ;
        ImageView pic ;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            high = itemView.findViewById(R.id.highTxt);
            low = itemView.findViewById(R.id.lowTxt);
            status = itemView.findViewById(R.id.statusText);
            day = itemView.findViewById(R.id.dayTxt);
            pic = itemView.findViewById(R.id.futurePic);
        }
    }
}
