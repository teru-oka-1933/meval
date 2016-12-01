package meval;


import com.ibm.icu.text.Transliterator;
import meval.parts.*;
import org.apache.commons.cli.*;
import toolbox.*;
import toolbox.myio.WithWriter;


final public class WbdErrAnalysis {
    
    //静的なフィールド
    static private String goldfile;
    static private String predfile;
    static private String outfile = null;
    
    //単語境界判定スコア表示用
    static private int tp = 0;
    static private int fp = 0;
    static private int fn = 0;
    
    
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
            hf.printHelp("meval.WbdErrAnalysis [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-g --gold (必須)
        if (cl.hasOption("g") || cl.hasOption("gold")) {
            WbdErrAnalysis.goldfile = cl.getOptionValue("g");
        } else {
            System.out.println("");
            System.out.println("Not find -g or --gold option !");
            System.out.println("");
            System.exit(0);
        }
        //-p --predict (必須)
        if (cl.hasOption("p") || cl.hasOption("pred")) {
            WbdErrAnalysis.predfile = cl.getOptionValue("p");
        } else {
            System.out.println("");
            System.out.println("Not find -p or --pred option !");
            System.out.println("");
            System.exit(0);
        }
        //-o --output (任意)
        if (cl.hasOption("o") || cl.hasOption("output")) {
            WbdErrAnalysis.outfile = cl.getOptionValue("o");
        }
    }
    
    

    static private void wbdErrAnalysis(Corpus goldCorpus, Corpus predCorpus) {
        
        //ファイル出力用のwriter
        WithWriter writer = null;
        if (WbdErrAnalysis.outfile != null) {
            writer = new WithWriter(WbdErrAnalysis.outfile);
            writer.writeLine(ToolBox.doubleLine);
            writer.writeLine("MEVAL WBD-ERR ANALYSIS");
            writer.writeLine(ToolBox.doubleLine);
            writer.writeLine("Gold:  " + WbdErrAnalysis.goldfile);
            writer.writeLine("Pred:  " + WbdErrAnalysis.predfile);
            writer.writeLine(ToolBox.singleLine);
            writer.writeLine("");
        }
        
        for (int i=0; i<goldCorpus.sentNum; ++i) {
            
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
                if (writer != null) { writer.close(); }
                System.exit(0);
            }
            
            String[] goldSegChars = WbdErrAnalysis.toSegChars(goldSent);
            String[] predSegChars = WbdErrAnalysis.toSegChars(predSent);

            
            for (int j=2; j<goldSegChars.length; j+=2) {
                
                if (goldSegChars[j].equals("\b") && predSegChars[j].equals("\b")) {
                    ++ WbdErrAnalysis.tp;
                    
                } else if (! goldSegChars[j].equals(predSegChars[j])) {
                    
                    int bodyStartIndex = j;
                    String goldBody = "";
                    String predBody = "";
                    String falseBody = "";
                    boolean hasFP = false;
                    boolean hasFN = false;
                       
         MAKE_BODY: for (int k=j; k<goldSegChars.length; ++k) {
                        
                        goldBody += goldSegChars[k];
                        predBody += predSegChars[k];
                        
                        if (!goldSegChars[k].equals("\b") && !goldSegChars[k].equals("\n") &&
                            !predSegChars[k].equals("\b") && !predSegChars[k].equals("\n") ) {
                            if (WbdErrAnalysis.isHalf(goldSegChars[k])) {
                                falseBody += " ";
                            } else {
                                falseBody += "  ";
                            }
                            
                        } else if (goldSegChars[k].equals("\b") && predSegChars[k].equals("\n")) {
                            falseBody += "N";
                            hasFN = true;
                            ++ WbdErrAnalysis.fn;
                            
                        } else if (goldSegChars[k].equals("\n") && predSegChars[k].equals("\b")) {
                            falseBody += "P";
                            hasFP = true;
                            ++ WbdErrAnalysis.fp;
                            
                        }else if (goldSegChars[k].equals("\n") && predSegChars[k].equals("\n")) {
                            falseBody += " ";
                            
                        } else if (goldSegChars[k].equals("\b") && predSegChars[k].equals("\b")) {
                            
                            ++ WbdErrAnalysis.tp;
                            
                            if (writer != null) {
                                
                                int bodyEndIndex = k;
                                
                                //HEAD
                                String head;
                                if (hasFP && hasFN) { head = "FPFN  "; }
                                else if (hasFP) { head = "FP//  "; }
                                else {head = "//FN  "; }
                                
                                //直前の末尾がちゃんと切れている単語の表層形(preWord)を取得
                                String goldPreWord = WbdErrAnalysis.getPreWord(goldSegChars, bodyStartIndex);
                                String predPreWord = WbdErrAnalysis.getPreWord(predSegChars, bodyStartIndex);
                                StringStringTuple stPreWord = WbdErrAnalysis.makeSameLen(goldPreWord, predPreWord);
                                goldPreWord = stPreWord.str1;
                                predPreWord = stPreWord.str2;
                                String falsePreWord = WbdErrAnalysis.makeSameLen("", goldPreWord).str1;
                                
                                //直前の単語末尾と現在位置までにある文字列(Prefix)を取得
                                String goldPrefix = WbdErrAnalysis.getPrefix(goldSegChars, j).replace("\b", "|").replace("\n", " ");
                                String predPrefix = goldPrefix;
                                String falsePrefix = WbdErrAnalysis.makeSameLen("", goldPrefix).str1;
                                
                                //Body
                                goldBody = goldBody.replace("\b", "|").replace("\n", " ");
                                predBody = predBody.replace("\b", "|").replace("\n", " ");
                                falseBody = falseBody.replace("P ", "FP").replace("N ", "FN");
                                
                                //直後の頭がちゃんと切れている表層形(NextWord)を取得
                                String goldNextWord = WbdErrAnalysis.getNextWord(goldSegChars, bodyEndIndex);
                                String predNextWord = WbdErrAnalysis.getNextWord(predSegChars, bodyEndIndex);

                                //書き出し
                                writer.writeLine(head + "Sentence Num: " + (i+1));
                                writer.writeLine(head + "GOLD: " + goldPreWord + "|" + goldPrefix + goldBody + goldNextWord);
                                writer.writeLine(head + "PRED: " + predPreWord + "|" + predPrefix + predBody + predNextWord);
                                writer.writeLine(head + "      " + falsePreWord + " " + falsePrefix + falseBody);
                                writer.writeLine(head);
                                j = k;
                                break MAKE_BODY;
                            }  
                        } 
                    }
                }
            }
            -- WbdErrAnalysis.tp; //EOS直前の境界は数えない
        }
        if (writer != null) { writer.close(); }
    }
    
    //\b:単語境界（Boundary）
    //\n:非単語境界（Non-Boundary）
    static private String[] toSegChars(Sentence sent) {
        String[] ret = new String[(sent.charNum*2)+1];
        int retIndex = 0;
        for (int i=0; i<sent.wordNum; ++i) {
            ret[retIndex++] = "\b";
            Word word = sent.wordList[i];
            String wordSurf = word.surface;
            for (int j=0; j<wordSurf.length(); j=wordSurf.offsetByCodePoints(j, 1)) {
                if (j != 0) {
                    ret[retIndex++] = "\n";
                }
                String moji = wordSurf.substring(j, wordSurf.offsetByCodePoints(j, 1));
                ret[retIndex++] = moji;
            }
        }
        ret[retIndex++] = "\b";
        return ret;
    }
    

    static private String getPreWord(String[] segChars, int bodyStartIndex) {
        int i = bodyStartIndex - 1;
        while(! segChars[i].equals("\b")) {
            -- i;
        }
        if (i == 0) {
            return "<BOS>";
        } else {
            StringBuilder ret = new StringBuilder("");
            int preWordEndIndex = i-1;
            int j = preWordEndIndex;
            while(!segChars[j].equals("\b")) {
                if (! segChars[j].equals("\n")) {
                    ret.insert(0, segChars[j]);
                }
                -- j;
            }
            return ret.toString();
        }
    }
    
    static private String getPrefix(String[] segChars, int bodyStartIndex) {
        StringBuilder ret = new StringBuilder("");
        int i = bodyStartIndex-1;
        while(! segChars[i].equals("\b")) {
            ret.insert(0, segChars[i]);
            -- i;
        }
        return ret.toString();
    }
    
    static private String getNextWord(String[] segChars, int bodyEndIndex) {
        if (bodyEndIndex == segChars.length-1) {
            return "<EOS>";
        } else {
            StringBuilder ret = new StringBuilder("");
            int i = bodyEndIndex + 1;
            while(! segChars[i].equals("\b")) {
                if (! segChars[i].equals("\n")) {
                    ret.append(segChars[i]); }
                ++ i;
            }
            return ret.toString();
        }
    }
    
    static private StringStringTuple makeSameLen(String str1, String str2) {
        
        int len1 = str1.codePointCount(0, str1.length());
        int len2 = str2.codePointCount(0, str2.length());
        
        if (len1 == len2) {
            return new StringStringTuple(str1, str2);
        
        } else {
            
            String longStr; int longLen;
            String shortStr; int shortLen;
            
            if (len1 > len2) {
                longStr = str1; longLen = len1;
                shortStr = str2; shortLen = len2;
            } else { //if (len1 < len2)
                longStr = str2; longLen = len2;
                shortStr = str1; shortLen = len1;
            }
            
            int i = 0;
            StringBuilder sbSpace = new StringBuilder("");
            for (int j=0; j<longLen-shortLen; ++j) {
                String moji = longStr.substring(i, longStr.offsetByCodePoints(i, 1));
                if (WbdErrAnalysis.isHalf(moji)) {
                    sbSpace.append(" ");
                } else {
                    sbSpace.append("  ");
                }
                i = longStr.offsetByCodePoints(i, 1);
            }
            if (len1 > len2) {
                return new StringStringTuple(str1, sbSpace.append(str2).toString());
            } else { //if (len1 < len2)
                return new StringStringTuple(sbSpace.append(str1).toString(), str2);
            }
        }
    }
    
    
    static private Transliterator trH2F = Transliterator.getInstance("Halfwidth-Fullwidth");
    static private boolean isHalf(String moji) {
        if (moji.equals("\b") || moji.equals("\n")) {
            return true;
        } else {
            String fullMoji = trH2F.transliterate(moji);
            if (! moji.equals(fullMoji)) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    
    //MAIN文
    public static void main(String[] args) throws Exception {
        
        //args = "-g gold.mecab -p pred.mecab -o test.wbd -h".split(" ");
    
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL WBD-ERR ANALYSIS");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        WbdErrAnalysis.readOption(args);
        
        //読み込んだ引数の確認
        System.out.println("Gold:  " + WbdErrAnalysis.goldfile);
        System.out.println("Pred:  " + WbdErrAnalysis.predfile);
        System.out.println("Output:  " + WbdErrAnalysis.outfile);
        
        System.out.println(ToolBox.singleLine);
        
        //goldの読み込み
        Corpus goldCorpus = new Corpus(WbdErrAnalysis.goldfile);
        
        //predの読み込み
        Corpus predCorpus = new Corpus(WbdErrAnalysis.predfile);
        
        //2つのコーパスを比較して,その結果を画面表示
        Corpus.displayCorpusInfo(goldCorpus, predCorpus);
        
        //エラー分析の実行（内部でファイルへの書き出し）
        WbdErrAnalysis.wbdErrAnalysis(goldCorpus, predCorpus);
        
        //スコア計算
        float prec = (float)WbdErrAnalysis.tp / (float)(WbdErrAnalysis.tp+WbdErrAnalysis.fp);
        float rec = (float)WbdErrAnalysis.tp / (float)(WbdErrAnalysis.tp+WbdErrAnalysis.fn);
        float f1 = ToolBox.calcF(prec, rec);
        
        //結果を表示
        System.out.println(ToolBox.singleLine);
        System.out.println("Sentence Boundary Detection Score:");
        System.out.println(ToolBox.singleLine);
        System.out.println("Prec. : " + ToolBox.round2d(prec*100f) + "% (" + 
                           WbdErrAnalysis.tp + "/" + (WbdErrAnalysis.tp+WbdErrAnalysis.fp) + 
                           " = " + prec + ")");
        System.out.println("Rec. :  " + ToolBox.round2d(rec*100f) + "% (" + 
                           WbdErrAnalysis.tp + "/" + (WbdErrAnalysis.tp+WbdErrAnalysis.fn) + 
                           " = " + rec + ")"); 
        System.out.println("F  :    " + ToolBox.round2d(f1*100f) + "  (" + f1 + ")");
        System.out.println(ToolBox.singleLine);
        System.out.println("TP: " + WbdErrAnalysis.tp + 
                           "   FP: " + WbdErrAnalysis.fp + 
                           "   FN: " + WbdErrAnalysis.fn);
        
        System.out.println(ToolBox.singleLine);
        System.out.println("");
        System.out.println("...Done!");
        System.out.println("");
    }
    
}
