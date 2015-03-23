package goeuro;

import com.google.gson.*;
import goeuro.util.HttpUtil;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main {

    @Option(name = "-o", usage = "output to this file")
    private String fileName = "";

    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>();

    private static final String ApiBaseUrl = "http://api.goeuro.com/api/v2/position/suggest/en/";

    public static void main(String[] args) {
        new Main().doMain(args);
    }

    public void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.out.println("usage: java -jar GoEuropeTest.jar \"CITY_NAME\" [-o <path>]");
            return;
        }
        if (arguments.isEmpty()) {
            System.out.println("usage: java -jar GoEuropeTest.jar \"CITY_NAME\" [-o <path>]");
            return;
        }

        String cityName = arguments.get(0);
        if (fileName.equals("")) {
            fileName = cityName + ".csv";
        }

        String data = "[]";
        try {
            data = HttpUtil.get(ApiBaseUrl + cityName);
        } catch (Exception e) {
            //System.err.println(e.getMessage());
        }

        JsonParser jsonParser = new JsonParser();
        JsonArray cityArray = jsonParser.parse(data).getAsJsonArray();

        if (cityArray.size() > 0) {
            writeDataToFile(fileName, cityArray);
        }else{
            System.out.println("No data was found for \"" + cityName+"\"");
        }
    }

    private static void writeDataToFile(String fileName, JsonArray cityArray) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (Exception e) {
            System.out.println("Couldn't write to file: " + fileName);
            System.err.println(e.getMessage());
            return;
        }
        for (JsonElement cityData : cityArray) {
            JsonObject cityJson = cityData.getAsJsonObject();
            JsonObject geoPosition = cityJson.getAsJsonObject("geo_position");
            String csvEntry = cityJson.get("_id").toString();
            csvEntry += "," + cityJson.get("name");
            csvEntry += "," + cityJson.get("type");
            csvEntry += "," + geoPosition.get("latitude");
            csvEntry += "," + geoPosition.get("longitude");
            writer.println(csvEntry);
        }
        writer.close();
        System.out.println(cityArray.size() + " lines were written to " + fileName);
    }
}
