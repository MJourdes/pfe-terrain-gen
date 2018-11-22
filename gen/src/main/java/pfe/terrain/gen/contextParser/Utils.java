package pfe.terrain.gen.contextParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    public static String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;

        br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line /*+ System.getProperty("line.separator")*/);
        }
        br.close();

        return sb.toString();
    }
}
