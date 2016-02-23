package me.nallen.divsionselector.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends AppCompatActivity {
    public static final int ACTIVITY_REQUEST_CODE = 124;

    private ConnectTask connectTask = null;

    private EditText server_ip_box;
    private Button connect_button;

    private String server_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Connect");
        setContentView(R.layout.activity_connect);

        server_ip_box = (EditText) findViewById(R.id.input_server_ip);
        connect_button = (Button) findViewById(R.id.button_connect);

        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        boolean auto_login = true;

        if(getIntent().getStringExtra("server_ip") != null) {
            server_ip_box.setText(getIntent().getStringExtra("server_ip"));
        }
        else {
            auto_login = false;
        }

        // If we have data already then let's try automatically login
        if(auto_login) {
            login();
        }
    }

    private void login() {
        if(connectTask != null) {
            return;
        }

        // Get all the fields
        server_ip = server_ip_box.getText().toString();

        if(!Patterns.IP_ADDRESS.matcher(server_ip).matches()) {
            Toaster.doToast(getApplicationContext(), "Invalid Server IP entered");
            return;
        }

        connect_button.setEnabled(false);
        connectTask = new ConnectTask();
        connectTask.execute((Void) null);
    }

    public class ConnectTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            return TcpClient.getInstance().connect(server_ip);
        }

        @Override
        protected void onPostExecute(final Integer response) {
            connectTask = null;
            connect_button.setEnabled(true);

            if(response != TcpClient.CONNECT_OK) {
                if(response == TcpClient.CONNECT_ISSUE) {
                    Toaster.doToast(getApplicationContext(), "Unable to connect to Server at " + server_ip);
                }
                else {
                    Toaster.doToast(getApplicationContext(), "An unknown issue occured");
                }
                return;
            }

            Intent result = new Intent();
            result.putExtra("server_ip", server_ip);
            setResult(RESULT_OK, result);
            finish();
        }

        @Override
        protected void onCancelled() {
            connectTask = null;
            connect_button.setEnabled(true);
        }
    }
}
