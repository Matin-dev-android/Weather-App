package ir.matin.application.weather.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ir.matin.application.weather.Adapters.FutureAdapter;
import ir.matin.application.weather.Domains.Future;
import ir.matin.application.weather.R;
import ir.matin.application.weather.Ui.ScaleBtn;

public class FutureActivity extends AppCompatActivity {
    String CITY = "Tehran";
    RecyclerView recyclerView;

    RecyclerView.Adapter adapter;
    TextView txtFutureTemp, weatherStateFuture, rainChanceF, winSpeedF, humidityF;
    ImageView picF, backBtn;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future);
        txtFutureTemp = findViewById(R.id.tempFutureText);
        weatherStateFuture = findViewById(R.id.weatherStateFuture);
        picF = findViewById(R.id.picF);
        rainChanceF = findViewById(R.id.rainChanceF);
        winSpeedF = findViewById(R.id.winSpeedF);
        humidityF = findViewById(R.id.humidityF);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnTouchListener(new ScaleBtn(this));
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        CITY = getIntent().getStringExtra("city");
        if (isInternetAvailable()) {
            getDatasAndSetFutureUi(CITY);
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

    }

    private void getDatasAndSetFutureUi(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, "UTF-8");
            String url = "https://api.weatherapi.com/v1/forecast.json?key=0d885891cdda42b99c3170642251209&q="
                    + encodedCity + "&days=7&aqi=no&alerts=no";

            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            if (!response.has("forecast")) {
                                // هیچ اطلاعات forecast نیست — هندل کن
                                Toast.makeText(this, "Forecast not available", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONArray forecastDays = response.getJSONObject("forecast")
                                    .getJSONArray("forecastday");

                            if (forecastDays.length() < 2) {
                                Toast.makeText(this, "Tomorrow's forecast not available", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONObject tomorrowObj = forecastDays.getJSONObject(1); // index 1 = فردا
                            JSONObject dayObj = tomorrowObj.getJSONObject("day");

                            // دما (می‌تونی maxtemp/mintemp هم استفاده کنی)
                            double avgTemp = dayObj.optDouble("avgtemp_c", Double.NaN);
                            // احتمال باران (درصد)
                            int rainChance = dayObj.optInt("daily_chance_of_rain", 0);
                            // سرعت باد (میانگین/حداکثر — ما از max استفاده می‌کنیم)
                            double windKph = dayObj.optDouble("maxwind_kph", 0.0);
                            // درصد رطوبت متوسط
                            int humidity = dayObj.optInt("avghumidity", 0);

                            // وضعیت متنی و آیکون از داخل day.condition
                            JSONObject condition = dayObj.optJSONObject("condition");
                            String conditionText = "";
                            String iconStr = "";
                            if (condition != null) {
                                conditionText = condition.optString("text", "");
                                iconStr = condition.optString("icon", "");
                            }

                            // تکمیل URL آیکون (API گاهی با "//cdn..." برمی‌گردونه)
                            if (!iconStr.isEmpty() && iconStr.startsWith("//")) {
                                iconStr = "https:" + iconStr;
                            }

                            // ست کردن UI (مثال)
                            if (!Double.isNaN(avgTemp))
                                txtFutureTemp.setText(Math.round(avgTemp) + "°C");
                            else txtFutureTemp.setText("-");

                            rainChanceF.setText(rainChance + "%");
                            winSpeedF.setText(Math.round(windKph) + " km/h");
                            humidityF.setText(humidity + "%");
                            weatherStateFuture.setText(conditionText);

                            // انتخاب آیکون: اول تلاش کن آیکون محلی (getIconName)، اگر آیکون محلی نبود، از API لود کن
                            String localIconName = getIconName(conditionText, tomorrowObj);

                            Glide.with(this).load(iconStr).into(picF);


                            // todo fill list

                            ArrayList<Future> list = new ArrayList<>();

                            // شروع از اندیس 1 چون [0] = امروز
                            for (int i = 1; i < forecastDays.length(); i++) {
                                JSONObject dayObj1 = forecastDays.getJSONObject(i);
                                String date = dayObj1.getString("date"); // مثل 2025-09-15
                                JSONObject day = dayObj1.getJSONObject("day");
                                JSONObject condition1 = day.getJSONObject("condition");

                                // گرفتن اطلاعات اصلی
                                int maxTemp = (int) Math.round(day.optDouble("maxtemp_c", 0));
                                int minT = (int) Math.round(day.optDouble("mintemp_c", 0));
                                int rainChance1 = day.optInt("daily_chance_of_rain", 0);
                                String statusText = condition1.optString("text", "");

                                // آیکون → یا لوکال یا API
                                String iconName = getIconName(condition1.getString("text"), response);

                                // روز هفته از روی تاریخ
                                String weekDay = getWeekDay(date);

                                // پر کردن مدل Future (مدلت ۴ یا ۵ پارامتر داره؟ من فرض می‌کنم مثل مثالت: weekday, picPath, desc, temp, rain)
                                list.add(new Future(weekDay, iconName, statusText, maxTemp, minT));
                            }

                            // ست کردن توی آداپتر
                            adapter = new FutureAdapter(list, FutureActivity.this);
                            recyclerView = findViewById(R.id.view2);
                            recyclerView.setLayoutManager(new LinearLayoutManager(FutureActivity.this));
                            recyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Error loading forecast", Toast.LENGTH_SHORT).show();
                    });

            queue.add(request);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getWeekDay(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.getDefault()); // Sat, Sun...
            return sdfDay.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    private String getIconName(String conditionText, JSONObject jsonObject) throws JSONException {
        conditionText = conditionText.toLowerCase();

        if (conditionText.contains("rain")) {
            return "rainy";
        } else if (conditionText.contains("snow")) {
            return "snowy";
        } else if (conditionText.contains("thunder") || conditionText.contains("storm")) {
            return "storm";
        } else if (conditionText.contains("sun") && !conditionText.contains("cloud")) {
            return "sunny";
        } else if (conditionText.contains("wind")) {
            return "windy";
        } else if (conditionText.contains("cloud") && conditionText.contains("sun")) {
            return "cloudy_sunny";
        } else if (conditionText.contains("cloud")) {
            return "cloudy";
        } else {
            String apiIconUrl = jsonObject.getJSONObject("current")
                    .getJSONObject("condition")
                    .getString("icon");
            return "https:" + apiIconUrl;
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
}