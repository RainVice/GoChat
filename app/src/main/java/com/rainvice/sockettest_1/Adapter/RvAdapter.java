package com.rainvice.sockettest_1.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rainvice.sockettest_1.utils.DataUtil;

import java.util.List;

public class RvAdapter<T> extends RecyclerView.Adapter<RvAdapter.InnerHolder> {



    private List<T> mList;
    private final int mLayoutId;
    private final Callback<T> mCallback;

    public RvAdapter(List<T> list, int layoutId, Callback<T> callback) {
        mList = list;
        mLayoutId = layoutId;
        mCallback = callback;
    }


    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InnerHolder(LayoutInflater.from(parent.getContext()).inflate(mLayoutId,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
        mCallback.callback(holder.itemView,position,mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class InnerHolder extends RecyclerView.ViewHolder{
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 给每个ItemView指定不同的类型，这样在RecyclerView看来，这些ItemView全是不同的，不能复用
        return position;
    }

    public void notifyData(List<T> list){
        this.mList = list;
        notifyDataSetChanged();
    }

    public interface Callback<T>{
        void callback(View itemView,int position,T t);
    }

}
