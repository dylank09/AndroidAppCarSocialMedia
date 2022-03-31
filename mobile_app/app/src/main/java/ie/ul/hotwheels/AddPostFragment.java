package ie.ul.hotwheels;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import ie.ul.hotwheels.Utils.Permissions;
import static android.app.Activity.RESULT_OK;

public class AddPostFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CODE = 2;
    private static final int CAMERA_REQUEST_CODE = 10000;
    private static final int IMAGE_PICK_CODE = 4;

    private ImageButton openCameraBtn;
    private ImageButton openGalleryBtn;
    private Uri imageCaptureUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post,container,false);

        openCameraBtn =  view.findViewById(R.id.cameraButton);
        openGalleryBtn = view.findViewById(R.id.galleryButton);


        if(!checkPermissionArray(Permissions.PERMISSIONS)) { //check for all required permissions
            verifyPermissions(Permissions.PERMISSIONS); //ask user to verify permissions
        }

        //on click listener for the camera button
        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions(Permissions.CAMERA_PERMISSION[0])) { // ensure we have permission

                    captureImage(getContext());

                } else {
                    verifyPermissions(Permissions.PERMISSIONS); //if we don't have permission, ask user
                }
            }
        });

        //on click listener for gallery button
        openGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions(Permissions.READ_PERMISSION[0])) { // ensure we have permission

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK); //open gallery intent
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, IMAGE_PICK_CODE);

                }
                else {
                    verifyPermissions(Permissions.PERMISSIONS); //if we don't have permission, ask user
                }
            }
        });

        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data); //get cropped image

            if(resultCode == RESULT_OK) {
                goToCreatePost(result.getUri());
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                System.out.println(result.getError());
            }

        }
        else if(requestCode == CAMERA_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                startCrop(imageCaptureUri); //go to crop image screen with this picture
            }
        }
        else if (requestCode == IMAGE_PICK_CODE) {
            if(resultCode == RESULT_OK) {
                startCrop(data.getData()); //go to crop image screen with picture
            }
        }
    }

    private void startCrop(Uri imageUri) {
        //go to crop image screen, crop aspect ratio is 16:9
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setAspectRatio(16,9)
                .start(getContext(), this);
    }

    protected void goToCreatePost(Uri imgLocation) {
        //take the image to the createPostFragment
        Fragment cf = new CreatePostFragment();
        Bundle args = new Bundle();
        args.putString("Image", imgLocation.toString());
        cf.setArguments(args);

        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, cf).commit();
    }

    private static Uri getImageUri(Context context) {
        File outputDir = context.getCacheDir(); //get the cache directory for the app
        File imagePath = new File(outputDir, context.getString(R.string.app_name)); //make path

        if (!imagePath.exists()) {
            imagePath.mkdir();
        }

        File tempFile = new File(imagePath, System.currentTimeMillis()+".jpg"); //make temp file for image
        Uri uri = null;

        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            uri = FileProvider.getUriForFile(
                    context,
                    "com.example.android.fileprovider",
                    tempFile);  //get the uri for the file
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return uri;
    }

    private void captureImage(Context context) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //camera intent

        try {
            String storageState = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(storageState)) {
                imageCaptureUri = getImageUri(context);
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                cameraIntent.setClipData(ClipData.newRawUri("", imageCaptureUri)); //set the clip data for the intent if the sdk is lower than lollipop
            }

            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //allow reading of uri
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri); //attach the uri to intent and put image in
            cameraIntent.putExtra("return-data", true);

            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE); //start intent

        }
        catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    public void verifyPermissions(String[] permissions) { //verify permissions array

        requestPermissions(
                permissions,
                PERMISSIONS_REQUEST_CODE
        );
    }

    public boolean checkPermissionArray(String[] permissions) { //check permissions array to check all permissions granted or denied

        for(int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if(!checkPermissions(check)) return false;

        }
        return true;
    }

    public boolean checkPermissions(String permission) { //Check permission was granted for spectific permission

        int permissionRequest = ActivityCompat.checkSelfPermission(getContext(), permission);
        return permissionRequest == PackageManager.PERMISSION_GRANTED;
    }
}
