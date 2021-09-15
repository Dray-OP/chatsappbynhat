package com.example.chatsappbynhat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsappbynhat.databinding.RowConversationBinding;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;
    public   UserAdapter (Context context,ArrayList<User> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nhat: LayoutInflater là 1 component giúp bạn chuyển layout file(Xml) thành View
        // Nhat: phương thức inflate để chuyển đổi 1 xml layout file thành 1 View trong java
        // Nhat: https://viblo.asia/p/tim-hieu-ve-layoutinflater-trong-android-07LKXzL2lV4
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);
        return new  UsersViewHolder(view);
    }
//xử lý việc gọi ra view
    @Override
    public void onBindViewHolder(@NonNull  UserAdapter.UsersViewHolder holder, int position) {
        User user = users.get(position);
        // Nhat: lấy thằng tên
        holder.binding.username.setText(user.getName());
        // Nhat: lấy ảnh bằng thư viện Glide
        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    //Paging
    public class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull  View itemView) {
            super(itemView);
            // Nhat: gán
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
