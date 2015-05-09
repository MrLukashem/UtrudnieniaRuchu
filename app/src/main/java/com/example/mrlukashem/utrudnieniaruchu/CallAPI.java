package com.example.mrlukashem.utrudnieniaruchu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mrlukashem on 08.04.15.
 */
public class CallAPI {
    //TODO: Opis!
    private static String usersString
            = "http://virt2.iiar.pwr.edu.pl/api/uzytkownicy/getAll";
    private static String usersByIdString
            = "http://virt2.iiar.pwr.edu.pl/api/uzytkownicy/getById";
    private static String problemsListString
            = "http://virt2.iiar.pwr.edu.pl/api/zgloszenia/getAll";
    private static String problemsByIdString
            = "http://virt2.iiar.pwr.edu.pl/api/zgloszenia/getById";
    private static String problemsByTypeString
            = "http://virt2.iiar.pwr.edu.pl/api/zgloszenia/getByType";
    private static String addNewUserString
            = "http://virt2.iiar.pwr.edu.pl:8080/RestApi/service/uzytkownicy/post";
    private static String addNewProblemString
            = "http://virt2.iiar.pwr.edu.pl:8080/RestApi/service/zgloszenia/post";

    private CallAPI() { };

    private static CallAPI callAPIInstance =
            new CallAPI();

    public static CallAPI getInstance() {
        return callAPIInstance;
    }

    public static enum GetOperation {
        GET_USERS_LIST {
            @Override
            public String toString() {
                return usersString;
            }
        },
        GET_USER_BY_ID {
            @Override
            public String toString() {
                return usersByIdString;
            }
        },
        GET_PROBLEMS_LIST {
            @Override
            public String toString() {
                return problemsListString;
            }
        },
        GET_PROBLEM_BY_ID {
            @Override
            public String toString() {
                return problemsByIdString;
            }
        },
        GET_PROBLEMS_BY_TYPE {
            @Override
            public String toString() {
                return problemsByTypeString;
            }
        };
    }

    public static enum PostOperation {
        ADD_NEW_USER {
            @Override
            public String toString() {
                return addNewUserString;
            }
        },
        ADD_NEW_PROBLEM {
            @Override
            public String toString() {
                return addNewProblemString;
            }
        };
    }

    public AsyncTask getProblemsToHandler() {
        GetApi getApi = new GetApi(GetOperation.GET_PROBLEMS_LIST);
        getApi.execute();

        return getApi;
    }

    public AsyncTask getUsersListToUserManager() {
        GetApi getApi = new GetApi(GetOperation.GET_USERS_LIST);
        getApi.execute();

        return getApi;
    }

    public AsyncTask addProblem(ProblemInstance __problem) {
        PostAddProblemApi postApi = new PostAddProblemApi(__problem);
        postApi.execute(__problem);

        return postApi;
    }

    public AsyncTask addUser(String __email, String __login, String __password) {
        PostAddUserApi postApi = new PostAddUserApi();
        postApi.execute(__email, __login, __password);

        return postApi;
    }

    public class GetApi extends AsyncTask<String, String, String> {
        public GetApi(GetOperation __mode) {
            mode = __mode;
        }

        protected GetOperation mode;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... __params) {
            String _json_result = null;
            InputStream _in;
            BufferedReader _reader = null;

            try {
                URL _url = new URL(mode.toString());
                HttpURLConnection _url_connection = (HttpURLConnection)_url.openConnection();
                _in = new BufferedInputStream(_url_connection.getInputStream());
                _reader = new BufferedReader(new InputStreamReader(_in));

            } catch(Exception __e) {
                Log.e("connection exc", "Nie mozna ustanowic polaczenia");
            }

            try {
                String _tmp = "";
                while((_json_result = _reader.readLine()) != null) {
                    _tmp += _json_result;
                }
                _json_result = _tmp;
            } catch(Exception __e) {
                Log.e("reader failed", "Blad podczas czytania strumienia");
            }

            return _json_result;
        }

        @Override
        protected void onPostExecute(String __json_result) {
            try {
                JSONArray _array = new JSONArray(__json_result);

                switch (mode) {
                    case GET_PROBLEMS_LIST:
                    for (int i = 0; i < _array.length(); i++) {
                        JSONObject _object = _array.getJSONObject(i).getJSONObject("zgloszenia");
                        ProblemInstance.ProblemData _data = ProblemInstance.createProblemData(
                                _object.getString("opis"),
                                "przykladowy_email@email.com",
                                _object.getInt("id_typu"),
                                new LatLng(_object.getJSONObject("wspolrzedne").getDouble("x"),
                                        _object.getJSONObject("wspolrzedne").getDouble("y"))
                        );
                        ObjectsOnMapHandler.objectsOnMapHandler.addProblem(_data);
                    }
                    break;

                    case GET_PROBLEM_BY_ID:
                    break;

                    case GET_PROBLEMS_BY_TYPE:
                    break;

                    case GET_USER_BY_ID:
                    break;

                    case GET_USERS_LIST:
                    List<JSONObject> _user_list = new ArrayList<>();

                    for(int i = 0; i < _array.length(); i++) {
                        JSONObject _object = _array.getJSONObject(i).getJSONObject("uzytkownicy");
                        _user_list.add(_object);
                    }

                    UserManager.getInstance().setUsersList(_user_list);
                    break;
                }
            } catch(JSONException __jexc) {
                Log.e("json error", __jexc.toString());
            } catch(Exception __exc) {
                Log.e("problem object build", __exc.toString());
            }
        }
    }

    public class PostAddUserApi extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            JSONObject _json_out;

            try {
                String _string =
                        "{\"nick\":\"" + params[1] + "\",\n"
                        + "\"email\":\"" + params[0] + "\",\n"
                        + "\"haslo\":\"" + params[2] +"\"\n"
                        + "}";

                _json_out = new JSONObject(_string);
            } catch (JSONException __json_exc) {
                Log.e("convertProblem error", __json_exc.toString());
                return -1;
            }

            OutputStreamWriter _out;
            BufferedReader _reader;

            try {
                URL _url = new URL(addNewUserString);
                URLConnection _url_connection = _url.openConnection();
                _url_connection.setDoOutput(true);
                _url_connection.setRequestProperty("Content-Type", "application/json");
                _url_connection.setConnectTimeout(5000);
                _url_connection.setReadTimeout(5000);
                _out = new OutputStreamWriter(_url_connection.getOutputStream());
                _out.write(_json_out.toString());
                _out.close();


                _reader = new BufferedReader(new InputStreamReader(_url_connection.getInputStream()));
                String _reader_out;
                while ((_reader_out = _reader.readLine()) != null) {
                    Log.e("ADD_NEW_USER", _reader_out);
                }
                _reader.close();

            } catch (Exception __e) {
                Log.e("connection exc", "Nie mozna ustanowic polaczenia" + __e.toString());
                return -1;
            }

            return 1;
        }
    }

    public class PostAddProblemApi extends AsyncTask<ProblemInstance, String, Integer> {

        private ProblemInstance problem;

        public PostAddProblemApi(ProblemInstance __problem) {
            problem = __problem;
        }

        @Override
        protected Integer doInBackground(ProblemInstance... __params) {
            JSONObject _json_out;
            int _id = -1;
            try {
                String _x = String.valueOf(__params[0].getCords().latitude);
                String _y = String.valueOf(__params[0].getCords().longitude);

                String _string = "{\"id_uzytkownika\":1,\n"
                        + "\"id_typu\":" + String.valueOf(__params[0].getCategoryId()) + ",\n"
                        + "\"id_disqus\": 5,\n"
                        + "\"x\":" + _x + ",\n"
                        + "\"y\":" + _y + "\n"
                        + "}";

                _json_out = new JSONObject(_string);
            } catch (JSONException __json_exc) {
                Log.e("convertProblem error", __json_exc.toString());
                return _id;
            }

            OutputStreamWriter _out;
            BufferedReader _reader;

            try {
                URL _url = new URL(addNewProblemString);
                URLConnection _url_connection = _url.openConnection();
                _url_connection.setDoOutput(true);
                _url_connection.setRequestProperty("Content-Type", "application/json");
                _url_connection.setConnectTimeout(5000);
                _url_connection.setReadTimeout(5000);
                _out = new OutputStreamWriter(_url_connection.getOutputStream());
                _out.write(_json_out.toString());
                _out.close();


                _reader = new BufferedReader(new InputStreamReader(_url_connection.getInputStream()));
                String _reader_out;
                while ((_reader_out = _reader.readLine()) != null) {
                    Log.e("ADD_NEW_PROBLEM error", _reader_out);
                    JSONObject _json = new JSONObject(_reader_out);
                    _id = _json.getInt("id_zgloszenia");
                }
                _reader.close();

            } catch (Exception __e) {
                Log.e("connection exc", "Nie mozna ustanowic polaczenia" + __e.toString());
                return _id;
            }

            return _id;
        }

        @Override
        protected void onPostExecute(Integer __id) {
            problem.setId(__id);
        }
    }
}
