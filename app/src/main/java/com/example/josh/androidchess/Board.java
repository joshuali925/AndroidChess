package com.example.josh.androidchess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Board extends Activity {
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
    String pieceSelected = null;
    List<String[]> savedMoves = new ArrayList<String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        initialize();

        updateBoard();
    }

    /**
     * Creates new chess, initializes idMap and pieceView
     */
    protected void initialize() {
        chess = new Chess();
        chess.setup();
        for (int i = 0; i < ids.length; i++)
            for (int j = 0; j < ids[0].length; j++) {
                ImageView imageView = (ImageView) findViewById(ids[i][j]);
                imageView.setClickable(true);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
//                Toast.makeText(Board.this, pieceSelected + "->" + pos, Toast.LENGTH_SHORT).show();
        if (piece != null && chess.colorToMove == piece.color) {
            pieceSelected = pos;
            List<String> moves = chess.getMoves(pos);
            moves.add(pos);
            updateHighlight(moves);
            return;
        }
        if (pieceSelected != null) {
            if (chess.canPromote(pieceSelected, pos)) {
                final String promotionSrc = pieceSelected;
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
                        if (chess.move(promotionSrc, promotionDst)) {
                            updateBoard();
                            savedMoves.add(new String[]{promotionSrc, promotionDst, chess.promotion + ""});
                            checkIfFinished();
                        }
                    }
                });
                builder.show();
            } else if (chess.move(pieceSelected, pos)) {
                updateBoard();
                savedMoves.add(new String[]{pieceSelected, pos});
                checkIfFinished();
            }
        }
        updateHighlight(pos);
        pieceSelected = null;
    }

    protected void checkIfFinished() {
        if (chess.winner == null)
            return;
        String message;
        if (chess.winner.equals("Stalemate"))
            message = "Stalemate.";
        else
            message = "Checkmate. " + chess.winner + " wins.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message);
        builder.setMessage("\nSave game as:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                Toast.makeText(Board.this, m_Text, Toast.LENGTH_SHORT).show();
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
        for (int row = 0; row < pieceView.length; row++) {
            for (int col = 0; col < pieceView[0].length; col++) {
                String hex = row % 2 == col % 2 ? "#d18b47" : "#ffce9e";
                pieceView[row][col].setBackgroundColor(Color.parseColor(hex));
            }
        }
    }

    protected void updateHighlight(List<String> positions) {
        updateHighlight();
        if (positions == null)
            return;
        for (String pos :
                positions)
            setHighlight(pos);
    }

    protected void updateHighlight(String pos) {
        updateHighlight();
        if (pos == null)
            return;
        setHighlight(pos);
    }

    protected void setHighlight(String pos) {
        int row = pos.charAt(1) - '1';
        int col = pos.charAt(0) - 'a';
        String hex = row % 2 == col % 2 ? "#c0dc88" : "#d5e6af";
        pieceView[row][col].setBackgroundColor(Color.parseColor(hex));
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

}
