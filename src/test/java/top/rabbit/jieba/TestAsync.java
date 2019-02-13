package top.rabbit.jieba;

import top.rabbit.jieba.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class TestAsync {

    private static final int MS_INTERVAL = 500;
    private static final int THREAD_NUMS = 2;
    private static final int REPEAT_COUNT = 200;

    public static void asnyc_cut() throws InterruptedException, ExecutionException {
        String s = IOUtils.readFrom(IOUtils.TEST_FILE_1);
        LinkedList<Tokenizer> dts = new LinkedList<Tokenizer>();
        for (int i = 0; i < THREAD_NUMS; ++i) {
            Tokenizer dt = new Tokenizer();
            dt.initialize();
            dts.push(dt);
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(THREAD_NUMS);
        List<Future<List>> resList = new ArrayList<Future<List>>();

        long st = System.currentTimeMillis();

        for (int i = 0; i < REPEAT_COUNT; ++i) {
            Tokenizer dt = dts.pollLast();
            Future<List> res = executor.submit(new AsyncTokenizer(dt).cutAll(s));
            resList.add(res);
            dts.push(dt);
        }

        //while (executor.getCompletedTaskCount() < resList.size()) {
        //    TimeUnit.MILLISECONDS.sleep(MS_INTERVAL);
        //}

        for (Future<List> res : resList) {
            System.out.println(res.get().size());
        }

        long et = System.currentTimeMillis();
        System.out.println(String.format("cost %s ms", et-st));

        executor.shutdown();
    }
}
