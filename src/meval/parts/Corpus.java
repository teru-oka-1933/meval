package meval.parts;


import java.util.*;
import toolbox.ToolBox;
import toolbox.myio.WithReadLine;


final public class Corpus {
    
    //静的なフィールド
    final public int charNum;
    final public int wordNum;
    final public int sentNum;
    final public ArrayList<Sentence> sentList = new ArrayList();
    
    
    //コンストラクタ
    public Corpus(String filename) {
        int tmpCharNum = 0;
        int tmpWordNum = 0;
        ArrayList<String> lineList = new ArrayList();
        for (String line : new WithReadLine(filename)) {
            if (! line.isEmpty()) {
                if (line.equals("EOS")) {
                    Sentence sent = new Sentence(lineList);
                    tmpCharNum += sent.charNum;
                    tmpWordNum += sent.wordNum;
                    this.sentList.add(sent);
                    lineList = new ArrayList();
                } else {
                    lineList.add(line);
                }
            }
        }
        //ファイルがEOSで終わっていない場合の例外発生
        if (! lineList.isEmpty()) {
            String message = ToolBox.lsep +
                             "MeVal Original Exception !" + ToolBox.lsep + 
                             "  " + filename + " is not end \"EOS\" !" + ToolBox.lsep +
                             ToolBox.lsep;
            System.err.println(message);
            System.exit(0);
        }
        this.charNum = tmpCharNum;
        this.wordNum = tmpWordNum;
        this.sentNum = this.sentList.size();
    }
    
    
    
    //1つのコーパスの内容を画面表示
    static public void displayCorpusInfo(Corpus corpus) {
        //コーパス内総文数表示
        System.out.println(" Sentence Num: " + corpus.sentNum);
        //コーパス内総単語数表示
        System.out.println("     Word Num: " + corpus.wordNum);
        //コーパス内総文字数表示
        System.out.println("Character Num: " + corpus.charNum);
    }
    
    
    //2つのコーパスを比較しつつ，その内容を画面表示
    static public void displayCorpusInfo(Corpus goldCorpus, Corpus predCorpus) {
        //コーパス内総文数表示
        if ( goldCorpus.sentNum == predCorpus.sentNum ) {
            System.out.println("       Sentence Num: " + goldCorpus.sentNum);
        } else {
            String message = ToolBox.lsep +
                             "MeVal Exception!:" + ToolBox.lsep + 
                             "  Gold sentence num != Pred sentence Num !" + ToolBox.lsep + 
                             "    Gold: " + goldCorpus.sentNum + ToolBox.lsep + 
                             "    Pred: " + predCorpus.sentNum + ToolBox.lsep +
                             ToolBox.lsep;
            System.err.println(message);
            System.exit(0);
        }
        //コーパス内総単語数表示
        System.out.println("Gold Word Num (GLD): " + goldCorpus.wordNum);
        System.out.println("Pred Word Num (PRD): " + predCorpus.wordNum);  
        //コーパス内総文字数表示
        if ( goldCorpus.charNum == predCorpus.charNum ) {
            System.out.println("      Character Num: " + goldCorpus.charNum);
        } else {
            String message = ToolBox.lsep +
                             "MeVal Exception !" + ToolBox.lsep + 
                             "  Gold char num != Pred char num !" + ToolBox.lsep +
                             "    Gold: " + goldCorpus.charNum + ToolBox.lsep +
                             "    Pred: " + predCorpus.charNum + ToolBox.lsep +
                             ToolBox.lsep;
            System.err.println(message);
            System.exit(0);
        }
    }
    
    
    //sentList内の文をシャッフル
    final public void sentListShuffle() {
        Collections.shuffle(this.sentList);
    }
    
    
}
