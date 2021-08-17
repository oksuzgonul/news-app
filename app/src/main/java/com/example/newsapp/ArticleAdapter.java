package com.example.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ArticleAdapter extends ArrayAdapter<Article> {
    public ArticleAdapter(@NonNull Context context, List<Article> articles) {
        super(context, 0, articles);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View articleListView = convertView;
        if (articleListView == null) {
            articleListView = LayoutInflater.from(getContext()).inflate(
                    R.layout.article_list_item, parent, false);
        }

        final Article currentArticle = getItem(position);

        TextView titleView = articleListView.findViewById(R.id.title);
        String titleText = currentArticle.getTitle();
        titleView.setText(titleText);

        TextView sectionView = articleListView.findViewById(R.id.section_name);
        String sectionText = currentArticle.getSectionName();
        sectionView.setText(sectionText);

        TextView authorView = articleListView.findViewById(R.id.author);
        if (currentArticle.hasAuthor()) {
            String authorText = currentArticle.getAuthor();
            authorView.setText(authorText);
        } else {
            authorView.setVisibility(View.GONE);
        }

        TextView dateView = articleListView.findViewById(R.id.date_published);
        if (currentArticle.hasDate()) {
            String date = currentArticle.getDatePublished();
            dateView.setText(date);
        } else {
            dateView.setVisibility(View.GONE);
        }

        Button readButton = articleListView.findViewById(R.id.read_button);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri clickUri = Uri.parse(currentArticle.getUrl());
                Intent webSiteIntent = new Intent(Intent.ACTION_VIEW, clickUri);
                getContext().startActivity(webSiteIntent);
            }
        });


        return articleListView;
    }
}
