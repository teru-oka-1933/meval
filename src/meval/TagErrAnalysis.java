package meval;


import java.util.ArrayList;
import meval.parts.*;
import org.apache.commons.cli.*;
import toolbox.*;
import toolbox.myio.WithWriter;


final public class TagErrAnalysis {
    
    //静的なフィールド
    static private String goldfile;
    static private String predfile;
    static private String outfile;
    static private Level level;
    static private int top = 100;
    static private int modeId = 0;
    
    //POS-Taggingスコアの計算用
    static private int corSegWordNum = 0;
    static private int corTagWordNum = 0;

      
    //コマンドライン引数を処理するメソッド
    static private void readOption(String[] args) throws Exception {
        
        //オプション定義
        Options options = new Options();
        //goldファイル
        options.addOption("g", "gold", true, "*Gold-annotation file (.mecab)");
        //predictファイル
        options.addOption("p", "pred", true, "*System-predict file (.mecab)");
        //出力ファイル
        options.addOption("o", "output", true, "Output file");
        //対象とするレベルで見るフィールド
        options.addOption("f", "field", true, "*Field numbers using for target level (e.g., \"0+1+2\")");
        //上位何件表示するか
        options.addOption("t", "top", true, "Output top <arg> error instances (Default: MIN(100, ALL))");
        //出力モード
        options.addOption("m", "mode", true, "Output mode (Default: 0)"+ ToolBox.lsep +
                                             "0: Output frequency of confusion patterns" + ToolBox.lsep +
                                             "1: Output frequency of un-distinguished tags in gold" + ToolBox.lsep +
                                             "2: Output frequency of incorrect tags in pred");
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
            hf.printHelp("meval.TagErrAnalysis [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-g --gold (必須)
        if (cl.hasOption("g") || cl.hasOption("gold")) {
            TagErrAnalysis.goldfile = cl.getOptionValue("g");
        } else {
            System.out.println("");
            System.out.println("Not find -g or --gold option !");
            System.out.println("");
            System.exit(0);
        }
        //-p --predict (必須)
        if (cl.hasOption("p") || cl.hasOption("pred")) {
            TagErrAnalysis.predfile = cl.getOptionValue("p");
        } else {
            System.out.println("");
            System.out.println("Not find -p or --pred option !");
            System.out.println("");
            System.exit(0);
        }
        //-o --output (任意)
        if (cl.hasOption("o") || cl.hasOption("output")) {
            TagErrAnalysis.outfile = cl.getOptionValue("o");
        }
        //-f --field (必須)
        if (cl.hasOption("f") || cl.hasOption("field")) {
            TagErrAnalysis.parseLevel(cl.getOptionValue("f"));
        } else {
            System.out.println("");
            System.out.println("Not find -f or --field option !");
            System.out.println("");
            System.exit(0);
        }
        //-t --top (任意)
        if (cl.hasOption("t") || cl.hasOption("top")) {
            TagErrAnalysis.top = Integer.parseInt(cl.getOptionValue("t"));
        }
        //-m --mode (任意)
        if (cl.hasOption("m") || cl.hasOption("mode")) {
            String id = cl.getOptionValue("m");
            if (id.equals("0") || id.equals("1") || id.equals("2")) {
                TagErrAnalysis.modeId = Integer.parseInt(id);
            } else {
                System.out.println("");
                System.out.println("Mode Option is 0 or 1 or 2!");
                System.out.println("");
                System.exit(0);
            }
        }
    }
    
    
    //評価対象となるレベル（"0+2+3"）のパース
    static private void parseLevel(String strLevels) {
        strLevels = strLevels.replace("\"", "");
        TagErrAnalysis.level = new Level(strLevels);
    }
    
     
    
    //各レベルのgoldとpredの一致数をカウントするメソッド
    static private void tagErrAnalysis (Corpus goldCorpus, Corpus predCorpus) {
        
        //ファイル出力用のwriter
        WithWriter writer = null;
        if (TagErrAnalysis.outfile != null) {
            writer = new WithWriter(TagErrAnalysis.outfile);
            writer.writeLine(ToolBox.doubleLine);
            writer.writeLine("MEVAL TAG-ERR ANALYSIS");
            writer.writeLine(ToolBox.doubleLine);
            writer.writeLine("Gold:  " + TagErrAnalysis.goldfile);
            writer.writeLine("Pred:  " + TagErrAnalysis.predfile);
            writer.writeLine("Mode:  " + TagErrAnalysis.modeId);
            writer.writeLine("Top :  " + TagErrAnalysis.top);
            writer.writeLine(ToolBox.singleLine);
            writer.writeLine("");
            if (TagErrAnalysis.modeId == 0) {
                writer.writeLine("  GOLD  |  ->  |  PRED  |  ERR_NUM");
            } else if (TagErrAnalysis.modeId == 1) {
                writer.writeLine("  GOLD  |  ERR_NUM  |  COR_SEG  |  ALL");
            } else {
                writer.writeLine("  PRED  |  ERR_NUM  |  COR_SEG  |  ALL");
            }
            writer.writeLine(ToolBox.singleLine);
        }
        
        //カウンターを用意
        KeyFreqCounter errCounter = new KeyFreqCounter();
        KeyFreqCounter corSegCounter = null;
        if (TagErrAnalysis.modeId != 0) {
           corSegCounter = new KeyFreqCounter();
        }
        KeyFreqCounter allCounter = null;
        if (TagErrAnalysis.modeId != 0) {
           allCounter = new KeyFreqCounter();
        }
        
        //1文ずつ処理
        for (int i=0; i<goldCorpus.sentNum; ++i) {
            
            //文内文字数が違わないかチェック
            Sentence goldSent = goldCorpus.sentList.get(i);
            Sentence predSent = predCorpus.sentList.get(i);
            if (goldSent.charNum != predSent.charNum) {
                String message = ToolBox.lsep +
                                 "MeVal Exception !" + ToolBox.lsep +
                                 "  Gold sentence's char num != Pred sentence's char num !" + ToolBox.lsep +
                                 "    Sentence Num: " + i+1 + ToolBox.lsep +
                                 "    Gold: " + goldSent.toRawSentence() + ToolBox.lsep +
                                 "    Pred: " + predSent.toRawSentence() + ToolBox.lsep +
                                 ToolBox.lsep;
                System.err.println(message);
                System.exit(0);
            }
            
            //Mode 1 or 2 の場合，ALLのカウントを更新
            if (TagErrAnalysis.modeId == 1) {
                for (int j=0; j<goldSent.wordNum; ++j) {
                    Word goldWord = goldSent.wordList[j];
                    String[] goldFields = goldWord.fields;
                    String goldKey = TagErrAnalysis.makeKey(goldFields);
                    allCounter.addCount(goldKey);
                }
            } else if (TagErrAnalysis.modeId == 2) {
                for (int j=0; j<predSent.wordNum; ++j) {
                    Word predWord = predSent.wordList[j];
                    String[] predFields = predWord.fields;
                    String predKey = TagErrAnalysis.makeKey(predFields);
                    allCounter.addCount(predKey);
                }
            }
            
            //単語の積み合い処理
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
                        
                        ++ TagErrAnalysis.corSegWordNum;
                        
                        String[] goldFields = goldSent.wordList[goldWordIndex-1].fields;
                        String[] predFields = predSent.wordList[predWordIndex-1].fields;
                        String goldKey = TagErrAnalysis.makeKey(goldFields);
                        String predKey = TagErrAnalysis.makeKey(predFields);
                        
                        //Mode 1 or 2 の場合，COE_SEGのカウントを更新
                        if (TagErrAnalysis.modeId == 1) {
                            corSegCounter.addCount(goldKey);
                        } else if (TagErrAnalysis.modeId == 2) {
                            corSegCounter.addCount(predKey);
                        }
                        
                        if (goldKey.equals(predKey)) {
                            ++ TagErrAnalysis.corTagWordNum;
                            
                        } else {
                            String key;
                            if (TagErrAnalysis.modeId == 0) {
                                key = goldKey + "\t->\t" + predKey;
                            } else if (TagErrAnalysis.modeId == 1) {
                                key = goldKey;
                            } else {
                                key = predKey;
                            }
                            errCounter.addCount(key);
                        }
                    }
                }
            }
        }
        //ファイル出力
        if (writer != null) {
            ArrayList<StringIntTuple> sortedTags = errCounter.getSortedItems();
            int maxNum = Math.min(TagErrAnalysis.top, sortedTags.size());
            for (int i=0; i<maxNum; ++i) {
                StringIntTuple siTuple = sortedTags.get(i);
                String key = siTuple.str;
                int err = siTuple.i;
                String outputLine = key + "\t" + err;
                if (TagErrAnalysis.modeId != 0) {
                    int corSeg = corSegCounter.getFreq(key);
                    int all = allCounter.getFreq(key);
                    outputLine = outputLine + "\t" + corSeg + "\t" + all;
                }
                writer.writeLine(outputLine);
            }
            writer.close();
        }
    }
    
    static private String makeKey(String[] fields) {
        StringBuilder ret = new StringBuilder("");
        for (int i=0; i<TagErrAnalysis.level.fieldNumlList.length; ++i) {
            if (i != 0) { ret.append("+"); }
            int fieldNum = TagErrAnalysis.level.fieldNumlList[i];
            String field = (fields.length > fieldNum)? fields[fieldNum]: "*";
            if (field.equals("")) { field = "*"; }
            ret.append(field);
        }
        return ret.toString();
    }

        
    //MAIN文
    public static void main(String[] args) throws Exception {
        
        //args = "-g gold.mecab -p pred.mecab -l \"0+1+2+3+4+5+6\" -t 5 -m 2 -o tagTest.txt".split(" ");
    
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL TAG-ERR ANALYSIS");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        TagErrAnalysis.readOption(args);
        
        //読み込んだ引数の確認
        System.out.println("Gold:  " + TagErrAnalysis.goldfile);
        System.out.println("Pred:  " + TagErrAnalysis.predfile);
        System.out.println("Output:  " + TagErrAnalysis.outfile);
        System.out.println("Target Fields: " + TagErrAnalysis.level.strFieldNums);
        System.out.println(ToolBox.singleLine);
        
        //goldの読み込み
        Corpus goldCorpus = new Corpus(TagErrAnalysis.goldfile);
        
        //predの読み込み
        Corpus predCorpus = new Corpus(TagErrAnalysis.predfile);
        
        //2つのコーパスを比較して,その結果を画面表示
        Corpus.displayCorpusInfo(goldCorpus, predCorpus);
        
        System.out.println(ToolBox.singleLine);
        
        //結果を出力
        TagErrAnalysis.tagErrAnalysis(goldCorpus, predCorpus);
        
        //結果を表示
        System.out.println("Correctly Tagged Words:    " + TagErrAnalysis.corTagWordNum);
        System.out.println("Correctly Segmented Words: " + TagErrAnalysis.corSegWordNum);
        
        float acc = (float)TagErrAnalysis.corTagWordNum / (float)TagErrAnalysis.corSegWordNum;
        
        System.out.println("Acc. : " + ToolBox.round2d(acc*100f) + "% (" +
                           TagErrAnalysis.corTagWordNum + "/" + 
                           TagErrAnalysis.corSegWordNum + " = " + acc + ")");
        
        System.out.println(ToolBox.singleLine);
        System.out.println("");
        System.out.println("...Done!");
        System.out.println("");
        
    }
}
