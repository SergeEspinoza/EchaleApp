package bit01.com.mx.echale.models;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bit01.com.mx.echale.R;
import bit01.com.mx.echale.ui.PartidosRecyclerViewActvity;
import bit01.com.mx.echale.utils.Constants;
import bit01.com.mx.echale.utils.IabBroadcastReceiver;
import bit01.com.mx.echale.utils.IabHelper;
import bit01.com.mx.echale.utils.IabResult;
import bit01.com.mx.echale.utils.Inventory;
import bit01.com.mx.echale.utils.Purchase;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;


public class ApuestaActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener{

    // Link de los elementos de la vista a la actividad
    @BindView(R.id.momioLocal)
    TextView tvMomioLocal;

    @BindView(R.id.momioEmpate)
    TextView tvMomioEmpate;

    @BindView(R.id.momioVisita)
    TextView tvMomioVisita;

    @BindView(R.id.btnApuesta)
    Button btnApuesta;

    @BindView(R.id.montoApuesta)
    EditText montoApuesta;

    @BindView(R.id.tvPosibleGanancia)
    TextView tvPosibleGanancia;

    @BindView(R.id.localTeamNameApuesta)
    TextView localName;

    @BindView(R.id.awayTeamNameApuesta)
    TextView awayName;

    @BindView(R.id.localTeamImage)
    CircleImageView logoLocal;

    @BindView(R.id.awayTeamImage)
    CircleImageView logoVisita;

    @BindView(R.id.tvEmpate)
    TextView tvEmpate;

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;

    SweetAlertDialog pDialog;

    User usuarioActual;
    int monedasActuales;

    Boolean yaSelecciono = false, alertaSeleccionMostrada=false;
    String evento = "";
    String apostadoresType="";
    String bolsaType="";
    String equipo = "";
    String partidoID;
    int intPartidoId;

    String urlLocal;
    String urlVisita;

    String mFechaPartido;
    float mtotalEvento;
    int intMontoApuesta;
    String mGananciaProbable;

    public static final String TAG ="LogVerijas";

    boolean terminoMetodoConfirmacionApoostado = false;
    boolean yaApostoLocal = false;
    boolean yaApostoEmpate = false;
    boolean yaApostoVisita = false;

    IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    FirebaseAuth mAuth;
    String userUid;

    private Menu menu;

    boolean guiaYaMostrada = false;

    private List<Partido> mPartidos = new ArrayList<>();
    private long mBolsaTotal, mBolsaLocal, mBolsaEmpate,mBolsaVisita;
    private float mFloatBolsaTotal, mFloatBolsaLocal, mFloatBolsaEmpate, mFloatBolsaVisita;

    public boolean yaAposto(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/partidosActuales/partido"+intPartidoId+"/apuestas/empate/apostadoresEmpate");

        if(!yaApostoLocal && !yaApostoVisita && !yaApostoEmpate){

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild(userUid)){
                        yaApostoEmpate= true;
                        Toast.makeText(ApuestaActivity.this, "soy bien pinshi lento empate", Toast.LENGTH_SHORT).show();


                    }else{

                        yaApostoEmpate=false;

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(!yaApostoLocal && !yaApostoVisita && !yaApostoEmpate){

            DatabaseReference myRef2 = database.getReference("/partidosActuales/partido"+intPartidoId+"/apuestas/local/apostadoresLocal");
            myRef2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild(userUid)){
                        Toast.makeText(ApuestaActivity.this, "soy bien pinshi lento Local", Toast.LENGTH_SHORT).show();

                        yaApostoLocal= true;

                    }else{

                        yaApostoLocal=false;

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(!yaApostoLocal && !yaApostoVisita && !yaApostoEmpate){

            DatabaseReference myRef3 = database.getReference("/partidosActuales/partido"+intPartidoId+"/apuestas/visita/apostadoresVisita");
            myRef3.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Toast.makeText(ApuestaActivity.this, "yaaaaa", Toast.LENGTH_SHORT).show();
                    if(dataSnapshot.hasChild(userUid)){
                        yaApostoVisita= true;
                        Toast.makeText(ApuestaActivity.this, "soy bien pinshi lento visita", Toast.LENGTH_SHORT).show();
                    }else{
                        yaApostoVisita=false;
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        terminoMetodoConfirmacionApoostado = true;
        return yaApostoLocal || yaApostoEmpate || yaApostoVisita;
         //TODO Arreglar la lentitud cawn

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apuesta_activity);
        ButterKnife.bind(this);

        String base64EncodedPublicKey = "123456";

        Log.d("logVerijas", "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d("LogVerijas", "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d("LogVerijas", "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Toast.makeText(ApuestaActivity.this, "Problem setting up in-app billing: " + result, Toast.LENGTH_SHORT).show();
                    //complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(ApuestaActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d("LogVerijas", "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Toast.makeText(ApuestaActivity.this, "Error querying inventory. Another async operation in progress.", Toast.LENGTH_SHORT).show();
                    //complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarApuestaActivity);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Échale!");
        }



        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();

        Bundle extras = getIntent().getExtras();
        if(extras != null){

            localName.setText(extras.getString(Constants.TAG_LOCAL));
            awayName.setText(extras.getString(Constants.TAG_AWAY));

            partidoID = extras.getString(Constants.TAG_PARTIDO_ID);
            intPartidoId = calcularIDPartido(partidoID);


            if(!extras.getString(Constants.TAG_AWAY_IMAGE).isEmpty()) {
                Picasso.with(ApuestaActivity.this)
                        .load(extras.getString(Constants.TAG_AWAY_IMAGE))
                        .resize(100,100)
                        .into(logoVisita);

                urlVisita = extras.getString(Constants.TAG_AWAY_IMAGE);
            }

            if(!extras.getString(Constants.TAG_LOCAL_IMAGE).isEmpty()) {
                Picasso.with(ApuestaActivity.this)
                        .load(extras.getString(Constants.TAG_LOCAL_IMAGE))
                        .resize(100,100)
                        .into(logoLocal);

                urlLocal = extras.getString(Constants.TAG_LOCAL_IMAGE);
            }
            mFechaPartido = extras.getString(Constants.TAG_DATE);

        }

        yaAposto();
        traerPartidos();
        traerDatosUsuario();
        if(yaApostoLocal || yaApostoEmpate || yaApostoVisita){
            //TODO es super lento este pedo y no lo jala


            //btnApuesta.setClickable(false);

        }

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.TAG_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        guiaYaMostrada = sharedPreferences.getBoolean(Constants.TAG_GUIA_APUESTA, false);

        if(!guiaYaMostrada){

            mostrarGuia();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.TAG_GUIA_APUESTA, true);
            editor.commit();


        }

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Toast.makeText(ApuestaActivity.this, "Failed to query inventory: " + result, Toast.LENGTH_SHORT).show();
                //complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            /*
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));*/


            /*
            // First find out which subscription is auto renewing
            Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
            Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);
            if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                mInfiniteGasSku = "";
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly))
                    || (gasYearly != null && verifyDeveloperPayload(gasYearly));
            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }*/

            //updateUi();
            //setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_apuesta_activity, menu);
        this.menu = menu;

        MenuItem coinsItem = menu.findItem(R.id.toolbar_coins_indicator);
        coinsItem.setTitle(monedasActuales+"");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(ApuestaActivity.this, PartidosRecyclerViewActvity.class));
                break;
            case R.id.toolbar_coins_icon:
            case R.id.toolbar_coins_indicator:

                pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Más monedas!")
                        .setContentText("Quieres conseguir más monedas?")
                        .setCancelText("No, cancelar!")
                        .setConfirmText("Sí, ¡échale!")
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {

                                Toast.makeText(ApuestaActivity.this, "Cancelaste la compra", Toast.LENGTH_SHORT).show();
                                sDialog.cancel();

                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                //TODO agregar in app billings
                                Toast.makeText(ApuestaActivity.this, "Aceptaste la compra cawn!", Toast.LENGTH_SHORT).show();
                                sweetAlertDialog.dismiss();

                            }
                        });
                pDialog.show();
                break;
        }
        return true;
    }

    public String cadenaConfirmacionApuesta(){

        if(evento.equalsIgnoreCase("empate")){

            return "el empate";

        }else{

            return "la victoria de "+equipo;

        }

    }


    public void cambioSeleccion(){

        switch(evento){

            case "local":
                mtotalEvento = mFloatBolsaLocal;
                break;
            case "visita":
                mtotalEvento= mFloatBolsaVisita;
                break;
            case "empate":
                mtotalEvento = mFloatBolsaEmpate;
                break;
            default:
                mtotalEvento=0;
                break;

        }

        intMontoApuesta = Integer.parseInt(montoApuesta.getText().toString());
        mGananciaProbable = String.format("%.1f", ((intMontoApuesta/(mtotalEvento+intMontoApuesta))*(mBolsaTotal+intMontoApuesta)));
        tvPosibleGanancia.setText("$"+mGananciaProbable);

    }


    @OnTextChanged(R.id.montoApuesta)
    public void montoCambiado(Editable editable){

        if(!editable.toString().isEmpty()){

            if(!yaSelecciono){

                if(!alertaSeleccionMostrada){

                    alertaSeleccionMostrada = true;
                    Toast.makeText(this, "Selecciona un equipo para ver tu posible ganancia", Toast.LENGTH_LONG).show();

                }

            }else{

                switch(evento){

                    case "local":
                        mtotalEvento = mFloatBolsaLocal;
                        break;
                    case "visita":
                        mtotalEvento= mFloatBolsaVisita;
                        break;
                    case "empate":
                        mtotalEvento = mFloatBolsaEmpate;
                        break;
                    default:
                        mtotalEvento=0;
                        break;

                }

                intMontoApuesta = Integer.parseInt(editable.toString());
                mGananciaProbable = String.format("%.1f", ((intMontoApuesta/(mtotalEvento+intMontoApuesta))*(mBolsaTotal+intMontoApuesta)));
                tvPosibleGanancia.setText("$"+mGananciaProbable);

            }


        }else{

            tvPosibleGanancia.setText("$0");

        }

    }

    @OnClick(R.id.localTeamImage)
    public void clickLocal(){

        equipo = localName.getText().toString();
        evento = "local";
        logoLocal.setBorderWidth(25);
        logoLocal.setBorderColor(getResources().getColor(R.color.colorAccent));
        logoVisita.setBorderWidth(0);
        logoVisita.setBorderColor(getResources().getColor(R.color.colorPrimaryText));
        tvEmpate.setBackgroundColor(getResources().getColor(R.color.colorTransparente));
        yaSelecciono = true;

        if(!montoApuesta.getText().toString().isEmpty()){
            cambioSeleccion();
        }else{

            Toast.makeText(this, "Ahora ingresa una apuesta", Toast.LENGTH_SHORT).show();

        }

    }

    @OnClick(R.id.awayTeamImage)
    public void clickAway(){

        equipo = awayName.getText().toString();
        evento = "visita";
        logoLocal.setBorderWidth(0);
        logoLocal.setBorderColor(getResources().getColor(R.color.colorPrimaryText));
        logoVisita.setBorderWidth(25);
        logoVisita.setBorderColor(getResources().getColor(R.color.colorAccent));
        tvEmpate.setBackgroundColor(getResources().getColor(R.color.colorTransparente));
        yaSelecciono = true;

        if(!montoApuesta.getText().toString().isEmpty()){
            cambioSeleccion();
        }else{
            Toast.makeText(this, "Ahora ingresa una apuesta", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.tvEmpate)
    public void clickEmpate(){

        equipo = "";
        evento = "empate";
        logoLocal.setBorderWidth(0);
        logoLocal.setBorderColor(getResources().getColor(R.color.colorPrimaryText));
        logoVisita.setBorderWidth(0);
        logoVisita.setBorderColor(getResources().getColor(R.color.colorPrimaryText));
        tvEmpate.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        yaSelecciono = true;

        if(!montoApuesta.getText().toString().isEmpty()){
            cambioSeleccion();
        }else{
            Toast.makeText(this, "Ahora ingresa una apuesta", Toast.LENGTH_SHORT).show();
        }

    }

    public void traerPartidos(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/");

        final List<Partido> partidos = new ArrayList<>();


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Toast.makeText(ApuestaActivity.this, "Partidos actualizados", Toast.LENGTH_SHORT).show();

                partidos.clear();

                Iterable<DataSnapshot> children = dataSnapshot.child("partidosActuales").getChildren();
                for(DataSnapshot child : children){

                    partidos.add(child.getValue(Partido.class));

                }

                mPartidos = partidos;

                updateApuestas(mPartidos);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void traerDatosUsuario(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/users/"+userUid);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                usuarioActual = dataSnapshot.getValue(User.class);
                monedasActuales = usuarioActual.getMonedas();

                Toast.makeText(ApuestaActivity.this, "Usuario Actualizado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void mostrarGuia(){

        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(

                        TapTarget.forView(logoLocal,"Equipos","Para seleccionar tu favorito presiona sobre él. También puedes presionar empate.")
                                .cancelable(false)
                                .id(1),

                        TapTarget.forView(tvMomioLocal, "Distribución de las apuestas", "Aquí puedes ver la porción del dinero que se ha destinado a cada equipo.")
                                .cancelable(false)
                                .id(2),

                        TapTarget.forView(montoApuesta, "Tu apuesta", "Aquí deberás ingresar el monto que deseas apostar.")
                                .cancelable(false)
                                .id(3),

                        TapTarget.forView(tvPosibleGanancia, "Posible Ganancia", "Acá podrás ver lo que puedes ganar hasta el momento en caso que ganes la apuesta.")
                                .cancelable(false)
                                .id(4),

                        TapTarget.forView(btnApuesta, "¡Échale!", "Por último presiona el botón, acepta y échale!")
                                .cancelable(false)
                                .id(5)



                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {

                        Toast.makeText(ApuestaActivity.this, "Ahora has tu primer apuesta", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                        Toast.makeText(ApuestaActivity.this, "La regaste cawn!", Toast.LENGTH_SHORT).show();

                    }
                });

        sequence.start();

    }

    public int calcularIDPartido(String string){

        return Integer.parseInt(string.substring(1));

    }

    private void updateApuestas(List<Partido> partidos) {

        Partido partidoActual = partidos.get(calcularIDPartido(partidoID)-1);

        Map<String, Object> apuestasPartidoActual = partidoActual.getApuestas();

        mBolsaTotal = (long) apuestasPartidoActual.get("bolsaTotalPartido");

        Map<String, Object> apuestasLocal = (Map<String, Object>) apuestasPartidoActual.get("local");
        mBolsaLocal = (long) apuestasLocal.get("bolsaLocal");

        Map<String, Object> apuestasEmpate = (Map<String, Object>) apuestasPartidoActual.get("empate");
        mBolsaEmpate = (long) apuestasEmpate.get("bolsaEmpate");

        Map<String, Object> apuestasVista = (Map<String, Object>) apuestasPartidoActual.get("visita");
        mBolsaVisita = (long) apuestasVista.get("bolsaVisita");


        mFloatBolsaEmpate = mBolsaEmpate;
        mFloatBolsaLocal = mBolsaLocal;
        mFloatBolsaTotal = mBolsaTotal;
        mFloatBolsaVisita = mBolsaVisita;

        tvMomioLocal.setText(String.format("%.1f", ((mFloatBolsaLocal/mFloatBolsaTotal)*100.0))+"%");
        tvMomioEmpate.setText(String.format("%.1f", ((mFloatBolsaEmpate/mFloatBolsaTotal)*100.0))+"%");
        tvMomioVisita.setText(String.format("%.1f", ((mFloatBolsaVisita/mFloatBolsaTotal)*100.0))+"%");

        if(!montoApuesta.getText().toString().isEmpty() && yaSelecciono){

            cambioSeleccion();

        }

    }

    public void postData() {

        switch(evento){

            case "local":
                apostadoresType = "apostadoresLocal";
                bolsaType = "bolsaLocal";
                break;
            case "visita":
                apostadoresType = "apostadoresVisita";
                bolsaType = "bolsaVisita";
                break;
            case "empate":
                apostadoresType = "apostadoresEmpate";
                bolsaType = "bolsaEmpate";
                break;
            default:
                apostadoresType="";
                bolsaType="";
                break;

        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef= database.getReference("/partidosActuales/partido"+intPartidoId+"/apuestas/"+evento+"/"+bolsaType);
        myRef.setValue((long)(mtotalEvento+intMontoApuesta));

        myRef = database.getReference("/partidosActuales/partido"+intPartidoId+"/apuestas/bolsaTotalPartido");
        myRef.setValue(mBolsaTotal+intMontoApuesta);

        myRef = database.getReference("/partidosActuales/partido"+intPartidoId+"/apuestas/"+evento+"/"+apostadoresType+"/"+userUid);
        myRef.setValue(new Apostador(intMontoApuesta));

        myRef = database.getReference("/users/"+userUid+"/monedas");
        myRef.setValue(monedasActuales-intMontoApuesta);

        myRef = database.getReference("/users/"+userUid+"/historial/p"+intPartidoId);
        myRef.setValue(new Historial(evento, (long)intMontoApuesta, localName.getText().toString(), urlLocal, awayName.getText().toString(), urlVisita, mFechaPartido));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @OnClick(R.id.btnApuesta)
    public void apostar(){

        if(!(yaApostoLocal || yaApostoEmpate || yaApostoVisita)){ //TODO Debe de ir si ya aposto, de todos modos se deberia de deshabilitar el boton

            if(!montoApuesta.getText().toString().isEmpty() && yaSelecciono && intMontoApuesta>0 && intMontoApuesta<=monedasActuales){

                String monto = montoApuesta.getText().toString();

                pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Estás seguro?")
                        .setContentText("Estás apostando $"+monto+" por "+cadenaConfirmacionApuesta()+"!")
                        .setCancelText("No, cancelar!")
                        .setConfirmText("Sí, ¡échale!")
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {

                                Toast.makeText(ApuestaActivity.this, "Cancelaste", Toast.LENGTH_SHORT).show();
                                sDialog.cancel();

                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                Toast.makeText(ApuestaActivity.this, "Lo aceptaste cawn!", Toast.LENGTH_SHORT).show();
                                sweetAlertDialog.dismiss();

                                postData();
                                SweetAlertDialog pDialog2 = new SweetAlertDialog(ApuestaActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Perfecto!")
                                        .setContentText("Apuesta hecha con éxito.")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                startActivity(new Intent(ApuestaActivity.this,PartidosRecyclerViewActvity.class));

                                            }
                                        });
                                pDialog2.show();


                            }
                        });

                pDialog.show();

            }else{

                if(montoApuesta.getText().toString().isEmpty()){

                    Toast.makeText(this, "Ingresa un monto a apostar", Toast.LENGTH_SHORT).show();

                }else if(!yaSelecciono){

                    Toast.makeText(this, "Selecciona un evento (Presiona sobre un equipo)", Toast.LENGTH_SHORT).show();

                }else if(intMontoApuesta <=0){

                    Toast.makeText(this, "La apuesta debe de ser mayor a cero", Toast.LENGTH_SHORT).show();

                } else if(intMontoApuesta > monedasActuales){

                    pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Sin Monedas Suficientes")
                            .setContentText("Quieres conseguir más monedas?")
                            .setCancelText("No, cancelar!")
                            .setConfirmText("Sí, ¡échale!")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {

                                    Toast.makeText(ApuestaActivity.this, "Cancelaste la compra", Toast.LENGTH_SHORT).show();
                                    sDialog.cancel();

                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {

                                    //TODO agregar in app billings
                                    Toast.makeText(ApuestaActivity.this, "Aceptaste la compra cawn!", Toast.LENGTH_SHORT).show();
                                    sweetAlertDialog.dismiss();

                                }
                            });
                    pDialog.show();

                }

            }

        }else{

            pDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Parece que ya apostaste...")
                    .setContentText("No podrás apostar de nuevo en este partido aunque puedes ver la información ");
            pDialog.show();

        }

    }

    @Override
    public void receivedBroadcast() {

    }
}
