/*
 * Copyright 2007-2013 the original author or authors.
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
package com.fluxchess.x88.board;

import com.fluxchess.x88.Configuration;

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

  private static final int HISTORYSIZE = Configuration.MAX_SEARCH_HEIGHT + 1;
  private static final int STATELISTSIZE = 256;

  // States
  private static final int GEN_GOODCAPTURE = 1;
  private static final int GEN_NONCAPTURE = 3;
  private static final int GEN_BADCAPTURE = 4;
  private static final int GEN_EVASION = 5;
  private static final int GEN_END = 8;

  // State list
  private static final int[] stateList = new int[STATELISTSIZE];
  private static final int statePositionMain;
  private static final int statePositionEvasion;

  // Generator
  private final class Generator {
    public int statePosition = -1;
    public int testState = GEN_END;
  }

  // Board
  private final X88Board board;

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
    stateList[position++] = GEN_GOODCAPTURE;
    stateList[position++] = GEN_NONCAPTURE;
    stateList[position++] = GEN_BADCAPTURE;
    stateList[position++] = GEN_END;

    statePositionEvasion = position;
    stateList[position++] = GEN_END;
  }

  public MoveGenerator(X88Board board) {
    assert board != null;

    this.board = board;

    moveList = new MoveList();
    tempMoveList = new MoveList();
    nonCaptureMoveList = new MoveList();

    // Initialize generator
    for (int i = 0; i < generator.length; i++) {
      generator[i] = new Generator();
    }
    generatorHistory = 0;
  }

  public void initializeMain(Attack attack) {
    moveList.newList();
    tempMoveList.newList();
    nonCaptureMoveList.newList();
    generatorHistory++;

    if (attack.isCheck()) {
      generateEvasion(attack);
      generator[generatorHistory].statePosition = statePositionEvasion;
      generator[generatorHistory].testState = GEN_EVASION;

      // Set the move number
      attack.numberOfMoves = moveList.getLength();
    } else {
      generator[generatorHistory].statePosition = statePositionMain;
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
          case GEN_GOODCAPTURE:
            if (!isLegal(move)) {
              continue;
            }
            assert X88Move.getTarget(move) != IntChessman.NOPIECE;
            if (!isGoodCapture(move)) {
              tempMoveList.move[tempMoveList.tail++] = move;
              continue;
            }
            break;
          case GEN_NONCAPTURE:
            if (!isLegal(move)) {
              continue;
            }
            break;
          case GEN_BADCAPTURE:
            assert isLegal(move);
            assert !isGoodCapture(move) : board.getBoard().toString() + ", " + X88Move.toGenericMove(move).toString();
            break;
          case GEN_EVASION:
            assert isLegal(move);
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
        case GEN_GOODCAPTURE:
          generateCaptures();
          tempMoveList.resetList();
          generator[generatorHistory].testState = GEN_GOODCAPTURE;
          break;
        case GEN_NONCAPTURE:
          generateNonCaptures();
          generator[generatorHistory].testState = GEN_NONCAPTURE;
          break;
        case GEN_BADCAPTURE:
          System.arraycopy(tempMoveList.move, tempMoveList.head, moveList.move, moveList.tail, tempMoveList.getLength());
          moveList.tail += tempMoveList.getLength();
          generator[generatorHistory].testState = GEN_BADCAPTURE;
          break;
        case GEN_END:
          return X88Move.NOMOVE;
        default:
          assert false : state;
          break;
      }
    }
  }

  private boolean isLegal(int move) {
    // Slow test for en passant
    if (X88Move.getType(move) == X88Move.ENPASSANT) {
      int activeColor = board.activeColor;
      board.makeMove(move);
      boolean isCheck = board.getAttack(activeColor).isCheck();
      board.undoMove(move);

      return !isCheck;
    }

    int chessmanColor = X88Move.getChessmanColor(move);

    // Special test for king
    if (X88Move.getChessman(move) == IntChessman.KING) {
      return !board.isAttacked(X88Move.getEnd(move), IntColor.switchColor(chessmanColor));
    }

    assert board.kingList[chessmanColor].size() == 1;
    if (board.isPinned(X88Move.getStart(move), chessmanColor)) {
      // We are pinned. Test if we move on the line.
      int kingPosition = BitPieceList.next(board.kingList[chessmanColor].list);
      int attackDeltaStart = AttackVector.delta[kingPosition - X88Move.getStart(move) + 127];
      int attackDeltaEnd = AttackVector.delta[kingPosition - X88Move.getEnd(move) + 127];
      return attackDeltaStart == attackDeltaEnd;
    }

    return true;
  }

  private boolean isGoodCapture(int move) {
    if (X88Move.getType(move) == X88Move.PAWNPROMOTION) {
      return X88Move.getPromotion(move) == IntChessman.QUEEN;
    }

    int chessman = X88Move.getChessman(move);
    int target = X88Move.getTarget(move);

    assert chessman != IntChessman.NOPIECE;
    assert target != IntChessman.NOPIECE;

    if (IntChessman.getValueFromChessman(chessman) <= IntChessman.getValueFromChessman(target)) {
      return true;
    }

    return false;
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
      addDefaultCaptureMovesTo(board.board[position], position, false, moveDeltaKnight, X88Position.NOPOSITION, oppositeColor);
    }
    for (long positions = board.bishopList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaBishop, X88Position.NOPOSITION, oppositeColor);
    }
    for (long positions = board.rookList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaRook, X88Position.NOPOSITION, oppositeColor);
    }
    for (long positions = board.queenList[activeColor].list; positions != 0; positions &= positions - 1) {
      int position = BitPieceList.next(positions);
      addDefaultCaptureMovesTo(board.board[position], position, true, moveDeltaQueen, X88Position.NOPOSITION, oppositeColor);
    }
    assert board.kingList[activeColor].size() == 1;
    int position = BitPieceList.next(board.kingList[activeColor].list);
    addDefaultCaptureMovesTo(board.board[position], position, false, moveDeltaKing, X88Position.NOPOSITION, oppositeColor);
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
    int moveTemplate = X88Move.createMove(X88Move.NORMAL, kingPosition, kingPosition, king, IntChessman.NOPIECE, IntChessman.NOPIECE);

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
            int move = X88Move.setEndPosition(moveTemplate, end);
            moveList.move[moveList.tail++] = move;
          } else {
            if (IntChessman.getColor(target) == oppositeColor) {
              assert IntChessman.getChessman(target) != IntChessman.KING;
              int move = X88Move.setEndPositionAndTarget(moveTemplate, end, target);
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
    int moveTemplate = X88Move.createMove(X88Move.NORMAL, position, position, piece, IntChessman.NOPIECE, IntChessman.NOPIECE);

    for (int delta : moveDelta) {
      int end = position + delta;

      // Get moves to empty squares
      while ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
        if (targetPosition == X88Position.NOPOSITION || end == targetPosition) {
          int move = X88Move.setEndPosition(moveTemplate, end);
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
    int moveTemplate = X88Move.createMove(X88Move.NORMAL, position, position, piece, IntChessman.NOPIECE, IntChessman.NOPIECE);

    for (int delta : moveDelta) {
      int end = position + delta;

      // Get moves to empty squares
      while ((end & 0x88) == 0) {
        int target = board.board[end];
        if (target == IntChessman.NOPIECE) {
          if (targetPosition == X88Position.NOPOSITION || end == targetPosition) {
            int move = X88Move.setEndPosition(moveTemplate, end);
            nonCaptureMoveList.move[nonCaptureMoveList.tail++] = move;
          }

          if (!sliding) {
            break;
          }

          end += delta;
        } else {
          if (targetPosition == X88Position.NOPOSITION || end == targetPosition) {
            // Get the move to the square the next chessman is standing on
            if (IntChessman.getColor(target) == oppositeColor
              && IntChessman.getChessman(target) != IntChessman.KING) {
              int move = X88Move.setEndPositionAndTarget(moveTemplate, end, target);
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
        int moveTemplate = X88Move.createMove(X88Move.PAWNPROMOTION, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
        int move = X88Move.setPromotion(moveTemplate, IntChessman.QUEEN);
        moveList.move[moveList.tail++] = move;
        move = X88Move.setPromotion(moveTemplate, IntChessman.ROOK);
        moveList.move[moveList.tail++] = move;
        move = X88Move.setPromotion(moveTemplate, IntChessman.BISHOP);
        moveList.move[moveList.tail++] = move;
        move = X88Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
        moveList.move[moveList.tail++] = move;
      } else {
        int move = X88Move.createMove(X88Move.NORMAL, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;

        // Move two squares forward
        end += delta;
        if ((end & 0x88) == 0 && board.board[end] == IntChessman.NOPIECE) {
          // GenericRank.R4 = end >>> 4 == 3
          // GenericRank.R5 = end >>> 4 == 4
          if (((end >>> 4) == 3 && pawnColor == IntColor.WHITE)
            || ((end >>> 4) == 4 && pawnColor == IntColor.BLACK)) {
            assert ((pawnPosition >>> 4) == 1 && (end >>> 4) == 3 && pawnColor == IntColor.WHITE) || ((pawnPosition >>> 4) == 6 && (end >>> 4) == 4 && pawnColor == IntColor.BLACK);

            move = X88Move.createMove(X88Move.PAWNDOUBLE, pawnPosition, end, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
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
              int moveTemplate = X88Move.createMove(X88Move.PAWNPROMOTION, pawnPosition, targetPosition, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
              int move = X88Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = X88Move.createMove(X88Move.NORMAL, pawnPosition, targetPosition, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
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
                int move = X88Move.createMove(X88Move.PAWNDOUBLE, pawnPosition, targetPosition, pawn, IntChessman.NOPIECE, IntChessman.NOPIECE);
                moveList.move[moveList.tail++] = move;
              }
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
              int moveTemplate = X88Move.createMove(X88Move.PAWNPROMOTION, pawnPosition, end, pawn, target, IntChessman.NOPIECE);
              int move = X88Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = X88Move.createMove(X88Move.NORMAL, pawnPosition, end, pawn, target, IntChessman.NOPIECE);
              moveList.move[moveList.tail++] = move;
            }
          }
        } else if (end == board.enPassantSquare) {
          // En passant move
          assert board.enPassantSquare != X88Position.NOPOSITION;
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

          int move = X88Move.createMove(X88Move.ENPASSANT, pawnPosition, end, pawn, target, IntChessman.NOPIECE);
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
              int moveTemplate = X88Move.createMove(X88Move.PAWNPROMOTION, pawnPosition, targetPosition, pawn, target, IntChessman.NOPIECE);
              int move = X88Move.setPromotion(moveTemplate, IntChessman.QUEEN);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.ROOK);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.BISHOP);
              moveList.move[moveList.tail++] = move;
              move = X88Move.setPromotion(moveTemplate, IntChessman.KNIGHT);
              moveList.move[moveList.tail++] = move;
            } else {
              int move = X88Move.createMove(X88Move.NORMAL, pawnPosition, targetPosition, pawn, target, IntChessman.NOPIECE);
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

              int move = X88Move.createMove(X88Move.ENPASSANT, pawnPosition, enPassantPosition, pawn, target, IntChessman.NOPIECE);
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
        && board.board[X88Position.f1] == IntChessman.NOPIECE
        && board.board[X88Position.g1] == IntChessman.NOPIECE
        && !board.isAttacked(X88Position.f1, IntColor.BLACK)) {
        assert board.board[X88Position.e1] == IntChessman.WHITE_KING;
        assert board.board[X88Position.h1] == IntChessman.WHITE_ROOK;

        int move = X88Move.createMove(X88Move.CASTLING, kingPosition, X88Position.g1, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
      // Do not test c1 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.WHITE_QUEENSIDE) != 0
        && board.board[X88Position.b1] == IntChessman.NOPIECE
        && board.board[X88Position.c1] == IntChessman.NOPIECE
        && board.board[X88Position.d1] == IntChessman.NOPIECE
        && !board.isAttacked(X88Position.d1, IntColor.BLACK)) {
        assert board.board[X88Position.e1] == IntChessman.WHITE_KING;
        assert board.board[X88Position.a1] == IntChessman.WHITE_ROOK;

        int move = X88Move.createMove(X88Move.CASTLING, kingPosition, X88Position.c1, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
    } else {
      assert color == IntColor.BLACK;

      // Do not test g8 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.BLACK_KINGSIDE) != 0
        && board.board[X88Position.f8] == IntChessman.NOPIECE
        && board.board[X88Position.g8] == IntChessman.NOPIECE
        && !board.isAttacked(X88Position.f8, IntColor.WHITE)) {
        assert board.board[X88Position.e8] == IntChessman.BLACK_KING;
        assert board.board[X88Position.h8] == IntChessman.BLACK_ROOK;

        int move = X88Move.createMove(X88Move.CASTLING, kingPosition, X88Position.g8, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
      // Do not test c8 whether it is attacked as we will test it in isLegal()
      if ((board.castling & IntCastling.BLACK_QUEENSIDE) != 0
        && board.board[X88Position.b8] == IntChessman.NOPIECE
        && board.board[X88Position.c8] == IntChessman.NOPIECE
        && board.board[X88Position.d8] == IntChessman.NOPIECE
        && !board.isAttacked(X88Position.d8, IntColor.WHITE)) {
        assert board.board[X88Position.e8] == IntChessman.BLACK_KING;
        assert board.board[X88Position.a8] == IntChessman.BLACK_ROOK;

        int move = X88Move.createMove(X88Move.CASTLING, kingPosition, X88Position.c8, king, IntChessman.NOPIECE, IntChessman.NOPIECE);
        moveList.move[moveList.tail++] = move;
      }
    }
  }

}
