import serializable.Message;
import serializable.User;
import javax.swing.Timer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

class ChatFrame extends JFrame {
    Socket socket = Main.getSocket();
    ObjectOutputStream out = Main.getOutputStream();
    ObjectInputStream in = Main.getInputStream();
    DefaultListModel<String> dlm = new DefaultListModel<String>();
    String myUser;
    JPanel userAndExitPanel;
    ChatPanel chatPanel;
    private Timer timer;
    private final Color backColor = new Color(49, 58, 68);
    public ChatFrame (String username) {
        myUser = username;
        setTitle("ErroriestMsg");
        setSize(1000, 1000);
        setResizable(false);
        userAndExitPanel = new JPanel(new BorderLayout());
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                try {
                    out.write("disconnect\n".getBytes());
                    out.flush();
                    out.close();
                    in.close();
                    socket.close();
                } catch (IOException ex) {
                    System.err.println("Исключение: " + ex.getMessage());
                }
                System.exit(0);
            }
        });
        setLayout(new GridLayout(1, 2, 0, 0)); // 1 строка, 2 столбца, отступы 0 пикселей
        // Создание и установка иконки на фрейм
        URL url = getClass().getResource("messengericon.png");
        ImageIcon icon = new ImageIcon(url);
        setIconImage(icon.getImage());
        try {
            out.write("getUsers\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
            List<User> users;
            try {
                users = (List<User>) in.readObject();
                for (User user:users) {
                    if(user.getUserName().equals(myUser)) dlm.add(0, "Избранное");
                    else dlm.add(0, user.getUserName());
                }
                JList<String> userList = new JList<String>(dlm);
                userList.setFont(new Font("Courier", Font.BOLD, 30));
                userList.addListSelectionListener(e -> {
                    if(e.getValueIsAdjusting()) {
                        return;
                    }
                    String username1 = userList.getSelectedValue();
                    User necessaryUser = null;
                    if(username1.equals("Избранное")) username1 = myUser;
                    for (User user:users) {
                        if (user.getUserName().equals(username1)) {
                            necessaryUser = user;
                            break;
                        }
                    }
                    chatPanel.setToUser(necessaryUser);
                    chatPanel.getMessagesFromServer();
                    chatPanel.activate();
                });
                JButton exitButton = new JButton("Выйти из аккаунта");
                exitButton.setForeground(new Color(142, 124, 46));
                exitButton.setBackground(new Color(210, 210, 210));
                exitButton.addActionListener(e -> {
                    timer.stop();
                    disconnectFromServer();
                    JOptionPane.showMessageDialog(ChatFrame.this, "Успешный выход из аккаунта!!");
                    new AuthorizationForm();
                    dispose();
                });
                userList.setBackground(backColor);
                userList.setForeground(Color.WHITE);
                userAndExitPanel.add(userList);
                userAndExitPanel.add(exitButton, BorderLayout.SOUTH);
                userAndExitPanel.setBackground(backColor);
                chatPanel = new ChatPanel(this);
                add(userAndExitPanel);
                add(chatPanel);
            } catch(ClassNotFoundException e) {
                System.err.println("ERROR: ошибка получения результата getUsers");
                return;
            }
        }
        catch (IOException e) {
            System.out.println("Ошибка установки соединения: " + e.getMessage());
        }
        setLocationRelativeTo(null); // Центрирование окна
        setVisible(true);
        timer = new Timer(1000, e -> {
            if (chatPanel.getToUser() == null) return;
            chatPanel.getMessagesFromServer();
            chatPanel.repaint();
        });
        timer.start();
    }
    // Метод для отключения от сервера
    private void disconnectFromServer() {
        try {
            out.write("disconnect\n".getBytes());
            out.flush();
            // Попытка закрытия потоков ввода-вывода
            in.close();
            out.close();
            socket.close();
        } catch(IOException e) {
            System.out.println("Исключение: " + e.getMessage()); // Иначе вывод исключения
        }
    }
}