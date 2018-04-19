package com.example.josh.androidchess;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 *
 */
public class Queen extends Piece {

    /**
     * @param color color of piece
     */
    public Queen(char color) {
        super(color);
    }

    @Override
    public void setType() {
        type = 'Q';
    }

    @Override
    public List<String> getMoves(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        Piece rook = new Rook(color);
        Piece bishop = new Bishop(color);
        moves.addAll(rook.getMoves(row, col, chess));
        moves.addAll(bishop.getMoves(row, col, chess));

        return moves;
    }

}
