# Kafka Twitter Integration

This project streams Covid-19 tweets from twitter for analysis. The analysis currently includes:

- Sentiment and entities analysis using AWS Comprehend of tweets by users with a large number of followers. 
- Word counts for showing how language being used in tweets is changing over time.

## Architecture

The project makes use of standard Kafka architecture. Kafka Connect is used as the source connector for streaming the data from twitter, and as the sink connector for sending the analysed data into Postgres. Kafka Streams is used for the analysis as it is streamed.

Once the data has been persisted in the database, aggregated analysis is completed using Spark.

![Image of architecture](https://github.com/stevewb1993/KafkaTwitterIntegration/blob/master/KafkaTwitterIntegrationDiagram.svg)

## Details of streams applications
### Sentiment Analysis

- For users who tweet about Covid-19 that have at least 1000 followers, the application selects a random sample to analyse using AWS Comprehend (only a sample is analysed to stay within AWS free tier). Based on this, three output streams are created.
  - Detail of each tweet and associated sentiment results (KStream), as well as an aggregation by date and the overall sentiment of the tweets (KTable). This facilitates:
    - Showing how overall sentiment is trending
    - Analysis of whether particular characteristics of a user (number of followers, location, etc) are associated with positive or negative sentiment
    ![Sentiment Over Time:](https://github.com/stevewb1993/KafkaTwitterIntegration/blob/master/sentimentOverTime.png)
  
  - The above, but also joined with all the entities identified within the tweet (KStream)
    - This facilitates showing how sentiment is trending for particular entities, such as high profile individuals or organisations
    
    Analysis of entities identified as people in tweets and the associated sentiment of the tweet:
    ![Analysis of entities identified as people in tweets and the associated sentiment of the tweet:](https://github.com/stevewb1993/KafkaTwitterIntegration/blob/master/entitySentiment.png)

### Word Count
- All tweets are analysed and an aggregation is performed to show the number of occurences of every word used each hour of each day. Based on this, people interested in particular terms will be able to see how the frequency of the term is changing over time. Commonly used 'Stop words' are removed in the database layer using the NLTK list. 
![Total Word Counts:](https://github.com/stevewb1993/KafkaTwitterIntegration/blob/master/wordCount.png)