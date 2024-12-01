import serializable.Message;
import serializable.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
// Класс панели чатов
public class ChatPanel extends JPanel {
    private final String myUser;
    private User toUser;
    private final JButton sendButton;
    private final JTextField messageField;
    private final MessagesPanel messagesPanel;
    private final JPanel sendMessagePanel;
    private final ObjectOutputStream out = Main.getOutputStream();
    private final ObjectInputStream in = Main.getInputStream();
    public ChatPanel(ChatFrame chatFrame) {
        this.myUser = chatFrame.myUser;
        messageField = new JTextField();
        sendButton = new JButton("Отправить");
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
            }
        });
        setLayout(new BorderLayout());
        // Настройка поля для сообщения и кнопки его отправки
        messageField.setPreferredSize(new Dimension(350, 30));
        sendButton.addActionListener(e -> sendMessage());
        sendMessagePanel = new JPanel();
        sendMessagePanel.setVisible(false);
        sendMessagePanel.add(messageField);
        sendMessagePanel.add(sendButton);
        sendMessagePanel.setBackground(new Color(41, 60, 48));
        add(sendMessagePanel, BorderLayout.SOUTH);
        messagesPanel = new MessagesPanel();
        JScrollPane scrollbar = new JScrollPane(messagesPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollbar, BorderLayout.CENTER);
    }
    // Метод для получения пользователя
    public User getToUser() {
        return toUser;
    }
    // Метод для установки необходимого пользователя
    public void setToUser(User toUser) {
        this.toUser = toUser;
        messagesPanel.setToUser(toUser);
    }
    // Метод для показа панели отправления
    public void activate() {
        sendMessagePanel.setVisible(true);
    }
    // Метод для получения сообщений с сервера
    public void getMessagesFromServer() {
        messagesPanel.getMessagesFromServer();
    }
    // Метод для отправления сообщений
    private void sendMessage(){
        try {
            String msg = messageField.getText();
            Message message = new Message(myUser, toUser.getUserName(), msg);
            out.write("sendMessage\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.writeObject(message);
            out.flush();
            messageField.setText("");
            Scanner sc = new Scanner(in);
            String response = sc.nextLine();
            if(!response.equals("OK")) return;
            messagesPanel.getMessages().add(message);
        } catch(IOException ex) {
            messageField.setText("");
        }
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }
}
// Класс панели сообщений
class MessagesPanel extends JPanel {
    private List<Message> messages;
    private User toUser;
    private final ObjectOutputStream out = Main.getOutputStream();
    private final ObjectInputStream in = Main.getInputStream();
    private final Color backColor = new Color(41, 60, 48);
    public MessagesPanel(){
        setLayout(new GridBagLayout());
        setBackground(backColor);
    }
    // Метод для получения сообщений с сервера
    public void getMessagesFromServer() {
        try {
            out.write("getMessagesInChat\n".getBytes(StandardCharsets.UTF_8));out.flush();
            out.writeObject(toUser.getUserID());out.flush();
            try {
                removeAll();
                messages = (List<Message>) in.readObject();
                int gridY = 0;
                for(Message message : messages) {
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.gridy = gridY;
                    c.anchor = GridBagConstraints.FIRST_LINE_START;
                    c.ipady = 10;
                    c.ipadx = 10;
                    c.insets = new Insets(5, 0, 5, 0);
                    gridY++;
                    if(toUser.getUserName().equals(message.getSenderName()) && !toUser.getUserName().equals(message.getReceiverName())) add(new MessagePanel(message, false), c);
                    else add(new MessagePanel(message, true), c);
                }
                revalidate();
                repaint();
            } catch(ClassNotFoundException e) {
                System.err.println("ERROR: ошибка получения результата getMessagesInChat");
            }
        } catch (IOException ie) {
            System.err.println("Исключение IOException: " + ie.getMessage());
        }
    }
    // Метод для установки пользователя
    public void setToUser(User toUser) {
        this.toUser = toUser;
    }
    // Метод для получения списка сообщений
    public List<Message> getMessages() {
        return messages;
    }
    // Внутренний класс: панель для отдельного сообщения
    class MessagePanel extends JPanel {
        public MessagePanel(Message msg, boolean canBeDeleted) {
            setBackground(backColor);
            String messageText = msg.getSenderName() + ": " + msg.getText();
            String allInformationString = getHtmlText(messageText);

            setLayout(new GridBagLayout());
            JLabel messageLabel = new JLabel(allInformationString);
            messageLabel.setForeground(Color.WHITE);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 3, 3, 5);
            c.gridx = 0;
            add(messageLabel, c);
            if(canBeDeleted) {
                DeleteButton deleteButton = new DeleteButton(msg);
                c.insets = new Insets(0, 0, 0, 0);
                c.gridx = 1;
                add(deleteButton, c);
            }
        }
        // Получение спарсенного варианта текста под панель сообщения
        public String getHtmlText(String text) {
            StringBuilder sb = new StringBuilder();
            String[] words = text.split(" ");
            sb.append("<html>");
            int lettersCount = 0;
            int maxLettersCount = 20;
            for(String word: words) {
                if((lettersCount + word.length() + 1) > maxLettersCount) {
                    sb.append("<br>");
                    lettersCount = 0;
                }
                sb.append(word).append(" ");
                lettersCount += word.length() + 1;
            }
            sb.append("</html>");
            return sb.toString();
        }
        // Внутренний класс внутреннего класса: кнопка удаления сообщений
        class DeleteButton extends JButton {
            public DeleteButton(Message msg) {
                super("X");
                setForeground(new Color(120, 0, 0));
                setPreferredSize(new Dimension(50, 30));
                setBorderPainted(false);
                setContentAreaFilled(false);
                // Добавление обработчика событий для удаления сообщения у пользователей
                addActionListener(e -> {
                    // Обработка нажатия
                    try {
                        out.write("deleteMessage\n".getBytes());out.flush();
                        out.writeObject(toUser.getUserID());out.flush();
                        out.writeObject(msg.getId());
                        Scanner sc = new Scanner(in);
                        String response = sc.nextLine();
                        if(!response.equals("OK")) return;
                        getMessagesFromServer();
                    } catch (IOException ex) {
                        System.out.println("Исключение" + ex.getMessage());
                    }
                });
            }
        }
        // Переопределённый метод для отрисовки сообщения
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.GRAY);
            g.fillRoundRect(0,0, getWidth() - 2, getHeight() - 2, 10, 10);
        }
    }
}