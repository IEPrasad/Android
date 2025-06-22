package com.s23010155.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.s23010155.R;
import com.s23010155.models.Issue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Issue> issueList;
    private final Context context;

    public ReportAdapter(Context context, List<Issue> issueList) {
        this.context = context;
        this.issueList = issueList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Issue issue = issueList.get(position);
        holder.bind(issue);
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, statusText, titleText, dateText;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.report_category_text);
            statusText = itemView.findViewById(R.id.report_status_text);
            titleText = itemView.findViewById(R.id.report_title_text);
            dateText = itemView.findViewById(R.id.report_date_text);
        }

        void bind(final Issue issue) {
            categoryText.setText(issue.getCategory());
            titleText.setText(issue.getTitle());
            statusText.setText(issue.getStatus());

            // Set status background color
            switch (issue.getStatus()) {
                case "Responded":
                    statusText.setBackground(ContextCompat.getDrawable(context, R.drawable.background_status_responded));
                    break;
                case "Closed":
                    statusText.setBackground(ContextCompat.getDrawable(context, R.drawable.background_status_closed));
                    break;
                case "Pending":
                default:
                    statusText.setBackground(ContextCompat.getDrawable(context, R.drawable.background_status_pending));
                    break;
            }

            // Format the date
            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = dbFormat.parse(issue.getCreatedAt());
                if (date != null) {
                    SimpleDateFormat appFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    dateText.setText(appFormat.format(date));
                }
            } catch (ParseException e) {
                dateText.setText(issue.getCreatedAt()); // Fallback to raw date
            }
        }
    }
} 