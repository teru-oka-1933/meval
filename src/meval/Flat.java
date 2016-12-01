package meval;


import meval.parts.*;
import org.apache.commons.cli.*;
import toolbox.ToolBox;
import toolbox.myio.WithWriter;


final public class Flat {
    
    
    //静的なフィールド
    static private String inputfile;
    static private String outfile;
    
    
    //コマンドライン引数を処理するメソッド
    static private void readOption(String[] args) throws Exception {
        
        //オプション定義
        Options options = new Options();
        //入力ファイル
        options.addOption("i", "input", true, "*Input file (.mecab)");
        //出力ファイル
        options.addOption("o", "output", true, "*Raw text file (.txt)");
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
            hf.printHelp("meval.Flat [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-i --input (必須)
        if (cl.hasOption("i") || cl.hasOption("input")) {
            Flat.inputfile = cl.getOptionValue("i");
        } else {
            System.err.println("");
            System.err.println("Not find -i or --input option !");
            System.err.println("");
            System.exit(0);
        }
        //-tr --train (必須)
        if (cl.hasOption("o") || cl.hasOption("output")) {
            Flat.outfile = cl.getOptionValue("o");
        } else {
            System.err.println("");
            System.err.println("Not find -o or --output option !");
            System.err.println("");
            System.exit(0);
        }
    }
    
    
    //MAIN文
    public static void main(String[] args) throws Exception {
        
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL FLAT");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        Flat.readOption(args);
        
        //読み込んだ引数の確認
        System.out.println("Input:  " + Flat.inputfile);
        System.out.println("Output:  " + Flat.outfile);

        //入力ファイルの読み込み
        Corpus corpus = new Corpus(Flat.inputfile);
        
        System.out.println(ToolBox.singleLine);
        
        //読み込んだコーパスの内容を画面表示
        Corpus.displayCorpusInfo(corpus);
        
        //書き出し
        { WithWriter writer = new WithWriter(Flat.outfile);
            for (int i=0; i<corpus.sentNum; ++i) {
                Sentence sent = corpus.sentList.get(i);
                writer.writeLine(sent.toRawSentence());
            }
        writer.close(); }
        
        System.out.println(ToolBox.singleLine);
        System.out.println("");
        System.out.println("...Done!");
        System.out.println("");
    }
    
}
