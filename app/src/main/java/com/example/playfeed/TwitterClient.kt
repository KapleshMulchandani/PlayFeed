import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException

object TwitterClient {

    // Replace these values with your actual Twitter keys
    private const val CONSUMER_KEY = "ULyvRZRSuA5xpFoRDemsnfC4I"
    private const val CONSUMER_SECRET = "gGVesbFaxSc29QQN73NJ7O6Lkp0auqDqNzEi0YDVmpfpzK83TJ"
    private const val ACCESS_TOKEN = "797517779519864832-LT4s5s34AEW7XsMiOagRHZ5vA9pFOtp"
    private const val ACCESS_TOKEN_SECRET = "nULgLtbhB1u3XcrmCWzvheuQ0pDrRIBLu0Eox9I57PpOi"

    // Initialize Twitter instance with OAuth credentials
    fun getTwitterInstance(): Twitter {
        val config = ConfigurationBuilder()
            .setOAuthConsumerKey(CONSUMER_KEY)
            .setOAuthConsumerSecret(CONSUMER_SECRET)
            .setOAuthAccessToken(ACCESS_TOKEN)
            .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET)
            .build()

        val twitterFactory = TwitterFactory(config)
        return twitterFactory.instance
    }

    // Fetch latest tweets from a specific user's timeline (excluding replies and retweets)
    @Throws(TwitterException::class)
    fun getTweetsFromUser(username: String, tweetCount: Int): List<Status> {
        val twitter = getTwitterInstance()
        val paging = Paging(1, tweetCount) // Get the latest 'tweetCount' tweets

        // Fetch the user timeline and exclude retweets and replies
        val userTimeline = twitter.getUserTimeline(username, paging)

        // Filter out retweets and replies manually
        return userTimeline.filter { status ->
            !status.isRetweet && status.inReplyToStatusId == -1L
        }
    }
}