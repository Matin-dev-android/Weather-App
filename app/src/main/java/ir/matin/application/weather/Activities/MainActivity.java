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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.alterac.blurkit.BlurLayout;
import ir.matin.application.weather.Adapters.HourlyAdapter;
import ir.matin.application.weather.Config;
import ir.matin.application.weather.Domains.Hourly;
import ir.matin.application.weather.Fragments.LoadingDialog;
import ir.matin.application.weather.R;
import ir.matin.application.weather.Ui.ScaleBtn;
import ir.matin.application.weather.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mainBinding;

    RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    TextView degreeC, weatherStatusTv, cityName, dateText, maxMin, rainChanceTv, windSpeed, humidityChance , WeekNextBtn;
    ImageView picStatus;
    ConstraintLayout cityBtn ;

    String SelectedCity ;
    Bundle savedInstanceState ;
    LoadingDialog dialog ;
    BlurLayout blurLayout ;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState ;
        degreeC = findViewById(R.id.degreeTxt);
        picStatus = findViewById(R.id.weatherStatusImg);
        weatherStatusTv = findViewById(R.id.weatherStatusTxt);
        cityName = findViewById(R.id.city_name_txt);
        dateText = findViewById(R.id.dateTxt);
        maxMin = findViewById(R.id.maxMinText);
        rainChanceTv = findViewById(R.id.rainChance);
        windSpeed = findViewById(R.id.windSpeed);
        humidityChance = findViewById(R.id.hunidityChance);
        cityBtn = findViewById(R.id.cityBtn);
        WeekNextBtn = findViewById(R.id.weekNext);
        WeekNextBtn.setOnTouchListener(new ScaleBtn(this));
        cityBtn.setOnTouchListener(new ScaleBtn(this));


        cityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,GetCityActivity.class),25);

            }
        });

        SharedPreferences preferences = getSharedPreferences("user",MODE_PRIVATE);
        String city = preferences.getString("city","Tehran");
        this.SelectedCity = city ;
        WeekNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this , FutureActivity.class);
                intent.putExtra("city",SelectedCity);
                startActivity(intent);
            }
        });

        if (isInternetAvailable()) {
            getDatasAndSetUi(city);


        }
        else {
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


        dialog = new LoadingDialog(this);
        dialog.show(getSupportFragmentManager(),"LoadingDialog");

    }

    private void getDatasAndSetUi(String city) {
        String url = "https://api.weatherapi.com/v1/forecast.json?key="+ Config.API_KEY+"&q=" + city + "&aqi=no";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        JSONObject current = null;
                        try {
                            current = jsonObject.getJSONObject("current");
                            String tempC = current.getString("temp_c");
                            String condition = current.getJSONObject("condition").getString("text");
                            degreeC.setText(tempC + "°");
                            cityName.setText(city);
                            weatherStatusTv.setText(condition);

                            dialog.dismiss();

                            int iconResId = 0;
                            String apiIconUrl = jsonObject.getJSONObject("current")
                                    .getJSONObject("condition")
                                    .getString("icon");

                            if (condition.toLowerCase().contains("rain")) {
                                iconResId = R.drawable.rainy;
                            } else if (condition.toLowerCase().contains("snow")) {
                                iconResId = R.drawable.snowy;
                            } else if (condition.toLowerCase().contains("thunder") ||
                                    condition.toLowerCase().contains("storm")) {
                                iconResId = R.drawable.storm;
                            } else if (condition.toLowerCase().contains("sun") &&
                                    !condition.toLowerCase().contains("cloud")) {
                                iconResId = R.drawable.sunny;
                            } else if (condition.toLowerCase().contains("wind")) {
                                iconResId = R.drawable.windy;
                            } else if (condition.toLowerCase().contains("cloud") &&
                                    condition.toLowerCase().contains("sun")) {
                                iconResId = R.drawable.cloudy_sunny;
                            } else if (condition.toLowerCase().contains("cloud")) {
                                iconResId = R.drawable.cloudy;
                            } else if (condition.toLowerCase().contains("Clear")) {
                                iconResId = R.drawable.sunny;
                            }

                            if (iconResId != 0) {
                                picStatus.setImageResource(iconResId);
                            } else {
                                Glide.with(MainActivity.this)
                                        .load("https:" + apiIconUrl)
                                        .into(picStatus);
                                Log.d("TAG", "Icon Loaded By Server: " + apiIconUrl);
                            }


                            JSONObject forecast = jsonObject
                                    .getJSONObject("forecast")
                                    .getJSONArray("forecastday")
                                    .getJSONObject(0)
                                    .getJSONObject("day");

                            String maxTemp = forecast.getString("maxtemp_c");
                            String minTemp = forecast.getString("mintemp_c");

                            JSONObject location = jsonObject.getJSONObject("location");
                            String localtime = location.getString("localtime");

                            String[] parts = localtime.split(" ");
                            String date = parts[0];
                            String time = parts[1];

                            dateText.setText(date + "  |  " + time);
                            maxMin.setText("H : " + maxTemp + "  L :" + minTemp);


                            int humidity = current.getInt("humidity");
                            humidityChance.setText(humidity + "%");

                            double windKph = current.getDouble("wind_kph");
                            windSpeed.setText(windKph + " km/h");

                            int rainChance = 0;

                            if (jsonObject.has("forecast")) {
                                JSONObject forecastDay = jsonObject.getJSONObject("forecast")
                                        .getJSONArray("forecastday")
                                        .getJSONObject(0)
                                        .getJSONObject("day");
                                rainChance = forecastDay.getInt("daily_chance_of_rain");
                            } else {
                                double precipMm = current.getDouble("precip_mm");
                                rainChance = (precipMm > 0) ? 80 : 0;
                            }

                            rainChanceTv.setText(rainChance + "%");

                            JSONObject forecastDay = jsonObject.getJSONObject("forecast")
                                    .getJSONArray("forecastday")
                                    .getJSONObject(0);

                            ArrayList<Hourly> items = new ArrayList<>();

                            JSONArray hoursArray = forecastDay.getJSONArray("hour");

                            for (int i = 0; i < hoursArray.length(); i++) {
                                JSONObject hourObj = hoursArray.getJSONObject(i);

                                String timeFull = hourObj.getString("time");
                                String time1 = timeFull.split(" ")[1];

                                int tempC1 = (int) Math.round(hourObj.getDouble("temp_c"));

                                String picName = getIconName(condition , jsonObject);

                                if (picName == null) {

                                    picName = "ic_api_weather";
                                }

                                items.add(new Hourly(time1, tempC1, picName));

                            }
                            recyclerView = findViewById(R.id.view1);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                            adapter = new HourlyAdapter(items, MainActivity.this);
                            recyclerView.setAdapter(adapter);




                        } catch (JSONException e) {
                            dialog.setMessage(e.getMessage());
                            dialog.setTitle("Error Happened : "+e.toString());
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.setTitle("Error :");
                dialog.setMessage(volleyError.toString());
            }
        }
        );

        requestQueue.add(request);

    }

    private String getIconName(String conditionText, JSONObject jsonObject) throws JSONException {
        conditionText = conditionText.toLowerCase();

        int resId = getLocalIconRes(conditionText);

        if (resId != 0) {
            return getResources().getResourceEntryName(resId);
        }

        String apiIconUrl = jsonObject
                .getJSONObject("current")
                .getJSONObject("condition")
                .getString("icon");

        return "https:" + apiIconUrl;
    }
    private int getLocalIconRes(String c) {

        if (c.contains("rain")) return R.drawable.rainy;
        if (c.contains("snow")) return R.drawable.snowy;
        if (c.contains("thunder") || c.contains("storm")) return R.drawable.storm;
        if (c.contains("sun") && !c.contains("cloud")) return R.drawable.sunny;
        if (c.contains("wind")) return R.drawable.windy;
        if (c.contains("cloud") && c.contains("sun")) return R.drawable.cloudy_sunny;
        if (c.contains("cloud")) return R.drawable.cloudy;
        if (c.equals("clear")) return R.drawable.sunny;

        return 0;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== RESULT_OK && requestCode == 25 && data!= null){
            String s = data.getExtras().getString("selected_city",null);
            this.SelectedCity = s ;
            if (s!= null){
                getDatasAndSetUi(s);
            }
            else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else {

        }
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

    @Override
    protected void onStart() {
        super.onStart();
        blurLayout = findViewById(R.id.blur);
        blurLayout.pauseBlur();
    }
}