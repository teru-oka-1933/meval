package toolbox.myio;


import java.util.Iterator;


public final class WithReadLine implements Iterable<String> {
    
    //動的なフィールド
    final protected String fileName;
        
    
    //コンストラクタ
    public WithReadLine(String fileName){
        this.fileName = fileName;
    }
    

    @Override
    public Iterator iterator() {
        return new WithReadLineIterator(this.fileName);
    }

    
////////////////////////////////////////////////////////////////////////////////
//動作確認
////////////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) {
        String fileName = "withcodecsopenTest.txt";
        for (String line : new WithReadLine(fileName)){
            System.out.print(line+"\n");
        }
    }
    
    
}
