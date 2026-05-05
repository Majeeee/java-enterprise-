package se.scb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class ScbApiClient {

    private static final Logger log = LoggerFactory.getLogger(ScbApiClient.class);

    private static final String SCB_URL =
            "https://api.scb.se/OV0104/v1/doris/en/ssd/BE/BE0101/BE0101J/Flyttningar97";

    private static final String COUNTY_CODES = """
            "01","03","04","05","06","07","08","09","10","12","13","14",
            "17","18","19","20","21","22","23","24","25"
            """;

    private static final String YEARS = """
            "1997","1998","1999","2000","2001","2002","2003","2004",
            "2005","2006","2007","2008","2009","2010","2011","2012",
            "2013","2014","2015","2016","2017","2018","2019","2020",
            "2021","2022","2023","2024"
            """;

    private final HttpClient httpClient;

    public ScbApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String fetchMigrationData(String ageValuesJson) throws IOException, InterruptedException {
        log.info("Skickar forfragan till SCB API: {}", SCB_URL);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SCB_URL))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(buildQueryBody(ageValuesJson)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("SCB API svarade med statuskod " + response.statusCode());
        }

        log.info("SCB API svarade med {} tecken JSON", response.body().length());
        return response.body();
    }

    private String buildQueryBody(String ageValuesJson) {
        return """
                {
                  "query": [
                    {
                      "code": "Region",
                      "selection": {
                        "filter": "item",
                        "values": [%s]
                      }
                    },
                    {
                      "code": "Alder",
                      "selection": {
                        "filter": "item",
                        "values": [%s]
                      }
                    },
                    {
                      "code": "Kon",
                      "selection": {
                        "filter": "item",
                        "values": ["1", "2"]
                      }
                    },
                    {
                      "code": "ContentsCode",
                      "selection": {
                        "filter": "item",
                        "values": ["BE0101AX", "BE0101AY"]
                      }
                    },
                    {
                      "code": "Tid",
                      "selection": {
                        "filter": "item",
                        "values": [%s]
                      }
                    }
                  ],
                  "response": {
                    "format": "json"
                  }
                }
                """.formatted(COUNTY_CODES, ageValuesJson, YEARS);
    }
}
