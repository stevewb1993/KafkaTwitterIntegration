// Databricks notebook source
//imports
import org.apache.spark.sql.functions._
import java.util.Properties

// COMMAND ----------

//connection details
val jdbcUsername =
val jdbcPassword =
val jdbcHostname =
val jdbcPort = 5432
val jdbcDatabase =

// COMMAND ----------

//Create the connection string from connection details
val jdbcUrl = s"jdbc:postgresql://${jdbcHostname}:${jdbcPort}/${jdbcDatabase}"
// Create a Properties() object to hold the parameters.
val connectionProperties = new Properties()
connectionProperties.put("user", s"${jdbcUsername}")
connectionProperties.put("password", s"${jdbcPassword}")

// COMMAND ----------

// MAGIC %md ##Entities Analysis
// MAGIC The aim of tis analysis is to show which individuals are mentioned the most, and what the sentiment is towards them

// COMMAND ----------

//get data from the database
val query = "(select * from public.vw_twitter_sentiment_with_entities) as TwitterSentimentWithEntities"
val twitterEntitiesDF = spark.read.jdbc(jdbcUrl, query, connectionProperties)
//add overall date to help with aggregation
val twitterEntitiesWithDateDF = twitterEntitiesDF.withColumn("tweetDate",twitterEntitiesDF.col("tweettimestamp").cast("date"))

//Analysis of entities identied as People and the sentiment towards them
val twitterEntitiesAggDF = twitterEntitiesWithDateDF
	//filter for people
	.filter($"type" === "PERSON")
	//remove neutral sentiment to make results clearer
	.filter($"overallSentiment" =!= "NEUTRAL")
	//aggregate
	.groupBy("entity","overallSentiment","type","tweetDate")
	.count()

display(twitterEntitiesAggDF.orderBy(asc("count")))

// COMMAND ----------

// MAGIC %md ## Analysis of overall sentiment
// MAGIC The aim of this analysis is to show how sentiment is changing over time. We do this by averaging the sentiment scores of tweets over a rolling window

// COMMAND ----------

//get data from the database
val sentimentQuery = "(select * from public.vw_twitter_sentiment_detail) as TwitterSentiment"
val twitterSentimentDF = spark.read.jdbc(jdbcUrl, query, connectionProperties)

//cast the timestamp column as long to help with rolling window analysis
val twitterSentimentDFWithLongTimeStamp = twitterSentimentDF.withColumn("timestampLong", twitterSentimentDF.col("tweettimestamp").cast("long"))
val w = Window.orderBy("timestampLong").rangeBetween(-60*60,0) //the previous hour

//show the averages of the sentiment scores over the rolling window
val twitterRollingSentimentDF = twitterSentimentDFWithLongTimeStamp
	.withColumn("averagePositive",mean("Positive").over(w))
	.withColumn("averageNegative",mean("Negative").over(w))
	.withColumn("averageMixed",mean("Mixed").over(w))
	.withColumn("averageNeutral",mean("Neutral").over(w))
display(twitterRollingSentimentDF)

// COMMAND ----------

// MAGIC %md ## Analysis of word count
// MAGIC This analysis shows the most used words each day

// COMMAND ----------

//get data from the database
val wordCountQuery = "(select * from public.vw_twitter_word_count) as WordCount"
val wordCountDF = spark.read.jdbc(jdbcUrl, wordCountQuery, connectionProperties)

//aggregate word count over all days
val wordCountAggDF = wordCountDF
	//aggregate
	.groupBy("word")
	.agg(sum("wordcount").alias("overallWordCount"))

display(wordCountAggDF.orderBy(desc("overallWordCount")).limit(20))

// COMMAND ----------


