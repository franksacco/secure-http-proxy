# Secure HTTP Proxy
The objective of this project is to create a system for protecting HTTP communication between a client (e.g. a browser)
and an HTTP server (e.g. web server), also in case TLS (HTTPS) is not used. The system should consist of two
intermediate entities P1 and P2 that are used to protect the communication between the HTTP client and server. The
client is configured to use P1 as standard HTTP proxy, while P2 acts as HTTP client toward the remote server. All HTTP
messages exchanged between the client and server are encrypted and (optionally) integrity protected between P1 and P2.

## Description
In order to protect all communication between a client and one (or  possible any) remote server(s), HTTP traffic passes
through a secure tunnel established between an outbound HTTP proxy P1 and an exit node P2. When the client wants to
send a HTTP request to a server, it sends the request to P1 acting as HTTP proxy. Instead of relaying the request
directly to the server, P1 encrypts the message and send it to the node P2. The node P2 then extract the message and
sends it to the server as it were the actual client. The response messages are sent back accordingly in the opposite
way, encrypted between P2 and P1.

The system acts like a sort of "anonymous proxy" or "application level VPN".

The traffic between P1 and P2 can be protected on a per-message basis (each HTTP message is separately
encrypted/decrypted), or on a per-flow basis (encryption is applied directly to the TCP streams).

A possible implementation of the exit node P2 may consist of two sub-entities: one node P2' acting as relay node
between P1 and P2'', and the second node P2'' that is a standard HTTP proxy.

In a simplified deployment, in case of only one remote server is considered, the node P'' could be omitted by
configuring P2' to communicate directly with the given remote server.

## Authors
 - [Matteo Rinaldini](https://github.com/matterina) <matteo.rinaldini@studenti.unipr.it>
 - [Francesco Saccani](https://github.com/franksacco/) <francesco.saccani2@studenti.unipr.it>
