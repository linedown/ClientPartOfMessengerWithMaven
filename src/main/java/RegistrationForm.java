import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

public class RegistrationForm extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField repeatPasswordField;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    boolean failConnection = false;

    public RegistrationForm() {
        setTitle("Окно регистрации");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(new Color(190, 190, 190));
        setLayout(new GridLayout(4, 2, 10, 10)); // 2 строки, 4 столбца, отступы 10 пикселей
        // Создание и установка иконки на фрейм
        URL url = getClass().getResource("registrationicon.png");
        ImageIcon icon = new ImageIcon(url);
        setIconImage(icon.getImage());
        // Добавление надписей и полей ввода
        JLabel nameLabel = new JLabel("Имя пользователя:");
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        add(nameLabel);
        usernameField = new JTextField();
        add(usernameField);

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setHorizontalAlignment(JLabel.CENTER);
        add(passwordLabel);
        // Поле для ввода пароля
        passwordField = new JPasswordField();
        add(passwordField);

        JLabel repeatPasswordLabel = new JLabel("Повтор пароля:");
        repeatPasswordLabel.setHorizontalAlignment(JLabel.CENTER);
        add(repeatPasswordLabel);
        // Поле для повторного ввода пароля
        repeatPasswordField = new JPasswordField();
        add(repeatPasswordField);
        // Кнопка-текст "Войти в существующий аккаунт" и её настройка
        JButton loginButton = new JButton("Войти в существующий аккаунт");
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setForeground(Color.BLUE);
        loginButton.addActionListener(e -> {
            new AuthorizationForm();
            dispose();
        });
        add(loginButton);
        // Кнопка "Вход"
        JButton registerButton = new JButton("Зарегистрироваться");
        registerButton.addActionListener(e -> {
            checkRegistration();
        });
        // Обработчики события от клавиши ENTER на клавиатуры для каждой текстовой панели
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) checkRegistration();
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) checkRegistration();
            }
        });
        repeatPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) checkRegistration();
            }
        });
        // Настройка кнопки регистрации
        registerButton.setBackground(new Color(49, 58, 68));
        registerButton.setForeground(Color.WHITE);
        add(registerButton);
        // Установка фрейма по центру, а также его видимости
        setLocationRelativeTo(null);
        setVisible(true);
    }

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
            // Попытка закрытия потоков ввода-вывода
            in.close();
            out.close();
            socket.close();
        } catch(IOException e) {
            System.out.println("Исключение: " + e.getMessage()); // Иначе выво исключения
        }
    }
    /* Попытка регистрации аккаунта username с паролем password.
    * При удачной регистрации возвращает true, иначе - false*/
    private boolean isValidRegistration(String username, String password) {
        boolean connStatus = connectToServer();
        if(!connStatus) {
            // Если метод connectToServer возвращает false, выводится диалоговое окно с плохой новостью
            JOptionPane.showMessageDialog(RegistrationForm.this, "Ошибка подключения к серверу!");
            failConnection = true;
            return false;
        }
        String s = "";
        Scanner scanner = new Scanner(in);
        if (username.isBlank() || password.isBlank()) {
            return false;
        }
        try {
            out.write(("createUser " + username + " " + password + "\n").getBytes());
            out.flush();
            s = scanner.nextLine();
        }
        catch(IOException e) {
            System.out.println("Исключение: " + e.getMessage());
            scanner.close();
            return false;
        }
        if(s.equals("OK")) {
            return scanner.nextLine().equals("OK");
        } else {
            return false;
        }
    }

    private void checkRegistration(){
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String repeatedPassword = new String(repeatPasswordField.getPassword());
        if (isValidRegistration(username, password)) {
            if(!password.equals(repeatedPassword)) {
                JOptionPane.showMessageDialog(RegistrationForm.this, "Пароли не совпадают!");
                disconnectFromServer();
                return;
            }
            JOptionPane.showMessageDialog(RegistrationForm.this, "Вуаля! Регистрация прошла успешно!");
            dispose(); // Закрытие окна
            ChatFrame chat = new ChatFrame(username);
        } else {
            if(failConnection) return;
            JOptionPane.showMessageDialog(RegistrationForm.this, "Пользователь с таким именем уже зарегистрирован!!");
            disconnectFromServer();
        }
    }
}
