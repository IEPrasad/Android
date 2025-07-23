package com.s23010155.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
        
        final NavController navController = Navigation.findNavController(view);

        binding.cardVotePolicy.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_votePolicyFragment));
        binding.cardViewMap.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_mapViewFragment));
        binding.cardSubmitIssue.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_reportIssueFragment));
        binding.cardMyReports.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_myReportsFragment));
        binding.cardMonthlyPoll.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_monthlyPollFragment));
        binding.cardProfile.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_profileFragment));
        binding.cardSettings.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_settingsFragment));
        binding.cardLogout.setOnClickListener(v -> navController.navigate(R.id.action_dashboardFragment_to_logoutFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 