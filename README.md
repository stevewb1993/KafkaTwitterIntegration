# Kafka Twitter Integration

This project streams Covid-19 tweets from twitter for analysis. The analysis currently includes:

- Sentiment analysis using AWS Comprehend of tweets by users with a large number of followers. 
- Word counts for showing how language being used in tweets is changing over time.

## Architecture

The project makes use of standard Kafka architecture. Kafka Connect is used as the source connector for streaming the data from twitter, and as the sink connector for sending the analysed data into Postgres. Kafka Streams is used for the analysis as it is streamed.

Once the data has been persisted in the database, aggregated analysis is completed using Spark.

![Image of architecture](https://github.com/stevewb1993/KafkaTwitterIntegration/blob/master/KafkaTwitterIntegration%20diagram.svg)

## Details of streams applications
### Sentiment Analysis

- A sample of tweets is picked out based on the number of followers of the user and a call is made to the AWS Comprehend Service for Sentiment Analysis. Based on this, two output streams are created.
  - Detail of each tweet and associated sentiment results (KStream)
  - An aggregation by date and the overall sentiment of the tweets (KTable)

### Word Count
- All tweets are analysed and an aggregation is performed to show the number of occurences of every word used each hour of each day. Based on this, people interested in particular terms will be able to see how the frequency of the term is changing over time. Commonly used 'Stop words' are removed in the database layer using the NLTK list. 


## Items to work on

- Dockerfiles are currently built using fat jars. Ideally the build should be completed within docker and the image should be built using multiple layers to avoid such large images
- Addition of a Spark Streaming Application for analysis processed twitter data
