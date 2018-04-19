package com.example.josh.androidchess;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 *
 */
public class Knight extends Piece {

    /**
     * @param color
     *            color of piece
     */
    public Knight(char color) {
        super(color);
    }

    @Override
    public void setType() {
        type = 'N';
    }

    @Override
    public List<String> getMoves(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        tryAddMove(row + 2, col + 1, chess, moves);
        tryAddMove(row + 2, col - 1, chess, moves);
        tryAddMove(row - 2, col + 1, chess, moves);
        tryAddMove(row - 2, col - 1, chess, moves);
        tryAddMove(row + 1, col + 2, chess, moves);
        tryAddMove(row + 1, col - 2, chess, moves);
        tryAddMove(row - 1, col + 2, chess, moves);
        tryAddMove(row - 1, col - 2, chess, moves);

        return moves;
    }

}
