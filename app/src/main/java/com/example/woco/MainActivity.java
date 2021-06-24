package com.example.woco;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woco.data.Contract;
import com.example.woco.data.dbHelper;
import com.example.woco.Utilities.AppSharedPref;

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

import static android.view.View.VISIBLE;
import static com.example.woco.data.Contract.NamesEntry.COLUMN_NAME;
import static com.example.woco.data.Contract.NamesEntry.SYNC_STATUS;


public class MainActivity extends AppCompatActivity implements wocoNamesAdapter.nameClicked {

   // ListView names;
    TextView add;
    TextView delete;
    int position;
    String selected_name;
    int sync_details;
    private dbHelper mdbHelper;
    //ArrayList<String> namesList;
    String [] names_array;
    String msg="";
    ArrayList<wocoNames> namesList= new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    wocoNamesAdapter namesAdapter;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(new NetworkMonitor(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        initialUI();
        namesList= new ArrayList<>();
        woco_set_adapter();


        mdbHelper=new dbHelper(this);

        String flag = AppSharedPref.instance.getFirstTime(this);
        if(checkInternetConnection() && flag=="true") {
            fetch_data(Contract.SERVER_BASE_URL+"?fetch=1");
            AppSharedPref.instance.setFirstTime(this, "false");
            Toast.makeText(getApplicationContext()," Data fetched from the server",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext()," Data fetched from the local database",Toast.LENGTH_SHORT).show();
            displayingNames();
        }

        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                displayingNames();
            }
        };

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_name_dialog(v);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Position: " + position + "Selected Name: " + selected_name);
                if(selected_name == null) {
                    Toast.makeText(getApplicationContext(),"Select name to be deleted",Toast.LENGTH_SHORT).show();
                }else{
                    delete_name_dialog(v, selected_name);
                }
            }
        });

        registerReceiver(broadcastReceiver,new IntentFilter(Contract.UI_UPDATE_BROADCAST));

    }

    public void fetch_data(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Failed to connect");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // This code runs does not runs on the UI thread
                String result= response.body().string();
                try {
                    if (new JSONObject(result).getString("status").equals("Success")){
                        String names_data= new JSONObject(result).getString("get_data");
                        names_array = names_data.split("@!");
                        for(int i=0;i<names_array.length;i++) {
                            mdbHelper.addName(names_array[i],Contract.SYNC_STATUS_SUCCESS);
                            namesList.add(new wocoNames(convert(names_array[i]), Contract.SYNC_STATUS_SUCCESS));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // This code will run in the UI thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Now here set values in adapter
                        woco_set_adapter();
                        namesAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void check_name(String name){
        String url =Contract.SERVER_BASE_URL+"?check_name="+name.toLowerCase();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Failed to connect");
          }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // This code runs does not runs on the UI thread
                String result= response.body().string();
                namesList=new ArrayList<wocoNames>();
                try {
                    if (new JSONObject(result).getString("status").equals("Success")){
                      msg="Name already exists add other name";
                    }else if (new JSONObject(result).getString("status").equals("Failed")){
                        insert_names(name);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // This code will run in the UI thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Now here add values in arrayList
                        if(msg!=null) {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            msg="";
                        }
                    }
                });
            }
        });
    }

    public void insert_names(String name){
        String url =Contract.SERVER_BASE_URL+"?name="+name.toLowerCase();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Failed to connect");
                mdbHelper.addName(name.toLowerCase(),Contract.SYNC_STATUS_FAILED);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // This code runs does not runs on the UI thread
                String result= response.body().string();
                try {
                    if (new JSONObject(result).getString("status").equals("Success")) {
                        msg = "The name is added successfully";
                    }else {
                        mdbHelper.addName(name.toLowerCase(),Contract.SYNC_STATUS_FAILED);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // This code will run in the UI thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(msg!=null){
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            msg="";
                        }
                        woco_set_adapter();
                        namesAdapter.notifyDataSetChanged();
                        displayingNames();
                    }
                });
            }
        });
    }


    public void delete_name(String name){
        String url =Contract.SERVER_BASE_URL+"?delete_name="+name.toLowerCase();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Failed to connect");

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // This code runs does not runs on the UI thread
                String result= response.body().string();
                try {
                    if (new JSONObject(result).getString("status").equals("Success")) {
                        msg="The name " + name + " is deleted successfully";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // This code will run in the UI thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(msg!=null){
                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT ).show();
                            msg="";
                        }
                        woco_set_adapter();
                        displayingNames();
                        namesAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initialUI(){
        add=findViewById(R.id.add);
        delete=findViewById(R.id.delete);
        recyclerView = findViewById(R.id.name_recyclerView);
    }

    public boolean checkInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private String convert(String name){
        String first,rem;
        String conv="";
        name.trim();

            first = name.substring(0,1).toUpperCase();
            rem = name.substring(1).toLowerCase();
            conv = first+rem;

        return (conv.trim());
    }

    public void add_name_dialog(View view){
        final EditText entryForName = new EditText(view.getContext());
        final AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext(),R.style.dialog_custom);
        dialog.setTitle("Add Name");
        dialog.setMessage("Enter name you want to add");
        dialog.setView(entryForName);

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String enterName = entryForName.getText().toString().trim().toLowerCase();

                // Check if the name is already listed or no name entered
                if(enterName==null || mdbHelper.findName(enterName)!=null){
                    Toast.makeText(getApplicationContext(),"The name is either empty or is already listed", Toast.LENGTH_SHORT).show();
                }else if(checkInternetConnection()){
                    // Checking if the value is in our server our not
                    check_name(enterName);
                    // If entered name is not empty or not in the list then add name
                    boolean result = mdbHelper.addName(enterName,Contract.SYNC_STATUS_SUCCESS);
                    if(result){
                        displayingNames();
                    }
                }else if(!checkInternetConnection()) {
                    // If entered name is not empty or not in the list then add name
                    boolean result = mdbHelper.addName(enterName, Contract.SYNC_STATUS_FAILED);
                    if (result) {
                        Toast.makeText(getApplicationContext(), "The name is added successfully", Toast.LENGTH_SHORT).show();
                        displayingNames();
                    }
                }
                displayingNames();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void displayingNames() {
        namesList.clear();
        Cursor cursor = mdbHelper.view_all_names();
        namesList =new ArrayList<wocoNames>();
        while (cursor.moveToNext()) {
            String name=cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            int sync_status=cursor.getInt(cursor.getColumnIndex(SYNC_STATUS));
            namesList.add(new wocoNames(convert(name),sync_status));
            woco_set_adapter();
        }
        namesAdapter.notifyDataSetChanged();
        cursor.close();
        mdbHelper.close();

    }

    public void delete_name_dialog(View view, String selected_name){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext(),R.style.dialog_custom);
        dialog.setTitle("Delete Name");
        dialog.setMessage("Are you sure you want to delete name " + selected_name + " from the list");

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!checkInternetConnection() && sync_details == Contract.SYNC_STATUS_FAILED ){
                    mdbHelper.removeSelectedName(selected_name.toLowerCase());
                    Toast.makeText(getApplicationContext(),"The name " + selected_name + " is deleted successfully",Toast.LENGTH_SHORT).show();
                    displayingNames();
                }else if(checkInternetConnection()) {
                    delete_name(selected_name);
                    mdbHelper.removeSelectedName(selected_name.toLowerCase());
                  //  int rowsDeleted = getContentResolver().delete(Contract.NamesEntry.CONTENT_URI, selected_name.toLowerCase(), null);
                    Toast.makeText(getApplicationContext(),"The name " + selected_name + " is deleted successfully",Toast.LENGTH_SHORT).show();
                    displayingNames();
                }else{
                    Toast.makeText(getApplicationContext(),"Check Internet Connection",Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void name_onClick(int position) {
        wocoNames nameSelected = namesList.get(position);
        selected_name = nameSelected.getName();
        sync_details = nameSelected.getSyncStatus();
        Toast.makeText(getApplicationContext(),"Hi There "+selected_name, Toast.LENGTH_SHORT).show();
        delete.setVisibility(VISIBLE);
    }

    public void woco_set_adapter(){
        layoutManager= new LinearLayoutManager(getBaseContext());
        namesAdapter=new wocoNamesAdapter(getApplicationContext(),namesList,this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(namesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver,new IntentFilter(Contract.UI_UPDATE_BROADCAST));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,new IntentFilter(Contract.UI_UPDATE_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}