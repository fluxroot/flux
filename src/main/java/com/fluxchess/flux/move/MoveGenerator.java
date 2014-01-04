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
package com.fluxchess.flux.move;

import com.fluxchess.flux.Configuration;
import com.fluxchess.flux.Search;
import com.fluxchess.flux.board.*;
import com.fluxchess.flux.table.HistoryTable;
import com.fluxchess.flux.table.KillerTable;

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
  private static final int statePositionMain;
  private static final int statePositionQuiescentAll;
  private static final int statePositionQuiescentCapture;
  private static final int statePositionEvasion;

  // Generator
  private final class Generator {
    public int statePosition = -1;
    public int testState = GEN_END;
    public int transpositionMove = Move.NOMOVE;
    public int primaryKillerMove = Move.NOMOVE;
    public int secondaryKillerMove = Move.NOMOVE;
  }

  // Board
  private final Board board;

  // Tables
  private final KillerTable killerTable;

  // Sorter and Rater
  private final MoveRater moveRater;

  // Move list
  private final MoveList moveList;
  private final MoveList tempMoveList;
  private final MoveList nonCaptureMoveList;

  // Generator history
  private final Generator[] generator = new Generator[HISTORYSIZE];
  private int generatorHistory = 0;

  static {
    // Initialize state list
    int position = 0;

    statePositionMain = position;
    stateList[position++] = GEN_TRANSPOSITION;
    stateList[position++] = GEN_GOODCAPTURE;
    stateList[position++] = GEN_KILLER;
    stateList[position++] = GEN_NONCAPTURE;
    stateList[position++] = GEN_BADCAPTURE;
    stateList[position++] = GEN_END;

    statePositionQuiescentAll = position;
    stateList[position++] = GEN_GOODCAPTURE_QS;
    stateList[position++] = GEN_CHECK_QS;
    stateList[position++] = GEN_END;

    statePositionQuiescentCapture = position;
    stateList[position++] = GEN_GOODCAPTURE_QS;
    stateList[position++] = GEN_END;

    statePositionEvasion = position;
    stateList[position++] = GEN_END;
  }

  public MoveGenerator(Board board, KillerTable killerTable, HistoryTable historyTable) {
    assert board != null;
    assert killerTable != null;
    assert historyTable != null;

    this.board = board;
    this.killerTable = killerTable;
    moveRater = new MoveRater(historyTable);

    moveList = new MoveList();
    tempMoveList = new MoveList();
    nonCaptureMoveList = new MoveList();

    // Initialize generator
    for (int i = 0; i < generator.length; i++) {
      generator[i] = new Generator();
    }
    generatorHistory = 0;
  }

  public void initializeMain(Attack attack, int height, int transpositionMove) {
    moveList.newList();
    tempMoveList.newList();
    nonCaptureMoveList.newList();
    generatorHistory++;

    generator[generatorHistory].transpositionMove = transpositionMove;
    generator[generatorHistory].primaryKillerMove = killerTable.getPrimaryKiller(height);
    generator[generatorHistory].secondaryKillerMove = killerTable.getSecondaryKiller(height);

    if (attack.isCheck()) {
      generateEvasion(attack);
      moveRater.rateEvasion(moveList, generator[generatorHistory].transpositionMove, generator[generatorHistory].primaryKillerMove, generator[generatorHistory].secondaryKillerMove);
      MoveSorter.sort(moveList);
      generator[generatorHistory].statePosition = statePositionEvasion;
      generator[generatorHistory].testState = GEN_EVASION;

      // Set the move number
      attack.numberOfMoves = moveList.getLength();
    } else {
      generator[generatorHistory].statePosition = statePositionMain;
    }
  }

  public void initializeQuiescent(Attack attack, boolean generateCheckingMoves) {
    moveList.newList();
    tempMoveList.newList();
    nonCaptureMoveList.newList();
    generatorHistory++;

    generator[generatorHistory].transpositionMove = Move.NOMOVE;
    generator[generatorHistory].primaryKillerMove = Move.NOMOVE;
    generator[generatorHistory].secondaryKillerMove = Move.NOMOVE;

    if (attack.isCheck()) {
      generateEvasion(attack);
      moveRater.rateEvasion(moveList, generator[generatorHistory].transpositionMove, generator[generatorHistory].primaryKillerMove, generator[generatorHistory].secondaryKillerMove);
      MoveSorter.sort(moveList);
      generator[generatorHistory].statePosition = statePositionEvasion;
      generator[generatorHistory].testState = GEN_EVASION;

      // Set the move number
      attack.numberOfMoves = moveList.getLength();
    } else if (generateCheckingMoves) {
      generator[generatorHistory].statePosition = statePositionQuiescentAll;
    } else {
      generator[generatorHistory].statePosition = statePositionQuiescentCapture;
    }
  }

  public void destroy() {
    generatorHistory--;
    nonCaptureMoveList.deleteList();
    tempMoveList.deleteList();
    moveList.deleteList();
  }

  public int getNextMove() {
    while (true) {
      if (moveList.index < moveList.tail) {
        int move = moveList.move[moveList.index++];

        switch (generator[generatorHistory].testState) {
          case GEN_TRANSPOSITION:
            assert isLegal(move);
            assert moveList.getLength() == 1;
            break;
          case GEN_GOODCAPTURE:
            if (move == generator[generatorHistory].transpositionMove) {
              continue;
            }
            if (!isLegal(move)) {
              continue;
            }
            assert Move.getTarget(move) != IntChessman.NOPIECE;
            if (!isGoodCapture(move)) {
              tempMoveList.move[tempMoveList.tail++] = move;
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
            assert Move.getTarget(move) != IntChessman.NOPIECE : IntChessman.valueOfIntChessman(Move.getTarget(move)).toString();
            if (!isGoodCapture(move)) {
              continue;
            }
            break;
          case GEN_CHECK_QS:
            if (!isLegal(move)) {
              continue;
            }
            if (MoveSee.seeMove(move, Move.getChessmanColor(move)) < 0) {
              continue;
            }
            assert board.isCheckingMove(move) : board.getBoard().toString() + ", " + Move.toGenericMove(move).toString();
            break;
          case GEN_END:
            assert false : stateList[generator[generatorHistory].statePosition];
            break;
          default:
            assert false : stateList[generator[generatorHistory].statePosition];
            break;
        }

        return move;
      }

      // Move generation
      int state = stateList[generator[generatorHistory].statePosition++];
      moveList.resetList();

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
          tempMoveList.resetList();
          moveRater.rateFromMVVLVA(moveList);
//              moveRater.rateFromMVPD(moveList);
          MoveSorter.sort(moveList);
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
            moveRater.rateFromHistory(moveList);
            MoveSorter.sort(moveList);
          }
          generator[generatorHistory].testState = GEN_NONCAPTURE;
          break;
        case GEN_BADCAPTURE:
          System.arraycopy(tempMoveList.move, tempMoveList.head, moveList.move, moveList.tail, tempMoveList.getLength());
          moveList.tail += tempMoveList.getLength();
          generator[generatorHistory].testState = GEN_BADCAPTURE;
          break;
        case GEN_GOODCAPTURE_QS:
          generateCaptures();
          moveRater.rateFromMVVLVA(moveList);
//              moveRater.rateFromMVPD(moveList);
          MoveSorter.sort(moveList);
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
    int chessmanPosition = Move.getStart(move);
    int piece = board.board[chessmanPosition];

    // Check chessman
    if (piece == IntChessman.NOPIECE || Move.getChessman(move) != IntChessman.getChessman(piece)) {
      return false;
    }

    int color = Move.getChessmanColor(move);

    // Check color
    if (color != IntChessman.getColor(piece)) {
      return false;
    }

    assert color == board.activeColor;

    int targetPosition = Move.getEnd(move);

    // Check empty target
    if (board.board[targetPosition] != IntChessman.NOPIECE) {
      return false;
    }

    assert Move.getTarget(move) == IntChessman.NOPIECE;

    int type = Move.getType(move);

    switch (type) {
      case Move.NORMAL:
        break;
      case Move.PAWNDOUBLE:
        int delta = 0;
        if (color == IntColor.WHITE) {
          delta = 16;
        } else {
          assert color == IntColor.BLACK;

          delta = -16;
        }

        if (board.board[chessmanPosition + delta] == IntChessman.NOPIECE) {
          assert board.board[chessmanPosition + 2 * delta] == IntChessman.NOPIECE;
          return true;
        } else {
          return false;
        }
      case Move.PAWNPROMOTION:
      case Move.ENPASSANT:
      case Move.NULL:
        return false;
      case Move.CASTLING:
        switch (targetPosition) {
          case Position.g1:
            // Do not test g1 whether it is attacked as we will test it in isLegal()
            if ((board.castling & IntCastling.WHITE_KINGSIDE) != 0
              && board.board[Position.f1] == IntChessman.NOPIECE
              && board.board[Position.g1] == IntChessman.NOPIECE
              && !board.isAttacked(Position.f1, IntColor.BLACK)) {
              assert board.board[Position.e1] == IntChessman.WHITE_KING;
              assert board.board[Position.h1] == IntChessman.WHITE_ROOK;

              return true;
            }
            break;
          case Position.c1:
            // Do not test c1 whether it is attacked as we will test it in isLegal()
            if ((board.castling & IntCastling.WHITE_QUEENSIDE) != 0
              && board.board[Position.b1] == IntChessman.NOPIECE
              && board.board[Position.c1] == IntChessman.NOPIECE
              && board.board[Position.d1] == IntChessman.NOPIECE
              && !board.isAttacked(Position.d1, IntColor.BLACK)) {
              assert board.board[Position.e1] == IntChessman.WHITE_KING;
              assert board.board[Position.a1] == IntChessman.WHITE_ROOK;

              return true;
            }
            break;
          case Position.g8:
            // Do not test g8 whether it is attacked as we will test it in isLegal()
            if ((board.castling & IntCastling.BLACK_KINGSIDE) != 0
              && board.board[Position.f8] == IntChessman.NOPIECE
              && board.board[Position.g8] == IntChessman.NOPIECE
              && !board.isAttacked(Position.f8, IntColor.WHITE)) {
              assert board.board[Position.e8] == IntChessman.BLACK_KING;
              assert board.board[Position.h8] == IntChessman.BLACK_ROOK;

              return true;
            }
            break;
          case Position.c8:
            // Do not test c8 whether it is attacked as we will test it in isLegal()
            if ((board.castling & IntCastling.BLACK_QUEENSIDE) != 0
              && board.board[Position.b8] == IntChessman.NOPIECE
              && board.board[Position.c8] == IntChessman.NOPIECE
              && board.board[Position.d8] == IntChessman.NOPIECE
              && !board.isAttacked(Position.d8, IntColor.WHITE)) {
              assert board.board[Position.e8] == IntChessman.BLACK_KING;
              assert board.board[Position.a8] == IntChessman.BLACK_ROOK;

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

    int chessman = IntChessman.getChessman(piece);
    assert chessman == Move.getChessman(move);

    // Check pawn move
    if (chessman == IntChessman.PAWN) {
      int delta = 0;
      if (color == IntColor.WHITE) {
        delta = 16;
      } else {
        assert color == IntColor.BLACK;

        delta = -16;
      }

      assert board.board[chessmanPosition + delta] == IntChessman.NOPIECE;
      return true;
    }

    // Check normal move
    if (board.canAttack(chessman, color, chessmanPosition, targetPosition)) {
      return true;
    }

    return false;
  }

  private boolean isLegal(int move) {
    // Slow test for en passant
    if (Move.getType(move) == Move.ENPASSANT) {
      int activeColor = board.activeColor;
      board.makeMove(move);
      boolean isCheck = board.getAttack(activeColor).isCheck();
      board.undoMove(move);

      return !isCheck;
    }

    int chessmanColor = Move.getChessmanColor(move);

    // Special test for king
    if (Move.getChessman(move) == IntChessman.KING) {
      return !board.isAttacked(Move.getEnd(move), IntColor.switchColor(chessmanColor));
    }

    assert board.kingList[chessmanColor].size() == 1;
    if (board.isPinned(Move.getStart(move), chessmanColor)) {
      // We are pinned. Test if we move on the line.
      int kingPosition = BitPieceList.next(board.kingList[chessmanColor].list);
      int attackDeltaStart = Attack.deltas[kingPosition - Move.getStart(move) + 127];
      int attackDeltaEnd = Attack.deltas[kingPosition - Move.getEnd(move) + 127];
      return attackDeltaStart == attackDeltaEnd;
    }

    return true;
  }

  private boolean isGoodCapture(int move) {
    if (Move.getType(move) == Move.PAWNPROMOTION) {
      return Move.getPromotion(move) == IntChessman.QUEEN;
    }

    int chessman = Move.getChessman(move);
    int target = Move.getTarget(move);

    assert chessman != IntChessman.NOPIECE;
    assert target != IntChessman.NOPIECE;

    if (IntChessman.getValueFromChessman(chessman) <= IntChessman.getValueFromChessman(target)) {
      return true;
    }

    return MoveSee.seeMove(move, Move.getChessmanColor(move)) >= 0;
  }

  private void generateNonCaptures() {
    assert board != null;
    assert moveList != null;

    int activeColor = board.activeColor;

    for (long positions = board.pawnList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addPawnNonCaptureMovesTo(board.board[position], activeColor, position);
    }
    System.arraycopy(nonCaptureMoveList.move, nonCaptureMoveList.head, moveList.move, moveList.tail, nonCaptureMoveList.getLength());
    moveList.tail += nonCaptureMoveList.getLength();
    assert board.kingList[activeColor].size() == 1;
    int position = BitPieceList.next(board.kingList[activeColor].list);
    int king = board.board[position];
    addCastlingMoveIfAllowed(king, position, activeColor);
  }

  private void generateCaptures() {
    assert board != null;
    assert moveList != null;

    int activeColor = board.activeColor;
    int oppositeColor = IntColor.switchColor(activeColor);

    for (long positions = board.pawnList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addPawnCaptureMovesTo(board.board[position], activeColor, position);
    }
    for (long positions = board.knightList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addDefaultCaptureMovesTo(board.board[position], position, false, moveDeltaKnight, Position.NOPOSITION, oppositeColor);
    }
    for (long positions = board.bishopList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaBishop, Position.NOPOSITION, oppositeColor);
    }
    for (long positions = board.rookList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaRook, Position.NOPOSITION, oppositeColor);
    }
    for (long positions = board.queenList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaQueen, Position.NOPOSITION, oppositeColor);
    }
    assert board.kingList[activeColor].size() == 1;
    int position = BitPieceList.next(board.kingList[activeColor].list);
    addDefaultCaptureMovesTo(board.board[position], position, false, moveDeltaKing, Position.NOPOSITION, oppositeColor);
  }

  private void generateEvasion(Attack attack) {
    assert board != null;
    assert moveList != null;

    int activeColor = board.activeColor;
    assert board.kingList[activeColor].size() == 1;
    int kingPosition = BitPieceList.next(board.kingList[activeColor].list);
    int king = board.board[kingPosition];
    int attackerColor = IntColor.switchColor(activeColor);
    int oppositeColor = IntChessman.getColorOpposite(king);
    int moveTemplate = Move.createMove(Move.NORMAL, kingPosition, kingPosition, king, IntChessman.NOPIECE, IntChessman.NOPIECE);

    // Generate king moves
    for (int delta : moveDeltaKing) {
      assert attack.count > 0;
      boolean isOnCheckLine = false;
      for (int i = 0; i < attack.count; i++) {
        if (IntChessman.isSliding(board.board[attack.position[i]]) && delta == attack.delta[i]) {
          isOnCheckLine = true;
          break;
        }
      }
      if (!isOnCheckLine) {
        int end = kingPosition + delta;
        if ((end & 0x88) == 0 && !board.isAttacked(end, attackerColor)) {
          int target = board.board[end];
          if (target == IntChessman.NOPIECE) {
            int move = Move.setEndPosition(moveTemplate, end);
            moveList.move[moveList.tail++] = move;
          } else {
            if (IntChessman.getColor(target) == oppositeColor) {
              assert IntChessman.getChessman(target) != IntChessman.KING;
              int move = Move.setEndPositionAndTarget(moveTemplate, end, target);
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

    int attackerPosition = attack.position[0];
    int attacker = board.board[attackerPosition];

    // Capture the attacker

    addPawnCaptureMovesToTarget(activeColor, attacker, attackerPosition);
    for (long positions = board.knightList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      if (!board.isPinned(position, activeColor)) {
        addDefaultCaptureMovesTo(board.board[position], position, false, moveDeltaKnight, attackerPosition, oppositeColor);
      }
    }
    for (long positions = board.bishopList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      if (!board.isPinned(position, activeColor)) {
        addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaBishop, attackerPosition, oppositeColor);
      }
    }
    for (long positions = board.rookList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      if (!board.isPinned(position, activeColor)) {
        addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaRook, attackerPosition, oppositeColor);
      }
    }
    for (long positions = board.queenList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      if (!board.isPinned(position, activeColor)) {
        addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaQueen, attackerPosition, oppositeColor);
      }
    }

    int attackDelta = attack.delta[0];

    // Interpose a chessman
    if (IntChessman.isSliding(board.board[attackerPosition])) {
      int end = attackerPosition + attackDelta;
      while (end != kingPosition) {
        assert (end & 0x88) == 0;
        assert board.board[end] == IntChessman.NOPIECE;

        addPawnNonCaptureMovesToTarget(activeColor, end);
        for (long positions = board.knightList[activeColor].list; positions != 0; positions &= positions - 1) {
          int position = BitPieceList.next(positions);
          if (!board.isPinned(position, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[position], position, moveDeltaKnight, end);
          }
        }
        for (long positions = board.bishopList[activeColor].list; positions != 0; positions &= positions - 1) {
          int position = BitPieceList.next(positions);
          if (!board.isPinned(position, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[position], position, moveDeltaBishop, end);
          }
        }
        for (long positions = board.rookList[activeColor].list; positions != 0; positions &= positions - 1) {
          int position = BitPieceList.next(positions);
          if (!board.isPinned(position, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[position], position, moveDeltaRook, end);
          }
        }
        for (long positions = board.queenList[activeColor].list; positions != 0; positions &= positions - 1) {
          int position = BitPieceList.next(positions);
          if (!board.isPinned(position, activeColor)) {
            addDefaultNonCaptureMovesTo(board.board[position], position, moveDeltaQueen, end);
          }
        }

        end += attackDelta;
      }
    }
  }

  private void generateChecks() {
    int activeColor = board.activeColor;

    assert board.kingList[IntColor.switchColor(activeColor)].size() == 1;
    int enemyKingColor = IntColor.switchColor(activeColor);
    int enemyKingPosition = BitPieceList.next(board.kingList[enemyKingColor].list);

    for (long positions = board.pawnList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      boolean isPinned = board.isPinned(position, enemyKingColor);
      addPawnNonCaptureCheckMovesTo(board.board[position], activeColor, position, enemyKingPosition, isPinned);
    }
    for (long positions = board.knightList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      boolean isPinned = board.isPinned(position, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[position], IntChessman.KNIGHT, activeColor, position, moveDeltaKnight, enemyKingPosition, isPinned);
    }
    for (long positions = board.bishopList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      boolean isPinned = board.isPinned(position, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[position], IntChessman.BISHOP, activeColor, position, moveDeltaBishop, enemyKingPosition, isPinned);
    }
    for (long positions = board.rookList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      boolean isPinned = board.isPinned(position, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[position], IntChessman.ROOK, activeColor, position, moveDeltaRook, enemyKingPosition, isPinned);
    }
    for (long positions = board.queenList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      boolean isPinned = board.isPinned(position, enemyKingColor);
      addDefaultNonCaptureCheckMovesTo(board.board[position], IntChessman.QUEEN, activeColor, position, moveDeltaQueen, enemyKingPosition, isPinned);
    }
    assert board.kingList[activeColor].size() == 1;
    int position = BitPieceList.next(board.kingList[activeColor].list);
    int king = board.board[position];
    boolean isPinned = board.isPinned(position, enemyKingColor);
    addDefaultNonCaptureCheckMovesTo(king, IntChessman.KING, activeColor, position, moveDeltaKing, enemyKingPosition, isPinned);
    addCastlingCheckMoveIfAllowed(king, position, activeColor, enemyKingPosition);
  }

  /**
   * Add non-capturing moves from the move delta of the chessman.
   *
   * @param piece          the piece.
   * @param moveDelta      the move delta list.
   * @param targetPosition the target position.
   */
  private void addDefaultNonCaptureMovesTo(int piece, int position, int[] moveDelta, int targetPosition) {
    assert board != null;
    assert moveList != null;
    assert moveDelta != null;

    boolean sliding = IntChessman.isSliding(piece);
    int moveTemplate = Move.createMove(Move.NORMAL, position, position, piece, IntChessman.NOPIECE, IntChessman.NOPIECE);

    for (int delta : moveDelta) {
      int end = position + delta;

      // Get moves to empty squares
      while ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
        if (targetPosition == Position.NOPOSITION || end == targetPosition) {
          int move = Move.setEndPosition(moveTemplate, end);
          moveList.move[moveList.tail++] = move;
        }

        if (!sliding) {
          break;
        }

        end += delta;
      }
    }
  }

  /**
   * Add non-capturing check moves from the move delta of the chessman.
   *
   * @param piece        the piece.
   * @param moveDelta    the move delta list.
   * @param kingPosition the position of the enemy king.
   * @param isPinned     whether the chessman is pinned.
   */
  private void addDefaultNonCaptureCheckMovesTo(int piece, int chessman, int chessmanColor, int chessmanPosition, int[] moveDelta, int kingPosition, boolean isPinned) {
    assert board != null;
    assert moveList != null;
    assert moveDelta != null;

    boolean sliding = IntChessman.isSliding(piece);
    int attackDeltaStart = Attack.deltas[kingPosition - chessmanPosition + 127];
    int moveTemplate = Move.createMove(Move.NORMAL, chessmanPosition, chessmanPosition, piece, IntChessman.NOPIECE, IntChessman.NOPIECE);

    for (int delta : moveDelta) {
      int end = chessmanPosition + delta;

      // Get moves to empty squares
      while ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
        if (isPinned) {
          // We are pinned. Test if we move on the line.
          int attackDeltaEnd = Attack.deltas[kingPosition - end + 127];
          if (attackDeltaStart != attackDeltaEnd) {
            int move = Move.setEndPosition(moveTemplate, end);
            moveList.move[moveList.tail++] = move;
          }
        } else if (board.canAttack(chessman, chessmanColor, end, kingPosition)) {
          int move = Move.setEndPosition(moveTemplate, end);
          moveList.move[moveList.tail++] = move;
        }

        if (!sliding) {
          break;
        }

        end += delta;
      }
    }
  }

  /**
   * Add capturing moves from the move delta of the chessman.
   *
   * @param piece          the piece.
   * @param moveDelta      the move delta list.
   * @param targetPosition the target position.
   */
  private void addDefaultCaptureMovesTo(int piece, int position, boolean sliding, int[] moveDelta, int targetPosition, int oppositeColor) {
    assert piece != IntChessman.NOPIECE;
    assert moveDelta != null;
    assert board != null;
    assert moveList != null;

    assert IntChessman.isSliding(piece) == sliding;
    assert oppositeColor == IntChessman.getColorOpposite(piece);
    int moveTemplate = Move.createMove(Move.NORMAL, position, position, piece, IntChessman.NOPIECE, IntChessman.NOPIECE);

    for (int delta : moveDelta) {
      int end = position + delta;

      // Get moves to empty squares
      while ((end & 0x88) == 0) {
        int target = board.board[end];
        if (target == IntChessman.NOPIECE) {
          if (targetPosition == Position.NOPOSITION || end == targetPosition) {
            int move = Move.setEndPosition(moveTemplate, end);
            nonCaptureMoveList.move[nonCaptureMoveList.tail++] = move;
          }

          if (!sliding) {
            break;
          }

          end += delta;
        } else {
          if (targetPosition == Position.NOPOSITION || end == targetPosition) {
            // Get the move to the square the next chessman is standing on
            if (IntChessman.getColor(target) == oppositeColor
              && IntChessman.getChessman(target) != IntChessman.KING) {
              int move = Move.setEndPositionAndTarget(moveTemplate, end, target);
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
   * @param pawnPosition the pawn position.
   */
  private void addPawnNonCaptureMovesTo(int pawn, int pawnColor, int pawnPosition) {
    assert pawn != IntChessman.NOPIECE;
    assert IntChessman.getChessman(pawn) == IntChessman.PAWN;
    assert IntChessman.getColor(pawn) == pawnColor;
    assert (pawnPosition & 0x88) == 0;
    assert board != null;
    assert board.board[pawnPosition] == pawn;
    assert moveList != null;

    int delta = moveDeltaPawn[0];
    if (pawnColor == IntColor.BLACK) {
      delta *= -1;
    }

    // Move one square forward
    int end = pawnPosition + delta;
    if ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
      // GenericRank.R8 = position > 111
      // GenericRank.R1 = position < 8
      if ((end > 111 && pawnColor == IntColor.WHITE)
        || (end < 8 && pawnColor == IntColor.BLACK)) {
        int moveTemplate = Move.createMove(Move.PAWNPROMOTION, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
        int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
        moveList.move[moveList.tail++] = move;
        move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
        moveList.move[moveList.tail++] = move;
        move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
        moveList.move[moveList.tail++] = move;
        move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
        moveList.move[moveList.tail++] = move;
      } else {
        int move = Move.createMove(Move.NORMAL, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;

        // Move two squares forward
        end += delta;
        if ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
          // GenericRank.R4 = end >>> 4 == 3
          // GenericRank.R5 = end >>> 4 == 4
          if (((end >>> 4) == 3 && pawnColor == IntColor.WHITE)
            || ((end >>> 4) == 4 && pawnColor == IntColor.BLACK)) {
            assert ((pawnPosition >>> 4) == 1 && (end >>> 4) == 3 && pawnColor == IntColor.WHITE) || ((pawnPosition >>> 4) == 6 && (end >>> 4) == 4 && pawnColor == IntColor.BLACK);

            move = Move.createMove(Move.PAWNDOUBLE, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
            moveList.move[moveList.tail++] = move;
          }
        }
      }
    }
  }

  /**
   * Add non-capturing moves of pawns to the target position.
   *
   * @param pawnColor      the pawn color.
   * @param targetPosition the target position.
   */
  private void addPawnNonCaptureMovesToTarget(int pawnColor, int targetPosition) {
    assert pawnColor == IntColor.WHITE || pawnColor == IntColor.BLACK;
    assert (targetPosition & 0x88) == 0;
    assert board != null;
    assert moveList != null;
    assert board.board[targetPosition] == IntChessman.NOPIECE;

    int delta = moveDeltaPawn[0];
    int pawnPiece = IntChessman.BLACK_PAWN;
    if (pawnColor == IntColor.WHITE) {
      delta *= -1;
      pawnPiece = IntChessman.WHITE_PAWN;
    }

    // Move one square backward
    int pawnPosition = targetPosition + delta;
    if ((pawnPosition & 0x88) == 0) {
      int pawn = board.board[pawnPosition];
      if (pawn != IntChessman.NOPIECE) {
        if (pawn == pawnPiece) {
          // We found a valid pawn

          if (!board.isPinned(pawnPosition, pawnColor)) {
            // GenericRank.R8 = position > 111
            // GenericRank.R1 = position < 8
            if ((targetPosition > 111 && pawnColor == IntColor.WHITE)
              || (targetPosition < 8 && pawnColor == IntColor.BLACK)) {
              int moveTemplate = Move.createMove(Move.PAWNPROMOTION, pawnPosition, targetPosition, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
              int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = Move.createMove(Move.NORMAL, pawnPosition, targetPosition, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
              moveList.move[moveList.tail++] = move;
            }
          }
        }
      } else {
        // Move two squares backward
        pawnPosition += delta;
        if ((pawnPosition & 0x88) == 0) {
          // GenericRank.R4 = end >>> 4 == 3
          // GenericRank.R5 = end >>> 4 == 4
          if (((pawnPosition >>> 4) == 1 && pawnColor == IntColor.WHITE)
            || ((pawnPosition >>> 4) == 6 && pawnColor == IntColor.BLACK)) {
            assert ((pawnPosition >>> 4) == 1 && (targetPosition >>> 4) == 3 && pawnColor == IntColor.WHITE) || ((pawnPosition >>> 4) == 6 && (targetPosition >>> 4) == 4 && pawnColor == IntColor.BLACK);

            pawn = board.board[pawnPosition];
            if (pawn != IntChessman.NOPIECE && pawn == pawnPiece) {
              if (!board.isPinned(pawnPosition, pawnColor)) {
                int move = Move.createMove(Move.PAWNDOUBLE, pawnPosition, targetPosition, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
                moveList.move[moveList.tail++] = move;
              }
            }
          }
        }
      }
    }
  }

  /**
   * Add non-capturing check moves of the pawn to the target position.
   *
   * @param pawn         the IntChessman.
   * @param kingPosition the enemy king position.
   * @param isPinned     whether the pawn is pinned.
   */
  private void addPawnNonCaptureCheckMovesTo(int pawn, int color, int pawnPosition, int kingPosition, boolean isPinned) {
    assert pawn != IntChessman.NOPIECE;
    assert (kingPosition & 0x88) == 0;
    assert board != null;
    assert moveList != null;

    int delta = moveDeltaPawn[0];
    if (color == IntColor.BLACK) {
      delta *= -1;
    }

    // Move one square forward
    int end = pawnPosition + delta;
    if ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
      // GenericRank.R8 = position > 111
      // GenericRank.R1 = position < 8
      if ((end > 111 && color == IntColor.WHITE)
        || (end < 8 && color == IntColor.BLACK)) {
        int moveTemplate = Move.createMove(Move.PAWNPROMOTION, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
        int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
        board.makeMove(move);
        boolean isCheck = board.isAttacked(kingPosition, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
        move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
        board.makeMove(move);
        isCheck = board.isAttacked(kingPosition, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
        move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
        board.makeMove(move);
        isCheck = board.isAttacked(kingPosition, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
        move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
        board.makeMove(move);
        isCheck = board.isAttacked(kingPosition, color);
        board.undoMove(move);
        if (isCheck) {
          moveList.move[moveList.tail++] = move;
        }
      } else {
        if (isPinned) {
          // We are pinned. Test if we move on the line.
          int attackDeltaStart = Attack.deltas[kingPosition - pawnPosition + 127];
          int attackDeltaEnd = Attack.deltas[kingPosition - end + 127];
          if (attackDeltaStart != attackDeltaEnd) {
            int move = Move.createMove(Move.NORMAL, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
            moveList.move[moveList.tail++] = move;
          }
        } else if (board.canAttack(IntChessman.PAWN, color, end, kingPosition)) {
          int move = Move.createMove(Move.NORMAL, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
          moveList.move[moveList.tail++] = move;
        }

        // Move two squares forward
        end += delta;
        if ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
          // GenericRank.R4 = end >>> 4 == 3
          // GenericRank.R5 = end >>> 4 == 4
          if (((end >>> 4) == 3 && color == IntColor.WHITE)
            || ((end >>> 4) == 4 && color == IntColor.BLACK)) {
            assert ((pawnPosition >>> 4) == 1 && (end >>> 4) == 3 && color == IntColor.WHITE) || ((pawnPosition >>> 4) == 6 && (end >>> 4) == 4 && color == IntColor.BLACK);

            if (isPinned) {
              // We are pinned. Test if we move on the line.
              int attackDeltaStart = Attack.deltas[kingPosition - pawnPosition + 127];
              int attackDeltaEnd = Attack.deltas[kingPosition - end + 127];
              if (attackDeltaStart != attackDeltaEnd) {
                int move = Move.createMove(Move.PAWNDOUBLE, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
                moveList.move[moveList.tail++] = move;
              }
            } else if (board.canAttack(IntChessman.PAWN, color, end, kingPosition)) {
              int move = Move.createMove(Move.PAWNDOUBLE, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
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
   * @param pawnPosition the pawn position.
   */
  private void addPawnCaptureMovesTo(int pawn, int pawnColor, int pawnPosition) {
    assert pawn != IntChessman.NOPIECE;
    assert IntChessman.getChessman(pawn) == IntChessman.PAWN;
    assert IntChessman.getColor(pawn) == pawnColor;
    assert (pawnPosition & 0x88) == 0;
    assert board != null;
    assert board.board[pawnPosition] == pawn;
    assert moveList != null;

    for (int i = 1; i < moveDeltaPawn.length; i++) {
      int delta = moveDeltaPawn[i];
      if (pawnColor == IntColor.BLACK) {
        delta *= -1;
      }

      int end = pawnPosition + delta;
      if ((end & 0x88) == 0) {
        int target = board.board[end];
        if (target != IntChessman.NOPIECE) {
          if (IntChessman.getColorOpposite(target) == pawnColor
            && IntChessman.getChessman(target) != IntChessman.KING) {
            // Capturing move

            // GenericRank.R8 = position > 111
            // GenericRank.R1 = position < 8
            if ((end > 111 && pawnColor == IntColor.WHITE)
              || (end < 8 && pawnColor == IntColor.BLACK)) {
              int moveTemplate = Move.createMove(Move.PAWNPROMOTION, pawnPosition, end, pawn, target, IntChessman.NOPIECE);
              int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = Move.createMove(Move.NORMAL, pawnPosition, end, pawn, target, IntChessman.NOPIECE);
              moveList.move[moveList.tail++] = move;
            }
          }
        } else if (end == board.enPassantSquare) {
          // En passant move
          assert board.enPassantSquare != Position.NOPOSITION;
          assert ((end >>> 4) == 2 && pawnColor == IntColor.BLACK) || ((end >>> 4) == 5 && pawnColor == IntColor.WHITE);

          // Calculate the en passant position
          int enPassantTargetPosition;
          if (pawnColor == IntColor.WHITE) {
            enPassantTargetPosition = end - 16;
          } else {
            assert pawnColor == IntColor.BLACK;

            enPassantTargetPosition = end + 16;
          }
          target = board.board[enPassantTargetPosition];
          assert IntChessman.getChessman(target) == IntChessman.PAWN;
          assert IntChessman.getColor(target) == IntColor.switchColor(pawnColor);

          int move = Move.createMove(Move.ENPASSANT, pawnPosition, end, pawn, target, IntChessman.NOPIECE);
          moveList.move[moveList.tail++] = move;
        }
      }
    }
  }

  /**
   * Add capturing moves of pawns to the target position.
   *
   * @param pawnColor      the color of the attacking pawn.
   * @param target         the target chessman.
   * @param targetPosition the target position.
   */
  private void addPawnCaptureMovesToTarget(int pawnColor, int target, int targetPosition) {
    assert pawnColor == IntColor.WHITE || pawnColor == IntColor.BLACK;
    assert target != IntChessman.NOPIECE;
    assert IntChessman.getColor(target) == IntColor.switchColor(pawnColor);
    assert (targetPosition & 0x88) == 0;
    assert board != null;
    assert moveList != null;
    assert board.board[targetPosition] != IntChessman.NOPIECE;
    assert board.board[targetPosition] == target;
    assert IntChessman.getChessman(board.board[targetPosition]) != IntChessman.KING;
    assert IntChessman.getColorOpposite(board.board[targetPosition]) == pawnColor;

    int pawnPiece = IntChessman.BLACK_PAWN;
    int enPassantDelta = -16;
    if (pawnColor == IntColor.WHITE) {
      pawnPiece = IntChessman.WHITE_PAWN;
      enPassantDelta = 16;
    }
    int enPassantPosition = targetPosition + enPassantDelta;

    for (int i = 1; i < moveDeltaPawn.length; i++) {
      int delta = moveDeltaPawn[i];
      if (pawnColor == IntColor.WHITE) {
        delta *= -1;
      }

      int pawnPosition = targetPosition + delta;
      if ((pawnPosition & 0x88) == 0) {
        int pawn = board.board[pawnPosition];
        if (pawn != IntChessman.NOPIECE && pawn == pawnPiece) {
          // We found a valid pawn

          if (!board.isPinned(pawnPosition, pawnColor)) {
            // GenericRank.R8 = position > 111
            // GenericRank.R1 = position < 8
            if ((targetPosition > 111 && pawnColor == IntColor.WHITE)
              || (targetPosition < 8 && pawnColor == IntColor.BLACK)) {
              int moveTemplate = Move.createMove(Move.PAWNPROMOTION, pawnPosition, targetPosition, pawn, target, IntChessman.NOPIECE);
              int move = Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = Move.createMove(Move.NORMAL, pawnPosition, targetPosition, pawn, target, IntChessman.NOPIECE);
              moveList.move[moveList.tail++] = move;
            }
          }
        }
        if (enPassantPosition == board.enPassantSquare) {
          // En passant move
          pawnPosition = pawnPosition + enPassantDelta;
          assert (enPassantPosition & 0x88) == 0;
          assert (pawnPosition & 0x88) == 0;

          pawn = board.board[pawnPosition];
          if (pawn != IntChessman.NOPIECE && pawn == pawnPiece) {
            // We found a valid pawn which can do a en passant move

            if (!board.isPinned(pawnPosition, pawnColor)) {
              assert ((enPassantPosition >>> 4) == 2 && pawnColor == IntColor.BLACK) || ((enPassantPosition >>> 4) == 5 && pawnColor == IntColor.WHITE);
              assert IntChessman.getChessman(target) == IntChessman.PAWN;

              int move = Move.createMove(Move.ENPASSANT, pawnPosition, enPassantPosition, pawn, target, IntChessman.NOPIECE);
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
  private void addCastlingMoveIfAllowed(int king, int kingPosition, int color) {
    assert king != IntChessman.NOPIECE;
    assert (kingPosition & 0x88) == 0;
    assert board != null;
    assert moveList != null;

    if (color == IntColor.WHITE) {
      // Do not test g1 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.WHITE_KINGSIDE) != 0
        && board.board[Position.f1] == IntChessman.NOPIECE
        && board.board[Position.g1] == IntChessman.NOPIECE
        && !board.isAttacked(Position.f1, IntColor.BLACK)) {
        assert board.board[Position.e1] == IntChessman.WHITE_KING;
        assert board.board[Position.h1] == IntChessman.WHITE_ROOK;

        int move = Move.createMove(Move.CASTLING, kingPosition, Position.g1, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
      // Do not test c1 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.WHITE_QUEENSIDE) != 0
        && board.board[Position.b1] == IntChessman.NOPIECE
        && board.board[Position.c1] == IntChessman.NOPIECE
        && board.board[Position.d1] == IntChessman.NOPIECE
        && !board.isAttacked(Position.d1, IntColor.BLACK)) {
        assert board.board[Position.e1] == IntChessman.WHITE_KING;
        assert board.board[Position.a1] == IntChessman.WHITE_ROOK;

        int move = Move.createMove(Move.CASTLING, kingPosition, Position.c1, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
    } else {
      assert color == IntColor.BLACK;

      // Do not test g8 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.BLACK_KINGSIDE) != 0
        && board.board[Position.f8] == IntChessman.NOPIECE
        && board.board[Position.g8] == IntChessman.NOPIECE
        && !board.isAttacked(Position.f8, IntColor.WHITE)) {
        assert board.board[Position.e8] == IntChessman.BLACK_KING;
        assert board.board[Position.h8] == IntChessman.BLACK_ROOK;

        int move = Move.createMove(Move.CASTLING, kingPosition, Position.g8, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
      // Do not test c8 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.BLACK_QUEENSIDE) != 0
        && board.board[Position.b8] == IntChessman.NOPIECE
        && board.board[Position.c8] == IntChessman.NOPIECE
        && board.board[Position.d8] == IntChessman.NOPIECE
        && !board.isAttacked(Position.d8, IntColor.WHITE)) {
        assert board.board[Position.e8] == IntChessman.BLACK_KING;
        assert board.board[Position.a8] == IntChessman.BLACK_ROOK;

        int move = Move.createMove(Move.CASTLING, kingPosition, Position.c8, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
    }
  }

  /**
   * Add the castling check moves to the move list.
   *
   * @param king           the king.
   * @param targetPosition the position of the enemy king.
   */
  private void addCastlingCheckMoveIfAllowed(int king, int kingPosition, int color, int targetPosition) {
    assert king != IntChessman.NOPIECE;
    assert (kingPosition & 0x88) == 0;
    assert board != null;
    assert moveList != null;

    if (color == IntColor.WHITE) {
      // Do not test g1 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.WHITE_KINGSIDE) != 0
        && board.board[Position.f1] == IntChessman.NOPIECE
        && board.board[Position.g1] == IntChessman.NOPIECE
        && !board.isAttacked(Position.f1, IntColor.BLACK)) {
        assert board.board[Position.e1] == IntChessman.WHITE_KING;
        assert board.board[Position.h1] == IntChessman.WHITE_ROOK;

        if (board.canAttack(IntChessman.ROOK, color, Position.f1, targetPosition)) {
          int move = Move.createMove(Move.CASTLING, kingPosition, Position.g1, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
          moveList.move[moveList.tail++] = move;
        }
      }
      // Do not test c1 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.WHITE_QUEENSIDE) != 0
        && board.board[Position.b1] == IntChessman.NOPIECE
        && board.board[Position.c1] == IntChessman.NOPIECE
        && board.board[Position.d1] == IntChessman.NOPIECE
        && !board.isAttacked(Position.d1, IntColor.BLACK)) {
        assert board.board[Position.e1] == IntChessman.WHITE_KING;
        assert board.board[Position.a1] == IntChessman.WHITE_ROOK;

        if (board.canAttack(IntChessman.ROOK, color, Position.d1, targetPosition)) {
          int move = Move.createMove(Move.CASTLING, kingPosition, Position.c1, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
          moveList.move[moveList.tail++] = move;
        }
      }
    } else {
      assert color == IntColor.BLACK;

      // Do not test g8 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.BLACK_KINGSIDE) != 0
        && board.board[Position.f8] == IntChessman.NOPIECE
        && board.board[Position.g8] == IntChessman.NOPIECE
        && !board.isAttacked(Position.f8, IntColor.WHITE)) {
        assert board.board[Position.e8] == IntChessman.BLACK_KING;
        assert board.board[Position.h8] == IntChessman.BLACK_ROOK;

        if (board.canAttack(IntChessman.ROOK, color, Position.f8, targetPosition)) {
          int move = Move.createMove(Move.CASTLING, kingPosition, Position.g8, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
          moveList.move[moveList.tail++] = move;
        }
      }
      // Do not test c8 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.BLACK_QUEENSIDE) != 0
        && board.board[Position.b8] == IntChessman.NOPIECE
        && board.board[Position.c8] == IntChessman.NOPIECE
        && board.board[Position.d8] == IntChessman.NOPIECE
        && !board.isAttacked(Position.d8, IntColor.WHITE)) {
        assert board.board[Position.e8] == IntChessman.BLACK_KING;
        assert board.board[Position.a8] == IntChessman.BLACK_ROOK;

        if (board.canAttack(IntChessman.ROOK, color, Position.d8, targetPosition)) {
          int move = Move.createMove(Move.CASTLING, kingPosition, Position.c8, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
          moveList.move[moveList.tail++] = move;
        }
      }
    }
  }

}
