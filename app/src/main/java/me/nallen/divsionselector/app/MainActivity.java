package me.nallen.divsionselector.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements DataListener {
    private static final String PREFS_NAME = "me.nallen.divisionselector.app";

    private SharedPreferences mPrefs;
    private TcpClient tcpClient;

    String division;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ConnectActivity.ACTIVITY_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                String server_ip = data.getStringExtra("server_ip");

                SharedPreferences.Editor ed = mPrefs.edit();
                ed.putString("server_ip", server_ip);
                ed.commit();

                Toaster.doToast(getApplicationContext(), "Successfully Connected");

                showDivisionPicker();
            }
            else {
                finish();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tcpClient = TcpClient.getInstance();
        tcpClient.addDataListener(this);
        if(!tcpClient.isConnected()) {
            // We need to try connect
            mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            showConnectPage();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tcpClient.removeDataListener(this);
    }

    private void showDivisionPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Division");
        builder.setCancelable(false);

        final String[] options = new String[tcpClient.getAllDivisions().length + 1];
        int i = 0;
        for(String option : tcpClient.getAllDivisions()) {
            options[i++] = option;
        }
        options[i] = "All Divisions";

        builder.setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                division = options[item];

                showSelector();

                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showSelector() {
        setTitle(division);
    }

    private void showConnectPage() {
        String server_ip = mPrefs.getString("server_ip", null);

        Intent localIntent = new Intent(this, ConnectActivity.class);
        localIntent.putExtra("server_ip", server_ip);

        startActivityForResult(localIntent, ConnectActivity.ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void connectionDropped() {
        tcpClient.logout();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toaster.doToast(getApplicationContext(), "Connection dropped");

                showConnectPage();
            }
        });


    }

    private void logout() {
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.remove("server_ip");
        ed.commit();

        tcpClient.logout();

        Toaster.doToast(getApplicationContext(), "Logged Out");

        showConnectPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
