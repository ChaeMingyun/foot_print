package landvive.footprint;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by chaemingyun on 2016. 7. 21..
 */

public class UploadActivity extends AppCompatActivity implements
        View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "Storage#DBMainActivity";

    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private BroadcastReceiver mDownloadReceiver;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    // [START declare_ref]
    private StorageReference mStorageRef = storage.getReferenceFromUrl("gs://footprint-7f978.appspot.com");
    // [END declare_ref]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Storage Ref
        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]

        // Click listeners
        findViewById(R.id.button_camera).setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_download).setOnClickListener(this);

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }

        // Download receiver
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "downloadReceiver:onReceive:" + intent);
                hideProgressDialog();

                if (DownloadActivity.ACTION_COMPLETED.equals(intent.getAction())) {
                    String path = intent.getStringExtra(DownloadActivity.EXTRA_DOWNLOAD_PATH);
                    long numBytes = intent.getLongExtra(DownloadActivity.EXTRA_BYTES_DOWNLOADED, 0);

                    // Alert success
                    showMessageDialog("Success", String.format(Locale.getDefault(),
                            "%d bytes downloaded from %s", numBytes, path));
                }

                if (DownloadActivity.ACTION_ERROR.equals(intent.getAction())) {
                    String path = intent.getStringExtra(DownloadActivity.EXTRA_DOWNLOAD_PATH);

                    // Alert failure
                    showMessageDialog("Error", String.format(Locale.getDefault(),
                            "Failed to download from %s", path));
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());

        // Register download receiver
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDownloadReceiver, DownloadActivity.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }


    //    @AfterPermissionGranted(RC_STORAGE_PERMS)
    public void getPictureFromGallery() throws FileNotFoundException {
    /*
    //This allows to select the application to use when selecting an image.
    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
    i.setType("image/*");
    startActivityForResult(Intent.createChooser(i, "Escoge una foto"), PICTURE_TAKEN_FROM_GALLERY);
    */
//        // Create a reference to "mountains.jpg"
//        StorageReference mountainsRef = mStorageRef.child("mountains.jpg");
//
//// Create a reference to 'images/mountains.jpg'
//        StorageReference mountainImagesRef = mStorageRef.child("images/mountains.jpg");
//
//// While the file names are the same, the references point to different files
//        mountainsRef.getName().equals(mountainImagesRef.getName());    // true
//
//        // Check that we have permission to read images from external storage.
//        String perm = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
//        if (!EasyPermissions.hasPermissions(this, perm)) {
//            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
//                    RC_STORAGE_PERMS, perm);
//            return;
//        }

        // Choose file storage location, must be listed in res/xml/file_paths.xml
//        File externalDir = Environment.getExternalStorageDirectory();
//        File file = new File(externalDir, "photos/" + UUID.randomUUID().toString() + ".jpg");
//
//        // Create content:// URI for file, required since Android N
//        // See: https://developer.android.com/reference/android/support/v4/content/FileProvider.html
//        mFileUri = FileProvider.getUriForFile(this,
//                "com.google.firebase.quickstart.firebasestorage.fileprovider", file);

        //This takes images directly from gallery
        Intent gallerypickerIntent = new Intent(Intent.ACTION_PICK);
        gallerypickerIntent.setType("image/*");

        startActivityForResult(gallerypickerIntent, 2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    uploadFromUri(data.getData());
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // [START upload_from_uri]
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos").child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        UploadTask uploadTask = photoRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload succeeded
                Log.d(TAG, "uploadFromUri:onSuccess");

                // Get the public download URL
                mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                // [START_EXCLUDE]
                hideProgressDialog();
                updateUI(mAuth.getCurrentUser());
                // [END_EXCLUDE]
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        mDownloadUrl = null;

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(UploadActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                        updateUI(mAuth.getCurrentUser());
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]


    //    @AfterPermissionGranted(RC_STORAGE_PERMS)
    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Check that we have permission to read images from external storage.
        String perm = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (!EasyPermissions.hasPermissions(this, perm)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    RC_STORAGE_PERMS, perm);
            return;
        }

        // Choose file storage location, must be listed in res/xml/file_paths.xml
        File externalDir = Environment.getExternalStorageDirectory();
        File file = new File(externalDir, "photos/" + UUID.randomUUID().toString() + ".jpg");

        // Create content:// URI for file, required since Android N
        // See: https://developer.android.com/reference/android/support/v4/content/FileProvider.html
        mFileUri = FileProvider.getUriForFile(this,
                "com.google.firebase.quickstart.firebasestorage.fileprovider", file);

        // Create and launch the intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

        startActivityForResult(takePictureIntent, RC_TAKE_PICTURE);
    }

    private void signInAnonymously() {
        // Sign in anonymously. Authentication is required to read or write from Firebase Storage.
        showProgressDialog();
        mAuth.signInAnonymously()
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInAnonymously:SUCCESS");
                        hideProgressDialog();
                        updateUI(authResult.getUser());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                        hideProgressDialog();
                        updateUI(null);
                    }
                });
    }

    private void beginDownload() {
        // Get path
        String path = "photos/" + mFileUri.getLastPathSegment();

        // Kick off download service
        Intent intent = new Intent(this, DownloadActivity.class);
        intent.setAction(DownloadActivity.ACTION_DOWNLOAD);
        intent.putExtra(DownloadActivity.EXTRA_DOWNLOAD_PATH, path);
        startService(intent);

        // Show loading spinner
        showProgressDialog();
    }

    private void updateUI(FirebaseUser user) {
        // Signed in or Signed out
        if (user != null) {
            findViewById(R.id.layout_signin).setVisibility(View.GONE);
            findViewById(R.id.layout_storage).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_signin).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_storage).setVisibility(View.GONE);
        }

        // Download URL and Download button
        if (mDownloadUrl != null) {
            ((TextView) findViewById(R.id.picture_download_uri))
                    .setText(mDownloadUrl.toString());
            findViewById(R.id.layout_download).setVisibility(View.VISIBLE);
        } else {
            ((TextView) findViewById(R.id.picture_download_uri))
                    .setText(null);
            findViewById(R.id.layout_download).setVisibility(View.GONE);
        }
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_camera:
                try {
                    getPictureFromGallery();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button_sign_in:
                signInAnonymously();
                break;
            case R.id.button_download:
                beginDownload();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }
}
