
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class QuizMaster extends Frame implements ActionListener {

    private final TextField nameField = new TextField(22);
    private final Choice topicChoice = new Choice();
    private final Label timerLabel = new Label("Time: 10:00");
    private final Label questionLabel = new Label("Question");
    private final Label progressLabel = new Label("Question 0 of 0");
    private final Label statusLabel = new Label("Welcome to Quiz Master!");

    private final CheckboxGroup answersGroup = new CheckboxGroup();
    private final Checkbox[] answerBoxes = new Checkbox[4];
    private final List questionList = new List(8, false);
    private final TextArea outputArea = new TextArea("", 12, 55,
                                                     TextArea.SCROLLBARS_VERTICAL_ONLY);

    private final Button startButton = new Button("Start Quiz");
    private final Button previousButton = new Button("Previous");
    private final Button nextButton = new Button("Next");
    private final Button submitButton = new Button("Submit Quiz");
    private final Button reviewButton = new Button("Review Answers");
    private final Button clearButton = new Button("Clear Selection");

    private final ArrayList<Question> allQuestions = new ArrayList<>();
    private final ArrayList<Question> quizQuestions = new ArrayList<>();

    private int[] selectedAnswers;
    private int currentQuestion = 0;
    private int timeLeft = 600;
    private boolean quizStarted = false;
    private boolean quizFinished = false;
    private Timer countdown;

    private final class Question {
        String topic;
        String text;
        String[] options;
        int correctAnswer;
        String explanation;

        Question(String topic, String text, String[] options,
                 int correctAnswer, String explanation) {
            this.topic = topic;
            this.text = text;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }
    }

    public QuizMaster() {
        super("Quiz Master - Java Programming");

        setSize(900, 620);
        setLayout(new BorderLayout(10, 10));

        buildQuestions();
        buildInterface();
        buildMenu();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopTimer();
                dispose();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
        setQuizControls(false);
    }

    private void buildInterface() {
        Panel topPanel = new Panel(new GridLayout(2, 1, 5, 5));

        Panel playerPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        playerPanel.add(new Label("Player Name:"));
        playerPanel.add(nameField);
        playerPanel.add(new Label("Topic:"));

        topicChoice.add("All Topics");
        topicChoice.add("Java Basics");
        topicChoice.add("OOP");
        topicChoice.add("Exceptions");
        playerPanel.add(topicChoice);
        playerPanel.add(startButton);
        playerPanel.add(timerLabel);

        Panel statusPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(progressLabel);
        statusPanel.add(statusLabel);

        topPanel.add(playerPanel);
        topPanel.add(statusPanel);
        add(topPanel, BorderLayout.NORTH);

        Panel centerPanel = new Panel(new BorderLayout(8, 8));
        centerPanel.setBackground(new Color(242, 246, 250));

        Panel questionPanel = new Panel(new GridLayout(5, 1, 5, 8));
        questionPanel.setBackground(new Color(242, 246, 250));

        questionLabel.setFont(new Font("Arial", Font.BOLD, 17));
        questionPanel.add(questionLabel);

        for (int i = 0; i < 4; i++) {
            answerBoxes[i] = new Checkbox("", answersGroup, false);
            answerBoxes[i].setFont(new Font("Arial", Font.PLAIN, 14));
            questionPanel.add(answerBoxes[i]);
        }

        centerPanel.add(questionPanel, BorderLayout.CENTER);

        Panel navigationPanel = new Panel(new FlowLayout());
        navigationPanel.add(previousButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(clearButton);
        navigationPanel.add(submitButton);
        navigationPanel.add(reviewButton);

        centerPanel.add(navigationPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        Panel sidePanel = new Panel(new BorderLayout(5, 5));
        sidePanel.add(new Label("Question Navigator",
                                Label.CENTER), BorderLayout.NORTH);
        sidePanel.add(questionList, BorderLayout.CENTER);

        Panel instructions = new Panel(new FlowLayout());
        instructions.add(new Label("Select a number to jump to a question."));
        sidePanel.add(instructions, BorderLayout.SOUTH);

        add(sidePanel, BorderLayout.EAST);

        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.add(outputArea);
        scrollPane.setPreferredSize(new Dimension(100, 140));
        add(scrollPane, BorderLayout.SOUTH);

        startButton.addActionListener(this);
        previousButton.addActionListener(this);
        nextButton.addActionListener(this);
        clearButton.addActionListener(this);
        submitButton.addActionListener(this);
        reviewButton.addActionListener(this);
        questionList.addActionListener(this);
    }

    private void buildMenu() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem startItem = new MenuItem("Start / Restart");
        MenuItem submitItem = new MenuItem("Submit Quiz");
        MenuItem exitItem = new MenuItem("Exit");

        startItem.addActionListener(e -> startQuiz());
        submitItem.addActionListener(e -> submitQuiz());
        exitItem.addActionListener(e -> {
            stopTimer();
            dispose();
        });

        fileMenu.add(startItem);
        fileMenu.add(submitItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        Menu helpMenu = new Menu("Help");
        MenuItem instructionsItem = new MenuItem("Instructions");
        MenuItem aboutItem = new MenuItem("About");

        instructionsItem.addActionListener(e ->
            showDialog("Instructions",
                "1. Enter your name.\n"
                + "2. Select a topic and click Start Quiz.\n"
                + "3. Select one answer for each question.\n"
                + "4. Use Previous, Next, or the question navigator.\n"
                + "5. Submit before the 10-minute timer ends.")
        );

        aboutItem.addActionListener(e ->
            showDialog("About Quiz Master",
                "Quiz Master\n"
                + "A Java AWT multiple-choice quiz application.\n"
                + "Created for Java Assessment-4.")
        );

        helpMenu.add(instructionsItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setMenuBar(menuBar);
    }

    private void buildQuestions() {
        allQuestions.add(new Question("Java Basics",
            "Which keyword is used to inherit a class in Java?",
            new String[]{"implements", "extends", "inherits", "instanceof"},
            1, "The extends keyword establishes class inheritance."));

        allQuestions.add(new Question("Java Basics",
            "Which method is the usual entry point of a Java application?",
            new String[]{"start()", "run()", "main()", "init()"},
            2, "The main method is the standard application entry point."));

        allQuestions.add(new Question("Java Basics",
            "Which type stores a true or false value?",
            new String[]{"int", "char", "boolean", "double"},
            2, "The boolean type stores true or false."));

        allQuestions.add(new Question("Java Basics",
            "Which symbol ends a typical Java statement?",
            new String[]{":", ";", ".", ","},
            1, "A semicolon terminates a typical Java statement."));

        allQuestions.add(new Question("OOP",
            "Which concept allows one class to acquire another class's members?",
            new String[]{"Encapsulation", "Inheritance", "Abstraction", "Compilation"},
            1, "Inheritance lets a subclass acquire accessible superclass members."));

        allQuestions.add(new Question("OOP",
            "Which keyword creates an object in Java?",
            new String[]{"class", "this", "new", "void"},
            2, "The new operator creates an object."));

        allQuestions.add(new Question("OOP",
            "Which feature means one interface can have multiple implementations?",
            new String[]{"Polymorphism", "Iteration", "Casting", "Packaging"},
            0, "Polymorphism allows a common interface to represent different implementations."));

        allQuestions.add(new Question("Exceptions",
            "Which block is commonly used to handle an exception?",
            new String[]{"check-catch", "try-catch", "do-catch", "if-catch"},
            1, "A try block can be followed by a catch block to handle exceptions."));

        allQuestions.add(new Question("Exceptions",
            "Which keyword explicitly throws an exception?",
            new String[]{"throws", "throw", "catch", "finally"},
            1, "The throw keyword explicitly throws an exception object."));

        allQuestions.add(new Question("Exceptions",
            "Which block is commonly used for cleanup after try/catch?",
            new String[]{"final", "finish", "finally", "last"},
            2, "A finally block is commonly used for cleanup code."));
    }

    private void startQuiz() {
        String playerName = nameField.getText().trim();

        if (playerName.isEmpty()) {
            showDialog("Missing Name", "Please enter your name before starting.");
            nameField.requestFocus();
            return;
        }

        stopTimer();

        quizQuestions.clear();
        String chosenTopic = topicChoice.getSelectedItem();

        for (Question q : allQuestions) {
            if (chosenTopic.equals("All Topics")
                    || q.topic.equals(chosenTopic)) {
                quizQuestions.add(q);
            }
        }

        if (quizQuestions.isEmpty()) {
            showDialog("No Questions", "No questions are available for this topic.");
            return;
        }

        selectedAnswers = new int[quizQuestions.size()];
        for (int i = 0; i < selectedAnswers.length; i++) {
            selectedAnswers[i] = -1;
        }

        currentQuestion = 0;
        timeLeft = 600;
        quizStarted = true;
        quizFinished = false;
        outputArea.setText("");
        statusLabel.setText("Good luck, " + playerName + "!");
        questionList.removeAll();

        for (int i = 0; i < quizQuestions.size(); i++) {
            questionList.add("Question " + (i + 1));
        }

        setQuizControls(true);
        displayQuestion();
        startTimer();
    }

    private void displayQuestion() {
        if (quizQuestions.isEmpty()) {
            return;
        }

        Question q = quizQuestions.get(currentQuestion);
        questionLabel.setText((currentQuestion + 1) + ". " + q.text);
        progressLabel.setText("Question " + (currentQuestion + 1)
                              + " of " + quizQuestions.size());

        answersGroup.setSelectedCheckbox(null);

        for (int i = 0; i < 4; i++) {
            answerBoxes[i].setLabel((char)('A' + i) + ". " + q.options[i]);
            answerBoxes[i].setState(selectedAnswers[currentQuestion] == i);
        }

        questionList.select(currentQuestion);
        previousButton.setEnabled(currentQuestion > 0);
        nextButton.setEnabled(currentQuestion < quizQuestions.size() - 1);
    }

    private void saveCurrentAnswer() {
        for (int i = 0; i < answerBoxes.length; i++) {
            if (answerBoxes[i].getState()) {
                selectedAnswers[currentQuestion] = i;
                return;
            }
        }
        selectedAnswers[currentQuestion] = -1;
    }

    private void moveQuestion(int step) {
        if (!quizStarted || quizFinished) {
            return;
        }

        saveCurrentAnswer();

        int nextIndex = currentQuestion + step;
        if (nextIndex >= 0 && nextIndex < quizQuestions.size()) {
            currentQuestion = nextIndex;
            displayQuestion();
        }
    }

    private void startTimer() {
        countdown = new Timer(true);

        countdown.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> {
                    if (!quizStarted || quizFinished) {
                        stopTimer();
                        return;
                    }

                    timeLeft--;
                    timerLabel.setText(String.format("Time: %02d:%02d",
                        timeLeft / 60, timeLeft % 60));

                    if (timeLeft <= 0) {
                        submitQuiz();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
    }

    private void submitQuiz() {
        if (!quizStarted || quizFinished) {
            return;
        }

        saveCurrentAnswer();

        int unanswered = 0;
        for (int answer : selectedAnswers) {
            if (answer == -1) {
                unanswered++;
            }
        }

        if (timeLeft > 0 && unanswered > 0) {
            showDialog("Unanswered Questions",
                "You have " + unanswered + " unanswered question(s).\n"
                + "You can continue or submit anyway.");
            // This method is called by the Submit button too.
            // Ask for confirmation only when questions remain unanswered.
            if (!confirmSubmission()) {
                return;
            }
        } else if (timeLeft > 0) {
            if (!confirmSubmission()) {
                return;
            }
        }

        quizFinished = true;
        quizStarted = false;
        stopTimer();

        int score = 0;
        for (int i = 0; i < quizQuestions.size(); i++) {
            if (selectedAnswers[i] == quizQuestions.get(i).correctAnswer) {
                score++;
            }
        }

        double percentage = score * 100.0 / quizQuestions.size();

        outputArea.setText("QUIZ RESULTS\n"
            + "============================\n"
            + "Player: " + nameField.getText().trim() + "\n"
            + "Topic: " + topicChoice.getSelectedItem() + "\n"
            + "Score: " + score + " / " + quizQuestions.size() + "\n"
            + String.format("Percentage: %.1f%%\n", percentage)
            + "Unanswered: " + unanswered + "\n"
            + "Performance: " + getPerformance(percentage) + "\n\n"
            + "Click Review Answers to see explanations.");

        statusLabel.setText("Quiz completed! Score: " + score
                            + "/" + quizQuestions.size());
        timerLabel.setText(timeLeft <= 0 ? "Time: 00:00" : timerLabel.getText());
        setQuizControls(false);
        reviewButton.setEnabled(true);

        showDialog("Quiz Completed",
            "Your score: " + score + " / " + quizQuestions.size()
            + "\nPercentage: " + String.format("%.1f%%", percentage)
            + "\n" + getPerformance(percentage)
            + "\nClick Review Answers for explanations.");
    }

    private boolean confirmSubmission() {
        final boolean[] confirmed = {false};

        Dialog dialog = new Dialog(this, "Confirm Submission", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(new Label("Are you sure you want to submit the quiz?"),
                   BorderLayout.CENTER);

        Panel buttons = new Panel(new FlowLayout());
        Button yesButton = new Button("Submit");
        Button noButton = new Button("Continue Quiz");

        yesButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        noButton.addActionListener(e -> dialog.dispose());

        buttons.add(yesButton);
        buttons.add(noButton);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setSize(360, 130);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return confirmed[0];
    }

    private String getPerformance(double percentage) {
        if (percentage >= 90) return "Excellent!";
        if (percentage >= 70) return "Very Good!";
        if (percentage >= 50) return "Good effort!";
        return "Keep practising!";
    }

    private void showReview() {
        if (!quizFinished) {
            showDialog("Not Available", "Finish the quiz before reviewing answers.");
            return;
        }

        StringBuilder review = new StringBuilder("ANSWER REVIEW\n");
        review.append("============================\n\n");

        for (int i = 0; i < quizQuestions.size(); i++) {
            Question q = quizQuestions.get(i);
            review.append("Q").append(i + 1).append(": ").append(q.text).append("\n");

            if (selectedAnswers[i] == -1) {
                review.append("Your answer: Not answered\n");
            } else {
                review.append("Your answer: ")
                    .append(q.options[selectedAnswers[i]]).append("\n");
            }

            review.append("Correct answer: ")
                .append(q.options[q.correctAnswer]).append("\n");
            review.append("Explanation: ")
                .append(q.explanation).append("\n\n");
        }

        outputArea.setText(review.toString());
    }

    private void setQuizControls(boolean enabled) {
        previousButton.setEnabled(enabled && currentQuestion > 0);
        nextButton.setEnabled(enabled && currentQuestion < quizQuestions.size() - 1);
        clearButton.setEnabled(enabled);
        submitButton.setEnabled(enabled);
        questionList.setEnabled(enabled);
        topicChoice.setEnabled(!enabled);
        nameField.setEditable(!enabled);
        startButton.setEnabled(!enabled);
        reviewButton.setEnabled(quizFinished);
        for (Checkbox box : answerBoxes) {
            box.setEnabled(enabled);
        }
    }

    private void showDialog(String title, String message) {
        Dialog dialog = new Dialog(this, title, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(new Label(message, Label.CENTER), BorderLayout.CENTER);

        Button okButton = new Button("OK");
        okButton.addActionListener(e -> dialog.dispose());

        Panel panel = new Panel();
        panel.add(okButton);
        dialog.add(panel, BorderLayout.SOUTH);
        dialog.setSize(430, 170);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == startButton) {
            startQuiz();
        } else if (source == previousButton) {
            moveQuestion(-1);
        } else if (source == nextButton) {
            moveQuestion(1);
        } else if (source == clearButton) {
            if (quizStarted && !quizFinished) {
                answersGroup.setSelectedCheckbox(null);
                selectedAnswers[currentQuestion] = -1;
            }
        } else if (source == submitButton) {
            submitQuiz();
        } else if (source == reviewButton) {
            showReview();
        } else if (source == questionList && quizStarted && !quizFinished) {
            saveCurrentAnswer();
            int selected = questionList.getSelectedIndex();
            if (selected >= 0) {
                currentQuestion = selected;
                displayQuestion();
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(QuizMaster::new);
    }
}
