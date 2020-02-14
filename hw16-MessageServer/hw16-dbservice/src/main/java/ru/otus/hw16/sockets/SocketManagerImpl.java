package ru.otus.hw16.sockets;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.hw16.msclient.MsClient;
import ru.otus.hw16.shared.mesages.MessageTransport;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public final class SocketManagerImpl implements SocketManager {
    private static final Logger logger = LoggerFactory.getLogger(SocketManagerImpl.class);

    private static final int MESSAGE_QUEUE_SIZE = 100_000;
    private static final int MSG_HANDLER_THREAD_LIMIT = 2;

    private final AtomicBoolean runFlag = new AtomicBoolean(true);

    private final Map<String, MsClient> clientMap = new ConcurrentHashMap<>();
    private SocketClient socketClient;

    private final BlockingQueue<MessageTransport> messageQueue = new ArrayBlockingQueue<>(MESSAGE_QUEUE_SIZE);

    private final ExecutorService msgProcessor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("msg-processor-thread");
        return thread;
    });

    private final ExecutorService socketMessageProcessor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("msg-processor-thread");
        return thread;
    });

    private final ExecutorService msgHandler = Executors.newFixedThreadPool(MSG_HANDLER_THREAD_LIMIT, new ThreadFactory()
    {
        private final AtomicInteger threadNameSeq = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("msg-handler-thread-" + threadNameSeq.incrementAndGet());
            return thread;
        }
    });

    @Override
    public void addMsClient(MsClient msClient) {
        logger.debug("new client:{}", msClient.getName());
        if (clientMap.containsKey(msClient.getName())) {
            throw new IllegalArgumentException("Error. client: " + msClient.getName() + " already exists");
        }
        clientMap.put(msClient.getName(), msClient);
    }

    @Override
    public void addSocketClient(SocketClient socketClient) {
        this.socketClient = socketClient;
        if (socketClient != null) {
            socketMessageProcessor.submit(this::socketMessageProcessor);
        }
    }

    @Override
    public void start() {
        msgProcessor.submit(this::msgProcessor);
    }

    @Override
    public boolean newMessage(MessageTransport msg) {
        if (runFlag.get()) {
            return messageQueue.offer(msg);
        } else {
            logger.warn("MS is being shutting down... rejected:{}", msg);
            return false;
        }
    }

    @Override
    public void receiveMessage(MessageTransport msg) {
        if (runFlag.get()) {
            socketClient.sendMessage(msg);;
        } else {
            logger.warn("MS is being shutting down... rejected:{}", msg);
        }
    }

    private void msgProcessor() {
        logger.debug("Start msgProcessor");
        while (runFlag.get()) {
            try {
                logger.debug("messageQueue.take");
                MessageTransport msg = messageQueue.take();
                MsClient clientTo = clientMap.get(msg.getTo());
                if (clientTo == null) {
                    logger.warn("client not found");
                } else {
                    logger.debug("msgHandler.submit");
                    msgHandler.submit(() -> {
                        logger.debug("msgHandler submit message: {} ",msg);
                        handleMessage(clientTo, msg);
                    });
                }
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        logger.debug("msgProcessor finished");
    }

    private void socketMessageProcessor(){
        logger.debug("Start socketMessageProcessor");
        while (runFlag.get()) {
            if (socketClient == null) {
                logger.warn("socketClient not found");
            } else {
                     MessageTransport message = socketClient.getMessageFromMS();
                    logger.debug("new message: {}",message );
                    newMessage(message);
            }
        }
    }

    private void handleMessage(MsClient msClient, MessageTransport msg) {
        try {
            logger.debug("handleMessage ");
            msClient.handle(msg);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("message:{}", msg);
        }
    }

    private void insertStopMessage() throws InterruptedException {
        boolean result = messageQueue.offer(MessageTransport.VOID_MESSAGE);
        while (!result) {
            Thread.sleep(100);
            result = messageQueue.offer(MessageTransport.VOID_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        runFlag.set(false);
        try {
            insertStopMessage();
            msgProcessor.shutdown();
            msgHandler.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
           logger.error(e.getMessage(),e);
        }
    }

}
