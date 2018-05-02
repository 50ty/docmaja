package com.docmala.parser.blocks;

import com.docmala.parser.Anchor;
import com.docmala.parser.Block;
import com.docmala.parser.SourcePosition;

import java.util.ArrayDeque;

public class Table extends Block {

    public static class Cell {
        public final ArrayDeque<Block> content;
        public final int columnSpan;
        public final int rowSpan;
        public final boolean isHeading;
        public final boolean isHiddenBySpan;

        public Cell(ArrayDeque<Block> content, int columnSpan, int rowSpan, boolean isHeading, boolean isHiddenBySpan) {
            this.content = content;
            this.columnSpan = columnSpan;
            this.rowSpan = rowSpan;
            this.isHeading = isHeading;
            this.isHiddenBySpan = isHiddenBySpan;
        }

        public static class Builder {
            private ArrayDeque<Block> content = new ArrayDeque<>();
            private int columnSpan = 1;
            private int rowSpan = 1;
            private boolean isHeading = false;
            private boolean isHiddenBySpan = false;

            public Builder setColumnSpan(int columnSpan) {
                this.columnSpan = columnSpan;
                return this;
            }

            public Builder setRowSpan(int rowSpan) {
                this.rowSpan = rowSpan;
                return this;
            }

            public Builder setHeading(boolean heading) {
                isHeading = heading;
                return this;
            }

            public Builder setHiddenBySpan(boolean hiddenBySpan) {
                isHiddenBySpan = hiddenBySpan;
                return this;
            }

            public Cell build() {
                return new Cell(content, columnSpan, rowSpan, isHeading, isHiddenBySpan);
            }
        }
    }

    final Cell[][] cells;

    public Table(SourcePosition start, SourcePosition end, ArrayDeque<Anchor> anchors, Cell[][] cells) {
        super(start, end, anchors);
        this.cells = cells;
    }

    public Cell[][] cells() {
        return cells;
    }

    public static class Builder {
        private SourcePosition start;
        private SourcePosition end;
        private ArrayDeque<Anchor> anchors;
        private Cell[][] cells;

        public Builder setStart(SourcePosition start) {
            this.start = new SourcePosition(start);
            return this;
        }

        public Builder setEnd(SourcePosition end) {
            this.end = new SourcePosition(end);
            return this;
        }

        public Builder setAnchors(ArrayDeque<Anchor> anchors) {
            this.anchors = anchors;
            return this;
        }

        public Builder setCells(Cell[][] cells) {
            this.cells = cells;
            return this;
        }

        public Table build() {
            return new Table(start, end, anchors, cells);
        }

    }
}
