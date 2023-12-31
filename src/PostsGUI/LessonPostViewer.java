package PostsGUI;

import HomePages.HomeMain.HomeMain;
import HomePages.HomeMain.MainInterface;
import Posts.LessonPost;
import Request.RequestsAndViewers.AcceptedRequest;
import Request.RequestsAndViewers.DeniedRequest;
import Request.RequestsAndViewers.Request;
import Request.RequestsAndViewers.UnansweredRequest;
import UserProfileGUI.PPImageHandler;
import UserRelated.Student;
import UserRelated.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class LessonPostViewer extends PostViewer {
    private JLabel typeLabel;
    private JLabel topicLabel;
    private ArrayList<DayButtons> dayButtonsList;
    private LessonPost lesPost;
    private JButton requestButton;
    private User sender;



    public LessonPostViewer(LessonPost p, MainInterface main) {
        super(main);
        this.lesPost = p;
        setUp();
        contentSetUp();

        g.fill = GridBagConstraints.NONE;
        g.gridy += 1;
        g.gridx = 1;
        setUpInformationPanel();

        g.gridy += 1;
        g.gridx = 1;
        addDaysWithBinaryBoolean();
        setUpDays();
    }

    public void setUp() {
        super.setUp();
        proPhoto = new PPImageHandler(lesPost.getSender());
        sender = lesPost.getSender();
        requestButton = new JButton("Send Request");
        if (main.getCurrentUser().getId() == sender.getId()) {
            requestButton.setVisible(false);
        }

    }

    @Override
    public LessonPost getPost() {
        return lesPost;
    }

    public void contentSetUp() {

        proName.setText(lesPost.getSender().getName());
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(10, 0, 0, 10);
        add(proPhoto, g);

        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.ipady = 0;
        g.insets = new Insets(10, 0, 10, 0);
        add(proName, g);

        g.gridy = 1;
        g.insets = new Insets(0, 0, 0, 0);
        g.fill = GridBagConstraints.BOTH;
        textArea2.setText(lesPost.getPostDescription());
        add(textArea2, g);
        Request userRequest = lesPost.isUserInRequests((Student) main.getCurrentUser());
        if ( userRequest != null){
            if (userRequest instanceof UnansweredRequest){
                requestButton.setText("Request Sent!");
                requestButton.setBackground(new Color(228, 232, 183));
                requestButton.setOpaque(true);
                requestButton.setBorder(new EmptyBorder(5,5,5,5));
                requestButton.setEnabled(false);
            }else if (userRequest instanceof DeniedRequest){
                requestButton.setText("Request Denied!");
                requestButton.setEnabled(false);
                requestButton.setBackground(new Color(200,130,130));
                requestButton.setOpaque(true);
                requestButton.setBorder(new EmptyBorder(5,5,5,5));
            } else if (userRequest instanceof AcceptedRequest){
                requestButton.setText("Request Accepted!");
                requestButton.setEnabled(false);
                requestButton.setBackground(new Color(129, 161, 231));
                requestButton.setOpaque(true);
                requestButton.setBorder(new EmptyBorder(5,5,5,5));
            }
        } else {
            requestButton.addActionListener(new RequestActionListener(lesPost,(Student) main.getCurrentUser()));
        }
    }

    public void setUpInformationPanel() {
        g.anchor = GridBagConstraints.WEST;
        messageLabel = new JLabel(envelope);

        topInformationPanel.add(commentLabel);
        topInformationPanel.add(messageLabel);

        // will insert p.getType() here
        typeLabel = new JLabel(lesPost.getRequestType() ? "LESSON REQUEST" : "LESSON GIVE");
        typeLabel.setBackground(informationBackground);
        typeLabel.setOpaque(true);
        addPadding(typeLabel);
        topInformationPanel.add(typeLabel);

        // will insert p.getTopic() here
        topicLabel = new JLabel(lesPost.getTypeFilter());
        topicLabel.setBackground(informationBackground);
        addPadding(topicLabel);
        topicLabel.setOpaque(true);
        topInformationPanel.add(topicLabel, g);


        topInformationPanel.add(requestButton);
        add(topInformationPanel, g);

    }

    public void setUpDays() {
        bottomIformationPanel = new JPanel();
        for (DayButtons d :
                dayButtonsList) {
            d.removeActionListener(d);
            bottomIformationPanel.add(d);
        }
        add(bottomIformationPanel, g);

    }


    private void addDaysWithBinaryBoolean() {
        int binaryboolean = lesPost.getDateBinaryBoolean();

        dayButtonsList = new ArrayList<>();
        boolean[] daysAvailable = lesPost.getDaysAvailable();
        String day = "";
        for (int i = 0; i < daysAvailable.length; i++) {
            if (daysAvailable[i]) {
                switch (i) {
                    case 0:
                        day = "Monday";
                        break;
                    case 1:
                        day = "Tuesday";
                        break;
                    case 2:
                        day = "Wednesday";
                        break;
                    case 3:
                        day = "Thursday";
                        break;
                    case 4:
                        day = "Friday";
                        break;
                    case 5:
                        day = "Saturday";
                        break;
                    case 6:
                        day = "Sunday";
                        break;
                }
                dayButtonsList.add(new DayButtons(day));
            }
        }


    }

    public LessonPost getLesPost() {
        return lesPost;
    }

    public static class DayButtons extends GeneralButton {
        public static final Font daysButtonFont = new Font("Times New Roman",Font.PLAIN,10);
        public DayButtons(String str){
            super(str);
            setFont(daysButtonFont);
            setOpaque(true);
            setForeground(Color.black);

        }

    }

    public static class GeneralButton extends JButton implements ActionListener {
        public GeneralButton(String str){
            super(str);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof DayButtons) {
                setSelected(!isSelected());
            }
        }

    }
}
