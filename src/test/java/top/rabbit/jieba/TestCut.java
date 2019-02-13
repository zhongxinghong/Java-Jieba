package top.rabbit.jieba;

import top.rabbit.jieba.struct.LocatedWord;
import top.rabbit.jieba.tokenizer.Tokenizer;

import java.util.List;

public class TestCut {

    public static void test_cut(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<String> segs = dt.cut(s);
        filename += IOUtils.CUT_SUFFIX + IOUtils.HMM_SUFFIX;
        IOUtils.writeToJSON(filename, segs);
    }

    public static void test_cutAll(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<String> segs = dt.cutAll(s);
        filename += IOUtils.CUTALL_SUFFIX + IOUtils.HMM_SUFFIX;
        IOUtils.writeToJSON(filename, segs);
    }

    public static void test_cutForSearch(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<String> segs = dt.cutForSearch(s);
        filename += IOUtils.CUT_FOR_SEARCH_SUFFIX + IOUtils.HMM_SUFFIX;
        IOUtils.writeToJSON(filename, segs);
    }

    public static void test_cut_noHMM(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<String> segs = dt.cut_noHMM(s);
        filename += IOUtils.CUT_SUFFIX + IOUtils.NO_HMM_SUFFIX;
        IOUtils.writeToJSON(filename, segs);
    }

    public static void test_cutForSearch_noHMM(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<String> segs = dt.cutForSearch_noHMM(s);
        filename += IOUtils.CUT_FOR_SEARCH_SUFFIX + IOUtils.NO_HMM_SUFFIX;
        IOUtils.writeToJSON(filename, segs);
    }

    public static void test_cutWithIndex(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<LocatedWord> segs = dt.cutWithIndex(s);
        //for (LocatedWord w : segs)
        //    System.out.println(w);
        System.out.println(s.length()); // 可校验长度一致
        System.out.println(segs.stream().mapToInt(x->x.word.length()).sum());
    }

    public static void test_cutWithIndex_noHMM(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<LocatedWord> segs = dt.cutWithIndex_noHMM(s);
        //for (LocatedWord w : segs)
        //    System.out.println(w);
        System.out.println(s.length()); // 可校验长度一致
        System.out.println(segs.stream().mapToInt(x->x.word.length()).sum());
    }

    public static void test_cutForSearchWithIndex(Tokenizer dt, String filename) {
        String s = IOUtils.readFrom(filename);
        List<LocatedWord> segs = dt.cutForSearchWithIndex_noHMM(s);
        //for (LocatedWord w : segs)
        //    System.out.println(w);
    }
}
