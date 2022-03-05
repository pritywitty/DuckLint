import javax.swing.*;

public class VerificationBox {
    String title;
    String message;

    VerificationBox(){
        message = "";
        title = "";
    }

    VerificationBox(String t, String m){
        message = m;
        title = t;
    }
    int getAnswer(){
        return JOptionPane.showOptionDialog(new JOptionPane(), message, title, JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.CANCEL_OPTION);
    }

}
