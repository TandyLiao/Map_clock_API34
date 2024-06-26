package com.example.map_clock_api34.book;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.map_clock_api34.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateBookmark extends Fragment {

    private ListView listView1;
    private ListView listView2;
    private EditText editText;
    private ArrayAdapter<String> adapter1;
    private ArrayAdapter<String> adapter2;
    private SharedPreferences sharedPreferences;
    private boolean nextList = false;

    private static final String TAG = "CreateBookmark";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.book_create_bookmark, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        List<String> list1 = new ArrayList<>(sharedPreferences.getStringSet("list1", new HashSet<>()));
        List<String> list2 = new ArrayList<>(sharedPreferences.getStringSet("list2", new HashSet<>()));

        listView1 = view.findViewById(R.id.list_view1);
        listView2 = view.findViewById(R.id.list_view2);
        editText = view.findViewById(R.id.my_textbox);

        adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list1);
        adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list2);

        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);

        listView1.setOnItemLongClickListener((parent, view1, position, id) -> {
            String bookmarkName = list1.get(position);
            list1.remove(position);
            adapter1.notifyDataSetChanged();
            return true;
        });

        listView2.setOnItemLongClickListener((parent, view12, position, id) -> {
            String bookmarkName = list2.get(position);
            list2.remove(position);
            adapter2.notifyDataSetChanged();
            return true;
        });

        Button addRoute = view.findViewById(R.id.AddRoute);
        addRoute.setOnClickListener(view13 -> {
            Log.d(TAG, "Add Route button clicked");
            String input = editText.getText().toString();
            if (!input.equals("")) {
                if (!nextList) {
                    list1.add(input);
                    adapter1.notifyDataSetChanged();
                    editText.setText("");
                } else {
                    list2.add(input);
                    adapter2.notifyDataSetChanged();
                    editText.setText("");
                }
                nextList = !list1.isEmpty();
            }
        });

        Button setRoute = view.findViewById(R.id.setRoute);
        setRoute.setOnClickListener(view14 -> {
            Log.d(TAG, "Set Route button clicked");
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });

        Button confirmButton = view.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            Log.d(TAG, "Confirm button clicked");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> set1 = new HashSet<>(list1);
            Set<String> set2 = new HashSet<>(list2);
            editor.putStringSet("list1", set1);
            editor.putStringSet("list2", set2);
            editor.apply();

            Fragment targetFragment = getTargetFragment();
            if (targetFragment != null) {
                Intent dataToSendBack = new Intent();
                dataToSendBack.putStringArrayListExtra("list1", new ArrayList<>(list1));
                dataToSendBack.putStringArrayListExtra("list2", new ArrayList<>(list2));
                targetFragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, dataToSendBack);
            }
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(CreateBookmark.this);
            fragmentTransaction.commit();
        });

        Button cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "Cancel button clicked");
            Set<String> backup = sharedPreferences.getStringSet("list1_backup", new HashSet<>());
            if (backup.size() > 0) {
                list1.clear();
                list1.addAll(backup);
                adapter1.notifyDataSetChanged();
            }
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(CreateBookmark.this);
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() == null)
            return;

        CardView cardViewTitle = new CardView(getActivity());
        cardViewTitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.cardviewtitle_shape);
        cardViewTitle.setBackground(drawable);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView mark = new ImageView(getActivity());
        mark.setImageResource(R.drawable.clock);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        params.setMarginStart(10);
        mark.setLayoutParams(params);

        TextView bookTitle = new TextView(getActivity());
        bookTitle.setText("書籤");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 10, 10);

        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewTitle.addView(linearLayout);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(cardViewTitle, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END));
            actionBar.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setCustomView(null);
        }
    }
}
