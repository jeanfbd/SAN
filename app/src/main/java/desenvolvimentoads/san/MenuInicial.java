package desenvolvimentoads.san;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;

import desenvolvimentoads.san.Observer.Action;
import desenvolvimentoads.san.Observer.ActionObserver;
import desenvolvimentoads.san.Observer.SharedContext;

public class MenuInicial extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, ActionObserver {


    private static final int MY_PERMISSION_REQUEST_CODE = 2508;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    NavigationView navigationView;
    private String userName;
    private String userEmail;
    private String userId;

    private boolean buttomAddMarkerVisivel;

    private FragmentManager fragmentManager;
    public static FloatingActionButton btnAddMarker;
    public static FloatingActionButton btnCancelAddMarker;
    static MenuItem denunciar;
    public static Boolean vDenunciar = true;
    public static Boolean permissionOk = false;
    boolean menuStarted = false;
    SharedContext sharedContext = SharedContext.getInstance();
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Action.getInstance().registraInteressados(this);
        buttomAddMarkerVisivel = Action.getInstance().getButtomAddMakerClickado();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicial);
        sharedContext.setContext(this.getBaseContext());
        SharedPreferences mySharedPrefers = getSharedPreferences("tutorial", Context.MODE_PRIVATE);
        if (!mySharedPrefers.getBoolean("skip", false)) {

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);


        if (currentUser != null) {
            userName = currentUser.getDisplayName();
            userEmail = currentUser.getEmail();
            userId = currentUser.getUid();

            TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textView);
            textView.setText(userEmail);

            TextView userText = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userText);
            userText.setText(userName);

            if (currentUser.getPhotoUrl() != null) {
                ImageView imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);
                Picasso.with(this).load(currentUser.getPhotoUrl()).placeholder(R.mipmap.ic_logo_san)
                        .error(R.mipmap.ic_logo_san)
                        .into(imageView, new com.squareup.picasso.Callback() {

                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {

                                    }
                                }
                        );
            }
        }

        if (Build.VERSION.SDK_INT <= 22) {
            permissionOk = true;
            Log.i("teste", "------------------- BUILD VERSION ---------------------  " + Build.VERSION.SDK_INT);
            menuStart();

        } else {
            checkPermission();

        }


    }

    public void checkPermission() {


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);


        } else {

            menuStart();

        }


    }

    public void menuStart() {
        navigationView.setNavigationItemSelectedListener(this);


        btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
        btnAddMarker.setVisibility(View.VISIBLE);


        btnCancelAddMarker = (FloatingActionButton) findViewById(R.id.fabCancel);
        btnCancelAddMarker.setVisibility(View.GONE);


        btnAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vDenunciar) {

                    changeDenunciar();


                }


                Snackbar.make(view, "Pressione no mapa para criar um marcador ou pressione novamente para Cancelar.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();

            }
        });

        btnCancelAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Criação do novo marcador Cancelada.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                addMarkerFloatButtom();


            }
        });

        menuStarted = true;

        startFragment();
    }

    public void startFragment() {
        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.container, new MapsTerceiro(), "MapsTerceiro");

        fragmentTransaction.commitAllowingStateLoss();

    }

    public void addMarkerFloatButtom() {

        if (buttomAddMarkerVisivel) {
            // if(btnAddMarker.isShown()){
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);
            Action.getInstance().setButtomAddMakerClickado(false);
        } else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);
            Action.getInstance().setButtomAddMakerClickado(true);
        }


    }

    public void changeDenunciar() {
        if (vDenunciar) {
            vDenunciar = false;
            Action.getInstance().setReportNotSelected(false);
            denunciar.setTitle("Cancel");
            Snackbar.make(getCurrentFocus(), "Selecione o marcador que deseja denunciar.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        } else {

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

        if (id == R.id.denunciar) {
            denunciar = item;


            if (!buttomAddMarkerVisivel) {
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

        switch (id) {
            case R.id.nav_all_marker:

                showFragment(new MapsTerceiro(), "Todos Marcadores");
                setTitle("Todos Marcadores");
                findViewById(R.id.denunciar).setVisibility(View.VISIBLE);
                btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
                btnAddMarker.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_user_marker:
                showFragment(new MapsSegundo(), "Marcadores do Usuário");
                setTitle("Marcadores do Usuário");
                findViewById(R.id.denunciar).setVisibility(View.GONE);
                btnAddMarker = (FloatingActionButton) findViewById(R.id.fab);
                btnAddMarker.setVisibility(View.GONE);
                break;
            case R.id.nav_tutorial:
                tutorial();
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        Toast.makeText(getApplicationContext(), "Logout efetuado com sucesso!", Toast.LENGTH_SHORT).show();
        clearApplicationData();

        Intent intent = new Intent(MenuInicial.this, TelaInicial.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }

    private void showFragment(Fragment fragment, String name) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.container, fragment, name);

        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void notificaticarInteressados(Action action) {
        buttomAddMarkerVisivel = action.getButtomAddMakerClickado();
        if (!buttomAddMarkerVisivel) {
            btnAddMarker.setVisibility(View.GONE);
            btnCancelAddMarker.setVisibility(View.VISIBLE);

        } else {
            btnAddMarker.setVisibility(View.VISIBLE);
            btnCancelAddMarker.setVisibility(View.GONE);

        }


        if (vDenunciar != action.isReportNotSelected()) {
            Log.i("Teste", "Denunciar mudou..");
            changeDenunciar();

        }


    }

    /*Se fosse necessario mudar algo apos as permissões*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i("teste", "Permission called !!!");
        Log.i("teste", "Permission called  code " + requestCode + " mycode " + MY_PERMISSION_REQUEST_CODE);
        switch (requestCode) {

            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("teste", "permission terceiro accepted ");
                    permissionOk = true;
                    if (!menuStarted) {
                        menuStart();

                    }

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        Log.i("teste", "checkGpsPermission Rationale");
                        //     try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().

                        //
                        // User selected the Never Ask Again Option Change settings in app settings manually

                    }

                }
                break;

            default:
                Log.i("teste", "Permission neged!!!");

        }
    }


    public void tutorial() {
        TaskStackBuilder.create(getApplicationContext())
                .addNextIntentWithParentStack(new Intent(MenuInicial.this, MenuInicial.class))
                .addNextIntent(new Intent(MenuInicial.this, IntroActivity.class))
                .startActivities();
    }


    public void createPrefers(String packName, String name, boolean bol) {


        SharedPreferences myPreferences = getApplicationContext().getApplicationContext().getSharedPreferences(packName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = myPreferences.edit();
        edit.putBoolean(name, bol);
        edit.commit();


    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("teste", "In Stop main");


        if (!ForegroundService.IS_SERVICE_RUNNING) {
            Intent service = new Intent(MenuInicial.this, ForegroundService.class);
            service.setAction(ForegroundService.START);
            startService(service);
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("teste", "In Resume main");


        if (isMyServiceRunning(ForegroundService.class)) {


            Log.i("teste", "shutdown");
            Intent service = new Intent(MenuInicial.this, ForegroundService.class);
            service.setAction(ForegroundService.STOP);
            startService(service);


            Log.i("teste", "In Resume is running" + ForegroundService.IS_SERVICE_RUNNING);

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("teste", "In onDestroy Menuinicial");
        if (ForegroundService.IS_SERVICE_RUNNING) {
            Intent service = new Intent(MenuInicial.this, ForegroundService.class);
            service.setAction(ForegroundService.STOP);
            startService(service);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("teste", "---- onActivityResult main called :') ---- ");
        Intent intent = new Intent(MenuInicial.this, MenuInicial.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}




