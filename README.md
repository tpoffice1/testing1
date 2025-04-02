<details>
  <summary><strong>Application Summary</strong> (Click to expand)</summary>
  The objective of the application is to obtain the ability to select and send today's updated pictures and details of top choice cars via MMS to potential customers on any given day
  
  **GitHub Copilot Application Summary:**
The Value Cars application is an Android app designed to manage car data and contacts. It includes the following features:
1. **CSV File Processing**: Allows users to select and process a CSV file containing car data.
2. **Displaying Car Data**: Displays the processed car data in a RecyclerView.
3. **Contact Management**: Fetches and displays contact groups and their contacts from the user's device.
4. **Sending MMS**: Enables users to send images as MMS messages.
5. **Permissions Handling**: Manages permissions for reading contacts and sending SMS/MMS.
6. **Broadcast Receivers**: Handles updates on CSV processing progress and displays errors if any occur.

</details>
<details>
  <summary><strong>Issue Description</strong> (Click to expand)</summary>

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

It usually doesn't take me more than 30 minutes to get back home for screensharing at virtually anytime during my daylight hours and my current time is always accurately reflected here https://time.is/CT

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
  <summary><strong>Debugging and Fixing the `sendMMS` Method</strong> (Click to expand)</summary>

**Co-Pilot has suggested the following changes to improve the debugging process:**  

1. **Check for permissions**: Ensure the app has the necessary permissions to send SMS/MMS  
2. **Convert `Bitmap` to `Uri`**: Convert the Bitmap images to Uri objects  
3. **Create and send the MMS**: Use an Intent to send the MMS with the images attached  

### Updated `sendMMS` Method:  

```java
private void sendMMS(ArrayList<Bitmap> images) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        return;
    }

    ArrayList<Uri> imageUris = new ArrayList<>();
    for (Bitmap bitmap : images) {
        Uri imageUri = getImageUri(this, bitmap);
        if (imageUri != null) {
            imageUris.add(imageUri);
        }
    }

    Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
    sendIntent.setType("image/*");
    sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
    sendIntent.putExtra("address", "+1234567890");
    sendIntent.putExtra("sms_body", "Here are the images");

    try {
        startActivity(sendIntent);
    } catch (Exception e) {
        Log.e("MMS", "sendMMS: " + e.getMessage());
        Toast.makeText(this, "Failed to send MMS", Toast.LENGTH_SHORT).show();
    }
}

private Uri getImageUri(Context context, Bitmap bitmap) {
    String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image", null);
    return path != null ? Uri.parse(path) : null;
}
```
### Explanation  

1. **Permission Check**  
   - The method first checks if `Manifest.permission.SEND_SMS` is granted.  
   - If not, it requests the permission using `requestPermissionLauncher`.  
   
2. **Convert Bitmaps to `Uri`**  
   - Uses `getImageUri` to convert each `Bitmap` into a `Uri`.  
   - These URIs are stored in an `ArrayList` for sending.  

3. **Send MMS via Intent**  
   - Uses `Intent.ACTION_SEND_MULTIPLE` to send multiple images.  
   - Attaches the images as `EXTRA_STREAM`.  
   - Adds recipient's number and message body.  
   - The intent is then started using `startActivity()`.  

</details>
