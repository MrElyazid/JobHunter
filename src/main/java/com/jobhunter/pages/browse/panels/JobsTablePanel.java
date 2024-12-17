package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;
import java.util.function.BiConsumer;

public class JobsTablePanel extends JPanel {
    private JTable jobsTable;
    private DefaultTableModel tableModel;
    private BiConsumer<String, String> onJobSelected;

    public JobsTablePanel(BiConsumer<String, String> onJobSelected) {
        this.onJobSelected = onJobSelected;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        String[] columnNames = {
            "Title", "Company", "Location", "Salary", "Contract", "Experience", 
            "Remote", "Sector", "Required Skills"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(tableModel);
        jobsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jobsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Title
        jobsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Company
        jobsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Location
        jobsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Salary
        jobsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Contract
        jobsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Experience
        jobsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Remote
        jobsTable.getColumnModel().getColumn(7).setPreferredWidth(150); // Sector
        jobsTable.getColumnModel().getColumn(8).setPreferredWidth(200); // Skills

        jobsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jobsTable.getSelectedRow() != -1) {
                int row = jobsTable.getSelectedRow();
                String company = (String) jobsTable.getValueAt(row, 1);
                String title = (String) jobsTable.getValueAt(row, 0);
                onJobSelected.accept(company, title);
            }
        });

        JScrollPane scrollPane = new JScrollPane(jobsTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void addRow(Vector<Object> rowData) {
        tableModel.addRow(rowData);
    }
}
