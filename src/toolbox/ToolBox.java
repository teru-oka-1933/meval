package toolbox;


final public class ToolBox {
    
    
    //静的なフィールド
    static final public String singleLine = "----------------------------------------";
    static final public String doubleLine = "========================================";
    static final public String lsep = System.lineSeparator(); 
 
    
    //F値の計算
    static public float calcF(float prec, float rec) {
        if (prec == 0 && rec == 0) { return 0.0f; }
        else { return (2f*prec*rec) / (prec+rec); }
    }
    
    
    //floatを小数点以下2桁で四捨五入
    static public float round2d(float val) {
        val = val * 100f;
        int tmpVal = Math.round(val);
        return (float)tmpVal / 100f;
    }
    
    
    //フィールド同士を比較
    static public boolean fEquals(String field1, String field2, boolean isSurf) {
        if (isSurf) {
            if (field1.equals(field2)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (field1.equals(field2)) {
                return true;
            } else if ( (field1.equals("*") && field2.equals("")) ||
                        (field1.equals("") && field2.equals("*")) ) {
                return true;
            } else {
                return false;
            }
        }
    }

}
