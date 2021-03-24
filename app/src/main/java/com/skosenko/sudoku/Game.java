package com.skosenko.sudoku;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.skosenko.sudoku.fragments.CellFragment;
import com.skosenko.sudoku.fragments.Numbers;
import com.skosenko.sudoku.model.Board;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Game extends AppCompatActivity implements CellFragment.OnFragmentInteractionListener, Numbers.OnNumberInteractionListener {
    private static final String TAG = "Sudoku";
    private TextView clickedCell;
    private int clickedGroup;
    private int clickedCellId;
    private Board startBoard;
    private Board currentBoard;
    private static boolean isEnd=false;
    private static final String FILE_NAME="continue.txt";
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    public static final String KEY_DIFFICULTY = "org.example.sudoku.difficulty";

    protected static final int DIFFICULTY_CONTINUE = -1;

    private int cellFragments[] = new int[]{R.id.cellFragment, R.id.cellFragment2, R.id.cellFragment3, R.id.cellFragment4,
            R.id.cellFragment5, R.id.cellFragment6, R.id.cellFragment7, R.id.cellFragment8, R.id.cellFragment9};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        int difficulty = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
        ArrayList<Board> boards = readGameBoards(difficulty);
        startBoard = chooseRandomBoard(boards);
        currentBoard = new Board();
        currentBoard.copyValues(startBoard.getGameCells());

        for (int i = 1; i < 10; i++) {
            CellFragment thisCellGroupFragment = (CellFragment) getSupportFragmentManager().findFragmentById(cellFragments[i - 1]);
            thisCellGroupFragment.setGroupId(i);
        }

        //Appear all values from the current board
        CellFragment tempCellGroupFragment;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int column = j / 3;
                int row = i / 3;

                int fragmentNumber = (row * 3) + column;
                tempCellGroupFragment = (CellFragment) getSupportFragmentManager().findFragmentById(cellFragments[fragmentNumber]);
                int groupColumn = j % 3;
                int groupRow = i % 3;

                int groupPosition = (groupRow * 3) + groupColumn;
                int currentValue = currentBoard.getValue(i, j);

                if (currentValue != 0) {
                    tempCellGroupFragment.setValue(groupPosition, currentValue);
                }
            }
        }
        Log.i(TAG, "Board created");

        getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
    }

    private ArrayList<Board> getSavedBoard() {
        ArrayList<Board> boards = new ArrayList<>();
        try {
            InputStream inputStream =openFileInput(FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    Board board = new Board();
                    // read all lines in the board
                    for (int i = 0; i < 9; i++) {
                        String rowCells[] = line.split(" ");
                        for (int j = 0; j < 9; j++) {
                            if (rowCells[j].equals("-")) {
                                board.setValue(i, j, 0);
                            } else {
                                board.setValue(i, j, Integer.parseInt(rowCells[j]));
                            }
                        }
                        line = bufferedReader.readLine();
                    }
                    boards.add(board);
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return boards;
    }

    private ArrayList<Board> readGameBoards(int difficulty) {
        ArrayList<Board> boards = new ArrayList<>();
        int fileId=-1;
        switch (difficulty) {
            case DIFFICULTY_EASY:
                fileId=R.raw.easy;
                break;
            case DIFFICULTY_MEDIUM:
                fileId=R.raw.medium;
                break;
            case DIFFICULTY_HARD:
                fileId=R.raw.hard;
                break;
            default:
                return getSavedBoard();
        }

        if(fileId!=-1) {
            InputStream inputStream = getResources().openRawResource(fileId);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    Board board = new Board();
                    // read all lines in the board
                    for (int i = 0; i < 9; i++) {
                        String rowCells[] = line.split(" ");
                        for (int j = 0; j < 9; j++) {
                            if (rowCells[j].equals("-")) {
                                board.setValue(i, j, 0);
                            } else {
                                board.setValue(i, j, Integer.parseInt(rowCells[j]));
                            }
                        }
                        line = bufferedReader.readLine();
                    }
                    boards.add(board);
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        Log.i(TAG, "Board war read from file");
        return boards;
    }

    private Board chooseRandomBoard(ArrayList<Board> boards) {
        int randomNumber = (int) (Math.random() * boards.size());
        return boards.get(randomNumber);
    }

    private boolean isStartPiece(int group, int cell) {
        int row = ((group - 1) / 3) * 3 + (cell / 3);
        int column = ((group - 1) % 3) * 3 + ((cell) % 3);
        return startBoard.getValue(row, column) != 0;
    }

    private boolean checkAllGroups() {
        for (int i = 0; i < 9; i++) {
            CellFragment thisCellGroupFragment = (CellFragment) getSupportFragmentManager().findFragmentById(cellFragments[i]);
            if (!thisCellGroupFragment.checkGroupCorrect()) {
                return false;
            }
        }
        return true;
    }

    public void onCheckBoardButtonClicked(View view) {
        Log.i(TAG, "Checking whether board is correct");
        currentBoard.isBoardCorrect();
        if (checkAllGroups() && currentBoard.isBoardCorrect()) {
            Toast.makeText(this, getString(R.string.board_correct), Toast.LENGTH_SHORT).show();
            isEnd=true;
        } else {
            Toast.makeText(this, getString(R.string.board_incorrect), Toast.LENGTH_SHORT).show();
        }
    }

    public void onGoBackButtonClicked(View view) {
        if (!isEnd) {
            try {
                OutputStream outputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                try {
                    int[][] gameCells = currentBoard.getGameCells();
                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            //if usual
                            if (gameCells[i][j] == 0) {
                                writer.write("-");
                            } else {
                                writer.write(String.valueOf(gameCells[i][j]));
                            }

                            if (j != 8)
                                writer.write(" ");
                        }
                        writer.newLine();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    writer.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Судоку сохранено", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onFragmentInteraction(int groupId, int cellId, View view) {
        Log.i(TAG, "Clicked group " + groupId + ", cell " + cellId);

        if (!isStartPiece(groupId, cellId)) {
            //добавить очистку предыдущей клетки
            if (clickedCell != null) {
                clickedCell.setBackground(getResources().getDrawable(R.drawable.table_border_cell));
            }

            clickedCell = (TextView) view;
            clickedGroup = groupId;
            clickedCellId = cellId;
            clickedCell.setBackground(getResources().getDrawable(R.drawable.table_border_cell_unsure));

        } else {
//            Toast.makeText(this, getString(R.string.start_piece_error), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onFragmentInteraction(String value) {
        if (clickedCell != null) {
            CellFragment emptyFragment = (CellFragment) getSupportFragmentManager().findFragmentById(cellFragments[clickedGroup - 1]);
            if (value.equals("Clear")) {
                emptyFragment.setValue(clickedCellId, 0);
                value="0";
            } else {
                emptyFragment.setValue(clickedCellId, Integer.parseInt(value));
            }


            int row = ((clickedGroup - 1) / 3) * 3 + (clickedCellId / 3);
            int column = ((clickedGroup - 1) % 3) * 3 + ((clickedCellId) % 3);
            currentBoard.setValue(row,column,Integer.parseInt(value));

            Button buttonCheckBoard = findViewById(R.id.buttonCheckBoard);

            if (currentBoard.isBoardFull()) {
                buttonCheckBoard.setVisibility(View.VISIBLE);
            } else {
                buttonCheckBoard.setVisibility(View.INVISIBLE);
            }
        }
    }
}