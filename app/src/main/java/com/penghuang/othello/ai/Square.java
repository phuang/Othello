package com.penghuang.othello.ai;

/**
 * Created by penghuang on 2/24/15.
 */

class Square {
    private long mData = 0l;

    Square() {
        this(0l);
    }

    Square(final Square q) {
        this(q.mData);
    }

    Square(long d) {
        setSquare(d);
    }

    void setSquare(final Square q) {
        setSquare(q.mData);
    }

    void setSquare(long d) {
        mData = d;
    }

    void setChess(int x, int y) {
        assert((x <= 7) && (x >= 0) && (y <= 7) && (y >= 0));
        mData |= 1l << ((y << 3) + x);
    }

    void clearChess(int x, int y) {
        assert((x <= 7) && (x >= 0) && (y <= 7) && (y >= 0));
        mData &= ~(1l << ((y << 3) + x));
    }

    boolean getChess(int x, int y) {
        assert((x <= 7) && (x >= 0) && (y <= 7) && (y >= 0));
        return (mData & (1l << ((y << 3) + x))) != 0l;
    }

    int computeScore(final int[] scoreMap) {
        int score = 0;
        for (int i = 0; i < 64; ++i) {
            if (0 != ((1l << i) & mData)) {
                score += scoreMap[i];
            }
        }
        return score;
    }

    boolean equals(final Square q) {
        return (q.mData == mData);
    }

    Square bitOr(final Square q) {

        mData |= q.mData;
        return this;
    }

    Square bitAnd(final Square q) {

        mData &= q.mData;
        return this;
    }

    boolean isEmpty() {
        return (mData == 0);
    }

    public String toString() {
        String str = new String();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (this.getChess(x, y)) {
                    str += "|X";
                } else {
                    str += "| ";
                }
            }
            str += "|\n";
        }
        str += "\n";
        return str;
    }
}