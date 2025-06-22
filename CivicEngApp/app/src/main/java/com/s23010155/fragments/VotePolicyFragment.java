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
import com.s23010155.databinding.FragmentVotePolicyBinding;
public class VotePolicyFragment extends Fragment {
    private FragmentVotePolicyBinding binding;
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup c, Bundle s) {
        binding = FragmentVotePolicyBinding.inflate(inflater, c, false);
        return binding.getRoot();
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonBackToDashboard.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_votePolicyFragment_to_dashboardFragment));
    }
} 