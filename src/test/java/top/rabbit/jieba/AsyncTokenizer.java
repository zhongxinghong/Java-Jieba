package top.rabbit.jieba;

import top.rabbit.jieba.tokenizer.Tokenizer;

import java.util.List;
import java.util.concurrent.Callable;

public final class AsyncTokenizer implements Callable<List> {

    private final Tokenizer dt;
    private String sentence;
    private Task task;
    private boolean assgined = false;
    private enum Task {
        CUT,
        CUT_NO_HMM,
        CUT_ALL,
    }

    public AsyncTokenizer(Tokenizer dt) {
        this.dt = dt;
    }

    private void check() {

        assgined = true;
    }

    public AsyncTokenizer cut(String sentence) {
        check();
        this.sentence = sentence;
        task = Task.CUT;
        return this;
    }

    public AsyncTokenizer cut_noHMM(String sentence) {
        check();
        this.sentence = sentence;
        task = Task.CUT_NO_HMM;
        return this;
    }

    public AsyncTokenizer cutAll(String sentence) {
        check();
        this.sentence = sentence;
        task = Task.CUT_ALL;
        return this;
    }

    @Override
    public List call() throws Exception {
        if (task == null || sentence == null) {
            throw new Exception("No assigned task");
        }
        long st = System.currentTimeMillis();
        List<String> res = null;
        switch (task) {
            case CUT:
                res = dt.cut(sentence);
                break;
            case CUT_NO_HMM:
                res = dt.cut_noHMM(sentence);
                break;
            case CUT_ALL:
                res = dt.cutAll(sentence);
                break;
        }
        long et = System.currentTimeMillis();
        System.out.println(String.format("cost %s ms", et-st));
        return res;
    }
}
