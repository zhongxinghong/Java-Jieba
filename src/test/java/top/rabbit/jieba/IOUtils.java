package top.rabbit.jieba;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

final class IOUtils {

    public static final String TEST_FILE_1 = "围城.txt";

    public static final String OUTPUT_DIR = "output";
    public static final String INPUT_DIR = "input";

    public static final String jAVA_SUFFIX = ".java.json";
    public static final String CUT_SUFFIX = ".cut";
    public static final String CUTALL_SUFFIX = ".cutall";
    public static final String CUT_FOR_SEARCH_SUFFIX = ".cutforsearch";
    public static final String HMM_SUFFIX = ".HMM";
    public static final String NO_HMM_SUFFIX = ".notHMM";

    public static boolean writeTo(String filename, Object data) {
        try {
            Path p = Paths.get(OUTPUT_DIR, filename);
            FileWriter fout = new FileWriter(p.toString());
            fout.write(data.toString());
            fout.close();
            System.out.println(String.format("Write to '%s'", p.toAbsolutePath()));
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static String readFrom(String filename) {
        try {
            int bufSize = 1024 * 8;
            Path p = Paths.get(INPUT_DIR, filename);
            BufferedReader fin = new BufferedReader(
                    new InputStreamReader(new FileInputStream(p.toString())));
            char[] buf = new char[bufSize];
            StringBuilder sb = new StringBuilder();
            int len = -1;
            while ((len = fin.read(buf)) != -1) {
                if (len == bufSize) {
                    sb.append(buf);
                } else {
                    sb.append(Arrays.copyOfRange(buf, 0, len));
                    break;
                }
            }
            fin.close();
            System.out.println(String.format("Read from '%s'", p.toAbsolutePath()));
            return sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean writeToJSON(String filename, Object data) {
        return writeTo(filename + jAVA_SUFFIX, JSON.toJSON(data));
    }
}
