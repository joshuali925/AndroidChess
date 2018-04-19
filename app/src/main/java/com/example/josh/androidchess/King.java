package com.example.josh.androidchess;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 *
 */
public class King extends Piece {

    /**
     * @param color
     *            color of piece
     */
    public King(char color) {
        super(color);
    }

    @Override
    public void setType() {
        type = 'K';
    }

    @Override
    public List<String> getMoves(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        tryAddMove(row, col + 1, chess, moves);
        tryAddMove(row, col - 1, chess, moves);
        tryAddMove(row + 1, col, chess, moves);
        tryAddMove(row - 1, col, chess, moves);
        tryAddMove(row + 1, col + 1, chess, moves);
        tryAddMove(row + 1, col - 1, chess, moves);
        tryAddMove(row - 1, col + 1, chess, moves);
        tryAddMove(row - 1, col - 1, chess, moves);

        tryAddCastle(row, col, chess, moves);

        return moves;
    }

    @Override
    public List<String> getAttackPos(int row, int col, Chess chess) {
        Piece[][] board = chess.board;
        List<String> moves = new ArrayList<String>();

        if (outOfBounds(row, col) || board[row][col] == null)
            return moves;

        tryAddMove(row, col + 1, chess, moves);
        tryAddMove(row, col - 1, chess, moves);
        tryAddMove(row + 1, col, chess, moves);
        tryAddMove(row - 1, col, chess, moves);
        tryAddMove(row + 1, col + 1, chess, moves);
        tryAddMove(row + 1, col - 1, chess, moves);
        tryAddMove(row - 1, col + 1, chess, moves);
        tryAddMove(row - 1, col - 1, chess, moves);

        return moves;
    }

    /**
     * adds castle move into moves if the King can castle
     * 
     * @param row
     *            row of King
     * @param col
     *            column of King
     * @param chess
     *            board
     * @param moves
     *            list of available moves
     */
    public void tryAddCastle(int row, int col, Chess chess, List<String> moves) {
        Piece[][] board = chess.board;
        if (isFirstMove == false) {
            return;
        }
        int origRow = color == 'w' ? 0 : 7;
        if (row != origRow) {
            return;
        }

        Piece rook = board[row][0];
        if (rook instanceof Rook && rook.isFirstMove && board[row][1] == null && board[row][2] == null
                && board[row][3] == null && safeToCastle(row, 0, 2, 3, 4, getEnemyColor(), chess)) {
            moves.add(toPosition(row, 2));
        }
        rook = board[row][7];
        if (rook instanceof Rook && rook.isFirstMove && board[row][5] == null && board[row][6] == null
                && safeToCastle(row, 4, 5, 6, 7, getEnemyColor(), chess)) {
            moves.add(toPosition(row, 6));
        }
    }

    /**
     * @param row
     *            row of the King
     * @param col1
     *            column of rook
     * @param col2
     *            column two spaces from the King
     * @param col3
     *            column next to the King
     * @param col4
     *            column of the King
     * @param enemyColor
     *            enemy's color
     * @param chess
     *            board
     * @return returns true if positions satisfies the requirements to castle
     */
    public boolean safeToCastle(int row, int col1, int col2, int col3, int col4, char enemyColor, Chess chess) {
        Piece[][] board = chess.board;
        String pos1 = toPosition(row, col1);
        String pos2 = toPosition(row, col2);
        String pos3 = toPosition(row, col3);
        String pos4 = toPosition(row, col4);

        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                Piece curr = board[i][j];
                if (curr != null && curr.color == enemyColor) {
                    List<String> attack = curr.getAttackPos(i, j, chess);
                    if (attack.contains(pos1) || attack.contains(pos2) || attack.contains(pos3)
                            || attack.contains(pos4)) {
                        return false;
                    }
                }
            }
        return true;
    }

}
