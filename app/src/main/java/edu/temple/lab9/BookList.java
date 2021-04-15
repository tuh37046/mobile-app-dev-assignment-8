package edu.temple.lab9;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BookList {

    public static final List<Book>        ITEMS = new ArrayList<Book>();
    public static final Map<String, Book> ITEM_MAP = new HashMap<String, Book>();


    private static final int SIZE = 25;

    static {
        for (int i = 1; i <= SIZE; i++) {
            addItem(createBook(i));
        }
    }

    public static ArrayList<Book> search(String token) {
        if(token == "") return (ArrayList<Book>) ITEMS;
        ArrayList<Book> books = new ArrayList<Book>();
        for(int i = 0;i<25;i++) {
            boolean matching = true;
            for(int j=0;j<token.length();j++) {
                if(!ITEMS.get(i).content.toLowerCase().startsWith(token.toLowerCase())) {
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

    private static String getTitle() {
        ArrayList<String> words = new ArrayList<String>();
        words.add("The");
        words.add("Adventure");
        words.add("Picture");
        words.add("Of");
        words.add("Epic");
        words.add("Dorian");
        words.add("From");
        words.add("Land");
        words.add("Creature");
        words.add("When");
        words.add("Grammar");
        words.add("Gray");
        words.add("How");

        Random rand = new Random();
        int nameLength = 2+rand.nextInt(4);
        String title = "";
        for(int i=0;i<nameLength;i++) {
            title += words.get(rand.nextInt(words.size()))+" ";
        }

        return title;
    }

    private static String getAuthor() {
       ArrayList<String> names = new ArrayList<String>();
        names.add("Mary");
        names.add("Scott");
        names.add("James");
        names.add("Wells");
        names.add("Wilde");
        names.add("Noam");
        Random rand = new Random();
        return "By "+names.get(rand.nextInt(names.size()))+" "+names.get(rand.nextInt(names.size()));
    }

    private static Book createBook(int position) {
        return new Book(String.valueOf(position), getTitle(),getAuthor());
    }

    public static class Book {

        public final int position;
        public final String content;
        public final String details;
        public final String id;

        public Book(String id,String title,String author) {
            this.position = Integer.valueOf(id);
            this.id = id;
            this.details = author;
            this.content = title;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}