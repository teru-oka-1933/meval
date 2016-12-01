package meval;


import meval.parts.*;
import org.apache.commons.cli.*;
import toolbox.ToolBox;


final public class Count {
    
    //静的なフィールド
    static private String inputfile;
    
    
    //コマンドライン引数を処理するメソッド
    static private void readOption(String[] args) throws Exception {
        
        //オプション定義
        Options options = new Options();
        //入力ファイル
        options.addOption("i", "input", true, "*Input corpus (.mecab)");
        //help
        options.addOption("h", "help", false, "Help");
        
        //コマンドライン引数をパース
        CommandLineParser parser = new BasicParser();
        CommandLine cl = parser.parse(options, args);
        
        //パース結果からパラメータ読み込み
        //-h --help (任意)
        if (cl.hasOption("h") || cl.hasOption("help")) {
            HelpFormatter hf = new HelpFormatter();
            System.out.println("");
            hf.printHelp("meval.Count [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-i --input (必須)
        if (cl.hasOption("i") || cl.hasOption("input")) {
            Count.inputfile = cl.getOptionValue("i");
        } else {
            System.err.println("");
            System.err.println("Not find -i or --input option !");
            System.err.println("");
            System.exit(0);
        }
    }
    
    
    //MAIN文
    public static void main(String[] args) throws Exception {
        
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL COUNT");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        Count.readOption(args);
        
        //読み込んだ引数の確認
        System.out.println("Input:  " + Count.inputfile);

        //入力ファイルの読み込み
        Corpus corpus = new Corpus(Count.inputfile);
        
        System.out.println(ToolBox.singleLine);
        
        //読み込んだコーパスの内容を画面表示
        Corpus.displayCorpusInfo(corpus);
        
        System.out.println(ToolBox.singleLine);
        System.out.println("");
        System.out.println("...Done!");
        System.out.println("");
    }
        

}
