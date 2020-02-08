package ru.otus.hw16.sockets;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.otus.hw16.mesages.Message;
import ru.otus.hw16.msclient.MsClient;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public final class SocketManagerImpl implements SocketManager{
    private static final Logger logger = LoggerFactory.getLogger(SocketManagerImpl.class);

    private static final int MESSAGE_QUEUE_SIZE = 100_000;
    private static final int MSG_HANDLER_THREAD_LIMIT = 2;

    private final AtomicBoolean runFlag = new AtomicBoolean(true);

    private  MsClient msClient;
    private SocketClient socketClient;

    private final BlockingQueue<Message> messageQueue = new ArrayBlockingQueue<>(MESSAGE_QUEUE_SIZE);

    private final ExecutorService msgProcessor = Executors.newSingleThreadExecutor(runnable -> {
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
        logger.debug("new msClient:{}", msClient.getName());
        this.msClient = msClient;
    }

    @Override
    public void addSocketClient(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    @Override
    public void start() {
        msgProcessor.submit(this::msgProcessor);
    }

    @Override
    public boolean newMessage(Message msg) {
        if (runFlag.get()) {
            return messageQueue.offer(msg);
        } else {
            logger.warn("MS is being shutting down... rejected:{}", msg);
            return false;
        }
    }

    private void msgProcessor() {
        logger.debug("Start msgProcessor");
        while (runFlag.get()) {
            try {
                logger.debug("messageQueue.take");
                Message msg = messageQueue.take();

                if (msClient == null) {
                    logger.warn("client not found");
                } else {
                    logger.debug("msgHandler.submit");

                    msgHandler.submit(() -> {
                        logger.debug("msgHandler sendMessage: {} ",msg);
                        socketClient.sendMessage(msg);
                        sleep();
                        Message message = socketClient.receiveMessage();
                        logger.debug("msgHandler receiveMessage: {} ",message);
                        handleMessage(msClient, message);
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


    private static void sleep() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleMessage(MsClient msClient, Message msg) {
        try {
            logger.debug("handleMessage ");
            msClient.handle(msg);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("message:{}", msg);
        }
    }

    private void insertStopMessage() throws InterruptedException {
        boolean result = messageQueue.offer(Message.VOID_MESSAGE);
        while (!result) {
            Thread.sleep(100);
            result = messageQueue.offer(Message.VOID_MESSAGE);
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
            e.printStackTrace();
        }
    }

}
