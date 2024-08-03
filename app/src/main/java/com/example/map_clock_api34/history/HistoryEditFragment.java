package com.example.map_clock_api34.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.HistoryDatabase.HistoryDatabaseHelper;
import androidx.lifecycle.ViewModelProvider;


public class HistoryEditFragment extends Fragment {

    private HistoryDatabaseHelper dbHelper;
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_fragment_history_edit, container, false);

        // Initialize AppDatabaseHelper and SharedViewModel
        dbHelper = new HistoryDatabaseHelper(requireContext()); // Pass only context here
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        Button clearButton = view.findViewById(R.id.SelectButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete all data?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Handle clearing operation
                            dbHelper.clearAllTables(); // Example of using dbHelper to clear tables
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // User chose to cancel, do nothing
                        })
                        .show();
            }
        });

        return view;
    }
}
