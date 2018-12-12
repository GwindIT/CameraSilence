package xiang.camerasilence;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Author : Xiang
 * E-mail : Gwind_IT@163.com
 * Date   : 18-12-12
 * Version: 1.0
 * Desc   : 静默拍照
 */
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {


    public static String PHOTO_PATH = Environment.getExternalStorageDirectory() + "/silent";

    Button mBtnTakePicture;
    SurfaceView mSurface;
    SurfaceView mSurface2;

    SurfaceHolder holder;
    SurfaceHolder holder2;

    static Camera mCamera;
    static Camera mCamera2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        initView();
    }

    /**
     * 简单的申请权限,待完善
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void initView() {
        mBtnTakePicture = findViewById(R.id.btn_take_picture);
        mSurface = findViewById(R.id.camera_surface);
        mSurface2 = findViewById(R.id.camera_surface2);
        holder = mSurface.getHolder();//获得句柄
        holder2 = mSurface2.getHolder();

        mBtnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBackPreview();
                takBackPicture();
            }
        });
    }

    /**
     * 打开前置摄像头,并设置预览
     */
    private void getFrontPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();//停掉原来摄像头的预览
            mCamera.release();//释放资源
            mCamera = null;//取消原来摄像头
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();
        for (int i = 0; i < count; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    mCamera = Camera.open(i); // 尝试打开前置摄像头
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            mCamera.setPreviewDisplay(holder);//通过surfaceview显示取景画面,否则无法拍摄
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();//开始预览
    }

    /**
     * 打开后置摄像头,并且设置预览
     */
    private void getBackPreview() {
        if (mCamera2 != null) {
            mCamera2.stopPreview();//停掉原来摄像头的预览
            mCamera2.release();//释放资源
            mCamera2 = null;//取消原来摄像头
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();
        for (int i = 0; i < count; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera2 = Camera.open(i); // 尝试打开后置摄像头
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            mCamera2.setPreviewDisplay(holder2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera2.startPreview();
    }

    /**
     * 前置摄像头拍照
     */
    private void takeFrontPicture() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {//自动对焦
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                if (success) {
                    savePicture(camera, 0 + "", false);
                }
            }
        });
    }

    /**
     * 后置摄像头拍照
     */
    private void takBackPicture() {
        mCamera2.autoFocus(new Camera.AutoFocusCallback() {//自动对焦
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                if (success) {
                    savePicture(camera, 1 + "", true);
                }
            }
        });
    }

    /**
     * 保存图片并且保存成功后调用前置摄像头拍照
     *
     * @param camera    Camera
     * @param cameraId  前后置摄像头
     * @param continued 是否继续执行前置摄像头调用拍照.
     */
    private void savePicture(Camera camera, final String cameraId, final boolean continued) {
        //设置参数，并拍照
        Camera.Parameters params = camera.getParameters();
        params.setPictureFormat(PixelFormat.JPEG);//图片格式
        params.setPreviewSize(800, 480);//图片大小
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                System.out.println("take picture success");
                String name = DateFormat.format("yyyy_MM_dd_hhmmss", Calendar.getInstance(Locale.CHINA)) + cameraId + ".jpg";
                File file = new File(PHOTO_PATH);
                file.mkdirs(); // 创建文件夹保存照片
                String filename = file.getPath() + File.separator + name;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length);
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            filename);
                    boolean b = bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                            fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    if (b) {
                        Toast.makeText(MainActivity.this, "照片保存成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "照片保存失败", Toast.LENGTH_LONG).show();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (mCamera != null) {
                        try {
                            mCamera.setPreviewDisplay(null);
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (mCamera2 != null) {
                        try {
                            mCamera2.setPreviewDisplay(null);
                            mCamera2.stopPreview();
                            mCamera2.release();
                            mCamera2 = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (continued) {
                        getFrontPreview();
                        takeFrontPicture();
                    }
                }
            }
        });//将拍摄到的照片给自定义的对象
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            mCamera = Camera.open(0);
            try {
                mCamera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                mCamera.startPreview();//开始预览
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (mCamera2 == null) {
            mCamera2 = Camera.open(1);
            try {
                mCamera2.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                mCamera2.startPreview();//开始预览
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.removeCallback(this);


        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        mSurface = null;

        mCamera2.stopPreview();
        mCamera2.release();
        mCamera2 = null;
        mSurface2 = null;
    }
}
