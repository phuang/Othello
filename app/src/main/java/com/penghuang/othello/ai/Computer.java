package com.penghuang.othello.ai;

/**
 * Created by penghuang on 2/24/15.
 */
import java.util.Random;

class Computer {

    private static final int[] SCORE_MAP = new int[] {
            60,  -6,  0,  0,  0,  0,  -6, 60,
            -6, -12, -6, -6, -6, -6, -12, -6,
             0,  -6,  0,  0,  0,  0,  -6,  0,
             0,  -6,  0,  0,  0,  0,  -6,  0,
             0,  -6,  0,  0,  0,  0,  -6,  0,
             0,  -6,  0,  0,  0,  0,  -6,  0,
            -6, -12, -6, -6, -6, -6, -12, -6,
            60,  -6,  0,  0,  0,  0,  -6, 60
    };

    private static final int[][] DIR_MAP = new int[][] {
            { -1, -1 }, { -1, 0 }, { -1, 1 },
            { 0, -1 }, /* {0, 0}, */ { 0, 1 },
            { 1, -1 }, {1, 0}, { 1, 1 },
    };

    private Random mRandom = new Random();
    private Square[][] mAxis = new Square[8][8];
    private Square[][] mNeighbor = new Square[8][8];
    private int[] mMinMax = new int[20];
    private int mDepth = 4;
    private int mEffect;
    private boolean mExhaustive;
    int mSX;
    int mSY;

    class Status {
        Square square = new Square();
        int number = 0;
        int score = 0;

        Status () {
        }

        Status(final Status s) {
            setStatus(s);
        }

        void setStatus(final Status s) {
            square.setSquare(s.square);
            number = s.number;
            score = s.score;
        }

        void setStatus(final Square square, int number, int score) {
            this.square.setSquare(square);
            this.number = number;
            this.score = score;
        }
    }

    Computer() {
        initNeighbor();
        initAxis();
    }

    private boolean computeStep(Status p1, Status p2, int x, int y) {
        if (p1.square.getChess(x, y) || p2.square.getChess(x, y))
            return false;

        // Check if mNeighbor spots are occupied by player.
        Square ret = new Square();
        ret.setSquare(mNeighbor[x][y]);
        if (ret.bitAnd(p2.square).isEmpty())
            return false;

        int changeNum = 0;
        for (int i = 0; i < DIR_MAP.length; ++i) {
            final int inx = DIR_MAP[i][0];
            final int iny = DIR_MAP[i][1];
            int xx = x + inx;
            int yy = y + iny;
            int step = 0;
            while (xx >= 0 && xx < 8 && yy >= 0 && yy <= 8) {
                if (!p2.square.getChess(xx, yy))
                    break;
                ++step;
                xx += inx;
                yy += iny;
            }

            if (step == 0)
                continue;
            if (xx < 0 || xx >= 8)
                continue;
            if (yy < 0 || yy >= 8)
                continue;
            if (!p1.square.getChess(xx, yy))
                continue;

            changeNum += step;
            for (; step > 0; --step) {
                xx -= inx;
                yy -= iny;
                p1.score += getScore(xx, yy);
                p2.score -= getScore(xx, yy);
                p1.square.setChess(xx, yy);
                p2.square.clearChess(xx, yy);
            }
        }
        if (changeNum == 0)
            return false;
        p1.square.setChess(x, y);
        p1.score += getScore(x, y);
        p1.number += changeNum + 1;
        p2.number -= changeNum;
        return true;
    }

    boolean begin(final Square computer, final Square player, int comNum,
                  int plaNum) {
        boolean findValidatedStep = false;
        boolean b;

        final int chessNum = comNum + plaNum;
        if (chessNum == 4) {
            switch (mRandom.nextInt(4)) {
                case 0:
                    mSX = 3;
                    mSY = 2;
                    break;
                case 1:
                    mSX = 2;
                    mSY = 3;
                    break;
                case 2:
                    mSX = 5;
                    mSY = 4;
                    break;
                case 3:
                    mSX = 4;
                    mSY = 5;
                    break;
            }
            return true;
        }

        if (chessNum >= 52) {
            mDepth = 64 - chessNum;
        } else if (chessNum >= 48) {
            mDepth = 6;
        } else {
            mDepth = 4;
        }

        final int comScore = computer.computeScore(SCORE_MAP);
        final int plaScore = player.computeScore(SCORE_MAP);
        Status comStatus = new Status ();
        Status plaStatus = new Status ();

        mMinMax[0] = 30000;

        mExhaustive = (chessNum + mDepth >= 64);

        mEffect = 100 * (chessNum + mDepth - 4) / 60;

        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                comStatus.setStatus(computer, comNum, comScore);
                plaStatus.setStatus(player, plaNum, plaScore);

                if (!computeStep(comStatus, plaStatus, x, y))
                    continue;

                if (mDepth == 1) {
                    b = true;
                } else {
                    b = next(1, plaStatus, comStatus, false);
                }

                if (b) {
                    mSX = x;
                    mSY = y;
                    findValidatedStep = true;
                }
            }
        }
        return findValidatedStep;
    }

    private boolean next(int n, final Status p1, final Status p2, boolean already) {
        boolean hasValidatedStep = false;
        mMinMax[n] = 30000;
        Status pp1 = new Status();
        Status pp2 = new Status();
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                pp1.setStatus(p1);
                pp2.setStatus(p2);
                if (!computeStep(pp1, pp2, x, y))
                    continue;
                if (n == mDepth - 1) {
                    final int val = evaluation(pp1, pp2);
                    if (val < mMinMax[n]) {
                        mMinMax[n] = val;
                        if (val <= -mMinMax[n - 1])
                            return false;
                    }
                    hasValidatedStep = true;
                }
            }
        }
        return hasValidatedStep;
    }

    private boolean next(int n, final Square myself, final Square opponent,
                         int my_num, int opp_num, int my_score, int opp_score,
                         boolean already) {
        boolean chess;
        boolean sign = false;
        boolean b;
        boolean count = false;
        int b_my_score;
        int b_opp_score;
        Square my = new Square(myself);
        Square opp = new Square(opponent);
        Square ret = new Square();

        mMinMax[n] = 30000;
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                    if (n == mDepth - 1) {
                        int val = evaluation(opp, my, opp_num - changeNum, my_num
                                + changeNum + 1, b_opp_score, b_my_score);

                        if (val < mMinMax[n]) {
                            mMinMax[n] = val;
                            if (mMinMax[n] <= -mMinMax[n - 1]) {
                                return false;
                            }
                        }
                        sign = true;
                    } else {
                        b = next(n + 1, opp, my, opp_num - changeNum, my_num
                                + changeNum + 1, b_opp_score, b_my_score, false);
                        if (b) {
                            if (mMinMax[n] <= -mMinMax[n - 1]) {
                                return false;
                            }
                            sign = true;
                        }
                    }

                    changeNum = 0;
                    count = false;
                    my.setSquare(myself);
                    opp.setSquare(opponent);
                }
            }

            if (!sign) {
                if (already) {
                    int _final;
                    int base = 20000;

                    if (mExhaustive) {
                        base = 0;

                    }
                    if ((n % 2) == 0) {
                        _final = my_num - opp_num;
                        if (_final > 0) {
                            mMinMax[n] = -(base + _final);
                        } else if (_final < 0) {
                            mMinMax[n] = base - _final;
                        } else {
                            mMinMax[n] = 0;
                        }
                    } else {
                        _final = opp_num - my_num;
                        if (_final > 0) {
                            mMinMax[n] = base + _final;
                        } else if (_final < 0) {
                            mMinMax[n] = -base + _final;
                        } else {
                            mMinMax[n] = 0;
                        }
                    }

                    mMinMax[n - 1] = -mMinMax[n];
                    return false;
                } else {
                    mDepth++;
                    if (n == mDepth - 1) {
                        int val = evaluation(opp, my, opp_num, my_num, opp_score,
                                my_score);
                        mMinMax[n] = val;
                        mDepth--;
                        if (mMinMax[n] <= -mMinMax[n - 1]) {
                            return false;
                        }
                    } else {
                        b = next(n + 1, opp, my, opp_num, my_num, opp_score,
                                my_score, true);
                        mDepth--;
                        if (mMinMax[n] <= -mMinMax[n - 1]) {
                            return false;
                        }

                    }
                }
            }
        }

        mMinMax[n - 1] = -mMinMax[n];
        return true;
    }

    private void initNeighbor() {
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 8; ++x) {
                mNeighbor[x][y] = new Square();
                for (int i = 0; i < DIR_MAP.length; ++i) {
                    final int xx = x + DIR_MAP[i][0];
                    if (xx < 0 || xx >=8)
                        continue;

                    final int yy = y + DIR_MAP[i][1];
                    if (yy < 0 || yy >= 8)
                        continue;

                    mNeighbor[x][y].setChess(xx, yy);
                }
            }
        }
    }

    private void initAxis() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                mAxis[x][y] = new Square();
                for (int m = 0; m < 8; m++) {
                    for (int n = 0; n < 8; n++) {
                        if ((m == y) || (n == x) || ((m + n) == (y + x))
                                || ((m - n) == (y - x))) {
                            mAxis[x][y].setChess(n, m);
                        }
                    }
                }
            }
        }
    }

    private static int getScore(int x, int y) {
        assert(x >= 0 && x <= 7 && y >=0 && y <=7);
        return SCORE_MAP[x + (y << 3)];
    }

    private int evaluation(final Status p1, final Status p2) {
        Square ret = new Square();
        Square board = new Square(p1.square).bitOr(p2.square);

        int axis1 = 0;
        int axis2 = 0;

        if (mExhaustive)
            return p1.number - p2.number;

        if (p1.number + p2.number > 44) {
            for (int x = 0; x < 8; ++x) {
                for (int y = 0; y < 8; ++y) {
                    if (p1.square.getChess(x, y)) {
                        ret.setSquare(board);
                        if (ret.bitAnd(mAxis[x][y]).equals(mAxis[x][y]))
                            ++axis1;
                    } else if (p2.square.getChess(x, y)) {
                        ret.setSquare(board);
                        if (ret.bitAnd(mAxis[x][y]).equals(mAxis[x][y]))
                            ++axis2;
                    }
                }
            }
        }

        return (p1.number - p2.number) * mEffect +
               ((p1.score - p2.score) + (axis1 - axis2) * 4) * (100 - mEffect);
    }

    private int evaluation(final Square com, final Square pla, int com_num,
                           int pla_num, int com_score, int pla_score) {

        int x, y;
        int pla_axis = 0;
        int com_axis = 0;
        Square ret = new Square();
        Square board = new Square(com);
        board = new Square(board.bitOr(pla));

        if (mExhaustive)
            return com_num - pla_num;

        if (com_num + pla_num > 44) {
            for (x = 0; x < 8; x++) {
                for (y = 0; y < 8; y++) {
                    if (pla.getChess(x, y)) {
                        ret.setSquare(board);
                        if (ret.bitAnd(mAxis[x][y]).equals(mAxis[x][y])) {
                            pla_axis++;
                        }
                    } else if (com.getChess(x, y)) {
                        ret.setSquare(board);
                        if (ret.bitAnd(mAxis[x][y]).equals(mAxis[x][y])) {
                            com_axis++;
                        }
                    }
                }
            }
        }

        return mEffect * (com_num - pla_num) + (100 - mEffect)
                * ((com_score - pla_score) + (com_axis - pla_axis) * 4);
    }
}