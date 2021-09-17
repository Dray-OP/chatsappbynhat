package com.example.chatsappbynhat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsappbynhat.Activities.ChatActivity;
import com.example.chatsappbynhat.R;
import com.example.chatsappbynhat.Models.User;
import com.example.chatsappbynhat.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.lastMsg.setText(lastMsg);
                            holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                        }else {
                            holder.binding.lastMsg.setText("Tap to chat");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        // Nhat: lấy thằng tên
        holder.binding.username.setText(user.getName());
        // Nhat: lấy ảnh bằng thư viện Glide
        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);
        // Nhat: lấy data text
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name",user.getName());
                intent.putExtra("uid",user.getUid());
                context.startActivity(intent);
            }
        });

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
