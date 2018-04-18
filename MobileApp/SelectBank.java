package com.hbcu.Activities.SignUp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hbcu.R;
import com.hbcu.Utils.Constants;
import com.hbcu.Utils.Func;
import com.hbcu.Utils.SharedPreference;
import com.hbcu.Activities.BaseActivity;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectBank extends BaseActivity implements View.OnClickListener {

    Dialog dialog;
    int ScreenOpenType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_accounts);

        ScreenOpenType = getIntent().getIntExtra(Constants.ScreenOpenType, -1);

        //===Set Toolbar==//
        Func.set_title_to_actionbar("Select Your Bank", "", "", mContext, (Toolbar) findViewById(R.id.toolbar), true, 8, 8, SelectBank.this);

        //==Google Analytics==//
        setScreenName("Select Bank");

        // Initialize Link
        HashMap<String, String> linkInitializeOptions = new HashMap<String, String>();
        linkInitializeOptions.put("key", "PlaidKey");
        linkInitializeOptions.put("product", "auth");
        linkInitializeOptions.put("apiVersion", "v2"); // set this to "v1" if using the legacy Plaid API
        // TODO change mode before going live
//        linkInitializeOptions.put("env", "development");//development  sandbox
        linkInitializeOptions.put("env", "sandbox");
        linkInitializeOptions.put("clientName", "HBCU");
        linkInitializeOptions.put("selectAccount", "false");
        linkInitializeOptions.put("webhook", "RequestLink");
        linkInitializeOptions.put("baseUrl", "PlaidBaseURL");

        final Uri linkInitializationUrl = generateLinkInitializationUrl(linkInitializeOptions);
        final WebView plaidLinkWebview = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = plaidLinkWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        WebView.setWebContentsDebuggingEnabled(true);

        plaidLinkWebview.loadUrl(linkInitializationUrl.toString());
        Log.e("plaid url", "is " + linkInitializationUrl.toString());

        plaidLinkWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri parsedUri = Uri.parse(url);
                if (parsedUri.getScheme().equals("plaidlink")) {
                    String action = parsedUri.getHost();
                    HashMap<String, String> linkData = parseLinkUriData(parsedUri);
/*
                    if(linkData.get("view_name").equalsIgnoreCase("CREDENTIALS"))
                        findViewById(R.id.toolbar).setVisibility(View.GONE);
                    else
                        findViewById(R.id.toolbar).setVisibility(View.VISIBLE);*/

                    Log.e("linkData in method: ", "" + linkData);

                    if (action.equals("connected")) {
                        Log.e("Public token: ", linkData.get("public_token"));
                        Log.e("Account ID: ", linkData.get("account_id"));
                        Log.e("Account name: ", linkData.get("account_name"));
                        Log.e("Institution name: ", linkData.get("institution_name"));
                        plaidLinkWebview.loadUrl(linkInitializationUrl.toString());
                        sendToken(linkData.get("public_token"), linkData.get("institution_id"), linkData.get("account_id"));
                    } else if (action.equals("exit")) {
                        onBackPressed();
                    } else {

                    }
                    return true;
                } else if (parsedUri.getScheme().equals("https") ||
                        parsedUri.getScheme().equals("http")) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void sendToken(String public_token, final String ins_id, String account_id) {
        Log.e("Public token in method: ", "" + public_token + "\n" + ins_id);

        try {
            showLoader();
            if (ScreenOpenType == Constants.PinScreen)
                call = api.createaccesstoken(SharedPreference.getUserId(mContext), public_token, ins_id);
            else
                call = api.signUp3(SharedPreference.getUserId(mContext), public_token, ins_id, "3");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getBoolean("error")) {
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), SelectBank.this);
                        } else {

                            Intent intent = new Intent(SelectBank.this, SelectAccount.class)
                                    .putExtra("access_token", json.getJSONObject("result").getString("plaid_access_token"));

                            if (ScreenOpenType == Constants.PinScreen)
                                intent.putExtra(Constants.ScreenOpenType, Constants.UpdateProfile);
                            else
                                intent.putExtra(Constants.ScreenOpenType, Constants.SignUpLevel);

                            startActivity(intent);
                           /* startActivity(new Intent(SelectBank.this, SelectAccount.class)
                                    .putExtra("check", 1)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));*/

                        }

                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectBank.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectBank.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectBank.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    // Generate a Link initialization URL based on a set of configuration options
    public Uri generateLinkInitializationUrl(HashMap<String, String> linkOptions) {
        Uri.Builder builder = Uri.parse(linkOptions.get("baseUrl"))
                .buildUpon()
                .appendQueryParameter("isWebview", "true")
                .appendQueryParameter("isMobile", "true");
        for (String key : linkOptions.keySet()) {
            if (!key.equals("baseUrl")) {
                builder.appendQueryParameter(key, linkOptions.get(key));
            }
        }
        return builder.build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Parse a Link redirect URL querystring into a HashMap for easy manipulation and access
    public HashMap<String, String> parseLinkUriData(Uri linkUri) {
        HashMap<String, String> linkData = new HashMap<String, String>();
        for (String key : linkUri.getQueryParameterNames()) {
            linkData.put(key, linkUri.getQueryParameter(key));
        }
        return linkData;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit) {
            if (dialog != null)
                dialog.dismiss();
        }
    }
}
