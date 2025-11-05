package ir.matin.application.weather.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ir.matin.application.weather.Config;
import ir.matin.application.weather.R;
import ir.matin.application.weather.Ui.ScaleBtn;

public class GetCityActivity extends AppCompatActivity {
    ImageView backImgS;
    AutoCompleteTextView autoCity;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> cityList = new ArrayList<>();

    private String USER_CITY ;

    Button applyBtn ;

    private static final String API_KEY = Config.API_KEY;
    private static final String BASE_URL = "https://api.weatherapi.com/v1/search.json?key=" + API_KEY + "&q=";

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_city);
        autoCity = findViewById(R.id.autoCity);
        applyBtn = findViewById(R.id.button);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cityList);
        autoCity.setAdapter(adapter);

        if (!isInternetAvailable()){
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Check You Connection !");
            builder.setMessage("the app need internet Connection For Load Data .");
            builder.setCancelable(false);
            builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    recreate();
                }
            });
            builder.create().show();
            return;
        }






        autoCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 1) { // وقتی کاربر بیشتر از 1 کاراکتر زد
                    fetchCities(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        autoCity.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = adapter.getItem(position);
            Toast.makeText(this, "Selected: " + selectedCity, Toast.LENGTH_SHORT).show();

            USER_CITY = selectedCity ;

        });
        applyBtn.setOnTouchListener(new ScaleBtn(this));







        backImgS = findViewById(R.id.backImgS);
        backImgS.setOnTouchListener(new ScaleBtn(this));
        backImgS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (USER_CITY != null) {
                    Intent intent = new Intent();
                    intent.putExtra("selected_city",USER_CITY);
                    setResult(RESULT_OK,intent);

                    SharedPreferences preferences = getSharedPreferences("user",MODE_PRIVATE);
                    preferences.edit().putString("city",USER_CITY).apply();


                    finish();
                }
            }
        });

    }

    private void fetchCities(String query) {
        String url = BASE_URL + query;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    cityList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject cityObj = response.getJSONObject(i);
                            String name = cityObj.getString("name");
                            String country = cityObj.getString("country");
                            cityList.add(name + ", " + country);
                        }
                        Log.d("TEST : ",cityList.toString());
                        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cityList);
                        autoCity.setAdapter(adapter);
                        autoCity.showDropDown();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading cities!", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network == null) return false;

                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return (capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)));
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }
}