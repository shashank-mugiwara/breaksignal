# _A Concurrent Echo Server In Java using NIO SocketChannel Functionality_
Basically NIO refers to Non-Blocking Input/Output operations. Traditional methods of handling a client request included assigning a separate thread for each client connection, read the client request and carry on further processing, this behavior is **blocking io** and does not scale well. Consider a scenario where we have to write a server (just say echo server) which should be able to handle thousands of concurrent connections. Java NIO package now contains (fully from version 7) **non-blocking** constructs for writing a fully concurrent and high performance servers.

### Channel:
A channel represents an open connection to an entity which could be a hardware device, a network socket or even a small program which is capable of performing IO operations. Channels are safe for multithreaded access. To open a _ServerSocketChannel_ we use the below code:
```java
import java.nio.channels.ServerSocketChannel;
try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {}
```

### Multiplexing using Selector
Java NIO provides us a **selector** class which acts as a multiplexor of SelectableChannels. To understand more about multiplexing I would recommend you to go through the blog page [Event Multiplexing](../Concurrency/Python/Concurrency/03-Event-Multiplexing.md). To create a selector multiplexor we use the below code:
```java
import java.nio.channels.Selector;
try (Selector selector = Selector.open()) {}
```

### Channel Registration with Selector
A selectable channel's registration with a selector is represented by a **SelectionKey** object. A selector maintains three sets of selection keys:

1. The **_key set_** contains the keys representing the current channel registrations of this selector. This set is returned by the keys method.
2. The **_selected-key set_** is the set of keys such that each key's channel was detected to be ready for at least one of the operations identified in the key's interest set during a prior selection operation. This set is returned by the selectedKeys method. The selected-key set is always a subset of the key set.
3. The **_cancelled-key set_** is the set of keys that have been cancelled but whose channels have not yet been deregistered. This set is not directly accessible. The cancelled-key set is always a subset of the key set.

When a selectable channel is registered with selector a key is added to the selector's keyset and these added keys are removed when they are cancelled and are added to the selector's cancelled-key set. When we invoke _cancel()_ method on the selection key, it deregister's with the selector in next selection operation.

### How the selection happens ?
The underlying operating system is queried for an update as to the readiness of each remaining channels that are registered to perform IO operations. The ready set returned by the underlying operating system is bitwise-disjoined into the key's current ready set and are found using:

* _selectionKey.isAcceptable()_
* _selectionKey.isReadable()_
* _selectionKey.isWritable()_

Now using the above information, we can overall combine the code as below:
```java
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
                            // Handle a new Connection - newConnection()
                        } else if (selectionKey.isReadable()) {
                            // Perform read operation as OP_READ is ready - echoRead()
                        } else if (selectionKey.isWritable()) {
                            // Perform write operation as OP_WRITE is ready - echoWrite()
                        }

                        itr.remove();
                    }
                }

            }
        }
    }
```

### Follow up code
Since the server that we are writing is of Non-Blocking nature we would like to persist the incoming connections somewhere, say a HashMap, where we use socketChannel as Key and also a context to our connection which stores the current state of a connection.
```java
private static class Context {
    private final ByteBuffer nioBuffer = ByteBuffer.allocate(512);
    private String currentLine = "";
    private boolean terminating = false;
}

private static final HashMap<SocketChannel, Context> contexts = new HashMap<>();
```

We are going with ByteBuffer to store data, we can also go with DirectByteBuffer which is efficient as it remains accessible to the underlying operating system directly to read or write. The choice is yours here, but here I preferred using Heap managed ByteBuffer as creating DirectByteBuffer is a costly operation and also since the server we are writing is an echo server the Heap managed ByteBuffer will just serves the purpose without much difference.

**Handling a new connection**
```java
private static void newConnection(Selector selector, SelectionKey selectionKey) throws IOException {
    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
    SocketChannel socketChannel = serverSocketChannel.accept();
    socketChannel.configureBlocking(false) // making it non-blocking
            .register(selector, SelectionKey.OP_READ); // As our first work is to READ
    contexts.put(socketChannel, new Context()); // Save the state against that SocketChannel
}
```

**Cleanup work**
```java
private static void cleanup(SocketChannel socketChannel) throws IOException {
    socketChannel.close();
    contexts.remove(socketChannel);
}
```

#### Full code can be found here: [AsyncEchoServer.java](../code/AsyncEchoServer.java)