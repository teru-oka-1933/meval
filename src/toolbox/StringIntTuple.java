package toolbox;


final public class StringIntTuple implements Comparable<StringIntTuple> {
    
    //動的なフィールド
    final public String str;
    final public int i;
    
    
    //コンストラクタ
    public StringIntTuple(String str, int i) {
        this.str = str;
        this.i = i;
    }
    
    
    //Collections.sortでiの降順になるように設定
    @Override
    public int compareTo(StringIntTuple target) {
        if (this.i == target.i) {
            return 0;
        } else if(this.i < target.i) {
            return 1;
        } else {
            return -1;
        }
    }
    
    
}
