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

public class MasterFragment extends Fragment {
    private TcpClient tcpClient;

    private Spinner assignTeamSelector;
    private ArrayAdapter<String> assignTeamAdapter;
    private Spinner assignDivisionSelector;
    private ArrayAdapter<String> assignDivisionAdapter;
    private Button assignButton;

    private Spinner unassignTeamSelector;
    private ArrayAdapter<String> unassignTeamAdapter;
    private Button unassignButton;

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


        assignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code
            }
        });

        unassignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code
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
        updateAdapter(assignTeamSelector, assignTeamAdapter, tcpClient.getAllUnassignedTeams());
        updateAdapter(assignDivisionSelector, assignDivisionAdapter, tcpClient.getAllDivisions());

        assignButton.setEnabled(assignTeamSelector.isEnabled() && assignDivisionSelector.isEnabled());

        updateAdapter(unassignTeamSelector, unassignTeamAdapter, tcpClient.getAllAssignedTeams());

        unassignButton.setEnabled(unassignTeamSelector.isEnabled());
    }
}
