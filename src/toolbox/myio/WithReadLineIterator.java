package toolbox.myio;

import java.io.*;
import java.util.Iterator;

class WithReadLineIterator implements Iterator<String> {

    //動的なフィールド
    protected String line;
    protected BufferedReader reader;
        
    
    //コンストラクタ
    public WithReadLineIterator(String fileName) {
        //ファイルを開く
        try {
            this.reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(fileName), "utf-8"));
        } catch(IOException e){
            System.err.println("");
            e.printStackTrace(System.err);
            System.exit(0);
        }
        //1行読み込む
        try {
            this.line = reader.readLine();
        } catch(IOException e){
            System.err.println("");
            e.printStackTrace(System.err);
            this.close();
            System.exit(0);
        }
    }
    
    
    @Override
    public boolean hasNext(){
        if (this.line == null) {
            this.close();
            return false; 
        } else {
            return true;
        }
    }
    
    
    @Override
    public String next(){
        String ret = this.line; 
        try {
            this.line = this.reader.readLine();
        }  catch(IOException e){
            System.err.println("");
            e.printStackTrace(System.err);
            this.close();
            System.exit(0);
        }
        return ret;
    }
    
    
    @Override
    public void remove(){}
    
    
    private void close(){
        try {
            this.reader.close();
        }  catch(IOException e){
            System.err.println("");
            e.printStackTrace(System.err);
            System.exit(0);
        }
    }
        
    
}
