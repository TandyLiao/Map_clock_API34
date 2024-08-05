package com.example.map_clock_api34.home.ListAdapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.text.InputType;

import android.view.LayoutInflater;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.map_clock_api34.BusAdvice.busMapsFragment;
import com.example.map_clock_api34.R;
import com.example.map_clock_api34.SharedViewModel;
import com.example.map_clock_api34.Weather.WeatherAdviceHelper;
import com.example.map_clock_api34.Weather.WeatherService;
import com.example.map_clock_api34.Weather.WheatherFragment;
import com.example.map_clock_api34.book.BookDatabaseHelper;
import com.example.map_clock_api34.note.Note;
import com.example.map_clock_api34.setting.CreatLocation_setting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;


public class ListAdapterTool extends RecyclerView.Adapter<ListAdapterTool.ViewHolder> {

    private ArrayList<HashMap<String, String>> arrayList;
    private FragmentTransaction fragmentTransaction;
    private WeatherAdviceHelper weatherAdviceHelper;
    private SharedViewModel sharedViewModel;
    private BookDatabaseHelper dbBookHelper;

    String uniqueID;

    public ListAdapterTool(FragmentTransaction fragmentTransaction, SharedViewModel sharedViewModel, WeatherService weatherService, Context context) {
        this.fragmentTransaction = fragmentTransaction;
        this.arrayList = new ArrayList<>();
        this.weatherAdviceHelper = new WeatherAdviceHelper(sharedViewModel, weatherService, context);
        this.sharedViewModel = sharedViewModel;
        this.dbBookHelper = new BookDatabaseHelper(context);
        initData();
    }

    private void initData() {
        HashMap<String, String> item1 = new HashMap<>();
        item1.put("data", "記事");
        arrayList.add(item1);
        HashMap<String, String> item2 = new HashMap<>();
        item2.put("data", "加入書籤");
        arrayList.add(item2);
        HashMap<String, String> item3 = new HashMap<>();
        item3.put("data", "天氣");
        arrayList.add(item3);
        HashMap<String, String> item4 = new HashMap<>();
        item4.put("data", "推薦路線");
        arrayList.add(item4);
        HashMap<String, String> item5 = new HashMap<>();
        item5.put("data", "地點設定");
        arrayList.add(item5);
        HashMap<String, String> item6 = new HashMap<>();
        item6.put("data", "哇哭哇哭");
        arrayList.add(item6);
    }

    private void handleImageClick(View view, int position) {
        Context context = view.getContext();

        if (position == 0) {
            if (sharedViewModel.getLocationCount() == -1) {
                Toast.makeText(context, "你還沒有選擇地點喔", Toast.LENGTH_SHORT).show();
                return;
            }
            Note notesFragment = new Note();
            fragmentTransaction.replace(R.id.fl_container, notesFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
        else if (position == 1) {
            if (sharedViewModel.getLocationCount() == -1) {
                Toast.makeText(context, "你還沒有選擇地點喔", Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);

            //套用XML的布局
            LayoutInflater inflater = LayoutInflater.from(context);
            View customView = inflater.inflate(R.layout.dialog_setbook, null);

            //找到XML的輸入框
            EditText input = customView.findViewById(R.id.input);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(customView);

            // 创建并显示对话框
            AlertDialog dialog = builder.create();
            // 設置點擊對話框外部不會關閉對話框
            dialog.setCanceledOnTouchOutside(false);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // 找到並設置按鈕事件
            Button positiveButton = customView.findViewById(R.id.Popupsure);
            Button negativeButton = customView.findViewById(R.id.PopupCancel);

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String bookmarkName = input.getText().toString();
                    if (!bookmarkName.isEmpty()) {
                        // 處理確定按鈕點擊事件
                        saveInLocationDB();
                        saveInBookDB(bookmarkName);
                        Toast.makeText(context, "書籤 '" + bookmarkName + "' 已添加", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "請輸入書籤名稱", Toast.LENGTH_SHORT).show();
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



        else if (position == 2) {
            if (sharedViewModel.getLocationCount() == -1) {
                Toast.makeText(context, "你還沒有選擇地點喔", Toast.LENGTH_SHORT).show();
                return;
            }
            WheatherFragment wheatherFragment = new WheatherFragment();
            fragmentTransaction.replace(R.id.home_fragment_container, wheatherFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (position == 3) {
            if (sharedViewModel.getLocationCount() == -1) {
                Toast.makeText(context, "你還沒有選擇地點喔", Toast.LENGTH_SHORT).show();
                return;
            }
            busMapsFragment busFragment = new busMapsFragment();
            fragmentTransaction.replace(R.id.home_fragment_container, busFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else if (position == 4) {
            CreatLocation_setting createlocation_setting = new CreatLocation_setting();
            fragmentTransaction.replace(R.id.home_fragment_container, createlocation_setting);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else if (position == 5) {
           // settingsHandler.showSettingsPopupWindow();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String, String> item = arrayList.get(position);
        String data = item.get("data");
        holder.horecycleName.setText(data);
        //holder.textView.setText(data);
        //benson
        //holder.noteView.setText(data);

        switch (data) {
            case "記事":
                holder.horecycleimageView.setImageResource(R.drawable.note);
                break;
            case "加入書籤":
                holder.horecycleimageView.setImageResource(R.drawable.addbookmark);
                break;
            case "天氣":
                holder.horecycleimageView.setImageResource(R.drawable.weather);
                break;
            case "推薦路線":
                holder.horecycleimageView.setImageResource(R.drawable.route_well2);
                break;
            case "地點設定":
                holder.horecycleimageView.setImageResource(R.drawable.vibrate);
                break;
            case "哇哭哇哭":
                holder.horecycleimageView.setImageResource(R.drawable.bell);
                break;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView horecycleName;
        private ImageView horecycleimageView;
        //private TextView textView;
        //private TextView noteView;
//benson
        public ViewHolder(View itemView) {
            super(itemView);
            horecycleName = itemView.findViewById(R.id.horecycle_Name);
            horecycleimageView = itemView.findViewById(R.id.horecycle_imageView);

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerviewitem_tool, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private void saveInLocationDB() {

        //產生一組獨一無二的ID存入區域變數內(重複機率近乎為0)
        //用這ID去做Location_id和BookTable做配對
        uniqueID = UUID.randomUUID().toString();

        SQLiteDatabase db = dbBookHelper.getWritableDatabase();

        for (int i = 0; i <= sharedViewModel.getLocationCount(); i++) {

            String name = sharedViewModel.getDestinationName(i);
            double latitude = sharedViewModel.getLatitude(i);
            double longitude = sharedViewModel.getLongitude(i);
            String CityName = sharedViewModel.getCapital(i);
            String AreaName = sharedViewModel.getArea(i);
            String Note = sharedViewModel.getNote(i);

            if (name != null) {
                ContentValues values = new ContentValues();
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_PLACE_NAME, name);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_LATITUDE, latitude);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_LONGITUDE, longitude);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_ALARM_NAME, uniqueID);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_CITY_NAME, CityName);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_AREA_NAME, AreaName);
                values.put(BookDatabaseHelper.LocationTable2.COLUMN_NOTE_INFO, Note);

                db.insert(BookDatabaseHelper.LocationTable2.TABLE_NAME, null, values);
            }
        }
        db.close();
    }

    private void saveInBookDB(String bookName) {

        try {
            SQLiteDatabase writeDB = dbBookHelper.getWritableDatabase();
            SQLiteDatabase readDB = dbBookHelper.getReadableDatabase();

            Cursor cursor = readDB.rawQuery("SELECT location_id FROM location WHERE alarm_name=?", new String[]{uniqueID});

            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(new Date(currentTimeMillis));

            //此變數要讓History的每筆路線都從0開始存入
            int arranged_id_local=0;

            while (cursor.moveToNext()) {
                if (bookName != null) {
                    ContentValues values = new ContentValues();
                    values.put(BookDatabaseHelper.BookTable.COLUMN_START_TIME, formattedDate);
                    values.put(BookDatabaseHelper.BookTable.COLUMN_ALARM_NAME, bookName);
                    values.put(BookDatabaseHelper.BookTable.COLUMN_LOCATION_ID, cursor.getString(0));
                    //arranged_id_local存入History表後再+1
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
}
