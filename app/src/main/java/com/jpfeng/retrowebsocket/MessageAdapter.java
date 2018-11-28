package com.jpfeng.retrowebsocket;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.jpfeng.retrowebsocket.databinding.ItemMessageBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Jpfeng
 * E-mail: fengjup@live.com
 * Date: 2018/11/21
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private List<Record> mData;

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_message, parent, false);
        return new MessageHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        Record message = mData.get(position);
        if (null != message) {
            holder.binding.setItemTime(message.getTimeString());
            holder.binding.setItemContent(message.getMessage());
            holder.binding.executePendingBindings();
        }
    }

    @Override
    public int getItemCount() {
        return null == mData ? 0 : mData.size();
    }

    public void setData(List<Record> data) {
        if (null == mData) {
            mData = data;
            notifyItemRangeInserted(0, data.size());

        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mData.size();
                }

                @Override
                public int getNewListSize() {
                    return data.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    Record oldMessage = mData.get(oldItemPosition);
                    Record newMessage = data.get(newItemPosition);
                    return oldMessage.getId() == newMessage.getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Record oldMessage = mData.get(oldItemPosition);
                    Record newMessage = data.get(newItemPosition);
                    return TextUtils.equals(oldMessage.getRawMessage(), newMessage.getRawMessage())
                            && oldMessage.getTimeMillis() == newMessage.getTimeMillis()
                            && oldMessage.getType() == newMessage.getType();
                }
            });

            mData = data;
            result.dispatchUpdatesTo(this);
        }
    }

    static class MessageHolder extends RecyclerView.ViewHolder {

        private final ItemMessageBinding binding;

        MessageHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
