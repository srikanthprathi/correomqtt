package org.correomqtt.business.utils;

import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.correomqtt.business.utils.VendorConstants.GITHUB_API_LATEST;

public class VersionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionUtils.class);

    private static String version;

    private VersionUtils() {
        // private constructor
    }

    public static String getVersion() {
        if (version == null) {
            try {
                version = IOUtils.toString(VersionUtils.class.getResourceAsStream("version.txt"), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("Error reading version: ", e);
                version = "N/A";
            }
        }

        return version;
    }

    public static Pair<Boolean, String> isNewerVersionAvailable() throws IOException, ParseException {

        URL url = new URL(GITHUB_API_LATEST);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", "utf-8");
        try {
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject)jsonParser.parse(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            ComparableVersion latestGithubVersion = new ComparableVersion(jsonObject.get("tag_name").toString());
            ComparableVersion currentLocalVersion = new ComparableVersion(getVersion());

            if (latestGithubVersion.compareTo(currentLocalVersion) == 1) {
                LOGGER.info("There is a new release available on github!");
                return new Pair(true, jsonObject.get("tag_name"));
            } else {
                LOGGER.info("Version is up to date or newer!");
                return new Pair(false, null);
            }
        } catch (UnknownHostException uhe) {
            LOGGER.error("No internet connection for checking latest version");
            return new Pair(false, null);
        }
    }
}