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
import com.s23010155.models.Poll;

import java.util.ArrayList;
import java.util.List;

public class MonthlyPollFragment extends Fragment implements PollAdapter.OnPollVoteListener {

    private FragmentMonthlyPollBinding binding;
    private PollAdapter adapter;
    private List<Poll> pollList;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "PollPrefs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMonthlyPollBinding.inflate(inflater, container, false);
        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadPolls();
    }

    private void setupRecyclerView() {
        pollList = new ArrayList<>();
        adapter = new PollAdapter(requireContext(), pollList, this);
        binding.pollsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.pollsRecyclerView.setAdapter(adapter);
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
        adapter.notifyDataSetChanged();
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