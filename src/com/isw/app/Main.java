package com.isw.app;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;

public class Main {
  private static String name = Main.class.getName();
  private static Logger logger = Logger.getLogger(name);

  public static void main(String[] args) {
    JFrame frame = new JFrame("Swing Testing!");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setLayout(null);
    frame.setSize(500, 500);
    frame.setResizable(false);

    JButton button = new JButton("Click Me");
    button.setBounds(200, 200, 100, 50);
    
    button.setFocusPainted(false);
    button.addActionListener(Main::onClick);
    
    frame.add(button);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private static void onClick(ActionEvent e) {
    logger.info("Button was clicked.");
  }
}
