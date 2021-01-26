package com.example.uber;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.Intent;
import android.icu.util.MeasureUnit;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.uber.Model.DriverInfoModel;
import com.example.uber.utils.UserUtils;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

import static android.widget.Toast.*;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171; // Any number you want
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @BindView (R.id.top_progress_bar)
    ProgressBar progress_bar;
    
    
    FirebaseDatabase database;
    DatabaseReference driverInfoRef;
    private Object DriverHomeActivity;
    private Dialog target;

    @Override
    protected void onStart() {
        super.onStart ();
        delaySplashScreen();
    }

    @Override
    protected void onStop() {
        if(firebaseAuth != null && listener != null)
            firebaseAuth.removeAuthStateListener ( listener );
        super.onStop ();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        init();
    }

    private void init ( ) {

        ButterKnife.bind(target:this)
        driverInfoRef = database.getReference (Common.DRIVER_INFO_REFERENCE);

        providers = Arrays.asList (
                new AuthUI.IdpConfig.PhoneBuilder ().build (),
                new AuthUI.IdpConfig.GoogleBuilder ().build ());

        firebaseAuth = FirebaseAuth.getInstance ();
        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser ();
            if (user != null)
                {
                    //Update token
                    FirebaseInstanceId.getInstance ()
                            .getInstanceId (new OnFailureListener ()
                            .addOnFailureListener(e -> makeText ( SplashScreenActivity.this , e.getMessage(), Toast.LENGTH_SHORT ).show ( ))
                            .addOnSuccessListener(instanceIdResult -> {
                                Log.d ( "Token",instanceIdResult.getToken ());
                                UserUtils.updateToken ( SplashScreenActivity.this,instanceIdResult.getToken () );
                            });
                    checkUserFromFirebase();
                }
            else
                showLoginLayout();
        };
    }

    private void checkUserFromFirebase() {
        driverInfoRef.child ( FirebaseAuth.getInstance ().getCurrentUser ().getUid () )
                .addListenerForSingleValueEvent ( new ValueEventListener ( ) {
                    @Override
                    public void onDataChange (@NonNull DataSnapshot snapshot) {
                        if (DataSnapshot.exist())
                        {
                            //Toast.makeText ( SplashScreenActivity.this , "User already register" , LENGTH_SHORT ).show ( );
                            DriverInfoModel driverInfoModel = DataSnapshot.getValue(DriverInfoModel.class );
                            goToHomeActivity(driverInfoModel);
                        }
                        else
                        {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError error) {
                        makeText ( SplashScreenActivity.this , ""+DatabaseError.getMessage(), LENGTH_SHORT ).show ( );
                    }
                } );

        
    }

    private void goToHomeActivity (DriverInfoModel driverInfoModel) {
        Common.CurrentUser = driverInfoModel; //INit value
        startActivity (new Intent ( SplashScreenActivity.this,DriverHomeActivity));
        finish ();
    }

    private void showRegisterLayout ( ) {
        AlertDialog.Builder builder = new AlertDialog.Builder ( this,R.style.DialogTheme );
        View itemView = LayoutInflater.from(this).inflate ( R.layout.layout_register,null );

        TextInputEditText edt_first_name = (TextInputEditText)itemView.findViewById ( R.id.edt_first_name );
        TextInputEditText edt_last_name = (TextInputEditText)itemView.findViewById ( R.id.edt_last_name );
        TextInputEditText edt_phone_number = (TextInputEditText)itemView.findViewById ( R.id.edit_phone_number );

        Button btn_continue = (Button)itemView.findViewById ( R.id.btn_register );

        //Set data
        if (FirebaseAuth.getInstance ().getCurrentUser ().getPhoneNumber () != null &&
                !TextUtils.isEmpty ( FirebaseAuth.getInstance ().getCurrentUser ().getPhoneNumber () ))
        edt_phone_number.setText(FirebaseAuth.getInstance ().getCurrentUser ().getPhoneNumber ());

        //Set View
        builder.setView ( itemView );
        AlertDialog = builder.create ();
        Dialog.show();

        btn_continue.setOnClickListener ( v -> {
                    if (TextUtils.isEmpty ( edt_first_name.getText ().toString () ))
                    {
                        makeText ( this , "Please enter first name" , LENGTH_SHORT ).show ( );
                        return;
                    }
                    else if (TextUtils.isEmpty ( edt_last_name.getText ().toString () ))
                    {
                        makeText ( this , "Please enter last name" , LENGTH_SHORT ).show ( );
                        return;
                    }
                    else if (TextUtils.isEmpty(edt_phone_number.getText ().toString ()));
                    {
                        makeText ( this , "Please enter phone number" , LENGTH_SHORT ).show ( );
                        return;
                    }
                    else
                    {
                        DriverInfoModel model = new DriverInfoModel ();
                        model.setFirstName (edt_first_name.getText ().toString () );
                        model.setLastName (edt_last_name.getText ().toString ());
                        model.setPhoneNumber (edt_phone_number.getText ().toString ());
                        model.setRating (0,0);

                        driverInfoRef.child ( FirebaseAuth.getInstance ().getCurrentUser ().getUid () )DatabaseReference
                                .setValue ( model )
                                .addOnFailureListener ( e ->
                                        {
                                           Dialog.dismiss();
                                           Toast.makeText ( SplashScreenActivity.this , e.getMessage () , LENGTH_SHORT ).show ( ))Task<Void>
                                        }
                                        )
                                .addOnSuccessListener(aVoid -> {
                                    makeText ( this , "Register Successfully!" , LENGTH_SHORT ).show ( );
                                    Dialog.dismiss();
                                    goToHomeActivity ( model );
                                }

                    }

        } );
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new  AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId ( R.id.btn_phone_sign_in )
                .setGoogleButtonId ( R.id.btn_google_sign_in )
                .build ();

                startActivityForResult ( AuthUI.getInstance ()
                .createSignInIntentBuilder ()AuthUI.SignInIntentBuilder
                .setAuthMethodPickerLayout (authMethodPickerLayout)AuthUI.SignInIntentBuilder
                .setIsSmartLockEnabled(false)AuthUI.SignInIntentBuilder
                .setTheme(R.style.LoginTheme)AuthUI.SignInIntentBuilder
                .setAvailableProviders(providers)AuthUI.SignInIntentBuilder
                .build(), LOGIN_REQUEST_CODE);
    }

    private void delaySplashScreen() {

        progress_bar.setVisibility ( View.VISIBLE );

        Completable.timer (delaySplashScreen ():3, MeasureUnit.SECOND
        Scheduler scheduler = AndroidSchedulers.mainThread ( ))
            .subscribe(() ->
                //After show Splash Screen, ask login if not login
        {
            return firebaseAuth.addAuthStateListener(listener);
        }
    }


    @Override
    protected void onActivityResult (int requestCode , int resultCode , @Nullable Intent data) {
        super.onActivityResult ( requestCode , resultCode , data );
        if(requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent ( data );
            if(resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance ().getCurrentUser ();
            }
            else
            {
                makeText ( this , "[ERROR]: "+response.getError ().getMessage () , LENGTH_SHORT ).show ( );
            }
        }
    }

    private static class DRIVER_INFO_REFERENCE {
    }
}