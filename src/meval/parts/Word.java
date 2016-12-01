package meval.parts;


import toolbox.CsvLiner;


final public class Word {
    
    //動的なフィールド
    final public String surface;
    final public String[] fields;
    final public int charNum;
    final private boolean isMeCabStyle;
    
    
    //コンストラクタ
    public Word(String line) {
        
        ////.mecab形式
        if (line.contains("\t")) {
            this.isMeCabStyle = true;
            String[] splittedLine = line.split("\t");
            this.surface = splittedLine[0];
            String[] tmpFields = CsvLiner.split(splittedLine[1], "");
            this.fields = new String[1+tmpFields.length];
            this.fields[0] = this.surface;
            System.arraycopy(tmpFields, 0, this.fields, 1, tmpFields.length);
        ////.myk形式
        } else {
            this.isMeCabStyle = false;
            this.fields = CsvLiner.split(line, "");
            this.surface = this.fields[0];
        }
        
        //文字数の初期化
        this.charNum = this.surface.codePointCount(0, this.surface.length());
        
    }
    
    
    @Override
    final public String toString() {
        if (this.isMeCabStyle) {
            return this.surface + "\t" + toolbox.CsvLiner.join(this.fields, 1, this.fields.length);
        } else {
            return toolbox.CsvLiner.join(this.fields);
        }
    }
     
}
