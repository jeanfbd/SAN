package desenvolvimentoads.san;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;

public class MenuInicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener,ActionObserver {

    private GoogleApiClient googleApiClient;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseAuth firebaseAuth;
    private String userName;
    private String userEmail;
    private String userId;

    private boolean buttomAddMarkerVisivel;
    private static final String TAG = "MenuInicial";

    private FragmentManager fragmentManager;
    public static FloatingActionButton btnAddMarker;
    public static FloatingActionButton btnCancelAddMarker;
    static MenuItem denunciar;
    public static Boolean vDenunciar = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Action.getInstance().registraInteressados(this);
        buttomAddMarkerVisivel = Action.getInstance().getButtomAddMakerClickado();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicial);

/*
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();*/
/*
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setUserData(user);
                } else {
                    goLogInScreen();
                }
            }
        };*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.container, new MapsTerceiro(), "MapsTerceiro");

        fragmentTransaction.commitAllowingStateLoss();


        btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
        btnAddMarker.setVisibility(View.VISIBLE);


        btnCancelAddMarker = (FloatingActionButton) findViewById(R.id.fabCancel);
        btnCancelAddMarker.setVisibility(View.GONE);



        btnAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!vDenunciar){

                    changeDenunciar();


                }



                Snackbar.make(view, "Clique no mapa para criar um marcador ou clique novamente para Cancelar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();

            }
        });

       btnCancelAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Criação do novo marcador Cancelada", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();


            }
        });

    }


    public  void addMarkerFloatButtom(){

        if(buttomAddMarkerVisivel){
       // if(btnAddMarker.isShown()){
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);
            Action.getInstance().setButtomAddMakerClickado(false);
        }

        else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);
            Action.getInstance().setButtomAddMakerClickado(true);
        }


    }

    public void changeDenunciar(){
        if(vDenunciar)    {
            vDenunciar = false;
            Action.getInstance().setReportNotSelected(false);
            denunciar.setTitle("Cancel");
            Snackbar.make(getCurrentFocus(), "Clique no marcador que deseja denunciar", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        }else{

            denunciar.setTitle("Denunciar");
            vDenunciar = true;
            Action.getInstance().setReportNotSelected(true);

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inicial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.denunciar) {
            denunciar = item;


            if(!buttomAddMarkerVisivel){
               addMarkerFloatButtom();

            }
            changeDenunciar();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_all_marker:

                showFragment(new MapsPrincipal(), "Todos Marcadores");
                setTitle("Todos Marcadores");
                break;
            case R.id.nav_user_marker:

                showFragment(new MapsSegundo(), "Marcadores do Usuário");
                setTitle("Marcadores do Usuário");
                break;
            case R.id.nav_user_disable:
                showFragment(new MapsTerceiro(), "Marcadores Inativos do sistema");
                setTitle("Marcadores Inativos do sistema");
                break;
            case R.id.nav_integracao:
                showFragment(new MapsQuarto(), "Integração back front");
                setTitle("Integração back front");
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(Fragment fragment, String name){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.container, fragment, name);

        fragmentTransaction.commit();
    }
/*
    private void setUserData(FirebaseUser user) {
        userName = user.getDisplayName();
        userEmail = user.getEmail();
        userId = user.getUid();

        Log.i(TAG, "setUserData: Nome "+userName+", Email: "+userEmail+", ID: "+userId);
//        Glide.with(this).load(user.getPhotoUrl()).into(photoImageView);
    }*/

    @Override
    protected void onStart() {
        super.onStart();

      //  firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    private void goLogInScreen() {
        Intent intent = new Intent(this, TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

  /*  public void logOut(View view) {
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goLogInScreen();
                } else {
                    Toast.makeText(getApplicationContext(), "Não foi possível encerrar a sessão", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

   /* public void revoke(View view) {
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    goLogInScreen();
                } else {
                    Toast.makeText(getApplicationContext(), "Não foi possível revokar a sessão", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/
/*

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
*/


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void notificaticarInteressados(Action action) {
       buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        if(!buttomAddMarkerVisivel){
            // if(btnAddMarker.isShown()){
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);

        }

        else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);

        }



        if(vDenunciar != action.isReportNotSelected()){
            Log.i("Teste","Denunciar mudou..");
            changeDenunciar();

        }


    }
}
