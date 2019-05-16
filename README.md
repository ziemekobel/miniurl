# MiniURL

MiniURL is a simple URL shortener API built with Spring Boot.

## Installation

MiniURL requires:
1. Java 8+
2. Maven 3+

It uses an in-memory H2 database that's wiped with every restart. Initial data is loaded from ``src/main/resources/data.sql``

#### Testing

In order to run tests execute ``mvn test``

```bash
$ mvn test
...
[INFO] Results:
[INFO]
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

#### Running

In order to start the server run ``mvn spring-boot:run``

```bash
$ mvn spring-boot:run
```

To stop it press *Cmd+C* | *Ctrl+C*

## Features

1. Convert a URL to a mini URL of up to 23 characters.

  * Given only the original URL, generate a random mini URL 
```json
$ curl -H "Authorization: Basic amltbXk6c2VjcmV0" -H "Content-Type: application/json" \
     --data '{"url": "http://example.com"}' -X POST http://localhost:5000/
```

```
{
  "url": "http://example.com",
  "miniurl": "http://localhost:5000/1"
}
```
* Given both the original URL and the desired mini URL, create the desired mini URL or give the user an error if that is not possible (ie. it was already taken)
```json
$ curl -H "Authorization: Basic amltbXk6c2VjcmV0" -H "Content-Type: application/json" \
     --data '{"url": "http://example.com", "miniurl": "foobar"}' \
     http://localhost:5000/
```

```     
{
  "url": "http://example.com",
  "miniurl": "http://localhost:5000/foobar"
}
```

```
$ curl -H "Authorization: Basic amltbXk6c2VjcmV0" -H "Content-Type: application/json" \
     --data '{"url": "http://example.com", "miniurl": "foobar"}' \
     http://localhost:5000/
```

```
{
  "error": "Could not create new link. One with the given `miniurl` already exists"
}
```
2. Retrieve the original URL, given a mini URL
```json
$ curl -v -H "Content-Type: application/json" \
     http://localhost:5000/foobar/redirect
```
```html
...
< HTTP/1.1 302
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Location: http://example.com
```

3. Retrieve information about an existing mini URL created by the user, including:

* what is the original URL

* when the minification happened

* how many times the mini URL has been accessed
```json
$ curl -H "Authorization: Basic amltbXk6c2VjcmV0" -H "Content-Type: application/json" \
     http://localhost:5000/foobar
```

```
{
  "accessed": 1,
  "created": "2017-07-11T21:38:46.595948",
  "url": "http://example.com",
  "miniurl": "http://localhost:5000/foobar",
}
```

4. See a list of the URLs he/she created and the information detailed in item 3
```json
$ curl -H "Authorization: Basic amltbXk6c2VjcmV0" -H "Content-Type: application/json" \
     http://localhost:5000/
```

```
[
  {
    "accessed": 0,
    "created": "2017-07-11T21:38:05.784988",
    "url": "http://example.com",
    "miniurl": "http://localhost:5000/1"
  },
  {
    "accessed": 2,
    "created": "2017-07-11T21:38:46.595948",
    "url": "http://example.com",
    "miniurl": "http://localhost:5000/foobar"
  }
]
```

