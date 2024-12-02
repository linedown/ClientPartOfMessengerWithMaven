import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

public class AuthorizationForm extends JFrame {
    ObjectOutputStream out;
    ObjectInputStream in;
    Socket socket;
    private JTextField usernameField;
    private JPasswordField passwordField;
    boolean failConnection = false;
    public AuthorizationForm() {
        // Задание необходимых настроек фрейму
        setTitle("Окно авторизации");
        setSize(500, 200);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1, 10, 10)); // 3 строки, 1 столбец, но как бы 2, отступы 10 пикселей
        getContentPane().setBackground(new Color(190, 190, 190));
        // Создание и установка иконки на фрейм
        URL url = getClass().getResource("loginicon.png");
        ImageIcon icon = new ImageIcon(url);
        setIconImage(icon.getImage());
        // Добавление надписей и полей ввода
        JLabel nameLabel = new JLabel("Имя пользователя:");
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        add(nameLabel);
        usernameField = new JTextField();
        add(usernameField);
        // Метка "пароль" и её настройка
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setHorizontalAlignment(JLabel.CENTER);
        add(passwordLabel);
        passwordField = new JPasswordField();
        add(passwordField);
        // Кнопка-текст "Создать пользователя" и её настройка
        JButton createUserButton = new JButton("Создать пользователя");
        createUserButton.setBorderPainted(false);
        createUserButton.setContentAreaFilled(false);
        createUserButton.setForeground(new Color(179, 34, 34));
        createUserButton.addActionListener(e -> {
            new RegistrationForm(); // Открытие регистрационной формы
            dispose(); //Закрытие текущей формы
        });
        add(createUserButton);
        // Кнопка "Вход"
        JButton loginButton = new JButton("Вход");
        loginButton.addActionListener(e -> {
            checkLogin();
        });
        // Обработчики события от клавиши ENTER на клавиатуры для каждой текстовой панели
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) checkLogin();
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) checkLogin();
            }
        });
        loginButton.setBackground(new Color(49, 58, 68));
        loginButton.setForeground(Color.WHITE);
        add(loginButton);
        setLocationRelativeTo(null); // Центрирование окна
        setVisible(true);
    }
    // Подключение к серверу. При удачном подключении возвращает true, иначе - false
    private boolean connectToServer() {
        try {
            socket = new Socket(Main.address, Main.port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch(IOException e) {
            return false;
        }
        Main.setInputStream(in);
        Main.setOutputStream(out);
        Main.setSocket(socket);
        return true;
    }
    // Метод для отключения от сервера
    private void disconnectFromServer() {
        try {
            out.write("disconnect\n".getBytes());
            out.flush();
            // Попытка закрытия потоков ввода-вывода и сокета
            in.close();
            out.close();
            socket.close();
        } catch(IOException e) {
            System.out.println("Исключение: " + e.getMessage()); // Обработка исключения - вывод в консоль краткое соообщение о нём
        }
    }
    // Проверка данных учётной записи
    private boolean isValidLogin(String username, String password) {
        boolean connStatus = connectToServer();
        if(!connStatus) {
            // Если метод connectToServer возвращает false, выводится диалоговое окно с плохой новостью
            JOptionPane.showMessageDialog(AuthorizationForm.this, "Ошибка подключения к серверу!");
            failConnection = true;
            return false;
        }
        // Логика проверки учётных данных
        String s = "";
        if (username.isBlank() || password.isBlank()) {
            return false;
        }
        try {
            out.write(("login " + username + " " + password + "\n").getBytes());
            out.flush();
            Scanner scanner = new Scanner(in);
            s = scanner.next();
        }
        catch(Exception e) {
            System.out.println("Исключение: " + e.getMessage());
        }
        return s.equals("OK");
    }
    // Метод для проверки заполненной формы авторизации
    private void checkLogin(){
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        // Проверка логина и пароля
        if(username.contains(" ")) {
            JOptionPane.showMessageDialog(AuthorizationForm.this, "Недопустимое имя пользователя");
            return;
        }
        if (isValidLogin(username, password)) {
            // Если успешная проверка, вывод информационного сообщения об успешном входе, закрытия фрейма с авторизацией.
            JOptionPane.showMessageDialog(AuthorizationForm.this, "Вход успешен!");
            dispose();
            // Создание фрейма чатов
            ChatFrame chat = new ChatFrame(username);
        } else {
            if(failConnection) return;
            // Иначе вывод сообщения о неверных данных, отключение от сервера.
            JOptionPane.showMessageDialog(AuthorizationForm.this, "Неверный логин или пароль.");
            disconnectFromServer();
        }
    }
}
