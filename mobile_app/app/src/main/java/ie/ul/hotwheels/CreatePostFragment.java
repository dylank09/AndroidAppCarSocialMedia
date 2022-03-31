package ie.ul.hotwheels;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePostFragment extends Fragment {

    ImageView postPicture;
    Button postButton;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
  
    private EditText carBrand;
    private EditText postDescription;
    private EditText yearEdit;
    private EditText modelEdit;
    private EditText engineEdit;
    private EditText locationEdit;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fstore;
    private String xCoordinate;
    private String yCoordinate;
    private ProgressBar postProgressBar;
  
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_post, container, false);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        getLastLocation();

        String imgPath = getArguments().getString("Image");
        postPicture = (ImageView) v.findViewById(R.id.postPicture);
        postPicture.setImageURI(Uri.parse(imgPath));

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        postButton = (Button) v.findViewById(R.id.button);
        carBrand = v.findViewById(R.id.carBrand);
        postDescription = v.findViewById(R.id.carDescription);
        yearEdit = v.findViewById(R.id.carYear);
        modelEdit = v.findViewById(R.id.carModel);
        engineEdit = v.findViewById(R.id.carEngine);
        postProgressBar = v.findViewById(R.id.progressBar2);
        String uniqueNumber = UUID.randomUUID().toString();
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postProgressBar.setVisibility(View.VISIBLE);
                StorageReference fileRef = storageReference.child("users/" + firebaseAuth
                        .getCurrentUser().getUid() + "/posts/" + uniqueNumber);
                fileRef.putFile(Uri.parse(imgPath)).addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onSuccess(Uri uri){
                                String userId = firebaseAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fstore
                                        .collection("users").document(userId)
                                        .collection("posts").document(uniqueNumber);
                                Map<String, Object> post = new HashMap<>();
                                post.put("postId", uniqueNumber);
                                post.put("brand", carBrand.getText().toString());
                                post.put("description", postDescription.getText().toString());
                                post.put("year", yearEdit.getText().toString());
                                post.put("model", modelEdit.getText().toString());
                                post.put("engine", engineEdit.getText().toString());
                                post.put("dateAdded", LocalDateTime.now().toString());
                                post.put("xCord", xCoordinate);
                                post.put("yCord", yCoordinate);

                                if(TextUtils.isEmpty(postDescription.getText().toString())){
                                    Toast.makeText(getContext(), "Description required.",
                                            Toast.LENGTH_SHORT).show();
                                    postProgressBar.setVisibility(View.INVISIBLE);
                                }
                                else if(TextUtils.isEmpty(carBrand.getText().toString())){
                                    Toast.makeText(getContext(), "Car brand field is empty.",
                                            Toast.LENGTH_SHORT).show();
                                    postProgressBar.setVisibility(View.INVISIBLE);
                                }
                                else if(TextUtils.isEmpty(yearEdit.getText().toString())){
                                    Toast.makeText(getContext(), "Year required.",
                                            Toast.LENGTH_SHORT).show();
                                    postProgressBar.setVisibility(View.INVISIBLE);
                                }
                                else if(TextUtils.isEmpty(modelEdit.getText().toString())){
                                    Toast.makeText(getContext(), "Model required.",
                                            Toast.LENGTH_SHORT).show();
                                    postProgressBar.setVisibility(View.INVISIBLE);
                                }
                                else if(TextUtils.isEmpty(engineEdit.getText().toString())){
                                    Toast.makeText(getContext(), "Engine required.",
                                            Toast.LENGTH_SHORT).show();
                                    postProgressBar.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    documentReference.set(post);
                                    Toast.makeText(getContext(), "Image Uploaded.",
                                            Toast.LENGTH_SHORT).show();
                                    postProgressBar.setVisibility(View.INVISIBLE);
                                    Fragment cf = new HomeFragment();
                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, cf).commit();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Image upload failure try again",
                                Toast.LENGTH_SHORT).show();
                        postProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        return v;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            xCoordinate = String.valueOf(location.getLatitude());
                            yCoordinate = String.valueOf(location.getLongitude());
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please turn on your location...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            xCoordinate = String.valueOf(mLastLocation.getLatitude());
            yCoordinate = String.valueOf(mLastLocation.getLongitude());
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                        getContext()
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                               @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
}
