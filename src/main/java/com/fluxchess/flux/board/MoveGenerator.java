/*
 * Copyright 2007-2014 the original author or authors.
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxchess.flux.board;

import com.fluxchess.flux.Configuration;
import com.fluxchess.flux.evaluation.Evaluation;
import com.fluxchess.flux.search.Search;
import com.fluxchess.jcpi.models.*;

/**
 * Notes: Ideas from Fruit. I specially like the Idea how to handle the state
 * list.
 */
public final class MoveGenerator {

  // Move deltas
  public static final int[] moveDeltaPawn = {16, 17, 15};
  public static final int[] moveDeltaKnight = {+33, +18, -14, -31, -33, -18, +14, +31};
  public static final int[] moveDeltaBishop = {+17, -15, -17, +15};
  public static final int[] moveDeltaRook = {+16, +1, -16, -1};
  public static final int[] moveDeltaQueen = {+16, +17, +1, -15, -16, -17, -1, +15};
  public static final int[] moveDeltaKing = {+16, +17, +1, -15, -16, -17, -1, +15};

  private static final int HISTORYSIZE = Search.MAX_HEIGHT + 1;
  private static final int STATELISTSIZE = 256;

  // States
  private static final int GEN_TRANSPOSITION = 0;
  private static final int GEN_GOODCAPTURE = 1;
  private static final int GEN_KILLER = 2;
  private static final int GEN_NONCAPTURE = 3;
  private static final int GEN_BADCAPTURE = 4;
  private static final int GEN_EVASION = 5;
  private static final int GEN_GOODCAPTURE_QS = 6;
  private static final int GEN_CHECK_QS = 7;
  private static final int GEN_END = 8;

  // State list
  private static final int[] stateList = new int[STATELISTSIZE];
  private static final int stateIndexMain;
  private static final int stateIndexQuiescentAll;
  private static final int stateIndexQuiescentCapture;
  private static final int stateIndexEvasion;

  // Generator
  private final class Generator {
    public int stateIndex = -1;
    public int testState = GEN_END;
    public int transpositionMove = Move.NOMOVE;
    public int primaryKillerMove = Move.NOMOVE;
    public int secondaryKillerMove = Move.NOMOVE;
  }

  // Board
  private final Board board;

  // Tables
  private final KillerTable killerTable;
  private final HistoryTable historyTable;

  // Move list
  private final MoveList moveList;
  private final MoveList tempList;
  private final MoveList nonCaptureMoveList;

  // Generator history
  private final Generator[] generator = new Generator[HISTORYSIZE];
  private int generatorHistory = 0;

  static {
    // Initialize state list
    int index = 0;

    stateIndexMain = index;
    stateList[index++] = GEN_TRANSPOSITION;
    stateList[index++] = GEN_GOODCAPTURE;
    stateList[index++] = GEN_KILLER;
    stateList[index++] = GEN_NONCAPTURE;
    stateList[index++] = GEN_BADCAPTURE;
    stateList[index++] = GEN_END;

    stateIndexQuiescentAll = index;
    stateList[index++] = GEN_GOODCAPTURE_QS;
    stateList[index++] = GEN_CHECK_QS;
    stateList[index++] = GEN_END;

    stateIndexQuiescentCapture = index;
    stateList[index++] = GEN_GOODCAPTURE_QS;
    stateList[index++] = GEN_END;

    stateIndexEvasion = index;
    stateList[index++] = GEN_END;
  }

  public MoveGenerator(Board board, KillerTable killerTable, HistoryTable historyTable) {
    assert board != null;
    assert killerTable != null;
    assert historyTable != null;

    this.board = board;
    this.killerTable = killerTable;
    this.historyTable = historyTable;

    moveList = new MoveList();
    tempList = new MoveList();
    nonCaptureMoveList = new MoveList();

    // Initialize generator
    for (int i = 0; i < generator.length; i++) {
      generator[i] = new Generator();
    }
    generatorHistory = 0;
  }

  public void initializeMain(Attack attack, int height, int transpositionMove) {
    moveList.newList();
    tempList.newList();
    nonCaptureMoveList.newList();
    generatorHistory++;

    generator[generatorHistory].transpositionMove = transpositionMove;
    generator[generatorHistory].primaryKillerMove = killerTable.getPrimaryKiller(height);
    generator[generatorHistory].secondaryKillerMove = killerTable.getSecondaryKiller(height);

    if (attack.isCheck()) {
      generateEvasion(attack);
      moveList.rateEvasion(
        generator[generatorHistory].transpositionMove,
        generator[generatorHistory].primaryKillerMove,
        generator[generatorHistory].secondaryKillerMove,
        historyTable
      );
      moveList.sort();
      generator[generatorHistory].stateIndex = stateIndexEvasion;
      generator[generatorHistory].testState = GEN_EVASION;

      // Set the move number
      attack.numberOfMoves = moveList.size();
    } else {
      generator[generatorHistory].stateIndex = stateIndexMain;
    }
  }

  public void initializeQuiescent(Attack attack, boolean generateCheckingMoves) {
    moveList.newList();
    tempList.newList();
    nonCaptureMoveList.newList();
    generatorHistory++;

    generator[generatorHistory].transpositionMove = Move.NOMOVE;
    generator[generatorHistory].primaryKillerMove = Move.NOMOVE;
    generator[generatorHistory].secondaryKillerMove = Move.NOMOVE;

    if (attack.isCheck()) {
      generateEvasion(attack);
      moveList.rateEvasion(
        generator[generatorHistory].transpositionMove,
        generator[generatorHistory].primaryKillerMove,
        generator[generatorHistory].secondaryKillerMove,
        historyTable
      );
      moveList.sort();
      generator[generatorHistory].stateIndex = stateIndexEvasion;
      generator[generatorHistory].testState = GEN_EVASION;

      // Set the move number
      attack.numberOfMoves = moveList.size();
    } else if (generateCheckingMoves) {
      generator[generatorHistory].stateIndex = stateIndexQuiescentAll;
    } else {
      generator[generatorHistory].stateIndex = stateIndexQuiescentCapture;
    }
  }

  public void destroy() {
    generatorHistory--;
    nonCaptureMoveList.deleteList();
    tempList.deleteList();
    moveList.deleteList();
  }

  public int getNextMove() {
    while (true) {
      if (moveList.index < moveList.tail) {
        int move = moveList.move[moveList.index++];

        switch (generator[generatorHistory].testState) {
          case GEN_TRANSPOSITION:
            assert isLegal(move);
            assert moveList.size() == 1;
            break;
          case GEN_GOODCAPTURE:
            if (move == generator[generatorHistory].transpositionMove) {
              continue;
            }
            if (!isLegal(move)) {
              continue;
            }
            assert Move.getTargetPiece(move) != IntPiece.NOPIECE;
            if (!isGoodCapture(move)) {
              tempList.move[tempList.tail++] = move;
              continue;
            }
            break;
          case GEN_KILLER:
            if (move == generator[generatorHistory].transpositionMove) {
              continue;
            }
            if (!isPseudo(move)) {
              continue;
            }
            if (!isLegal(move)) {
              continue;
            }
            break;
          case GEN_NONCAPTURE:
            if (move == generator[generatorHistory].transpositionMove) {
              continue;
            }
            if (move == generator[generatorHistory].primaryKillerMove) {
              continue;
            }
            if (move == generator[generatorHistory].secondaryKillerMove) {
              continue;
            }
            if (!isLegal(move)) {
              continue;
            }
            break;
          case GEN_BADCAPTURE:
            assert isLegal(move);
            assert !isGoodCapture(move) : board.getBoard().toString() + ", " + Move.toGenericMove(move).toString();
            break;
          case GEN_EVASION:
            assert isLegal(move);
            break;
          case GEN_GOODCAPTURE_QS:
            if (!isLegal(move)) {
              continue;
            }
            assert Move.getTargetPiece(move) != IntPiece.NOPIECE : IntPiece.toGenericPiece(Move.getTargetPiece(move)).toString();
            if (!isGoodCapture(move)) {
              continue;
            }
            break;
          case GEN_CHECK_QS:
            if (!isLegal(move)) {
              continue;
            }
            if (MoveSee.seeMove(move, IntPiece.getColor(Move.getOriginPiece(move))) < 0) {
              continue;
            }
            assert board.isCheckingMove(move) : board.getBoard().toString() + ", " + Move.toGenericMove(move).toString();
            break;
          case GEN_END:
            assert false : stateList[generator[generatorHistory].stateIndex];
            break;
          default:
            assert false : stateList[generator[generatorHistory].stateIndex];
            break;
        }

        return move;
      }

      // Move generation
      int state = stateList[generator[generatorHistory].stateIndex++];
      moveList.clear();

      switch (state) {
        case GEN_TRANSPOSITION:
          if (Configuration.useTranspositionTable) {
            if (generator[generatorHistory].transpositionMove != Move.NOMOVE) {
              moveList.move[moveList.tail++] = generator[generatorHistory].transpositionMove;
            }
            generator[generatorHistory].testState = GEN_TRANSPOSITION;
          } else {
            generator[generatorHistory].transpositionMove = Move.NOMOVE;
          }
          break;
        case GEN_GOODCAPTURE:
          generateCaptures();
          tempList.clear();
          moveList.rateFromMVVLVA();
          moveList.sort();
          generator[generatorHistory].testState = GEN_GOODCAPTURE;
          break;
        case GEN_KILLER:
          if (Configuration.useKillerTable) {
            if (generator[generatorHistory].primaryKillerMove != Move.NOMOVE) {
              moveList.move[moveList.tail++] = generator[generatorHistory].primaryKillerMove;
            }
            if (generator[generatorHistory].secondaryKillerMove != Move.NOMOVE) {
              moveList.move[moveList.tail++] = generator[generatorHistory].secondaryKillerMove;
            }
            generator[generatorHistory].testState = GEN_KILLER;
          } else {
            generator[generatorHistory].primaryKillerMove = Move.NOMOVE;
            generator[generatorHistory].secondaryKillerMove = Move.NOMOVE;
          }
          break;
        case GEN_NONCAPTURE:
          generateNonCaptures();
          if (Configuration.useHistoryTable) {
            moveList.rateFromHistory(historyTable);
            moveList.sort();
          }
          generator[generatorHistory].testState = GEN_NONCAPTURE;
          break;
        case GEN_BADCAPTURE:
          System.arraycopy(tempList.move, tempList.head, moveList.move, moveList.tail, tempList.size());
          moveList.tail += tempList.size();
          generator[generatorHistory].testState = GEN_BADCAPTURE;
          break;
        case GEN_GOODCAPTURE_QS:
          generateCaptures();
          moveList.rateFromMVVLVA();
          moveList.sort();
          generator[generatorHistory].testState = GEN_GOODCAPTURE_QS;
          break;
        case GEN_CHECK_QS:
          generateChecks();
          generator[generatorHistory].testState = GEN_CHECK_QS;
          break;
        case GEN_END:
          return Move.NOMOVE;
        default:
          assert false : state;
          break;
      }
    }
  }

  private boolean isPseudo(int move) {
    int chessmanSquare = Move.getOriginSquare(move);
    int piece = board.board[chessmanSquare];

    // Check chessman
    if (piece == IntPiece.NOPIECE || IntPiece.getChessman(Move.getOriginPiece(move)) != IntPiece.getChessman(piece)) {
      return false;
    }

    int color = IntPiece.getColor(Move.getOriginPiece(move));

    // Check color
    if (color != IntPiece.getColor(piece)) {
      return false;
    }

    assert color == board.activeColor;

    int targetSquare = Move.getTargetSquare(move);

    // Check empty target
    if (board.board[targetSquare] != IntPiece.NOPIECE) {
      return false;
    }

    assert Move.getTargetPiece(move) == IntPiece.NOPIECE;

    int type = Move.getType(move);

    switch (type) {
      case Move.Type.NORMAL:
        break;
      case Move.Type.PAWNDOUBLE:
        int delta;
        if (color == IntColor.WHITE) {
          delta = 16;
        } else {
          assert color == IntColor.BLACK;

          delta = -16;
        }

        if (board.board[chessmanSquare + delta] == IntPiece.NOPIECE) {
          assert board.board[chessmanSquare + 2 * delta] == IntPiece.NOPIECE;
          return true;
        } else {
          return false;
        }
      case Move.Type.PAWNPROMOTION:
      case Move.Type.ENPASSANT:
      case Move.Type.NULL:
        return false;
      case Move.Type.CASTLING:
        switch (targetSquare) {
          case Square.g1:
            // Do not test g1 whether it is attacked as we will test it in isLegal()
            if (board.castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE
              && board.board[Square.f1] == IntPiece.NOPIECE
              && board.board[Square.g1] == IntPiece.NOPIECE
              && !board.isAttacked(Square.f1, IntColor.BLACK)) {
              assert board.board[Square.e1] == IntPiece.WHITEKING;
              assert board.board[Square.h1] == IntPiece.WHITEROOK;

              return true;
            }
            break;
          case Square.c1:
            // Do not test c1 whether it is attacked as we will test it in isLegal()
            if (board.castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE
              && board.board[Square.b1] == IntPiece.NOPIECE
              && board.board[Square.c1] == IntPiece.NOPIECE
              && board.board[Square.d1] == IntPiece.NOPIECE
              && !board.isAttacked(Square.d1, IntColor.BLACK)) {
              assert board.board[Square.e1] == IntPiece.WHITEKING;
              assert board.board[Square.a1] == IntPiece.WHITEROOK;

              return true;
            }
            break;
          case Square.g8:
            // Do not test g8 whether it is attacked as we will test it in isLegal()
            if (board.castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE
              && board.board[Square.f8] == IntPiece.NOPIECE
              && board.board[Square.g8] == IntPiece.NOPIECE
              && !board.isAttacked(Square.f8, IntColor.WHITE)) {
              assert board.board[Square.e8] == IntPiece.BLACKKING;
              assert board.board[Square.h8] == IntPiece.BLACKROOK;

              return true;
            }
            break;
          case Square.c8:
            // Do not test c8 whether it is attacked as we will test it in isLegal()
            if (board.castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE
              && board.board[Square.b8] == IntPiece.NOPIECE
              && board.board[Square.c8] == IntPiece.NOPIECE
              && board.board[Square.d8] == IntPiece.NOPIECE
              && !board.isAttacked(Square.d8, IntColor.WHITE)) {
              assert board.board[Square.e8] == IntPiece.BLACKKING;
              assert board.board[Square.a8] == IntPiece.BLACKROOK;

              return true;
            }
            break;
          default:
            assert false : Move.toGenericMove(move);
            break;
        }
        break;
      default:
        assert false : type;
        break;
    }

    int chessman = IntPiece.getChessman(piece);
    assert chessman == IntPiece.getChessman(Move.getOriginPiece(move));

    // Check pawn move
    if (chessman == IntChessman.PAWN) {
      int delta;
      if (color == IntColor.WHITE) {
        delta = 16;
      } else {
        assert color == IntColor.BLACK;

        delta = -16;
      }

      assert board.board[chessmanSquare + delta] == IntPiece.NOPIECE;
      return true;
    }

    // Check normal move
    if (board.canAttack(chessman, color, chessmanSquare, targetSquare)) {
      return true;
    }

    return false;
  }

  private boolean isLegal(int move) {
    // Slow test for en passant
    if (Move.getType(move) == Move.Type.ENPASSANT) {
      int activeColor = board.activeColor;
      board.makeMove(move);
      boolean isCheck = board.getAttack(activeColor).isCheck();
      board.undoMove(move);

      return !isCheck;
    }

    int chessmanColor = IntPiece.getColor(Move.getOriginPiece(move));

    // Special test for king
    if (IntPiece.getChessman(Move.getOriginPiece(move)) == IntChessman.KING) {
      return !board.isAttacked(Move.getTargetSquare(move), IntColor.opposite(chessmanColor));
    }

    assert Long.bitCount(board.kingList[chessmanColor]) == 1;
    if (board.isPinned(Move.getOriginSquare(move), chessmanColor)) {
      // We are pinned. Test if we move on the line.
      int kingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[chessmanColor]));
      int attackDeltaOrigin = Attack.deltas[kingSquare - Move.getOriginSquare(move) + 127];
      int attackDeltaTarget = Attack.deltas[kingSquare - Move.getTargetSquare(move) + 127];
      return attackDeltaOrigin == attackDeltaTarget;
    }

    return true;
  }

  private boolean isGoodCapture(int move) {
    if (Move.getType(move) == Move.Type.PAWNPROMOTION) {
      return Move.getPromotion(move) == IntChessman.QUEEN;
    }

    int chessman = IntPiece.getChessman(Move.getOriginPiece(move));
    int target = Move.getTargetPiece(move);

    assert chessman != IntChessman.NOCHESSMAN;
    assert target != IntPiece.NOPIECE;

    if (Evaluation.getValueFromChessman(chessman) <= Evaluation.getValueFromPiece(target)) {
      return true;
    }

    return MoveSee.seeMove(move, IntPiece.getColor(Move.getOriginPiece(move))) >= 0;
  }

  private void generateNonCaptures() {
    int activeColor = board.activeColor;

    for (long squares = board.pawnList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      addPawnNonCaptureMovesTo(board.board[square], activeColor, square);
    }
    System.arraycopy(nonCaptureMoveList.move, nonCaptureMoveList.head, moveList.move, moveList.tail, nonCaptureMoveList.size());
    moveList.tail += nonCaptureMoveList.size();
    assert Long.bitCount(board.kingList[activeColor]) == 1;
    int square = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[activeColor]));
    int king = board.board[square];
    addCastlingMoveIfAllowed(king, square, activeColor);
  }

  private void generateCaptures() {
    int activeColor = board.activeColor;
    int oppositeColor = IntColor.opposite(activeColor);

    for (long squares = board.pawnList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      addPawnCaptureMovesTo(board.board[square], activeColor, square);
    }
    for (long squares = board.knightList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      addDefaultCaptureMovesTo(board.board[square], square, false, moveDeltaKnight, Square.NOSQUARE, oppositeColor);
    }
    for (long squares = board.bishopList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      addDefaultCaptureMovesTo(board.board[square], square, true, moveDeltaBishop, Square.NOSQUARE, oppositeColor);
    }
    for (long squares = board.rookList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      addDefaultCaptureMovesTo(board.board[square], square, true, moveDeltaRook, Square.NOSQUARE, oppositeColor);
    }
    for (long squares = board.queenList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      addDefaultCaptureMovesTo(board.board[square], square, true, moveDeltaQueen, Square.NOSQUARE, oppositeColor);
    }
    assert Long.bitCount(board.kingList[activeColor]) == 1;
    int square = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[activeColor]));
    addDefaultCaptureMovesTo(board.board[square], square, false, moveDeltaKing, Square.NOSQUARE, oppositeColor);
  }

  private void generateEvasion(Attack attack) {
    int activeColor = board.activeColor;
    assert Long.bitCount(board.kingList[activeColor]) == 1;
    int kingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[activeColor]));
    int king = board.board[kingSquare];
    int attackerColor = IntColor.opposite(activeColor);
    int oppositeColor = IntColor.opposite(IntPiece.getColor(king));
    int moveTemplate = Move.valueOf(Move.Type.NORMAL, kingSquare, kingSquare, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);

    // Generate king moves
    for (int delta : moveDeltaKing) {
      assert attack.count > 0;
      boolean isOnCheckLine = false;
      for (int i = 0; i < attack.count; i++) {
        if (IntChessman.isSliding(IntPiece.getChessman(board.board[attack.square[i]])) && delta == attack.delta[i]) {
          isOnCheckLine = true;
          break;
        }
      }
      if (!isOnCheckLine) {
        int targetSquare = kingSquare + delta;
        if ((targetSquare & 0x88) == 0 && !board.isAttacked(targetSquare, attackerColor)) {
          int target = board.board[targetSquare];
          if (target == IntPiece.NOPIECE) {
            int move = Move.setTargetSquare(moveTemplate, targetSquare);
            moveList.move[moveList.tail++] = move;
          } else {
            if (IntPiece.getColor(target) == oppositeColor) {
              assert IntPiece.getChessman(target) != IntChessman.KING;
              int move = Move.setTargetSquareAndPiece(moveTemplate, targetSquare, target);
              moveList.move[moveList.tail++] = move;
            }
          }
        }
      }
    }

    // Double check
    if (attack.count >= 2) {
      return;
    }

    assert attack.count == 1;

    int attackerSquare = attack.square[0];
    int attacker = board.board[attackerSquare];

    // Capture the attacker

    addPawnCaptureMovesToTarget(activeColor, attacker, attackerSquare);
    for (long squares = board.knightList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (!board.isPinned(square, activeColor)) {
        addDefaultCaptureMovesTo(board.board[square], square, false, moveDeltaKnight, attackerSquare, oppositeColor);
      }
    }
    for (long squares = board.bishopList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (!board.isPinned(square, activeColor)) {
        addDefaultCaptureMovesTo(board.board[square], square, true, moveDeltaBishop, attackerSquare, oppositeColor);
      }
    }
    for (long squares = board.rookList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (!board.isPinned(square, activeColor)) {
        addDefaultCaptureMovesTo(board.board[square], square, true, moveDeltaRook, attackerSquare, oppositeColor);
      }
    }
    for (long squares = board.queenList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      if (!board.isPinned(square, activeColor)) {
        addDefaultCaptureMovesTo(board.board[square], square, true, moveDeltaQueen, attackerSquare, oppositeColor);
      }
    }

    int attackDelta = attack.delta[0];

    // Interpose a chessman
    if (IntChessman.isSliding(IntPiece.getChessman(board.board[attackerSquare]))) {
      int targetSquare = attackerSquare + attackDelta;
      while (targetSquare != kingSquare) {
        assert (targetSquare & 0x88) == 0;
        assert board.board[targetSquare] == IntPiece.NOPIECE;

        addPawnNonCaptureMovesToTarget(activeColor, targetSquare);
        for (long squares = board.knightList[activeColor]; squares != 0; squares &= squares - 1) {
          int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
          if (!board.isPinned(square, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[square], square, moveDeltaKnight, targetSquare);
          }
        }
        for (long squares = board.bishopList[activeColor]; squares != 0; squares &= squares - 1) {
          int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
          if (!board.isPinned(square, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[square], square, moveDeltaBishop, targetSquare);
          }
        }
        for (long squares = board.rookList[activeColor]; squares != 0; squares &= squares - 1) {
          int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
          if (!board.isPinned(square, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[square], square, moveDeltaRook, targetSquare);
          }
        }
        for (long squares = board.queenList[activeColor]; squares != 0; squares &= squares - 1) {
          int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
          if (!board.isPinned(square, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[square], square, moveDeltaQueen, targetSquare);
          }
        }

        targetSquare += attackDelta;
      }
    }
  }

  private void generateChecks() {
    int activeColor = board.activeColor;

    assert Long.bitCount(board.kingList[IntColor.opposite(activeColor)]) == 1;
    int enemyKingColor = IntColor.opposite(activeColor);
    int enemyKingSquare = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[enemyKingColor]));

    for (long squares = board.pawnList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      boolean isPinned = board.isPinned(square, enemyKingColor);
      addPawnNonCaptureCheckMovesTo(board.board[square], activeColor, square, enemyKingSquare, isPinned);
    }
    for (long squares = board.knightList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      boolean isPinned = board.isPinned(square, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[square], IntChessman.KNIGHT, activeColor, square, moveDeltaKnight, enemyKingSquare, isPinned);
    }
    for (long squares = board.bishopList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      boolean isPinned = board.isPinned(square, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[square], IntChessman.BISHOP, activeColor, square, moveDeltaBishop, enemyKingSquare, isPinned);
    }
    for (long squares = board.rookList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      boolean isPinned = board.isPinned(square, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[square], IntChessman.ROOK, activeColor, square, moveDeltaRook, enemyKingSquare, isPinned);
    }
    for (long squares = board.queenList[activeColor]; squares != 0; squares &= squares - 1) {
      int square = Square.toX88Square(Long.numberOfTrailingZeros(squares));
      boolean isPinned = board.isPinned(square, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[square], IntChessman.QUEEN, activeColor, square, moveDeltaQueen, enemyKingSquare, isPinned);
    }
    assert Long.bitCount(board.kingList[activeColor]) == 1;
    int square = Square.toX88Square(Long.numberOfTrailingZeros(board.kingList[activeColor]));
    int king = board.board[square];
    boolean isPinned = board.isPinned(square, enemyKingColor);
    addDefaultNonCaptureCheckMovesTo(king, IntChessman.KING, activeColor, square, moveDeltaKing, enemyKingSquare, isPinned);
    addCastlingCheckMoveIfAllowed(king, square, activeColor, enemyKingSquare);
  }

  /**
   * Add non-capturing moves from the move delta of the chessman.
   *
   * @param piece          the piece.
   * @param moveDelta      the move delta list.
   * @param endSquare the end square.
   */
  private void addDefaultNonCaptureMovesTo(int piece, int square, int[] moveDelta, int endSquare) {
    assert moveDelta != null;

    boolean sliding = IntChessman.isSliding(IntPiece.getChessman(piece));
    int moveTemplate = Move.valueOf(Move.Type.NORMAL, square, square, piece, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);

    for (int delta : moveDelta) {
      int targetSquare = square + delta;

      // Get moves to empty squares
      while ((targetSquare & 0x88) == 0 && board.board[targetSquare] == IntPiece.NOPIECE) {
        if (endSquare == Square.NOSQUARE || targetSquare == endSquare) {
          int move = Move.setTargetSquare(moveTemplate, targetSquare);
          moveList.move[moveList.tail++] = move;
        }

        if (!sliding) {
          break;
        }

        targetSquare += delta;
      }
    }
  }

  /**
   * Add non-capturing check moves from the move delta of the chessman.
   *
   * @param piece        the piece.
   * @param moveDelta    the move delta list.
   * @param kingSquare the square of the enemy king.
   * @param isPinned     whether the chessman is pinned.
   */
  private void addDefaultNonCaptureCheckMovesTo(int piece, int chessman, int chessmanColor, int chessmanSquare, int[] moveDelta, int kingSquare, boolean isPinned) {
    assert moveDelta != null;

    boolean sliding = IntChessman.isSliding(IntPiece.getChessman(piece));
    int attackDeltaOrigin = Attack.deltas[kingSquare - chessmanSquare + 127];
    int moveTemplate = Move.valueOf(Move.Type.NORMAL, chessmanSquare, chessmanSquare, piece, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);

    for (int delta : moveDelta) {
      int targetSquare = chessmanSquare + delta;

      // Get moves to empty squares
      while ((targetSquare & 0x88) == 0 && board.board[targetSquare] == IntPiece.NOPIECE) {
        if (isPinned) {
          // We are pinned. Test if we move on the line.
          int attackDeltaTarget = Attack.deltas[kingSquare - targetSquare + 127];
          if (attackDeltaOrigin != attackDeltaTarget) {
            int move = Move.setTargetSquare(moveTemplate, targetSquare);
            moveList.move[moveList.tail++] = move;
          }
        } else if (board.canAttack(chessman, chessmanColor, targetSquare, kingSquare)) {
          int move = Move.setTargetSquare(moveTemplate, targetSquare);
          moveList.move[moveList.tail++] = move;
        }

        if (!sliding) {
          break;
        }

        targetSquare += delta;
      }
    }
  }

  /**
   * Add capturing moves from the move delta of the chessman.
   *
   * @param piece          the piece.
   * @param moveDelta      the move delta list.
   * @param targetSquare the target square.
   */
  private void addDefaultCaptureMovesTo(int piece, int originSquare, boolean sliding, int[] moveDelta, int targetSquare, int oppositeColor) {
    assert piece != IntPiece.NOPIECE;
    assert moveDelta != null;

    assert IntChessman.isSliding(IntPiece.getChessman(piece)) == sliding;
    assert oppositeColor == IntColor.opposite(IntPiece.getColor(piece));
    int moveTemplate = Move.valueOf(Move.Type.NORMAL, originSquare, originSquare, piece, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);

    for (int delta : moveDelta) {
      int end = originSquare + delta;

      // Get moves to empty squares
      while ((end & 0x88) == 0) {
        int target = board.board[end];
        if (target == IntPiece.NOPIECE) {
          if (targetSquare == Square.NOSQUARE || end == targetSquare) {
            int move = Move.setTargetSquare(moveTemplate, end);
            nonCaptureMoveList.move[nonCaptureMoveList.tail++] = move;
          }

          if (!sliding) {
            break;
          }

          end += delta;
        } else {
          if (targetSquare == Square.NOSQUARE || end == targetSquare) {
            // Get the move to the square the next chessman is standing on
            if (IntPiece.getColor(target) == oppositeColor
              && IntPiece.getChessman(target) != IntChessman.KING) {
              int move = Move.setTargetSquareAndPiece(moveTemplate, end, target);
              moveList.move[moveList.tail++] = move;
            }
          }
          break;
        }
      }
    }
  }

  /**
   * Add non-capturing moves of the pawn.
   *
   * @param pawn         the pawn.
   * @param pawnColor    the pawn color.
   * @param pawnSquare the pawn square.
   */
  private void addPawnNonCaptureMovesTo(int pawn, int pawnColor, int pawnSquare) {
    assert pawn != IntPiece.NOPIECE;
    assert IntPiece.getChessman(pawn) == IntChessman.PAWN;
    assert IntPiece.getColor(pawn) == pawnColor;
    assert (pawnSquare & 0x88) == 0;
    assert board.board[pawnSquare] == pawn;

    int delta = moveDeltaPawn[0];
    if (pawnColor == IntColor.BLACK) {
      delta *= -1;
    }

    // Move one square forward
    int targetSquare = pawnSquare + delta;
    if ((targetSquare & 0x88) == 0 && board.board[targetSquare] == IntPiece.NOPIECE) {
      // GenericRank.R8 = square > 111
      // GenericRank.R1 = square < 8
      if ((targetSquare > 111 && pawnColor == IntColor.WHITE)
        || (targetSquare < 8 && pawnColor == IntColor.BLACK)) {
        int moveTemplate = Move.valueOf(Move.Type.PAWNPROMOTION, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
        int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
        moveList.move[moveList.tail++] = move;
        move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
        moveList.move[moveList.tail++] = move;
        move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
        moveList.move[moveList.tail++] = move;
        move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
        moveList.move[moveList.tail++] = move;
      } else {
        int move = Move.valueOf(Move.Type.NORMAL, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
        moveList.move[moveList.tail++] = move;

        // Move two squares forward
        targetSquare += delta;
        if ((targetSquare & 0x88) == 0 && board.board[targetSquare] == IntPiece.NOPIECE) {
          // GenericRank.R4 = end >>> 4 == 3
          // GenericRank.R5 = end >>> 4 == 4
          if (((targetSquare >>> 4) == 3 && pawnColor == IntColor.WHITE)
            || ((targetSquare >>> 4) == 4 && pawnColor == IntColor.BLACK)) {
            assert ((pawnSquare >>> 4) == 1 && (targetSquare >>> 4) == 3 && pawnColor == IntColor.WHITE) || ((pawnSquare >>> 4) == 6 && (targetSquare >>> 4) == 4 && pawnColor == IntColor.BLACK);

            move = Move.valueOf(Move.Type.PAWNDOUBLE, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
            moveList.move[moveList.tail++] = move;
          }
        }
      }
    }
  }

  /**
   * Add non-capturing moves of pawns to the target square.
   *
   * @param pawnColor      the pawn color.
   * @param targetSquare the target square.
   */
  private void addPawnNonCaptureMovesToTarget(int pawnColor, int targetSquare) {
    assert pawnColor == IntColor.WHITE || pawnColor == IntColor.BLACK;
    assert (targetSquare & 0x88) == 0;
    assert board.board[targetSquare] == IntPiece.NOPIECE;

    int delta = moveDeltaPawn[0];
    int pawnPiece = IntPiece.BLACKPAWN;
    if (pawnColor == IntColor.WHITE) {
      delta *= -1;
      pawnPiece = IntPiece.WHITEPAWN;
    }

    // Move one square backward
    int pawnSquare = targetSquare + delta;
    if ((pawnSquare & 0x88) == 0) {
      int pawn = board.board[pawnSquare];
      if (pawn != IntPiece.NOPIECE) {
        if (pawn == pawnPiece) {
          // We found a valid pawn

          if (!board.isPinned(pawnSquare, pawnColor)) {
            // GenericRank.R8 = square > 111
            // GenericRank.R1 = square < 8
            if ((targetSquare > 111 && pawnColor == IntColor.WHITE)
              || (targetSquare < 8 && pawnColor == IntColor.BLACK)) {
              int moveTemplate = Move.valueOf(Move.Type.PAWNPROMOTION, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
              int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = Move.valueOf(Move.Type.NORMAL, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
              moveList.move[moveList.tail++] = move;
            }
          }
        }
      } else {
        // Move two squares backward
        pawnSquare += delta;
        if ((pawnSquare & 0x88) == 0) {
          // GenericRank.R4 = end >>> 4 == 3
          // GenericRank.R5 = end >>> 4 == 4
          if (((pawnSquare >>> 4) == 1 && pawnColor == IntColor.WHITE)
            || ((pawnSquare >>> 4) == 6 && pawnColor == IntColor.BLACK)) {
            assert ((pawnSquare >>> 4) == 1 && (targetSquare >>> 4) == 3 && pawnColor == IntColor.WHITE) || ((pawnSquare >>> 4) == 6 && (targetSquare >>> 4) == 4 && pawnColor == IntColor.BLACK);

            pawn = board.board[pawnSquare];
            if (pawn != IntPiece.NOPIECE && pawn == pawnPiece) {
              if (!board.isPinned(pawnSquare, pawnColor)) {
                int move = Move.valueOf(Move.Type.PAWNDOUBLE, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
                moveList.move[moveList.tail++] = move;
              }
            }
          }
        }
      }
    }
  }

  /**
   * Add non-capturing check moves of the pawn to the target square.
   *
   * @param pawn         the IntChessman.
   * @param kingSquare the enemy king square.
   * @param isPinned     whether the pawn is pinned.
   */
  private void addPawnNonCaptureCheckMovesTo(int pawn, int color, int pawnSquare, int kingSquare, boolean isPinned) {
    assert pawn != IntPiece.NOPIECE;
    assert (kingSquare & 0x88) == 0;

    int delta = moveDeltaPawn[0];
    if (color == IntColor.BLACK) {
      delta *= -1;
    }

    // Move one square forward
    int targetSquare = pawnSquare + delta;
    if ((targetSquare & 0x88) == 0 && board.board[targetSquare] == IntPiece.NOPIECE) {
      // GenericRank.R8 = square > 111
      // GenericRank.R1 = square < 8
      if ((targetSquare > 111 && color == IntColor.WHITE)
        || (targetSquare < 8 && color == IntColor.BLACK)) {
        int moveTemplate = Move.valueOf(Move.Type.PAWNPROMOTION, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
        int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
        board.makeMove(move);
        boolean isCheck = board.isAttacked(kingSquare, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
        move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
        board.makeMove(move);
        isCheck = board.isAttacked(kingSquare, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
        move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
        board.makeMove(move);
        isCheck = board.isAttacked(kingSquare, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
        move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
        board.makeMove(move);
        isCheck = board.isAttacked(kingSquare, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
      } else {
        if (isPinned) {
          // We are pinned. Test if we move on the line.
          int attackDeltaOrigin = Attack.deltas[kingSquare - pawnSquare + 127];
          int attackDeltaTarget = Attack.deltas[kingSquare - targetSquare + 127];
          if (attackDeltaOrigin != attackDeltaTarget) {
            int move = Move.valueOf(Move.Type.NORMAL, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
            moveList.move[moveList.tail++] = move;
          }
        } else if (board.canAttack(IntChessman.PAWN, color, targetSquare, kingSquare)) {
          int move = Move.valueOf(Move.Type.NORMAL, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
          moveList.move[moveList.tail++] = move;
        }

        // Move two squares forward
        targetSquare += delta;
        if ((targetSquare & 0x88) == 0 && board.board[targetSquare] == IntPiece.NOPIECE) {
          // GenericRank.R4 = end >>> 4 == 3
          // GenericRank.R5 = end >>> 4 == 4
          if (((targetSquare >>> 4) == 3 && color == IntColor.WHITE)
            || ((targetSquare >>> 4) == 4 && color == IntColor.BLACK)) {
            assert ((pawnSquare >>> 4) == 1 && (targetSquare >>> 4) == 3 && color == IntColor.WHITE) || ((pawnSquare >>> 4) == 6 && (targetSquare >>> 4) == 4 && color == IntColor.BLACK);

            if (isPinned) {
              // We are pinned. Test if we move on the line.
              int attackDeltaOrigin = Attack.deltas[kingSquare - pawnSquare + 127];
              int attackDeltaTarget = Attack.deltas[kingSquare - targetSquare + 127];
              if (attackDeltaOrigin != attackDeltaTarget) {
                int move = Move.valueOf(Move.Type.PAWNDOUBLE, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
                moveList.move[moveList.tail++] = move;
              }
            } else if (board.canAttack(IntChessman.PAWN, color, targetSquare, kingSquare)) {
              int move = Move.valueOf(Move.Type.PAWNDOUBLE, pawnSquare, targetSquare, pawn, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
              moveList.move[moveList.tail++] = move;
            }
          }
        }
      }
    }
  }

  /**
   * Add capturing moves of the pawn.
   *
   * @param pawn         the pawn.
   * @param pawnColor    the pawn color.
   * @param pawnSquare the pawn square.
   */
  private void addPawnCaptureMovesTo(int pawn, int pawnColor, int pawnSquare) {
    assert pawn != IntPiece.NOPIECE;
    assert IntPiece.getChessman(pawn) == IntChessman.PAWN;
    assert IntPiece.getColor(pawn) == pawnColor;
    assert (pawnSquare & 0x88) == 0;
    assert board.board[pawnSquare] == pawn;

    for (int i = 1; i < moveDeltaPawn.length; i++) {
      int delta = moveDeltaPawn[i];
      if (pawnColor == IntColor.BLACK) {
        delta *= -1;
      }

      int targetSquare = pawnSquare + delta;
      if ((targetSquare & 0x88) == 0) {
        int target = board.board[targetSquare];
        if (target != IntPiece.NOPIECE) {
          if (IntColor.opposite(IntPiece.getColor(target)) == pawnColor
            && IntPiece.getChessman(target) != IntChessman.KING) {
            // Capturing move

            // GenericRank.R8 = square > 111
            // GenericRank.R1 = square < 8
            if ((targetSquare > 111 && pawnColor == IntColor.WHITE)
              || (targetSquare < 8 && pawnColor == IntColor.BLACK)) {
              int moveTemplate = Move.valueOf(Move.Type.PAWNPROMOTION, pawnSquare, targetSquare, pawn, target, IntChessman.NOCHESSMAN);
              int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = Move.valueOf(Move.Type.NORMAL, pawnSquare, targetSquare, pawn, target, IntChessman.NOCHESSMAN);
              moveList.move[moveList.tail++] = move;
            }
          }
        } else if (targetSquare == board.enPassantSquare) {
          // En passant move
          assert board.enPassantSquare != Square.NOSQUARE;
          assert ((targetSquare >>> 4) == 2 && pawnColor == IntColor.BLACK) || ((targetSquare >>> 4) == 5 && pawnColor == IntColor.WHITE);

          // Calculate the en passant square
          int enPassantTargetSquare;
          if (pawnColor == IntColor.WHITE) {
            enPassantTargetSquare = targetSquare - 16;
          } else {
            enPassantTargetSquare = targetSquare + 16;
          }
          target = board.board[enPassantTargetSquare];
          assert IntPiece.getChessman(target) == IntChessman.PAWN;
          assert IntPiece.getColor(target) == IntColor.opposite(pawnColor);

          int move = Move.valueOf(Move.Type.ENPASSANT, pawnSquare, targetSquare, pawn, target, IntChessman.NOCHESSMAN);
          moveList.move[moveList.tail++] = move;
        }
      }
    }
  }

  /**
   * Add capturing moves of pawns to the target square.
   *
   * @param pawnColor      the color of the attacking pawn.
   * @param target         the target chessman.
   * @param targetSquare the target square.
   */
  private void addPawnCaptureMovesToTarget(int pawnColor, int target, int targetSquare) {
    assert pawnColor == IntColor.WHITE || pawnColor == IntColor.BLACK;
    assert target != IntPiece.NOPIECE;
    assert IntPiece.getColor(target) == IntColor.opposite(pawnColor);
    assert (targetSquare & 0x88) == 0;
    assert board.board[targetSquare] != IntPiece.NOPIECE;
    assert board.board[targetSquare] == target;
    assert IntPiece.getChessman(board.board[targetSquare]) != IntChessman.KING;
    assert IntColor.opposite(IntPiece.getColor(board.board[targetSquare])) == pawnColor;

    int pawnPiece = IntPiece.BLACKPAWN;
    int enPassantDelta = -16;
    if (pawnColor == IntColor.WHITE) {
      pawnPiece = IntPiece.WHITEPAWN;
      enPassantDelta = 16;
    }
    int enPassantSquare = targetSquare + enPassantDelta;

    for (int i = 1; i < moveDeltaPawn.length; i++) {
      int delta = moveDeltaPawn[i];
      if (pawnColor == IntColor.WHITE) {
        delta *= -1;
      }

      int pawnSquare = targetSquare + delta;
      if ((pawnSquare & 0x88) == 0) {
        int pawn = board.board[pawnSquare];
        if (pawn != IntPiece.NOPIECE && pawn == pawnPiece) {
          // We found a valid pawn

          if (!board.isPinned(pawnSquare, pawnColor)) {
            // GenericRank.R8 = square > 111
            // GenericRank.R1 = square < 8
            if ((targetSquare > 111 && pawnColor == IntColor.WHITE)
              || (targetSquare < 8 && pawnColor == IntColor.BLACK)) {
              int moveTemplate = Move.valueOf(Move.Type.PAWNPROMOTION, pawnSquare, targetSquare, pawn, target, IntChessman.NOCHESSMAN);
              int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = Move.valueOf(Move.Type.NORMAL, pawnSquare, targetSquare, pawn, target, IntChessman.NOCHESSMAN);
              moveList.move[moveList.tail++] = move;
            }
          }
        }
        if (enPassantSquare == board.enPassantSquare) {
          // En passant move
          pawnSquare = pawnSquare + enPassantDelta;
          assert (enPassantSquare & 0x88) == 0;
          assert (pawnSquare & 0x88) == 0;

          pawn = board.board[pawnSquare];
          if (pawn != IntPiece.NOPIECE && pawn == pawnPiece) {
            // We found a valid pawn which can do a en passant move

            if (!board.isPinned(pawnSquare, pawnColor)) {
              assert ((enPassantSquare >>> 4) == 2 && pawnColor == IntColor.BLACK) || ((enPassantSquare >>> 4) == 5 && pawnColor == IntColor.WHITE);
              assert IntPiece.getChessman(target) == IntChessman.PAWN;

              int move = Move.valueOf(Move.Type.ENPASSANT, pawnSquare, enPassantSquare, pawn, target, IntChessman.NOCHESSMAN);
              moveList.move[moveList.tail++] = move;
            }
          }
        }
      }
    }
  }

  /**
   * Add the castling moves to the move list.
   *
   * @param king the king.
   */
  private void addCastlingMoveIfAllowed(int king, int kingSquare, int color) {
    assert king != IntPiece.NOPIECE;
    assert (kingSquare & 0x88) == 0;

    if (color == IntColor.WHITE) {
      // Do not test g1 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE
        && board.board[Square.f1] == IntPiece.NOPIECE
        && board.board[Square.g1] == IntPiece.NOPIECE
        && !board.isAttacked(Square.f1, IntColor.BLACK)) {
        assert board.board[Square.e1] == IntPiece.WHITEKING;
        assert board.board[Square.h1] == IntPiece.WHITEROOK;

        int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.g1, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
        moveList.move[moveList.tail++] = move;
      }
      // Do not test c1 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE
        && board.board[Square.b1] == IntPiece.NOPIECE
        && board.board[Square.c1] == IntPiece.NOPIECE
        && board.board[Square.d1] == IntPiece.NOPIECE
        && !board.isAttacked(Square.d1, IntColor.BLACK)) {
        assert board.board[Square.e1] == IntPiece.WHITEKING;
        assert board.board[Square.a1] == IntPiece.WHITEROOK;

        int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.c1, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
        moveList.move[moveList.tail++] = move;
      }
    } else {
      assert color == IntColor.BLACK;

      // Do not test g8 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE
        && board.board[Square.f8] == IntPiece.NOPIECE
        && board.board[Square.g8] == IntPiece.NOPIECE
        && !board.isAttacked(Square.f8, IntColor.WHITE)) {
        assert board.board[Square.e8] == IntPiece.BLACKKING;
        assert board.board[Square.h8] == IntPiece.BLACKROOK;

        int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.g8, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
        moveList.move[moveList.tail++] = move;
      }
      // Do not test c8 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE
        && board.board[Square.b8] == IntPiece.NOPIECE
        && board.board[Square.c8] == IntPiece.NOPIECE
        && board.board[Square.d8] == IntPiece.NOPIECE
        && !board.isAttacked(Square.d8, IntColor.WHITE)) {
        assert board.board[Square.e8] == IntPiece.BLACKKING;
        assert board.board[Square.a8] == IntPiece.BLACKROOK;

        int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.c8, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
        moveList.move[moveList.tail++] = move;
      }
    }
  }

  /**
   * Add the castling check moves to the move list.
   *
   * @param king           the king.
   * @param targetSquare the square of the enemy king.
   */
  private void addCastlingCheckMoveIfAllowed(int king, int kingSquare, int color, int targetSquare) {
    assert king != IntPiece.NOPIECE;
    assert (kingSquare & 0x88) == 0;

    if (color == IntColor.WHITE) {
      // Do not test g1 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.WHITE][IntCastling.KINGSIDE] != IntFile.NOFILE
        && board.board[Square.f1] == IntPiece.NOPIECE
        && board.board[Square.g1] == IntPiece.NOPIECE
        && !board.isAttacked(Square.f1, IntColor.BLACK)) {
        assert board.board[Square.e1] == IntPiece.WHITEKING;
        assert board.board[Square.h1] == IntPiece.WHITEROOK;

        if (board.canAttack(IntChessman.ROOK, color, Square.f1, targetSquare)) {
          int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.g1, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
          moveList.move[moveList.tail++] = move;
        }
      }
      // Do not test c1 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.WHITE][IntCastling.QUEENSIDE] != IntFile.NOFILE
        && board.board[Square.b1] == IntPiece.NOPIECE
        && board.board[Square.c1] == IntPiece.NOPIECE
        && board.board[Square.d1] == IntPiece.NOPIECE
        && !board.isAttacked(Square.d1, IntColor.BLACK)) {
        assert board.board[Square.e1] == IntPiece.WHITEKING;
        assert board.board[Square.a1] == IntPiece.WHITEROOK;

        if (board.canAttack(IntChessman.ROOK, color, Square.d1, targetSquare)) {
          int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.c1, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
          moveList.move[moveList.tail++] = move;
        }
      }
    } else {
      assert color == IntColor.BLACK;

      // Do not test g8 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.BLACK][IntCastling.KINGSIDE] != IntFile.NOFILE
        && board.board[Square.f8] == IntPiece.NOPIECE
        && board.board[Square.g8] == IntPiece.NOPIECE
        && !board.isAttacked(Square.f8, IntColor.WHITE)) {
        assert board.board[Square.e8] == IntPiece.BLACKKING;
        assert board.board[Square.h8] == IntPiece.BLACKROOK;

        if (board.canAttack(IntChessman.ROOK, color, Square.f8, targetSquare)) {
          int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.g8, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
          moveList.move[moveList.tail++] = move;
        }
      }
      // Do not test c8 whether it is attacked as we will test it in isLegal()
      if (board.castling[IntColor.BLACK][IntCastling.QUEENSIDE] != IntFile.NOFILE
        && board.board[Square.b8] == IntPiece.NOPIECE
        && board.board[Square.c8] == IntPiece.NOPIECE
        && board.board[Square.d8] == IntPiece.NOPIECE
        && !board.isAttacked(Square.d8, IntColor.WHITE)) {
        assert board.board[Square.e8] == IntPiece.BLACKKING;
        assert board.board[Square.a8] == IntPiece.BLACKROOK;

        if (board.canAttack(IntChessman.ROOK, color, Square.d8, targetSquare)) {
          int move = Move.valueOf(Move.Type.CASTLING, kingSquare, Square.c8, king, IntPiece.NOPIECE, IntChessman.NOCHESSMAN);
          moveList.move[moveList.tail++] = move;
        }
      }
    }
  }

}
