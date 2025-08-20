package com.s23010155.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.s23010155.R;
import com.s23010155.databinding.FragmentVotePolicyBinding;
import com.s23010155.database.PolicyVoteDatabaseHelper;

public class VotePolicyFragment extends Fragment {

    private FragmentVotePolicyBinding binding;
    private SharedPreferences sharedPreferences;
    private PolicyVoteDatabaseHelper policyDb;
    private String policyId = "policy_park_renovation";
    private static final String PREFS_NAME = "VotePolicyPrefs";
    private static final String KEY_HAS_VOTED = "hasVoted";
    private static final String KEY_VOTE_RESULT = "voteResult";

    private String currentSelection = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVotePolicyBinding.inflate(inflater, container, false);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        policyDb = new PolicyVoteDatabaseHelper(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkVoteStatus();
        updateTotals();

        binding.buttonAgree.setOnClickListener(v -> selectOption(binding.buttonAgree, "Agree"));
        binding.buttonDisagree.setOnClickListener(v -> selectOption(binding.buttonDisagree, "Disagree"));
        binding.buttonSubmit.setOnClickListener(v -> submitVote());
        binding.buttonBackToDashboard.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_votePolicyFragment_to_dashboardFragment));
    }

    private void checkVoteStatus() {
        boolean hasVoted = sharedPreferences.getBoolean(KEY_HAS_VOTED, false);
        if (hasVoted) {
            String voteResult = sharedPreferences.getString(KEY_VOTE_RESULT, "");
            showVotedState(voteResult);
        }
    }

    private void selectOption(MaterialButton selectedButton, String selection) {
        currentSelection = selection;

        // Reset both buttons to default outlined style
        updateButtonAppearance(binding.buttonAgree, false);
        updateButtonAppearance(binding.buttonDisagree, false);

        // Highlight the selected button
        updateButtonAppearance(selectedButton, true);

        binding.buttonSubmit.setVisibility(View.VISIBLE);
    }

    private void submitVote() {
        if (currentSelection != null) {
            SharedPreferences sp = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
            String nic = sp.getString("profileNic", null);
            if (nic == null) {
                Toast.makeText(getContext(), "Add NIC in Profile to vote", Toast.LENGTH_SHORT).show();
                return;
            }

            int vote = "Agree".equals(currentSelection) ? 1 : -1;
            policyDb.upsertVote(policyId, nic, vote);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_HAS_VOTED, true);
            editor.putString(KEY_VOTE_RESULT, currentSelection);
            editor.apply();

            Toast.makeText(getContext(), getString(R.string.vote_policy_toast_success), Toast.LENGTH_SHORT).show();
            showVotedState(currentSelection);
            updateTotals();
        }
    }

    private void updateTotals() {
        int[] totals = policyDb.getTotals(policyId);
        int agree = totals[0];
        int disagree = totals[1];
        int total = agree + disagree;
        if (total == 0) {
            binding.textVotedMessage.setText(getString(R.string.vote_policy_voted_message));
        } else {
            int agreePct = (int) ((agree * 100.0f) / total);
            int disagreePct = 100 - agreePct;
            binding.textVotedMessage.setText("Agree " + agreePct + "% • Disagree " + disagreePct + "% (" + total + " votes)");
        }
    }

    private void showVotedState(String voteResult) {
        binding.buttonAgree.setEnabled(false);
        binding.buttonDisagree.setEnabled(false);
        binding.buttonSubmit.setVisibility(View.GONE);
        binding.textVotedMessage.setVisibility(View.VISIBLE);

        // Reset both to unselected first
        updateButtonAppearance(binding.buttonAgree, false);
        updateButtonAppearance(binding.buttonDisagree, false);

        // Highlight the one they voted for
        if ("Agree".equals(voteResult)) {
            updateButtonAppearance(binding.buttonAgree, true);
        } else if ("Disagree".equals(voteResult)) {
            updateButtonAppearance(binding.buttonDisagree, true);
        }
    }

    private void updateButtonAppearance(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.vote_policy_yellow_primary));
            button.setTextColor(Color.BLACK);
            button.setIconTintResource(R.color.black);
        } else {
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(Color.WHITE);
            button.setIconTintResource(R.color.white);
            button.setStrokeColorResource(R.color.vote_policy_yellow_primary);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 