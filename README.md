# Kafka Twitter Integration

This project streams Covid-19 tweets from twitter for analysis. The analysis currently includes:

- Sentiment analysis using AWS Comprehend of tweets by users with a large number of followers. 
- Word counts for showing how language being used in tweets is changing over time.

## Architecture

The project makes use of standard Kafka architecture. Kafka Connect is used as the source connector for streaming the data from twitter, and as the sink connector for sending the analysed data into Postgres. Kafka Streams is used for the analysis.


![Image of architecture](https://github.com/stevewb1993/KafkaTwitterIntegration/blob/master/KafkaTwitterIntegration%20diagram.svg)
