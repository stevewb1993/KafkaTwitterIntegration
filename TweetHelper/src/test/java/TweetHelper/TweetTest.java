package TweetHelper;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.junit.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TweetText {

    @org.junit.jupiter.api.Test
    void formattedDate() throws ParseException {



        //ARRANGE
        JsonParser jsonParser = new JsonParser();

        ////get example tweet and parse into tweet class
        String tweetFileLocation = "src\\test\\java\\TweetHelper\\exampleTweet.txt";
        String tweetString = readLineByLine(tweetFileLocation);
        Tweet testTweet = new Gson().fromJson(jsonParser.parse(tweetString).getAsJsonObject(),Tweet.class);

        String expectedDate = "2020-08-10" ;

        //act
        String actualDate = testTweet.formattedDate();

        //asset
        Assert.assertTrue(actualDate.equals(expectedDate));

    }

    private static String readLineByLine(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}