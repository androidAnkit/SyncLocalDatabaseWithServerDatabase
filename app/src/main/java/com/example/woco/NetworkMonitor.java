package com.example.woco;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.example.woco.data.Contract;
import com.example.woco.data.dbHelper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


class NetworkMonitor extends BroadcastReceiver {
    dbHelper mDbHelper;
    SQLiteDatabase database;
    String msg;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(checkInternetConnection(context)==true){
             mDbHelper=new dbHelper(context);
             database=mDbHelper.getWritableDatabase();
            Cursor cursor = mDbHelper.view_all_names();
            while(cursor.moveToNext()){
                int syncStatus=cursor.getInt(cursor.getColumnIndex(Contract.NamesEntry.SYNC_STATUS));
                if(syncStatus==Contract.SYNC_STATUS_FAILED){
                    String name = cursor.getString(cursor.getColumnIndex(Contract.NamesEntry.COLUMN_NAME));
                    String url =Contract.SERVER_BASE_URL+"?name="+name.toLowerCase();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                            String result= response.body().string();
                                                try {
                                                    if (new JSONObject(result).getString("status").equals("Success")) {
                                                        mDbHelper.updateDatabase(name, Contract.SYNC_STATUS_SUCCESS);
                                                        context.sendBroadcast(new Intent(Contract.UI_UPDATE_BROADCAST));
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                        }
                                    });
                }
            }
        }
    }

    public boolean checkInternetConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
