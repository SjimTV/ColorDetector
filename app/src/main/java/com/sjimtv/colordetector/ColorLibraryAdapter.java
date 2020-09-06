package com.sjimtv.colordetector;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ColorLibraryAdapter extends RecyclerView.Adapter<ColorLibraryAdapter.MyViewHolder>{

    private ArrayList<JsonColor> colors;
    private Activity activty;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ImageView colorView;
        TextView colorNameView;
        ColorLibraryActivity activity;

        public MyViewHolder(@NonNull View itemView, Activity activity) {
            super(itemView);
            this.colorView = itemView.findViewById(R.id.colorView);
            this.colorNameView = itemView.findViewById(R.id.colorNameView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.activity = (ColorLibraryActivity) activity;

        }


        @Override
        public void onClick(View v) {
            activity.getItemOnClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            activity.getItemOnLongClick(getAdapterPosition());
            return true;
        }
    }

    public ColorLibraryAdapter(Activity activity, ArrayList<JsonColor> colors){
        this.activty = activity;
        this.colors = colors;
    }

    @NonNull
    @Override
    public ColorLibraryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_color_library_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view, activty);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ColorLibraryAdapter.MyViewHolder holder, int position) {
        ImageView colorView = holder.colorView;
        TextView colorNameView = holder.colorNameView;

        colorView.setBackgroundColor(colors.get(position).getColor());
        colorNameView.setText(colors.get(position).getName());
    }



    @Override
    public int getItemCount() {
        return colors.size();
    }
}

