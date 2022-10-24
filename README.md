# pb-assignment

Write an application providing an API endpoint for fetching the percentage of each language represented in Producboard’s GitHub organization public repositories. The application should fetch the data daily and persist them. The time when the data gets fetched is arbitrary, and you may use whichever persistence storage. 

GitHub provides REST and GraphQL APIs which are both well documented. You can choose what suits you the most to gather whatever data necessary.

*You shouldn’t need to use any authorization token to access the required information for the task if you choose REST API (just be aware of the rate limit for unauthenticated requests).*

*If you choose GraphQL API you’ll have to create and use access token to be able to query required information.*

The response should have the following format in JSON:

```json
{
  "Ruby": 0.5,
	"TypeScript": 0.2,
  "Python": 0.3
}
```

Code can be in Kotlin or Java and you can use build tool of your choice. We encourage you to use Spring framework.

You should provide tests and instructions on how to run your application. The deliverable should be a repository on GitHub containing source code for the application. 

You can find Productboard’s GitHub organisation here [https://github.com/productboard](https://github.com/productboard).

## How to Run It Locally

Launch the app:

    ./mvnw mn:run

* Languages statistics: http://localhost:9090/api/v1/repositories/languages
* Loading status (for debugging): http://localhost:9090/api/v1/repositories/_status
* To change logging, update `application.yml`


## Implementation Details

* App utilizes Micronaut framework.
* By design this app gives no guaranties on data consistency. Also, data is kept in memory, so it cannot survive app restart.
* It is intended to run as single instance. If multiple instances are deployed, there is no instance synchronization, and results can be biased more than single instance (due to sharing the rate limit among instances).
* Deleting of repositories is covered.
* Implementation is far from optimal, it is just PoC ;-)

### Obstacles

Main obstacle to get consistent data is rate limit of the GitHub API. Rate limit is known to reset after 1 hr. according to the API documentation for anonymous access. There is a way to get higher rate limit by authenticated access, but it just "postpones" the problem.
So this app implements proof of concept of "response error driven" approach to manage rate limits - to keep it simple, rate limit headers are ignored, and calling GitHub API is paused (for predefined time which is configurable in the properties) once HTTP 403 error response is received, and `x-ratelimit-remaining` header is zero:

```
HTTP/2 403
Date: Tue, 20 Aug 2013 14:50:41 GMT
x-ratelimit-limit: 60
x-ratelimit-remaining: 0
x-ratelimit-used: 60
x-ratelimit-reset: 1377013266

{
   "message": "API rate limit exceeded for xxx.xxx.xxx.xxx. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)",
   "documentation_url": "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
}
```

Also, GitHub API doesn't provide reasonable paging support for our use case. So we read the pages until empty result is received.

### Next Steps

* Better junit tests coverage
* Integration tests
* Fix repositories loading, check `LoadingContext#repositoriesPageToLoadMaybe()`
* Consider higher scale in the response values - if there are languages with low ratio, values are `0.00` (it happens for *productboard* organization)
* Expose via. API information about "stale" values


## Resources

* https://docs.github.com/en/rest/repos
* https://docs.micronaut.io/latest/guide
