package com.s23010155.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.s23010155.adapters.PollAdapter;
import com.s23010155.databinding.FragmentMonthlyPollBinding;
import com.s23010155.database.AuthDatabaseHelper;
import com.s23010155.database.PollDatabaseHelper;
import com.s23010155.models.Poll;

import java.util.ArrayList;
import java.util.List;

public class MonthlyPollFragment extends Fragment implements PollAdapter.OnPollVoteListener {

    private FragmentMonthlyPollBinding binding;
    private PollAdapter adapter;
    private List<Poll> pollList;
    private SharedPreferences prefs;
    private PollDatabaseHelper pollDb;
    private AuthDatabaseHelper authDb;

    private static final String PREFS_NAME = "PollPrefs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMonthlyPollBinding.inflate(inflater, container, false);
        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        pollDb = new PollDatabaseHelper(requireContext());
        authDb = new AuthDatabaseHelper(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupGovPerformanceVoting();
        loadPolls();

        binding.fabAddPoll.setOnClickListener(v -> showCreatePollDialog());
    }

    private void setupRecyclerView() {
        pollList = new ArrayList<>();
        adapter = new PollAdapter(requireContext(), pollList, this);
        binding.pollsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pollsRecyclerView.setAdapter(adapter);
    }

    // removed duplicate old loadPolls

    private void showCreatePollDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("Enter poll question");
        input.setTextColor(getResources().getColor(R.color.black));
        input.setHintTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dashboard_text_secondary));
        input.setPadding(32, 32, 32, 32);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Create New Poll")
                .setView(input)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Create", (d, w) -> {
                    String q = input.getText().toString().trim();
                    if (q.isEmpty()) return;
                    // Persist in SharedPreferences as a simple list
                    java.util.Set<String> set = prefs.getStringSet("custom_polls", new java.util.HashSet<>());
                    java.util.Set<String> copy = new java.util.HashSet<>(set);
                    String id = "poll_custom_" + System.currentTimeMillis();
                    // store as id||question
                    copy.add(id + "||" + q);
                    prefs.edit().putStringSet("custom_polls", copy).apply();
                    loadPolls();
                })
                .show();
    }

    private void loadPolls() {
        pollList.clear();
        // Sample poll data
        String[] questions = {
                "Should the city invest more in public parks?",
                "Is the current waste management system effective?",
                "Do we need stricter regulations for new construction projects?",
                "Should there be more public libraries in the suburbs?",
                "Are the current electricity tariffs fair?",
                "Do you support the proposed expansion of the main highway?"
        };

        for (int i = 0; i < questions.length; i++) {
            String pollId = "poll_" + i;
            int likes = prefs.getInt(pollId + "_likes", 0);
            int dislikes = prefs.getInt(pollId + "_dislikes", 0);
            Poll poll = new Poll(pollId, questions[i], likes, dislikes);
            poll.setUserVote(prefs.getInt(pollId + "_uservote", 0));
            pollList.add(poll);
        }

        // Load custom polls
        java.util.Set<String> set = prefs.getStringSet("custom_polls", new java.util.HashSet<>());
        for (String entry : set) {
            String[] parts = entry.split("\\|\\|", 2);
            if (parts.length != 2) continue;
            String pollId = parts[0];
            String question = parts[1];
            int likes = prefs.getInt(pollId + "_likes", 0);
            int dislikes = prefs.getInt(pollId + "_dislikes", 0);
            Poll poll = new Poll(pollId, question, likes, dislikes);
            poll.setUserVote(prefs.getInt(pollId + "_uservote", 0));
            pollList.add(poll);
        }
        adapter.notifyDataSetChanged();
    }

    private void setupGovPerformanceVoting() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1; // 1-12

        String monthName = new java.text.DateFormatSymbols().getMonths()[month - 1];
        binding.textCurrentMonth.setText(monthName + " " + year);

        // Load current totals
        updateMonthTotals(year, month);

        // Get NIC of logged in user if available (from shared prefs or auth DB). For now, ask for NIC from User profile storage.
        SharedPreferences sp = requireActivity().getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE);
        String nic = sp.getString("profileNic", null);
        String district = sp.getString("profileDistrict", null);

        int existing = nic == null ? 0 : pollDb.getUserVote(nic, year, month);
        reflectUserVote(existing);

        binding.buttonVoteGood.setOnClickListener(v -> {
            if (nic == null) { showNicMissing(); return; }
            pollDb.upsertGovPerfVote(nic, year, month, 1, district);
            reflectUserVote(1);
            updateMonthTotals(year, month);
        });

        binding.buttonVoteBad.setOnClickListener(v -> {
            if (nic == null) { showNicMissing(); return; }
            pollDb.upsertGovPerfVote(nic, year, month, -1, district);
            reflectUserVote(-1);
            updateMonthTotals(year, month);
        });
    }

    private void reflectUserVote(int vote) {
        int active = requireContext().getColor(R.color.vote_policy_yellow_primary);
        int inactive = requireContext().getColor(R.color.dashboard_text_secondary);
        binding.buttonVoteGood.setTextColor(vote == 1 ? active : inactive);
        binding.buttonVoteBad.setTextColor(vote == -1 ? active : inactive);
    }

    private void updateMonthTotals(int year, int month) {
        int[] totals = pollDb.getMonthTotals(year, month);
        int likes = totals[0];
        int dislikes = totals[1];
        int total = likes + dislikes;
        if (total == 0) {
            binding.textMonthResult.setText("No votes yet");
        } else {
            int likePct = (int) ((likes * 100.0f) / total);
            int dislikePct = 100 - likePct;
            binding.textMonthResult.setText("Good " + likePct + "% • Bad " + dislikePct + "% (" + total + " votes)");
        }
    }

    private void showNicMissing() {
        android.widget.Toast.makeText(requireContext(), "Add NIC in Profile to vote", android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVote(Poll poll, int newVote) {
        SharedPreferences.Editor editor = prefs.edit();

        int currentVote = poll.getUserVote();
        int likes = poll.getLikes();
        int dislikes = poll.getDislikes();

        // Revert the previous vote if it exists
        if (currentVote == 1) { // Was a like
            likes--;
        } else if (currentVote == -1) { // Was a dislike
            dislikes--;
        }

        // Apply the new vote
        if (newVote == 1) { // New vote is a like
            likes++;
        } else if (newVote == -1) { // New vote is a dislike
            dislikes++;
        }

        poll.setLikes(likes);
        poll.setDislikes(dislikes);
        poll.setUserVote(newVote);

        // Save new values to SharedPreferences
        editor.putInt(poll.getId() + "_likes", poll.getLikes());
        editor.putInt(poll.getId() + "_dislikes", poll.getDislikes());
        editor.putInt(poll.getId() + "_uservote", newVote);
        editor.apply();

        // Update the adapter
        adapter.notifyItemChanged(pollList.indexOf(poll));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 