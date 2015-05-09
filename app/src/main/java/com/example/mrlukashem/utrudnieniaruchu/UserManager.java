package com.example.mrlukashem.utrudnieniaruchu;

import android.os.Build;
import android.util.Log;
import android.util.Patterns;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Created by mrlukashem on 06.05.15.
 */
public class UserManager {

    private String email;
    private String login;
    private String password;

    private boolean isLoggedIn = false;
    private final static Integer MIN_SDK = 19;
    private List<JSONObject> usersList;

    private static UserManager userManager = new UserManager();
    private UserManager() {}

    public static UserManager getInstance() {
        return userManager;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void logOut() {
        isLoggedIn = false;
        email = null;
        login = null;
    }

    public boolean logIn(String __email, String __password)
            throws IllegalArgumentException, NullPointerException, ClassNotFoundException {

        if(Build.VERSION.SDK_INT >= MIN_SDK) {
            if(!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(__email)).matches()) {
                throw new IllegalArgumentException("Wrong email format");
            }

            email = __email;
            password = Objects.requireNonNull(__password);
        } else {
            if(__email == null || __password == null) {
                throw new NullPointerException("Param cannot be null");
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(__email)).matches()) {
                throw new IllegalArgumentException("Wrong email format");
            }

            email = __email;
            password = __password;
        }

        try {
            CallAPI.getInstance().getUsersListToUserManager().get();
        } catch(InterruptedException __exc) {
            Log.e("interrupted Exc", __exc.toString());
        } catch(ExecutionException __exc) {
            Log.e("Execution Exc", __exc.toString());
        } catch(Exception __exc) {
            Log.e("Exc", __exc.toString());
        }

        if(usersList != null) {
            String _email, _password;
            for(JSONObject object : usersList) {
                try {
                    _email = object.getString("email");
                    _password = object.getString("haslo");

                    if(_email.equals(__email) && _password.equals(__password)) {
                        isLoggedIn = true;
                        return true;
                    }
                } catch(JSONException __json_exc) {
                    return false;
                }
            }
        } else {
            throw new ClassNotFoundException("cannot load usersList");
        }

        return false;
    }

    public void adduser(String __email, String __login, String __password) throws IllegalArgumentException, NullPointerException {
        if(Build.VERSION.SDK_INT >= MIN_SDK) {
            if(!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(__email)).matches()) {
                throw new IllegalArgumentException("Wrong email format");
            }

            CallAPI.getInstance().addUser(
                    __email,
                    Objects.requireNonNull(__login),
                    Objects.requireNonNull(__password));
        } else {
            if(__email == null || __login == null || __password == null) {
                throw new NullPointerException("Param cannot be null");
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(__email).matches()) {
                throw new IllegalArgumentException("Wrong email format");
            }

            CallAPI.getInstance().addUser(
                    __email,
                    __login,
                    __password
            );
        }
    }

    public String getEmail() {
        return "213";
    }

    public void setUsersList(List<JSONObject> __usersList) {
        usersList = __usersList;
    }

    public boolean isUserExist(String __email) {
        if(usersList == null) {
            return false;
        }

        String _email;
        for(JSONObject json : usersList) {
            try {
                _email = json.getString("email");
                if(__email.equals(_email)) {
                    return true;
                }
            } catch (JSONException __json_exc) {
                Log.e("json exception", __json_exc.toString());
                return false;
            }
        }

        return false;
    }
}
