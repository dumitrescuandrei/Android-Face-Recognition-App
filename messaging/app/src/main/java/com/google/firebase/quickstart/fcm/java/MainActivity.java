/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.fcm.java;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.quickstart.fcm.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private StorageReference realImage;
    private DatabaseReference rootRef;
    private DatabaseReference myMessage;
    private BitmapDrawable bitmapDrawable;
    private Bitmap bitmap1;
    String mesaj = "null";

    @BindView(R.id.first_image)
    ImageView mImageView;
    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.share)
    Button button1;
    Map<String, String> dsMesaj = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String smsNumber = "40785636026"; //without '+'
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseAuth.getInstance()
                .signInAnonymously();

        FirebaseStorage storage = FirebaseStorage.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();
        myMessage = rootRef.child("mesaj");
        myMessage.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {};
                //Map<String, String> sMesaj = dataSnapshot.getValue(genericTypeIndicator);
                //System.out.println(sMesaj.toString());
                Map<String, String> sMesaj = (Map<String, String>)dataSnapshot.getValue();
                String messages = (String)sMesaj.get("mesaj");
                message.setText(messages);
                String images = (String)sMesaj.get("image");
                StorageReference realImage = storage.getReference(images);
                GlideApp.with(getApplicationContext())
                        .load(realImage)
                        .into(mImageView);
                Resources resources = getApplicationContext().getResources();
                Uri uri = new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(resources.getResourcePackageName(R.id.first_image))
                        .appendPath(resources.getResourceTypeName(R.id.first_image))
                        .appendPath(resources.getResourceEntryName(R.id.first_image))
                        .build();
                System.out.println(uri.toString());


                button1.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        try {
                            bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
                            bitmap1 = bitmapDrawable.getBitmap();
                            String imgBitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap1, "title", null);
                            Uri imgBitmapUri = Uri.parse(imgBitmapPath);
                            Intent sendIntent = new Intent("android.intent.action.MAIN");
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            //sendIntent.setType("text/plain");

                            sendIntent.putExtra("andrei", smsNumber + "@s.whatsapp.net"); //phone number without "+" prefix
                            sendIntent.putExtra(Intent.EXTRA_STREAM, imgBitmapUri);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, messages + " \n");
                            sendIntent.setType("*/*");
                            sendIntent.setPackage("com.whatsapp");
                            startActivity(sendIntent);
                        } catch(Exception e) {

                        }
                    }
                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

        /*ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String mesaj = dataSnapshot.child("/-M5SZeRtDhnTgPkZX286").getValue(String.class);
                System.out.println("here1");
                message.setText(mesaj);
                System.out.println("here2");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };*/




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]


    }


}
