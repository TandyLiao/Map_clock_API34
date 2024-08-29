package com.example.map_clock_api34.book;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.book.RecycleViewActionBook.SwipeToDeleteCallback;
import com.example.map_clock_api34.history.ListAdapter.ListAdapterHistory;
import com.example.map_clock_api34.book.BookDatabaseHelper;
import com.example.map_clock_api34.book.BookDatabaseHelper.BookTable;
import com.example.map_clock_api34.book.BookDatabaseHelper.LocationTable2;
import com.example.map_clock_api34.home.HomeFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.recyclerview.widget.ItemTouchHelper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class BookFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Toolbar toolbar;
    private ImageView createbook_imageView;
    private ImageView setbook_imageView;
    private Button user_sure;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    SharedViewModel sharedViewModel;

    private View rootView;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    RecyclerView recyclerViewBook;
    ListAdapterHistory listAdapterBook;

    private BookDatabaseHelper dbHelper;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.book_fragment_book, container, false);

        dbHelper = new BookDatabaseHelper(requireContext());

        setupActionBar();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setButton();

        if (getActivity() != null) {
            drawerLayout = getActivity().findViewById(R.id.drawerLayout);
            toolbar = requireActivity().findViewById(R.id.toolbar);
        }

        dbHelper = new BookDatabaseHelper(requireContext());
        setupRecyclerViews();

        addFromDB();
        //dbHelper.clearAllTables();
        return rootView;
    }

    private void setButton() {

        createbook_imageView = rootView.findViewById(R.id.bookcreate_imageView);
        setbook_imageView = rootView.findViewById(R.id.bookset_imageView);
        user_sure = rootView.findViewById(R.id.book_usesure);
        // 禁用 setbook_imageView，直到選擇了一個項目
        setbook_imageView.setEnabled(false);
        // Set click listeners for ImageViews
        createbook_imageView.setOnClickListener(v -> {
            sharedViewModel.clearAll();
            Log.d("BookFragment", "createbook_imageView clicked");
            FakeCreateLocation createFbook = new FakeCreateLocation();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createFbook);
            transaction.addToBackStack(null);
            transaction.commit();

        });

        setbook_imageView.setOnClickListener(v -> {

            HashMap<String, String> selectedItem = listAdapterBook.getSelectedItem();

            if (selectedItem != null) {
                sharedViewModel.routeName = selectedItem.get("placeName2");
                sharedViewModel.time = selectedItem.get("time");
                sharedViewModel.clearAll();
                String time = sharedViewModel.time;

                int count = 0;
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM " + BookTable.TABLE_NAME + " WHERE " + BookTable.COLUMN_START_TIME + " = ?", new String[]{time});

                try {
                    while (cursor.moveToNext()) {

                        String locationId = cursor.getString(0);
                        Cursor locationCursor = db.rawQuery("SELECT * FROM " + LocationTable2.TABLE_NAME + " WHERE " + LocationTable2.COLUMN_LOCATION_ID + " = ?", new String[]{locationId});

                        if (locationCursor.moveToFirst()) {
                            String placeName = locationCursor.getString(3);
                            Double latitude = locationCursor.getDouble(2);
                            Double longitude = locationCursor.getDouble(1);
                            String city = locationCursor.getString(5);
                            String area = locationCursor.getString(6);
                            String Note = locationCursor.getString(7);

                            boolean vibrate=locationCursor.getInt(8)!=0;
                            boolean ringtone=locationCursor.getInt(9)!=0;
                            int notificationTime=locationCursor.getInt(10);

                            sharedViewModel.uuid = locationCursor.getString(4);
                            sharedViewModel.setDestination(placeName, latitude, longitude);
                            sharedViewModel.setCapital(city);
                            sharedViewModel.setArea(area);

                            sharedViewModel.setNote(Note, count);
                            sharedViewModel.setVibrate(vibrate,count);
                            sharedViewModel.setRingtone(ringtone,count);
                            sharedViewModel.setNotification(notificationTime,count++);
                        }

                        locationCursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.close();
                    cursor.close();
                }

                // 處理選擇的項目
                // 你可以在這裡執行與選擇的項目相關的邏輯
                EditCreateLocation editCreateLocation = new EditCreateLocation();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, editCreateLocation);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        user_sure.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 套用按鈕在這實現功能
                saveInShareviewModel();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                Toast.makeText(getActivity(), "請開啟定位權限", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupActionBar();
        setupNavigationDrawer();
        RecycleViewReset();
        changeNotification();
    }

    private void RecycleViewReset() {
        arrayList.clear();
        addFromDB();
        listAdapterBook.notifyDataSetChanged();
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
                bookTitle.setText("收藏路線");
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

    private void setupNavigationDrawer() {
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout);
        toolbar = requireActivity().findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(
                requireActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.green));
    }

    //1刪除
    private void setupRecyclerViews() {
        recyclerViewBook = rootView.findViewById(R.id.recycleView_book);
        recyclerViewBook.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewBook.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        listAdapterBook = new ListAdapterHistory(arrayList);

        // 設置選項選擇監聽器
        listAdapterBook.setOnItemSelectedListener(new ListAdapterHistory.OnItemSelectedListener() {
            @Override
            public void onItemSelected() {
                // 當選擇了項目時啟用 setbook_imageView
                setbook_imageView.setEnabled(listAdapterBook.getSelectedPosition() != RecyclerView.NO_POSITION);
            }
        });

        recyclerViewBook.setAdapter(listAdapterBook);
        // 添加左滑刪除功能
        // 創建並設置 ItemTouchHelper
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getContext(), new SwipeToDeleteCallback.OnSwipedListener() {
            @Override
            public void onSwiped(int position) {
                View itemView = recyclerViewBook.getLayoutManager().findViewByPosition(position);
                if (itemView != null) {
                    showDeleteConfirmationDialog(itemView, position);
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBook);

    }

    //刪除提醒
    private void showDeleteConfirmationDialog(View view, int position) {
        Context context = view.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);

        //套用XML的布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_deltebook, null);

        builder.setView(customView);

        // 创建并显示对话框
        AlertDialog dialog = builder.create();
        // 設置點擊對話框外部不會關閉對話框
        dialog.setCanceledOnTouchOutside(false);

        Button positiveButton = customView.findViewById(R.id.Popupsure);
        Button negativeButton = customView.findViewById(R.id.PopupCancel);


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
                changeNotification();
                dialog.cancel();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listAdapterBook.notifyItemChanged(position);
                dialog.cancel();
            }

        });

        dialog.show();

    }

    //刪除項目
    private void removeItem(int position) {
        HashMap<String, String> item = arrayList.get(position);
        String time = item.get("time");

        // 從數據庫中刪除選中的項目
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            String locationId = DatabaseUtils.stringForQuery(db,
                    "SELECT " + BookTable.COLUMN_LOCATION_ID +
                            " FROM " + BookTable.TABLE_NAME +
                            " WHERE " + BookTable.COLUMN_START_TIME + " = ?", new String[]{time});

            String locationUUID = DatabaseUtils.stringForQuery(db,
                    "SELECT " + LocationTable2.COLUMN_ALARM_NAME +
                            " FROM " + LocationTable2.TABLE_NAME +
                            " WHERE " + LocationTable2.COLUMN_LOCATION_ID + "= ?", new String[]{locationId});
            // 刪除 BookTable 中的項目
            db.execSQL("DELETE FROM " + BookTable.TABLE_NAME +
                    " WHERE " + BookTable.COLUMN_START_TIME + " = ?", new String[]{time});

            // 刪除 LocationTable2 中的相關項目
            db.execSQL("DELETE FROM " + LocationTable2.TABLE_NAME +
                    " WHERE " + LocationTable2.COLUMN_ALARM_NAME + " = ?", new String[]{locationUUID});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        // 從 arrayList 中刪除項目
        arrayList.remove(position);
        listAdapterBook.notifyItemRemoved(position);
    }

    private void changeNotification() {
        if (arrayList.isEmpty()) {
            TextView notification = rootView.findViewById(R.id.textView7);
            notification.setText("目前還沒有東西喔");
        } else {
            TextView notification = rootView.findViewById(R.id.textView7);
            notification.setText("");
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

    private void addFromDB() {

        String time;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM book WHERE arranged_id=0", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String placeNameTemp = cursor.getString(3);
                time = cursor.getString(1);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("placeName2", placeNameTemp);
                hashMap.put("time", time);
                arrayList.add(0, hashMap);
            }
            cursor.close();
        }
        db.close();
    }

    private void saveInShareviewModel() {
        sharedViewModel.clearAll();
        String time = "";
        for (HashMap<String, String> item : arrayList) {
            if ("true".equals(item.get("isSelected"))) {
                time = item.get("time");
            }
        }
        int count = 0;
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
                    String note = locationCursor.getString(7);

                    //設定
                    boolean vibrate=locationCursor.getInt(8)!=0;
                    boolean ringtone=locationCursor.getInt(9)!=0;
                    int notificationTime=locationCursor.getInt(10);

                    sharedViewModel.setDestination(placeName, latitude, longitude);
                    sharedViewModel.setCapital(city);
                    sharedViewModel.setArea(area);
                    sharedViewModel.setNote(note, count);
                    //設定
                    sharedViewModel.setVibrate(vibrate,count);
                    sharedViewModel.setRingtone(ringtone,count);
                    sharedViewModel.setNotification(notificationTime,count++);
                    getLastKnownLocation();
                }
                locationCursor.close(); // Ensure the cursor is closed to avoid memory leaks
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("BookFragment", "Error while trying to save in ShareViewModel", e);
        } finally {
            db.endTransaction();
            cursor.close(); // Ensure the cursor is closed to avoid memory leaks

            HomeFragment createLocationFragment = new HomeFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, createLocationFragment); // 確保R.id.fl_container是你的Fragment容器ID
            transaction.addToBackStack(null);
            transaction.commit();

            NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
            navigationView.setCheckedItem(R.id.action_home);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            sharedViewModel.setnowLocation(location.getLatitude(), location.getLongitude());
                            // Logic to handle location object
                            Log.d("Location", "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude());
                        } else {
                            Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}