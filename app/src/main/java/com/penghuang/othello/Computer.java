package com.penghuang.othello;

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

    private static final int[][] THE_FIRST_STEP = new int[][] {
            { 3, 2 }, { 2, 3 }, { 5, 4 }, { 4, 5 }
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
        for (final int[] dir : DIR_MAP) {
            final int inx = dir[0];
            final int iny = dir[1];
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

    private boolean next(int n, final Status p1, final Status p2, boolean already) {
        boolean hasBetterStep = false;
        mMinMax[n] = 30000;
        Status pp1 = new Status();
        Status pp2 = new Status();
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                pp1.setStatus(p1);
                pp2.setStatus(p2);
                if (!computeStep(pp1, pp2, x, y))
                    continue;

                if (n == mDepth) {
                    final int val = evaluation(pp2, pp1);
                    if (val < mMinMax[n]) {
                        mMinMax[n] = val;
                        if (val <= -mMinMax[n - 1])
                            return false;
                    }
                } else {
                    final boolean b = next(n + 1, pp2, pp1, false);
                    if (b && mMinMax[n] <= -mMinMax[n - 1])
                        return false;
                }
                hasBetterStep = true;
            }
        }

        if (!hasBetterStep) {
            if (already) {
                int final_;
                final int base = mExhaustive ? 0 : 20000;
                if ((n & 1) == 1) {
                    final_ = p1.score - p2.score;
                    if (final_ > 0) {
                        mMinMax[n] = -(base + final_);
                    } else if (final_ < 0) {
                        mMinMax[n] = base - final_;
                    } else {
                        mMinMax[n] = 0;
                    }
                } else {
                    final_ = p2.score - p1.score;
                    if (final_ > 0) {
                        mMinMax[n] = base + final_;
                    } else if (final_ < 0) {
                        mMinMax[n] = -(base - final_);
                    } else {
                        mMinMax[n] = 0;
                    }
                }
                mMinMax[n - 1] = mMinMax[n];
                return false;
            } else {
                if (n == mDepth) {
                    final int val = evaluation(p1, p2);
                    mMinMax[n] = val;
                    if (mMinMax[n] <= -mMinMax[n - 1])
                        return false;
                } else {
                    ++mDepth;
                    next(n + 1, p2, p1, true);
                    --mDepth;
                    if (mMinMax[n] <= -mMinMax[n - 1])
                        return false;
                }
            }
        }
        mMinMax[n - 1] = -mMinMax[n];
        return hasBetterStep;
    }

    public boolean begin(final Square computer, final Square player, int comNum, int plaNum) {
        boolean findValidatedStep = false;
        final int chessNum = comNum + plaNum;
        if (chessNum == 4) {
            final int i  = mRandom.nextInt(4);
            mSX = THE_FIRST_STEP[i][0];
            mSY = THE_FIRST_STEP[i][1];
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

        mExhaustive = (chessNum + mDepth >= 64);
        mEffect = 100 * (chessNum + mDepth - 4) / 60;
        mMinMax[1] = 30000;

        Status comStatus = new Status ();
        Status plaStatus = new Status ();
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                comStatus.setStatus(computer, comNum, comScore);
                plaStatus.setStatus(player, plaNum, plaScore);

                if (!computeStep(comStatus, plaStatus, x, y))
                    continue;

                // Search next step if mDepth > 1.
                findValidatedStep = (mDepth == 1) || next(2, plaStatus, comStatus, false);

                if (findValidatedStep) {
                    mSX = x;
                    mSY = y;
                    findValidatedStep = true;
                }
            }
        }
        return findValidatedStep;
    }

    private void initNeighbor() {
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 8; ++x) {
                mNeighbor[x][y] = new Square();
                for (final int[] dir : DIR_MAP) {
                    final int xx = x + dir[0];
                    if (xx < 0 || xx >= 8)
                        continue;
                    final int yy = y + dir[1];
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
        assert(x >= 0 && x <= 7 && y >= 0 && y <= 7);
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
}