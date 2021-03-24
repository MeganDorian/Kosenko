package com.skosenko.sudoku;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.skosenko.sudoku.model.Board;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Game extends AppCompatActivity implements CellFragment.OnFragmentInteractionListener {
    private static final String TAG = "Sudoku";
    private TextView clickedCell;
    private int clickedGroup;
    private int clickedCellId;
    private Board startBoard;
    private Board currentBoard;

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    public static final String KEY_DIFFICULTY = "org.example.sudoku.difficulty";

    protected static final int DIFFICULTY_CONTINUE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        int difficulty = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
        ArrayList<Board> boards = readGameBoards(difficulty);
        startBoard = chooseRandomBoard(boards);
        currentBoard = new Board();
        currentBoard.copyValues(startBoard.getGameCells());

        int cellFragments[] = new int[]{R.id.cellFragment, R.id.cellFragment2, R.id.cellFragment3, R.id.cellFragment4,
                R.id.cellFragment5, R.id.cellFragment6, R.id.cellFragment7, R.id.cellFragment8, R.id.cellFragment9};
        for (int i = 1; i < 10; i++) {
            CellFragment thisCellGroupFragment = (CellFragment) getSupportFragmentManager().findFragmentById(cellFragments[i-1]);
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

        getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
    }

    private ArrayList<Board> readGameBoards(int difficulty) {
        ArrayList<Board> boards = new ArrayList<>();
        int fileId;
        if (difficulty == DIFFICULTY_MEDIUM) {
            fileId = R.raw.medium;
        } else if (difficulty == DIFFICULTY_EASY) {
            fileId = R.raw.easy;
        } else {
            fileId = R.raw.hard;
        }

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

        //reading from internal storage (/data/data/<package-name>/files)
        String fileName = "boards-";
        if (difficulty == 0) {
            fileName += "easy";
        } else if (difficulty == 1) {
            fileName += "normal";
        } else {
            fileName += "hard";
        }

        FileInputStream fileInputStream;
        try {
            fileInputStream = this.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader internalBufferedReader = new BufferedReader(inputStreamReader);
            String line;
            line = internalBufferedReader.readLine();
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
                    line = internalBufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                }
                boards.add(board);
                line = internalBufferedReader.readLine();
            }
            internalBufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return boards;
    }

    private Board chooseRandomBoard(ArrayList<Board> boards) {
        int randomNumber = (int) (Math.random() * boards.size());
        return boards.get(randomNumber);
    }

    private boolean isStartPiece(int group, int cell) {
        int row = ((group-1)/3)*3 + (cell/3);
        int column = ((group-1)%3)*3 + ((cell)%3);
        return startBoard.getValue(row, column) != 0;
    }

    private boolean checkAllGroups() {
        int cellFragments[] = new int[]{R.id.cellFragment, R.id.cellFragment2, R.id.cellFragment3, R.id.cellFragment4,
                R.id.cellFragment5, R.id.cellFragment6, R.id.cellFragment7, R.id.cellFragment8, R.id.cellFragment9};
        for (int i = 0; i < 9; i++) {
            CellFragment thisCellGroupFragment = (CellFragment) getSupportFragmentManager().findFragmentById(cellFragments[i]);
            if (!thisCellGroupFragment.checkGroupCorrect()) {
                return false;
            }
        }
        return true;
    }

    public void onCheckBoardButtonClicked(View view) {
        currentBoard.isBoardCorrect();
        if(checkAllGroups() && currentBoard.isBoardCorrect()) {
            Toast.makeText(this, getString(R.string.board_correct), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.board_incorrect), Toast.LENGTH_SHORT).show();
        }
    }

    public void onGoBackButtonClicked(View view) {
        finish();
    }

    public void onShowInstructionsButtonClicked(View view) {
        Intent intent = new Intent("me.kirkhorn.knut.InstructionsActivity");
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            int row = ((clickedGroup - 1) / 3) * 3 + (clickedCellId / 3);
            int column = ((clickedGroup - 1) % 3) * 3 + ((clickedCellId) % 3);

            Button buttonCheckBoard = findViewById(R.id.buttonCheckBoard);
            if (data.getBooleanExtra("removePiece", false)) {
                clickedCell.setText("");
                clickedCell.setBackground(getResources().getDrawable(R.drawable.table_border_cell));
                currentBoard.setValue(row, column, 0);
                buttonCheckBoard.setVisibility(View.INVISIBLE);
            } else {
                int number = data.getIntExtra("chosenNumber", 1);
                clickedCell.setText(String.valueOf(number));
                currentBoard.setValue(row, column, number);

                boolean isUnsure = data.getBooleanExtra("isUnsure", false);
                if (isUnsure) {
                    clickedCell.setBackground(getResources().getDrawable(R.drawable.table_border_cell_unsure));
                } else {
                    clickedCell.setBackground(getResources().getDrawable(R.drawable.table_border_cell));
                }

                if (currentBoard.isBoardFull()) {
                    buttonCheckBoard.setVisibility(View.VISIBLE);
                } else {
                    buttonCheckBoard.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public void onFragmentInteraction(int groupId, int cellId, View view) {
        clickedCell = (TextView) view;
        clickedGroup = groupId;
        clickedCellId = cellId;
        Log.i(TAG, "Clicked group " + groupId + ", cell " + cellId);
        if (!isStartPiece(groupId, cellId)) {
            Intent intent = new Intent("me.kirkhorn.knut.ChooseNumberActivity");
            startActivityForResult(intent, 1);
        } else {
//            Toast.makeText(this, getString(R.string.start_piece_error), Toast.LENGTH_SHORT).show();
        }
    }
}

//package com.skosenko.sudoku;
//
//        import android.app.Activity;
//        import android.app.Dialog;
//        import android.os.Bundle;
//        import android.util.Log;
//        import android.view.Gravity;
//        import android.widget.Toast;
//
//public class Game extends Activity {
//    private static final String TAG = "Sudoku";
//
//    public static final String KEY_DIFFICULTY =
//            "org.example.sudoku.difficulty";
//
//    private static final String PREF_PUZZLE = "puzzle" ;
//
//    public static final int DIFFICULTY_EASY = 0;
//    public static final int DIFFICULTY_MEDIUM = 1;
//    public static final int DIFFICULTY_HARD = 2;
//
//    protected static final int DIFFICULTY_CONTINUE = -1;
//
//
//    private int puzzle[] = new int[9 * 9];
//
//    private final String easyPuzzle =
//            "360000000004230800000004200" +
//                    "070460003820000014500013020" +
//                    "001900000007048300000000045";
//    private final String mediumPuzzle =
//            "650000070000506000014000005" +
//                    "007009000002314700000700800" +
//                    "500000630000201000030000097";
//    private final String hardPuzzle =
//            "009000000080605020501078000" +
//                    "000000700706040102004000000" +
//                    "000720903090301080000000600";
//
//    private SudokuView puzzleView;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        Log.d(TAG, "onCreate");
//
//        int diff = getIntent().getIntExtra(KEY_DIFFICULTY,
//                DIFFICULTY_EASY);
//        puzzle = getPuzzle(diff);
//        calculateUsedTiles();
//
//        puzzleView = new SudokuView(this);
//        setContentView(puzzleView);
//        puzzleView.requestFocus();
//
//
//        // ...
//        // If the activity is restarted, do a continue next time
//        getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        Music.play(this, R.raw.game);
//    }
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d(TAG, "onPause");
////        Music.stop(this);
//
//        // Save the current puzzle
//        getPreferences(MODE_PRIVATE).edit().putString(PREF_PUZZLE,
//                toPuzzleString(puzzle)).commit();
//    }
//
//
//
//    /** Given a difficulty level, come up with a new puzzle */
//    private int[] getPuzzle(int diff) {
//        String puz;
//        switch (diff) {
//            case DIFFICULTY_CONTINUE:
//                puz = getPreferences(MODE_PRIVATE).getString(PREF_PUZZLE,
//                        easyPuzzle);
//                break;
//            // ...
//
//            case DIFFICULTY_HARD:
//                puz = hardPuzzle;
//                break;
//            case DIFFICULTY_MEDIUM:
//                puz = mediumPuzzle;
//                break;
//            case DIFFICULTY_EASY:
//            default:
//                puz = easyPuzzle;
//                break;
//
//        }
//        return fromPuzzleString(puz);
//    }
//
//
//    /** Convert an array into a puzzle string */
//    static private String toPuzzleString(int[] puz) {
//        StringBuilder buf = new StringBuilder();
//        for (int element : puz) {
//            buf.append(element);
//        }
//        return buf.toString();
//    }
//
//    /** Convert a puzzle string into an array */
//    static protected int[] fromPuzzleString(String string) {
//        int[] puz = new int[string.length()];
//        for (int i = 0; i < puz.length; i++) {
//            puz[i] = string.charAt(i) - '0';
//        }
//        return puz;
//    }
//
//    /** Return the tile at the given coordinates */
//    private int getTile(int x, int y) {
//        return puzzle[y * 9 + x];
//    }
//
//    /** Change the tile at the given coordinates */
//    private void setTile(int x, int y, int value) {
//        puzzle[y * 9 + x] = value;
//    }
//
//    /** Return a string for the tile at the given coordinates */
//    protected String getTileString(int x, int y) {
//        int v = getTile(x, y);
//        if (v == 0)
//            return "";
//        else
//            return String.valueOf(v);
//    }
//
//    /** Change the tile only if it's a valid move */
//    protected boolean setTileIfValid(int x, int y, int value) {
//        int tiles[] = getUsedTiles(x, y);
//        if (value != 0) {
//            for (int tile : tiles) {
//                if (tile == value)
//                    return false;
//            }
//        }
//        setTile(x, y, value);
//        calculateUsedTiles();
//        return true;
//    }
//
//    /** Open the keypad if there are any valid moves */
//    protected void showKeypadOrError(int x, int y) {
//        int tiles[] = getUsedTiles(x, y);
//        if (tiles.length == 9) {
//            Toast toast = Toast.makeText(this,
//                    R.string.no_moves_label, Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
//        } else {
//            Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
//            Dialog v = new KeyPad(this, tiles, puzzleView);
//            v.show();
//        }
//    }
//
//    /** Cache of used tiles */
//    private final int used[][][] = new int[9][9][];
//
//    /** Return cached used tiles visible from the given coords */
//    protected int[] getUsedTiles(int x, int y) {
//        return used[x][y];
//    }
//
//    /** Compute the two dimensional array of used tiles */
//    private void calculateUsedTiles() {
//        for (int x = 0; x < 9; x++) {
//            for (int y = 0; y < 9; y++) {
//                used[x][y] = calculateUsedTiles(x, y);
//                // Log.d(TAG, "used[" + x + "][" + y + "] = "
//                // + toPuzzleString(used[x][y]));
//            }
//        }
//    }
//
//    /** Compute the used tiles visible from this position */
//    private int[] calculateUsedTiles(int x, int y) {
//        int c[] = new int[9];
//        // horizontal
//        for (int i = 0; i < 9; i++) {
//            if (i == y)
//                continue;
//            int t = getTile(x, i);
//            if (t != 0)
//                c[t - 1] = t;
//        }
//        // vertical
//        for (int i = 0; i < 9; i++) {
//            if (i == x)
//                continue;
//            int t = getTile(i, y);
//            if (t != 0)
//                c[t - 1] = t;
//        }
//        // same cell block
//        int startx = (x / 3) * 3;
//        int starty = (y / 3) * 3;
//        for (int i = startx; i < startx + 3; i++) {
//            for (int j = starty; j < starty + 3; j++) {
//                if (i == x && j == y)
//                    continue;
//                int t = getTile(i, j);
//                if (t != 0)
//                    c[t - 1] = t;
//            }
//        }
//        // compress
//        int nused = 0;
//        for (int t : c) {
//            if (t != 0)
//                nused++;
//        }
//        int c1[] = new int[nused];
//        nused = 0;
//        for (int t : c) {
//            if (t != 0)
//                c1[nused++] = t;
//        }
//        return c1;
//    }
//
//}