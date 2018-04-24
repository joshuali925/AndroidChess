package com.example.josh.androidchess;

import java.io.Serializable;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 */
public abstract class Piece implements Serializable{
    public boolean isFirstMove;
    public char color;
    public char type;

    /**
     * @param color color of piece
     */
    public Piece(char color) {
        this.isFirstMove = true;
        this.color = color;
        setType();
    }

    /**
     * set the type of piece
     */
    public abstract void setType();

    /**
     * @param row   row of piece
     * @param col   column of piece
     * @param chess board
     * @return returns list of available moves
     */
    public abstract List<String> getMoves(int row, int col, Chess chess);

    /**
     * @param position position of piece
     * @param chess    board
     * @return returns list of available moves
     */
    public List<String> getMoves(String position, Chess chess) {
        int row = position.charAt(1) - '1';
        int col = position.charAt(0) - 'a';
        return getMoves(row, col, chess);
    }

    /**
     * @param row   row of piece
     * @param col   column of piece
     * @param chess board
     * @return returns list of positions can be attacked
     */
    public List<String> getAttackPos(int row, int col, Chess chess) {
        return getMoves(row, col, chess);
    }

    /**
     * @param position position of piece
     * @param chess    board
     * @return returns list of positions can be attacked
     */
    public List<String> getAttackPos(String position, Chess chess) {
        int row = position.charAt(1) - '1';
        int col = position.charAt(0) - 'a';
        return getAttackPos(row, col, chess);
    }

    /**
     * @return returns enemy's color
     */
    public char getEnemyColor() {
        return color == 'w' ? 'b' : 'w';
    }

    /**
     * add position to moves if position is valid and the piece can move there
     *
     * @param row   row
     * @param col   column
     * @param chess board
     * @param moves list of moves
     */
    public void tryAddMove(int row, int col, Chess chess, List<String> moves) {
        Piece[][] board = chess.board;
        if (!outOfBounds(row, col) && (board[row][col] == null || board[row][col].color != color))
            moves.add(toPosition(row, col));
    }

    @Override
    public String toString() {
        return "" + color + type;
    }

    /**
     * @param row row
     * @param col column
     * @return returns if position is inside the board
     */
    public static boolean outOfBounds(int row, int col) {
        return row < 0 || row > 7 || col < 0 || col > 7;
    }

    /**
     * @param position position
     * @return returns if position is inside the board
     */
    public static boolean outOfBounds(String position) {
        int row = position.charAt(1) - '1';
        int col = position.charAt(0) - 'a';
        return outOfBounds(row, col);
    }

    /**
     * @param row row
     * @param col column
     * @return returns row and column in String form (0, 0 -> a1)
     */
    public static String toPosition(int row, int col) {
        return "" + (char) (col + 'a') + (char) (row + '1');
    }

}
