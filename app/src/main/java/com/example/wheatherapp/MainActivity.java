package com.example.wheatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);


        homeRL=findViewById(R.id.idRLHome);
        loadingPB=findViewById(R.id.idPBLoading);
        cityNameTV=findViewById(R.id.idTVCityName);
        temperatureTV=findViewById(R.id.idTVTemperature);
        conditionTV=findViewById(R.id.idTVCondition);
        weatherRV=findViewById(R.id.idRVWeather);
        cityEdt=findViewById(R.id.idEdtCity);
        backIV=findViewById(R.id.idIVBack);
        iconIV=findViewById(R.id.idIVICon);
        searchIV=findViewById(R.id.idIVSearch);

        weatherRVModelArrayList=new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city=cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });


    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please provide the permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude,double latitude){
        String cityName="Not found";
        Geocoder gcd = new Geocoder(getBaseContext(),Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);

            for(Address adr : addresses){
                if(adr!=null){
                    String city=adr.getLocality();
                    if(city!=null && !city.isEmpty()){
                        cityName=city;
                    }else{
                        Log.d("TAG","CITY NOT FOUND");
                        //Toast.makeText(this, "User city not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }


    private void getWeatherInfo(String cityName){
        String url= "https://api.weatherapi.com/v1/forecast.json?key=4b9bdb5519cc4fe7a92103115222906&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try{
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°c");
                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon= response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);

                    if(isDay==1){
                        Picasso.get().load("https://www.google.com/search?q=morning+shine+pic&sxsrf=ALiCzsY4Pzes8RgF3J1HuS43ZqdStcPfmA:1656527665026&tbm=isch&source=iu&ictx=1&vet=1&fir=MUHJx4WQZSEZNM%252CuJLo_Q7zJmB5_M%252C_%253BeQ8v-p6N3qAX2M%252CU1cMCoekAzDayM%252C_%253BdLRLzxNk16P6vM%252CY3FuNeTk9_AfUM%252C_%253Bq8TMIADCOmtcTM%252C8zuic1nGeppzFM%252C_%253BDtcKDEKwNEZzLM%252C2aihmIza-YoMYM%252C_%253BC3OvPoKn_GLbiM%252CuJLo_Q7zJmB5_M%252C_%253B-VL6MLeBghk3-M%252Cm7FS7RDtb9K3YM%252C_%253B41lpfwwuDZ-KlM%252Cxq-ZU2UotT2O6M%252C_%253B-PRiM0KkknZLFM%252C6gFClhGzUBnoPM%252C_%253B9zNAoISO7Q_fEM%252CuJLo_Q7zJmB5_M%252C_%253BbgarvSa_HR__GM%252Cxq-ZU2UotT2O6M%252C_%253BSI6PlfMNuW2MeM%252Ca5jsNZ7LHW4FCM%252C_%253BHTA7gGQDjYXONM%252CuJLo_Q7zJmB5_M%252C_%253BcxsFJlEp1kDwVM%252C8zuic1nGeppzFM%252C_%253BRTPMgEfHUAefcM%252CLEANmpesBHKzBM%252C_%253BtNlVuHUEooU34M%252CSGMdHqHd8CQFhM%252C_%253BzWafMPuIVI38UM%252CxCLCUtWu_BUuyM%252C_&usg=AI4_-kRPPn2DK_nsFQK0xSc2SEhRIoC_uA&sa=X&ved=2ahUKEwi69qK4ptP4AhXu6jgGHdGOAlEQ9QF6BAgHEAE#imgrc=UTiOofpkUv52AM").into(backIV);
                    }else{
                        Picasso.get().load("https://www.google.com/search?q=night+pic+sky&sxsrf=ALiCzsZnQ8yEhw0_qCS3pfV4nXZcofW0pg:1656527809975&tbm=isch&source=iu&ictx=1&vet=1&fir=yD3QP3yI_h-blM%252CeQ2aZrNiE_ZC_M%252C_%253BPay8OuTg5DKARM%252CfzhPC5r7rLcHfM%252C_%253BRfRQ2j8NjBKnMM%252CXsMyMLXkY7u7rM%252C_%253B39ieeRpkCaVTYM%252CeQ2aZrNiE_ZC_M%252C_%253BgqhLyYC_ukzykM%252CAHAZ4ewu9EYA-M%252C_%253B4Y0-QsgC0yLF7M%252CeQ2aZrNiE_ZC_M%252C_%253BG2ETAY0TCoeO9M%252CXsMyMLXkY7u7rM%252C_%253BPRCfZviuJ2p8sM%252CXsMyMLXkY7u7rM%252C_%253BLQIzFyQuxyH8GM%252CrDG2CY38lDUC8M%252C_%253BgdSM1b589vhEhM%252C1vhMlpTka8JkSM%252C_%253BJFsgUSt5d_pZiM%252CxaXfRgyhExQEVM%252C_%253BhJCtnjWiRzxakM%252C8gQl1s5rd01v9M%252C_%253Bfb2mk_TawtfsIM%252CFt_iLku6ZNjakM%252C_%253BIAKLOmmlPnr95M%252Cf48akJGQOY7MAM%252C_&usg=AI4_-kTcp6FEBBz6s-Qh7Iw9fwmrYMwY8A&sa=X&ved=2ahUKEwi357H9ptP4AhUS-zgGHeXqBsAQ9QF6BAgHEAE#imgrc=yD3QP3yI_h-blM").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forcastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray= forcastO.getJSONArray("hour");

                    for(int i=0;i<hourArray.length();i++){
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,img,wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Enter valid city name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);


    }
}