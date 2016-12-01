package toolbox.myio;


import java.io.*;


public final class WithWriter {

    //動的なフィールド
    private BufferedWriter writer;
    
    
    //コンストラクタ
    public WithWriter(String fileName){
        try {
            this.writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(fileName), "utf-8"));
        } catch (IOException e) {
            System.err.println("");
            e.printStackTrace(System.err);
            System.exit(0);
        }
    }
    
    
    public void write(String str){
        try {
            this.writer.write(str);
        } catch (IOException e) {
            System.err.println("");
            e.printStackTrace(System.err);
            this.close();
            System.exit(0);
        }
    }
    
    
    public void writeLine(String str){
        try {
            this.writer.write(str+"\n");
        } catch (IOException e) {
            System.err.println("");
            e.printStackTrace(System.err);
            this.close();
            System.exit(0);
        }
    }
    
    
    public void flush(){
        try {
            this.writer.flush();
        } catch (IOException e) {
            System.err.println("");
            e.printStackTrace(System.err);
            this.close();
            System.exit(0);
        }  
    }
    
    
    public void close(){
        try {
            this.writer.close();
        } catch (IOException e) {
            System.err.println("");
            e.printStackTrace(System.err);
            System.exit(0);
        }  
    }

////////////////////////////////////////////////////////////////////////////////
//動作確認
////////////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) {
        String fileName = "withcodecsopenTest.txt";
        WithWriter writer = new WithWriter(fileName);
        for (Integer i=0; i<5; i++){
            writer.writeLine(i.toString());
        }
        for (Integer i=5; i<10; i++){
            writer.write(i.toString()+"\r\n");
        }
        writer.write("END.");
        writer.close();
    }
    
    
}
