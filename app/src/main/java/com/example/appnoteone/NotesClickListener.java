package com.example.appnoteone;
import androidx.cardview.widget.CardView;

import com.example.appnoteone.Models.Notes;


public interface NotesClickListener {
    void onClick(Notes notes);
    void onLongClick(Notes notes, CardView cardView);
}
