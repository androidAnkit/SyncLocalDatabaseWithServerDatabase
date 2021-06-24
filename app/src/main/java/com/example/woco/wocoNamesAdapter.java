package com.example.woco;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.woco.data.Contract;

import java.util.ArrayList;

public class wocoNamesAdapter extends RecyclerView.Adapter<wocoNamesAdapter.NamesViewHolder> {

    ArrayList<wocoNames> name_list = new ArrayList<>();
    public nameClicked clickedName;
    private LayoutInflater inflater;

    private int selected_position = -1;

    public wocoNamesAdapter(Context context, ArrayList<wocoNames> arrayList,nameClicked clickedName){
        inflater = LayoutInflater.from(context);
        this.name_list=arrayList;
        this.clickedName=clickedName;
    }

    @NonNull
    @Override
    public NamesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.from(parent.getContext()).inflate(R.layout.names_list,parent,false);
        return new NamesViewHolder(view,clickedName);
    }

    @Override
    public void onBindViewHolder(@NonNull NamesViewHolder holder, int position) {
        holder.name.setText(name_list.get(position).getName());
        int syncStatus = name_list.get(position).getSyncStatus();
        if(syncStatus == Contract.SYNC_STATUS_SUCCESS)
            holder.syncStatus.setImageResource(R.drawable.check);
        else
            holder.syncStatus.setImageResource(R.drawable.sync);

        if(selected_position==position){
            holder.row_linear.setBackgroundResource(R.drawable.bac_selected_name);
        }else if(syncStatus!=Contract.SYNC_STATUS_SUCCESS){
            holder.row_linear.setBackgroundResource(R.drawable.bac_sync);
        }
        else{
            holder.row_linear.setBackgroundResource(R.drawable.bac_name_list);
        }



    }

    @Override
    public int getItemCount() {
        return name_list.size();
    }

    public class NamesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView syncStatus;
        TextView name;
        LinearLayout row_linear;
        wocoNamesAdapter.nameClicked clickListener;
        int position;

        public NamesViewHolder(@NonNull View itemView,nameClicked listner) {
            super(itemView);

            syncStatus=itemView.findViewById(R.id.syncCheck);
            name=itemView.findViewById(R.id.text_name);
            row_linear = itemView.findViewById(R.id.row_linear);
            clickListener=listner;
            itemView.setOnClickListener(this);

        }
        @Override
        public void onClick(View view) {
            clickListener.name_onClick(getLayoutPosition());
            position=getLayoutPosition();
            selected_position =position;
            notifyDataSetChanged();
        }
    }

    public interface nameClicked{
        void name_onClick(int position);
    }

}
