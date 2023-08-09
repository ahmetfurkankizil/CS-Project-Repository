package Request.RequestsAndViewers;

import Posts.LessonPost;
import UserRelated.Student;

import javax.swing.*;

public class RequestMiddlePanelUnanswered {
    private JPanel panel1;
    private JPanel buttonPanel;
    private JLabel requestLabel;
    private JPanel middlePanel;
    private JButton unansweredButton;
    private JButton deniedButton;
    private JPanel insideScrollPanel;
    private JButton acceptedButton;
    private JButton backButton;
    public RequestMiddlePanelUnanswered(){
        Student tutor = new Student("Tutor", null, 22203112, null, null, null, null);

        Student student1 = new Student("Jack", null, 10, null, null, null, null);
        Student student2 = new Student("Saul", null, 10, null, null, null, null);
        Student student3 = new Student("Heisenberg", null, 10, null, null, null, null);
        student1.setAverageRating(2);
        student2.setAverageRating(3);
        student3.setAverageRating(4);

        LessonPost lpTest1 = new LessonPost(101, tutor, null, null, 1, true, null);
        LessonPost lpTest2 = new LessonPost(102, tutor, null, null, 1, true, null);
        LessonPost lpTest3 = new LessonPost(103, tutor, null, null, 1, true, null);
        //lpTest1.addRequest(student1);
        //lpTest2.addRequest(student2);
        //lpTest3.addRequest(student3);
        lpTest2.acceptRequest(student2);
        lpTest3.denyRequest(student3);

        for (int i = 0; i < tutor.pullTheRequestsFromDB().size(); i++) {
            Request request = tutor.pullTheRequestsFromDB().get(i);
            if (request instanceof UnansweredRequest) {
                insideScrollPanel.add(new UnansweredViewer(request));
            }
        }
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("RequestMiddlePanel");
        frame.setContentPane(new RequestMiddlePanelUnanswered().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}