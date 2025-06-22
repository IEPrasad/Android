package com.s23010155.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.s23010155.R;
import com.s23010155.adapters.ReportAdapter;
import com.s23010155.database.IssueDatabaseHelper;
import com.s23010155.databinding.FragmentMyReportsBinding;
import com.s23010155.models.Issue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyReportsFragment extends Fragment {

    private FragmentMyReportsBinding binding;
    private ReportAdapter adapter;
    private List<Issue> issueList;
    private IssueDatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyReportsBinding.inflate(inflater, container, false);
        dbHelper = new IssueDatabaseHelper(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadReportsFromDb();
        
        binding.fabAddReport.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_myReportsFragment_to_reportIssueFragment));
    }

    private void setupRecyclerView() {
        issueList = new ArrayList<>();
        adapter = new ReportAdapter(requireContext(), issueList);
        binding.reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reportsRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void loadReportsFromDb() {
        issueList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                IssueDatabaseHelper.COLUMN_ID,
                IssueDatabaseHelper.COLUMN_CATEGORY,
                IssueDatabaseHelper.COLUMN_TITLE,
                IssueDatabaseHelper.COLUMN_DESCRIPTION,
                IssueDatabaseHelper.COLUMN_LOCATION_NAME,
                IssueDatabaseHelper.COLUMN_LATITUDE,
                IssueDatabaseHelper.COLUMN_LONGITUDE,
                IssueDatabaseHelper.COLUMN_PHOTO_URI,
                IssueDatabaseHelper.COLUMN_STATUS,
                IssueDatabaseHelper.COLUMN_CREATED_AT
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = IssueDatabaseHelper.COLUMN_CREATED_AT + " DESC";

        Cursor cursor = db.query(
                IssueDatabaseHelper.TABLE_ISSUES,   // The table to query
                projection,                         // The array of columns to return (pass null to get all)
                null,                      // The columns for the WHERE clause
                null,                   // The values for the WHERE clause
                null,                       // don't group the rows
                null,                        // don't filter by row groups
                sortOrder                           // The sort order
        );

        while (cursor.moveToNext()) {
            Issue issue = new Issue();
            issue.setId(cursor.getLong(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_ID)));
            issue.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_CATEGORY)));
            issue.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_TITLE)));
            issue.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_DESCRIPTION)));
            issue.setLocationName(cursor.getString(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_LOCATION_NAME)));
            issue.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_LATITUDE)));
            issue.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_LONGITUDE)));
            issue.setPhotoUri(cursor.getString(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_PHOTO_URI)));
            issue.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_STATUS)));
            issue.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(IssueDatabaseHelper.COLUMN_CREATED_AT)));
            issueList.add(issue);
        }
        cursor.close();

        if (issueList.isEmpty()) {
            binding.reportsRecyclerView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.reportsRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 