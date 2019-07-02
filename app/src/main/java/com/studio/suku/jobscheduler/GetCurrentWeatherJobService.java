package com.studio.suku.jobscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class GetCurrentWeatherJobService extends JobService {

    public static final String TAG = GetCurrentWeatherJobService.class.getSimpleName();

    private static final String Key = "8b7635437c078245de62cd6d768cc41e";

    private static final String City = "Pasuruan";



    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() Executed");
        getCurrentWeather(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob() Executed");
        return true;
    }


    private void getCurrentWeather(final JobParameters job){
        //Get Some Data Here
        Log.d(TAG, "Running");
        //Initialize
        AsyncHttpClient client = new AsyncHttpClient();
        //Link Api
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+City+"&appid="+Key;
        Log.e(TAG, "getCurrentWeather: "+url );

        //Start Here
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //If Request Is Success

                //Convert To String For Response Body
                String result = new String(responseBody);
                try {
                    //This Is An Object from the responseBody
                    JSONObject object = new JSONObject(result);
                    //Parse The Value from object
                    String currentWeather = object.getJSONArray("weather").getJSONObject(0).getString("main");
                    String desc = object.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tmpKevin = object.getJSONObject("main").getDouble("temp");

                    //Covert Temperature From kelvin to Celcius

                    double tmpCelcius = tmpKevin - 273;

                    //Decimal Format and convert it to String
                    String temp = new DecimalFormat("##.##").format(tmpCelcius);

                    String title = "Current Weather";
                    String message = currentWeather +", "+desc+" with "+temp+" celcius";
                    int notifId = 100;

                    showNotification(getApplicationContext(), title, message, notifId);

                }
                catch (Exception e) {
                    jobFinished(job, true);
                    e.printStackTrace();
                }
            }



            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // ketika proses gagal, maka jobFinished diset dengan parameter true. Yang artinya job perlu di reschedule
                jobFinished(job, true);
            }
        });

    }

    private void showNotification(Context context, String title, String message, int notifId) {
        String CHANNEL_ID = "Channel_1";
        String CHANNEL_NAME = "Job scheduler channel";

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_replay_black_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.black))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            builder.setChannelId(CHANNEL_ID);
            if (notificationManagerCompat != null) {
                notificationManagerCompat.createNotificationChannel(channel);
            }
        }
        Notification notification = builder.build();
        if (notificationManagerCompat != null) {
            notificationManagerCompat.notify(notifId, notification);
        }
    }
}

