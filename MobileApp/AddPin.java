package com.hbcu.Activities.SignUp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hbcu.CustomViews.CustomTextView;
import com.hbcu.R;
import com.hbcu.Utils.Constants;
import com.hbcu.Utils.Func;
import com.hbcu.Utils.SharedPreference;
import com.hbcu.Utils.SpaceItemDecoration;
import com.hbcu.Activities.BaseActivity;
import com.hbcu.Activities.Forgot.ForgotPin;
import com.hbcu.Activities.MainActivity;
import com.hbcu.Adapters.DigitAdapter;
import com.hbcu.Interfaces.OnDigitItemClickListener;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPin extends BaseActivity implements View.OnClickListener, OnDigitItemClickListener {

    RecyclerView digitList;
    TextView congratsText, forgotPin, heading;
    EditText et1, et2, et3, et4;
    Button btn_finish, viewBtn;
    RelativeLayout congRL;
    ImageView cross;
    LinearLayout linear;

    GridLayoutManager manager;
    DigitAdapter adapter;
    int[] digitData = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, R.drawable.ic_cut};
    String pin = "";
    Dialog dialog;
    int key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pin);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            key = getIntent().getIntExtra("check", 0);
        }

        //===Initialize Here===//
        init();

        //===Implement Listeners here===//
        implementListeners();

        //===Set Toolbar==//
        if (key == 1)
            Func.set_title_to_actionbar("Enter PIN", "", "", mContext, (Toolbar) findViewById(R.id.toolbar), false, 8, 8, AddPin.this);
        else if (key == 2)
            Func.set_title_to_actionbar("My Account", "90% Complete", "", mContext, (Toolbar) findViewById(R.id.toolbar), false, 0, 8, AddPin.this);
        else if (key == 3)
            Func.set_title_to_actionbar("Change PIN", "", "", mContext, (Toolbar) findViewById(R.id.toolbar), true, 8, 8, AddPin.this);

        //==Google Analytics==//
        setScreenName(key == 1 ? "Enter PIN" : key == 2 ? "Add PIN" : "Change PIN");
    }

    private void init() {
        loader = (LinearLayout) findViewById(R.id.loader);
        congratsText = (CustomTextView) findViewById(R.id.congratsText);
        forgotPin = (CustomTextView) findViewById(R.id.forgotPin);
        heading = (CustomTextView) findViewById(R.id.heading);
        digitList = (RecyclerView) findViewById(R.id.digitList);
        cross = (ImageView) findViewById(R.id.cross);
        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et3 = (EditText) findViewById(R.id.et3);
        et4 = (EditText) findViewById(R.id.et4);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        viewBtn = (Button) findViewById(R.id.viewBtn);
        congRL = (RelativeLayout) findViewById(R.id.congRL);
        linear = (LinearLayout) findViewById(R.id.linear);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.margin_normal_10dp);
        digitList.addItemDecoration(new SpaceItemDecoration(3, spacingInPixels, true, 0));

        manager = new GridLayoutManager(mContext, 3);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        digitList.setLayoutManager(manager);
        adapter = new DigitAdapter(mContext, digitData);
        adapter.setItemClick(this);
        digitList.setAdapter(adapter);

        //===Set Data===//
        if (key == 1) {
            forgotPin.setVisibility(View.VISIBLE);
            linear.setVisibility(View.GONE);
            heading.setText("Enter your 4-digit PIN");
            btn_finish.setText("Enter");
        } else if (key == 2) {
            forgotPin.setVisibility(View.GONE);
            linear.setVisibility(View.VISIBLE);
        } else if (key == 3) {
            forgotPin.setVisibility(View.GONE);
            linear.setVisibility(View.GONE);
            heading.setText("Enter a new 4-digit PIN");
            btn_finish.setText("Save Changes");
        }
    }

    private void implementListeners() {
        loader.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        cross.setOnClickListener(this);
        congRL.setOnClickListener(this);
        viewBtn.setOnClickListener(this);
        forgotPin.setOnClickListener(this);
    }

    public void snackbar(final int type) {
        if (checkInternetConnection()) {
            if (key == 1)
                enterPin();
            else if (key == 2)
                addPin();
            else if (key == 3)
                changePin();
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

    private void changePin() {

        try {
            showLoader();
            call = api.changePin(pin, SharedPreference.getUserId(mContext));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getBoolean("error")) {
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), AddPin.this);
                        } else {
                            et1.setText("");
                            et2.setText("");
                            et3.setText("");
                            et4.setText("");
                            pin = "";
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), AddPin.this);
                        }

                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    private void enterPin() {
        try {
            showLoader();
            call = api.enterPin(pin, SharedPreference.getUserId(mContext));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getBoolean("error")) {
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), AddPin.this);
                            et1.setText("");
                            et2.setText("");
                            et3.setText("");
                            et4.setText("");
                            pin = "";
                        } else {
                            if (json.getJSONObject("result").getInt("plaid_access_token")==1) {
                                startActivity(new Intent(AddPin.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            } else {
                                dialog = Func.ACHDialog(mContext,  AddPin.this);
                            }
                        }

                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    private void addPin() {
        try {
            showLoader();
            call = api.signUp5(pin, Func.DeviceId(mContext), SharedPreference.getToken(mContext), "0", "5", SharedPreference.getUserId(mContext));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getBoolean("error")) {
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), AddPin.this);
                        } else {
                            JSONObject obj = json.getJSONObject("result");
                            SharedPreference.setUserId(obj.getString("id"), mContext);
                            SharedPreference.setSignUpLevel(obj.getString("signup_level"), mContext);
                            SharedPreference.setEmail(obj.getString("email"), mContext);
                            congRL.setVisibility(View.VISIBLE);
                            congratsText.setText(SharedPreference.getName(mContext));
                        }

                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddPin.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_proceed:
                if (dialog != null)
                    dialog.dismiss();

                startActivity(new Intent(AddPin.this, SelectBank.class).putExtra(Constants.ScreenOpenType, Constants.PinScreen)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                break;
            case R.id.btn_finish:
                if (pin.trim().length() < 4)
                    dialog = Func.OneButtonDialog(mContext, "Please enter 4-digit pin to proceed further", this);
                else {
                    setEventName(key == 1 || key == 2 ? "Completed Registration process" : "PIN changed");
                    snackbar(1);
                }
                break;

            case R.id.viewBtn:
                startActivity(new Intent(AddPin.this, MainActivity.class)
                        .putExtra("position", 1)
                        .putExtra("flag", 0)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                break;

            case R.id.cross:
                startActivity(new Intent(AddPin.this, MainActivity.class)
                        .putExtra("position", 0)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                break;

            case R.id.forgotPin:
                startActivity(new Intent(AddPin.this, ForgotPin.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                break;

            case R.id.btn_submit:
                if (dialog != null)
                    dialog.dismiss();
                break;
        }
    }

    @Override
    public void onItemClick(int position) {

        if (position == digitData.length - 1) {
            if (pin.trim().length() == 4) {
                et4.setText("");
                pin = pin.substring(0, 3);
            } else if (pin.trim().length() == 3) {
                et3.setText("");
                pin = pin.substring(0, 2);
            } else if (pin.trim().length() == 2) {
                et2.setText("");
                pin = pin.substring(0, 1);
            } else if (pin.trim().length() == 1) {
                et1.setText("");
                pin = "";
            }
            Log.e("value of pin in delete", "is :-" + pin);
        } else {
            if (pin.trim().length() < 4) {
                pin = pin + String.valueOf(digitData[position]);
                if (pin.trim().length() == 1)
                    et1.setText(String.valueOf(digitData[position]));
                else if (pin.trim().length() == 2)
                    et2.setText(String.valueOf(digitData[position]));
                else if (pin.trim().length() == 3)
                    et3.setText(String.valueOf(digitData[position]));
                else if (pin.trim().length() == 4)
                    et4.setText(String.valueOf(digitData[position]));
                Log.e("value of pin in add", "is :-" + pin);
            }
        }

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
}
