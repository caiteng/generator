package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import config.Config;
import database.Link;

/**
 * 界面
 *
 * @author cait
 */
public class Start extends JFrame {
    public JButton jb, sel;
    public JTextField driver, url, username, password, tableName, path;

    public Start() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new GridLayout(3, 1, 5, 5));

        JPanel driverPane = new JPanel();
        JLabel driverLabel = new JLabel("驱            动：");
        driver = new JTextField(Config.DRIVER);
        driver.setColumns(18);
        driverPane.add(driverLabel);
        driverPane.add(driver);
        contentPane.add(driverPane);

        JPanel pane2 = new JPanel();
        contentPane.add(pane2);
        JLabel label2 = new JLabel("数据库地址：");
        url = new JTextField(Config.DB_URL);
        url.setColumns(18);
        pane2.add(label2);
        pane2.add(url);

        JPanel pane3 = new JPanel();
        contentPane.add(pane3);
        JLabel label3 = new JLabel("用    户    名：");
        username = new JTextField(Config.USERNAME);
        username.setColumns(18);
        pane3.add(label3);
        pane3.add(username);

        JPanel pane4 = new JPanel();
        contentPane.add(pane4);
        JLabel label4 = new JLabel("密            码：");
        password = new JTextField(Config.PASSWORD);
        password.setColumns(18);
        pane4.add(label4);
        pane4.add(password);

        JPanel pane5 = new JPanel();
        contentPane.add(pane5);
        JLabel label5 = new JLabel("表            名：");
        tableName = new JTextField("");
        tableName.setColumns(18);
        pane5.add(label5);
        pane5.add(tableName);

        JPanel pane6 = new JPanel();
        contentPane.add(pane6);
        JLabel label6 = new JLabel("输 出 路 径：");
        path = new JTextField(Config.PATH);
        path.setColumns(18);
        pane6.add(label6);
        pane6.add(path);

        setBounds(640, 330, 350, 340);
        setTitle("配置");
        setVisible(true);
        setLayout(new FlowLayout());
        sel = new JButton("文件生成目标地址...");
        add(sel);
        jb = new JButton("连接数据库");
        add(jb);
        this.addListener();
        //Jbutton偶尔无法渲染，加上刷新保证按钮一定出现
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * 监听按钮
     */
    public void addListener() {

        // 监听连接数据库按钮
        jb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();// 关闭配置页面
                Config.DRIVER = driver.getText();
                Config.DB_URL = url.getText();
                Config.PASSWORD = password.getText();
                Config.USERNAME = username.getText();
                Config.PATH = path.getText();
                String databaseName = url.getText().substring(url.getText().lastIndexOf("/") + 1);
                List<String> list = null;
                try {
                    list = Link.getTableName(databaseName, tableName.getText());
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(new JPanel(), e1.getMessage(), "error", JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                }
                new TableFrame("tableName", list);
            }
        });

        // 监听文件夹选择按钮
        sel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = chooser.showOpenDialog(sel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    /**
                     *获取绝对路径
                     */
                    String filepath = chooser.getSelectedFile().getAbsolutePath();
                    path.setText(filepath);
                }
            }
        });
    }
}

