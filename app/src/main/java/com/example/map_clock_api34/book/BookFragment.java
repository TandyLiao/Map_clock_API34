package com.example.map_clock_api34.book;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.Database.AppDatabaseHelper;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.Database.AppDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class BookFragment extends Fragment {

    private Toolbar toolbar;
    private ImageView createbook_imageView;
    private ImageView setbook_imageView;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;

    private View rootView;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    RecyclerView recyclerViewBook;
    ListAdapterHistory listAdapterBook;

    private BookDatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.book_fragment_book, container, false);
        dbHelper = new BookDatabaseHelper(requireContext());

        setupActionBar();

        createbook_imageView = rootView.findViewById(R.id.bookcreate_imageView);
        setbook_imageView = rootView.findViewById(R.id.bookset_imageView);

        // Set click listeners for ImageViews
        createbook_imageView.setOnClickListener(v -> {
            CreateBook createbook = new CreateBook();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createbook);
            transaction.addToBackStack(null);
            transaction.commit();

        });


        setbook_imageView.setOnClickListener(v -> {
            CreateBook createbook = new CreateBook();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createbook);
            transaction.addToBackStack(null);
            transaction.commit();
        });


        if (getActivity() != null) {
            drawerLayout = getActivity().findViewById(R.id.drawerLayout);
            toolbar = requireActivity().findViewById(R.id.toolbar);
        }
        dbHelper = new BookDatabaseHelper(requireContext());
        setupRecyclerViews();

        addFromDB();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupActionBar();
    }

    private void setupActionBar() {
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

    private void setupRecyclerViews() {
        recyclerViewBook = rootView.findViewById(R.id.recycleView_book);
        recyclerViewBook.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewBook.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterBook = new ListAdapterHistory(arrayList);
        //檢測是否有選擇RecycleView的監聽器
        listAdapterBook.setOnItemSelectedListener(this::updateButtonState);
        recyclerViewBook.setAdapter(listAdapterBook);
    }

    private void updateButtonState() {
        // 根據選擇狀態更新按鈕狀態
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
        saveInShareviewModel();
    }

    private void addFromDB() {

        String time;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history WHERE arranged_id=0", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String placeNameTemp = cursor.getString(3);
                //找到"->"的位置
                int index = placeNameTemp.indexOf("->");
                //把"->"前的資料抓出來
                String beforeArrow = placeNameTemp.substring(0,index);
                if(beforeArrow.length()>20){
                    beforeArrow=beforeArrow.substring(0,20)+"...";
                }
                //把"->"後的資料抓出來
                String afterArrow = placeNameTemp.substring(index+2);
                if(afterArrow.length()>20){
                    afterArrow=afterArrow.substring(0,20)+"...";
                }

                time = cursor.getString(1);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("placeName", beforeArrow);
                hashMap.put("placeName2", "\u2193");
                hashMap.put("placeName3", afterArrow);
                hashMap.put("time", time);
                arrayList.add(hashMap);
            }
            cursor.close();
        }
        db.close();
    }

    private void saveInShareviewModel() {
        String time = "";
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                time = item.get("time");
            }
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + BookDatabaseHelper.BookTable.TABLE_NAME + " WHERE " + BookDatabaseHelper.BookTable.COLUMN_START_TIME + " = ?", new String[]{time});
        db.beginTransaction();
        try {
            while (cursor.moveToNext()) {
                String locationId = cursor.getString(0);
                Cursor locationCursor = db.rawQuery("SELECT * FROM " + BookDatabaseHelper.LocationTable2.TABLE_NAME + " WHERE " + BookDatabaseHelper.LocationTable2.COLUMN_LOCATION_ID + " = ?", new String[]{locationId});
                if (locationCursor.moveToFirst()) {
                    String placeName = locationCursor.getString(3);
                    Double latitude = locationCursor.getDouble(2);
                    Double longitude = locationCursor.getDouble(1);
                    String city = locationCursor.getString(5);
                    String area = locationCursor.getString(6);
                }
                locationCursor.close(); // Ensure the cursor is closed to avoid memory leaks
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("BookFragment", "Error while trying to save in ShareViewModel", e);
        } finally {
            db.endTransaction();
            cursor.close(); // Ensure the cursor is closed to avoid memory leaks
        }
    }
}