package desenvolvimentoads.san;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import desenvolvimentoads.san.DAO.ConfigFireBase;

/**
 * Created by jeanf on 10/07/2017.
 */

public class TelaInicial extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseAuth firebaseAuth;

    private SignInButton signInButton;

    private static final String TAG = "TelaInicial";

    private static final int SIGN_IN_CODE = 9001;
    private static final int TIME_RUNTIME = 5000;
    private boolean pbActive;

    protected ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_inicial);
//
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
//
//        signInButton = (SignInButton) findViewById(R.id.signInButton);
//        signInButton.setSize(SignInButton.SIZE_WIDE);
//        signInButton.setColorScheme(SignInButton.COLOR_DARK);
//
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
//                startActivityForResult(intent, SIGN_IN_CODE);
//            }
//        });
//
//
//        firebaseAuth = ConfigFireBase.getAuth();
//        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    goMainScreen();
//                }else{
//                    Toast.makeText(TelaInicial.this, "Usuário NULL", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
        Intent intent = new Intent(this, MenuInicial.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(TelaInicial.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    private void goMainScreen() {
        Intent intent = new Intent(this, MenuInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void updateProgressBar(final int timePassed) {
        if (null != progressBar) {
            final int progress = progressBar.getMax() * timePassed / TIME_RUNTIME;
            progressBar.setProgress(progress);
        }
    }

//    public void onContinue() {
//        Log.d("mensagemFinal", "Sua barra concluiu");
//    }

    public void threadTelaInicial(){
        Thread timerThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(TIME_RUNTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(TelaInicial.this, MenuInicial.class);
                    startActivity(intent);
                }

            }
        };

        final Thread timerBar = new Thread() {
            @Override
            public void run() {
                pbActive = true;
                try {
                    int waited = 0;
                    while (pbActive && (waited < TIME_RUNTIME)) {
                        sleep(200);
                        if (pbActive) {
                            waited += 200;
                            updateProgressBar(waited);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
//                    onContinue();
                }
            }
        };

        timerThread.start();
        timerBar.start();
    }
}