package com.group06.bsms.books;

import com.group06.bsms.DB;
import com.group06.bsms.authors.AuthorRepository;
import com.group06.bsms.authors.AuthorService;
import com.group06.bsms.publishers.PublisherRepository;
import com.group06.bsms.publishers.PublisherService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

public class BookRevenue extends javax.swing.JPanel {

    private final BookService bookService;
    private BookRevenueTableModel model;
    private Map<Integer, SortOrder> columnSortOrders = new HashMap<>();
    private static String previousComboBoxSelection;
    private LocalDate endDate = LocalDate.now();
    private LocalDate startDate = LocalDate.now().minusDays(7);
    private List<Book> books;

    public BookRevenue() {
        this(
                new BookService(
                        new BookRepository(DB.db()),
                        new AuthorService(new AuthorRepository(DB.db())),
                        new PublisherService(new PublisherRepository(DB.db()))
                )
        );
    }

    public BookRevenue(BookService bookService) {
        this.bookService = bookService;
        this.model = new BookRevenueTableModel();
        initComponents();
        setUpTable();
        loadBooksIntoTable();
        isVisibleDatePicker(false);

        startDatePicker.setDate(new java.util.Date());
        var sm = new SimpleDateFormat("dd/MM/yyyy");
        startDatePicker.setText(sm.format(startDatePicker.getDate()));

        endDatePicker.setDate(new java.util.Date());
        endDatePicker.setText(sm.format(endDatePicker.getDate()));

        this.durationDaysComboBox.requestFocus();
    }

    public void setUpTable() {

        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", 0, 16));
        table.setShowVerticalLines(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        columnSortOrders.put(6, SortOrder.DESCENDING);
        table.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = table.columnAtPoint(e.getPoint());
                toggleSortOrder(columnIndex);
                loadBooksIntoTable();
                table.getTableHeader().repaint();
            }
        });

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                table.editCellAt(row, column);
                table.setRowSelectionInterval(row, row);
            }
        });
    }

    public void loadBooksIntoTable() {
        try {
            Date start = java.sql.Date.valueOf(startDate);
            Date end = java.sql.Date.valueOf(endDate);
            books = bookService.getTop10BooksWithHighestRevenue(columnSortOrders, start, end);
            model.reloadAllBooks(books);
            showBarChart();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "An error has occurred: " + e.getMessage(),
                    "BSMS Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void toggleSortOrder(int columnIndex) {
        SortOrder currentOrder = columnSortOrders.getOrDefault(columnIndex, SortOrder.UNSORTED);
        SortOrder newOrder = currentOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING;
        columnSortOrders.clear();
        columnSortOrders.put(columnIndex, newOrder);
    }

    class CustomHeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelColumn = table.convertColumnIndexToModel(column);
            SortOrder sortOrder = columnSortOrders.getOrDefault(modelColumn, SortOrder.UNSORTED);
            Icon sortIcon = null;
            if (column == 3 || column == 4 || column == 5 || column == 6) {
                setHorizontalAlignment(JLabel.CENTER);
                if (sortOrder == SortOrder.ASCENDING) {
                    sortIcon = UIManager.getIcon("Table.descendingSortIcon");
                } else if (sortOrder == SortOrder.DESCENDING) {
                    sortIcon = UIManager.getIcon("Table.ascendingSortIcon");
                }
            } else {
                setHorizontalAlignment(JLabel.LEFT);
                if (sortOrder == SortOrder.ASCENDING) {
                    sortIcon = UIManager.getIcon("Table.descendingSortIcon");
                } else if (sortOrder == SortOrder.DESCENDING) {
                    sortIcon = UIManager.getIcon("Table.ascendingSortIcon");
                }
            }
            setHorizontalTextPosition(JLabel.LEFT);
            label.setIcon(sortIcon);
            return label;
        }
    }

    private void isVisibleDatePicker(boolean isVisible) {
        startDatePicker.setVisible(isVisible);
        endDatePicker.setVisible(isVisible);
        startDateLabel.setVisible(isVisible);
        endDateLabel.setVisible(isVisible);
        confimrBtn.setVisible(isVisible);
    }

    public void showBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (var book : books) {
            dataset.setValue(book.revenue.revenue, "$", book.title);
        }

        JFreeChart chart = ChartFactory.createBarChart("Best selling books", "Title", "Revenue",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot categoryPlot = chart.getCategoryPlot();
        categoryPlot.setBackgroundPaint(UIManager.getColor("mutedBackground"));

        BarRenderer renderer = (BarRenderer) categoryPlot.getRenderer();
        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator("{1}, Revenue: {2}", NumberFormat.getCurrencyInstance(Locale.US)));
        renderer.setBarPainter(new StandardBarPainter());

        Color clr3 = UIManager.getColor("accentColor");
        renderer.setSeriesPaint(0, clr3);

        ChartPanel barpChartPanel = new ChartPanel(chart);
        barpChartPanel.setDomainZoomable(false);
        barpChartPanel.setRangeZoomable(false);
        chartBarPanel.removeAll();
        chartBarPanel.add(barpChartPanel, BorderLayout.CENTER);
        chartBarPanel.validate();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bookRevenueLabel = new javax.swing.JLabel();
        confimrBtn = new javax.swing.JButton();
        startDatePicker = new com.group06.bsms.components.DatePickerPanel();
        endDatePicker = new com.group06.bsms.components.DatePickerPanel();
        durationDaysComboBox = new javax.swing.JComboBox<>();
        startDateLabel = new javax.swing.JLabel();
        endDateLabel = new javax.swing.JLabel();
        jTabbedPane = new javax.swing.JTabbedPane();
        tablePanel = new javax.swing.JPanel();
        scrollBar = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        chartPanel = new javax.swing.JPanel();
        chartBarPanel = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));

        bookRevenueLabel.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        bookRevenueLabel.setText("BEST SELLING BOOKS");

        confimrBtn.setBackground(new java.awt.Color(65, 105, 225));
        confimrBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        confimrBtn.setForeground(new java.awt.Color(255, 255, 255));
        confimrBtn.setMnemonic(java.awt.event.KeyEvent.VK_C);
        confimrBtn.setText("Confirm");
        confimrBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        confimrBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confimrBtnActionPerformed(evt);
            }
        });

        startDatePicker.setMaximumSize(new java.awt.Dimension(215, 31));
        startDatePicker.setPlaceholder("dd/mm/yyyy");
        startDatePicker.setPreferredSize(new java.awt.Dimension(215, 31));

        endDatePicker.setMaximumSize(new java.awt.Dimension(215, 31));
        endDatePicker.setPlaceholder("dd/mm/yyyy");
        endDatePicker.setPreferredSize(new java.awt.Dimension(215, 31));

        durationDaysComboBox.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        durationDaysComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "by Week", "by Month", "Date to Date" }));
        durationDaysComboBox.setPreferredSize(new java.awt.Dimension(154, 28));
        durationDaysComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                durationDaysComboBoxActionPerformed(evt);
            }
        });

        startDateLabel.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        startDateLabel.setLabelFor(startDatePicker);
        startDateLabel.setText("From Date");

        endDateLabel.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        endDateLabel.setLabelFor(endDatePicker);
        endDateLabel.setText("To Date");

        jTabbedPane.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        table.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        table.setModel(this.model);
        table.setToolTipText("");
        table.setRowHeight(40);
        table.getTableHeader().setReorderingAllowed(false);
        scrollBar.setViewportView(table);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(scrollBar, javax.swing.GroupLayout.DEFAULT_SIZE, 853, Short.MAX_VALUE))
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(scrollBar, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        jTabbedPane.addTab("Table", tablePanel);

        chartBarPanel.setLayout(new java.awt.GridLayout(1, 0));

        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(chartPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(chartBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        chartPanelLayout.setVerticalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(chartPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(chartBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        jTabbedPane.addTab("Chart", chartPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane)
                        .addGap(42, 42, 42))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bookRevenueLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(durationDaysComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(startDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(endDateLabel))))
                        .addGap(20, 20, 20)
                        .addComponent(confimrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(bookRevenueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startDateLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(3, 3, 3)
                                    .addComponent(startDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(durationDaysComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(confimrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(endDateLabel)
                        .addGap(3, 3, 3)
                        .addComponent(endDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane)
                .addGap(50, 50, 50))
        );

        jTabbedPane.getAccessibleContext().setAccessibleName("Table\nChart\n");
    }// </editor-fold>//GEN-END:initComponents

    private void durationDaysComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_durationDaysComboBoxActionPerformed
        if (durationDaysComboBox.getSelectedItem().toString() == previousComboBoxSelection) {
            return;
        }

        if (durationDaysComboBox.getSelectedItem().toString() == "by Week") {
            endDate = LocalDate.now();
            startDate = LocalDate.now().minusDays(7);
            isVisibleDatePicker(false);
            previousComboBoxSelection = "by Week";
            columnSortOrders.clear();
            columnSortOrders.put(6, SortOrder.DESCENDING);
            loadBooksIntoTable();
        } else if (durationDaysComboBox.getSelectedItem().toString() == "by Month") {
            endDate = LocalDate.now();
            startDate = LocalDate.now().minusDays(30);
            isVisibleDatePicker(false);
            previousComboBoxSelection = "by Month";
            columnSortOrders.clear();
            columnSortOrders.put(6, SortOrder.DESCENDING);
            loadBooksIntoTable();
        } else if (durationDaysComboBox.getSelectedItem().toString() == "Date to Date") {
            isVisibleDatePicker(true);
            previousComboBoxSelection = "by Date";
            columnSortOrders.clear();
            columnSortOrders.put(6, SortOrder.DESCENDING);
        }
    }//GEN-LAST:event_durationDaysComboBoxActionPerformed

    private void confimrBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confimrBtnActionPerformed
        LocalDate start = startDatePicker.getDateSQL().toLocalDate();
        LocalDate end = endDatePicker.getDateSQL().toLocalDate();

        if (start.isAfter(end)) {
            JOptionPane.showMessageDialog(null, "Start date must be before end date", "BSMS Error", JOptionPane.ERROR_MESSAGE);
        } else {
            startDate = start;
            endDate = end;
            loadBooksIntoTable();
        }
    }//GEN-LAST:event_confimrBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bookRevenueLabel;
    private javax.swing.JPanel chartBarPanel;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JButton confimrBtn;
    private javax.swing.JComboBox<String> durationDaysComboBox;
    private javax.swing.JLabel endDateLabel;
    private com.group06.bsms.components.DatePickerPanel endDatePicker;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JScrollPane scrollBar;
    private javax.swing.JLabel startDateLabel;
    private com.group06.bsms.components.DatePickerPanel startDatePicker;
    private javax.swing.JTable table;
    private javax.swing.JPanel tablePanel;
    // End of variables declaration//GEN-END:variables
}
