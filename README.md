## basically, i imported some new java packages, you need to clean the gradle cache,
## and then rebuild gradle to import the javax.mail package！
## for macOS command

```sh
./gradlew build --refresh-dependencies
```
```sh
./gradlew clean build
```

## there are many requests in the Client.java one by one, you only have to run one of them to test function!!

## Running the programs

First we need to start the server by running the following from a terminal in the server folder:

```sh
gradle run
```

After installing the required libraries, it will say `> :app:run`, which means you are ready to start the client by running
the following from a terminal in the root of this java folder:

```sh
java Client.py
```

The client goes through and tests each of the example endpoints of the server.

