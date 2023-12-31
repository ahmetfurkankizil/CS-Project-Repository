package Main;

import SignupAndLogin.LoginFrame;
import SignupAndLogin.SignUpHandler;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SignUpHandler sh = new SignUpHandler();
        LoginFrame loginFrame = new LoginFrame(sh);
        loginFrame.setTitle("BilkenTogether");
        loginFrame.setIconImage(new ImageIcon("src/HomePages/HomeMain/logo.PNG").getImage());
        loginFrame.setSize(500,300);
        loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
    }
}