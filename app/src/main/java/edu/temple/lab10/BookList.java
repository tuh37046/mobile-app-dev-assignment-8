package edu.temple.lab10;


import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BookList {

    public static final List<Book>        ITEMS = new ArrayList<Book>();
    public static final Map<String, Book> ITEM_MAP = new HashMap<String, Book>();
    public static boolean created = false;

    private static final int SIZE = 25;
    JSONArray books;

    static {
        for (int i = 1; i <= SIZE; i++) {
            //addItem(createBook(i));
        }
    }

    public static ArrayList<Book> create(JSONArray books) {
        ArrayList<Book> bookArray = new ArrayList<Book>();
        if(books == null) {
            return bookArray;
        }
        JSONObject bookJSON;
        for(int i=0;i<books.length();i++) {
            try {
                bookJSON = (JSONObject)books.get(i);
                int id = bookJSON.getInt("id");
                String cover = bookJSON.getString("cover_url");
                String title = bookJSON.getString("title");
                String author = bookJSON.getString("author");
                int duration = bookJSON.getInt("duration");
                Book book = new Book(String.valueOf(id),title,author,cover,duration);
                addItem(book);
                bookArray.add(book);
            } catch(JSONException e) {
                System.out.println("Error reading book json");
            }
        }
        created=true;
        return bookArray;
    }

    public static ArrayList<Book> search(String token) {
        ArrayList<Book> books = new ArrayList<Book>();
        if(token.equals("")) return (ArrayList<Book>) ITEMS;
        for(int i = 0;i<25;i++) {
            boolean matching = true;
            for(int j=0;j<token.length();j++) {
                if(!ITEMS.get(i).title.toLowerCase().startsWith(token.toLowerCase())) {
                    matching = false;
                    break;
                }
            }
            if(matching) {
                books.add(ITEMS.get(i));
            }
        }
        System.out.println("Returning "+String.valueOf(books.size()));
        return books;
    }

    private static void addItem(Book item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Book createBook(int position) {
        return new Book(String.valueOf(position),"Test title","Test author","Test cover URL",100);
    }

    public static class Book {

        public final int position;
        public final String title;
        public final String author;
        public final String id;
        public final String coverURL;
        public final int duration;


        public Book(String id,String title,String author,String cover,int duration) {
            this.position = Integer.valueOf(id);
            this.id = id;
            this.author = author;
            this.title = title;
            this.coverURL = cover;
            this.duration = duration;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}