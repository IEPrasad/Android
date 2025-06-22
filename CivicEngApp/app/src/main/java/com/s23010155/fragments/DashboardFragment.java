package com.s23010155.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.s23010155.R;
import com.s23010155.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.buttonGoToVotePolicy.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_votePolicyFragment));
        binding.buttonGoToMapView.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_mapViewFragment));
        binding.buttonGoToReportIssue.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_reportIssueFragment));
        binding.buttonGoToMyReports.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_myReportsFragment));
        binding.buttonGoToMonthlyPoll.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_monthlyPollFragment));
        binding.buttonGoToProfile.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_profileFragment));
        binding.buttonGoToSettings.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_settingsFragment));
        binding.buttonGoToLogout.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_dashboardFragment_to_logoutFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 