package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
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
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Initialize table model with columns
        String[] columnNames = {
            "Title", "Company", "Location", "Region", "Sector", "Job",
            "Min Salary", "Contract Type", "Min Experience", "Remote",
            "Company Type", "Internship", "Source", "Link",
            "Application Date", "Publication Date", "Company Address",
            "Company Website", "Hard Skills", "Soft Skills", "Diploma",
            "Desired Profile", "Personality Traits", "Languages",
            "Language Proficiency", "Recommended Skills"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 9 || column == 10 || column == 11) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        // Initialize table with the model
        jobsTable = new JTable(tableModel);
        jobsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jobsTable.setFillsViewportHeight(true);
        jobsTable.setShowGrid(true);
        jobsTable.setGridColor(Color.LIGHT_GRAY);
        jobsTable.setRowHeight(25);
        jobsTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        int[] columnWidths = {
            200, 150, 100, 100, 150, 150,  // Title, Company, Location, Region, Sector, Job
            80, 100, 80, 60,               // Min Salary, Contract, Experience, Remote
            80, 60, 100, 150,              // Company Type, Internship, Source, Link
            100, 100, 150,                 // Application Date, Publication Date, Company Address
            150, 200, 200, 150,            // Company Website, Hard Skills, Soft Skills, Diploma
            200, 150, 100,                 // Desired Profile, Personality, Languages
            100, 200                       // Language Proficiency, Recommended Skills
        };

        for (int i = 0; i < columnWidths.length; i++) {
            jobsTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // Style the table header
        jobsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        jobsTable.getTableHeader().setBackground(new Color(240, 240, 240));
        jobsTable.getTableHeader().setForeground(new Color(51, 51, 51));

        // Make the Link column clickable
        int linkColumnIndex = 13;
        jobsTable.getColumnModel().getColumn(linkColumnIndex).setCellRenderer(new LinkCellRenderer());
        
        // Add link click listener
        jobsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = jobsTable.rowAtPoint(e.getPoint());
                int col = jobsTable.columnAtPoint(e.getPoint());
                
                if (col == linkColumnIndex && row >= 0) {
                    try {
                        String url = (String) jobsTable.getValueAt(row, col);
                        if (url != null && !url.isEmpty()) {
                            Desktop.getDesktop().browse(new URI(url));
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(JobsTablePanel.this,
                            "Error opening link: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Add selection listener for job details
        jobsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jobsTable.getSelectedRow() != -1) {
                int row = jobsTable.getSelectedRow();
                String company = (String) jobsTable.getValueAt(row, 1);
                String title = (String) jobsTable.getValueAt(row, 0);
                onJobSelected.accept(company, title);
            }
        });

        // Create scroll pane with custom styling
        JScrollPane scrollPane = new JScrollPane(jobsTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Add to panel
        add(scrollPane, BorderLayout.CENTER);
    }

    private static class LinkCellRenderer extends JLabel implements TableCellRenderer {
        public LinkCellRenderer() {
            setForeground(new Color(51, 122, 183)); // Bootstrap-like link color
            setFont(new Font(getFont().getName(), Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? "<html><u>" + value.toString() + "</u></html>" : "");
            setToolTipText("Click to open link");
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setOpaque(true);
            } else {
                setBackground(table.getBackground());
                setOpaque(false);
            }
            
            return this;
        }
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void addRow(Vector<Object> rowData) {
        tableModel.addRow(rowData);
    }
}
