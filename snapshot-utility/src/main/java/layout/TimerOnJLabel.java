package layout;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.Timer;

public class TimerOnJLabel extends JFrame {

    private static final long serialVersionUID = 1L;        
    long start = System.currentTimeMillis();
    long elapsedTimeMillis;
    int sec = 5;
    Timer timer;

    public TimerOnJLabel() {
        super("TooltipInSwing");
        setSize(400, 300);
        getContentPane().setLayout(new FlowLayout());
        final JLabel b1;
        final JRadioButton jrb = new JRadioButton();
        b1 = new JLabel("Simple tooltip 1");

        ActionListener timerTask = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTimeMillis = System.currentTimeMillis();
                b1.setText("Timer : " + (elapsedTimeMillis-start)/1000+" ::::: " +sec);
                System.out.println("Timer working: " + sec);
                if(--sec == 0){
                    timer.stop();
                    System.out.println("Timer Stopped");
                }
            }
        };
        timer = new Timer(1000, timerTask);
        System.out.println("Timer Started");
        timer.start();

        getContentPane().add(b1);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);          
        setVisible(true);
    }

    public static void main(String args[]){
        new TimerOnJLabel();
    }
}
