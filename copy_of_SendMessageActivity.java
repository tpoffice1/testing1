package com.test.valuecars.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static androidx.core.content.IntentCompat.getSerializableExtra;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.test.valuecars.R;
import com.test.valuecars.adapters.BitmapPagerAdapter;
import com.test.valuecars.adapters.ContactAdapter;
import com.test.valuecars.adapters.GroupAdapter;
import com.test.valuecars.models.BitmapStorage;
import com.test.valuecars.models.CarItems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SendMessageActivity extends AppCompatActivity {

    private  ActivityResultLauncher<Intent> imagePickerLauncher;

    private FrameLayout contact_groups;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private TextView tvContacts;
    private ListView lvGroups;
    private ListView lvContacts;
    private ImageView forward_mms;
    private ImageView contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send_message);
        ViewPager2 viewPager = findViewById(R.id.images);
        forward_mms = findViewById(R.id.forward_mms);
        contacts = findViewById(R.id.contacts);
        contact_groups = findViewById(R.id.contact_groups);
        tvContacts = findViewById(R.id.tvContacts);
        lvGroups = findViewById(R.id.lvGroups);
        lvContacts = findViewById(R.id.lvContacts);

        BitmapPagerAdapter adapter = new BitmapPagerAdapter(BitmapStorage.getBitmaps());
        viewPager.setAdapter(adapter);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {

                        fetchContactGroups(); // Permission granted
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    image = uri;
                }
            }
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Permission granted! You can send MMS.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permission denied. Cannot send MMS.", Toast.LENGTH_SHORT).show();
                    }
                }
        );


        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);

                return;
//                Check and request permission
//                Log.d("send---------------", "clicked");
//                if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//                    Log.d("send---------------", "granted--permission");
//                    fetchContactGroups(); // Permission already granted
//                } else {
//                    permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS); // Request permission
//                }
            }
        });

        forward_mms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMMS(BitmapStorage.getBitmaps());
            }
        });

        Log.d("CARDATA:", " size " + CarItems.getCarItems().size());
        for (int i = 0; i < CarItems.getCarItems().size(); i++) {
            Log.d("CARDATA", CarItems.getCarItems().get(i).toString());
        }
    }

    private void checkAndRequestPermission() {

    }

    private void fetchContactGroups() {
        contact_groups.setVisibility(VISIBLE);
        lvGroups.setVisibility(VISIBLE);
        lvContacts.setVisibility(GONE);
        Log.d("Value Cars: ", "fetchContactGroups()");

        List<String> groupNames = new ArrayList<>();
        List<Long> groupIds = new ArrayList<>();
        List<Boolean> groupChecked = new ArrayList<>();

        Cursor cursor = getContentResolver().query(
                ContactsContract.Groups.CONTENT_URI,
                new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE},
                null,
                null,
                null
        );

        if (cursor != null) {
            Log.d("Value Cars: ", "Cursor is not null");
            while (cursor.moveToNext()) {
                Log.d("Value Cars: ", "Cursor is in loop");
                int titleIndex = cursor.getColumnIndex(ContactsContract.Groups.TITLE);
                int idIndex = cursor.getColumnIndex(ContactsContract.Groups._ID);

                String groupTitle = cursor.getString(titleIndex);
                groupNames.add(groupTitle != null ? groupTitle : "Unknown Group");

                long groupId = cursor.getLong(idIndex);
                groupIds.add(groupId);

                groupChecked.add(false); // Initially unchecked

                Log.d("Value Cars: ", "Group: " + groupTitle + ", ID: " + groupId);
            }
            cursor.close();
        } else {
            Log.d("Value Cars: ", "Cursor is null");
        }

        // Display the groups in the Groups ListView
        GroupAdapter adapter = new GroupAdapter(this, groupNames, groupIds, groupChecked, new GroupAdapter.OnGroupClickListener() {

            @Override
            public void onGroupClick(long groupId) {
                showContactsInGroup(groupId);
            }
        });
        lvGroups.setAdapter(adapter);
        Log.d("Value Cars: ", "Groups: " + groupNames.size());
    }

    private void showContactsInGroup(long groupId) {
        lvContacts.setVisibility(VISIBLE);
        lvGroups.setVisibility(GONE);

        List<String> contactNames = new ArrayList<>();
        List<String> contactPhoneNumbers = new ArrayList<>();
        List<Boolean> contactChecked = new ArrayList<>();

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ?",
                new String[]{
                        String.valueOf(groupId),
                        ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                },
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String contactName = cursor.getString(nameIndex);
                String contactPhoneNumber = cursor.getString(phoneIndex);

                contactNames.add(contactName != null ? contactName : "Unknown Contact");
                contactPhoneNumbers.add(contactPhoneNumber != null ? contactPhoneNumber : "Unknown Number");
                contactChecked.add(false); // Initially unchecked
            }
            cursor.close();
        }

        if (contactNames.isEmpty()) {
            tvContacts.setText("No Contacts in this Group");
            lvContacts.setAdapter(null);
        } else {
            tvContacts.setText("Contacts");
            ContactAdapter contactAdapter = new ContactAdapter(this, contactNames, contactPhoneNumbers, contactChecked);
            lvContacts.setAdapter(contactAdapter);
        }
    }



    Uri image;
    private void sendMMS(ArrayList<Bitmap> images) {
        Bitmap[] pictures = new Bitmap[images.size()];
        for (int index = 0; index < images.size(); index++) {
            pictures[index] = images.get(index);
        }

        ArrayList<Uri> imageUris = new ArrayList<>();
        for (Bitmap bitmap : images) {
            Uri imageUri = getUri(this, bitmap, "test");
            imageUris.add(imageUri);

        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permison -- MMS-- granted", "Grated");
            String phonenumber = "string_removed";
            // Create an intent to track MMS status
            Intent sentIntent = new Intent("android.intent.action.MMS_SENT");
            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Bundle configOverrides = new Bundle();
            configOverrides.putString("text", "text");
            SmsManager smsmanager = SmsManager.getDefault();
            Log.d("Permission[1] -- MMS-- granted", imageUris.get(0).toString());
//            smsmanager.sendTextMessage("+12549024486", null, "message --- test", sentPendingIntent, null);

            smsmanager.sendMultimediaMessage(this, image, phonenumber, configOverrides,sentPendingIntent);

            Log.d("Permission[1] -- MMS-- granted", "Granted");
        } else {
            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }

    }

    public  Uri getUri(Context context, Bitmap bitmap, String displayName)  {
        // Specify the directory for saving (Pictures directory)
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, displayName + ".png");

        // Ensure the directory exists
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the bitmap
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Return the file Uri
        return Uri.fromFile(file);
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image", null);
        return path != null ? Uri.parse(path) : null;
    }

}
