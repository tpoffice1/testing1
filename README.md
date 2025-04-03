<details>
  <summary><strong>Overall Objective and Summary of Application</strong> (Click to expand)</summary>

  <br>

  **The owner's overall objective of the application is to obtain the ability to select and send today's updated pictures and details of top choice cars via MMS to potential customers on any given day.**
  
  **GitHub Copilot Application Summary:** This is what Copilot sees currently.

  The Value Cars application is an Android app designed to manage car data and contacts. It includes the following features:

  1. **CSV File Processing**: Allows users to select and process a CSV file containing car data.  
  2. **Displaying Car Data**: Displays the processed car data in a RecyclerView.  
  3. **Contact Management**: Fetches and displays contact groups and their contacts from the user's device.  
  4. **Permissions Handling**: Manages permissions for reading contacts and sending SMS/MMS.  
  5. **Broadcast Receivers**: Handles updates on CSV processing progress and displays errors if any occur.

**References**:
1. `activity_send_message.xml`
2. `activity_main.xml`
3. `SendMessageActivity.java`
4. `MainActivity.java`

</details>

<details>
  <summary><strong>Current Issue Description</strong> (Click to expand)</summary>

The `SendMMS` method initiated from within the `SendMessageActivity.java` that has been copied to this public repository at  
[https://github.com/tpoffice1/testing1](https://github.com/tpoffice1/testing1)

It isn't working correctly and we need to figure out the best way to test it together.

According to the prior developer who apparently doesn't want to answer questions now said "only Google can fix it"

I don't yet fully understand what is happening on the developer's side, and he has some logistical problems that prevent him from being in front of a computer and online when needed.

He doesn't respond now when I send him messages and I think he is just stuck.

He mentioned that the workaround is to find or write another API, which seems dangerous to me from a personal security standpoint.  

I don't want there to be anything in the code that interacts directly with my account until it is running on my phone only, and I explicitly select the Google account for it to work with.  

I need additional eyes on what is broken within the `sendMMS` method perhaps using the recommended troubleshooting method that Co-Pilot mentions as depicted in the shared public version of the repository code in question.

If you are ok with the online payment terms of $3 per successful session and would like to see how it goes, then send me a link with audio and screensharing and let's try to solve this one

</details>

<details>
  <summary><strong>Availability</strong> (Click to expand)</summary>

My day usually starts at 7:30 a.m. Central Time https://time.is/CT.  It usually doesn't take me more than 30 minutes to get back home for screensharing at virtually anytime during my daylight hours and my current time is always accurately reflected here https://time.is/CT

</details>

<details>
  <summary><strong>Screenshots</strong> (Click to expand)</summary>

  **Screen 1:** This screen allows the user to bring up a list of data sources by clicking on the purple button for the approved data processing action.  
  ![Screen 1](https://github.com/user-attachments/assets/752c8659-e666-4274-84e8-37a2bc432031)

  **Screen 2:** This screen allows the user to select which data is to be processed from the selected list.  
  | ![Screen 2](https://github.com/user-attachments/assets/ff5b5c5a-da9d-4739-97e5-5e2772eebd02) |
  |---|

  **Screen 3:** The code that produces this screen has been temporarily commented out for the purpose of testing the SendMMS method.

  **Screen 4:** This screen allows the user to select up to twelve images from within each of the seven groups of cars showing.  
  ![Screen 4](https://github.com/user-attachments/assets/7998fadc-6858-468c-884c-f73c7e0d7ada)

  **Screen 5:** This screen allows the user to confirm selected images from here, select contacts from one of the contact groups, and then type a message before sending the selected pictures to the potential customer.  
  | ![Screen 5](https://github.com/user-attachments/assets/9ea7e461-b105-42f6-a2de-b2cf5ae3241b) |
  |---|

</details>

<details>
  <summary><strong>Notes from last update including Co-Pilot recommendation</strong> (Click to expand)</summary>

We developed the code to get the current default messaging. We now have the ability to select which SMS application to use, but sending MMS is still not working. The problem that if the google messaging app is selected, then it still returns null

To address the issue of sending MMS when the Google Messaging app is selected, ensure that the `sendMMS` method is correctly implemented and that the `Transaction` class is properly configured. Here is a step-by-step approach:

1. **Check Default SMS App**: Ensure that your app is set as the default SMS app before sending MMS.
2. **Handle Null Return**: Add checks to handle cases where the Google Messaging app might return null.

### Updated `sendMMS` Method

```java
private void sendMMS(ArrayList<Bitmap> images) {
    if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        default_launcher.launch(intent);
        return; // Exit the method to wait for the user to set the default SMS app
    }

    ArrayList<Uri> imageUris = new ArrayList<>();
    for (Bitmap bitmap : images) {
        Uri imageUri = getUri(this, bitmap, "test");
        if (imageUri != null) {
            imageUris.add(imageUri);
        } else {
            Log.e("sendMMS", "Failed to get URI for image");
        }
    }

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
        Log.d("Permission -- MMS-- granted", "Granted");

        mThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("ThreadPool/MMSManager", "Trying to send MMS.");
                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc(mSettings.getMmsc());
                sendSettings.setProxy(mSettings.getProxy());
                sendSettings.setPort(mSettings.getPort());
                sendSettings.setUseSystemSending(true);

                Transaction transaction = new Transaction(getApplicationContext(), sendSettings);

                Message message = new Message("Check out this image!", phonenumber);
                message.setSave(false);

                for (Uri uri : imageUris) {
                    message.addImage(uri);
                }

                transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
                Log.d("Permission[1] -- MMS-- granted", "Granted");
            }
        });
    } else {
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }
}
```

### `getUri` Method

Ensure the `getUri` method correctly saves the bitmap and returns the `Uri`.

```java
public Uri getUri(Context context, Bitmap bitmap, String displayName) {
    File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MMS");
    if (!directory.exists()) {
        directory.mkdirs();
    }

    File file = new File(directory, displayName + ".png");
    try (FileOutputStream out = new FileOutputStream(file)) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    } catch (IOException e) {
        e.printStackTrace();
        return null; // Return null if there is an error
    }

    return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
}
```

### `FileProvider` Configuration in `AndroidManifest.xml`

Ensure the `FileProvider` is correctly configured.

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### `file_paths.xml`

Create `res/xml/file_paths.xml` to define the file paths.

```xml
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="external_files" path="." />
</paths>
```

These steps should help ensure that the `sendMMS` method works correctly, even when the Google Messaging app is selected.

## Additional Debugging  
- Ensure that `imageUri` correctly points to the image file and that the file exists.  
- If the issue persists, check the Logcat for any error messages that might provide more insight into the problem.

</details>
