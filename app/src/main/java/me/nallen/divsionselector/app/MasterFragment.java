package me.nallen.divsionselector.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class MasterFragment extends Fragment implements DataListener {
    private TcpClient tcpClient;

    private Spinner assignTeamSelector;
    private ArrayAdapter<String> assignTeamAdapter;
    private Spinner assignDivisionSelector;
    private ArrayAdapter<String> assignDivisionAdapter;
    private Button assignButton;

    private Spinner unassignTeamSelector;
    private ArrayAdapter<String> unassignTeamAdapter;
    private Button unassignButton;

    private Button randomiseButton;

    public static MasterFragment newInstance() {
        MasterFragment fragment = new MasterFragment();
        return fragment;
    }

    public MasterFragment() {
        tcpClient = TcpClient.getInstance();
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
        View rootView = inflater.inflate(R.layout.fragment_master, container, false);

        assignTeamSelector = (Spinner) rootView.findViewById(R.id.assign_team_selector);
        assignTeamAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1);
        assignTeamSelector.setAdapter(assignTeamAdapter);

        assignDivisionSelector = (Spinner) rootView.findViewById(R.id.assign_division_selector);
        assignDivisionAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1);
        assignDivisionSelector.setAdapter(assignDivisionAdapter);

        assignButton = (Button) rootView.findViewById(R.id.assign_button);


        unassignTeamSelector = (Spinner) rootView.findViewById(R.id.unassign_team_selector);
        unassignTeamAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1);
        unassignTeamSelector.setAdapter(unassignTeamAdapter);

        unassignButton = (Button) rootView.findViewById(R.id.unassign_button);


        randomiseButton = (Button) rootView.findViewById(R.id.randomise_button);


        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = assignTeamAdapter.getItem(assignTeamSelector.getSelectedItemPosition());
                String division = assignDivisionAdapter.getItem(assignDivisionSelector.getSelectedItemPosition());
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

        randomiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tcpClient.requestRandomiseRemainingTeams();
            }
        });

        updateOptions();

        return rootView;
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
            updateAdapter(assignDivisionSelector, assignDivisionAdapter, tcpClient.getAllDivisions());

            assignButton.setEnabled(assignTeamSelector.isEnabled() && assignDivisionSelector.isEnabled());

            updateAdapter(unassignTeamSelector, unassignTeamAdapter, tcpClient.getAllAssignedTeams());

            unassignButton.setEnabled(unassignTeamSelector.isEnabled());

            randomiseButton.setEnabled(assignButton.isEnabled());
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
