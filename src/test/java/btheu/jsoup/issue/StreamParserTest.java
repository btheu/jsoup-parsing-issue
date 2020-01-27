package btheu.jsoup.issue;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StreamParserTest {

    @Test
    public void testJsoup() throws IOException {

        // https://www.letrot.com/stats/fiche-course/2019-09-22/202/6/partants/tableau

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://www.letrot.com/stats/fiche-course/2019-09-22/202/6/partants/tableau").build();

        Response response = client.newCall(request).execute();

        Document parse = Jsoup.parse(response.body().byteStream(), "UTF-8", "https://www.letrot.com/");

        int numberOfCells = parse.select("td").size();

        System.out.println(numberOfCells);

        // Here: JSoup fails parsing the comment for the second html table row
        // <!-- <td>2</td> -->
        // this can be read at line 687 (more or less) from the raw response page
        System.out.println(parse.select("#result_table tr").get(1).select("td").text());
        System.out.println(parse.select("#result_table tr").get(2).select("td").text());
        System.out.println(parse.select("#result_table tr").get(3).select("td").text());

        assertEquals(156, numberOfCells);
    }

    @Test
    public void testWorkAround() throws IOException {

        // https://www.letrot.com/stats/fiche-course/2019-09-22/202/6/partants/tableau

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://www.letrot.com/stats/fiche-course/2019-09-22/202/6/partants/tableau").build();

        Response response = client.newCall(request).execute();

        // HERE we copy every bytes to force the response stream reading, before JSoup
        // does this by itself
        Document parse = Jsoup.parse(bufferize(response.body().byteStream()), "UTF-8", "https://www.letrot.com/");

        int numberOfCells = parse.select("td").size();

        System.out.println(numberOfCells);
        // Here: The 3 rows are well parsed as it was before JSoup <= 1.11.1
        System.out.println(parse.select("#result_table tr").get(1).select("td").text());
        System.out.println(parse.select("#result_table tr").get(2).select("td").text());
        System.out.println(parse.select("#result_table tr").get(3).select("td").text());

        assertEquals(156, numberOfCells);
    }

    /**
     * Workaround of JSoup stream parsing that fail on very rare byte configuration
     * 
     * @param inputStream
     * @return new InputStream with datas get fetched
     * @throws IOException
     */
    private InputStream bufferize(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] byteArray = buffer.toByteArray();

        return new ByteArrayInputStream(byteArray);
    }
}
