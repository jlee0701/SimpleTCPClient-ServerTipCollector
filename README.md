## Description
 
This program can add tips for a class to the server and display the ones that were successfully added.
A tip will be taken in from the client as a String type and send it to the server, then the nodes will 
vote on either commit or not commit the add request. The vote decisions will be generated at random by the two
nodes(therefore it can take many add requests to successfully add a tip). A tip will be added when 
both node1 and node2 vote agree to add, otherwise the tip will not be added.

Two Phase Commit structure: The above structure utilized Two Phase commit with the TCServer sending add request to the nodes, and 
the nodes return vote decision back to the TCServer first, then the TCServer checks the votes returned to 
see if both nodes allow the add request. If any one of the nodes doesn't allow the add request, the server will
signal both nodes to abort the add, otherwise it will signal both nodes to add the tip to their lists.

The server will also be able to handle multiple clients at the same time, so all of them can make requests
to the server at the same time, and they will be sharing the same tip pool.

Client menu:

    1. add <string> - adds a tip to the list and display the lists from 2 nodes 
    3. display - display the tip list
    0. quit
    

## Protocol

### Requests

Client to TCServer: 

request: { "service": <String: 1=add, 2=display, 0=quit>, "data": <String: tip to send> }

  add: data <string>
  display: no data
  quit: no data
  
TCServer to nodes: 

request: { "request": <String: request to node>, "status": <String: vote status>, "message": <String: server message>
, "data": <String: tip to send> }  

### Responses

Nodes to TCServer: 

success response: {"response": <String: node response type>, "value": <String/int: response value> }

error response: {"error": <String: error message> }

TCServer to client: 

success response: {"response": <String: server response type>, "message": <String: server message>
, "data_n1": <String: node1 list>, "data_n2": <String: node2 list>}

error response: {"error": <String: error message> }

## Error handling

The program avoids crashes with input not match and null pointer handlers for improper inputs
, as well as socket, IO and EOF exception handlers to handle connection failures

## How to run the program
### Terminal
For gradle, please use the following commands:

```
    For the 2 nodes, run "gradle node1" for Node 1 and "gradle node2" for Node 2
    For your information, Node 1 and 2 runs on port 7000 and 7001 respectively
```
```
    For TCServer, run "gradle TC -Pport=<port> -q --console=plain", port is the port 
    number you want to use. Default port is '8000' if not specified
    
```
```   
    For Client, run "gradle runClient -Pport=<port> -Phost=<host> -q --console=plain"
    port host are the port number and the host you want to use. Default host and port 
    are 'localhost' and '8000' if not specified
```   
 
