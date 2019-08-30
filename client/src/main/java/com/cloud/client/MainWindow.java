package com.cloud.client;

import com.cloud.client.protocol.NettyNetwork;
import com.cloud.common.transfer.FileListMessage;
import com.cloud.common.transfer.FileMessage;
import com.cloud.common.utils.FileAbout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow extends JFrame implements ListFileReciever {

//    private final JList<FileList> fileListClient;
//    private final DefaultListModel<FileList> fileListModelClient;
//    private final FileListCellRender fileListCellRenderClient;
//    private final JScrollPane scrollClient;
    // Данные для таблиц
//    private Object[][] array = new String[][] {{"GuiHelper.java", "2464 bytes"},
//                                               {"SimpleTableTest", "3486 bytes"}};
    private Object[][] arrServer = new String[][] {{"    ", "    "}};
    private Object[][] arrClient;
    private int rows, cols;
    private static final String rootPath = "client/repository";
    
    // Заголовки столбцов
    private Object[] columnsHeader = new String[] {"Имя файла", "Размер"};
    private final JTable tableClient;
    private final JTable tableServer;
    // Модель данных таблицы
    private DefaultTableModel tableModelServer;
    private DefaultTableModel tableModelClient;
    // Модель столбцов таблицы
    private TableColumnModel columnModelClient;
    private TableColumnModel columnModelServer;

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
    private String userName;

    private FileListMessage fll;

    public MainWindow() throws IOException {

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

        clientListFile();

        tableModelClient = new DefaultTableModel(arrClient.length, columnsHeader.length);
        tableModelClient.setDataVector(arrClient, columnsHeader);
/*
        for (int i = 0; i < arrServer.length; i++) {
            tableModelClient.addRow(arrClient[i]);
        }
*/

//        tableClient = new JTable(arrClient, columnsHeader);
        tableClient = new JTable(tableModelClient);

        tableClient.setAutoCreateRowSorter(true);
        // Получаем стандартную модель
        columnModelClient = tableClient.getColumnModel();
        DefaultTableCellRenderer rightRendererClient = new DefaultTableCellRenderer();
        rightRendererClient.setHorizontalAlignment(JLabel.RIGHT);
        columnModelClient.getColumn(1).setCellRenderer(rightRendererClient);

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

        // Создание таблицы на основании модели данных
        tableModelServer = new DefaultTableModel(arrServer.length, columnsHeader.length);
        tableModelServer.setDataVector(arrServer, columnsHeader);
/*
        for (int i = 0; i < arrServer.length; i++) {
            tableModelServer.addRow(arrServer[i]);
        }
*/
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

        tableServer = new JTable(tableModelServer);
//        tableServer = new JTable(arrServer, columnsHeader);
        tableServer.setAutoCreateRowSorter(true);
        columnModelServer = tableServer.getColumnModel();
        DefaultTableCellRenderer rightRendererServer = new DefaultTableCellRenderer();
        rightRendererServer.setHorizontalAlignment(JLabel.RIGHT);
        columnModelServer.getColumn(1).setCellRenderer(rightRendererServer);
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

        sendCommandPanel = new JPanel();
        sendCommandPanel.setLayout(new BoxLayout(sendCommandPanel, BoxLayout.X_AXIS));

        sendButtonClient = new JButton("Отправить файл"); // на сервер
        sendButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Номер выделенной строки
                int idx = tableClient.getSelectedRow();
                String nameFile = arrClient[tableClient.getSelectedRow()][0].toString();
                Path path = Paths.get(rootPath + "/" + nameFile);
                if (nameFile != null && !nameFile.trim().isEmpty() && Files.exists(path)) {
                    try {
                        FileMessage fm = new FileMessage(path, userName);
                        network.sendMsg(fm);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(MainWindow.this,
                            "File does not exist! " + path,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeButtonClient = new JButton("Удалить файл"); // у клиента
        removeButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Номер выделенной строки
                int idx = tableClient.getSelectedRow();
                String nameFile = arrClient[tableClient.getSelectedRow()][0].toString();
                Path path = Paths.get(rootPath + "/" + nameFile);
                if (nameFile != null && !nameFile.trim().isEmpty() && Files.exists(path)) {
                    try {
                        Files.delete(path);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // удаление выделенной строки
                    tableModelClient.removeRow(idx);
                }
            }
        });

        updateButtonClient = new JButton("Обновить");
        updateButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientListFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                updateFileListLocal(arrClient);
            }
        });

//        setContentPane(contents);
        getContentPane().add(contents);

        downloadButtonServer = new JButton("Скачать файл"); // с сервера
        downloadButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Номер выделенной строки
                int idx = tableServer.getSelectedRow();

            }
        });

        removeButtonServer = new JButton("Удалить файл"); // на сервере
        removeButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Номер выделенной строки
                int idx = tableServer.getSelectedRow();

                // удаление выделенной строки
                tableModelServer.removeRow(idx);

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

        this.network = NettyNetwork.getOurInstance();
        network.setListFileReciever(this);

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(() -> network.start());

        LoginDialog loginDialog = new LoginDialog(this, network);
        loginDialog.setVisible(true);

        if (!loginDialog.isConnected()) {
            System.exit(0);
        } else {
            userName = loginDialog.getUserName();
        }

    }

    private void clientListFile() throws IOException {
        fll = new FileListMessage(Paths.get(getClientRootPath()));
        List<FileAbout> filesList = fll.getFilesList();
        rows = filesList.size();
        cols = 2;
        if (rows == 0) {
            rows = 1;
            arrClient = new String[rows][cols];
            arrClient[0][0] = "     ";
            arrClient[0][1] = "     ";
        } else {
            arrClient = new String[rows][cols];
            for (int i = 0; i < rows; i++) {
                arrClient[i][0] = filesList.get(i).getName();
                arrClient[i][1] = String.valueOf(filesList.get(i).getSize()) + " bytes";
            }
        }
    }

    @Override
    public void updateFileListLocal(Object[][] fll) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableModelClient.setDataVector(fll, columnsHeader);
                tableModelClient.fireTableDataChanged();
            }
        });
    }

    @Override
    public void updateFileListServer(Object[][] fls) {
        // возвратился двумерный массив (первый параметр JTable)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableModelServer.setDataVector(fls, columnsHeader);
                tableModelServer.fireTableDataChanged();
            }
        });
    }

    public static FileListMessage getListFileClient() {
        FileListMessage fll = null;
        try {
            fll = new FileListMessage(Paths.get(getClientRootPath()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fll;
    }
    
    private static String getClientRootPath() {
        Path path = Paths.get(rootPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return path.toString();
    }

}