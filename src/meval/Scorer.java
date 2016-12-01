package meval;


import meval.parts.*;
import org.apache.commons.cli.*;
import toolbox.ToolBox;


final public class Scorer {

    //静的なフィールド
    static private String goldfile;
    static private String predfile;
    static private Level[] levelList;
    
     
    //コマンドライン引数を処理するメソッド
    static private void readOption(String[] args) throws Exception {
        
        //オプション定義
        Options options = new Options();
        //goldファイル
        options.addOption("g", "gold", true, "*Gold-annotation file (.mecab)");
        //predictファイル
        options.addOption("p", "pred", true, "*System-predict file (.mecab)");
        //各レベルで見るフィールド
        options.addOption("f", "field", true, "Field numbers using at each level evaluation (e.g., \"1,2+3+4,7+9\")");
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
            hf.printHelp("meval.Scorer [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-g --gold (必須)
        if (cl.hasOption("g") || cl.hasOption("gold")) {
            Scorer.goldfile = cl.getOptionValue("g");
        } else {
            System.err.println("");
            System.err.println("Not find -g or --gold option !");
            System.err.println("");
            System.exit(0);
        }
        //-p --predict (必須)
        if (cl.hasOption("p") || cl.hasOption("pred")) {
            Scorer.predfile = cl.getOptionValue("p");
        } else {
            System.err.println("");
            System.err.println("Not find -p or --pred option !");
            System.err.println("");
            System.exit(0);
        }
        //-l --level (任意)
        if (cl.hasOption("f") || cl.hasOption("field")) {
            Scorer.parseLevel(cl.getOptionValue("f"));
        } else {
            Scorer.levelList = new Level[0];
        }
    }
    
    
    //評価対象となるレベル（"1,2,3+4+5"）のパース
    static private void parseLevel(String levelLine) {
        levelLine = levelLine.replace("\"", "");
        String[] splittedLevelLine = levelLine.split(",");
        Scorer.levelList = new Level[splittedLevelLine.length];
        for (int i=0; i<splittedLevelLine.length; ++i) {
            String strFieldNums = splittedLevelLine[i];
            Level level = new Level(strFieldNums);
            Scorer.levelList[i] = level;
        }
    }

     
    //各レベルのgoldとpredの一致数をカウントするメソッド(文用)
    static private int[] countSentCOR(Sentence goldSent, Sentence predSent) {
        
        int[] ret = new int[1+Scorer.levelList.length]; //1は単語分割(level0)の分
        
        int goldWordIndex = 0;
        int goldCharIndex = 0;
        int predWordIndex = 0;
        int predCharIndex = 0;
        
        while ( (goldWordIndex < goldSent.wordNum) || (predWordIndex < predSent.wordNum) ) {
            
            if (goldCharIndex < predCharIndex) {
                goldCharIndex += goldSent.wordList[goldWordIndex++].charNum;
                
            } else if (goldCharIndex > predCharIndex) {
                predCharIndex += predSent.wordList[predWordIndex++].charNum;
                
            } else { // if(goldCharIndex == predCharIndex)
                goldCharIndex += goldSent.wordList[goldWordIndex++].charNum;
                predCharIndex += predSent.wordList[predWordIndex++].charNum;
                if (goldCharIndex == predCharIndex) {
                    ++ ret[0];
                    String[] goldFields = goldSent.wordList[goldWordIndex-1].fields;
                    String[] predFields = predSent.wordList[predWordIndex-1].fields;
             CHECK: for (int i=0; i<Scorer.levelList.length; ++i) {
                        Level levels = Scorer.levelList[i];
                        for (int j=0; j<levels.fieldNumlList.length; ++j) {
                            int fieldNum = levels.fieldNumlList[j];
                            String goldField = (goldFields.length > fieldNum)? goldFields[fieldNum]: "";
                            String predField = (predFields.length > fieldNum)? predFields[fieldNum]: "";
                            if (! ToolBox.fEquals(goldField, predField, false)) {
                                break CHECK;
                            }
                        }
                        ++ ret[i+1];
                    }//CHECK:
                }
            }
        }
        return ret;
    }
    
    
    //各レベルのgoldとpredの一致数をカウントするメソッド(コーパス用)
    static private int[][] countCorpusCOR(Corpus goldCorpus, Corpus predCorpus) {
        int[][] counter = new int[2][1+Scorer.levelList.length];    //1は単語分割（LEVEL 0用）
        int[] corCounter = counter[0];
        int[] exactMatchCounter = counter[1];
        for (int i=0; i<goldCorpus.sentNum; ++i) {
            Sentence goldSent = goldCorpus.sentList.get(i);
            Sentence predSent = predCorpus.sentList.get(i);
            if (goldSent.charNum != predSent.charNum) {
                String message = ToolBox.lsep +
                                 "MeVal Exception !" + ToolBox.lsep +
                                 "  Gold sentence's char num != Pred sentence's char num !" + ToolBox.lsep +
                                 "    Sentence Num: " + i+1 + ToolBox.lsep +
                                 "    Gold: " + goldSent.charNum + ToolBox.lsep +
                                 "    Pred: " + predSent.charNum + ToolBox.lsep +
                                 ToolBox.lsep;
                System.err.println(message);
                System.exit(0);
            }
            int[] sentCorCounter = Scorer.countSentCOR(goldSent, predSent);
            for (int j=0; j<sentCorCounter.length; ++j) {
                corCounter[j] += sentCorCounter[j];
                if (sentCorCounter[j] == goldSent.wordNum) {
                    ++ exactMatchCounter[j];
                }
            }
        }
        return counter;
    }
    
    
    //スコアリングの結果を表示するための文字列を生成するメソッド
    static private void displayScore(Corpus goldCorpus, Corpus predCorpus) {
        
        //CORと完全一致した文数の計算
        int[][] counter = Scorer.countCorpusCOR(goldCorpus, predCorpus);
        int[] corCounter = counter[0];
        int[] exactMatchCounter = counter[1];
        
        //スコアの表示      
        for (int i=0; i<corCounter.length; ++i) {
            
            System.out.println(ToolBox.doubleLine);

            if (i == 0) { System.out.println("LEVEL 0 : 0"); }
            else {
                Level level = Scorer.levelList[i-1];
                System.out.println("LEVEL " + i + " : +" + level.strFieldNums);
            }
            
            System.out.println(ToolBox.doubleLine);
            
            System.out.println("Correctly Analysed Sentences:");
            float ems = (float) exactMatchCounter[i] / (float) goldCorpus.sentNum;
            System.out.println(ToolBox.round2d(ems*100f) + "%" +
                               " (" + exactMatchCounter[i] + "/" + goldCorpus.sentNum + " = " + ems + ")");
            
            System.out.println(ToolBox.singleLine);
            
            int cor = corCounter[i];
            System.out.println("COR : " + cor);
            
            System.out.println(ToolBox.singleLine);
            
            float prec = (float) cor / (float) predCorpus.wordNum;
            float rec = (float) cor / (float) goldCorpus.wordNum;
            float f = ToolBox.calcF(prec, rec);
            
            System.out.println("Prec. : " + ToolBox.round2d(prec*100f) + "%" +
                               " (" + cor + "/" + predCorpus.wordNum + " = " + prec + ")");
            System.out.println("Rec.  : " + ToolBox.round2d(rec*100f) + "%" +
                               " (" + cor + "/" + goldCorpus.wordNum + " = " + rec + ")");
            System.out.println("F     : " + ToolBox.round2d(f*100f) + " " +
                               " (" + f + ")");
        }
    }
    
    
    
    //MAIN文
    public static void main(String[] args) throws Exception {
        
        //args = "-g gold.mecab -p pred.mecab -l \"1+2+3+4,5+6,7+8\" -h".split(" ");
    
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL SCORER");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        Scorer.readOption(args);
        
        //読み込んだ引数の確認
        System.out.println("Gold:  " + Scorer.goldfile);
        System.out.println("Pred:  " + Scorer.predfile);
        System.out.println(ToolBox.singleLine);
        System.out.println("         Field Num");
        System.out.println("LEVEL 0 : 0");
        for (int i=0; i<Scorer.levelList.length; ++i) {
            Level level = Scorer.levelList[i];
            System.out.println("LEVEL " + (i+1) + " : +" + level.strFieldNums);
        }
        System.out.println(ToolBox.singleLine);
        
        //goldの読み込み
        Corpus goldCorpus = new Corpus(Scorer.goldfile);
        
        //predの読み込み
        Corpus predCorpus = new Corpus(Scorer.predfile);
        
        //2つのコーパスを比較して,その結果を画面表示
        Corpus.displayCorpusInfo(goldCorpus, predCorpus);
        
        //スコアを計算し，表示
        Scorer.displayScore(goldCorpus, predCorpus);
        
        System.out.println("");
    }
    
    
    
/////////////////////*BootstrapTest用のアダプタ的メソッド*///////////////////////////
    
    //BootstrapTestからcountSentCORをつかうときに，まずlevelListをセットする
    static protected void setLevelListBS(Level level) {
        if (level != null) {
            Scorer.levelList = new Level[1];
            Scorer.levelList[0] = level;
        } else {
            Scorer.levelList = new Level[0];
        }
    }
    
    static protected int[] countSentCorBS(Sentence goldSent, Sentence predSent) {
        return Scorer.countSentCOR(goldSent, predSent);
    }
    
}
