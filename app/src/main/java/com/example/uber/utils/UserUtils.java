package com.example.uber.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.uber.Common;
import com.example.uber.Model.TokenModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserUtils {
    public static void updateUser (View view , Map<String, Object> updateData) {
        FirebaseAuth.getInstance ()
                .getReference( Common.DRIVER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance ().getCurrentUser ().getUid ())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Snackbar.make (View,e.getMessage(),Snackbar.LENGTH_SHORT).show ())
                .addOnSuccessListener(new OnSuccessListener<Void>); {
            @Override
            (Void aVoid) {
                Snackbar.make ( view,"Update information successfully!",Snackbar.LENGTH_SHORT).show ();
            }
        }
    }

    public static void updateToken (Context context , String token) {
        TokenModel tokenModel = new TokenModel (token);

        FirebaseDatabase.getInstance ()
                .getReference (Common.TOKEN_REFERENCE)
                .child (FirebaseAuth.getInstance ().getCurrentUser ().getUid ())
                .addOnFailureListener(new OnFailureListener () {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText ( context , e.getMessage () , Toast.LENGTH_SHORT ).show ( );
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void> () {
           @Override
           public void onSuccess(Void aVoid) {

           }

        });
    }
}