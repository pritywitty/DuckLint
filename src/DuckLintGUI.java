import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Scanner;

import static javax.swing.JOptionPane.*;

public class DuckLintGUI extends JFrame {

    JPanel mainPanel = new JPanel(), programEntryPanel = new JPanel(), sidePanel = new JPanel(), buttonPanel = new JPanel();
    JButton backButton = new JButton("<"), goButton = new JButton("Start"), nextButton = new JButton(">");
    JTextArea mainText = new JTextArea(), sideText = new JTextArea("Progress appears here...");
    String text = "";
    Highlighter highlighter;

    DuckLintGUI(){
        setSize(1100,800);
        addTextPanel(programEntryPanel, mainText,900,700);
        addButtons(sidePanel, buttonPanel);
        addTextPanel(sidePanel, sideText, 200,700);
        sideText.setEditable(false);
        mainPanel.add(programEntryPanel);
        mainPanel.add(sidePanel);
        add(mainPanel);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
    }

    void addTextPanel(JPanel jPanel, JTextArea addThis, int wid_th, int hei_ght){
        addThis.setPreferredSize(new Dimension(wid_th,hei_ght));
        addThis.setLineWrap(true);
        jPanel.add(addThis);
    }

    void addButtons(JPanel jPanel, JPanel jPanelToAdd){
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanelToAdd.setLayout(new BoxLayout(jPanelToAdd, BoxLayout.X_AXIS));
        goButton.addActionListener(g -> {
            String sideTextSet;
            boolean quitCheck;
            StringBuilder stringBuilder = new StringBuilder();
            highlighter = mainText.getHighlighter();
            text = mainText.getText();
            ParseText parseText = new ParseText(text);
            for (ParseText.OneLine line : parseText.linesOfCodeInTypes) { stringBuilder.append(line.textOfLine); }
            mainText.setText(stringBuilder.toString());
            ParseText.OneLine.lineType
                    coType = ParseText.OneLine.lineType.code,
                    stType = ParseText.OneLine.lineType.string_literal,
                    chType = ParseText.OneLine.lineType.char_literal,
                    hType = ParseText.OneLine.lineType.header,
                    iType = ParseText.OneLine.lineType.include,
                    dType = ParseText.OneLine.lineType.define,
                    lcType = ParseText.OneLine.lineType.line_comment,
                    bcType = ParseText.OneLine.lineType.block_comment;
            // Syntax Check
            try {
                quitCheck = syntaxCheck(lcType, bcType, Color.yellow, parseText,
                        "Comments", "Are all comments and nothing else highlighted yellow?");
                // comments check

                if(!quitCheck){
                    quitCheck = syntaxCheck(chType, stType, Color.CYAN, parseText,
                            "Literally", "Are all string and character literals and nothing else highlighted blue?");
                } // literal check

                if(!quitCheck){
                    quitCheck = syntaxCheck(hType, coType, Color.lightGray, parseText,
                            "Code", "Are all headers and code and nothing else highlighted in gray?");
                } // code check

                if(!quitCheck){
                    highlighter.removeAllHighlights();
                    sideTextSet = "Syntax check complete...\n\n";
                    sideText.setText(sideTextSet);
                }

                else {
                    sideTextSet = "---Syntax error identified---\n\n";
                    sideText.setText(sideTextSet);
                } // Syntax Check

            }
            catch (BadLocationException e) {
                JOptionPane.showMessageDialog(new JOptionPane(), "Unknown Highlight Error.");
            }

            // find imported libraries

            // find reserved words
            // find special characters

            // find named variables
            // find numeric literals

            // identify expressions, sub-expressions, statements, conditionals, loops
            // identify scope of variables


        }); // goButton.addActionListener

        nextButton.addActionListener(n -> {
            sideText.setText("One line\nnext line");
            System.out.println("Next button clicked\n");
        });
        backButton.addActionListener(b -> {
            System.out.println("Back button clicked");
        });

        jPanelToAdd.add(backButton);
        jPanelToAdd.add(goButton);
        jPanelToAdd.add(nextButton);
        jPanel.add(jPanelToAdd);
    }

    boolean syntaxCheck(ParseText.OneLine.lineType typeI, ParseText.OneLine.lineType typeII, Color c, ParseText p,
                        String title, String message) throws BadLocationException {
        int goodResponse;
        highlighter.removeAllHighlights();
        for (ParseText.OneLine line : p.linesOfCodeInTypes) {
            if (line.typeOfLine == typeI || line.typeOfLine == typeII) {
                highlighter.addHighlight(line.startIndex, line.endIndex,
                        new DefaultHighlighter.DefaultHighlightPainter(c));
            }
            if(typeI == ParseText.OneLine.lineType.code){
                if (line.typeOfLine == ParseText.OneLine.lineType.define ||
                        line.typeOfLine == ParseText.OneLine.lineType.include) {
                    highlighter.addHighlight(line.startIndex, line.endIndex,
                            new DefaultHighlighter.DefaultHighlightPainter(c));
                }

            }
        }
        goodResponse = new VerificationBox(title, message).getAnswer();
        switch(goodResponse){
            case YES_OPTION: return false;
            case NO_OPTION:
                goodResponse = new VerificationBox(title, "Was something not highlighted?").getAnswer();

                if(goodResponse == CANCEL_OPTION){ return true; }
                if(goodResponse == YES_OPTION){ return true; }

                for (ParseText.OneLine line : p.linesOfCodeInTypes) {
                    highlighter.removeAllHighlights();
                    if (line.typeOfLine == typeI || line.typeOfLine == typeII) {
                        highlighter.addHighlight(line.startIndex, line.endIndex,
                                new DefaultHighlighter.DefaultHighlightPainter(c));
                        goodResponse = new VerificationBox(title, "Is this line correct?").getAnswer();
                        if(goodResponse == CANCEL_OPTION){ return true; }
                        else if(goodResponse == NO_OPTION){
                            JOptionPane.showMessageDialog(
                                    new JOptionPane(), "Correct highlighted syntactical error and restart.");
                            //TODO : allow user to identify the type of line highlighted
                            return true;
                        }
                    }
                }

                for (ParseText.OneLine line : p.linesOfCodeInTypes) {
                    if (line.typeOfLine == typeI || line.typeOfLine == typeII) {
                        highlighter.addHighlight(line.startIndex, line.endIndex,
                                new DefaultHighlighter.DefaultHighlightPainter(c));
                    }
                }

                goodResponse = new VerificationBox(title, message).getAnswer();

                switch (goodResponse){
                    case YES_OPTION: return false;
                    case NO_OPTION:
                        JOptionPane.showMessageDialog(new JOptionPane(), "Unknown Syntax Error.");
                        return true;
                    case CANCEL_OPTION: return true;
                    default: return true;
                }

            case CANCEL_OPTION: return true;
            default: return true;
        }
    } // syntaxCheck

    public static void main(String[] args){ new DuckLintGUI(); }
}

/*
switch (line.typeOfLine) {
    case header:
        highlighter.addHighlight(line.startIndex,line.endIndex,new DefaultHighlighter.DefaultHighlightPainter(Color.MAGENTA));
        break;
    case code:
        //highlighter.addHighlight(line.startIndex,line.endIndex,new DefaultHighlighter.DefaultHighlightPainter(Color.lightGray));
        break;
    case line_comment:
        highlighter.addHighlight(line.startIndex,line.endIndex,new DefaultHighlighter.DefaultHighlightPainter(Color.green));
        break;
    case block_comment:
        highlighter.addHighlight(line.startIndex,line.endIndex,new DefaultHighlighter.DefaultHighlightPainter(Color.yellow));
        break;
    case string_literal:
        highlighter.addHighlight(line.startIndex,line.endIndex,new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN));
        break;
    case char_literal:
        highlighter.addHighlight(line.startIndex,line.endIndex,new DefaultHighlighter.DefaultHighlightPainter(Color.PINK));
        break;
}
* */
