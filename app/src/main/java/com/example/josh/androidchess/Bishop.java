package com.example.josh.androidchess;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 *
 */
public class Bishop extends Piece {

    /**
     * @param color
     *            color of piece
     */
    public Bishop(char color) {
        super(color);
    }

    @Override
    public void setType() {
        type = 'B';
    }

    @Override
    public List<String> getMoves(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        int newRow = row - 1, newCol = col - 1;
        while (newRow >= 0 && newCol >= 0) {
            Piece curr = board[newRow][newCol];
            if (curr == null || curr.color != color)
                moves.add(toPosition(newRow, newCol));
            if (curr != null)
                break;
            newRow--;
            newCol--;
        }
        newRow = row - 1;
        newCol = col + 1;
        while (newRow >= 0 && newCol <= 7) {
            Piece curr = board[newRow][newCol];
            if (curr == null || curr.color != color)
                moves.add(toPosition(newRow, newCol));
            if (curr != null)
                break;
            newRow--;
            newCol++;
        }
        newRow = row + 1;
        newCol = col - 1;
        while (newRow <= 7 && newCol >= 0) {
            Piece curr = board[newRow][newCol];
            if (curr == null || curr.color != color)
                moves.add(toPosition(newRow, newCol));
            if (curr != null)
                break;
            newRow++;
            newCol--;
        }
        newRow = row + 1;
        newCol = col + 1;
        while (newRow <= 7 && newCol <= 7) {
            Piece curr = board[newRow][newCol];
            if (curr == null || curr.color != color)
                moves.add(toPosition(newRow, newCol));
            if (curr != null)
                break;
            newRow++;
            newCol++;
        }

        return moves;
    }

}
