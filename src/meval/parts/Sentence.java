package meval.parts;


import java.util.ArrayList;


final public class Sentence {
    
    //動的なフィールド
    final public int wordNum;
    final public Word[] wordList;
    final public int charNum;
    
    
    //コンストラクタ
    public Sentence(ArrayList<String> lineList) {
        
        //単語数の初期化
        this.wordNum = lineList.size();
        
        //単語リストと文字数の初期化
        this.wordList = new Word[this.wordNum];
        int tmpCharNum = 0;
        for (int i=0; i<this.wordNum; ++i) {
            String line = lineList.get(i);
            Word word = new Word(line);
            tmpCharNum += word.charNum;
            this.wordList[i] = word;
        }
        this.charNum = tmpCharNum;
        
    }
    
    //平文化
    final public String toRawSentence() {
        StringBuilder sb = new StringBuilder("");
        for (int i=0; i<this.wordNum; ++i) {
            sb.append(this.wordList[i].surface);
        }
        return sb.toString();
    }
    
    
    @Override
    final public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (int i=0; i<this.wordNum; ++i) {
            sb.append(this.wordList[i].toString());
            sb.append("\n");
        }
        sb.append("EOS\n");
        return sb.toString();
    }
    
    
    
}
