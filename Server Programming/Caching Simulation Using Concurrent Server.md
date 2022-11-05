# _Simple simulation to show how caching works_

## Requirements
We will try to create a **_server_**, **_proxy_** which should be able to handle 1000 concurrent connections. The server basically has a database/dict which is maintained globally and supports functionalities like adding a Key-Value pair, Updating and retrieving the Key-Value pair. More features include:

1. **_proxy_** server acts as an intermediary server to which the client connects.
2. **_client_** after connecting to the **_proxy_** server issues commands to store, retrieve and delete Key-Value pairs. Commands include:<br/>
a. **PUT key=value** - To add the key,value pair to the database.<br/>
b. **GET key** - To get value for the particular key.<br/>
c. **DUMP** - To dump entire existing Key-Value database as one json string.<br/>
3. **_proxy_** server should be able to cache these Key-Value pairs and if absent in its cache, should forward the request to the actual **_server_**.
4. All the servers should be able to handle multiple (1000s) of clients concurrently.

## Design Considerations
To handle multiple clients concurrently in a non-blocking fashion, we can use **_asyncio_** library as a fundamental building block of our servers. To understand more about Python's model of concurrency, I would like you to follow these tutorials below:</br>
1. [Trying to run concurrent code in Python](../Python/Concurrency/01-Trying-To-Run-Concurrent-Code.md)
2. [Understanding IO bound and CPU bound code](../Python/Concurrency/02-IO-vs-CPU-Bound.md)
3. [Understanding Event Multiplexing in Operating System](../Python/Concurrency/03-Event-Multiplexing.md)


## Code Design

We will start writing the **_server_** (Backend Server) first and then move to **_proxy_**. As mentioned above we will be using **_asyncio_** library for writing the server, so we will maintain two methods, mainly:<br/>
1. _run_server_ - where the asyncio server starting/managing code goes.
2. _handle_client_ - code for handling request and response.
----
#### `server.py`
We shall import asyncio and json at first and also with this we will declare a dict named **_key_value_db_** to store the key value. As we are just doing a simulation using in memory dict as database is totally fine. We will run the server on our local/loopback address on port **9090**. Let proxy server run on port **8080**.

```python
import asyncio
import json

key_value_db = {}

BACKEND_SERVER_HOST = 'localhost'
BACKEND_SERVER_PORT = 9090
```

Defining the **_run_server_** function and that's pretty much the code for writing a concurrent server, all the heavy lifting will be done by asyncio. We will use *asyncio.run ( run_server())* to start the server.

```python
async def run_server():
    server = await asyncio.start_server(handle_client, BACKEND_SERVER_HOST, BACKEND_SERVER_PORT)
    async with server:
        await server.serve_forever()

asyncio.run(run_server())
```

Defining the **_handle_client_** function; As we are using a global Key-Value database - **_key_value_db_** we make it as global in our client handling method. To get request data in form of string we use reader object's read method and we are interested in reading 255 bytes which is sufficient for accepting client's command with key-value pair.
```python
global key_value_db
request = (await reader.read(255)).decode('utf-8')
decoded_data = request.strip() # Making sure no white spaces at both ends
operation = decoded_data.split()[0] # Getting the operation to be performed - GET, PUT, DUMP etc
```

Then we check the type of operation and write logic of storing the key accordingly, which is shown below.<br/>
**handle_client logic**:
```python
    if operation == "GET":
        key_to_get = decoded_data.split()[1]
        if key_to_get in key_value_db:
            response_message = str(key_value_db[key_to_get])
            writer.write(response_message.encode('utf8'))
            await writer.drain()
        else:
            response_message = "No such key found"
            writer.write(response_message.encode('utf-8'))
            await writer.drain()
    elif operation == "PUT":
        ops_key = decoded_data.split("=")[0].strip()
        key = ops_key.split()[1].strip()
        value = decoded_data.split("=")[1].strip()
        key_value_db[key] = value
        response_message = "OK"
        writer.write(response_message.encode('utf-8'))
        await writer.drain()
    elif operation == "DUMP":
        cache_data = json.dumps(key_value_db)
        writer.write(cache_data.encode('utf-8'))
        await writer.drain()

    writer.close()
```
----

#### `proxy.py`
In **_proxy_** server we will use the socket import additionally to forward the request to our server. We will use two set of ports, and also a dict named cache as a cache memory.
```python
import asyncio
import socket

PROXY_HOST = "localhost"
PROXY_PORT = 8080

BACKEND_SERVER_HOST = "localhost"
BACKEND_SERVER_PORT = 9090

cache = {}
```

In the **handle_client** section we will add a special check for **GET** command where we check if the key is already present in the cache, so:
1. If key is absent, we forward the request to the actual backend server, retrieve the key and store it in our cache.
2. If key is present in the cache, we will directly return the response.

**handle_client logic:**
```python
async def handle_client(reader, writer):
    global cache
    request = (await reader.read(255)).decode('utf-8')
    decoded_data = request.strip()
    operation = decoded_data.split()[0]

    if operation == "GET":
        key_to_get = decoded_data.split()[1]
        key_to_get = key_to_get.split("=")[0].strip()
        if key_to_get in cache:
            response_message = '{Proxy Server} : ' + str(cache[key_to_get])
            writer.write(response_message.encode('utf-8'))
            await writer.drain()
        else:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as backend_server_socket:
                backend_server_socket.connect((BACKEND_SERVER_HOST, BACKEND_SERVER_PORT))
                backend_server_socket.sendall(request.encode('utf-8'))
                data = backend_server_socket.recv(1024)
                backend_server_socket.close()
                if data.decode('utf-8') != "No such key found":
                    cache[key_to_get] = data.decode('utf-8')
                response_message = "{Backend Server} : " + data.decode('utf-8')
                writer.write(response_message.encode('utf-8'))
                await writer.drain()
    elif operation == "PUT":
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as backend_server_socket:
            backend_server_socket.connect((BACKEND_SERVER_HOST, BACKEND_SERVER_PORT))
            backend_server_socket.sendall(request.encode('utf-8'))
            data = backend_server_socket.recv(1024)
            backend_server_socket.close()
            response_message = "{Backend Server} : " + data.decode('utf-8')
            writer.write(response_message.encode('utf-8'))
            await writer.drain()
    elif operation == "DUMP":
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as backend_server_socket:
            backend_server_socket.connect((BACKEND_SERVER_HOST, BACKEND_SERVER_PORT))
            backend_server_socket.sendall(request.encode('utf-8'))
            data = backend_server_socket.recv(1024)
            backend_server_socket.close()
            writer.write(data)
            await writer.drain()

    writer.close()
```

#### `client.py`
A simple client program written on sockets, to send request data to the proxy server.
```python
import socket

HOST = "localhost"
PORT = 8080

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect((HOST, PORT))
    s.sendall(b"DUMP")
    data = s.recv(1024)
    s.close()

print(f"Received {data.decode('utf-8')}")
```

### Outputs:
```kotlin
[PUT shashank=softwareEngineer] -> Received {Backend Server} : OK
[GET shashank] -> Received {Backend Server} : softwareEngineer
[GET shashank] -> Received {Proxy Server} : softwareEngineer
```
