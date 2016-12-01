package toolbox;


public final class CsvLiner {
    
    
    //csv形式のlineをsplitするメソッド
    static public String[] split(String csvLine){
        csvLine = csvLine.replace("\"\"", "\b"); //「""」を「\b」に置換
        StringBuilder newLine = new StringBuilder("");
        boolean dqFlag = false; //ダブルクォーテーションが前にあった(T)か否(F)か
        for (int i=0; i<csvLine.length(); ++i){
            char moji = csvLine.charAt(i);
            if (moji=='"') {
                dqFlag = (dqFlag)? false: true;
            } else {
                if ((moji == ',') && !dqFlag) { newLine.append('\t'); }
                else { 
                    if (moji == '\b'){ newLine.append('"'); }
                    else {newLine.append(moji);}
                }
            }
        }
        return newLine.toString().split("\t", -1);
    }
    
    
    //split時に要素が空になった時，そこに""以外を入れるsplit
    static public String[] split(String csvLine, String atEmptyElem) {
        String[] splittedLine = CsvLiner.split(csvLine);
        for (int i=0; i<splittedLine.length; ++i) {
            if (splittedLine[i].isEmpty()) {
                splittedLine[i] = atEmptyElem;
            }
        }
        return splittedLine;
    }
    
    
    //String[]をcsv形式の1行（String）に変換するメソッド（範囲指定なし）
    static public String join(String[] strArray) {
        return CsvLiner.join(strArray, 0, strArray.length);
    }
    
    //String[]をcsv形式の1行（String）に変換するメソッド（範囲指定あり）
    static public String join(String[] strArray, int start, int until) {
        StringBuilder ret = new StringBuilder("");
        for (int i=start; i<until; ++i) {
            if (i != start) {
                ret.append(",");
            }
            String str = strArray[i];
            if (str == null) {str = "";} //Null文字対応
            boolean hasCamma = false;
            if (str.contains(",")) {
                ret.append("\"");
                hasCamma = true;
            }
            for (int j=0; j<str.length(); j++) {
                char moji = str.charAt(j);
                if (moji == '"') {
                    ret.append('"');
                }
                ret.append(moji);
            }
            if (hasCamma) {
                ret.append("\"");
            }
        }
        return ret.toString();
    }
    
    
    
////////////////////////////////////////////////////////////////////////////////
//動作確認
////////////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) {
        String line = "ヲ,844,844,6662,助詞,格助詞,*,*,*,*,ヲ,を,ヲ,オ,ヲ,和,ヲ,オ,ヲ,ヲ,*,*,*,\"\"\"\",*,*,*,\"動詞%F2@0,名詞%F1,形容詞%F2@-1\",";
        String[] splittedLine = CsvLiner.split(line);
        String newLine = CsvLiner.join(splittedLine); 
        System.out.println(newLine);
    }
}
