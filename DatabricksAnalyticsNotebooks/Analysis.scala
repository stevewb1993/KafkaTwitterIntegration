// Databricks notebook source
//imports
import org.apache.spark.sql.functions._
import java.util.Properties
import org.apache.spark.sql.expressions.Window

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

//function for cleaning entities
val cleanEntities = (entity: String) => {
	val cleanEntity = {
		//change to lower case
		val lowerEntity: String = entity.toLowerCase();
		//check if there's a leading hashtag. if true, remove it
		if (lowerEntity(0) =='#')
			lowerEntity.reverse.substring(1).reverse
		else
			lowerEntity
	}
	cleanEntity
}
val cleanEntitiesUDF = spark.udf.register("cleanEntities",cleanEntities)

//Perform some cleaning on the data to help with analysis
val twitterEntitiesDFCleaned = twitterEntitiesDF
	//create a column with just the date (rather than timestamp) to help with aggregation use cases
	.withColumn("tweetDate",twitterEntitiesDF.col("tweettimestamp").cast("date"))
	//clean the identified entities
	.withColumn("entityCleaned",cleanEntitiesUDF(col("entity")))


//Analysis of entities identied as People and the sentiment towards them
val twitterEntitiesAggDF = twitterEntitiesDFCleaned
	//filter for people
	.filter($"type" === "PERSON")
	//aggregate sentiment for each individual
	.groupBy("entityCleaned","overallSentiment","type","tweetDate")
	.agg(count("overallSentiment").alias("entitySentimentCount"))
	//window aggregation used for sorting the diagram for entities with most analysis overall
	.withColumn("totalEntityCount", sum("entitySentimentCount") over Window.partitionBy("entityCleaned"))

display(twitterEntitiesAggDF.orderBy(desc("totalEntityCount")).limit(40))

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


