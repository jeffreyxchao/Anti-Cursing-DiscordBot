# Anti-Cursing Bot
A simple discord bot I made with JDA to help moderate foul language within two large servers that I'm in. 

## General Function
Everytime a message is sent in the server (does not matter what channel), the bot will take the message and compare it to a database of "banned words", checking if the message contains such a word. 

- If so, the sender will be timed out for one hour, and the owner of the server will receive a private message from the bot. This message will include the sender and the message itself.
- The bot will then save the sender and message into a separate table as a record that the owner of the server can access.

# Commands
**only owner can use commands

### addword
*add a word or phrase to be banned in the server* 

### removeword
*remove a banned word or phrase from the server*

### records
*allows owner to see all previous history of the desired user using innapropiate language in he server*

### showbannedwords
*displays all words "banned" in the server* 
