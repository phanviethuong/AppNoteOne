
package com.example.appnoteone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.appnoteone.Adapter.NotesListAdapter;
import com.example.appnoteone.Database.RoomDB;
import com.example.appnoteone.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add, fab_color, fab_pinned;
    SearchView searchView_home;
    Notes selectNotes;
    RelativeLayout relativeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);
        fab_color = findViewById(R.id.fab_color);
        fab_pinned =findViewById(R.id.fab_pinned);
        database =RoomDB.getInstance(this);
        notes= database.mainDAO().getAll();
        searchView_home = findViewById(R.id.searchView_home);

        relativeLayout = findViewById(R.id.main_layout);
        updateRecyler(notes);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(new Intent(MainActivity.this, NotesTakerActivity.class));
                startActivityForResult(intent, 101);
            }
        });

        fab_color.setOnClickListener(new View.OnClickListener() {
            final boolean[] isClicked = {false};
            @Override
            public void onClick(View v) {
                if(isClicked[0]) {
                    relativeLayout.setBackgroundColor(Color.BLACK);
                    searchView_home.setBackgroundColor(Color.GRAY);
                    isClicked[0] = false;
                } else {
                    relativeLayout.setBackgroundColor(Color.WHITE);
                    searchView_home.setBackgroundColor(Color.parseColor("#C0DCE8"));
                    isClicked[0] = true;
                }
            }
        });

        fab_pinned.setOnClickListener(new View.OnClickListener() {
            boolean isPinned = true;
            @Override
            public void onClick(View v) {
                if(isPinned){
                    notes.clear();
                    notes.addAll(database.mainDAO().pinned());
                    notesListAdapter.notifyDataSetChanged();
                    isPinned = false;
                    Toast.makeText(MainActivity.this, "Danh sách ghi chú đã ghim", Toast.LENGTH_SHORT).show();
                }
                else {
                    notes.clear();
                    notes.addAll(database.mainDAO().getAll());
                    notesListAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Tất cả danh sách", Toast.LENGTH_SHORT).show();
                    isPinned = true;
                }
            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filler(newText);
                return true;
            }
        });
    }

    private void filler(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNote : notes){
            if(singleNote.getTitle().toLowerCase().contains(newText.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(singleNote);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){
            if(resultCode == Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
        else if(requestCode == 102){
            if(resultCode == Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateRecyler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes,notesClickListener );
        recyclerView.setAdapter(notesListAdapter);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent,102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectNotes = new Notes();
            selectNotes = notes;
            showPopUp(cardView);
        }
    };

    private void showPopUp(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.pin:
                if(selectNotes.isPinned()){
                    database.mainDAO().pin(selectNotes.getID(),false);
                    Toast.makeText(MainActivity.this, "Đã bỏ ghim", Toast.LENGTH_LONG).show();
                }
                else {
                    database.mainDAO().pin(selectNotes.getID(), true);
                    Toast.makeText(MainActivity.this, "Đã ghim", Toast.LENGTH_LONG).show();
                }

                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete:
                database.mainDAO().delete(selectNotes);
                notes.remove(selectNotes);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Đã xóa", Toast.LENGTH_LONG).show();
                return  true;
            default:
                return false;
        }
    }
}