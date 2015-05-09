package com.example.mrlukashem.utrudnieniaruchu;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.facebook.FacebookSdk;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.net.Uri;
import java.util.Objects;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Created by mrlukashem
 */
/*
TODO: Settingsy!
TODO: Scustomizować dialog markera
 */
public class MainActivity extends ActionBarActivity
        implements OnMapReadyCallback, NewMarkerOnMap, SharedPreferences.OnSharedPreferenceChangeListener, LocationListener {

    //map fields
    private GoogleMap gMap;
    private MapFragment mapFragment;
    private UiSettings uiSettings;
    private final LatLng WROCLAW_RYNEK = new LatLng(51.1056248, 17.0381557);

    //listeners fields
    private GoogleMap.OnMapClickListener onMapClickListener;
    private GoogleMap.OnMarkerClickListener onMarkerClickListener;
    private GoogleMap.OnMapLongClickListener longClickListener;
    private ListView.OnItemClickListener navDrawerListListener;

    //drawer fields
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private NavDrawerArrayAdapter drawerListAdapter;

    //dialogs
    private Dialog markerContentDialog;
    private CategoriesChoiceDialogFragment chooseCatDialog;
    //ActionBar
    private ActionBar aBar;

    //new marker form data
    private boolean isFromCurrentPosition;
    private String tempContentFromNewMarkerForm;
    private int tempCatIdFromMarkerForm;
    private LatLng lastLongClickLatLng;

    private static final int NEW_MARKER = 0;
    private static final int MAP_SET_TERRAIN = 1;
    private static final int MAP_SET_SATATELITE = 2;
    private static final int CATEGORIES_FILTER = 3;
    private static final int CATEGORIES_LIST = 4;
    private static final int OPTIONS = 5;
    private static final int HELP = 6;
    private static final int LOG_IN = 7;

    //shared preferences
    private SharedPreferences sharedPreferences;

    //refeshing
    private final long minuteInMs = 1000 * 60;
    private long refreshDelay = minuteInMs * 20; //1000ms * 60(=1min) * 20 = 20min
    private Handler refreshingHandler = new Handler();
    private boolean refresh = true;

    //request code
    private final int requestCode = 1;

    private static final int GET_IMG_REQUEST_CODE = 2;
    ImageView imageHandler;
    //is Logged in?
    private static boolean isLoggedIn = false;

    private GPSTracker gpsTracker;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if(refresh) {
                CallAPI.getInstance().getProblemsToHandler();
                refreshingHandler.postDelayed(this, refreshDelay);
            }
        }
    };

    //preference screen fields
    private final String KEY_PREF_NO_REFRESH = "applicationRefresing";
    private final String KEY_PREF_APLICATION_REFRESING_DELAY = "applicationRefresingDelay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        ObjectsOnMapHandler.objectsOnMapHandler.setObjectsOnMapHandler(gMap, getApplicationContext());
        setContentView(R.layout.activity_main);
        enableListeners();
        setActionBar();
        setSharedPreferences();
    //    getLogInfoFromSP();

        mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_frame, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        markerContentDialog = DialogFactory
                .newInstance(DialogFactory.DIALOG_TYPE.MARKER_CONTENT_DIALOG, this);
        setDialogFragments();

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerListView = (ListView)findViewById(R.id.left_drawer);
        drawerListAdapter =
                new NavDrawerArrayAdapter(this, R.id.drawer_text_view_list_item);
        drawerListAdapter.makeAndSetItems();

        drawerListView.setOnItemClickListener(navDrawerListListener);
        drawerListView.setAdapter(drawerListAdapter);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                 R.string.app_name, R.string.app_name);

        drawerLayout.setDrawerListener(drawerToggle);
        aBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0099CC")));

        refreshingHandler.postDelayed(refreshRunnable, refreshDelay);
        setLogInfo();
    }

    @Override
    protected void onActivityResult(int __requestCode, int __resultCode, Intent __data) {
        switch(__requestCode) {
            case requestCode:
                if (__resultCode == RESULT_OK) {
                    Bundle _res = __data.getExtras();
                    Boolean _result = _res.getBoolean("log_in_status");
                    isLoggedIn = _result;
                    setLogInfo();
                }
                break;
            case GET_IMG_REQUEST_CODE:
                Uri _img_uri = __data.getData();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().putBoolean("fb_log_info", isLoggedIn).apply();
    }

    @Override
    protected void onPostCreate(Bundle __saved_instance_state) {
        super.onPostCreate(__saved_instance_state);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration __newConfig) {
        super.onConfigurationChanged(__newConfig);
        drawerToggle.onConfigurationChanged(__newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu __menu) {
        __menu.clear();
        getMenuInflater().inflate(R.menu.menu_main, __menu);
        super.onCreateOptionsMenu(__menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem __item) {
        int id = __item.getItemId();

        if (id == R.id.action_settings) {
            showPreferenceScreen();
            return true;
        }
        else
        if(drawerToggle.onOptionsItemSelected(__item)){
            return true;
        }
        else
        if(id == R.id.action_add_marker) {
            if(isLoggedIn) {
                prepareToNewMarker();
            } else {
                Toast _toast = Toast.makeText(
                        getApplicationContext(),
                        "Aby móc dodawać nowe utrudnienia ruchu trzeba być zalogowanym!", //TODO: Wpisac do res
                        Toast.LENGTH_LONG);
                _toast.show();
            }
        }
        else
        if(id == android.R.id.home) {
            FragmentTransaction fragmentTransaction =
                    getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.content_frame, mapFragment);
            fragmentTransaction.commit();

            drawerToggle.setDrawerIndicatorEnabled(true);
            mapFragment.getMapAsync(this);
            ObjectsOnMapHandler.objectsOnMapHandler.refresh();
            return true;
        }
        else
        if(id == R.id.set_start_location) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(WROCLAW_RYNEK, 14));
        }

        return super.onOptionsItemSelected(__item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences __sharedPreferences, String __key) {
        if(__key.equals(KEY_PREF_NO_REFRESH)) {
            try {
                boolean _value = __sharedPreferences.getBoolean(__key, true);
                if (_value) {
                    refreshingHandler.removeCallbacks(refreshRunnable);
                    refreshingHandler.postDelayed(refreshRunnable, refreshDelay);
                } else {
                    refreshingHandler.removeCallbacks(refreshRunnable);
                }
            } catch(Exception __e) {
                Log.e("Preference error", __e.toString());
            }
        }
        else
        if(__key.equals(KEY_PREF_APLICATION_REFRESING_DELAY)) {
            try {
                String _delay = __sharedPreferences.getString(__key, "60");
                Log.e("delay->", _delay);
                refreshDelay = minuteInMs * Long.parseLong(_delay);
            } catch(Exception __e) {
                Log.e("Preference error", __e.toString());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        uiSettings = googleMap.getUiSettings();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(WROCLAW_RYNEK, 14));

        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        gMap.setMyLocationEnabled(true);

        setMapListeners();
        setCustomInfoWindow();
        ObjectsOnMapHandler.objectsOnMapHandler.setMap(gMap);

        CallAPI.getInstance().getProblemsToHandler();
    }

    //TODO: wywalic
    @Override
    public void onLocationChanged(Location __location) {
    }

    @Override
    public void onStatusChanged(String __provider, int __status, Bundle __extras) {
    }

    @Override
    public void onProviderEnabled(String __provider) {
    }

    @Override
    public void onProviderDisabled(String __provider) {
    }

    public void loadImageButton(View __view) {
        Intent _intent = new Intent();
        _intent.setType("image/*");
        _intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(_intent, "Wybierz zdjęcie!"), GET_IMG_REQUEST_CODE);
    }

    private void setMapListeners() {
        gMap.setOnMapClickListener(onMapClickListener);
        gMap.setOnMarkerClickListener(onMarkerClickListener);
    }

    private void setSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void setCustomInfoWindow() {
        gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker __marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker __marker) {
                ProblemInstance _problem =
                        ObjectsOnMapHandler.objectsOnMapHandler.findProblemByMarker(__marker);
                LayoutInflater _inflater = getLayoutInflater();
                View _custom_view = _inflater.inflate(R.layout.window_info_content, null);

                TextView _content = (TextView) _custom_view.findViewById(R.id.contentWindowInfoTextView);
                _content.setText(__marker.getSnippet());

                TextView _title = (TextView) _custom_view.findViewById(R.id.titleWindowInfoTextView);
                _title.setText(__marker.getTitle());

                TextView _author = (TextView)_custom_view.findViewById(R.id.authorName);
                String _nick = _problem.getEmail().split("@")[0];
                _author.setText(_nick);

                return _custom_view;
            }
        });

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker __marker) {
                ProblemInstance _problem =
                        ObjectsOnMapHandler.objectsOnMapHandler.findProblemByMarker(__marker);
                if (_problem != null) {
                    showMarkerContentDialog(_problem);
                }
            }
        });
    }

    private void setActionBar() {
        aBar = this.getSupportActionBar();
        if(aBar == null)
            return;

        this.aBar.setDisplayHomeAsUpEnabled(true);
        this.aBar.setDisplayShowTitleEnabled(false);
        this.aBar.setHomeButtonEnabled(true);
    }

    private void setDialogFragments() {
        chooseCatDialog = CategoriesChoiceDialogFragment.newInstance();
    }

    private void enableListeners() {
        onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //temp
                Toast _toast =
                        Toast.makeText(getApplicationContext(), "kliknięcie na mape", Toast.LENGTH_LONG);
                _toast.show();
            }
        };

        onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        };

        navDrawerListListener = new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> __parent, View __view, int __position, long __id) {
                switch (__position) {
                    case NEW_MARKER:
                        if(isLoggedIn) {
                            prepareToNewMarker();
                        } else {
                            Toast _toast = Toast.makeText(
                                    getApplicationContext(),
                                    "Aby móc dodawać nowe utrudnienia ruchu trzeba być zalogowanym!", //TODO: Wpisac do res
                                    Toast.LENGTH_LONG);
                            _toast.show();
                        }
                        break;
                    case MAP_SET_TERRAIN:
                        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        drawerLayout.closeDrawers();
                        break;
                    case MAP_SET_SATATELITE:
                        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        drawerLayout.closeDrawers();
                        break;
                    case CATEGORIES_FILTER:
                        showFilterCategoriesDialog();
                        break;
                    case CATEGORIES_LIST:
                        /*
                            TODO: Zrobienie jakiś podstawowych opcji w nowym Activity
                         */
                        break;
                    case OPTIONS:
                        showPreferenceScreen();
                        break;
                    case HELP:

                        break;
                    case LOG_IN:
                        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                        startActivityForResult(intent, requestCode);
                        break;
                    default:
                        break;

                };
            }
        };

        longClickListener = new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng __latLng) {
                ProblemInstance.ProblemData _data =
                        ProblemInstance.createProblemData(
                                tempContentFromNewMarkerForm,
                                UserManager.getInstance().getEmail(),
                                tempCatIdFromMarkerForm,
                                __latLng);
                ObjectsOnMapHandler.objectsOnMapHandler.addProblemToServerAndMap(_data);

                gMap.setOnMapLongClickListener(null);
            }
        };
    }

    public void enableLongClickListener() {
        gMap.setOnMapLongClickListener(longClickListener);
    }

    @Override
    public void createMarkerFromFormData(boolean __is_from_current_position, int __category_id, String __content) {
        setFormData(
                __is_from_current_position,
                __category_id,
                Objects.requireNonNull(__content, "Content agr is null"));
        addMarkerFromFormData();
    }

    @Override
    public void showToastMarkerInfo() {
        Toast _text = Toast.makeText(
                this,
                R.string.onLongClickOnMapString,
                Toast.LENGTH_LONG);
        _text.show();
    }

    private void prepareToNewMarker() {
        drawerLayout.closeDrawers();
        showNewMarkerFormDialog();
    }

    private void setFormData(boolean __is_from_current_position, int __category_id, String __content) {
        isFromCurrentPosition = __is_from_current_position;
        tempCatIdFromMarkerForm = __category_id;
        tempContentFromNewMarkerForm = __content;
    }

    private void addMarkerFromFormData() {
        if(isFromCurrentPosition) {
            gpsTracker = new GPSTracker(getApplicationContext());
            Location _location;
            try {
                _location = gpsTracker.getLocation();

                if(_location == null) {
                    _location = gpsTracker.getLocation();
                    if(_location == null) {
                        isFromCurrentPosition = false;
                        gpsTracker.stopUsingGPS();
                        gpsTracker.showErrorWindow(getFragmentManager());
                    }
                    return;
                }
            } catch(GPSTracker.LocationException __exc) {
                Log.e("gps provider exc:", __exc.toString());
                gpsTracker.showErrorWindow(getFragmentManager());
                return;
            }

            LatLng _latlng = new LatLng(gpsTracker.getLastLatitude(), gpsTracker.getLastLongitude());
            ProblemInstance.ProblemData _data =
                    ProblemInstance.createProblemData(
                            tempContentFromNewMarkerForm,
                            UserManager.getInstance().getEmail(),
                            tempCatIdFromMarkerForm,
                            _latlng
                            );

            ObjectsOnMapHandler.objectsOnMapHandler.addProblemToServerAndMap(_data);
            isFromCurrentPosition = false;
            gpsTracker.stopUsingGPS();
        } else {
            enableLongClickListener();
            showToastMarkerInfo();
        }
    }

    private void setLogInfo() {
        if(isLoggedIn) {
            drawerListAdapter.setLoggedIn();
        } else {
            drawerListAdapter.setLoggedOut();
        }
    }

    private void getLogInfoFromSP() {
        isLoggedIn = sharedPreferences.getBoolean("fb_log_info", false);
    }

    private void showPreferenceScreen() {
        drawerToggle.setDrawerIndicatorEnabled(false);
        getFragmentManager().beginTransaction()
                .remove(mapFragment)
                .commit();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new PreferenceScreen())
                .commit();
        drawerLayout.closeDrawers();
    }

    private void showNewMarkerFormDialog() {
        FragmentTransaction _f_transaction = getFragmentManager().beginTransaction();
        DialogFragment _f_dialog = (DialogFragment)getFragmentManager().findFragmentByTag("FormDialog");
        if(_f_dialog != null) {
            _f_transaction.remove(_f_dialog);
        }
        _f_transaction.addToBackStack(null);

        FormDialogFragment _f = FormDialogFragment.newInstance();
        _f_transaction.add(_f, "FormDialog");
        _f_transaction.commit();
    }

    private void showFilterCategoriesDialog() {
        FragmentTransaction _f_transaction = getFragmentManager().beginTransaction();
        DialogFragment _f_dialog = (DialogFragment)getFragmentManager().findFragmentByTag("FilterDialog");
        if(_f_dialog != null) {
            _f_transaction.remove(_f_dialog);
        }
        _f_transaction.addToBackStack(null);

        CategoriesChoiceDialogFragment _f = CategoriesChoiceDialogFragment.newInstance();
        _f_transaction.add(_f, "FilterDialog");
        _f_transaction.commit();
    }

    private void showMarkerContentDialog(ProblemInstance __problem) {
        FragmentTransaction _f_transaction = getFragmentManager().beginTransaction();
        DialogFragment _f_dialog = (DialogFragment)getFragmentManager().findFragmentByTag("MarkerContentDialog");
        if(_f_dialog != null) {
            _f_transaction.remove(_f_dialog);
        }
        _f_transaction.addToBackStack(null);

        MarkerContentDialogFragment _f = MarkerContentDialogFragment.newInstance(__problem);
        _f_transaction.add(_f, "FilterDialog");
        _f_transaction.commit();
    }
}
