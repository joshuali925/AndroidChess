package com.example.josh.androidchess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class BoardActivity extends Activity {
    ImageView[][] pieceView = new ImageView[8][8];
    int[][] ids = {{R.id.a1, R.id.b1, R.id.c1, R.id.d1, R.id.e1, R.id.f1, R.id.g1, R.id.h1},
            {R.id.a2, R.id.b2, R.id.c2, R.id.d2, R.id.e2, R.id.f2, R.id.g2, R.id.h2},
            {R.id.a3, R.id.b3, R.id.c3, R.id.d3, R.id.e3, R.id.f3, R.id.g3, R.id.h3},
            {R.id.a4, R.id.b4, R.id.c4, R.id.d4, R.id.e4, R.id.f4, R.id.g4, R.id.h4},
            {R.id.a5, R.id.b5, R.id.c5, R.id.d5, R.id.e5, R.id.f5, R.id.g5, R.id.h5},
            {R.id.a6, R.id.b6, R.id.c6, R.id.d6, R.id.e6, R.id.f6, R.id.g6, R.id.h6},
            {R.id.a7, R.id.b7, R.id.c7, R.id.d7, R.id.e7, R.id.f7, R.id.g7, R.id.h7},
            {R.id.a8, R.id.b8, R.id.c8, R.id.d8, R.id.e8, R.id.f8, R.id.g8, R.id.h8}};
    HashMap<Integer, String> idMap = new HashMap<Integer, String>();
    Chess chess;
    boolean finished = false;
    String previousSelected = null;
    List<String[]> savedMoves = new ArrayList<String[]>(); // list of [src, dst] or [src, dst, promotion]
    String[] undoedMove = null;
    Button aiButton;
    Button drawButton;
    Button resignButton;
    Button undoButton;
    Button redoButton;
    Button recordsButton;
    Button restartButton;
    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        chess = new Chess();
        finished = false;
        initialize();

        try {
            FileInputStream fis = getApplicationContext().openFileInput("chess_data");
            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<SavedGames> list = (ArrayList<SavedGames>) is.readObject();
            is.close();
            fis.close();
            SavedGames.gameList = list;
        } catch (Exception e) {
//            e.printStackTrace();
        }

        updateBoard();
    }

    protected void setButtons() {
        aiButton = findViewById(R.id.aiButton);
        aiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finished)
                    return;
                String[] move = chess.randomMove();
                if (move != null)
                    move(move[0], move[1]);
            }
        });

        drawButton = findViewById(R.id.drawButton);
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finished)
                    return;
                if (chess.drawPrompt && chess.drawColor != chess.colorToMove) {
                    String[] lastMove = savedMoves.get(savedMoves.size() - 1);
                    String[] newLastMove = new String[4];
                    for (int i = 0; i < lastMove.length; i++)
                        newLastMove[i] = lastMove[i];
                    newLastMove[3] = "Draw";
                    savedMoves.set(savedMoves.size() - 1, newLastMove);
                    chess.winner = newLastMove[3];
                    checkIfFinished();
                    chess.winner = null;
                } else {
                    chess.drawColor = chess.colorToMove;
                }
                String message = chess.drawPrompt ? "Prompt draw" : "Cancel draw";
                drawButton.setText(message);
                chess.drawPrompt = !chess.drawPrompt;
            }
        });

        resignButton = findViewById(R.id.resignButton);
        resignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (savedMoves.size() == 0)
                    savedMoves.add(new String[0]);
                String[] lastMove = savedMoves.get(savedMoves.size() - 1);
                String[] newLastMove = new String[4];
                for (int i = 0; i < lastMove.length; i++)
                    newLastMove[i] = lastMove[i];
                newLastMove[3] = chess.colorToMove == 'w' ? "Black" : "White";
                savedMoves.set(savedMoves.size() - 1, newLastMove);
                chess.winner = newLastMove[3];
                checkIfFinished(true);
                chess.winner = null;
            }
        });

        undoButton = findViewById(R.id.undoButton);
        undoButton.setEnabled(false);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finished = false;
                previousSelected = null;
                chess.undoMove();
                updateBoard();
                updateHighlight();

                undoedMove = savedMoves.get(savedMoves.size() - 1);
                savedMoves.remove(savedMoves.size() - 1);

                undoButton.setEnabled(false);
                redoButton.setEnabled(true);
                chess.drawPrompt = false;
                drawButton.setText("Prompt draw");
                statusText.setText((chess.colorToMove == 'w' ? "White" : "Black") + "'s move");
            }
        });

        redoButton = findViewById(R.id.redoButton);
        resetRedoButton();

        recordsButton = findViewById(R.id.recordsButton);
        recordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SavedGames.gameList.size() == 0) {
                    Toast.makeText(BoardActivity.this, "No saved games", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(BoardActivity.this, RecordActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chess = new Chess();
                updateBoard();
                updateHighlight();
                savedMoves.clear();
                aiButton.setEnabled(true);
                drawButton.setEnabled(true);
                resignButton.setEnabled(true);
                undoButton.setEnabled(false);
                resetRedoButton();
                drawButton.setText("Prompt draw");
                statusText.setText("White's move");
                finished = false;
                previousSelected = null;
            }
        });
    }


    /**
     * Creates new chess, initializes idMap and pieceView
     */
    protected void initialize() {
        setButtons();
        statusText = findViewById(R.id.statusText);
        statusText.setText("White's move");

        for (int i = 0; i < ids.length; i++)
            for (int j = 0; j < ids[0].length; j++) {
                ImageView imageView = findViewById(ids[i][j]);
                imageView.setClickable(true);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!finished)
                            handleClick(view.getId());
                    }
                });
                pieceView[i][j] = imageView;
                idMap.put(ids[i][j], Piece.toPosition(i, j));
            }
    }

    protected void handleClick(int id) {
        String pos = idMap.get(id);
        int row = pos.charAt(1) - '1';
        int col = pos.charAt(0) - 'a';
        Piece piece = chess.getPiece(pos);
        if (piece != null && chess.colorToMove == piece.color) {
            previousSelected = pos;
            List<String> moves = chess.getMoves(pos);
            moves.add(pos);
            updateHighlight(moves);
            return;
        }
        if (chess.canPromote(previousSelected, pos) && chess.getMoves(previousSelected).contains(pos)) {
            final String promotionSrc = previousSelected;
            final String promotionDst = pos;

            final CharSequence promotions[] = new CharSequence[]{"Queen", "Rook", "Bishop", "Knight"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick promotion");
            builder.setItems(promotions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    chess.promotion = promotions[which].charAt(0);
                    if (chess.promotion == 'K')
                        chess.promotion = 'N';
                    move(promotionSrc, promotionDst, chess.promotion + "");
                }
            });
            builder.show();
        } else if (previousSelected == null || !move(previousSelected, pos))
            updateHighlight(pos, 'R');
        previousSelected = null;
    }

    protected boolean move(String src, String dst) {
        return move(src, dst, null);
    }

    protected boolean move(String src, String dst, String promotion) {
        if (!chess.move(src, dst))
            return false;
        updateBoard();
        updateHighlight(dst, 'G');
        if (promotion == null)
            savedMoves.add(new String[]{src, dst});
        else
            savedMoves.add(new String[]{src, dst, promotion});
        undoButton.setEnabled(true);
        undoedMove = null;
        redoButton.setEnabled(false);
        statusText.setText((chess.colorToMove == 'w' ? "White" : "Black") + "'s move");
        checkIfFinished();
        if (chess.drawPrompt) {
            if (chess.drawColor == chess.colorToMove) { // draw refused
                chess.drawPrompt = false;
                drawButton.setText("Prompt draw");
            } else {
                String color = chess.drawColor == 'w' ? "White" : "Black";
                Toast.makeText(BoardActivity.this, color + " prompts draw", Toast.LENGTH_SHORT).show();
                drawButton.setText("Confirm draw");
            }
        }
        if (chess.check != null)
            Toast.makeText(BoardActivity.this, chess.check + " is in check", Toast.LENGTH_LONG).show();
        return true;
    }

    protected void checkIfFinished() {
        checkIfFinished(false);
    }

    protected void checkIfFinished(boolean resign) {
        if (chess.winner == null)
            return;
        finished = true;
        String message;
        if (chess.winner.equals("Stalemate"))
            message = "Stalemate.";
        else if (chess.winner.equals("Draw"))
            message = "Draw.";
        else if (resign)
            message = "Resign. " + chess.winner + " wins.";
        else
            message = "Checkmate. " + chess.winner + " wins.";
        statusText.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message);
        builder.setMessage("\nSave game as:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                SavedGames.addGame(name, Calendar.getInstance().getTime(), savedMoves);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    protected void updateHighlight() {
        for (int row = 0; row < pieceView.length; row++)
            for (int col = 0; col < pieceView[0].length; col++) {
                String hex = row % 2 == col % 2 ? "#d18b47" : "#ffce9e";
                pieceView[row][col].setBackgroundColor(Color.parseColor(hex));
            }
    }

    protected void updateHighlight(List<String> positions) {
        updateHighlight();
        if (positions == null)
            return;
        if (positions.size() == 1) // piece with no available moves
            setHighlight(positions.get(0), 'R');
        else
            for (String pos :
                    positions)
                setHighlight(pos, 'G');
    }

    protected void updateHighlight(String pos, char color) {
        updateHighlight();
        if (pos == null)
            return;
        setHighlight(pos, color);
    }

    protected void setHighlight(String pos, char color) {
        int row = pos.charAt(1) - '1';
        int col = pos.charAt(0) - 'a';
        String hex;
        if (color == 'G')
            hex = row % 2 == col % 2 ? "#c0dc88" : "#d5e6af";
        else
            hex = row % 2 == col % 2 ? "#f58466" : "#f9a98e";
        pieceView[row][col].setBackgroundColor(Color.parseColor(hex));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (!data.hasExtra("gameIndex"))
                    return;
                int index = data.getIntExtra("gameIndex", -1);
                if (index != -1)
                    startPlayback(SavedGames.gameList.get(index));
            }
        }
    }

    protected void startPlayback(SavedGames savedGames) {
        aiButton.setEnabled(false);
        drawButton.setEnabled(false);
        undoButton.setEnabled(false);
        resignButton.setEnabled(false);
        chess = new Chess();
        savedGames.index = 0;
        updateBoard();
        updateHighlight();
        finished = true;
        statusText.setText("White's move");
        final SavedGames game = savedGames;

        redoButton.setEnabled(true);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] move = game.nextMove();
                if (move == null) {
                    resetRedoButton();
                    return;
                }
                if (move.length == 3)
                    chess.promotion = move[2].charAt(0);
                if (move[0] != null && chess.move(move[0], move[1])) {
                    updateHighlight(move[1], 'G');
                    updateBoard();
                }
                statusText.setText((chess.colorToMove == 'w' ? "White" : "Black") + "'s move");
                if (move.length == 4) {
                    String message;
                    if (move[3].length() == 5)
                        message = "Resign, " + move[3] + " wins";
                    else
                        message = move[3];
                    statusText.setText(message);
                    resetRedoButton();
                    return;
                }
                if (chess.winner != null) {
                    String message = "Checkmate, " + chess.winner;
                    if (chess.winner.length() == 5)
                        message += " wins";
                    statusText.setText(message);
                    resetRedoButton();
                }
            }
        });
    }

    protected void resetRedoButton() {
        redoButton.setEnabled(false);
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (undoedMove == null)
                    return;
                previousSelected = null;
                if (undoedMove.length >= 3 && undoedMove[2] != null)
                    move(undoedMove[0], undoedMove[1], undoedMove[2]);
                else
                    move(undoedMove[0], undoedMove[1]);
            }
        });
    }

    protected void updateBoard() {
        Piece[][] board = chess.getBoard();
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[0].length; j++)
                pieceView[i][j].setImageResource(getPieceImageId(board[i][j]));
    }

    protected int getPieceImageId(Piece piece) {
        if (piece != null) {
            char color = piece.color;
            char type = piece.type;
            if (color == 'w') {
                if (type == 'p')
                    return R.drawable.ic_wp;
                if (type == 'B')
                    return R.drawable.ic_wb;
                if (type == 'K')
                    return R.drawable.ic_wk;
                if (type == 'N')
                    return R.drawable.ic_wn;
                if (type == 'Q')
                    return R.drawable.ic_wq;
                if (type == 'R')
                    return R.drawable.ic_wr;
            } else {
                if (type == 'p')
                    return R.drawable.ic_bp;
                if (type == 'B')
                    return R.drawable.ic_bb;
                if (type == 'K')
                    return R.drawable.ic_bk;
                if (type == 'N')
                    return R.drawable.ic_bn;
                if (type == 'Q')
                    return R.drawable.ic_bq;
                if (type == 'R')
                    return R.drawable.ic_br;
            }
        }
        return R.drawable.ic_empty;
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput("chess_data", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(SavedGames.gameList);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
