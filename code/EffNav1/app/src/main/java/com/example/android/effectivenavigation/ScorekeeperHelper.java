package com.example.android.effectivenavigation;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Jango on 5/31/2015.
 */
public class ScorekeeperHelper {

    // class constants for scorekeeper section
    private final int SPORT_DEFAULT = 0;
    private final int SPORT_TENNIS = 1;
    private final int SPORT_BASKETBALL = 2;
    private final int SPORT_GOLF = 3;
//        private final int SPORT_SQUASH = 4;
//        private final int SPORT_BADMINTON = 5;
//        private final int SPORT_RACQUETBALL = 6;
//        private final int SPORT_TABLETENNIS = 7;
    private final int PLAYER_ONE = 1;
    private final int PLAYER_TWO = 2;
    private final int BEST_OF_FIVE = 3;
    private final int GAMES_TO_SET = 6;
    private final int DEFAULT_SCORE_TO_WIN = 21;
    private final int DEFAULT_SCORE_TO_WIN_BASKETBALL = 21;
    private final String[] tennisScore = new String[] {"0", "15", "30", "40", "Adv"};

    // initialize/declare variables
    private int sport = SPORT_DEFAULT;
    private int scoreToWin = DEFAULT_SCORE_TO_WIN;
    private int setsToWin = BEST_OF_FIVE;
    private int[] player1Score = new int[3];
    private int[] player2Score = new int[3];


    public ScorekeeperHelper() {
        sport = SPORT_DEFAULT;
        scoreToWin = DEFAULT_SCORE_TO_WIN;
        setsToWin = BEST_OF_FIVE;
        player1Score = new int[3];
        player2Score = new int[3];
    }
    public void incrementScore(int player, TextView player1, TextView player2, Context context) {
        int[] playerScore = new int[3];
        int[] opposingPlayerScore = new int[3];
        if (player == PLAYER_ONE) {
            playerScore = player1Score;
            opposingPlayerScore = player2Score;
        } else if (player == PLAYER_TWO) {
            playerScore = player2Score;
            opposingPlayerScore = player1Score;
        }
        playerScore[0]++;

        // tennis
        if (sport == SPORT_TENNIS) {
            Log.d("scorekeeper:", "Im in the tennis case");
            // after incrementing, if player1 is ad and player 2 is ad
            if (playerScore[0] == 4 && opposingPlayerScore[0] == 4) {
                // set both scores to 40
                playerScore[0] = 3;
                opposingPlayerScore[0] = 3;
            }

            // this is the case where player 1 wins the game
            if ((playerScore[0] >= tennisScore.length-1) && (playerScore[0]-opposingPlayerScore[0] >= 2)) {
                playerScore[0] = 0;
                opposingPlayerScore[0] = 0;
                playerScore[1]++;

                // this is the case where player 1 wins the set
                if ((playerScore[1] >= GAMES_TO_SET) && (playerScore[1]-opposingPlayerScore[1] >= 2)) {
                    playerScore[1] = 0;
                    opposingPlayerScore[1] = 0;
                    playerScore[2]++;
                    if (playerScore[2] == setsToWin) {
                        Toast.makeText(context, "Player " + player + " Won!!", Toast.LENGTH_LONG).show();

                        playerScore = new int[3];
                        opposingPlayerScore = new int[3];
                    }
                }
            }
            // basketball, golf
        } else {
            // when player wins in the default case, reset score and display text: Player x Won!!
            if (playerScore[0] == scoreToWin) {
                Toast.makeText(context, "Player " + player + " Won!!", Toast.LENGTH_LONG).show();
                playerScore = new int[3];
                opposingPlayerScore = new int[3];
            }
        }

        if (player == PLAYER_ONE) {
            player1Score = playerScore;
            player2Score = opposingPlayerScore;

        } else if (player == PLAYER_TWO) {
            player2Score = playerScore;
            player1Score = opposingPlayerScore;
        }
        updateScores(player1, player2);
    }

    public void decrementScore(int player, TextView player1, TextView player2) {

        int[] playerScore = new int[3];
        int[] opposingPlayerScore = new int[3];
        if (player == PLAYER_ONE) {
            playerScore = player1Score;
            opposingPlayerScore = player2Score;


        } else if (player == PLAYER_TWO) {
            playerScore = player2Score;
            opposingPlayerScore = player1Score;

        }
        // only do something if the score is not the smallest possible score 0
        if (playerScore[0] != 0 || playerScore[1] != 0 || playerScore[2] != 0) {
            playerScore[0]--;

            // tennis
            if (sport == SPORT_TENNIS) {
                Log.d("scorekeeper:", "Im in the tennis case");
                // after incrementing, if player1 is ad and player 2 is ad

                if (playerScore[0] == -1) {
                    // set both scores to 40
                    playerScore[1]--;
                    playerScore[0] = tennisScore.length - 1; // set the score to

                }
                if (playerScore[1] == -1) {
                    playerScore[2]--;
                    playerScore[1] = GAMES_TO_SET - 1;
                }

                if (player == PLAYER_ONE) {
                    player1Score = playerScore;
                    player2Score = opposingPlayerScore;


                } else if (player == PLAYER_TWO) {
                    player2Score = playerScore;
                    player1Score = opposingPlayerScore;
                }
            }
            updateScores(player1, player2);
        }
    }



    public void resetScores(TextView player1, TextView player2) {
        player1Score = new int[3];
        player2Score = new int[3];
        updateScores(player1, player2);

    }
    public void changeScoreToWin(int scoreToWin) { this.scoreToWin = scoreToWin; }

    public void changeSportToTennis() { changeSport(SPORT_TENNIS);}

    public void changeSportToBasketball() { changeSport(SPORT_BASKETBALL); }

    public void changeSportToGolf() { changeSport(SPORT_GOLF); }

    public void changeSportToDefault() { changeSport(SPORT_DEFAULT); }

    private void changeSport(int sport) { this.sport = sport; }

    private void updateScores(TextView player1, TextView player2) {
        if (sport == SPORT_TENNIS) {
            printTennisScoreToTextView(player1, Integer.toString(player1Score[2]), Integer.toString(player1Score[1]), tennisScore[player1Score[0]]);
            printTennisScoreToTextView(player2, Integer.toString(player2Score[2]), Integer.toString(player2Score[1]), tennisScore[player2Score[0]]);
        } else {
            printScoreToTextView(player1, Integer.toString(player1Score[0]));
            printScoreToTextView(player2, Integer.toString(player2Score[0]));
        }
    }

    // SPORT: TENNIS
    // Prints scores to given TextView v, set score, game score, and score
    private void printTennisScoreToTextView(TextView v, String setScore, String gameScore, String score) {
        v.setText("Set: " + setScore + "\nGame: " +  gameScore +
                "\nScore: " + score);
    }

    // SPORT: BASKETBALL, GOLF
    // Prints scores to given TextView v, set score, game score, and score
    private void printScoreToTextView(TextView v, String score) { v.setText(score); }


}
