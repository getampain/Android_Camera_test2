package beprogrammer.camera_test_02;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 *
 *  기존 휴대폰에 있는 카메라에 어플리케이션에 Intent를 사용하여
 *  사진기능을 요청하고  찍은사진을 불러와 ImageVIew에 표시해주며
 *  기존의 이미지를 복사하여 지정한 새로운 경로에 저장해주는 어플리케이션입니다.
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1111;

    Button btnCapture;
    ImageView ivView;
    String currentPhotoPath;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = (Button)findViewById(R.id.btnCcapture);
        ivView = (ImageView)findViewById(R.id.ivView);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureCamera();
            }
        });

        checkDangerousPermissions();
    }


    //<summary>
    //Intent를 이용하여 카메라에 사진기능을 요청하는 함수입니다.
    //</summary>
    private  void captureCamera(){

        String state = Environment.getExternalStorageState();
        //SD 접근 검사
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;

                try {
                    photoFile = createImageFile();
                }
                catch (IOException ex){
                    Log.e("captuerCamera Error",ex.toString());
                }
                if(photoFile != null){
                    Uri providerUri = FileProvider.getUriForFile(this,
                            "beprogrammer.camera_test_02.provider",photoFile);
                    imageUri = providerUri;
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,providerUri);
                    startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
                }

            }
        }
        else{
            Toast.makeText(this,"저장공간 접근 불가능",Toast.LENGTH_SHORT).show();
            return;
        }

    }
    //<summary>
    //파일을 불러오고  지정된 경로에 복사하는 함수입니다.
    //</summary>
    private void copyPicture(){

        Log.i("copyPicture","Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //새파일만들기
        File tempFile = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(tempFile);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

        Toast.makeText(this,"사진이 저장되었습니다",Toast.LENGTH_SHORT).show();

    }


    //<summary>
    //카메라에서 처리한 결과를 받아와 그에맞는 행동을 취하는 함수입니다.
    //</summary>
    //<param name="requestCode">요청코드의 값</param>
    //<param name="resultCode">처리한 결과의 값(잘되었는지 안되었는지)</param>
    //<param name="data">Intent 데이터</param>
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case REQUEST_TAKE_PHOTO:
                if(resultCode == Activity.RESULT_OK){
                    try{
                        Log.i("REQUEST_TAKE_PHOTO","OK");
                        copyPicture();
                        ivView.setImageURI(imageUri);
                    }
                    catch (Exception e){
                        Log.e("REQUEST_TAKE_PHOTO",e.toString());
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"사진취소",Toast.LENGTH_SHORT)
                            .show();
                }
                break;

        }

    }

    //<summary>
    //유저에게 보안상의 이슈가 될 수 있는 권한에대한 허가요청을 위한 함수 입니다.
    //</summary>
    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    //<summary>
    //android.support.v4.app.ActivityCompat의 함수로 권한요청의
    //결과를 받아주는 함수 입니다.
    //</summary>
    //<param name="permssion">
    //필요한 퍼미션의 갯수와 종류와 갯수
    //</param>
    //<param name="grantResults">
    //유저가 퍼미션을 승인했는지 하지않았는지 파악해주기위한변수  0일경우 승인
    //</param>
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //<summary>
    //현재 시간에 따라 JPEG형태의 이미지 파일을 저장소/Pictures/test 경로의 디랙토리안에
    //생성해주고 파일의 경로를 String currentPhotoPath 에 저장해주는 함수 입니다 또한
    //Pictures/test 디렉토리가 없다면 생성해줍니다.
    //</summary>
    //<returns> 새로 생성된 JPEG형태의 이미지파일 </returns>
    public File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory()+ "/Pictures",
                "test");

        //경로상의 디렉토리가 없을시 생성해줍니다.
        if(!storageDir.exists()){
            Log.i("currentPhotoPaht1",storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir,imageFileName);
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;

    }
}
