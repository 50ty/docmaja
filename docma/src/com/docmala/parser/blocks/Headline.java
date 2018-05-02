package com.docmala.parser.blocks;

import com.docmala.parser.Anchor;
import com.docmala.parser.Block;
import com.docmala.parser.SourcePosition;

import java.util.ArrayDeque;

public class Headline extends Block {
    public final int level;
    public final Block content;

    public Headline(SourcePosition start, SourcePosition end, ArrayDeque<Anchor> anchors, int level, Block content) {
        super(start, end, anchors);
        this.level = level;
        this.content = content;
    }

    public static class Builder {
        private SourcePosition start;
        private SourcePosition end;
        private ArrayDeque<Anchor> anchors;
        private int level = 0;
        private Block content;

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

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder increaseLevel() {
            this.level++;
            return this;
        }

        public Builder setContent(Block content) {
            this.content = content;
            return this;
        }

        public Headline build() {
            return new Headline(start, end, anchors, level, content);
        }
    }
}
