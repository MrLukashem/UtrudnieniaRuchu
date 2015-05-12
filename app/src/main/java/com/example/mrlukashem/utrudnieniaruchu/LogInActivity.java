package com.example.mrlukashem.utrudnieniaruchu;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;

public class LogInActivity extends ActionBarActivity implements Runnable{
    private LoginButton FbloginButton;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private android.support.v7.app.ActionBar aBar;
    private static AccessToken accessToken = null;
    private static boolean isLoggedIn = false;
    private String email;
    private String password;
    private ProgressDialog progressDialog;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        if(getIntent().getBooleanExtra("logout", false)) {
            LoginManager.getInstance().logOut();
            Log.e("wylogowano", "wylogowano!!!");
            Log.e("wylogowano", "wylogowano!!!");
            finish();
        }

        setContentView(R.layout.activity_log_in);
        setActionBar();
        FbloginButton = (LoginButton)findViewById(R.id.connectWithFbButton);
        FbloginButton.setReadPermissions(Arrays.asList("email"));
        FbloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __v) {
                if (isLoggedIn) {
                    accessToken = null;
                    isLoggedIn = false;
                }
            }
        });

        FbloginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                isLoggedIn = true;
                loginManager = LoginManager.getInstance();
                if(loginManager != null) {
                    Log.e("log managaer", "dziala!");
                }

                GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String _id = object.getString("id");
                            String _email = object.getString("email");

                            if(!UserManager.getInstance().logInWithFB(_email, _id)) {
                                loginManager.logOut();
                            }
                        } catch (JSONException __json_exc) {
                            Log.e("Json exception-facebook", __json_exc.toString());
                        } catch(ClassNotFoundException __cnf_exc) {
                            Log.e("login button error", __cnf_exc.toString());
                        }
                    }
                }).executeAsync();
            }

            @Override
            public void onCancel() {
                accessToken = null;
            }

            @Override
            public void onError(FacebookException exception) {
                accessToken = null;
            }
        });



        handler = new Handler( new Handler.Callback() {
            @Override
            public boolean handleMessage(Message __msg) {
                progressDialog.dismiss();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        else
        if(id == android.R.id.home) {
            finishActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setActionBar() {
        if(getSupportActionBar() == null) {
            return;
        }

        aBar = getSupportActionBar();
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0099CC")));
        aBar.setHomeButtonEnabled(true);
        aBar.setDisplayHomeAsUpEnabled(true);
    }

    private void finishActivity() {
        Bundle _bundle = new Bundle();
        Intent _intent = new Intent();

        if(accessToken == null) {
            _bundle.putBoolean("log_in_status", false);
        }
        else {
            _bundle.putBoolean("log_in_status", true);
        }

        _intent.putExtras(_bundle);
        setResult(RESULT_OK, _intent);
        finish();
    }

    public void registerButtonOnClick(View __view) {
        try {
            getFragmentManager().beginTransaction()
                    .add(RegistrationDialogFragment.newInstance(), "registrationDialog")
                    .commit();
        } catch(Exception __exc) {
            Log.e("failed commit", __exc.toString());
        }
    }

    public void loginButtonOnClick(View __view) {
        if(UserManager.getInstance().isLoggedIn()) {
            UserManager.getInstance().logOut();
        } else {
            EditText _email_field = (EditText) findViewById(R.id.logInActivityEmailEditText);
            EditText _password_field = (EditText) findViewById(R.id.logInActivityPasswordEditText);
            email = _email_field.getText().toString();
            password = _password_field.getText().toString();

            progressDialog =
                    ProgressDialog.show(LogInActivity.this, "Proszę czekać ...", "Trwa logowanie ...", true);

            //new Thread(this);
            try {
                if(!UserManager.getInstance().logIn(email, password)) {
                    progressDialog.dismiss();
                    showCantLogInDialog();
                } else {
                    Toast.makeText(this, "Logowanie przebiegło pomyślnie", Toast.LENGTH_LONG).show();
                    Button _log_in_button = (Button) findViewById(R.id.loginActivityLogInButton);
                    _log_in_button.setText(getResources().getString(R.string.logged_out));
                }
            } catch(Exception __exc) {
                Log.e("userManagerExc", __exc.toString());
            }
            progressDialog.dismiss();
        }
    }

    public void showCantLogInDialog() {
        CantLogInDialogFragment _frag = new CantLogInDialogFragment();

        getFragmentManager().beginTransaction()
                            .add(_frag, "cantLogInDialog")
                            .commit();
    }

    @Override
    public void run() {
        try {
            if(!UserManager.getInstance().logIn(email, password)) {
                progressDialog.dismiss();
                showCantLogInDialog();
            } else {
                Toast.makeText(this, "Logowanie przebiegło pomyślnie", Toast.LENGTH_LONG).show();
                Button _log_in_button = (Button) findViewById(R.id.loginActivityLogInButton);
                _log_in_button.setText(getResources().getString(R.string.logged_out));
            }
        } catch(Exception __exc) {
            Log.e("userManagerExc", __exc.toString());
        }
    }
}
