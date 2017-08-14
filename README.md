# CS410-Summer2017-Agile-Team1

# Getting Started

## Running from a .JAR
TODO: **Should we have instructions for running from a JAR for the professor?**

## Importing into IntelliJ

First clone branch `gradle` into a directory using the following command
 ```sh
 $ git clone -b gradle https://github.com/eli-cook/CS410-Summer2017-Agile-Team1.git
 ```

Next, to import this project do the following:

* Open IntelliJ
* Press File > New > Project from existing sources
* Choose using an external build system
* Select Gradle, hit Next
* Press finish

# Instructions

## Logging In
When logging in, the user will be prompted for the hostname, port, username, and password.

Note that if a successful connection occurs the user will be asked if they want to save their connection info. If the user chooses to save their info a ```profiles.json``` file will be created which stores the hostname, port number, and username in JSON format. On future uses of the program the user will be asked if they wish to use one of their saved profiles which allows them to only need to enter the password. Also, note that a variable number of profiles can be saved and if the user wishes to manually modify their saved profiles they can edit the ```profiles.json``` file as long as they follow the storage convention.

## Commands
* Logging off: ```logoff```
* Listing files: ```ls```
* Deletion:
    - File ```rm test.txt```
    - Directory: ```rm myDir```
* Change Directory (Local): ```ccd myDir```
* Change Directory (Remote): ```cd myDir```
* Copy Directories on Remote Server: ```cp srcDir destDir```
* Make Directory on Remote Server: ```mkdir myDir```
* Change File or Directory Permissions on Remote Server: ```chmod [groups][modifier][permissions] test.txt```
    - Groups (user,group,owner): u,g,o
    - Modifiers (add,set,remove): +,=,-
    - Permissions (read,write,execute): r,w,x
    - chmod with octal permission settings: ```chmod 777 test.txt```
    - Add multiple permissions: ```chmod ug+r,g+w,g-x,u+x test.txt```
* Resume transfer: ```resume```
