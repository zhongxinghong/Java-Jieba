package top.rabbit.jieba;

import top.rabbit.jieba.tokenizer.Tokenizer;
import top.rabbit.jieba.viterbi.WordFrequencyViterbi;

import java.util.List;

public class TestMultiCut {

    public static void multi_test_cut(Tokenizer dt, String filename, int N) {
        String s = IOUtils.readFrom(filename);
        dt.initialize();
        WordFrequencyViterbi.initialize();
        List<String> segs;

        System.out.println("Start multi-test cut, N = " + N);
        long st = System.currentTimeMillis();
        long lt = st;
        long ct;
        for (int i = 0; i < N; ++i) {
            System.out.print(String.format("%3d ", i+1));
            segs = dt.cut(s);
            ct = System.currentTimeMillis();
            System.out.println(String.format("cost: %s ms", ct-lt));
            lt = ct;
        }
        long et = System.currentTimeMillis();
        System.out.println(String.format("TOTAL cost %s ms", et-st));
    } /* Output:
    Start multi-test cut, N = 200
      1 cost: 791 ms
      2 cost: 458 ms
      3 cost: 338 ms
      4 cost: 295 ms
      5 cost: 359 ms
      6 cost: 267 ms
      7 cost: 242 ms
      8 cost: 268 ms
      9 cost: 254 ms
     10 cost: 298 ms
     11 cost: 347 ms
     12 cost: 289 ms
     13 cost: 250 ms
     14 cost: 191 ms
     15 cost: 198 ms
      ......
    197 cost: 179 ms
    198 cost: 182 ms
    199 cost: 174 ms
    200 cost: 177 ms
    TOTAL cost 38298 ms
    */

    public static void multi_test_cutForSearch(Tokenizer dt, String filename, int N) {
        String s = IOUtils.readFrom(filename);
        dt.initialize();
        WordFrequencyViterbi.initialize();
        List<String> segs;

        System.out.println("Start multi-test cutForSearch, N = " + N);
        long st = System.currentTimeMillis();
        long lt = st;
        long ct;
        for (int i = 0; i < N; ++i) {
            System.out.print(String.format("%3d ", i+1));
            segs = dt.cutForSearch(s);
            ct = System.currentTimeMillis();
            System.out.println(String.format("cost: %s ms", ct-lt));
            lt = ct;
        }
        long et = System.currentTimeMillis();
        System.out.println(String.format("TOTAL cost %s ms", et-st));
    } /* Output:
    Start multi-test cutForSearch, N = 200
      1 cost: 1041 ms
      2 cost: 713 ms
      3 cost: 491 ms
      4 cost: 425 ms
      5 cost: 534 ms
      6 cost: 441 ms
      7 cost: 444 ms
      8 cost: 420 ms
      9 cost: 337 ms
     10 cost: 388 ms
     11 cost: 415 ms
     12 cost: 376 ms
     13 cost: 342 ms
     14 cost: 343 ms
     15 cost: 408 ms
     16 cost: 366 ms
     17 cost: 316 ms
      ......
    197 cost: 321 ms
    198 cost: 336 ms
    199 cost: 314 ms
    200 cost: 324 ms
    TOTAL cost 69760 ms
    */

    public static void multi_test_cut_noHMM(Tokenizer dt, String filename, int N) {
        String s = IOUtils.readFrom(filename);
        dt.initialize();
        WordFrequencyViterbi.initialize();
        List<String> segs;

        System.out.println("Start multi-test cut_noHMM, N = " + N);
        long st = System.currentTimeMillis();
        long lt = st;
        long ct;
        for (int i = 0; i < N; ++i) {
            System.out.print(String.format("%3d ", i+1));
            segs = dt.cut_noHMM(s);
            ct = System.currentTimeMillis();
            System.out.println(String.format("cost: %s ms", ct-lt));
            lt = ct;
        }
        long et = System.currentTimeMillis();
        System.out.println(String.format("TOTAL cost %s ms", et-st));
    } /* Output:
    Start multi-test cut_noHMM, N = 200
      1 cost: 480 ms
      2 cost: 253 ms
      3 cost: 252 ms
      4 cost: 257 ms
      5 cost: 265 ms
      6 cost: 200 ms
      7 cost: 198 ms
      8 cost: 233 ms
      9 cost: 138 ms
     10 cost: 142 ms
      ......
    196 cost: 125 ms
    197 cost: 113 ms
    198 cost: 111 ms
    199 cost: 110 ms
    200 cost: 110 ms
    TOTAL cost 24418 ms
    */

    public static void multi_test_cutAll(Tokenizer dt, String filename, int N) {
        String s = IOUtils.readFrom(filename);
        dt.initialize();
        WordFrequencyViterbi.initialize();
        List<String> segs;

        System.out.println("Start multi-test cutAll, N = " + N);
        long st = System.currentTimeMillis();
        long lt = st;
        long ct;
        for (int i = 0; i < N; ++i) {
            System.out.print(String.format("%3d ", i+1));
            segs = dt.cutAll(s);
            ct = System.currentTimeMillis();
            System.out.println(String.format("cost: %s ms", ct-lt));
            lt = ct;
        }
        long et = System.currentTimeMillis();
        System.out.println(String.format("TOTAL cost %s ms", et-st));
    } /*
    Start multi-test cutAll, N = 200
      1 cost: 482 ms
      2 cost: 298 ms
      3 cost: 292 ms
      4 cost: 133 ms
      5 cost: 88 ms
      6 cost: 87 ms
      7 cost: 110 ms
      8 cost: 108 ms
      9 cost: 111 ms
     10 cost: 138 ms
      ......
    197 cost: 89 ms
    198 cost: 81 ms
    199 cost: 79 ms
    200 cost: 80 ms
    TOTAL cost 17816 ms
    */

    public static void multi_test_cutForSearch_noHMM(Tokenizer dt, String filename, int N) {
        String s = IOUtils.readFrom(filename);
        dt.initialize();
        WordFrequencyViterbi.initialize();
        List<String> segs;

        System.out.println("Start multi-test cutForSearch_noHMM, N = " + N);
        long st = System.currentTimeMillis();
        long lt = st;
        long ct;
        for (int i = 0; i < N; ++i) {
            System.out.print(String.format("%3d ", i+1));
            segs = dt.cutForSearch_noHMM(s);
            ct = System.currentTimeMillis();
            System.out.println(String.format("cost: %s ms", ct-lt));
            lt = ct;
        }
        long et = System.currentTimeMillis();
        System.out.println(String.format("TOTAL cost %s ms", et-st));
    } /* Output:
    Start multi-test cutForSearch_noHMM, N = 200
      1 cost: 878 ms
      2 cost: 854 ms
      3 cost: 389 ms
      4 cost: 379 ms
      5 cost: 560 ms
      6 cost: 365 ms
      7 cost: 416 ms
      8 cost: 449 ms
      9 cost: 283 ms
     10 cost: 280 ms
      ......
    197 cost: 269 ms
    198 cost: 270 ms
    199 cost: 279 ms
    200 cost: 277 ms
    TOTAL cost 59771 ms
    */
}
