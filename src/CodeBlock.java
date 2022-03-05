import java.util.ArrayList;

public class CodeBlock {

    ArrayList<CodeBlock> subBlocks;


    class Statement {

        //block type : function , loop , conditional-then , comment
        //block has : sub-blocks , statement(s) , initial expression
            //statement type : variable declaration , print , assignment , input , T/F , include , namespace , comment
            //statement has : expression(s) , token(s)
                //expression type : comparator , declaration , assignment
                //expression has : sub-expressions , tokens
                    //token type : keyword , variable , literal , symbol

        class Parenthesis {
            Parenthesis innerParenthesis;
            Expression expression;

        }

        class Expression {
            Expression expression;

        }

    }

    class Variable {

    }

}
