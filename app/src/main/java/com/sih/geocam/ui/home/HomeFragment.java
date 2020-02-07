package com.sih.geocam.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.sih.geocam.CamActivity;
import com.sih.geocam.FilesAdapter;
import com.sih.geocam.MainActivity;
import com.sih.geocam.R;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OrderType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ListView listView;
    private Storage storage;
    String FOLDER_PATH;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        storage = new Storage(getActivity().getApplicationContext());
        FOLDER_PATH = storage.getExternalStorageDirectory()+File.separator+"GeoCam";
        listView = root.findViewById(R.id.fileslist);
        if(storage.isDirectoryExists(FOLDER_PATH)){
            ArrayList<String> namelist = new ArrayList<>();
            List<File> files = storage.getFiles(FOLDER_PATH);
            for(int i = 0;i<files.size();i++){
                namelist.add(files.get(i).getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),R.layout.files_listview,namelist);
            listView.setAdapter(adapter);
            textView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
        final Button button = root.findViewById(R.id.recordbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CamActivity.class);
                startActivity(intent);
            }
        });
        return root;
    }
}