package com.example.map_clock_api34.history;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.Database.AppDatabaseHelper;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;

import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    boolean isEdit, isDelete;

    View rootView;
    Button btnEdit, btnSelect, btnClearAll;

    RecyclerView recyclerViewHistory;
    ListAdapterHistory listAdapterHistory;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    private AppDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.history_fragment_history, container, false);

        setupActionBar();
        setupButtons();
        setupRecyclerViews();

        btnClearAll = rootView.findViewById(R.id.ClearAllButton);
        btnClearAll.setOnClickListener(v -> {
            AppDatabaseHelper dbHelper = new AppDatabaseHelper(getActivity());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM location");
            db.close();
            arrayList.clear();
            listAdapterHistory.notifyDataSetChanged();
            Toast.makeText(getActivity(), "已清除所有紀錄", Toast.LENGTH_SHORT).show();
        });

        return rootView;
    }

    private void setupButtons() {
        btnEdit = rootView.findViewById(R.id.EditButton);
        btnEdit.setOnClickListener(v -> {
            isEdit = !isEdit;
            isDelete = isEdit;
            updateButtonState();
            listAdapterHistory.setEditMode(isEdit);
            if (!isEdit) {
                clearSelections();
            }
        });

        btnSelect = rootView.findViewById(R.id.SelectButton);
        btnSelect.setOnClickListener(v -> {
            if (isDelete) {
                ArrayList<HashMap<String, String>> toRemove = new ArrayList<>();
                AppDatabaseHelper dbHelper = new AppDatabaseHelper(getActivity());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                for (HashMap<String, String> item : arrayList) {
                    if (item.getOrDefault("isSelected", "false").equals("true")) {
                        toRemove.add(item);
                        // 使用 SQL DELETE 語句刪除資料庫中的相應項目
                        String placeName = item.get("placeName");
                        db.execSQL("DELETE FROM history WHERE place_name = ?", new String[]{placeName});
                    }
                }
                db.close();
                arrayList.removeAll(toRemove);
                isEdit = false;
                isDelete = false;
                updateButtonState();
                listAdapterHistory.notifyDataSetChanged();
            } else {
                // Other action
            }
        });
    }

    private void setupRecyclerViews() {
        recyclerViewHistory = rootView.findViewById(R.id.recycleViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewHistory.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterHistory = new ListAdapterHistory(arrayList);
        recyclerViewHistory.setAdapter(listAdapterHistory);
    }

    private void RecycleViewReset() {
        arrayList.clear();
        addFromDB();
        listAdapterHistory.notifyDataSetChanged();
    }

    private void addFromDB() {
        String placeName;
        String lan;
        String lon;

        AppDatabaseHelper dbHelper = new AppDatabaseHelper(getActivity());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM history", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                placeName = cursor.getString(3);
                lan = cursor.getString(2);
                lon = cursor.getString(1);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("placeName", placeName);
                hashMap.put("latitude", lan);
                hashMap.put("longitude", lon);
                arrayList.add(hashMap);
            }
            cursor.close();
        }
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        isDelete = false;
        isEdit = false;
        RecycleViewReset();
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.lightgreen)));
        }

        CardView cardViewTitle = new CardView(requireContext());
        cardViewTitle.setLayoutParams(new CardView.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.cardviewtitle_shape);
        cardViewTitle.setBackground(drawable);

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView mark = new ImageView(requireContext());
        mark.setImageResource(R.drawable.history_record1);
        mark.setPadding(10, 10, 5, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                100, 100
        );
        params.setMarginStart(10);
        mark.setLayoutParams(params);

        TextView bookTitle = new TextView(requireContext());
        bookTitle.setText("歷史紀錄");
        bookTitle.setTextSize(15);
        bookTitle.setTextColor(getResources().getColor(R.color.green));
        bookTitle.setPadding(10, 10, 10, 10);

        linearLayout.addView(mark);
        linearLayout.addView(bookTitle);
        cardViewTitle.addView(linearLayout);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
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

    private void updateButtonState() {
        if (isEdit) {
            btnEdit.setText("確認");
            btnSelect.setText("刪除");
        } else {
            btnEdit.setText("編輯");
            btnSelect.setText("套用");
        }
    }

    private void clearSelections() {
        for (HashMap<String, String> item : arrayList) {
            item.put("isSelected", "false");
        }
        listAdapterHistory.notifyDataSetChanged();
    }
}
