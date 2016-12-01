package meval;


import java.util.*;
import meval.parts.*;
import org.apache.commons.cli.*;
import toolbox.*;


final public class BootstrapTest {
    
    //静的なフィールド
    static private String goldfile1;
    static private String goldfile2;
    static private String predfile1;
    static private String predfile2;
    static private Level level = null;
    static private int B;
    static private float alpha;
    static private boolean testPrec;
    static private boolean testRec;
    
     
    //コマンドライン引数を処理するメソッド
    static private void readOption(String[] args) throws Exception {
        
        //オプション定義
        Options options = new Options();
        //goldファイル
        options.addOption("g1", "gold1", true, "*Method 1's gold-annotation file (.mecab)");
        options.addOption("g2", "gold2", true, "Method 2's gold-annotation file (.mecab)");
        //predictファイル
        options.addOption("p1", "pred1", true, "*Method 1's system-predict file (.mecab)");
        options.addOption("p2", "pred2", true, "*Method 2's system-predict file (.mecab)");
        //対象とするレベルで見るフィールド
        options.addOption("f", "field", true, "Field-numbers using for test (e.g., \"1+2+3\")");
        //リサンプリング回数
        options.addOption("B", true, "Resampling size (Default: 1000)");
        //優位水準
        options.addOption("alpha", true, "Significance level (Default: 0.001 (1%))");
        //Precisionの検定も実施
        options.addOption("prec", false, "Display Precision's result");
        //Recallの検定も実施
        options.addOption("rec", false, "Display Recall's result");
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
            hf.printHelp("meval.BootstrapTest [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-g1 --gold1 (必須)
        if (cl.hasOption("g1") || cl.hasOption("gold1")) {
            BootstrapTest.goldfile1 = cl.getOptionValue("g1");
        } else {
            System.err.println("");
            System.err.println("Not find -g1 or --gold1 option !");
            System.err.println("");
            System.exit(0);
        }
        //-g2 --gold2 (任意)
        if (cl.hasOption("g2") || cl.hasOption("gold2")) {
            BootstrapTest.goldfile2 = cl.getOptionValue("g2");
        } else {
            //指定されていない場合はMethod1のgoldと同じものを使う
            BootstrapTest.goldfile2 = BootstrapTest.goldfile1;
        }
        //-p1 --predict1 (必須)
        if (cl.hasOption("p1") || cl.hasOption("pred1")) {
            BootstrapTest.predfile1 = cl.getOptionValue("p1");
        } else {
            System.err.println("");
            System.err.println("Not find -p1 or --pred1 option !");
            System.err.println("");
            System.exit(0);
        }
        //-p2 --predict2 (必須)
        if (cl.hasOption("p2") || cl.hasOption("pred2")) {
            BootstrapTest.predfile2 = cl.getOptionValue("p2");
        } else {
            System.err.println("");
            System.err.println("Not find -p2 or --pred2 option !");
            System.err.println("");
            System.exit(0);
        }
        //-f --field (任意)
        if (cl.hasOption("f") || cl.hasOption("field")) {
            BootstrapTest.parseLevel(cl.getOptionValue("f"));
        }
        //-B (任意)
        if (cl.hasOption("B")) {
            BootstrapTest.B = Integer.parseInt(cl.getOptionValue("B"));
            if (BootstrapTest.B < 100) {
                System.err.println("");
                System.err.println("You should set resampling size more large (>=100) !");
                System.err.println("");
                System.exit(0);
            }
        } else {
            BootstrapTest.B = 1000;
        }
        //-alpha (任意)
        if (cl.hasOption("alpha")) {
            BootstrapTest.alpha = Float.parseFloat(cl.getOptionValue("alpha"));
            if ( (BootstrapTest.alpha <= 0.0f) || (BootstrapTest.alpha >= 1.0f) ) {
                System.err.println("");
                System.err.println("You should set significantce level (0 < alpha < 1) !");
                System.err.println("");
                System.exit(0);
            }
        } else {
            BootstrapTest.alpha = 0.001f;
        }
        //-prec (任意)
        if (cl.hasOption("prec")) {
            BootstrapTest.testPrec = true;
        } else {
            BootstrapTest.testPrec = false;
        }
        //-rec (任意)
        if (cl.hasOption("rec")) {
            BootstrapTest.testRec = true;
        } else {
            BootstrapTest.testRec = false;
        }
    }
    
    
    //評価対象となるレベル（"0+2+3"）のパース
    static private void parseLevel(String strLevels) {
        strLevels = strLevels.replace("\"", "");
        BootstrapTest.level = new Level(strLevels);
    }

    
    //sentListのリサンプリング
    static private Sentence[][] resampling(Corpus goldCorpus, Corpus predCorpus) {
        ArrayList<Sentence> goldSentList = goldCorpus.sentList;
        ArrayList<Sentence> predSentList = predCorpus.sentList;
        int sentNum = goldCorpus.sentNum;
        Sentence[][] ret = new Sentence[2][sentNum];
        for (int i=0; i<sentNum; ++i) {
            int rand = (int)(Math.random()*sentNum);
            ret[0][i] = goldSentList.get(rand);
            ret[1][i] = predSentList.get(rand);
        }
        return ret;
    }
    
    
    //act値の計算
    static private float[] calcAct(Corpus goldCorpus, Corpus  predCorpus) {
        float[] ret = new float[3];
        int cor = 0;
        for (int i=0; i<goldCorpus.sentNum; ++i) {
            Sentence goldSent = goldCorpus.sentList.get(i);
            Sentence predList = predCorpus.sentList.get(i);
            int[] sentCorCounter = Scorer.countSentCorBS(goldSent, predList);
            cor += sentCorCounter[sentCorCounter.length-1]; //0にはlevel0だけの結果が入っている．
        }
        float prec = ((float) cor) / ((float) predCorpus.wordNum); 
        float rec = ((float) cor) / ((float) goldCorpus.wordNum);
        float f = ToolBox.calcF(prec, rec);
        ret[0] = prec;
        ret[1] = rec;
        ret[2] = f;
        return ret;
    }
    
    //psd値を計算
    static private float[] calcPsd(Sentence[] goldSentList, Sentence[] predSentList) {
        float[] ret = new float[3];
        int cor = 0;
        float goldWordNum = 0.0f;
        float predWordNum = 0.0f;
        for (int i=0; i<goldSentList.length; ++i) {
            Sentence goldSent = goldSentList[i];
            Sentence predSent = predSentList[i];
            int[] sentCorCounter = Scorer.countSentCorBS(goldSent, predSent);
            cor += sentCorCounter[sentCorCounter.length-1]; //0にはlevel0だけの結果が入っている．
            goldWordNum += goldSent.wordNum;
            predWordNum += predSent.wordNum;
        }
        float prec = ((float) cor) / predWordNum; 
        float rec = ((float) cor) / goldWordNum;
        float f = ToolBox.calcF(prec, rec);
        ret[0] = prec;
        ret[1] = rec;
        ret[2] = f;
        return ret;
    }
    
    
    //信頼区間の計算
    static private float[] calcConfidenceInterval(float act1, float act2, float[] psdList1, float[] psdList2) {
        float sitaAct = act1 - act2;
        float[] psdSitaList = new float[BootstrapTest.B];
        for (int b=0; b<BootstrapTest.B; ++b) {
            psdSitaList[b] = psdList1[b] - psdList2[b];
        }
        //昇順に並び替え
        Arrays.sort(psdSitaList);
        //信頼区間を計算
        float[] ret = new float[2];
        ////下限
        float beta_upper = 1.0f - (BootstrapTest.alpha / 2.0f);
        int betaB_upper = (int) (  beta_upper * ((float) BootstrapTest.B) ); 
        ret[0] = (2.0f * sitaAct) - psdSitaList[betaB_upper];
        ////上限
        float beta_under = BootstrapTest.alpha / 2.0f;
        int betaB_under = (int) (  beta_under * ((float) BootstrapTest.B) );
        ret[1] = (2.0f * sitaAct) - psdSitaList[betaB_under];
        return ret; 
    }
    
    
    //MAIN文
    public static void main(String[] args) throws Exception {
        
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL BOOTSTRAP TEST");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        BootstrapTest.readOption(args);

        //読み込んだ引数の確認
        System.out.println("Method1:");
        System.out.println("  Gold:  " + BootstrapTest.goldfile1);
        System.out.println("  Pred:  " + BootstrapTest.predfile1);
        System.out.println("Method2:");
        System.out.println("  Gold:  " + BootstrapTest.goldfile2);
        System.out.println("  Pred:  " + BootstrapTest.predfile2);
        System.out.print("Target Fields : 0");
        if (BootstrapTest.level != null) {
            System.out.print("+" + BootstrapTest.level.strFieldNums);
        }
        System.out.println("");
        System.out.println("Resampling Size : " + BootstrapTest.B);
        System.out.println("Significance Level : " + BootstrapTest.alpha);
        
        System.out.println(ToolBox.doubleLine);
        
        
        /////////////////////*検定*//////////////////////////////
        
        //levelListをScorerに渡す
        Scorer.setLevelListBS(BootstrapTest.level);
        
        //Method 1
        
        ////goldの読み込み
        Corpus goldCorpus1 = new Corpus(BootstrapTest.goldfile1);
        ////predの読み込み
        Corpus predCorpus1 = new Corpus(BootstrapTest.predfile1);
        ////2つのコーパスを比較して,その結果を画面表示
        System.out.println("Method 1's Corpus:");
        Corpus.displayCorpusInfo(goldCorpus1, predCorpus1);
        
        ////act値の計算
        float[] act1 = BootstrapTest.calcAct(goldCorpus1, predCorpus1);
        float actPrec1 = act1[0];
        float actRec1 = act1[1];
        float actF1 = act1[2];
       
        ////bootstrap
        System.out.println(ToolBox.singleLine);
        System.out.println("Now Resampling ...");
        float[] psdPrecList1 = new float[BootstrapTest.B];
        float[] psdRecList1 = new float[BootstrapTest.B];
        float[] psdFList1 = new float[BootstrapTest.B];
        for (int b=0; b<BootstrapTest.B; ++b) {
            if ((b+1)%100 == 0) { System.out.print("."); }
            //sentListのリサンプリング
            Sentence[][] gpSentList = BootstrapTest.resampling(goldCorpus1, predCorpus1);
            Sentence[] goldSentList = gpSentList[0];
            Sentence[] predSentList = gpSentList[1];
            float[] psd = BootstrapTest.calcPsd(goldSentList, predSentList);
            psdPrecList1[b] = psd[0];
            psdRecList1[b] = psd[1];
            psdFList1[b] = psd[2];
        }
        System.out.println("Done!");
        System.out.println(ToolBox.singleLine);
        
        //Method 2
        
        ////goldの読み込み
        Corpus goldCorpus2 = new Corpus(BootstrapTest.goldfile2);
        ////predの読み込み
        Corpus predCorpus2 = new Corpus(BootstrapTest.predfile2);
        ////2つのコーパスを比較して,その結果を画面表示
        System.out.println("Method 2's Corpus:");
        Corpus.displayCorpusInfo(goldCorpus2, predCorpus2);
        
        ////act値の計算
        float[] act2 = BootstrapTest.calcAct(goldCorpus2, predCorpus2);
        float actPrec2 = act2[0];
        float actRec2 = act2[1];
        float actF2 = act2[2];
       
        ////bootstrap
        System.out.println(ToolBox.singleLine);
        System.out.println("Now Resampling ...");
        float[] psdPrecList2 = new float[BootstrapTest.B];
        float[] psdRecList2 = new float[BootstrapTest.B];
        float[] psdFList2 = new float[BootstrapTest.B];
        for (int b=0; b<BootstrapTest.B; ++b) {
            if ((b+1)%100 == 0) { System.out.print("."); }
            //sentListのリサンプリング
            Sentence[][] gpSentList = BootstrapTest.resampling(goldCorpus2, predCorpus2);
            Sentence[] goldSentList = gpSentList[0];
            Sentence[] predSentList = gpSentList[1];
            float[] psd = BootstrapTest.calcPsd(goldSentList, predSentList);
            psdPrecList2[b] = psd[0];
            psdRecList2[b] = psd[1];
            psdFList2[b] = psd[2];
        }
        System.out.println("Done!");
        
        
        //結果を表示
        ////Precision
        if (BootstrapTest.testPrec) {
            float[] ci = BootstrapTest.calcConfidenceInterval(actPrec1, actPrec2, 
                                                              psdPrecList1, psdPrecList2);
            System.out.println(ToolBox.doubleLine);
            System.out.println("Precision");
            System.out.println(ToolBox.doubleLine);
            System.out.println("Method1: " + ToolBox.round2d(actPrec1*100f) + "%" +
                               " (" + actPrec1 + ")");
            System.out.println("Method2: " + ToolBox.round2d(actPrec2*100f) + "%" +
                               " (" + actPrec2 + ")");
            System.out.println(ToolBox.singleLine);
            System.out.println("Confidence Interval:");
            System.out.println("  [" + ci[0] + ", " + ci[1] +"]");
            System.out.println("Result:");
            if (ci[0] <= 0.0f && 0.0f <= ci[1]) {
                System.out.println("  NOT SIGNIFICANT (0 is in this interval !)");
            } else {
                System.out.println("  SIGNIFICANT (0 is NOT in this interval)");
            }
        }
        ////Recall
        if (BootstrapTest.testRec) {
            float[] ci = BootstrapTest.calcConfidenceInterval(actRec1, actRec2, 
                                                              psdRecList1, psdRecList2);
            System.out.println(ToolBox.doubleLine);
            System.out.println("Recall");
            System.out.println(ToolBox.doubleLine);
            System.out.println("Method1: " + ToolBox.round2d(actRec1*100f) + "%" +
                               " (" + actRec1 + ")");
            System.out.println("Method2: " + ToolBox.round2d(actRec2*100f) + "%" +
                               " (" + actRec2 + ")");
            System.out.println(ToolBox.singleLine);
            System.out.println("Confidence Interval:");
            System.out.println("  [" + ci[0] + ", " + ci[1] +"]");
            System.out.println("Result:");
            if (ci[0] <= 0.0f && 0.0f <= ci[1]) {
                System.out.println("  NOT SIGNIFICANT (0 is in this interval !)");
            } else {
                System.out.println("  SIGNIFICANT (0 is NOT in this interval)");
            }
        }
        ////F
        float[] ci = BootstrapTest.calcConfidenceInterval(actF1, actF2, 
                                                          psdFList1, psdFList2);
        System.out.println(ToolBox.doubleLine);
        System.out.println("F");
        System.out.println(ToolBox.doubleLine);
        System.out.println("Method1: " + ToolBox.round2d(actF1*100f) +
                           " (" + actF1 + ")");
        System.out.println("Method2: " + ToolBox.round2d(actF2*100f) +
                           " (" + actF2 + ")");
        System.out.println(ToolBox.singleLine);
        System.out.println("Confidence Interval:");
        System.out.println("  [" + ci[0] + ", " + ci[1] +"]");
        System.out.println("Result:");
        if (ci[0] <= 0.0f && 0.0f <= ci[1]) {
            System.out.println("  NOT SIGNIFICANT (0 is in this interval !)");
        } else {
            System.out.println("  SIGNIFICANT (0 is NOT in this interval)");
        }
        
        System.out.println("");
        
        
    }
        

}
