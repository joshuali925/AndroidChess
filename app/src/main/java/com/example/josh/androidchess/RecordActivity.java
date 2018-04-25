package com.example.josh.androidchess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class RecordActivity extends Activity {
    public ListView listView;
    public static RecordActivity instance;
    Button sortByNameButton;
    Button sortByDateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        listView = findViewById(R.id.record_list);
        instance = this;

        updateAdapter();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final CharSequence promotions[] = new CharSequence[]{"Open", "Remove"};
                final int index = i;
                AlertDialog.Builder builder = new AlertDialog.Builder(RecordActivity.this);
                builder.setTitle(null);
                builder.setItems(promotions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openGame(index);
                        } else {
                            SavedGames.gameList.remove(index);
                            updateAdapter();
                        }
                    }
                });
                builder.show();
            }
        });

        setButtons();
    }

    protected void updateAdapter() {
        ArrayAdapter<SavedGames> gamesAdapter = new ArrayAdapter<SavedGames>(RecordActivity.this, R.layout.record);
        gamesAdapter.addAll(SavedGames.gameList);
        listView.setAdapter(gamesAdapter);
    }

    protected void openGame(int index) {
        Intent intent = new Intent();
        intent.putExtra("gameIndex", index);
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void setButtons() {
        sortByNameButton = findViewById(R.id.sortByNameButton);
        sortByNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedGames.sortByName();
                updateAdapter();
            }
        });

        sortByDateButton = findViewById(R.id.sortByDateButton);
        sortByDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedGames.sortByDate();
                updateAdapter();
            }
        });
    }

}
