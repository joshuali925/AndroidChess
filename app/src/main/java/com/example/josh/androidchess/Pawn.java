package com.example.josh.androidchess;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 */
public class Pawn extends Piece {

    int offset;

    /**
     * sets offset, the direction to move according to Pawn's color
     *
     * @param color color of piece
     */
    public Pawn(char color) {
        super(color);
        offset = color == 'w' ? 1 : -1;
    }

    @Override
    public void setType() {
        type = 'p';
    }

    @Override
    public List<String> getMoves(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        if (!outOfBounds(row + offset, col) && board[row + offset][col] == null) {
            moves.add(toPosition(row + offset, col));
            if (isFirstMove && !outOfBounds(row + 2 * offset, col) && board[row + 2 * offset][col] == null)
                moves.add(toPosition(row + 2 * offset, col));
        }

        tryAddMove(row + offset, col + 1, chess, moves);
        tryAddMove(row + offset, col - 1, chess, moves);

        tryEnpassant(row, col, chess, moves);

        return moves;
    }

    /**
     * adds en passant position to moves if can en passant
     *
     * @param row   row of Pawn
     * @param col   column of Pawn
     * @param chess board
     * @param moves list of available moves
     */
    public void tryEnpassant(int row, int col, Chess chess, List<String> moves) {
        String target = chess.canBeEnpass;

        if (target == null)
            return;

        Piece enemy = chess.getPiece(target);
        if (enemy == null) {
            return;
        }
        int tarRow = target.charAt(1) - '1';
        int tarCol = target.charAt(0) - 'a';

        if (enemy.color != color && tarRow == row && Math.abs(tarCol - col) == 1)
            moves.add(toPosition(row + offset, tarCol));
    }

    @Override
    public List<String> getAttackPos(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        tryAddMove(row + offset, col + 1, chess, moves);
        tryAddMove(row + offset, col - 1, chess, moves);

        String target = chess.canBeEnpass;
        if (target == null)
            return moves;

        Piece enemy = chess.getPiece(target);
        int tarRow = target.charAt(1) - '1';
        int tarCol = target.charAt(0) - 'a';

        if (enemy.color != color && tarRow == row && Math.abs(tarCol - col) == 1) {
            moves.add(target);
        }

        return moves;
    }

    @Override
    public void tryAddMove(int row, int col, Chess chess, List<String> moves) {
        Piece[][] board = chess.board;
        if (!outOfBounds(row, col) && (board[row][col] != null && board[row][col].color != color))
            moves.add(toPosition(row, col));
    }

}
