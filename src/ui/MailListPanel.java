package ui;

import i18n.I18n;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

public class MailListPanel extends JPanel {

    private final JList<File> list;
    private final DefaultListModel<File> model;
    private File currentDir;

    public MailListPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(280, 0));

        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(40); // Mehr Platz pro Eintrag
        
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new FileDropHandler());

        list.setCellRenderer((l, value, i, sel, focus) -> {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            if (value.getName().equals("HINWEIS_LEER")) {
                label.setText(I18n.t("btn.erk"));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setForeground(Color.GRAY);
                label.setBackground(l.getBackground());
            } else {
                label.setText(value.getName());
                label.setIcon(UIManager.getIcon("FileView.fileIcon"));
                if (sel) {
                    label.setBackground(new Color(59, 130, 246));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(l.getBackground());
                    label.setForeground(l.getForeground());
                }
            }
            return label;
        });

        currentDir = new File(System.getProperty("user.home"), "Downloads");
        loadEmails(currentDir);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    private void loadEmails(File dir) {
        model.clear();
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".eml"));
            if (files != null) {
                for (File f : files) model.addElement(f);
            }
        }
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (model.isEmpty()) {
            model.addElement(new File("HINWEIS_LEER"));
        }
    }

    public File getSelectedMail() {
        File selected = list.getSelectedValue();
        if (selected == null || selected.getName().equals("HINWEIS_LEER")) return null;
        return selected;
    }

    private class FileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support) {
            try {
                List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                boolean added = false;
                for (File file : files) {
                    if (file.getName().toLowerCase().endsWith(".eml")) {
                        if (model.size() == 1 && model.get(0).getName().equals("HINWEIS_LEER")) model.clear();
                        model.addElement(file);
                        list.setSelectedValue(file, true);
                        added = true;
                    }
                }
                return added;
            } catch (Exception e) { return false; }
        }
    }

    public void chooseDirectory(Component parent) {
        JFileChooser chooser = new JFileChooser(currentDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            currentDir = chooser.getSelectedFile();
            loadEmails(currentDir);
        }
    }
}