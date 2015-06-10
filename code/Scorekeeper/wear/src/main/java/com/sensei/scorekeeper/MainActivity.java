package com.sensei.scorekeeper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ScorekeeperHelper myScorekeeper;
    private TextView player1, player2, reset, popupMenu;
    private final int DEFAULT_SCORE_TO_WIN = 21;
    private final int DEFAULT_SCORE_TO_WIN_BASKETBALL = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        myScorekeeper = new ScorekeeperHelper();
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                player1 = (TextView) stub.findViewById(R.id.plus);
                player2 = (TextView) stub.findViewById(R.id.minus);
                reset = (TextView) stub.findViewById(R.id.reset);
                popupMenu = (TextView) stub.findViewById(R.id.popup);

                /* onclick on plus and minus buttons increments value */
                player1.setOnClickListener(new View.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void onClick(View v) {
                        myScorekeeper.incrementScore(1, player1, player2, getApplicationContext());
                    }
                });

                player2.setOnClickListener(new View.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void onClick(View v) {
                        myScorekeeper.incrementScore(2, player1, player2, getApplicationContext());

                    }

                });

                /* longclick on plus and minus buttons decrement value */
                player1.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        myScorekeeper.decrementScore(1, player1, player2);
                        return true;
                    }

                });
                player2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        myScorekeeper.decrementScore(2, player1, player2);
                        return true;
                    }
                });

                /* reset button */
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myScorekeeper.resetScores(player1, player2);
                    }
                });

                popupMenu.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        PopupMenu popup = new PopupMenu(MainActivity.this, v);
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.popup_scorekeeper, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                switch (item.getItemId()) {
                                    case R.id.popup_scorekeeper_tennis:
                                        myScorekeeper.changeSportToTennis();
                                        // change sets to win based on what user inputs
                                        // setsToWin = {user_input}
                                        player1.setBackground(getResources().getDrawable((R.drawable.tennisa)));
                                        player2.setBackground(getResources().getDrawable((R.drawable.tennisb)));
                                        myScorekeeper.resetScores(player1, player2);
                                        player1.setTextSize(10);
                                        player2.setTextSize(10);
                                        return true;
                                    case R.id.popup_scorekeeper_basketball:
                                        myScorekeeper.changeSportToBasketball();
                                        myScorekeeper.changeScoreToWin(DEFAULT_SCORE_TO_WIN_BASKETBALL);
                                        player1.setBackground(getResources().getDrawable((R.drawable.basketballa)));
                                        player2.setBackground(getResources().getDrawable((R.drawable.basketballb)));
                                        player1.setTextSize(20);
                                        player2.setTextSize(20);
                                        myScorekeeper.resetScores(player1, player2);
                                        return true;
                                    case R.id.popup_scorekeeper_golf:
                                        myScorekeeper.changeSportToGolf();
                                        myScorekeeper.changeScoreToWin(-1); // never allow them to win
                                        player1.setBackground(getResources().getDrawable((R.drawable.golfa)));
                                        player2.setBackground(getResources().getDrawable((R.drawable.golfb)));
                                        myScorekeeper.resetScores(player1, player2);
                                        player1.setTextSize(20);
                                        player2.setTextSize(20);
                                        return true;

                                    default:
                                        myScorekeeper.changeSportToDefault();
                                        myScorekeeper.changeScoreToWin(DEFAULT_SCORE_TO_WIN);
                                        player1.setBackground(getResources().getDrawable((R.drawable.greencircle)));
                                        player2.setBackground(getResources().getDrawable((R.drawable.redcircle)));
                                        myScorekeeper.resetScores(player1, player2);
                                        player1.setTextSize(20);
                                        player2.setTextSize(20);
                                        return true;
                                }
                            }
                        });

                        popup.show();
                    }
                });

            }
        });


    }
}
