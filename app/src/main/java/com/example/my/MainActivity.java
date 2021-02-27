package com.example.my;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    Button addButton, findButton;
    EditText addName, addAuthor, addYear;
    LinkedList<HashMap<String, Object>> mapBooks = new LinkedList<>();
    SimpleAdapter simpleAdapter;

    MyOpenHelper myOpenHelper;
    SQLiteDatabase sdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myOpenHelper =new MyOpenHelper(this);
        sdb = myOpenHelper.getWritableDatabase();

        addButton = findViewById(R.id.add_button);
        addName = findViewById(R.id.edit_name);
        addAuthor = findViewById(R.id.edit_author);
        addYear = findViewById(R.id.edit_year);
        findButton = findViewById(R.id.find);
        listView = findViewById(R.id.bookList);

        //Подготовка данных - 1 этап создание списка объектов
        final LinkedList<Book> books = new LinkedList<>();
        books.add(new Book("Война и мир", "Лев Толстой", "2004", R.drawable.book));
        books.add(new Book("Основание", "Айзек Азимов", "2017", R.drawable.osnovanie));
        books.add(new Book("Преступление и наказание", "Федор Достоевский", "1986", R.drawable.prestuplenie));
        books.add(new Book("Шинель", "Николай Гоголь", "2008", R.drawable.shinel));
        books.add(new Book("Зерцалия", "Евгений Гоглоев", "2019", R.drawable.zertsalia));
        books.add(new Book("Феникс Сапиенс", "Борис Штерн", "2020", R.drawable.book));

        //добавление списка книг в базу данных
        for (int i = 0; i < books.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MyOpenHelper.COLUMN_AUTHOR, books.get(i).author);
            contentValues.put(MyOpenHelper.COLUMN_TITLE, books.get(i).title);
            contentValues.put(MyOpenHelper.COLUMN_YEAR, books.get(i).year);
            contentValues.put(MyOpenHelper.COLUMN_COVER, books.get(i).cover);
            sdb.insert(MyOpenHelper.TABLE_NAME, null, contentValues);
        }

        //Подготовка данных 2 этап: список с ключами
        for (int i = 0; i < books.size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("author", books.get(i).author);
            map.put("title", books.get(i).title);
            map.put("year", books.get(i).year);
            map.put("cover", books.get(i).cover);
            mapBooks.add(map);
        }

        //подготовка 3 этап: вспомогательные массивы
        String[] keyFrom = {"author", "title", "year", "cover"};
        int [] idTo = {R.id.author, R.id.title, R.id.year, R.id.cover};
        //Создание адаптера
        simpleAdapter = new SimpleAdapter(this, mapBooks, R.layout.list_item,
                keyFrom, idTo);

        //установка адаптера на ListView
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), i + ") " + books.get(i), Toast.LENGTH_SHORT)
                        .show();
            }
        });

        //добавление новых книг
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MyOpenHelper.COLUMN_AUTHOR, addAuthor.getText().toString());
                contentValues.put(MyOpenHelper.COLUMN_TITLE, addName.getText().toString());
                contentValues.put(MyOpenHelper.COLUMN_YEAR, addYear.getText().toString());
                contentValues.put(MyOpenHelper.COLUMN_COVER, R.drawable.book);
                sdb.insert(MyOpenHelper.TABLE_NAME, null, contentValues);
            }
        });

        //запросы к базе дынных
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String findAuthor = addAuthor.getText().toString();
                String findTittle = addName.getText().toString();
                String findDate = addYear.getText().toString();
                int findCover;

                String query = "SELECT * FROM " + MyOpenHelper.TABLE_NAME;
                if (!findAuthor.equals(""))
                    query += " WHERE author = \"" + findAuthor + "\";";
                else if (!findTittle.equals(""))
                    query += " WHERE title = \"" + findTittle + "\";";
                else if (!findDate.equals(""))
                    query += " WHERE date = \"" + findDate + "\";";
                else query += ";";
                Cursor cursor = sdb.rawQuery(query, null);

                mapBooks.clear();

                cursor.moveToFirst();
                while (cursor.moveToNext()){
                    findAuthor = cursor.getString(cursor.getColumnIndex(MyOpenHelper.COLUMN_AUTHOR));
                    findTittle = cursor.getString(cursor.getColumnIndex("title"));
                    findDate = cursor.getString(cursor.getColumnIndex(MyOpenHelper.COLUMN_YEAR));
                    findCover = cursor.getInt(cursor.getColumnIndex(MyOpenHelper.COLUMN_COVER));

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("author", findAuthor);
                    map.put("title", findTittle);
                    map.put("year", findDate);
                    map.put("cover", findCover);
                    mapBooks.add(map);
                }
                cursor.close();
                simpleAdapter.notifyDataSetChanged();
            }
        });
    }
}