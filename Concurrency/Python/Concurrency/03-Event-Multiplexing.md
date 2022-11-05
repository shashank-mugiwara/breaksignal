## **_Event Multiplexing_**

Before jumping into the working mechanics of **_epoll_** or **_kqueue_** I am listing down few points on event multiplexing below:

1. Multiplexing is an art of doing multiple I/O operations with just a single thread.
2. In general we mean that we first give/distribute a task, track it and then wait for more than one task to complete or a preferred event to happen.
3. If one thread can do I/O on many file descriptors and if I/O is slow then one thread may handle all the needs of the application that is invoking these I/O tasks.
4. Webservers greatly benefit from this as they work on multiple I/O events at a time, thus able to handle thousands of concurrent connections.
5. Now once a I/O work is given, how do we know when the work is being completed ? Can we use signals for this?, but signal handling in linux is quite complicated and we will get into multiple signal handling problems. The key for this is also Multiplexing, let us understand it taking reactor pattern as an example:

    **(_a_)** Reactor pattern is readily available in many modern networking libraries which are built on top of the OS specific multiplexing API.

    **(_b_)** We construct multiple objects with methods to handle various type of events. The framework then uses a single thread to wait for any of these events using the OS API.

    **(_c_)** When the event happens the thread wakes up and calls the appropriate method on the appropriate object.

Linux multiplexing API is comprised of three system calls/groups which all work on file descriptors, namely:

1. select
2. poll
3. epoll

And in case of FreeBSD and MacOS we have:

1. kqueue

## **_epoll vs kqueue_**
**_epoll_** and **_kqueue_** are kernel system calls for scalable I/O event notification mechanisms in an efficient manner. In simple words, you subscribe to certain kernel events and you get notified when any of those events occur. These system calls are desigend for scalable situations such as a webserver where 10,000 concurrent connections are being handled by one server.

In terms of performance, the epoll design has a weakness; it does not support multiple updates on the interest set in a single system call. When you have 100 file descriptors to update their status in the interest set, you have to make 100 epoll_ctl() calls. The performance degradation from the excessive system calls is significant. In contrast, you can specify multiple interest updates in a single kevent() call.