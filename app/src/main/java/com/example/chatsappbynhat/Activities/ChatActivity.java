package com.example.chatsappbynhat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.example.chatsappbynhat.Adapters.MessagesAdapter;
import com.example.chatsappbynhat.Models.Message;
import com.example.chatsappbynhat.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages = new ArrayList<>();




        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();

        // Nhat: người gửi
        senderRoom = senderUid + receiverUid;
        // Nhat: người nhận
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance();

        // Nhat: lấy tứng thằng trong chat đê render ra
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {//???
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        messages.clear();
                        // Nhat: add từng thằng message vào arrlist
                        for (DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            // Nhat: lấy key
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        // Nhat: update lại tài nguyên
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Nhat: gán tin nhắn xong gửi lên db
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = binding.messageBox.getText().toString();

                Date date = new Date();
                Message message = new Message(messageTxt,senderUid,date.getTime());
                // Nhat: nhắn xong xóa text
                binding.messageBox.setText("");
                //push method generates a unique key every time a new child is add to the specified FireBase reference
                // Nhat: key chuyển đển room
                String randomKey = database.getReference().push().getKey();

                // Nhat: lấy thằng cuối cùng để hiện ra
                HashMap<String,Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg",message.getMessage());
                lastMsgObj.put("lastMsgTime",date.getTime());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


                database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(randomKey)
                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                        // Nhat: nếu gửi tin thành công thì ...
                        @Override
                        public void onSuccess(Void aVoid) {
                            database.getReference().child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(randomKey)
                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                // Nhat: nếu gửi tin thành công thì ...
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                     }
                    });
            }
        });

        getSupportActionBar().setTitle(name);
        // Nhat: nút back lại
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Nhat: kết thúc sự kiện của getSupportActionBar để chạy sự kiện mới
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}