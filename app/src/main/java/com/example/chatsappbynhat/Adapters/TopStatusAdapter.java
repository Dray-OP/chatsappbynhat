package com.example.chatsappbynhat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsappbynhat.Activities.MainActivity;
import com.example.chatsappbynhat.Models.Status;
import com.example.chatsappbynhat.Models.User;
import com.example.chatsappbynhat.Models.UserStatus;
import com.example.chatsappbynhat.R;
import com.example.chatsappbynhat.databinding.ItemStatusBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

// Nhat: adapter == model
public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder> {

    Context context;
    ArrayList<UserStatus> userStatuses;

    public TopStatusAdapter(Context context,ArrayList<UserStatus> userStatuses){
        this.context = context;
        this.userStatuses = userStatuses;
    }
    // Nhat: view
    @NotNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status,parent,false);
        return new TopStatusViewHolder(view);
    }
    //liên kết adapter tại đây
    @Override
    public void onBindViewHolder(@NonNull @NotNull TopStatusViewHolder holder, int position) {

        UserStatus userStatus = userStatuses.get(position);
        // Nhat: lấy thằng cuối cùng
        Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
        // Nhat: bidding ra image
        Glide.with(context).load(lastStatus.getImageUrl()).into(holder.binding.image);
        // Nhat: gán số vòng tròn
        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatuses().size());
        holder.binding.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nhat: in storyview
                ArrayList<MyStory> myStories = new ArrayList<>();
                for (Status status : userStatus.getStatuses()){
                    myStories.add(new MyStory(status.getImageUrl()));
                }
                // Nhat: trong ex của git
                // Nhat:
                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(userStatus.getName()) // Default is Hidden
                        .setSubtitleText("") // Default is Hidden
                        .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }

                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before calling show method
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    //ViewHolder == partialView và được quản lý bởi Adapter
    public class TopStatusViewHolder extends RecyclerView.ViewHolder {
        ItemStatusBinding binding;
        public TopStatusViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = ItemStatusBinding.bind(itemView);
        }
    }



}
