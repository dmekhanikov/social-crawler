# Social Networks Crawler
This application is intended to collect information about social graph of users of various social networks, namely Twitter, Instagram, Facebook and Foursquare. Currently Facebook is not supported. All collected information is saved in MongoDB.

## Build
It is written in Java 8 using Apache Maven build system. To build it you should have them installed on your computer. Run ```mvn clean package``` in the project directory to assemble artifacts. Executable jars will appear in target directories in corresponding modules.

## Configuration
Settings of the crawler are read from the __crawler.properties__ file in the working directory. It should have the following format:
```properties
# Database
mongo.host=
mongo.port=
mongo.database=
mongo.username=
mongo.password=

# Twitter
oauth.consumerKey=
oauth.consumerSecret=
oauth.accessToken=
oauth.accessTokenSecret=

# Foursquare
foursquare.client_id=
foursquare.client_secret=

# Instagram
instagram.access_token=
```

List of users that should be visited are read from an input file that is specified as a command-line argument. If it is not specified, then standard input stream is used. Users should be presented as a list of ids, one in a row.

Run command example:

```java -jar twitter.jar userList.txt```

## Making your own crawler
The core module is designed to let you create your own modules collecting information from your favorite social network. To do that you should only implement the __FriendsService__ interface and mark your class with the __@Target__ annotation. Name of the result collection in MongoDB should be specified as an argument of this annotation. To start crawling run the __main__ method of the __ru.ifmo.ctd.mekhanikov.crawler.Runner__ class. It will find your __FriendsService__ and start the process.
