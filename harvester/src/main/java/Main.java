import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.*;

/**
 * Created by giko on 10/13/14.
 */
public class Main {
    public static void main(String[] args) {
        String nodeName = System.getenv().get("vNode");
        Scanner s = null;
        try {
            s = new Scanner(new File("/home/giko/logio.list"));
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
                Path directory = Paths.get(argsL[0]);
                WatchService watchService = directory.getFileSystem().newWatchService();
                MyWatchQueueReader reader = new MyWatchQueueReader(watchService, FileSystems.getDefault().getPathMatcher("glob:".concat(argsL[1])), argsL[0]);
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

        public MyWatchQueueReader(WatchService myWatcher, PathMatcher matcher, String path) {
            this.myWatcher = myWatcher;
            this.matcher = matcher;
            this.path = path;
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
                        if (matcher.matches(Paths.get(String.valueOf(event.context())))){
                            File file = Paths.get(path+"/"+(String)event.context().toString()).toFile();
                            RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
                            long offset = fileSizes.get(event.context().toString()) == null ? 0 : fileSizes.get(event.context().toString());
                            byte[] arra = new byte[(int) (randomAccessFile.length()-offset)];
                            randomAccessFile.readFully(arra,(int)  offset, (int) (randomAccessFile.length() - offset));
                            fileSizes.put(event.context().toString(), randomAccessFile.length());
                            System.out.println(new String(arra));
                            System.out.printf("Received %s event for file: %s\n",
                                    event.kind(), event.context());
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
