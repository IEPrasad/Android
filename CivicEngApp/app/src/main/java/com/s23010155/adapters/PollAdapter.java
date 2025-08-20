package com.s23010155.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.s23010155.R;
import com.s23010155.models.Poll;

import java.util.List;
import java.util.Locale;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollViewHolder> {

    private final List<Poll> pollList;
    private final OnPollVoteListener voteListener;
    private final Context context;

    public interface OnPollVoteListener {
        void onVote(Poll poll, int vote);
    }

    public PollAdapter(Context context, List<Poll> pollList, OnPollVoteListener voteListener) {
        this.context = context;
        this.pollList = pollList;
        this.voteListener = voteListener;
    }

    @NonNull
    @Override
    public PollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_poll, parent, false);
        return new PollViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PollViewHolder holder, int position) {
        Poll poll = pollList.get(position);
        holder.bind(poll, position);
    }

    @Override
    public int getItemCount() {
        return pollList.size();
    }

    class PollViewHolder extends RecyclerView.ViewHolder {
        TextView questionText, likesPercentage, dislikesPercentage, pollNumberText;
        ImageButton likeButton, dislikeButton;
        ProgressBar progressBar;

        public PollViewHolder(@NonNull View itemView) {
            super(itemView);
            pollNumberText = itemView.findViewById(R.id.poll_number_text);
            questionText = itemView.findViewById(R.id.poll_question_text);
            likesPercentage = itemView.findViewById(R.id.poll_likes_percentage);
            dislikesPercentage = itemView.findViewById(R.id.poll_dislikes_percentage);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
            progressBar = itemView.findViewById(R.id.poll_progress_bar);
        }

        void bind(final Poll poll, final int position) {
            pollNumberText.setText(String.format(Locale.getDefault(), "%d.", position + 1));
            questionText.setText(poll.getQuestion());

            int totalVotes = poll.getTotalVotes();
            if (totalVotes > 0) {
                int likesPercent = (int) (((double) poll.getLikes() / totalVotes) * 100);
                int dislikesPercent = 100 - likesPercent;
                likesPercentage.setText(String.format(Locale.getDefault(), "%d%% Liked", likesPercent));
                dislikesPercentage.setText(String.format(Locale.getDefault(), "%d%% Disliked", dislikesPercent));
                progressBar.setProgress(likesPercent);
            } else {
                likesPercentage.setText("0% Liked");
                dislikesPercentage.setText("0% Disliked");
                progressBar.setProgress(50);
            }

            updateButtonTints(poll.getUserVote());

            likeButton.setOnClickListener(v -> {
                if (poll.getUserVote() != 1) {
                    voteListener.onVote(poll, 1);
                }
            });

            dislikeButton.setOnClickListener(v -> {
                if (poll.getUserVote() != -1) {
                    voteListener.onVote(poll, -1);
                }
            });
        }

        void updateButtonTints(int userVote) {
            ColorStateList activeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.vote_policy_yellow_primary));
            ColorStateList inactiveColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dashboard_text_secondary));

            likeButton.setImageTintList(userVote == 1 ? activeColor : inactiveColor);
            dislikeButton.setImageTintList(userVote == -1 ? activeColor : inactiveColor);
        }
    }
} 