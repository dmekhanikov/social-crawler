# Social Networks Crawler
This application is intended to collect information about social graph of users of various social networks, namely Twitter, Instagram, Facebook and Foursquare. Currently only Twitter and Foursquare are supported. All collected information is saved in MongoDB.

## Build
It is written in Java 8 using Apache Maven build system. To build it you should have them installed on your computer. Run ```mvn clean package``` in the project directory to assemble artifacts. Executable jars will appear in target directories in corresponding modules.

## Configuration
The crawler has parameters that are specified in two ways: as system properties and in a property file.

List of users that should be visited are read from an input file that is specified with __input__ system property. If this property is not specified, then __input.txt__ is used as an input file. Users are presented as a list of ids, one in a row.

The rest properties are read from the __crawler.properties__ file in the working directory. It should have the following format:
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
```

Run command example:

```java -Dinput=userList.txt -jar twitter.jar```
