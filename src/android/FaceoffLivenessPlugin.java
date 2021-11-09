package com.unikrew.faceoff;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;



import com.unikrew.faceoff.liveness.FaceoffLivenessInitializationActivity;
import com.unikrew.faceoff.liveness.LivenessConfig;
import com.unikrew.faceoff.liveness.LivenessConfigException;
import com.unikrew.faceoff.liveness.LivenessResponse;
import com.unikrew.faceoff.liveness.LivenessResponseIPC;
import com.unikrew.faceoff.liveness.R;



import android.content.Context;
import android.content.Intent;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;
/**
 * This class echoes a string called from JavaScript.
 */
public class FaceoffLivenessPlugin extends CordovaPlugin {

    private static final String TAG = "HomeActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private static final int LIVENESS_CHECK_REQUEST = 2;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("checkLiveness")) {
            String message = args.getString(0);
            this.getRuntimePermissions();
            this.checkLiveness( callbackContext);
            return true;
        }
        return false;
    }

    private void checkLiveness( CallbackContext callbackContext) {
        Context context = cordova.getActivity().getApplicationContext();
        this.openNewActivity(context);   
        callbackContext.success("Launched Liveness Checker");
    }

    private void openNewActivity(Context context) {
        try {
            LivenessConfig config = new LivenessConfig.Builder()
        .setChallengeMoveYourFaceToLeft()
        .setChallengeMoveYourFaceToRight()
        .setChallengeBlinkYourEyes()
        .setChallengeOpenYourMouth()
        .setFaceComparisonRequired(false)
        .setOcrRequired(false)
//                            .setNicFrontImageInByte(byteArray)
        .setMaxChallenges(4) //This defines the max number of challenges to show randomly from the defined challenges.
        .build();

        cordova.setActivityResultCallback (this);
        
        Intent intent = new Intent(context,FaceoffLivenessInitializationActivity.class);
        intent.putExtra(FaceoffLivenessInitializationActivity.FACEOFF_LIVENESS_CONFIG, config);
        
       this.cordova.getActivity().startActivityForResult(intent,this.LIVENESS_CHECK_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        
        if (requestCode == this.LIVENESS_CHECK_REQUEST) {
            int responseCode = data.getIntExtra(FaceoffLivenessInitializationActivity.LIVENESS_RESPONSE_CODE, -1);
            if (responseCode > 0) {
                LivenessResponse livenessResponse = LivenessResponseIPC.getInstance().getLivenessResponse(responseCode);
                if (livenessResponse.getResponse() == LivenessResponse.Response.SUCCESS) {
                    System.out.println("Success : " + livenessResponse.getResponse().getResponseMessage());
                } else {
                    System.out.println("Failure : " + livenessResponse.getResponse().getResponseMessage());
                }
            } else {
                System.out.println( "Failed to receive the liveness response");
            }
        }
    }

    private String[] getRequiredPermissions() {
        try {
            Context context = cordova.getActivity().getApplicationContext();
            PackageInfo info =
                    context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        Context context = cordova.getActivity().getApplicationContext();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        Context context = cordova.getActivity().getApplicationContext();
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(context, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this.cordova.getActivity(), allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
