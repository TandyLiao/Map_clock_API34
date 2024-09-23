package com.example.map_clock_api34.CreateLocation.CreatlocationListAdapter;

// 引入所需的 Android 和其他庫
import static android.app.PendingIntent.getActivity;
import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.text.InputType;

import android.view.LayoutInflater;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.BusAdvice.busMapsFragment;
import com.example.map_clock_api34.MRTStationFinder.MRTStationDistanceCalculator;
import com.example.map_clock_api34.MRTStationFinder.NearestMRTStationFinder;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.TutorialFragment;
import com.example.map_clock_api34.Weather.WeatherAdviceHelper;
import com.example.map_clock_api34.Weather.WeatherService;
import com.example.map_clock_api34.Weather.WheatherFragment;
import com.example.map_clock_api34.book.BookDatabaseHelper;
import com.example.map_clock_api34.note.NoteFragment;
import com.example.map_clock_api34.setting.CreatLocation_setting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

// 自定義的 RecyclerView Adapter，主要用來處理列表中的數據展示及點擊事件
public class ListAdapterTool extends RecyclerView.Adapter<ListAdapterTool.ViewHolder> {

    private final ArrayList<HashMap<String, String>> arrayList; // 定義 ArrayList 來存放列表數據
    private final FragmentTransaction fragmentTransaction;
    private WeatherAdviceHelper weatherAdviceHelper;
    private final SharedViewModel sharedViewModel;
    private final BookDatabaseHelper dbBookHelper;

    private final Context context;

    private String uniqueID; // 定義一個唯一 ID，用來辨識資料

    // ListAdapterTool 的構造函數，初始化基本屬性
    public ListAdapterTool(FragmentTransaction fragmentTransaction, SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.fragmentTransaction = fragmentTransaction;
        this.arrayList = new ArrayList<>();
        this.weatherAdviceHelper = new WeatherAdviceHelper(sharedViewModel, weatherService, context);
        this.sharedViewModel = sharedViewModel;
        this.dbBookHelper = new BookDatabaseHelper(context);
        this.context=context;
        initializeData();  // 初始化列表數據
    }

    // 初始化列表中的資料項目，並加入到 arrayList
    private void initializeData() {
        HashMap<String, String> item1 = new HashMap<>();
        item1.put("data", "記事");
        arrayList.add(item1);

        HashMap<String, String> item2 = new HashMap<>();
        item2.put("data", "地點設定");
        arrayList.add(item2);

        HashMap<String, String> item3 = new HashMap<>();
        item3.put("data", "收藏路線");
        arrayList.add(item3);

        HashMap<String, String> item4 = new HashMap<>();
        item4.put("data", "直達公車路線");
        arrayList.add(item4);

        HashMap<String, String> item5 = new HashMap<>();
        item5.put("data", "天氣");
        arrayList.add(item5);

        HashMap<String, String> item6 = new HashMap<>();
        item6.put("data", "捷運查詢");
        arrayList.add(item6);
    }

    // 處理圖片的點擊事件，根據不同位置切換到對應的 Fragment 或彈出對話框
    private void handleImageClick(View view, int position) {
        Context context = view.getContext();

        if (position == 0) {  // 如果點擊的是第一個項目 "記事"
            if (sharedViewModel.getLocationCount() == -1) {
                // 如果未選擇地點，提示使用者
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            // 切換到記事的 Fragment
            NoteFragment notesFragment = new NoteFragment();
            fragmentTransaction.replace(R.id.fl_container, notesFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (position == 1) {  // 第二個項目 "地點設定"
            if (sharedViewModel.getLocationCount() == -1) {
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            // 切換到地點設定的 Fragment
            CreatLocation_setting createlocation_setting = new CreatLocation_setting();
            fragmentTransaction.replace(R.id.fl_container, createlocation_setting);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (position == 2) {  // 第三個項目 "收藏路線"
            if (sharedViewModel.getLocationCount() == -1) {
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            // 顯示一個彈出對話框，讓使用者輸入路線名稱
            makeDialog();

        } else if (position == 3) {  // 第四個項目 "直達公車路線"
            if (sharedViewModel.getLocationCount() == -1) {
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            // 檢查是否需要顯示教學頁面
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("BusLogin", false);

            if (!isLoggedIn) {
                // 如果第一次進入，顯示教學頁面
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("WhichPage", 5);
                editor.putBoolean("BusLogin", true);
                editor.apply();

                TutorialFragment tutorialFragment = new TutorialFragment();
                fragmentTransaction.replace(R.id.fl_container, tutorialFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }else{
                busMapsFragment busFragment = new busMapsFragment();
                fragmentTransaction.replace(R.id.fl_container, busFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }


        } else if (position == 4) {  // 第五個項目 "天氣"
            if (sharedViewModel.getLocationCount() == -1) {
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            WheatherFragment wheatherFragment = new WheatherFragment();
            fragmentTransaction.replace(R.id.fl_container, wheatherFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else if (position == 5) {  // 第六個項目 "捷運查詢"
            if (sharedViewModel.getLocationCount() == -1) {
                makeToast("還沒有選擇地點喔",1000);
                return;
            }
            NearestMRTStationFinder userMRTStation = new NearestMRTStationFinder();
            NearestMRTStationFinder destinationMRTStation = new NearestMRTStationFinder();

            MRTStationDistanceCalculator mrtStationDistanceCalculator = new MRTStationDistanceCalculator(context, sharedViewModel.getNowLantitude(), sharedViewModel.getNowLontitude(), userMRTStation);
            mrtStationDistanceCalculator.findNearestStation();

            MRTStationDistanceCalculator StationDistanceCalculator = new MRTStationDistanceCalculator(context, sharedViewModel.getLatitude(0), sharedViewModel.getLongitude(0), destinationMRTStation);
            StationDistanceCalculator.findNearestStation();

            Log.d("MRT",userMRTStation.toString() + destinationMRTStation);
        }
    }

    // 綁定 ViewHolder，將數據顯示在相應的 UI 元件上
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String, String> item = arrayList.get(position);
        String data = item.get("data");
        holder.horecycleName.setText(data);

        // 根據不同的數據項目設置相應的圖片
        switch (data) {
            case "記事":
                holder.horecycleimageView.setImageResource(R.drawable.note);
                break;
            case "收藏路線":
                holder.horecycleimageView.setImageResource(R.drawable.routemark);
                break;
            case "天氣":
                holder.horecycleimageView.setImageResource(R.drawable.weather);
                break;
            case "直達公車路線":
                holder.horecycleimageView.setImageResource(R.drawable.route_well2);
                break;
            case "地點設定":
                holder.horecycleimageView.setImageResource(R.drawable.vibrate);
                break;
            case "捷運查詢":
                holder.horecycleimageView.setImageResource(R.drawable.middle_finger);
                break;
        }
    }

    // ViewHolder 類別，用來持有 UI 元件
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView horecycleName;
        private ImageView horecycleimageView;

        public ViewHolder(View itemView) {
            super(itemView);
            horecycleName = itemView.findViewById(R.id.horecycle_Name);
            horecycleimageView = itemView.findViewById(R.id.horecycle_imageView);

            // 設置點擊事件，根據點擊的位置呼叫 handleImageClick 方法
            horecycleimageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    handleImageClick(v, position);
                }
            });

            horecycleName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    handleImageClick(v, position);
                }
            });
        }
    }

    // 創建 ViewHolder 並設置其佈局
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerviewitem_tool, parent, false);
        return new ViewHolder(view);
    }

    // 返回列表項目的總數
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // 顯示對話框
    public void makeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_setbook, null);

        EditText input = customView.findViewById(R.id.input);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(customView);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 設置觸摸事件來隱藏鍵盤
        dialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View currentFocus = dialog.getCurrentFocus();
                    if (currentFocus != null && currentFocus instanceof EditText) {
                        int[] location = new int[2];
                        currentFocus.getLocationOnScreen(location);
                        float x = event.getRawX() + currentFocus.getLeft() - location[0];
                        float y = event.getRawY() + currentFocus.getTop() - location[1];

                        // 判斷是否點擊在輸入框外
                        if (x < currentFocus.getLeft() || x > currentFocus.getRight()
                                || y < currentFocus.getTop() || y > currentFocus.getBottom()) {
                            hideKeyboard(currentFocus);  // 隱藏鍵盤
                        }
                    }
                }
                return false;
            }
        });


        Button positiveButton = customView.findViewById(R.id.Popupsure);
        Button negativeButton = customView.findViewById(R.id.PopupCancel);

        // 設定確定按鈕點擊事件，儲存路線資料
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookmarkName = input.getText().toString();
                if (!bookmarkName.isEmpty()) {
                    if(bookmarkName.length() > 10){
                        makeToast("書籤名稱必須小於10個字",1000);
                        return;
                    }
                    saveInLocationDB();  // 儲存地點資料
                    saveInBookDB(bookmarkName);  // 儲存書籤資料
                    makeToast("路線'" + bookmarkName + "' 已添加",1000);
                    dialog.dismiss();
                } else {
                    makeToast("請輸入路線名稱",1000);
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();


    }

    // 隱藏鍵盤方法
    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 儲存地點資料到資料庫的方法
    private void saveInLocationDB() {
        // 產生一組獨一無二的ID存入區域變數內(重複機率近乎為0)
        uniqueID = UUID.randomUUID().toString();

        SQLiteDatabase db = dbBookHelper.getWritableDatabase();

        // 遍歷所有選擇的地點，將其資料存入資料庫
        for (int i = 0; i <= sharedViewModel.getLocationCount(); i++) {
            String name = sharedViewModel.getDestinationName(i);
            double latitude = sharedViewModel.getLatitude(i);
            double longitude = sharedViewModel.getLongitude(i);
            String CityName = sharedViewModel.getCapital(i);
            String AreaName = sharedViewModel.getArea(i);
            String Note = sharedViewModel.getNote(i);
            boolean vibrate = sharedViewModel.getVibrate(i);
            boolean ringtone = sharedViewModel.getRingtone(i);
            int notificationTime = sharedViewModel.getNotification(i);

            // 檢查地點名稱是否不為空，並將地點資料插入資料庫
            if (name != null) {
                ContentValues values = new ContentValues();
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_PLACE_NAME, name);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_LATITUDE, latitude);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_LONGITUDE, longitude);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_ALARM_NAME, uniqueID);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_CITY_NAME, CityName);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_AREA_NAME, AreaName);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_NOTE_INFO, Note);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_VIBRATE, vibrate);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_RINGTONE, ringtone);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_notificationTime, notificationTime);

                db.insert(BookDatabaseHelper.LocationTable2.TABLE_NAME, null, values);
            }
        }
        db.close();
    }

    // 儲存書籤資料到資料庫的方法
    private void saveInBookDB(String bookName) {
        try {
            SQLiteDatabase writeDB = dbBookHelper.getWritableDatabase();
            SQLiteDatabase readDB = dbBookHelper.getReadableDatabase();

            // 查詢剛剛儲存的地點 ID
            Cursor cursor = readDB.rawQuery("SELECT location_id FROM location WHERE alarm_name=?", new String[]{uniqueID});

            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(new Date(currentTimeMillis));

            int arranged_id_local = 0;

            // 將地點資料插入書籍表中
            while (cursor.moveToNext()) {
                if (bookName != null) {
                    ContentValues values = new ContentValues();
                    values.put(BookDatabaseHelper.BookTable.COLUMN_START_TIME, formattedDate);
                    values.put(BookDatabaseHelper.BookTable.COLUMN_ALARM_NAME, bookName);
                    values.put(BookDatabaseHelper.BookTable.COLUMN_LOCATION_ID, cursor.getString(0));
                    values.put(BookDatabaseHelper.BookTable.COLUMN_ARRANGED_ID, arranged_id_local++);
                    writeDB.insert(BookDatabaseHelper.BookTable.TABLE_NAME, null, values);
                }
            }

            writeDB.close();
            readDB.close();

        } catch (Exception e) {
            Log.d("DBProblem", e.getMessage());
        }
    }
    public void makeToast(String message, int durationInMillis) {
        // 創建 Toast
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();

        // 使用 Handler 來控制顯示時長
        new Handler().postDelayed(toast::cancel, durationInMillis);
    }
}
