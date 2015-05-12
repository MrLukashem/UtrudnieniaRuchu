package com.example.mrlukashem.utrudnieniaruchu;

import android.os.Build;
import android.util.Log;
import android.util.Patterns;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by mrlukashem on 06.05.15.
 */
public class UserManager {

    private enum SessionKind {
        normal,
        facebook,
        google
    }

    private String email;
    private String login;
    private String password;
    private String token;
    private String fbId;
    private SessionKind sessionKind;

    private Integer error = 0;
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

    public void setError(Integer __error) {
        error = __error;
    }

    public boolean logInWithFB(String __email, String __id)
            throws IllegalArgumentException, NullPointerException, ClassNotFoundException {

        error = 0;

        if(Build.VERSION.SDK_INT >= MIN_SDK) {
            if(!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(__email)).matches()) {
                throw new IllegalArgumentException("Wrong email format");
            }

            email = __email;
            fbId = Objects.requireNonNull(__id);
        } else {
            if(__email == null || __id == null) {
                throw new NullPointerException("Param cannot be null");
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(__email)).matches()) {
                throw new IllegalArgumentException("Wrong email format");
            }

            email = __email;
            fbId = __id;
        }

        try {
            CallAPI.getInstance().logInUserWithFB().get();
        } catch(InterruptedException __exc) {
            Log.e("interrupted Exc", __exc.toString());
            return false;
        } catch(ExecutionException __exc) {
            Log.e("Execution Exc", __exc.toString());
            return false;
        } catch(Exception __exc) {
            Log.e("Exc", __exc.toString());
            return false;
        }

        if(error != 0) {
            return false;
        }

        sessionKind = SessionKind.facebook;
        isLoggedIn = true;

        return true;
    }

    public boolean logIn(String __email, String __password)
            throws IllegalArgumentException, NullPointerException, ClassNotFoundException {

        error = 0;

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
            CallAPI.getInstance().logInUser().get();
        } catch(InterruptedException __exc) {
            Log.e("interrupted Exc", __exc.toString());
        } catch(ExecutionException __exc) {
            Log.e("Execution Exc", __exc.toString());
        } catch(Exception __exc) {
            Log.e("Exc", __exc.toString());
        }

        if(error != 0) {
            return false;
        }
        sessionKind = SessionKind.normal;
        isLoggedIn = true;

        return true;
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
        return email;
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

    public void setToken(String __token) {
        token = __token;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }

    public String getFBId() {
        return fbId;
    }

    public boolean setFBId(String __id) {
        if(SessionKind.facebook == sessionKind) {
            fbId = __id;
            return true;
        } else {
            return false;
        }
    }
}
