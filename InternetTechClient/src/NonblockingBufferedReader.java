import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.util.concurrent.BlockingQueue;

public class NonblockingBufferedReader
{
    private final BlockingQueue<String> lines;
    private volatile boolean closed;
    private Thread backgroundReaderThread;

    public NonblockingBufferedReader(final BufferedReader bufferedReader) {
        this.lines = new LinkedBlockingQueue<String>();
        this.closed = false;
        this.backgroundReaderThread = null;
        (this.backgroundReaderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        final String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        NonblockingBufferedReader.this.lines.add(line);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    NonblockingBufferedReader.this.closed = true;
                }
            }
        })).setDaemon(true);
        this.backgroundReaderThread.start();
    }

    public String readLine() throws IOException {
        try {
            return (this.closed && this.lines.isEmpty()) ? null : this.lines.poll(500L, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            throw new IOException("The BackgroundReaderThread was interrupted!", e);
        }
    }

    public void close() {
        if (this.backgroundReaderThread != null) {
            this.backgroundReaderThread.interrupt();
            this.backgroundReaderThread = null;
        }
    }
}