package com.hbcu.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbcu.Activities.SignUp.AddPin;
import com.hbcu.R;
import com.hbcu.Retrofit.RetrofitInterface;
import com.hbcu.Utils.Constants;
import com.hbcu.Utils.HbcuApplication;
import com.hbcu.Utils.SharedPreference;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseActivity extends AppCompatActivity {

    public String regid;
    public Activity mContext;
    public LinearLayout loader;
    public RetrofitInterface api,api1;
    public Call<ResponseBody> call;
    public final int PERMISSION_REQUEST_CODE = 3, PERMISSION_REQUEST_CODE_CG = 4;
    boolean check;
    public Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        HbcuApplication application = (HbcuApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //====Initialize Here===//
        initializeVariable();
    }

    void initializeVariable() {
        if (mContext == null)
            mContext = BaseActivity.this;

        if (api == null)
            api = retrofitCall().create(RetrofitInterface.class);

        if (api1 == null)
            api1 = retrofitCall1().create(RetrofitInterface.class);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (call != null)
            call.cancel();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    public void registerFCM() {
        if (SharedPreference.getToken(mContext) == null || SharedPreference.getToken(mContext).trim().length() == 0) {
            regid = FirebaseInstanceId.getInstance().getToken();
            SharedPreference.setToken(regid, mContext);
        } else {
            regid = SharedPreference.getToken(mContext);
        }
        Log.e("Access Token", regid + "   ID");
    }

    public void alertDialog(final String type) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BaseActivity.this);
        alertDialogBuilder.setMessage("For using camera, you need to enable camera and storage permission.")
                .setPositiveButton("Proceed",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (type.equalsIgnoreCase("0")) {
                                    if (!checkPermissionForCG())
                                        requestPermissionCG();
                                } else {
                                    requestPermissionCG();
                                }
                            }
                        })
                .setNegativeButton("Exit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void alertDialogDeny(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BaseActivity.this);
        alertDialogBuilder.setMessage(msg)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final Intent i = new Intent();
                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + getPackageName()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                startActivity(i);
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionCG() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(BaseActivity.this, Manifest.permission.CAMERA)
                && ActivityCompat.shouldShowRequestPermissionRationale(BaseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && ActivityCompat.shouldShowRequestPermissionRationale(BaseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_CG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_CG);
        }
    }

    //Permission for camera & gallery permissions
    public boolean checkPermissionForCG() {
        int result = ContextCompat.checkSelfPermission(BaseActivity.this, Manifest.permission.CAMERA);
        int write = ContextCompat.checkSelfPermission(BaseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int resultGallery = ContextCompat.checkSelfPermission(BaseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
                && resultGallery == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkInternetConnection() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected();
    }

    //===== retrofit for api calling =====//
    public Retrofit retrofitCall() {
        String baseUrl = Constants.baseURL;
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(40, TimeUnit.SECONDS)
                .connectTimeout(40, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public Retrofit retrofitCall1() {
        String baseUrl = "https://sandbox.plaid.com/";
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(40, TimeUnit.SECONDS)
                .connectTimeout(40, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public void showLoader() {
        if (loader != null) {
            loader.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoader() {
        if (loader != null)
            loader.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null)
            call.cancel();
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(check) {
            if (SharedPreference.getUserId(getApplicationContext()).length() > 0 && SharedPreference.getEmail(getApplicationContext()).length() > 0) {
                startActivity(new Intent(BaseActivity.this, AddPin.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("check", 1));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        check=isAppIsInBackground(mContext);
        Log.e("on stop","called "+isAppIsInBackground(mContext));
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }else
                isInBackground=true;
        }
        return isInBackground;
    }

    public void setScreenName(String name){
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void setEventName(String event){
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction(event)
                .build());
    }
}