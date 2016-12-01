package meval;


import java.util.*;
import meval.parts.*;
import org.apache.commons.cli.*;
import toolbox.ToolBox;
import toolbox.myio.*;


final public class Divide {
    
    
    //静的なフィールド
    static private String inputfile;
    static private String trainfile;
    static private String testfile;
    static private String devfile = null;
    static private int trainRatio;
    static private int testRatio;
    static private int devRatio;
    

      
    //コマンドライン引数を処理するメソッド
    static private void readOption(String[] args) throws Exception {
        
        //オプション定義
        Options options = new Options();
        //goldファイル
        options.addOption("i", "input", true, "*Input corpus (.mecab)");
        //訓練用コーパス
        options.addOption("tr", "train", true, "*Training corpus (.mecab)");
        //テスト用コーパス
        options.addOption("te", "test", true, "*Test corpus (.mecab)");
        //開発用コーパス
        options.addOption("d", "dev", true, "Development corpus (.mecab)" + ToolBox.lsep +
                                            "                   (Default: null)");
        //割合
        options.addOption("r", "ratio", true, "Divideing ratio (\"Train:Test\" or \"Train:Test:Dev\")" + ToolBox.lsep +
                                              "                (Default: 9:1 or 8:1:1)");
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
            hf.printHelp("meval.Divide [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-i --input (必須)
        if (cl.hasOption("i") || cl.hasOption("input")) {
            Divide.inputfile = cl.getOptionValue("i");
        } else {
            System.err.println("");
            System.err.println("Not find -i or --input option !");
            System.err.println("");
            System.exit(0);
        }
        //-tr --train (必須)
        if (cl.hasOption("tr") || cl.hasOption("train")) {
            Divide.trainfile = cl.getOptionValue("tr");
        } else {
            System.err.println("");
            System.err.println("Not find -tr or --train option !");
            System.err.println("");
            System.exit(0);
        }
        //-te --test (必須)
        if (cl.hasOption("te") || cl.hasOption("test")) {
            Divide.testfile = cl.getOptionValue("te");
        } else {
            System.err.println("");
            System.err.println("Not find -te or --test option !");
            System.err.println("");
            System.exit(0);
        }
        //-d --dev (任意)
        if (cl.hasOption("d") || cl.hasOption("dev")) {
            Divide.devfile = cl.getOptionValue("d");
        }
        //-r --ratio (任意)
        if (cl.hasOption("r") || cl.hasOption("ratio")) {
            
            String ratioLine = cl.getOptionValue("r");
            ratioLine = ratioLine.replace("\"", "");
            String[] splittedRatioLine = ratioLine.split(":");
            
            //与えられた比率の数を確認
            if ( (splittedRatioLine.length!=2) && (splittedRatioLine.length!=3) ) {
                System.err.println("");
                System.err.println("Dividing ratios are (\"Train:Test\" or \"Train:Test:Dev\") !");
                System.err.println("");
                System.exit(0);
            }
          
            //与えられた比率がいずれも0以上であることを確認
            for (int i=0; i<splittedRatioLine.length; ++i) {
                int r = Integer.parseInt(splittedRatioLine[i]);
                if (r < 0) {
                   System.err.println("");
                   System.err.println("Each dividing ratio must be > 0 !");
                   System.err.println("");
                   System.exit(0); 
                }
            }
            //比率を設定
            if (splittedRatioLine.length == 2) {
                if (cl.hasOption("d") || cl.hasOption("dev")) {
                    System.err.println("");
                    System.err.println("Dev corpus's ratio is not set !");
                    System.err.println("");
                    System.exit(0);
                } else {
                    Divide.trainRatio = Integer.parseInt(splittedRatioLine[0]);
                    Divide.testRatio = Integer.parseInt(splittedRatioLine[1]);
                    Divide.devRatio = 0;
                }
            } else if (splittedRatioLine.length == 3) {
                if ( !(cl.hasOption("d") || cl.hasOption("dev")) ) {
                    System.err.println("");
                    System.err.println("Not find -d or --dev option !");
                    System.err.println("");
                    System.exit(0);
                } else {
                    Divide.trainRatio = Integer.parseInt(splittedRatioLine[0]);
                    Divide.testRatio = Integer.parseInt(splittedRatioLine[1]);
                    Divide.devRatio = Integer.parseInt(splittedRatioLine[2]);
                }
            }
            
        } else {
            if (cl.hasOption("d") || cl.hasOption("dev")) {
                Divide.trainRatio = 8;
                Divide.testRatio = 1;
                Divide.devRatio = 1;
            } else {
                Divide.trainRatio = 9;
                Divide.testRatio = 1;
                Divide.devRatio = 0;
            }
        }
    }
    
    
    
    public static void main(String[] args) throws Exception {
        
        //args = "-g gold.mecab -p pred.mecab -l \"1+2+3+4,5+6,7+8\" -h".split(" ");
    
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL DIVIDE");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        Divide.readOption(args);
        
        //読み込んだ引数の確認
        System.out.println("Input:  " + Divide.inputfile);
        System.out.println("Train:  " + Divide.trainfile);
        System.out.println("Test :  " + Divide.testfile);
        if (Divide.devfile != null) {
            System.out.println("Dev  :  " + Divide.devfile);
        }
        System.out.print("Dividing Ratio:  " + 
                         Divide.trainRatio + ":" +
                         Divide.testRatio);
        if (Divide.devfile != null) {
            System.out.println(":" + Divide.devRatio);
        } else {
            System.out.println("");
        }
        
        //コーパスの読み込み
        Corpus corpus = new Corpus(Divide.inputfile);
        
        int sentNum = corpus.sentNum;
        
        int n = Divide.trainRatio + Divide.testRatio + Divide.devRatio;
        
        int devNum = sentNum * Divide.devRatio / n;
        int testNum = sentNum * Divide.testRatio / n;
        int trainNum = sentNum - devNum - testNum;
        
        //書き出し
        ArrayList<Sentence> sentList = corpus.sentList;
        ////訓練用コーパス
        { WithWriter writer = new WithWriter(Divide.trainfile);
            for (int i=0; i<trainNum; ++i) {
                writer.write(sentList.remove(0).toString());
            }
        writer.close(); }
        ////テスト用コーパス
        { WithWriter writer = new WithWriter(Divide.testfile);
            for (int i=0; i<testNum; ++i) {
                writer.write(sentList.remove(0).toString());
            }
        writer.close(); }
        ////開発用コーパス
        if (devNum > 0) {
            { WithWriter writer = new WithWriter(Divide.devfile);
                for (int i=0; i<devNum; ++i) {
                    writer.write(sentList.remove(0).toString());
                }
            writer.close(); }
        }
        
        System.out.println(ToolBox.singleLine);
        System.out.println("");
        System.out.println("...Done!");
        System.out.println("");
    }
    
}
