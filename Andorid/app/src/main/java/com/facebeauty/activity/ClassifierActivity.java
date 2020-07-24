package com.facebeauty.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Size;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.facebeauty.R;
import com.facebeauty.utils.BitmapUtils;
import com.facebeauty.utils.FoundationDraw;
import com.facebeauty.utils.LipDraw;
import com.facebeauty.utils.MakeupBeautyUtils;
import com.facebeauty.utils.SensorEventUtil;
import com.felipecsl.gifimageview.library.GifImageView;
import com.tenginekit.AndroidConfig;
import com.tenginekit.Face;
import com.tenginekit.model.FaceLandmarkInfo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class ClassifierActivity extends AppCompatActivity {
    private static final String TAG = "ClassifierActivity";

    private GifImageView facingGif;
    List<FaceLandmarkInfo> faceLandmarks;
    private final Paint circlePaint = new Paint();
    Paint beautyPaint = new Paint();

    private MakeupBeautyUtils makeupBeautyUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier);
        onInit();
        Log.d(TAG, "###### 1111");
    }


    public void onInit() {

        makeupBeautyUtils = new MakeupBeautyUtils();


        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStrokeWidth((float) 2.0);
        circlePaint.setStyle(Paint.Style.STROKE);

        beautyPaint.setColor(Color.WHITE);
        beautyPaint.setAlpha(50);
        beautyPaint.setStyle(Paint.Style.FILL);


        facingGif = findViewById(R.id.facing_gif);

        facingGif.setBytes(readStream("TengineKitDemo3.gif"));
        Log.d(TAG, "###### GIF width is " + facingGif.getGifWidth());
        Log.d(TAG, "###### GIF height is " + facingGif.getGifHeight());


        com.tenginekit.Face.init(getBaseContext(),
                AndroidConfig.create()
                        .setNormalMode()
                        .openFunc(AndroidConfig.Func.Detect)
                        .openFunc(AndroidConfig.Func.Landmark)
                        .setInputImageFormat(AndroidConfig.ImageFormat.RGBA)
                        .setInputImageSize(facingGif.getGifWidth(), facingGif.getGifHeight())
                        .setOutputImageSize(facingGif.getGifWidth(), facingGif.getGifHeight())
        );


        facingGif.setOnFrameAvailable(new GifImageView.OnFrameAvailable() {
            @Override
            public Bitmap onFrameAvailable(Bitmap bitmap) {
                // bitmap RGB_565

                Bitmap out_bitmap = Bitmap.createBitmap(
                        facingGif.getGifWidth(),
                        facingGif.getGifHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(out_bitmap);

                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle();

                byte[] bytes = bitmap2Bytes(out_bitmap);
                Face.FaceDetect faceDetect = com.tenginekit.Face.detect(bytes);
                if(faceDetect.getFaceCount() > 0){
                    faceLandmarks = faceDetect.landmark2d();
                    if(faceLandmarks != null){
                        for (int i = 0; i < faceLandmarks.size(); i++) {
                            Path m_p = getMouthLandmarks(faceLandmarks.get(i));
                            LipDraw.drawLipPerfect(canvas, m_p, Color.WHITE, 100);
                        }
                    }
                }
                return out_bitmap;
            }
        });

        facingGif.startAnimation();



    }


    Path getFaceLandmarks(FaceLandmarkInfo fi){
        Path path = new Path();
        path.moveTo(fi.landmarks.get(0).X,fi.landmarks.get(0).Y);
        for(int i = 1; i < 37; i++){
            path.lineTo(
                    fi.landmarks.get(i).X,
                    fi.landmarks.get(i).Y
            );
        }
        for(int i = 68; i >= 53; i--){
            path.lineTo(
                    fi.landmarks.get(i).X,
                    fi.landmarks.get(i).Y
            );
        }
        for(int i = 37; i < 53; i++){
            path.lineTo(
                    fi.landmarks.get(i).X,
                    fi.landmarks.get(i).Y
            );
        }
        path.close();
        return path;
    }

    Path getMouthLandmarks(FaceLandmarkInfo fi){
        Path outPath = new Path();
        outPath.moveTo(fi.landmarks.get(180).X,fi.landmarks.get(180).Y);
        for(int i = 180; i < 189; i++){
            outPath.lineTo(
                    fi.landmarks.get(i).X,
                    fi.landmarks.get(i).Y
            );
        }
        for(int i = 204; i >= 196; i--){
            outPath.lineTo(
                    fi.landmarks.get(i).X,
                    fi.landmarks.get(i).Y
            );
        }

        outPath.close();

        Path inPath = new Path();
        inPath.moveTo(fi.landmarks.get(180).X,fi.landmarks.get(180).Y);

        for(int i = 195; i >= 188; i--){
            inPath.lineTo(
                    fi.landmarks.get(i).X,
                    fi.landmarks.get(i).Y
            );
        }
        for(int i = 204; i <= 211; i++){
            inPath.lineTo(
                    fi.landmarks.get(i).X,
                    fi.landmarks.get(i).Y
            );
        }

        outPath.op(inPath, Path.Op.DIFFERENCE);
        return  outPath;
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        com.tenginekit.Face.release();
        makeupBeautyUtils.destroy();
    }

    public byte[] readStream(String fileName) {
        try{
            InputStream inStream = getResources().getAssets().open(fileName);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while((len = inStream.read(buffer)) != -1){
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private byte[] bitmap2Bytes(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the
        return temp;
    }
}