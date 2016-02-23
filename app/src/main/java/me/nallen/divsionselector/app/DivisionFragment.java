package me.nallen.divsionselector.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DivisionFragment extends Fragment {
    private String division;
    private TcpClient tcpClient;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_division, container, false);



        return rootView;
    }
}
