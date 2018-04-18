package com.hbcu.Activities.SignUp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hbcu.Activities.BaseActivity;
import com.hbcu.Activities.MainActivity;
import com.hbcu.Adapters.SelectAccountAdapter;
import com.hbcu.Models.AccountModel;
import com.hbcu.R;
import com.hbcu.Utils.Constants;
import com.hbcu.Utils.Func;
import com.hbcu.Utils.SharedPreference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectAccount extends BaseActivity implements View.OnClickListener {

    RecyclerView selectAccountList;
    Button btn_continue;
    LinearLayoutManager manager;

    SelectAccountAdapter adapter;
    ArrayList<AccountModel> list = new ArrayList<>();

    String access_token = "", account_id = "";
    int ScreenOpenType;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_account);

        ScreenOpenType = getIntent().getIntExtra(Constants.ScreenOpenType, -1);

        getData();

        //===Set Toolbar==//
        Func.set_title_to_actionbar("Select Your Account", "", "", mContext, (Toolbar) findViewById(R.id.toolbar), false, 8, 8, SelectAccount.this);

        //==Google Analytics==//
        setScreenName("Select Account");

        init();

        implementListeners();
    }

    private void getData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            access_token = getIntent().getStringExtra("access_token");

    }

    private void init() {
        loader = (LinearLayout) findViewById(R.id.loader);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        selectAccountList = (RecyclerView) findViewById(R.id.selectAccountList);

        manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        selectAccountList.setLayoutManager(manager);

        snackbar(1);
    }

    private void implementListeners() {
        loader.setOnClickListener(this);
        btn_continue.setOnClickListener(this);
    }

    public void snackbar(final int type) {
        if (checkInternetConnection()) {
            if (type == 1)
                getAccounts();
            else
                saveAccount();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.line1), getString(R.string.NO_INTERNET), Snackbar.LENGTH_LONG)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar(type);
                        }
                    });
            snackbar.setActionTextColor(Color.WHITE);
            snackbar.show();
        }
    }

    private void saveAccount() {
        try {
            showLoader();
            call = api.createAccountId(SharedPreference.getUserId(mContext), access_token, account_id);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getBoolean("error"))
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), SelectAccount.this);
                        else {
                            if (ScreenOpenType == Constants.UpdateProfile)
                                startActivity(new Intent(SelectAccount.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            else
                                startActivity(new Intent(SelectAccount.this, AddCard.class)
                                        .putExtra("check", 1)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }

                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectAccount.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectAccount.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectAccount.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    private void getAccounts() {
        try {
            Log.e("access token", " is " + access_token);

            Map<String, String> obj = new HashMap<>();
            obj.put("client_id", "596cd0c14e95b810ac887df6");
            obj.put("secret", "36ca7ae963c88b05111f1246a5df69");
            obj.put("access_token", access_token);

            showLoader();
            call = api1.getAccounts(obj);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String s = response.body().string();
                        JSONObject json = new JSONObject(s);
                        if (list.size() > 0)
                            list.clear();

                        JSONArray obj = json.getJSONArray("accounts");
                        for (int i = 0; i < obj.length(); i++) {
                            JSONObject object = obj.getJSONObject(i);
                            if (object.getString("subtype").equalsIgnoreCase("checking")) {
                                AccountModel model = new AccountModel();
                                model.setAccount_id(object.getString("account_id"));
                                model.setMask(object.getString("mask"));
                                model.setSubtype(object.getString("subtype"));
                                model.setSelected(false);
                                list.add(model);
                            }
                        }

                        if (list.size() > 0) {
                            checkSingleCheckingAccount();
                            setAdapter();
                        } else
                            dialog = Func.OneButtonDialog(mContext, "No checking account found!", SelectAccount.this);
                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectAccount.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectAccount.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), SelectAccount.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    private void checkSingleCheckingAccount() {
        if (list.size() == 1) {
            AccountModel accountModel = list.get(0);
            accountModel.setSelected(true);
            list.set(0, accountModel);
        }
    }

    private void setAdapter() {
        adapter = new SelectAccountAdapter(mContext, list);
        selectAccountList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                if (dialog != null)
                    dialog.dismiss();
                break;

            case R.id.btn_continue:
                for (int i = 0; i < list.size(); i++) {
                    AccountModel accountModel = list.get(i);
                    if (accountModel.isSelected()) {
                        account_id = accountModel.getAccount_id();
                        break;
                    }
                }

                if (account_id.length() <= 0) {
                    dialog = Func.OneButtonDialog(mContext, "Please select account to continue", this);
                } else {
                    snackbar(2);
                }
                break;

        }
    }

}
