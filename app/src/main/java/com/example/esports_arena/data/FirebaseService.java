package com.example.esports_arena.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class FirebaseService {
    private static final String PLAYERS_NODE = "players";

    private FirebaseService() {
        // Utility class
    }

    public static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance();
    }

    public static DatabaseReference root() {
        return db().getReference();
    }

    public static DatabaseReference playersRef() {
        return root().child(PLAYERS_NODE);
    }
}
