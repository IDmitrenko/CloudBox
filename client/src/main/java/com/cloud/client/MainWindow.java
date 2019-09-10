package com.cloud.client;

import com.cloud.client.protocol.NettyNetwork;
import com.cloud.common.transfer.CommandMessage;
import com.cloud.common.utils.FileAbout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow extends JFrame implements ListFileReciever {

    public Object[][] arrServer = new String[][]{{"    ", "    "}};
    public Object[][] arrClient = new String[][]{{"    ", "    "}};

    private Object[] columnsHeader = new String[]{"Имя файла", "Размер"};
    private final JTable tableClient;
    private final JTable tableServer;

    private DefaultTableModel tableModelServer;
    private DefaultTableModel tableModelClient;

    private final JButton sendButtonClient;
    private final JButton removeButtonClient;
    private final JButton updateButtonClient;

    private final JPanel sendCommandPanel;

    private final JButton downloadButtonServer;
    private final JButton removeButtonServer;
    private final JButton updateButtonServer;

    private final JPanel titleBox;
    private final JLabel titleClient;
    private final JLabel titleServer;

    private final NettyNetwork network;
    private static String userName;

    ExecutorService executorService = Executors.newCachedThreadPool();

    public MainWindow() throws IOException {

        setTitle("CLOUD");
        setBounds(200, 200, 800, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        tableModelClient = new DefaultTableModel(arrClient.length, columnsHeader.length) {
            @Override
            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Integer.class;
                } else {
                    return String.class;
                }
            }
        };
        tableModelClient.setDataVector(arrClient, columnsHeader);

        tableClient = new JTable(tableModelClient);

        tableClient.setAutoCreateRowSorter(true);

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

        tableModelServer = new DefaultTableModel(arrServer.length, columnsHeader.length) {
            @Override
            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Integer.class;
                } else {
                    return String.class;
                }
            }
        };
        tableModelServer.setDataVector(arrServer, columnsHeader);

        tableServer = new JTable(tableModelServer);
        tableServer.setAutoCreateRowSorter(true);

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
                String nameFile = arrClient[tableClient.getSelectedRow()][0].toString();
                Path path = Paths.get(network.getClientRootPath() + "/" + nameFile);
                if (nameFile != null && !nameFile.trim().isEmpty() && Files.exists(path)) {
                    try {
                        if (network.bigFile(path)) {
                            executorService.submit(() -> {
                                try {
                                    network.sendBigFile(path);
                                } catch (InterruptedException | IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                        } else {
                            network.sendSmallFile(path);
                        }
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
                int idx = tableClient.getSelectedRow();
                String nameFile = arrClient[idx][0].toString();
                network.deleteFile(nameFile);
            }
        });

        updateButtonClient = new JButton("Обновить");
        updateButtonClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                network.clientListFile();
            }
        });

        getContentPane().add(contents);

        downloadButtonServer = new JButton("Скачать файл"); // с сервера
        downloadButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameFile = arrServer[tableServer.getSelectedRow()][0].toString();
                downloadFileFromServer(nameFile);
            }
        });

        removeButtonServer = new JButton("Удалить файл"); // на сервере
        removeButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nameFile = arrServer[tableServer.getSelectedRow()][0].toString();
                serverDeleteFile(nameFile);
            }
        });

        updateButtonServer = new JButton("Обновить");
        updateButtonServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverListFile();
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
        network.setMainFrame(this);
        network.setExecutorService(executorService);
        network.clientListFile();

        executorService.submit(() -> network.start());

        LoginDialog loginDialog = new LoginDialog(this, network);
        loginDialog.setVisible(true);

        if (!loginDialog.isConnected()) {
            network.closeConnection();
            System.exit(0);
        } else {
            userName = loginDialog.getUserName();
        }

    }

    private void serverListFile() {
        CommandMessage slf = new CommandMessage(CommandMessage.CMD_MSG_REQUEST_FILES_LIST);
        network.sendMsg(slf);
    }

    private void serverDeleteFile(String nameFile) {
        File file = new File(nameFile);
        FileAbout fa = new FileAbout(file);
        CommandMessage sdf = new CommandMessage(CommandMessage.CMD_MSG_REQUEST_SERVER_DELETE_FILE, fa);
        network.sendMsg(sdf);
    }

    private void downloadFileFromServer(String nameFile) {
        File file = new File(nameFile);
        FileAbout fa = new FileAbout(file);
        CommandMessage dffs = new CommandMessage(CommandMessage.CMD_MSG_REQUEST_FILE_DOWNLOAD, fa);
        network.sendMsg(dffs);
    }

    @Override
    public void updateFileListLocal(Object[][] fll) {
        arrClient = fll;
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
        arrServer = fls;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tableModelServer.setDataVector(fls, columnsHeader);
                tableModelServer.fireTableDataChanged();
            }
        });
    }

    public static String getUserName() {
        return userName;
    }
}
