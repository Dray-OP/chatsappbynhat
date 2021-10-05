package com.example.chatsappbynhat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsappbynhat.Models.Message;
import com.example.chatsappbynhat.Models.User;
import com.example.chatsappbynhat.R;
import com.example.chatsappbynhat.databinding.ItemReceiveBinding;
import com.example.chatsappbynhat.databinding.ItemReceiveGroupBinding;
import com.example.chatsappbynhat.databinding.ItemSentBinding;
import com.example.chatsappbynhat.databinding.ItemSentGroupBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

//RecyclerView.Adapter hẫu trợ load trang
public class GroupMessagesAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT =1;
    final int ITEM_RECEIVE = 2;


    public GroupMessagesAdapter(Context content, ArrayList<Message> messages){
        this.context = content;
        this.messages = messages;
    }
    // Nhat: 2
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent_group,parent,false);
            return new SentViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive_group,parent,false);
            return new SentViewHolder(view);
        }
    }
    // Nhat: ???
    // Nhat: return the view type of the item at position for the purposes of view recycling
    // Nhat: 1
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        // Nhat:
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId()))
        {
            return ITEM_SENT;
        }else {
            return ITEM_RECEIVE;
        }
//        return super.getItemViewType(position);
    }
    // Nhat: 3 db trả về load trang
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        // Nhat: cấu hình cho icon
        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (holder.getClass() == SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder)holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            // Nhat: set cái biểu tượng cảm xúc
            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("public")
                    .child(message.getMessageId()).setValue(message);

            return true;
        });

        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;
            // Nhat: check hình ảnh
            if(message.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

            // Nhat: get name user
            FirebaseDatabase.getInstance()
                    .getReference().child("users")
                    .child(message.getSenderId())
                   .addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                           if (snapshot.exists()){
                               User user = snapshot.getValue(User.class);
                               viewHolder.binding.name.setText("@" +user.getName());
                           }
                       }

                       @Override
                       public void onCancelled(@NonNull @NotNull DatabaseError error) {

                       }
                   });

            viewHolder.binding.message.setText(message.getMessage());

            if (message.getFeeling() >= 0){
//                message.setFeeling(reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else {
                viewHolder.binding.feeling.setVisibility(View.GONE);

            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
//            if (message.getMessage().equals("photo")){
//                // Nhat: hiển thị ảnh
//                viewHolder.binding.image.setVisibility(View.VISIBLE);
//                viewHolder.binding.message.setVisibility(View.GONE);
//                Glide.with(context).load(message.getImageUrl()).into(viewHolder.binding.image);
//            }
            if(message.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }
            // Nhat: get name user
            FirebaseDatabase.getInstance()
                    .getReference().child("users")
                    .child(message.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText("@" +user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
            viewHolder.binding.message.setText(message.getMessage());

            if (message.getFeeling() >= 0){
//                message.setFeeling(reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else {
                viewHolder.binding.feeling.setVisibility(View.GONE);

            }
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Nhat: gửi
    public  class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSentGroupBinding binding;
        public SentViewHolder(@NonNull  View itemView) {
            super(itemView);
            binding = ItemSentGroupBinding.bind(itemView);
        }
    }
    // Nhat: nhận
    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveGroupBinding binding;
        public ReceiverViewHolder(@NonNull  View itemView) {
            super(itemView);
            binding = ItemReceiveGroupBinding.bind(itemView);
        }
    }

}
