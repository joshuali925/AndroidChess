package com.example.josh.androidchess;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 *
 */
public class Rook extends Piece {

    /**
     * @param color color of piece
     */
    public Rook(char color) {
        super(color);
    }

    @Override
    public void setType() {
        type = 'R';
    }

    @Override
    public List<String> getMoves(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        int newRow = row - 1;
        while (newRow >= 0) {
            Piece curr = board[newRow][col];
            if (curr == null || curr.color != color)
                moves.add(toPosition(newRow, col));
            if (curr != null)
                break;
            newRow--;
        }
        newRow = row + 1;
        while (newRow <= 7) {
            Piece curr = board[newRow][col];
            if (curr == null || curr.color != color)
                moves.add(toPosition(newRow, col));
            if (curr != null)
                break;
            newRow++;
        }

        int newCol = col - 1;
        while (newCol >= 0) {
            Piece curr = board[row][newCol];
            if (curr == null || curr.color != color)
                moves.add(toPosition(row, newCol));
            if (curr != null)
                break;
            newCol--;
        }
        newCol = col + 1;
        while (newCol <= 7) {
            Piece curr = board[row][newCol];
            if (curr == null || curr.color != color)
                moves.add(toPosition(row, newCol));
            if (curr != null)
                break;
            newCol++;
        }

        return moves;
    }

}
