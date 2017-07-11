package com.chimpler.example.kmeans

import java.io._
import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import twitter4j._

object FetchTweets extends App with LazyLogging {

  logger.debug("Here goes my debug message.")
  val properties = new Properties()
  properties.load(new FileInputStream("twitter-credentials.txt"))

  val apiKey = properties.getProperty("TWITTER_API_KEY").trim
  val apiSecret = properties.getProperty("TWITTER_API_SECRET").trim
  val accessToken = properties.getProperty("TWITTER_ACCESS_TOKEN").trim
  val accessTokenSecret = properties.getProperty("TWITTER_ACCESS_TOKEN_SECRET").trim

  val twitterConfig = new twitter4j.conf.ConfigurationBuilder()
    .setOAuthConsumerKey(apiKey)
    .setOAuthConsumerSecret(apiSecret)
    .setOAuthAccessToken(accessToken)
    .setOAuthAccessTokenSecret(accessTokenSecret)
    .build


  val twitterStream = new TwitterStreamFactory(twitterConfig).getInstance()

  val outputFile = "output1.txt"
  val fileWriter = new FileWriter(outputFile)

  val geoStatusListener = new GeoStatusListener(fileWriter)
  twitterStream.addListener(geoStatusListener)

  //val queryKeywords = args.slice(2, args.length)
  val queryKeywords = "Twitter".trim.split(" ")
  var query = new FilterQuery()

  // with the twitter api, we cannot do filter by both location and keyword
  if (queryKeywords.isEmpty) {
    // cover all the globe so we only get tweets with geolocation information
    val locations = Array(Array(-180d, -90d), Array(180d, 90d))
    query = query.locations(locations)
  } else {
    query = query.track(queryKeywords)
  }

  twitterStream.filter(query)
}

class GeoStatusListener(writer: Writer) extends StatusListener() {
  def onStatus(status: Status) {
    val geoLocation = status.getGeoLocation
    if (geoLocation != null) {
      val text = status.getText.replaceAll("[\r\n]", " ")
      val line = s"${geoLocation.getLongitude},${geoLocation.getLatitude},${status.getCreatedAt.getTime},${status.getUser.getId},$text\n"
      print(line)
      writer.write(line)
      writer.flush()
    }
  }

  def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
  def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
  def onException(ex: Exception) {}
  def onScrubGeo(arg0: Long, arg1: Long) {}
  def onStallWarning(warning: StallWarning) {}
}

