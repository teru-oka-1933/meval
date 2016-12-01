package meval;

import java.util.Scanner;
import org.apache.commons.cli.*;
import toolbox.ToolBox;


final public class PropTest {
    
    //静的なフィールド    
    static private boolean prec_flag = false;
    static private boolean rec_flag = false;

       
    //コマンドライン引数を処理するメソッド
    static private void readOption(String[] args) throws Exception {
        
        //オプション定義
        Options options = new Options();
        //precisionの検定を実行する
        options.addOption("p", "prec", false, "Test only precision (Default: Precsion & Recall)");
        //recallの検定を実行する
        options.addOption("r", "rec", false, "Test only recall (Default: Precsion & Recall)");
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
            hf.printHelp("meval.PropTest [options]", options);
            System.out.println("");
            System.exit(0);
        }
        //-p --prec (任意)
        if (cl.hasOption("p") || cl.hasOption("prec")) {
            PropTest.prec_flag = true;
        }
        //-r --rec (任意)
        if (cl.hasOption("r") || cl.hasOption("rec")) {
            PropTest.rec_flag = true;
        }
        
    }
    
    
    //検定統計量Zの計算
    static private float calcZ(float cor1, float n, float cor2, float m) {
        float p = (cor1 + cor2) / (n + m);
        float p1 = cor1 / n;
        float p2 = cor2 / m;
        float z = (p1-p2) / (float) Math.sqrt( p * (1f-p) * (1/n + 1/m) );
        z = (z < 0)? -z: z;
        return z;
    }
    
    
    //Zが統計的に有意か否か表示
    static private void displaySigOrNot(float Z) {
        
        System.out.println("Both-Side Test");
        System.out.println("  Significance Level:");
        System.out.print("    0.05 (5%): ");
        if (Z >= 1.960f) {
            System.out.println("SIGNIFICANT (Z >= 1.960)");
        } else {
            System.out.println("NOT SIGNIFICANT (Z < 1.960)");
        }
        System.out.print("    0.01 (1%): ");
        if (Z >= 2.576f) {
            System.out.println("SIGNIFICANT (Z >= 2.576)");
        } else {
            System.out.println("NOT SIGNIFICANT (Z < 2.576)");
        }
        System.out.println("One-Side Test");
        System.out.println("  Significance Level:");
        System.out.print("    0.05 (5%): ");
        if (Z >= 1.645f) {
            System.out.println("SIGNIFICANT (Z >= 1.645)");
        } else {
            System.out.println("NOT SIGNIFICANT (Z < 1.645)");
        }
        System.out.print("    0.01 (1%): ");
        if (Z >= 2.326f) {
            System.out.println("SIGNIFICANT (Z >= 2.326)");
        } else {
            System.out.println("NOT SIGNIFICANT (Z < 2.326)");
        }
    }
    
    
    //Main文
    public static void main(String[] args) throws Exception {
        
        //args = "-h".split(" ");
        
        //タイトル表示
        System.out.println("");
        System.out.println(ToolBox.doubleLine);
        System.out.println("MEVAL PROP TEST");
        System.out.println(ToolBox.doubleLine);
        
        //コマンドライン引数の読み込み
        PropTest.readOption(args);
        
        //読み込んだ引数の確認
        System.out.print("Test ");
        if (PropTest.prec_flag == PropTest.rec_flag) {
            System.out.println("Precision and Recall");
        } else if (PropTest.prec_flag) {
            System.out.println("Precision");
        } else {
            System.out.println("Recall");
        }
   
        
        System.out.println(ToolBox.singleLine);
        
        //インタラクティブに数値を読み込み
        Scanner stdIn = new Scanner(System.in);
        
        System.out.println("Method 1:");
        System.out.print(" COR> ");
        float cor1 = stdIn.nextFloat();
        float gld1 = Float.NaN;
        if ( (PropTest.prec_flag==PropTest.rec_flag) || PropTest.prec_flag ) {
            System.out.print(" GLD> ");
             gld1 = stdIn.nextFloat();
        }
        float sys1 = Float.NaN;
        if ( (PropTest.prec_flag==PropTest.rec_flag) || PropTest.rec_flag ) {
            System.out.print(" PRD> ");
             sys1 = stdIn.nextFloat();
        }
      
        System.out.println("Method 2:");
        System.out.print(" COR> ");
        float cor2 = stdIn.nextFloat();
        float gld2 = Float.NaN;
        if ( (PropTest.prec_flag==PropTest.rec_flag) || PropTest.prec_flag ) {
            System.out.print(" GLD> ");
             gld2 = stdIn.nextFloat();
        }
        float sys2 = Float.NaN;
        if ( (PropTest.prec_flag==PropTest.rec_flag) || PropTest.rec_flag ) {
            System.out.print(" PRD> ");
             sys2 = stdIn.nextFloat();
        }
        
        //結果を表示
        ////precision
        if ( (PropTest.prec_flag==PropTest.rec_flag) || PropTest.prec_flag ) {
            System.out.println(ToolBox.doubleLine);
            System.out.println("Precision");
            System.out.println(ToolBox.doubleLine);
            float prec1 = cor1 / sys1;
            float prec2 = cor2 / sys2;
            System.out.println("Method 1: " + prec1 + 
                               " (" + (int)cor1 + "/" + (int)sys1 + ")");
            System.out.println("Method 2: " + prec2 + 
                               " (" + (int)cor2 + "/" + (int)sys2 + ")");
            System.out.println(ToolBox.singleLine);
            float Z = PropTest.calcZ(cor1, sys1, cor2, sys2);
            System.out.println("Z: " + Z);
            System.out.println(ToolBox.singleLine);
            PropTest.displaySigOrNot(Z);
        }
        ////recall
        if ( (PropTest.prec_flag==PropTest.rec_flag) || PropTest.rec_flag ) {
            System.out.println(ToolBox.doubleLine);
            System.out.println("Recall");
            System.out.println(ToolBox.doubleLine);
            float rec1 = cor1 / gld1;
            float rec2 = cor2 / gld2;
            System.out.println("Method 1: " + rec1 + 
                               " (" + (int)cor1 + "/" + (int)gld1 + ")");
            System.out.println("Method 2: " + rec2 +
                               " (" + (int)cor2 + "/" + (int)gld2 + ")");
            System.out.println(ToolBox.singleLine);
            float Z = PropTest.calcZ(cor1, gld1, cor2, gld2);
            System.out.println("Z: " + Z);
            System.out.println(ToolBox.singleLine);
            PropTest.displaySigOrNot(Z); 
        }
        
        System.out.println("");
        
    }
    
}
