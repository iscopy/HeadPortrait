package com.win.headportrait;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ImageView images;//图像
    private Button change;//更改

    protected static final int CHOOSE_PICTURE = 0;//本地图片参数
    protected static final int TAKE_PICTURE = 1;//照相参数
    protected static Uri uri;           //拍照图片路径
    private static final int CROP_SMALL_PICTURE = 2;//界面显示参数
    private Bitmap bitmap;     //位图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        images = (ImageView) findViewById(R.id.images);
        change = (Button) findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击更改图像按钮后，就弹一个对话框，问：是拍照还是在相册中选？
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("更改头像")
                        .setMessage("请获取新的头像！")
                        .setNegativeButton("拍照", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //调用摄像头拍照
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp_image.jpg"));
                                // 将拍照所得的相片保存到SD卡根目录
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                startActivityForResult(intent, TAKE_PICTURE);
                            }
                        })
                        .setPositiveButton("相册", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //调用相册、图库
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");//选择图片
                                startActivityForResult(intent, CHOOSE_PICTURE);
                            }
                        }).create();
                alertDialog.show();
            }
        });
    }

    //这里返回活动结果 （拍照或选择的图片）
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    cutImage(uri); // 对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    cutImage(data.getData()); // 对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        setImageToView(data); // 让刚才选择裁剪得到的图片显示在界面上
                    }
                    break;
            }
        }
    }


     // 裁剪图片
    protected void cutImage(Uri uri) {
        //调用系统自带的图片裁切功能（这里裁成正方形）
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }


    // 显示裁剪之后的图片数据
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            try {
                bitmap = extras.getParcelable("data");

                //如果需要保存或上传没有切割成圆的位图，在这个位置应该可以（没试过）

                // 将位图处理成圆形（如果不加这行代码则显示正方形的图片）
                bitmap = CircleUtils.toRoundBitmap(bitmap);
                images.setImageBitmap(bitmap);

                //也可以在这里与服务器交互或保存位图

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
