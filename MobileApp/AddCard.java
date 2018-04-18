package com.hbcu.Activities.SignUp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hbcu.R;
import com.hbcu.Utils.Func;
import com.hbcu.Utils.SharedPreference;
import com.hbcu.Activities.BaseActivity;
import com.hbcu.CustomViews.CustomTextView;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCard extends BaseActivity implements View.OnClickListener {

    EditText nameEt, cardNumEt, monthEt, yearEt, cvvEt;
    Button btn_continue;
    Toolbar toolbar;
    TextView title_tv, title1_tv;
    CheckBox defaultCard;
    LinearLayout linear;

    String SEPARATOR = " ", mPreviousText, APIcardnumber = "", APIexpiryMonth = "", APIexpiryYear = "", APIcvv = "", tid = "", cno = "", PUBLISHABLE_KEY = "pk_test_ho0D4zAZULYWZcvNHEHFasyE"
            //TODO change key before going live /*PUBLISHABLE_KEY = "pk_live_egNBOvzPd1DewC3sOGQe4H0c"*/
            , is_default = "";
    int key;
    Dialog dialog;
    //pk_test_LdsHMlToNNCf4FRSAdObZ20g  pk_live_egNBOvzPd1DewC3sOGQe4H0c

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

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
            Func.set_title_to_actionbar("My Account", "80% Complete", "", mContext, (Toolbar) findViewById(R.id.toolbar), false, 0, 8, AddCard.this);
        else
            Func.set_title_to_actionbar("Add Card", "", "", mContext, (Toolbar) findViewById(R.id.toolbar), true, 8, 8, AddCard.this);

        //==Google Analytics==//
        setScreenName("Add Card");

    }

    private void init() {
        loader = (LinearLayout) findViewById(R.id.loader);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title_tv = (CustomTextView) toolbar.findViewById(R.id.title_tv);
        title1_tv = (CustomTextView) toolbar.findViewById(R.id.title1_tv);
        nameEt = (EditText) findViewById(R.id.nameEt);
        cardNumEt = (EditText) findViewById(R.id.cardNumEt);
        monthEt = (EditText) findViewById(R.id.monthEt);
        yearEt = (EditText) findViewById(R.id.yearEt);
        cvvEt = (EditText) findViewById(R.id.cvvEt);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        linear = (LinearLayout) findViewById(R.id.linear);
        defaultCard = (CheckBox) findViewById(R.id.defaultCard);

        //===Set Visibility===//
        if (key == 1)
            linear.setVisibility(View.VISIBLE);
        else
            linear.setVisibility(View.GONE);
    }

    void implementListeners() {
        loader.setOnClickListener(this);
        btn_continue.setOnClickListener(this);
        cardNumEt.addTextChangedListener(cardWatcher);
    }

    public final TextWatcher cardWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                cardNumEt.setError(null);
            }
            String difference = "";

            if (cardNumEt.getText().toString().trim().length() + 1 == 5
                    || cardNumEt.getText().toString().trim().length() + 1 == 10
                    || cardNumEt.getText().toString().trim().length() + 1 == 15) {

                difference = SEPARATOR;
            } else {
                difference = Func.difference(s.toString(), mPreviousText);
            }
            if (!difference.equals(SEPARATOR)) {
                addSeparatorToText();
            }

            mPreviousText = s.toString();
        }
    };

    private void addSeparatorToText() {
        String text = cardNumEt.getText().toString();
        text = text.replace(SEPARATOR, "");
        if (text.length() >= 16) {
            return;
        }
        int interval = 4;
        char separator = SEPARATOR.charAt(0);

        StringBuilder stringBuilder = new StringBuilder(text);
        for (int i = 0; i < text.length() / interval; i++) {
            stringBuilder.insert(((i + 1) * interval) + i, separator);
        }
        cardNumEt.removeTextChangedListener(cardWatcher);
        cardNumEt.setText(stringBuilder.toString());
        cardNumEt.setSelection(cardNumEt.getText().length());
        cardNumEt.addTextChangedListener(cardWatcher);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_continue:
                setEventName("Clicked to continue to add a PIN for security");
                snackbar(1);
                break;

            case R.id.btn_submit:
                if (dialog != null)
                    dialog.dismiss();
                break;
        }
    }

    private void check_validations() {
        if (Func.editSize(nameEt) <= 0) {
            dialog = Func.OneButtonDialog(mContext, "Please enter name", this);
        } else if (Func.editSize(cardNumEt) < 19) {
            dialog = Func.OneButtonDialog(mContext, "Please enter valid card number", this);
        } else if (Func.editSize(monthEt) <= 0) {
            dialog = Func.OneButtonDialog(mContext, "Please enter expiry month", this);
        } else if (Func.editSize(yearEt) <= 0) {
            dialog = Func.OneButtonDialog(mContext, "Please enter expiry year", this);
        } else if (Func.editSize(cvvEt) <= 0) {
            dialog = Func.OneButtonDialog(mContext, "Please enter valid CVV", this);
        } else {
            SaveDataNProceed();
        }
    }

    public void snackbar(final int type) {
        if (checkInternetConnection()) {
            if (type == 1) {
                check_validations();
            }
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

    private void SaveDataNProceed() {
        APIcardnumber = cardNumEt.getText().toString();
        APIexpiryMonth = monthEt.getText().toString();
        APIexpiryYear = yearEt.getText().toString();
        APIcvv = cvvEt.getText().toString();

        final Card card = new Card(APIcardnumber, Integer.parseInt(APIexpiryMonth), Integer.parseInt(APIexpiryYear), APIcvv);

        boolean validation = card.validateCard();
        final String card_type = card.getBrand();

        if (validation) {
            showLoader();
            new Stripe(AddCard.this).createToken(card, PUBLISHABLE_KEY, new TokenCallback() {
                public void onSuccess(Token token) {
                    tid = token.getId();
                    cno = token.getCard().getLast4();
                    if (!tid.isEmpty()) {
                        if (checkInternetConnection()) {
                            if (defaultCard.isChecked())
                                is_default = "1";
                            else
                                is_default = "0";
                            if (key == 1)
                                saveCardAPI(tid, cno, card_type, is_default);
                            else
                                saveNewCard(tid, cno, card_type, is_default);
                        } else
                            Func.showSnackbar(getCurrentFocus(), getResources().getString(R.string.NO_INTERNET));
                    }
                }

                public void onError(Exception error) {
                    handleError(error.getLocalizedMessage());
                    hideLoader();
                }
            });

        } else if (!card.validateNumber()) {
            handleError("The card number that you entered is invalid");
        } else if (!card.validateExpiryDate()) {
            handleError("The expiration date that you entered is invalid");
        } else if (!card.validateCVC()) {
            handleError("The CVV code that you entered is invalid");
        } else {
            handleError("The card details that you entered are invalid");
        }
    }

    private void saveNewCard(String tid, String cno, String card_type, String is_default) {
        try {
            Log.e("token", "is" + tid);
            call = api.addCard(nameEt.getText().toString().trim(), cno, tid, card_type, is_default, SharedPreference.getUserId(mContext));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getBoolean("error")) {
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), AddCard.this);
                        } else {
                            Intent intt = new Intent();
                            setResult(RESULT_OK, intt);
                            finish();
                        }

                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddCard.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddCard.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddCard.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    private void saveCardAPI(String tid, String cno, String card_type, String is_default) {
        try {
            Log.e("token", "is" + tid);
            call = api.signUp4(nameEt.getText().toString().trim(), cno, tid, card_type, is_default, SharedPreference.getUserId(mContext), "4");
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getBoolean("error")) {
                            dialog = Func.OneButtonDialog(mContext, json.getString("message"), AddCard.this);
                        } else {
                            JSONObject obj = json.getJSONObject("result");
                            SharedPreference.setCardCount("1", mContext);
                            SharedPreference.setCardId(obj.getString("id"), mContext);
                            startActivity(new Intent(AddCard.this, AddPin.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("check", 2));
                        }

                    } catch (Exception e) {
                        dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddCard.this);
                        e.printStackTrace();
                    }
                    hideLoader();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddCard.this);
                    hideLoader();
                }
            });
        } catch (Exception e) {
            dialog = Func.OneButtonDialog(mContext, getString(R.string.ERROR_MSG), AddCard.this);
            hideLoader();
            e.printStackTrace();
        }
    }

    private void handleError(String error) {
        Toast.makeText(AddCard.this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
