package com.example.josh.androidchess;

import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SavedGames implements Serializable {
    public String name;
    public Date date;
    public List<String[]> moves;
    public int index;

    public static ArrayList<SavedGames> gameList = new ArrayList<SavedGames>();

    public SavedGames(String name, Date date, List<String[]> moves) {
        this.name = name;
        this.date = date;
        this.moves = new ArrayList<String[]>(moves);
    }

    public static void addGame(String name, Date date, List<String[]> moves) {
        gameList.add(new SavedGames(name, date, moves));
    }

    public static void sortByName() {
        Comparator<SavedGames> comparator = new Comparator<SavedGames>() {
            @Override
            public int compare(SavedGames savedGames, SavedGames t1) {
                return savedGames.name.compareTo(t1.name);
            }
        };
        Collections.sort(gameList, comparator);
    }

    public static void sortByDate() {
        Comparator<SavedGames> comparator = new Comparator<SavedGames>() {
            @Override
            public int compare(SavedGames savedGames, SavedGames t1) {
                return savedGames.date.compareTo(t1.date);
            }
        };
        Collections.sort(gameList, comparator);
    }

    public String getDate() {
        DateFormat df = new SimpleDateFormat("M/d/yy HH:mm");
        return df.format(date);
    }

    public String[] nextMove() {
        if (index < moves.size())
            return moves.get(index++);
        return null;
    }

    @Override
    public String toString() {
        return name + " " + getDate();
    }
}
