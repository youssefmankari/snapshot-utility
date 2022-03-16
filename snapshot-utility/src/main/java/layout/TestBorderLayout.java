package layout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Color;


public class TestBorderLayout 
{

    public static void main(String[] args) 
    {
        new TestBorderLayout();
    }

    TestBorderLayout()
    {
        JFrame frame = new JFrame("MyFrame");
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        JLabel left = new JLabel("LEFT");
        Border redline = BorderFactory.createLineBorder(Color.red);
		
        JLabel right = new JLabel("RIGHT");
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(redline);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JLabel("Another dummy Label"), BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}