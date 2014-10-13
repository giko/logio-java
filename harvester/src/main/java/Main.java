import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

/**
 * Created by giko on 10/13/14.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 28878);
        OutputStreamWriter outToServer = new OutputStreamWriter(clientSocket.getOutputStream());

        outToServer.write("node|".concat("fuck"));
        outToServer.flush();

        String nodeName = System.getenv().get("vNode");
        Scanner s = null;
        try {
            s = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("We are fucked, file not found!", e);
        }
        s.useDelimiter("\n");
        List<String> globs = new ArrayList<String>();
        while (s.hasNext()) {
            globs.add(s.next());
        }
        s.close();
        for (String glob : globs) {
            try {
                String[] argsL = glob.split("\\ ");
                Path directory = Paths.get(argsL[1]);
                WatchService watchService = directory.getFileSystem().newWatchService();
                MyWatchQueueReader reader = new MyWatchQueueReader(watchService, FileSystems.getDefault().getPathMatcher("glob:".concat(argsL[2])), argsL[1], outToServer, argsL[0]);
                Thread readerThread = new Thread(reader);
                readerThread.start();
                directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {

            }
        }
    }

    private static class MyWatchQueueReader implements Runnable {

        /**
         * the watchService that is passed in from above
         */
        private WatchService myWatcher;
        private PathMatcher matcher;
        private String path;
        private Map<String, Long> fileSizes = new HashMap<>();
        private OutputStreamWriter outputStream;
        private String stream;

        public MyWatchQueueReader(WatchService myWatcher, PathMatcher matcher, String path, OutputStreamWriter dataOutputStream, String stream) {
            this.myWatcher = myWatcher;
            this.matcher = matcher;
            this.path = path;
            this.outputStream = dataOutputStream;
            this.stream = stream;
        }

        /**
         * In order to implement a file watcher, we loop forever
         * ensuring requesting to take the next item from the file
         * watchers queue.
         */
        @Override
        public void run() {
            try {
                // get the first event before looping
                WatchKey key = myWatcher.take();
                while (key != null) {
                    // we have a polled event, now we traverse it and 
                    // receive all the states from it
                    for (WatchEvent event : key.pollEvents()) {
                        if (matcher.matches(Paths.get(event.context().toString()))) {
                            File file = Paths.get(path + "/" + (String) event.context().toString()).toFile();
                            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                            long offset = fileSizes.get(event.context().toString()) == null ? 0 : fileSizes.get(event.context().toString());
                            long len = (randomAccessFile.length() - offset);
                            byte[] changes = new byte[(int) len];
                            randomAccessFile.seek(offset);
                            randomAccessFile.readFully(changes);
                            fileSizes.put(event.context().toString(), randomAccessFile.length());
                            randomAccessFile.close();
                            outputStream.write("log|" + stream + "|info|".concat(new String(changes)));
                            outputStream.flush();
                        }
                    }
                    key.reset();
                    key = myWatcher.take();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                throw new RuntimeException("An error occurred!", e);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred!", e);
            }
            System.out.println("Stopping thread");
        }
    }
}
