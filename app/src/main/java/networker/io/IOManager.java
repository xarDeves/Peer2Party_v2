package networker.io;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOManager {
    public static final int MULTIMEDIA_BLOCK_SIZE = 500_000; // 500 KB
    public static final int TEXT_BLOCK_SIZE = 10_000; // 10 KB

    private final ExecutorService inboundExecutor;
    private final ExecutorService outboundExecutor;

    public IOManager(int executorTCount) {
        inboundExecutor = Executors.newFixedThreadPool(executorTCount);
        outboundExecutor = Executors.newFixedThreadPool(executorTCount);
    }

    //TODO
}
