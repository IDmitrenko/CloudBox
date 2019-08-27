package com.cloud.client;

import com.cloud.client.protocol.NettyNetwork;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoxMainWindow extends JFrame {

//    private final JList<FileList> fileListClient;
//    private final DefaultListModel<FileList> fileListModelClient;
//    private final FileListCellRender fileListCellRenderClient;
//    private final JScrollPane scrollClient;
    // Данные для таблиц
    private Object[][] array = new String[][] {{"GuiHelper.java", "2464 bytes"},
                                               {"SimpleTableTest", "3486 bytes"}};
    // Заголовки столбцов
    private Object[] columnsHeader = new String[] {"Имя файла", "Размер"};
    private final JTable tableClient;
    private final JTable tableServer;
    // Модель данных таблицы
//    private DefaultTableModel tableModel;
    // Модель столбцов таблицы
    private TableColumnModel columnModel;

    private final JButton sendButtonClient;
    private final JButton removeButtonClient;
    private final JButton updateButtonClient;

    private final JPanel sendCommandPanel;

//    private final JList<FileList> fileListServer;
//    private final DefaultListModel<FileList> fileListModelServer;
//    private final FileListCellRender fileListCellRenderServer;
//    private final JScrollPane scrollServer;
    private final JButton downloadButtonServer;
    private final JButton removeButtonServer;
    private final JButton updateButtonServer;

    private final JPanel titleBox;
    private final JLabel titleClient;
    private final JLabel titleServer;

    private final NettyNetwork network;

    public BoxMainWindow() {

        setTitle("CLOUD");
        setBounds(200, 200, 800, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

//        fileListClient = new JList<>();
//        fileListModelClient = new DefaultListModel<>();
//        fileListCellRenderClient = new FileListCellRender();
//        fileListClient.setModel(fileListModelClient);
//        fileListClient.setCellRenderer(fileListCellRenderClient);

//        scrollClient = new JScrollPane(fileListClient,
//                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        add(scrollClient, BorderLayout.WEST);
//        scrollClient.setPreferredSize(new Dimension(390, 720));

        // Создание таблицы на основании модели данных
        tableClient = new JTable(array, columnsHeader);
        tableClient.setAutoCreateRowSorter(true);
        // Получаем стандартную модель
        columnModel = tableClient.getColumnModel();

        tableClient.setPreferredSize(new Dimension(390, 668));
        add(tableClient, BorderLayout.WEST);

        titleBox = new JPanel();
        titleBox.setLayout(new FlowLayout());
        titleClient = new JLabel("Локальное хранилище", SwingConstants.CENTER);
        titleClient.setPreferredSize(new Dimension(386, 20));
        Font f = titleClient.getFont();
        titleClient.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        titleBox.add(titleClient);
        titleServer = new JLabel("Облачное хранилище", SwingConstants.CENTER);
        titleServer.setPreferredSize(new Dimension(386, 20));
        titleServer.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        titleBox.add(titleServer);
        add(titleBox, BorderLayout.NORTH);

        sendCommandPanel = new JPanel();
        sendCommandPanel.setLayout(new BoxLayout(sendCommandPanel, BoxLayout.X_AXIS));

        sendButtonClient = new JButton("Отправить файл");
        sendButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        removeButtonClient = new JButton("Удалить файл");
        removeButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        updateButtonClient = new JButton("Обновить");
        updateButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

//        fileListServer = new JList<>();
//        fileListModelServer = new DefaultListModel<>();
//        fileListCellRenderServer = new FileListCellRender();
//        fileListServer.setModel(fileListModelServer);
//        fileListServer.setCellRenderer(fileListCellRenderServer);

//        scrollServer = new JScrollPane(fileListServer,
//                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        add(scrollServer, BorderLayout.EAST);
//        scrollServer.setPreferredSize(new Dimension(390, 720));
        tableServer = new JTable(array, columnsHeader);
        tableServer.setAutoCreateRowSorter(true);
        columnModel = tableServer.getColumnModel();
/*
        // Определение минимального и максимального размеров столбцов
        Enumeration<TableColumn> e = columnModel.getColumns();
        while ( e.hasMoreElements() ) {
            TableColumn column = (TableColumn)e.nextElement();
            column.setMinWidth(50);
            column.setMaxWidth(200);
        }
*/
        tableServer.setPreferredSize(new Dimension(390, 668));
        add(tableServer, BorderLayout.EAST);
        Box contents = new Box(BoxLayout.X_AXIS);
        contents.add(new JScrollPane(tableClient));
        contents.add(new JScrollPane(tableServer));
//        setContentPane(contents);
        getContentPane().add(contents);


        downloadButtonServer = new JButton("Скачать файл");
        downloadButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        removeButtonServer = new JButton("Удалить файл");
        removeButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        updateButtonServer = new JButton("Обновить");
        updateButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        sendCommandPanel.setPreferredSize(new Dimension(800, 40));
        sendCommandPanel.add(sendButtonClient);
        sendButtonClient.setPreferredSize(new Dimension(144, 30));
        sendCommandPanel.add(removeButtonClient);
        removeButtonClient.setPreferredSize(new Dimension(130, 30));
        sendCommandPanel.add(updateButtonClient);
        updateButtonClient.setPreferredSize(new Dimension(120, 30));

        sendCommandPanel.add(downloadButtonServer);
        downloadButtonServer.setPreferredSize(new Dimension(134, 30));
        sendCommandPanel.add(removeButtonServer);
        removeButtonServer.setPreferredSize(new Dimension(134, 30));
        sendCommandPanel.add(updateButtonServer);
        updateButtonServer.setPreferredSize(new Dimension(130, 30));

        add(sendCommandPanel, BorderLayout.SOUTH);

        setVisible(true);

        this.network = new NettyNetwork();

        LoginDialog loginDialog = new LoginDialog(this, network);
        loginDialog.setVisible(true);

        if (!loginDialog.isConnected()) {
            System.exit(0);
        }

    }
}
