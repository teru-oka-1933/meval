package meval.parts;


import toolbox.ToolBox;


final public class Level {
    
    //動的なフィールド
    final public String strFieldNums;
    final public int[] fieldNumlList;
    
    
    //コンストラクタ
    public Level(String strFieldNums) {
        this.strFieldNums = strFieldNums; 
        String[] splittedStrFieldNums = strFieldNums.split("\\+");
        this.fieldNumlList = new int[splittedStrFieldNums.length];
        for (int i=0; i<splittedStrFieldNums.length; ++i) {
            String strFieldNum = splittedStrFieldNums[i];
            int fieldNum = Integer.parseInt(strFieldNum);
            if (fieldNum <= 0) {
                String message = ToolBox.lsep +
                                 "MeVal Original Exception !" + ToolBox.lsep +
                                 "  Field Numbers must be >=1" + ToolBox.lsep +
                                 " " + fieldNum + " is improper." +
                                 ToolBox.lsep;
                System.err.println(message);
                System.exit(0);
            } else {
                this.fieldNumlList[i] = fieldNum;
            }
        }
    }
    
    
}
