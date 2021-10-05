package com.example.chatsappbynhat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatsappbynhat.Adapters.MessagesAdapter;
import com.example.chatsappbynhat.Models.Message;
import com.example.chatsappbynhat.R;
import com.example.chatsappbynhat.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;
    String senderUid;
    String receiverUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nhat: gán actionbar tự viết
        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image ....");
        dialog.setCancelable(false);

        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");

        // Nhat: gán tên và ảnh người nhận useradapter /91/
        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.profile);
        // Nhat: back trở về
        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();
        // Nhat: check hieenr thij status
        // Nhat: addValueEventListener check sự thay đổi trong db
        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty()){
                        // Nhat: offline thì ẩn đi
                        if (status.equals("Offline")){
                            binding.status.setVisibility(View.GONE);
                        }else {
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        // Nhat: người gửi
        senderRoom = senderUid + receiverUid;
        // Nhat: người nhận
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);


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

        // Nhat: ???
        final Handler handler = new Handler();

        // Nhat: gửi ảnh
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);
            }
        });
        // Nhat: check bên kia có đang chat không
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            // Nhat: bên kia thay đổi thì hiện typing
            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");

                // Nhat: xóa text trả về
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };

        });


        // Nhat: tắt hiển thị title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle(name);
//        // Nhat: nút back lại
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }
    // Nhat: check gửi ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25){
            if (data !=null){
                 if(data.getData() !=null){
                     Uri selectedImage = data.getData();
                     Calendar calendar = Calendar.getInstance();
                     StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");

                     dialog.show();

                     reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                             dialog.dismiss();
                             // Nhat: tải ảnh thành công
                             if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageTxt = binding.messageBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt,senderUid,date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);

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

                                        Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                         }
                     });
                 }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }
//    @Override
//    protected void onStop() {
//        String currentId = FirebaseAuth.getInstance().getUid();
//        database.getReference().child("presence").child(currentId).setValue("Offline");
//        super.onStop();
//    }
@Override
protected void onPause() {
    super.onPause();
    String currentId = FirebaseAuth.getInstance().getUid();
    database.getReference().child("presence").child(currentId).setValue("Offline");
}
    // Nhat: hiển thị icon menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Nhat: kết thúc sự kiện của getSupportActionBar để chạy sự kiện mới
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}