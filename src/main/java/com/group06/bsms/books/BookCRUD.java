package com.group06.bsms.books;

import com.formdev.flatlaf.FlatClientProperties;
import com.group06.bsms.DB;
import com.group06.bsms.Main;
import com.group06.bsms.components.TableActionEvent;
import com.group06.bsms.utils.SVGHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class BookCRUD extends javax.swing.JPanel {

    private final BookService bookService;
    private BookTableModel model;
    private Map<Integer, SortOrder> columnSortOrders = new HashMap<>();
    private int currentOffset = 0;
    private int limit = Main.ROW_LIMIT;
    private boolean isScrollAtBottom = false;

    public BookCRUD() {
        this(new BookService(new BookRepository(DB.db())));
    }

    public BookCRUD(BookService bookService) {
        this.bookService = bookService;
        this.model = new BookTableModel(bookService);
        initComponents();

        searchBar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search");
        searchBar.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, SVGHelper.createSVGIconWithFilter(
                "icons/search.svg",
                Color.black, Color.black,
                14, 14
        ));

        setUpTable();
        loadBooksIntoTable();
    }

    private void loadBooksIntoTable() {
        var searchString = searchBar.getText();

        var searchChoiceKey = searchComboBox.getSelectedItem().toString();
        var searchChoiceMap = new HashMap<String, String>();
        searchChoiceMap.put("by Title", "Book.title");
        searchChoiceMap.put("by Author", "Author.name");
        searchChoiceMap.put("by Publisher", "Publisher.name");
        var searchChoiceValue = searchChoiceMap.get(searchChoiceKey);

        List<Integer> listBookCategoryId = null;
        try {
            var books = bookService.searchSortFilterBook(currentOffset, limit, columnSortOrders, searchString, searchChoiceValue, -1, -1, null, null, listBookCategoryId);
            if (currentOffset > 0) {
                model.loadNewBooks(books);
            } else {
                model.reloadAllBooks(books);
            }
            currentOffset += limit;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleSortOrder(int columnIndex) {
        if (columnIndex != 5) {
            SortOrder currentOrder = columnSortOrders.getOrDefault(columnIndex, SortOrder.UNSORTED);
            SortOrder newOrder = currentOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING;
            columnSortOrders.clear();
            columnSortOrders.put(columnIndex, newOrder);
        }
    }

    class CustomHeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int modelColumn = table.convertColumnIndexToModel(column);
            SortOrder sortOrder = columnSortOrders.getOrDefault(modelColumn, SortOrder.UNSORTED);
            Icon sortIcon = null;
            if (column == 3 || column == 4) {
                setHorizontalAlignment(JLabel.CENTER);
                if (sortOrder == SortOrder.ASCENDING) {
                    sortIcon = UIManager.getIcon("Table.descendingSortIcon");
                } else if (sortOrder == SortOrder.DESCENDING) {
                    sortIcon = UIManager.getIcon("Table.ascendingSortIcon");
                }
            } else if (column != 5) {
                if (sortOrder == SortOrder.ASCENDING) {
                    sortIcon = UIManager.getIcon("Table.descendingSortIcon");
                } else if (sortOrder == SortOrder.DESCENDING) {
                    sortIcon = UIManager.getIcon("Table.ascendingSortIcon");
                }
                setHorizontalAlignment(JLabel.LEFT);
            } else {
                //Non sorted column action
                setHorizontalAlignment(JLabel.CENTER);
                sortIcon = null;
            }
            setHorizontalTextPosition(JLabel.LEFT);
            label.setIcon(sortIcon);
            return label;
        }
    }

    private void setUpTable() {
        table.getColumnModel().getColumn(5).setCellRenderer(new TableActionCellRender());

        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", 0, 16));
        table.setShowVerticalLines(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        columnSortOrders.put(0, SortOrder.ASCENDING);

        table.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = table.columnAtPoint(e.getPoint());
                toggleSortOrder(columnIndex);
                currentOffset = 0;
                loadBooksIntoTable();
                table.getTableHeader().repaint();
            }
        });
        TableActionEvent event = new TableActionEvent() {
            @Override
            public void onEdit(int row) {
                System.out.println("Edit row " + row);
//                JOptionPane.showMessageDialog(
//                        app,
//                        "Could not disconnect from database. Please restart your computer.",
//                        "BSMS error",
//                        JOptionPane.ERROR_MESSAGE
//                );
            }

            @Override
            public int onHide(int row) {
                try {
                    if (model.getHiddenState(row) == 1) {
                        bookService.showBook(model.getBook(row).id);
                    } else if (model.getHiddenState(row) == 0) {
                        bookService.hideBook(model.getBook(row).id);
                    }
                    model.setHiddenState(row);
                } catch (Exception e) {
                    System.out.println("Some error occurred while trying to hide a book: " + e.getMessage());
                }

                System.out.println("True value of book with title " + model.getValueAt(row, 0) + ": " + model.getHiddenState(row));
                return model.getHiddenState(row);
            }
        };

        table.getColumnModel().getColumn(5).setCellEditor(new TableActionCellEditor(event));

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());

                if (column == 5) {
                    table.editCellAt(row, column);
                    table.setRowSelectionInterval(row, row);
                }
            }
        });

        scrollBar.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) {
                //Check if scrolled to the bottom
                isScrollAtBottom = e.getAdjustable().getMaximum() == e.getAdjustable().getValue() + e.getAdjustable().getVisibleAmount();
                if (isScrollAtBottom) {
                    loadBooksIntoTable();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bookLabel = new javax.swing.JLabel();
        searchBar = new javax.swing.JTextField();
        createBtn = new javax.swing.JButton();
        filterBtn = new javax.swing.JButton();
        scrollBar = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        searchComboBox = new javax.swing.JComboBox<>();

        setAutoscrolls(true);

        bookLabel.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        bookLabel.setText("BOOKS");

        searchBar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        searchBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBarActionPerformed(evt);
            }
        });

        createBtn.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        createBtn.setIcon(SVGHelper.createSVGIconWithFilter(
            "icons/add.svg",
            Color.black, Color.black,
            14, 14
        ));
        createBtn.setText("Create");
        createBtn.setToolTipText("");
        createBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        createBtn.setIconTextGap(2);
        createBtn.setMargin(new java.awt.Insets(10, 10, 10, 10));
        createBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBtnActionPerformed(evt);
            }
        });

        filterBtn.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        filterBtn.setIcon(SVGHelper.createSVGIconWithFilter(
            "icons/filter.svg",
            Color.black, Color.black,
            14, 14));
    filterBtn.setText("Filter");
    filterBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    filterBtn.setIconTextGap(2);
    filterBtn.setMargin(new java.awt.Insets(10, 10, 10, 10));
    filterBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            filterBtnActionPerformed(evt);
        }
    });

    table.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
    table.setModel(this.model);
    table.setToolTipText("");
    table.setRowHeight(40);
    table.getTableHeader().setReorderingAllowed(false);
    scrollBar.setViewportView(table);

    searchComboBox.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
    searchComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "by Title", "by Author", "by Publisher" }));
    searchComboBox.setPreferredSize(new java.awt.Dimension(130, 28));
    searchComboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            searchComboBoxActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(42, 42, 42)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(bookLabel)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(scrollBar, javax.swing.GroupLayout.DEFAULT_SIZE, 836, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)
                            .addComponent(searchComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)
                            .addComponent(createBtn)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(filterBtn)))
                    .addGap(50, 50, 50))))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(48, 48, 48)
            .addComponent(bookLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(createBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(filterBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(searchComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(scrollBar, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
            .addGap(30, 30, 30))
    );
    }// </editor-fold>//GEN-END:initComponents

    private void searchBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBarActionPerformed
        currentOffset = 0;
        loadBooksIntoTable();

    }//GEN-LAST:event_searchBarActionPerformed

    private void createBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBtnActionPerformed

    }//GEN-LAST:event_createBtnActionPerformed

    private void filterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterBtnActionPerformed

    }//GEN-LAST:event_filterBtnActionPerformed

    private void searchComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bookLabel;
    private javax.swing.JButton createBtn;
    private javax.swing.JButton filterBtn;
    private javax.swing.JScrollPane scrollBar;
    private javax.swing.JTextField searchBar;
    private javax.swing.JComboBox<String> searchComboBox;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
