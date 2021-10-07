package awais.backworddictionary.helpers;

import android.os.Build;

import androidx.annotation.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;

public final class URLEncoder {
    private static final int caseDiff = 'a' - 'A';
    private static final BitSet dontNeedEncoding;
    private static final CharacterWriter characterWriter = new CharacterWriter();
    private static Charset charset;


    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) charset = StandardCharsets.UTF_8;
        else try {
            charset = Charset.forName(Utils.CHARSET);
        } catch (final Exception ignored) {
            charset = Charset.defaultCharset();
        }

        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) dontNeedEncoding.set(i);
        for (i = 'A'; i <= 'Z'; i++) dontNeedEncoding.set(i);
        for (i = '0'; i <= '9'; i++) dontNeedEncoding.set(i);
        dontNeedEncoding.set(' ');
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');
    }

    private URLEncoder() { }

    @NonNull
    public static String encode(@NonNull final String s) {
        final int strLength = s.length();
        final StringBuilder out = new StringBuilder(strLength);

        characterWriter.reset();

        boolean needToChange = false;
        for (int i = 0; i < strLength; ) {
            int c = (int) s.charAt(i);
            if (dontNeedEncoding.get(c)) {
                if (c == ' ') {
                    c = '+';
                    needToChange = true;
                }
                out.append((char) c);
                i++;
            } else {
                do {
                    characterWriter.write(c);
                    if (c >= 0xD800 && c <= 0xDBFF && i + 1 < strLength) {
                        final int d = (int) s.charAt(i + 1);
                        if (d >= 0xDC00 && d <= 0xDFFF) {
                            characterWriter.write(d);
                            i++;
                        }
                    }
                    i++;
                } while (i < strLength && !dontNeedEncoding.get(c = (int) s.charAt(i)));

                final String str = new String(characterWriter.toCharArray());
                final byte[] ba = str.getBytes(charset);
                for (final byte b : ba) {
                    out.append('%');
                    char ch = Character.forDigit(b >> 4 & 0xF, 16);
                    if (Character.isLetter(ch)) ch -= caseDiff;
                    out.append(ch);
                    ch = Character.forDigit(b & 0xF, 16);
                    if (Character.isLetter(ch)) ch -= caseDiff;
                    out.append(ch);
                }
                characterWriter.reset();
                needToChange = true;
            }
        }

        characterWriter.reset();

        return needToChange ? out.toString() : s;
    }

    private static final class CharacterWriter {
        private final Object lock;
        private char[] buf;
        private int count;

        private CharacterWriter() {
            lock = this;
            buf = new char[32];
        }

        private void write(final int c) {
            synchronized (lock) {
                final int newcount = count + 1;
                if (newcount > buf.length)
                    buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
                buf[count] = (char) c;
                count = newcount;
            }
        }

        @NonNull
        private char[] toCharArray() {
            synchronized (lock) {
                return Arrays.copyOf(buf, count);
            }
        }

        private void reset() {
            count = 0;
        }
    }
}