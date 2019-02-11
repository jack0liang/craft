package io.craft.idl.util;

import freemarker.core.DirectiveCallPlace;
import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;

public class FormatDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {
        SimpleNumber blank = (SimpleNumber) params.get("blank");
        Writer out = env.getOut();
        out.flush();
        FormatWriter writer = new FormatWriter(out, blank.getAsNumber().intValue());
        body.render(writer);
    }

    private static class FormatWriter extends Writer {

        private Writer out;

        private char[] space;

        private LineBuffer buffer;

        public FormatWriter(Writer out, int blank) {
            this.out = out;
            this.space = new char[blank];
            this.buffer = new LineBuffer() {
                @Override
                protected void handleLine(String line, String end) throws IOException {
                    out.write(space);
                    out.write(line);
                    out.write(end);
                }
            };
            Arrays.fill(this.space, ' ');
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            buffer.add(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            buffer.finish();
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

    }

    abstract static class LineBuffer {
        /** Holds partial line contents. */
        private StringBuilder line = new StringBuilder();
        /** Whether a line ending with a CR is pending processing. */
        private boolean sawReturn;

        /**
         * Process additional characters from the stream. When a line separator is found the contents of
         * the line and the line separator itself are passed to the abstract {@link #handleLine} method.
         *
         * @param cbuf the character buffer to process
         * @param off the offset into the buffer
         * @param len the number of characters to process
         * @throws IOException if an I/O error occurs
         * @see #finish
         */
        protected void add(char[] cbuf, int off, int len) throws IOException {
            int pos = off;
            if (sawReturn && len > 0) {
                // Last call to add ended with a CR; we can handle the line now.
                if (finishLine(cbuf[pos] == '\n')) {
                    pos++;
                }
            }

            int start = pos;
            for (int end = off + len; pos < end; pos++) {
                switch (cbuf[pos]) {
                    case '\r':
                        line.append(cbuf, start, pos - start);
                        sawReturn = true;
                        if (pos + 1 < end) {
                            if (finishLine(cbuf[pos + 1] == '\n')) {
                                pos++;
                            }
                        }
                        start = pos + 1;
                        break;

                    case '\n':
                        line.append(cbuf, start, pos - start);
                        finishLine(true);
                        start = pos + 1;
                        break;

                    default:
                        // do nothing
                }
            }
            line.append(cbuf, start, off + len - start);
        }

        /** Called when a line is complete. */
        private boolean finishLine(boolean sawNewline) throws IOException {
            String separator = sawReturn
                    ? (sawNewline ? "\r\n" : "\r")
                    : (sawNewline ? "\n" : "");
            handleLine(line.toString(), separator);
            line = new StringBuilder();
            sawReturn = false;
            return sawNewline;
        }

        /**
         * Subclasses must call this method after finishing character processing, in order to ensure that
         * any unterminated line in the buffer is passed to {@link #handleLine}.
         *
         * @throws IOException if an I/O error occurs
         */
        protected void finish() throws IOException {
            if (sawReturn || line.length() > 0) {
                finishLine(false);
            }
        }

        /**
         * Called for each line found in the character data passed to {@link #add}.
         *
         * @param line a line of text (possibly empty), without any line separators
         * @param end the line separator; one of {@code "\r"}, {@code "\n"}, {@code "\r\n"}, or {@code ""}
         * @throws IOException if an I/O error occurs
         */
        protected abstract void handleLine(String line, String end) throws IOException;
    }
}