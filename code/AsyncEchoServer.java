import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

public class AsynchronousEchoServer {

    private static final Pattern QUIT = Pattern.compile("(\\r)?(\\n)?/quit$");


    public static void main(String[] args) throws IOException {
        try (Selector selector = Selector.open()) {
            try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                serverSocketChannel.bind(new InetSocketAddress(9090));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


                while (true) {
                    selector.select();
                    Iterator<SelectionKey> itr = selector.selectedKeys().iterator();

                    while(itr.hasNext()) {
                        SelectionKey selectionKey = itr.next();

                        if (selectionKey.isAcceptable()) {
                            newConnection(selector, selectionKey);
                        } else if (selectionKey.isReadable()) {
                            echoRead(selectionKey);
                        } else if (selectionKey.isWritable()) {
                            echoWrite(selector, selectionKey);
                        }

                        itr.remove();
                    }
                }

            }
        }
    }

    private static class Context {
        private final ByteBuffer nioBuffer = ByteBuffer.allocate(512);
        private String currentLine = "";
        private boolean terminating = false;
    }

    private static final HashMap<SocketChannel, Context> contexts = new HashMap<>();

    private static void newConnection(Selector selector, SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false)
                .register(selector, SelectionKey.OP_READ);
        contexts.put(socketChannel, new Context());
    }

    private static void echoRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Context context = contexts.get(socketChannel);

        try {
            socketChannel.read(context.nioBuffer);
            context.nioBuffer.flip();
            context.currentLine = context.currentLine + Charset.defaultCharset().decode(context.nioBuffer);

            if (QUIT.matcher(context.currentLine).find()) {
                context.terminating = true;
            } else if (context.currentLine.length() > 16) {
                context.currentLine = context.currentLine.substring(8);
            }

            context.nioBuffer.flip();
            int count = socketChannel.write(context.nioBuffer);

            if (count < context.nioBuffer.limit()) {
                selectionKey.cancel();
                socketChannel.register(selectionKey.selector(), SelectionKey.OP_WRITE);
            } else {
                context.nioBuffer.clear();
                if (context.terminating) {
                    cleanup(socketChannel);
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    private static void cleanup(SocketChannel socketChannel) throws IOException {
        socketChannel.close();
        contexts.remove(socketChannel);
    }

    private static void echoWrite(Selector selector, SelectionKey selectionKey) {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Context context = contexts.get(socketChannel);

        try {
            int remainingBytes = context.nioBuffer.limit() - context.nioBuffer.position();
            int count = socketChannel.write(context.nioBuffer);

            if (count == remainingBytes) {
                context.nioBuffer.clear();
                selectionKey.cancel();

                if (context.terminating) {
                    cleanup(socketChannel);
                } else {
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
