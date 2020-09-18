// Databricks notebook source
//imports
import org.apache.spark.sql.functions._

// COMMAND ----------

//connection details
val jdbcUsername = ""
val jdbcPassword = ""
val jdbcHostname = ""
val jdbcPort = 5432
val jdbcDatabase = ""

// COMMAND ----------

//Create the connection string from connection details
val jdbcUrl = s"jdbc:postgresql://${jdbcHostname}:${jdbcPort}/${jdbcDatabase}"

// Create a Properties() object to hold the parameters.
import java.util.Properties
val connectionProperties = new Properties()

connectionProperties.put("user", s"${jdbcUsername}")
connectionProperties.put("password", s"${jdbcPassword}")

// COMMAND ----------

//get data from the database
val query = "(select * from public.vw_twitter_sentiment_with_entities) as TwitterSentimentWithEntities"
val twitterEntitiesDF = spark.read.jdbc(jdbcUrl, query, connectionProperties)


// COMMAND ----------

//add overall date to help with aggregation
val twitterEntitiesWithDateDF = twitterEntitiesDF.withColumn("tweetDate",twitterEntitiesDF.col("tweettimestamp").cast("date"))

// COMMAND ----------

val twitterEntitiesAggDF = twitterEntitiesWithDateDF
  .filter($"type" === "PERSON")
  .groupBy("entity","overallSentiment","type","tweetDate")
  .count()
  .orderBy(asc("tweetDate"),asc("type"),asc("entity"))

// COMMAND ----------

display(twitterEntitiesAggDF)

// COMMAND ----------


