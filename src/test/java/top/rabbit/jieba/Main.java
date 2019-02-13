package top.rabbit.jieba;

public class Main {
/*
    private static final String PROJECT_VERSION = "1.0.0-SNAPSHOT";
    private static final String JIEBA_VERSION = "0.39";

    private static final void printHelpInfo(Options options) {
        final String syntax = "java-jieba";
        final String header =  String.format(
                "\nJava-Jieba is a copy of Jieba %s supporting ALL original features.\n\n", JIEBA_VERSION);
        final String footer = "\nI'm just a footer.\n";
        HelpFormatter fmt = new HelpFormatter();
        fmt.printHelp(syntax, header, options, footer, true);
    }

    private static final void printVersionInfo() {
        System.out.println(String.format("Java-Jieba %s", PROJECT_VERSION));
    }

    private static final boolean checkFileExists(String filename) {
        return false;
    }

    public static void main(String[] args) {

        Options options = new Options();

        Option help = new Option("h","help",false,"print this usage information and exit");
        Option version = new Option("v", "version", false, "show version number and exit");

        Option in = Option.builder("i").longOpt("if").hasArg().argName("file")
                        .desc("Input file *.txt\nIf no filename specified, use -s instead").build();
        Option out = Option.builder("o").longOpt("of").hasArg().argName("file")
                        .desc("Output file *.txt\nIf no filename specified, use STDOUT instead").build();
        Option str = Option.builder("s").longOpt("str").hasArg().argName("string")
                        .desc("Input string with \"quotation marks\"").build();

        Option pos = Option.builder("p").longOpt("pos")
                        .desc("Enable POS tagging").build();
        Option all = Option.builder("a").longOpt("cut-all")
                        .desc("Full pattern cutting").build();
        Option noHMM = Option.builder("nh").longOpt("no-hmm")
                        .desc("Don't use the Hidden Markov Model").build();

        Option bd = Option.builder("d").longOpt("dict").hasArg().argName("option")
                        .desc("Use STD(default)/SMALL/BIG/NONE as basic dict\nNone of them will be use if NONE specified").build();
        Option ud = Option.builder("ud").longOpt("user-dict").hasArgs().argName("file")
                        .desc("Additional user's dictionaries filename (if specified)\nMust be specified if use NONE dict").build();

        //Option idf = Option.builder("idf").longOpt("idfdict").hasArg().argName("file").desc("custom IDF table").build();
        //Option sw = Option.builder("sw").longOpt("stopword").hasArg().argName("file").desc("custom stop words").build();

        options.addOption(help).addOption(version)
                .addOption(in).addOption(out).addOption(str)
                .addOption(pos).addOption(all).addOption(noHMM)
                .addOption(bd).addOption(ud);
                //.addOption(wf).addOption(idf).addOption(sw);

        try {
            DefaultParser parser = new DefaultParser();
            CommandLine cli = parser.parse(options, args);

            if (cli.hasOption('h')) {
                printHelpInfo(options);
                return;
            }
            if (cli.hasOption('v')) {
                printVersionInfo();
                return;
            }
            if (!cli.hasOption('s') && !cli.hasOption('i')) {
                printHelpInfo(options);
                return;
            } else {
                String dict = cli.getOptionValue("d", "");
                if (dict.equals("STD") || dict.isEmpty()) {
                    dict = Dict.STD_WORD_DICT_GZ;
                } else if (dict.equals("BIG")) {
                    dict = Dict.BIG_WORD_DICT_GZ;
                } else if (dict.equals("SMALL")) {
                    dict = Dict.SMALL_WORD_DICT_GZ;
                } else if (dict.equals("NONE")) {
                    dict = ""; // 真正的表示不使用
                }
                String[] userDict = cli.getOptionValues("ud");
                boolean POS = cli.hasOption("pos");
                boolean HMM = cli.hasOption("nh");
                boolean cutAll = cli.hasOption('a');

                List<String> dicts = new ArrayList<String>();
                dicts.add(dict);
                if (userDict != null) {
                    dicts.addAll(Arrays.asList(userDict));
                }

                Tokenizer dt = new Tokenizer(dict);
                if (userDict != null) {
                    for (String filename : userDict) {
                        dt.addWord(filename);
                    }
                }

                if (POS) {
                    POSTokenizer pt = new POSTokenizer(dt);

                } else {

                }

            }

        } catch (ParseException ex) {
            printHelpInfo(options);
        }
    }*/
}
