package me.nallen.divsionselector.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class DivisionFragment extends Fragment implements DataListener {
    private String division;
    private TcpClient tcpClient;

    private Spinner assignTeamSelector;
    private ArrayAdapter<String> assignTeamAdapter;
    private Button assignButton;

    private Spinner unassignTeamSelector;
    private ArrayAdapter<String> unassignTeamAdapter;
    private Button unassignButton;

    private Button scanButton;

    public static DivisionFragment newInstance(String division) {
        DivisionFragment fragment = new DivisionFragment();
        fragment.assignDivision(division);
        return fragment;
    }

    public DivisionFragment() {
        tcpClient = TcpClient.getInstance();
    }

    public void assignDivision(String division) {
        this.division = division;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        tcpClient.addDataListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tcpClient.removeDataListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_division, container, false);

        ((TextView) rootView.findViewById(R.id.division_title)).setText(division);

        assignTeamSelector = (Spinner) rootView.findViewById(R.id.assign_team_selector);
        assignTeamAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1);
        assignTeamSelector.setAdapter(assignTeamAdapter);

        assignButton = (Button) rootView.findViewById(R.id.assign_button);

        unassignTeamSelector = (Spinner) rootView.findViewById(R.id.unassign_team_selector);
        unassignTeamAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1);
        unassignTeamSelector.setAdapter(unassignTeamAdapter);

        unassignButton = (Button) rootView.findViewById(R.id.unassign_button);

        scanButton = (Button) rootView.findViewById(R.id.scan_button);

        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = assignTeamAdapter.getItem(assignTeamSelector.getSelectedItemPosition());
                tcpClient.requestAssignDivisionForTeam(number, division);
            }
        });

        unassignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = unassignTeamAdapter.getItem(unassignTeamSelector.getSelectedItemPosition());
                tcpClient.requestRemoveDivisionForTeam(number);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

                    startActivityForResult(intent, 0);
                }
                catch (Exception e) {
                    Toaster.doToast(getContext(), "QR Code Scanner not installed!");

                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                    startActivity(marketIntent);
                }
            }
        });

        updateOptions();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                final String team = data.getStringExtra("SCAN_RESULT");

                if(Arrays.asList(tcpClient.getAllUnassignedTeams()).contains(team)) {
                    Toaster.doToast(getContext(), "Adding team " + team + " to division " + division);
                    tcpClient.requestAssignDivisionForTeam(team, division);
                }
                else {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Error")
                            .setMessage("Team " + team + " has already been added to a division!")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        }
    }

    private <T> void updateAdapter(Spinner spinner, ArrayAdapter<T> adapter, T[] items) {
        adapter.clear();
        for(T item : items) {
            adapter.add(item);
        }
        adapter.notifyDataSetChanged();

        spinner.setEnabled(items.length > 0);
    }

    private void updateOptions() {
        if(assignTeamSelector != null) {
            updateAdapter(assignTeamSelector, assignTeamAdapter, tcpClient.getAllUnassignedTeams());

            assignButton.setEnabled(assignTeamSelector.isEnabled());

            updateAdapter(unassignTeamSelector, unassignTeamAdapter, tcpClient.getTeamsForDivision(division));

            unassignButton.setEnabled(unassignTeamSelector.isEnabled());

            scanButton.setEnabled(assignButton.isEnabled());
        }
    }

    @Override
    public void connectionDropped() {

    }

    @Override
    public void dataUpdated() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateOptions();
            }
        });
    }
}
