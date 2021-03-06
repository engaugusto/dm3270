# dm3270
Yet another TN3270 Terminal Emulator. This one is written in Java 8, and uses JavaFX.
#### Warning
This software is not finished. At the moment it is only possible to run tests, you cannot connect it to a mainframe. You can however use it to watch an actual terminal-mainframe session in order to capture session details.
#### Modes
There are four ways in which this software can currently be used - Spy, Replay, Mainframe and Terminal. These are described below.                
![Initial screen](main1.png?raw=true "initial screen")
#### Spy Mode
Waits for a terminal emulator (not this one) to connect to the client port specified, and then it completes the connection to the actual mainframe at the server and port specified. The mainframe session can then be used as normal, however all communication buffers are recorded and can be saved in a session file to replay later.
![Spy screen](spy.png?raw=true "spy screen")
#### Replay Mode
Uses the dm3270 terminal to replay the commands that were previously saved in a session file. Individual buffers can be selected and examined. The anticipated reply message (if any) is also displayed. Each command is sent to the terminal for processing.
![Replay screen](replay.png?raw=true "replay screen")
#### Mainframe Mode
Waits for a terminal emulator (not this one) to connect to the client port specifed. This program is now serving as a mainframe, and the mainframe window can be used to send the terminal various 3270 commands. Any replies from the terminal are displayed. The session can also be saved for later replaying.
![Mainframe screen](mainframe.png?raw=true "mainframe screen")
#### Terminal Mode
Connects the dm3270 terminal to the test mainframe described above.
![Terminal screen](console.png?raw=true "dm3270")
