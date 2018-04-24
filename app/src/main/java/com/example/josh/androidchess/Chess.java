package com.example.josh.androidchess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joshua Li, Dingbang Chen
 */
public class Chess implements Serializable{
    public Piece[][] board;
    public Piece[][] copy;
    public String canBeEnpass, canBeEnBak;
    public String winner;
    public String check;
    public char colorToMove;
    public char promotion;
    public boolean drawPrompt;
    public char drawColor;

    public Piece[][] undo = new Piece[8][8];
    public boolean[][] lastIsFirstMove = new boolean[8][8];
    public String lastCanBeEnpass = null;

    /**
     * Initialize fields
     */
    public Chess() {
        board = new Piece[8][8];
        copy = new Piece[8][8];
        canBeEnpass = null;
        canBeEnBak = null;
        colorToMove = 'w';
        promotion = 0;
        drawPrompt = false;
        drawColor = 0;
        winner = null;
        check = null;
        setup();
    }

    /**
     * revert board back after doing a move with testing = true
     */
    public void restoreMove() {
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++)
                board[i][j] = copy[i][j];
        canBeEnpass = canBeEnBak;
    }

    public void copyState() {
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                undo[i][j] = board[i][j];
                if (getPiece(i, j) != null)
                    lastIsFirstMove[i][j] = getPiece(i, j).isFirstMove;
            }
        if (canBeEnpass != null)
            lastCanBeEnpass = canBeEnpass;
    }

    public void undoMove() {
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                board[i][j] = undo[i][j];
                if (getPiece(i, j) != null)
                    getPiece(i, j).isFirstMove = lastIsFirstMove[i][j];
            }
        colorToMove = toggleColor(colorToMove);
        canBeEnpass = lastCanBeEnpass;
        winner = null;
    }

    /**
     * @param src starting position
     * @param dst targeting position
     * @return returns move with testing = false
     */
    public boolean move(String src, String dst) {
        return move(src, dst, false);
    }

    /**
     * @param src     starting position
     * @param dst     targeting position
     * @param testing false if move made by player
     * @return returns true if success, false if invalid
     */
    public boolean move(String src, String dst, boolean testing) {
        if (Piece.outOfBounds(src) || Piece.outOfBounds(dst)
                || testing == false && getMoves(src).contains(dst) == false)
            return false;
        if (getPiece(src).color != colorToMove)
            return false;

        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++)
                copy[i][j] = board[i][j];
        canBeEnBak = canBeEnpass;

        Piece curr = getPiece(src);
        boolean castled = false, promote = false, enpassant = false, setEnpassant = false;

        if (curr instanceof King && Math.abs(dst.charAt(0) - src.charAt(0)) == 2) {
            int row = curr.color == 'w' ? 0 : 7;
            int col = dst.charAt(0) - 'a';
            Piece rook;
            switch (col) {
                case 2:
                    if (testing == false)
                        copyState();
                    rook = getPiece(row, 0);
                    setPiece(row, 0, null);
                    setPiece(row, 3, rook);
                    break;
                case 6:
                    if (testing == false)
                        copyState();
                    rook = getPiece(row, 7);
                    setPiece(row, 7, null);
                    setPiece(row, 5, rook);
                    break;
                default:
                    return false;
            }
            if (testing == false)
                rook.isFirstMove = false;
            setPiece(src, null);
            setPiece(dst, curr);
            castled = true;
        }

        if (curr instanceof Pawn)
            if (canPromote(curr.color, dst)) {
                if (testing == false)
                    copyState();
                setPiece(src, null);
                setPiece(dst, getPromotion(curr.color));
                promote = true;
            } else if (Math.abs(dst.charAt(1) - src.charAt(1)) == 2) {
                canBeEnpass = dst;
                setEnpassant = true;
            } else if (dst.charAt(0) != src.charAt(0) && getPiece(dst) == null) {
                if (testing == false)
                    copyState();
                setPiece(canBeEnpass, null);
                setPiece(src, null);
                setPiece(dst, curr);
                enpassant = true;
            }
        if (setEnpassant == false) {
            if (testing == false)
                lastCanBeEnpass = canBeEnpass;
            canBeEnpass = null;
        }
        if (!castled && !promote && !enpassant) {
            if (testing == false)
                copyState();
            setPiece(src, null);
            setPiece(dst, curr);
        }

        if (testing == false) {
            curr.isFirstMove = false;
//            drawPrompt = false;
            colorToMove = toggleColor(colorToMove);
//            System.out.println("\n" + toString());

            if (isCheck(toggleColor(curr.color))) {
                if (isCheckmate(toggleColor(curr.color))) {
//                    System.out.println("Checkmate");
                    winner = curr.color == 'w' ? "White" : "Black";
                } else {
//                    System.out.println("Check");
                    check = curr.color == 'w' ? "Black" : "White";
                }
            } else if (isStalemate(toggleColor(curr.color))) {
                winner = "Stalemate";
            } else {
                check = null;
            }
        }
        return true;
    }

    /**
     * @param color color of King to look for
     * @return returns the position of King
     */
    public String getKingPos(char color) {
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                Piece curr = getPiece(i, j);
                if (curr != null && curr instanceof King && curr.color == color)
                    return Piece.toPosition(i, j);
            }
        return null;
    }

    /**
     * @param color color of piece
     * @param dst   position of piece
     * @return returns true if position is at the end of the board
     */
    public boolean canPromote(char color, String dst) {
        return dst.charAt(1) == '1' && color == 'b' || dst.charAt(1) == '8' && color == 'w';
    }

    /**
     * @param src position of piece
     * @param dst target position
     * @return returns true if the piece can promote
     */
    public boolean canPromote(String src, String dst) {
        if (src == null)
            return false;
        Piece pawn = getPiece(src);
        if (pawn != null && pawn instanceof Pawn) {
            char color = pawn.color;
            char secondToLastRow = color == 'w' ? '7' : '2';
            if (src.charAt(1) == secondToLastRow && color == colorToMove)
                return canPromote(color, dst);
        }
        return false;
    }

    /**
     * @param color color of piece
     * @return returns a new Piece indicated by player, or a Queen
     */
    public Piece getPromotion(char color) {
        switch (promotion) {
            case 'Q':
                return new Queen(color);
            case 'N':
                return new Knight(color);
            case 'B':
                return new Bishop(color);
            case 'R':
                return new Rook(color);
            default:
                return new Queen(color);
        }
    }

    /**
     * @return returns {source position, dest position}, null if no move is possible
     */
    public String[] randomMove() {
        List<String[]> allMoves = new ArrayList<String[]>();
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                Piece curr = getPiece(i, j);
                if (curr == null || curr.color != colorToMove)
                    continue;
                List<String> moves = getMoves(i, j);
                if (moves.size() > 0)
                    allMoves.add(new String[]{Piece.toPosition(i, j), moves.get((int) (Math.random() * moves.size()))});
            }
        if (allMoves.size() > 0)
            return allMoves.get((int) (Math.random() * allMoves.size()));
        return null;
    }

    /**
     * @param color color of player
     * @return returns true if player is in check
     */
    public boolean isCheck(char color) {
        return isUnderAttack(getKingPos(color), toggleColor(color));
    }

    /**
     * @param color color of player
     * @return returns true if player is checkmated
     */
    public boolean isCheckmate(char color) {
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                Piece curr = getPiece(i, j);
                if (curr == null || curr.color != color)
                    continue;
                List<String> moves = getMoves(i, j);
                for (String dst : moves) {
                    move(Piece.toPosition(i, j), dst, true);
                    boolean escaped = !isCheck(curr.color);
                    restoreMove();
                    if (escaped)
                        return false;
                }
            }
        return true;

    }

    /**
     * @param color color of player
     * @return returns if is stalemate
     */
    public boolean isStalemate(char color) {
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                Piece curr = getPiece(i, j);
                if (curr == null || curr.color != color)
                    continue;
                List<String> moves = getMoves(i, j);
                if (moves.size() > 0) {
                    // System.out.println(Piece.toPosition(i, j) + "->" + moves.get(0));
                    return false;
                }
            }
        return true;
    }

    /**
     * @param row        row of piece
     * @param col        column of piece
     * @param enemyColor enemy's color
     * @return returns if piece is under attack
     */
    public boolean isUnderAttack(int row, int col, char enemyColor) {
        return isUnderAttack(Piece.toPosition(row, col), enemyColor);
    }

    /**
     * @param target     position of piece
     * @param enemyColor enemy's color
     * @return returns if piece is under attack
     */
    public boolean isUnderAttack(String target, char enemyColor) {
        if (target == null)
            return false;
        for (int i = board.length - 1; i >= 0; i--)
            for (int j = 0; j < board[0].length; j++) {
                Piece curr = getPiece(i, j);
                if (curr != null && curr.color == enemyColor && curr.getAttackPos(i, j, this).contains(target))
                    return true;
            }
        return false;
    }

    public void main() throws IOException {
        Chess test = new Chess();

        // test.setPiece("a1", new King('w'));
        // test.setPiece("c2", new Pawn('w'));
        // test.setPiece("d2", new Pawn('w'));
        // test.setPiece("c3", new Pawn('b'));
        // test.setPiece("c4", new Pawn('b'));
        // test.setPiece("h8", new King('b'));
        // test.setPiece("f7", new Queen('w'));
        // test.setPiece("g6", new Queen('w'));

        // test.setPiece("g1", new King('w'));
        // test.setPiece("g8", new King('b'));
        // test.setPiece("g2", new Rook('w'));
        // test.setPiece("g4", new Pawn('b'));
        // test.setPiece("f2", new Pawn('w'));

        test.setup();

//        System.out.println(test);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // BufferedReader br = new BufferedReader(new BufferedReader(new FileReader(new
        // File("input"))));

        test.startGame(br);
        br.close();
        System.exit(0); // weird JVM bug
    }

    /**
     * starts the game, returns when game finished or end of file
     *
     * @param br BufferedReader, from stdin or file
     * @throws IOException if file not found
     */
    public void startGame(BufferedReader br) throws IOException {
        String line = "";
        while (winner == null) {
            promotion = 0;
            String currColor = colorToMove == 'w' ? "White" : "Black";
            String enemyColor = colorToMove == 'w' ? "Black" : "White";
//            System.out.print(currColor + "'s move: ");
            line = br.readLine();
            if (line == null)
                break;
            // System.out.println(line);
            String[] input = line.split(" ");
            if (input.length == 1) {
                if (input[0].equalsIgnoreCase("resign")) {
                    winner = enemyColor;
                    break;
                } else if (input[0].equalsIgnoreCase("draw") && drawPrompt) {
//                    System.out.println("Draw");
                    return;
                }
//                System.out.println("\nIllegal move, try again");
            } else if (input.length == 2 && input[0].length() == 2 && input[1].length() == 2) {
                if (move(input[0], input[1]) == false) {
//                    System.out.println("\nIllegal move, try again");
                }
            } else if (input.length == 3 && input[0].length() == 2 && input[1].length() == 2) {
                if (input[2].equalsIgnoreCase("draw?")) {
                    if (move(input[0], input[1]) == false)
//                        System.out.println("\nIllegal move, try again");
                        drawPrompt = true;
                } else if (input[2].length() == 1 && "QNBR".indexOf(input[2].toUpperCase()) >= 0) {
                    promotion = input[2].toUpperCase().charAt(0);
                    if (move(input[0], input[1]) == false) {
//                        System.out.println("\nIllegal move, try again");
                    }
                } else {
//                    System.out.println("\nIllegal move, try again");
                }
            } else {
//                System.out.println("\nIllegal move, try again");
            }
        }
        if (winner.equals("White") || winner.equals("Black")) {
//            System.out.println(winner + " wins");
        } else if (winner != null) {
//            System.out.println(winner);
        }
    }

    /**
     * set up the chess board
     */
    public void setup() {
        for (int i = 0; i < board.length; i++)
            board[1][i] = new Pawn('w');
        for (int i = 0; i < board.length; i++)
            board[6][i] = new Pawn('b');

        board[0][0] = new Rook('w');
        board[0][1] = new Knight('w');
        board[0][2] = new Bishop('w');
        board[0][3] = new Queen('w');
        board[0][4] = new King('w');
        board[0][5] = new Bishop('w');
        board[0][6] = new Knight('w');
        board[0][7] = new Rook('w');

        board[7][0] = new Rook('b');
        board[7][1] = new Knight('b');
        board[7][2] = new Bishop('b');
        board[7][3] = new Queen('b');
        board[7][4] = new King('b');
        board[7][5] = new Bishop('b');
        board[7][6] = new Knight('b');
        board[7][7] = new Rook('b');
    }

    /**
     * @return board
     */
    public Piece[][] getBoard() {
        return board;
    }

    /**
     * @param color color to toggle
     * @return returns the enemy's color
     */
    public char toggleColor(char color) {
        return color == 'w' ? 'b' : 'w';
    }

    /**
     * sets piece in board[row][col]
     *
     * @param row   indicate which row
     * @param col   indicate which column
     * @param piece piece to set
     */
    public void setPiece(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    /**
     * sets piece at the position
     *
     * @param position indicate the position
     * @param piece    piece to set
     */
    public void setPiece(String position, Piece piece) {
        int row = position.charAt(1) - '1';
        int col = position.charAt(0) - 'a';
        setPiece(row, col, piece);
    }

    /**
     * @param row row
     * @param col column
     * @return returns piece at row, column, null if there is nothing or invalid
     * position
     */
    public Piece getPiece(int row, int col) {
        if (Piece.outOfBounds(row, col))
            return null;
        return board[row][col];
    }

    /**
     * @param position position
     * @return returns piece at row, column, null if there is nothing or invalid
     * position
     */
    public Piece getPiece(String position) {
        int row = position.charAt(1) - '1';
        int col = position.charAt(0) - 'a';
        return getPiece(row, col);
    }

    /**
     * removes moves that expose the King under attack
     *
     * @param position position of piece
     * @param moves    list of available moves of piece
     */
    public void filterMoves(String position, List<String> moves) {
        List<String> invalid = new ArrayList<>();
        char color = getPiece(position).color;
        for (String move : moves) {
            move(position, move, true);
            if (isCheck(color))
                invalid.add(move);
            restoreMove();
        }
        moves.removeAll(invalid);
    }

    /**
     * @param position position of piece
     * @return returns list of available moves
     */
    public List<String> getMoves(String position) {
        if (getPiece(position) == null)
            return new ArrayList<>();
        List<String> moves = getPiece(position).getMoves(position, this);
        filterMoves(position, moves);
        return moves;
    }

    /**
     * @param row row of piece
     * @param col column of piece
     * @return returns list of available moves
     */
    public List<String> getMoves(int row, int col) {
        return getMoves(Piece.toPosition(row, col));
    }

    /**
     * @param position position of piece
     * @return returns list of positions can be attacked
     */
    public List<String> getAttackPos(String position) {
        if (getPiece(position) == null)
            return new ArrayList<>();
        List<String> moves = getPiece(position).getAttackPos(position, this);
        filterMoves(position, moves);
        return moves;
    }

    /**
     * @param row row of piece
     * @param col column of piece
     * @return returns list of positions can be attacked
     */
    public List<String> getAttackPos(int row, int col) {
        return getAttackPos(Piece.toPosition(row, col));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = board.length - 1; i >= 0; i--) {
            for (int j = 0; j < board[0].length; j++)
                if (board[i][j] == null) {
                    if (i % 2 == j % 2)
                        sb.append("## ");
                    else
                        sb.append("   ");
                } else
                    sb.append(board[i][j] + " ");
            sb.append(i + 1 + "\n");
        }
        sb.append(" a  b  c  d  e  f  g  h\n");
        return sb.toString();
    }

}
