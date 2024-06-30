package com.example.map_clock_api34.book;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.map_clock_api34.R;
import com.example.map_clock_api34.home.SelectPlace;
import com.example.map_clock_api34.note.Note;

public class BookFragment extends Fragment {
    private Toolbar toolbar;
    private ImageView addbook_imageView;
    private ImageView usebook_imageView;
    private ImageView setbook_imageView;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_fragment_book, container, false);

        // Initialize ImageViews
        addbook_imageView = view.findViewById(R.id.bookadd_imageView);
        usebook_imageView = view.findViewById(R.id.bookuse_imageView);
        setbook_imageView = view.findViewById(R.id.bookset_imageView);

        // Set click listeners for ImageViews
        addbook_imageView.setOnClickListener(v -> {
            CreateBook createbook = new CreateBook();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createbook);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        usebook_imageView.setOnClickListener(v -> {
            // Handle Use Bookmark click
        });

        setbook_imageView.setOnClickListener(v -> {
            // Handle Set Bookmark click
        });

        // Initialize DrawerLayout
        if (getActivity() != null) {
            drawerLayout = getActivity().findViewById(R.id.drawerLayout);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = requireActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            // Ensure drawerLayout is not null
            if (drawerLayout != null) {
                // Set up ActionBarDrawerToggle
                if (toggle == null) {
                    toggle = new ActionBarDrawerToggle(
                            requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
                    drawerLayout.addDrawerListener(toggle);
                    toggle.syncState();
                }
                toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));

                // Create CardView and add it to the ActionBar
                CardView cardViewtitle = new CardView(requireContext());
                cardViewtitle.setLayoutParams(new CardView.LayoutParams(
                        ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT));
                Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
                cardViewtitle.setBackground(drawable);

                // Create LinearLayout inside CardView
                LinearLayout linearLayout = new LinearLayout(requireContext());
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                // Create ImageView
                ImageView mark = new ImageView(requireContext());
                mark.setImageResource(R.drawable.bookmark1);
                mark.setPadding(10, 10, 5, 10);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        100, // Width in pixels
                        100 // Height in pixels
                );
                params.setMarginStart(10); // Set left margin
                mark.setLayoutParams(params);

                // Create TextView
                TextView bookTitle = new TextView(requireContext());
                bookTitle.setText("書籤");
                bookTitle.setTextSize(15);
                bookTitle.setTextColor(getResources().getColor(R.color.green)); // Change text color
                bookTitle.setPadding(10, 10, 10, 10); // Set padding

                // Add ImageView and TextView to LinearLayout
                linearLayout.addView(mark);
                linearLayout.addView(bookTitle);
                cardViewtitle.addView(linearLayout);

                // Set custom view to ActionBar
                actionBar.setDisplayShowTitleEnabled(false); // Hide default title
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setCustomView(cardViewtitle, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT, // Width as WRAP_CONTENT
                        ActionBar.LayoutParams.WRAP_CONTENT, // Height as WRAP_CONTENT
                        Gravity.END)); // Align to the end

                actionBar.show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
            actionBar.setDisplayShowTitleEnabled(true); // Restore title display
        }
    }
}
