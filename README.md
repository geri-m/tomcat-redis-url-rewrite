# URL Rewriting for Tomcat with Redis

This is the implementation of a [RequestFilterValve](https://tomcat.apache.org/tomcat-8.5-doc/api/org/apache/catalina/valves/RequestFilterValve.html)
to rewrite URLs in Tomcat which are fetched from Redis.

## Local Testing

For local testing you simple can start a redis in a container

```
$ docker run -p 6379:6379 redis
```

## Usage

1) Copy ```reredis.jar``` to $CATALINA_HOME/lib
2) Add in $CATALINA_HOME/conf/Catalina/localhost a file manager.xml with the following content

```
<?xml version="1.0" encoding="UTF-8"?>
<!-- The contents of this file will be loaded for each web application -->
<Context privileged="true" antiResourceLocking="false"
		 docBase="${catalina.home}/webapps/manager">
	<!-- Valve for using Redis for the URL rewriting -->
	<Valve className="at.madlmayr.RedisRewrite" allow="^.*$"  host="localhost" port="6379" timeout="100" />
</Context>
```

## References

We want to implement exactly [this](https://agileweboperations.com/2014/10/13/supporting-millions-of-pretty-url-rewrites-in-nginx-with-lua-and-redis)

[Gist on github](https://gist.github.com/danackerson/af7481a869396839b3da)
