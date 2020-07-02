package su.dreamtime.bots.util.log;

import su.dreamtime.bots.Main;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggingErrorPrintStream extends PrintStream {

    public LoggingErrorPrintStream(OutputStream out) {
        super(out);
    }

    public LoggingErrorPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    List<String> buf = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void print(String x) {
        if (x.startsWith("\t")) {
            x = x.replaceFirst("\t", "    ");
        }
        Main.getLogger().error(x);
    }

    @Override
    public void print(boolean b) {
        print(String.valueOf(b));
    }

    @Override
    public void print(char c) {
        print(String.valueOf(c));
    }

    @Override
    public void print(int i) {
        print(String.valueOf(i));
    }

    @Override
    public void print(long l) {
        print(String.valueOf(l));
    }

    @Override
    public void print(float f) {
        print(String.valueOf(f));
    }

    @Override
    public void print(double d) {
        print(String.valueOf(d));
    }

    @Override
    public void print(char[] s) {
        print(String.valueOf(s));
    }

    @Override
    public void print(Object obj) {
        print(String.valueOf(obj));
    }

    public void println(String x) {
        print(x);
    }

    @Override
    public void println() {
        print("");
    }

    @Override
    public void println(boolean x) {
        print(String.valueOf(x));
    }

    @Override
    public void println(char x) {
        print(String.valueOf(x));
    }

    @Override
    public void println(int x) {
        print(String.valueOf(x));
    }

    @Override
    public void println(long x) {
        print(String.valueOf(x));
    }

    @Override
    public void println(float x) {
        print(String.valueOf(x));
    }

    @Override
    public void println(double x) {
        print(String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        print(String.valueOf(x));
    }

    @Override
    public void println(Object x) {
        print(String.valueOf(x));
    }
}
