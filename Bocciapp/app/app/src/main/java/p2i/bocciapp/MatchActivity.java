package p2i.bocciapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.net.*;

public class MatchActivity extends AppCompatActivity {

    public static final String TAG = "MyLogsMatch"; //this tag is only used to sort the logs I write from the ones the app writes by itself

    public static boolean ENABLE_MATCH = true;
    public static boolean ENABLE_TIMER = true;

    ImageView ivEnd;
    ImageView ivBox;
    ImageView ivN1;
    ImageView ivBox2;
    ImageView ivInstructs;

    TextView tvN1;
    TextView tvN2;

    Button btnBack;
    Button btnStart;
    Button btnLaunch;
    Button btnCall;
    Button btnStop;
    Button btnLaunchBig;

    ImageButton btnDown;
    ImageButton btnUp;
    ImageButton btnLeft;
    ImageButton btnRight;
    LinearLayout layoutButtons;

    ImageButton btnDownBig;
    ImageButton btnUpBig;
    ImageButton btnLeftBig;
    ImageButton btnRightBig;
    LinearLayout layoutButtonsBig;

    ImageButton btnIns;
    boolean ins;

    TextView tvBalls;
    LinearLayout layoutBalls;
    ImageView ivBall1;
    ImageView ivBall2;
    ImageView ivBall3;
    boolean[] balls;
    int countBalls;

    TextView tvTimer;
    long startTime = 0;
    int seconds;
    int minutes;
    long millis;
    long millisStartPause;
    long millisEndPause;
    long millisDiff;

    InetAddress host;
    static DatagramSocket UDPSocket;
    String serverMessage;

    View baseView;

    Handler handler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime - millisDiff;
            seconds = (int) (millis / 1000);
            minutes = seconds / 60;
            seconds = seconds % 60;

            if (minutes < 3) {
                tvTimer.setText(" " + String.format("%d:%02d", minutes, seconds) + " ");
            } else if (minutes < 100000 && ENABLE_TIMER) {
                endGame(true);
            }
            handler.postDelayed(this, 500);
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_match);

        //init of the views and their visibility at the beginning
        baseView = findViewById(R.id.constraintLayout);

        ins = true;

        ivEnd = (ImageView) findViewById(R.id.ivEndM);
        ivEnd.setVisibility(View.GONE);
        ivBox = (ImageView) findViewById(R.id.ivBox);
        ivN1 = (ImageView) findViewById(R.id.ivN1);

        tvN1 = (TextView) findViewById(R.id.tvN1);
        tvN2 = (TextView) findViewById(R.id.tvN2);

        btnBack = (Button) findViewById(R.id.btnBack);
        btnStart = (Button) findViewById(R.id.btnStart);
        tvTimer = (TextView) findViewById(R.id.tvTimer);

        ivBox2 = (ImageView) findViewById(R.id.ivBox2);
        ivInstructs = (ImageView) findViewById(R.id.ivInstructs);
        ivInstructs.setVisibility(View.GONE);
        ivBox2.setVisibility(View.GONE);

        btnCall = (Button) findViewById(R.id.btnCall);
        btnStop = (Button) findViewById(R.id.btnStop);

        btnLaunch = (Button) findViewById(R.id.btnLaunch);
        btnLaunch.setVisibility(View.GONE);
        btnLaunchBig = (Button) findViewById(R.id.btnLaunchBig);
        btnLaunchBig.setVisibility(View.GONE);

        tvBalls = (TextView) findViewById(R.id.tvBalls);
        tvBalls.setVisibility(View.GONE);

        layoutBalls = (LinearLayout) findViewById(R.id.layoutBalls);
        ivBall1 = (ImageView) findViewById(R.id.ivBall1);
        ivBall2 = (ImageView) findViewById(R.id.ivBall2);
        ivBall3 = (ImageView) findViewById(R.id.ivBall3);
        layoutBalls.setVisibility(View.GONE);

        layoutButtons = (LinearLayout) findViewById(R.id.layoutButtons);
        btnDown = (ImageButton) findViewById(R.id.btnDown);
        btnUp = (ImageButton) findViewById(R.id.btnUp);
        btnLeft = (ImageButton) findViewById(R.id.btnLeft);
        btnRight = (ImageButton) findViewById(R.id.btnRight);
        layoutButtons.setVisibility(View.GONE);

        layoutButtonsBig = (LinearLayout) findViewById(R.id.layoutButtonsBig);
        btnDownBig = (ImageButton) findViewById(R.id.btnDownBig);
        btnUpBig = (ImageButton) findViewById(R.id.btnUpBig);
        btnLeftBig = (ImageButton) findViewById(R.id.btnLeftBig);
        btnRightBig = (ImageButton) findViewById(R.id.btnRightBig);
        layoutButtonsBig.setVisibility(View.GONE);

        btnIns = (ImageButton) findViewById(R.id.btnIns);
        btnIns.setVisibility(View.GONE);

        balls = new boolean[3];
        countBalls = 0;

        //starts the UDP communication (socket) with the server specified in address
        try {
            UDPSocket = new DatagramSocket();
            host = InetAddress.getByName(MainActivity.address);
            UDPSend("open:match:");
            UDPReceive();
        } catch (Exception e) {
            Log.e(TAG, "error socket  " + e.getCause().toString());
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UDPSocket.close(); //closes the socket with the server
                finish(); //closes the activity
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("Le chrono a été mis en pause et la machine a été arretée." + "\n" + "\n" + "Appuie sur OK pour relancer le jeu.", "OK", "", R.style.DialogThemeAlert);
                startPause();
                UDPSend("stop:stop");
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("Le chrono a été mis en pause et la LED de la machine s'est allumée." + "\n" + "\n" + "Appuie sur OK pour relancer le jeu.", "OK", "", R.style.DialogThemeAlert);
                startPause();
                UDPSend("stop:call");
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sets all the views in visible/gone depending on whether we have to see them or not
                ivBox.setVisibility(View.GONE);
                ivN1.setVisibility(View.GONE);
                tvN1.setVisibility(View.GONE);
                tvN2.setVisibility(View.GONE);
                ivInstructs.setVisibility(View.VISIBLE);
                btnIns.setVisibility(View.VISIBLE);
                ivBox2.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
                btnLaunch.setVisibility(View.VISIBLE);
                layoutBalls.setVisibility(View.VISIBLE);
                tvBalls.setVisibility(View.VISIBLE);
                layoutButtons.setVisibility(View.VISIBLE);
                showDialog("Appuie sur suivant quand c'est à ton tour de jouer." + "\n" + "Le chrono se lancera automatiquement.", "", "Suivant", R.style.DialogTheme);
            }
        });

        btnIns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ins) {
                    ivInstructs.setVisibility(View.GONE);
                    ivBox2.setVisibility(View.GONE);
                    layoutButtons.setVisibility(View.GONE);
                    layoutButtonsBig.setVisibility(View.VISIBLE);
                    btnLaunchBig.setVisibility(View.VISIBLE);
                } else {
                    ivInstructs.setVisibility(View.VISIBLE);
                    ivBox2.setVisibility(View.VISIBLE);
                    layoutButtons.setVisibility(View.VISIBLE);
                    layoutButtonsBig.setVisibility(View.GONE);
                    btnLaunchBig.setVisibility(View.GONE);
                }
                ins = !ins;
            }
        });

        btnLaunch.setOnClickListener(view -> doBtnLaunch());

        btnLaunchBig.setOnClickListener(view -> doBtnLaunch());

        btnDown.setOnTouchListener((view, motionEvent) -> {
            doBtnDown((ImageButton) view, motionEvent);
            return false;
        });

        btnDownBig.setOnTouchListener((view, motionEvent) -> {
            doBtnDown((ImageButton) view, motionEvent);
            return false;
        });

        btnUp.setOnTouchListener((view, motionEvent) -> {
            doBtnUp((ImageButton) view, motionEvent);
            return false;
        });

        btnUpBig.setOnTouchListener((view, motionEvent) -> {
            doBtnUp((ImageButton) view, motionEvent);
            return false;
        });

        btnLeft.setOnTouchListener((view, motionEvent) -> {
            doBtnLeft((ImageButton) view, motionEvent);
            return false;
        });

        btnLeftBig.setOnTouchListener((view, motionEvent) -> {
            doBtnLeft((ImageButton) view, motionEvent);
            return false;
        });

        btnRight.setOnTouchListener((view, motionEvent) -> {
            doBtnRight((ImageButton) view, motionEvent);
            return false;
        });

        btnRightBig.setOnTouchListener((view, motionEvent) -> {
            doBtnRight((ImageButton) view, motionEvent);
            return false;
        });

    }

    /**
     * Pauses the timer
     */
    private void startPause() {
        handler.removeCallbacks(timerRunnable);
        millisStartPause = System.currentTimeMillis() - startTime;
    }

    /**
     * Unpauses the timer
     */
    private void endPause() {
        millisEndPause = System.currentTimeMillis() - startTime;
        millisDiff += millisEndPause - millisStartPause;
        handler.postDelayed(timerRunnable, 0);
    }

    /**
     * Checks if the game is supposed to be finished. Displays the ending screen if it is
     *
     * @param force can be set to true to force the end of the game even if all the balls haven't been thrown
     */
    private void endGame(boolean force) {
        if (countBalls >= balls.length || force) {
            ivInstructs.setVisibility(View.GONE);
            ivBox2.setVisibility(View.GONE);
            layoutBalls.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
            btnLaunch.setVisibility(View.GONE);
            layoutButtons.setVisibility(View.GONE);
            btnCall.setVisibility(View.GONE);
            tvBalls.setVisibility(View.GONE);
            layoutButtonsBig.setVisibility(View.GONE);
            btnIns.setVisibility(View.GONE);
            btnLaunchBig.setVisibility(View.GONE);
            ivEnd.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to execute when a down button is pressed
     *
     * @param view        button pressed
     * @param motionEvent MotionEvent of the press
     */
    public void doBtnDown(ImageButton view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setImageResource(R.drawable.arrow_down_clicked); //sets a different image when the button is clicked
            UDPSend("move:down"); //sends a message to the server
            UDPReceive();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.setImageResource(R.drawable.arrow_down); //sets the original image back when the button is unclicked
            UDPSend("move:stop:down");
        }
    }

    /**
     * Method to execute when a up button is pressed
     *
     * @param view        button pressed
     * @param motionEvent MotionEvent of the press
     */
    public void doBtnUp(ImageButton view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setImageResource(R.drawable.arrow_up_clicked);
            UDPSend("move:up");
            UDPReceive();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.setImageResource(R.drawable.arrow_up);
            UDPSend("move:stop:up");
        }
    }

    /**
     * Method to execute when a left button is pressed
     *
     * @param view        button pressed
     * @param motionEvent MotionEvent of the press
     */
    public void doBtnLeft(ImageButton view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setImageResource(R.drawable.arrow_left_clicked);
            UDPSend("move:left");
            UDPReceive();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.setImageResource(R.drawable.arrow_left);
            UDPSend("move:stop:left");
        }
    }

    /**
     * Method to execute when a right button is pressed
     *
     * @param view        button pressed
     * @param motionEvent MotionEvent of the press
     */
    public void doBtnRight(ImageButton view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setImageResource(R.drawable.arrow_right_clicked);
            UDPSend("move:righ");
            UDPReceive();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            view.setImageResource(R.drawable.arrow_right);
            UDPSend("move:stop:righ");
        }
    }

    /**
     * Method to execute when a launch button is pressed
     */
    public void doBtnLaunch() {
        UDPSend("laun:laun");
        //checks whether it can remove a ball from the array, and does it as well as updating the UI
        if (countBalls < balls.length) {
            balls[countBalls] = true;
            showToast("Lance ta balle !", Toast.LENGTH_SHORT);
            showDialog("Le chrono est mis en pause." + "\n" + "\n" + "Appuie sur suivant lorsque c'est à ton tour", "Suivant", "", R.style.DialogTheme);
            startPause();
            if (balls[0]) {
                ivBall3.setImageResource(R.drawable.boccia_ball_vide);
            }
            if (balls[1]) {
                ivBall2.setImageResource(R.drawable.boccia_ball_vide);
            }
            if (balls[2]) {
                ivBall1.setImageResource(R.drawable.boccia_ball_vide);
            }
            countBalls++;
        }
    }

    /**
     * Shows a toast on top of the app
     *
     * @param text   text to display in the toast
     * @param length length duration of the toast
     */
    private void showToast(String text, int length) {
        Toast toast = Toast.makeText(getApplicationContext(), text, length);
        toast.show();
    }

    /**
     * Shows a dialog on top of the app. The method does different things depending on which buttons are displayed
     *
     * @param text          text displayed in the dialog
     * @param textButtonPos text displayed for the positive button ; won't set a positive button if set to ""
     * @param textButtonNeg text displayed for the negative button ; won't set a negative button if set to ""
     * @param style         style of the dialog
     */
    private void showDialog(String text, String textButtonPos, String textButtonNeg, int style) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, style);
        dialog.setMessage(text);
        dialog.setCancelable(false); //set to false so the dialog does not disappear if you click anywhere outside

        if (!textButtonPos.equals("")) { //this part is used when the game needs to be paused. it unpauses the timer and checks if the game ended
            dialog.setPositiveButton(
                    textButtonPos,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            UDPSend("clos:stop");
                            dialog.cancel();
                            endPause();
                            endGame(false);
                        }
                    });
        }

        if (!textButtonNeg.equals("")) { //this part is only used when first launching the game
            dialog.setNegativeButton(
                    textButtonNeg,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            showToast("Le chrono est lancé !", Toast.LENGTH_SHORT);
                            dialog.cancel();

                            startTime = System.currentTimeMillis();
                            handler.postDelayed(timerRunnable, 0); //starts the timer for the first time
                        }
                    });
        }

        AlertDialog alert = dialog.create();
        alert.show();
    }

    /**
     * Sends a UDP message to a specified server. The server is specified in the MainActivity and the socket is open in the onCreate method.
     *
     * @param m message sent to the server via UDP protocol
     */
    public void UDPSend(String m) {
        if (ENABLE_MATCH) {
            final byte[] b = m.getBytes();
            Thread t = new Thread() {
                public void run() {
                    try {
                        DatagramPacket packet = new DatagramPacket(b, b.length, host, MainActivity.PORT);
                        UDPSocket.send(packet);
                        Log.d(TAG, "apres send");
                    } catch (Exception e) {
                        Log.e(TAG, "error send  " + e.getCause().toString());
                    }
                }
            };
            t.start();
        }
    }

    /**
     * Triggers the receiving of a UDP packet from the server via the Receive class
     */
    public void UDPReceive() {
        (new ReceiveMatch()).execute();
    }

    /**
     * Converts a received message from the server in a String, then computes it. This method is called by the Receive class.
     *
     * @param b byte array to convert back in String
     */
    public void fetchReceive(byte[] b) {
        serverMessage = new String(b);
        Log.i(TAG, "message reçu par le serveur : " + serverMessage);
        if (serverMessage.startsWith("open:")) {
            Log.i(TAG, "connecté");
        }
    }
}