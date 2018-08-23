package com.example.arimoussa.myapplication;


import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.MatOfRect;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String    TAG                 = "OCVSample::Activity";
    CameraBridgeViewBase cameraBridgeViewBase ;
    Mat mat1 , mGrayscaleImage  ;


   // private static final String FACE_PATH = "/Users/arimoussa/Downloads/OpenCV-android-sdk/sdk/etc/haarcascades/haarcascade_frontalface_alt.xml";
    //private static final String EYES_PATH = "/Users/arimoussa/Downloads/OpenCV-android-sdk/sdk/etc/haarcascades/haarcascade_eye_tree_eyeglasses.xml";
    private CascadeClassifier mFaceCascade;
    private CascadeClassifier mEyesCascade ;
    private int mAbsoluteFaceSize ;

    private File mcascadeFile ;
    private File mcascadeFileEye ;

    int absoluteFaceSize ;

    int nbFrame = 0;
    boolean eyeIsClosed;


    private BaseLoaderCallback  baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch(status)
            {
                case LoaderCallbackInterface.SUCCESS: {

                    Log.i(TAG, "onManagerConnected:  OpenCV loaded ");

                    try
                    {
                        //Load cascade file from appli
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                        File cascadeDir = getDir("cascade" , MODE_PRIVATE);
                        mcascadeFile = new File(cascadeDir , "haarcascade_frontalface_default.xml");
                        FileOutputStream os = new FileOutputStream(mcascadeFile) ;
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while( (bytesRead = is.read( buffer )) != -1 ){

                            os.write(buffer  , 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        InputStream iseye= getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
                        File cascadeDirEye = getDir("cascade" , Context.MODE_PRIVATE);
                        mcascadeFileEye = new File(cascadeDirEye , "haarcascade_eye_tree_eyeglasses.xml");
                        FileOutputStream oseye = new FileOutputStream(mcascadeFileEye);

                        while( (bytesRead = iseye.read(buffer)) != -1 )
                        {
                            oseye.write(buffer,0,bytesRead );
                        }
                        iseye.close();
                        oseye.close();

                        mFaceCascade = new CascadeClassifier(mcascadeFile.getAbsolutePath() );
                        mFaceCascade.load(mcascadeFile.getAbsolutePath());
                        if( mFaceCascade.empty() ) {

                            Log.i(TAG, "Failed to load cascade classifier");
                        }
                        else
                            Log.i(TAG, "Loaded cascade classifier for face from:"+mcascadeFile.getAbsolutePath() );


                        mEyesCascade = new CascadeClassifier(mcascadeFileEye.getAbsolutePath() );
                        mEyesCascade.load(mcascadeFileEye.getAbsolutePath());
                        if( mEyesCascade.empty())
                        {
                            Log.i(TAG, "Failed to load cascade classifier for eye ");
                        }
                        else
                            Log.i(TAG, "Loaded cascade classifier for eye from" + mcascadeFileEye.getAbsolutePath() );

                        cascadeDir.delete();
                        cascadeDirEye.delete();

                    }catch (IOException e){
                        e.printStackTrace();
                    }


                    cameraBridgeViewBase.enableFpsMeter();
                   // cameraBridgeViewBase.setCameraIndex(1);
                    cameraBridgeViewBase.enableView();
                    break;
                }

                default:
                    super.onManagerConnected(status);
                    break;


            }
        }
    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main   );
        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.myCameraView) ;
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE );
        cameraBridgeViewBase.setCvCameraViewListener(this);




    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mat1 = inputFrame.rgba();
        mGrayscaleImage = inputFrame.gray() ;

        MatOfRect detetedFaces = new MatOfRect();

        if(mFaceCascade != null )
        {
            mFaceCascade.detectMultiScale(mGrayscaleImage , detetedFaces , 1.1,2,2, new Size(mAbsoluteFaceSize,mAbsoluteFaceSize) , new Size() );
            Log.i(TAG, "mFaceCascade diff de null  ");

        }else
            Log.i(TAG, "mFaceCascade == null  ");

        Rect[] facesArray = detetedFaces.toArray();


        Log.i(TAG, "Nombre de face detecte : " + facesArray.length);

        // If face detected
        // Perform on eyes
        for( int i=0; i<facesArray.length ; i++ )
        {
            if( nbFrame == 3 )
            {
                Log.i(TAG, "Closed eyes detected in 3 frames : ");
                //messageBox("Closed eyes detected in 3 frames : ");

                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Define Notification Manager

            }
            // Draw a rectangle for each face
            Imgproc.rectangle(mat1 , facesArray[i].tl() , facesArray[i].br() ,  new Scalar(0,255,0,255) , 3 );

            // Detect eyes
            Mat faceROI = mGrayscaleImage.submat(facesArray[i] ) ;

            MatOfRect eyes = new MatOfRect();
            mEyesCascade.detectMultiScale(faceROI , eyes);
            List<Rect> listofeyes = eyes.toList() ;
            for( Rect eye : listofeyes )
            {
                Point eyecenter = new Point(facesArray[i].x + eye.x
                + eye.width/2 , facesArray[i].y+eye.y + eye.height/2) ;

                int radius = (int)Math.round(  (eye.width + eye.height ) * 0.25 ) ;
                Imgproc.circle(mat1, eyecenter , radius, new  Scalar(255,70,0) , 3 );
            }
            if( listofeyes.size() >= 1  ){
                eyeIsClosed = false;
                nbFrame=0;
            }
            else{
                eyeIsClosed = true;
                nbFrame++;
            }


        }



        return mat1;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mat1 = new Mat(width,height, CvType.CV_8UC4);
        mGrayscaleImage = new Mat(height, width, CvType.CV_8UC4);
        mAbsoluteFaceSize = (int) (height * 0.3f ) ;
    }


    @Override
    public void onCameraViewStopped() {

        mat1.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if( cameraBridgeViewBase!=null)
        {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( !OpenCVLoader.initDebug() )
        {
            Toast.makeText(getApplicationContext(), "There is a problem in opencv ", Toast.LENGTH_SHORT).show() ;
        }
        else
        {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( cameraBridgeViewBase!=null)
        {
            cameraBridgeViewBase.disableView();
        }

    }


    public void messageBox(String mes )
    {
        Dialog d = new Dialog(this);
       // d.setContentView(R.layout.activity_main);
        d.setTitle("Alert");
       // TextView text = (TextView) d.findViewById(R.id. );
        //text.setText(mes);


        d.show();
    }

}
