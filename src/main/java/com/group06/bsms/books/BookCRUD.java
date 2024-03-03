package com.group06.bsms.books;

import com.formdev.flatlaf.FlatClientProperties;
import com.group06.bsms.utils.SVGHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

interface TableActionEvent {

    public void setIsHiddenBtn(boolean isHiddenBtn);

    public boolean isIsHiddenBtn();

    public void onEdit(int row);

    public void onHide(int row);
}

class TableActionCellEditor extends DefaultCellEditor {

    private TableActionEvent event;

    public TableActionCellEditor(TableActionEvent event) {
        super(new JCheckBox());
        this.event = event;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        ActionPanel action = new ActionPanel();
        action.initEvent(event, row);
//        action.setBackground(table.getSelectionBackground());
        action.setBackground(Color.WHITE);
        return action;
    }

}

class TableActionCellRender extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        ActionPanel action = new ActionPanel();

        action.setBackground(com.getBackground());
        if (isSelected) {
            System.err.println("done");
        }
        return action;
    }
}

public class BookCRUD extends javax.swing.JPanel {

    public BookCRUD() {
        initComponents();

        searchBar.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search");
        searchBar.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, SVGHelper.createSVGIconWithFilter(
                "icons/search.svg",
                Color.black, Color.black,
                14, 14
        ));
        setUpTable();
    }

    public ActionPanel getActionPanelFromCell(int row, int column) {
        if (table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == column) {
            return (ActionPanel) table.getCellEditor(row, column).getTableCellEditorComponent(table, null, false, row, column);
        } else {
            return (ActionPanel) table.getCellRenderer(row, column).getTableCellRendererComponent(table, null, false, false, row, column);
        }
    }

    private void setUpTable() {
        table.getColumnModel().getColumn(5).setCellRenderer(new TableActionCellRender());

        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", 0, 16));
        table.setShowVerticalLines(true);

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortable(0, false);
        sorter.setSortable(1, false);
        sorter.setSortable(2, false);
        sorter.setSortable(5, false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        TableActionEvent event = new TableActionEvent() {
            private boolean isHiddenBtn;

            @Override
            public void setIsHiddenBtn(boolean isHiddenBtn) {
                this.isHiddenBtn = isHiddenBtn;
            }

            @Override
            public boolean isIsHiddenBtn() {
                return isHiddenBtn;
            }

            @Override
            public void onEdit(int row) {
                System.out.println("Edit row " + row);
            }

            @Override
            public void onHide(int row) {
                System.out.println("Hide row " + row);
                System.out.println(isIsHiddenBtn());
                System.out.println();
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

        setAutoscrolls(true);

        bookLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
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
        createBtn.setToolTipText("check");
        createBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        createBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
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
    filterBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
    filterBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            filterBtnActionPerformed(evt);
        }
    });

    table.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
    table.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {"Book1", "A", "B",  new Integer(12),  new Double(121.0), null},
            {"Book2", "A", "B",  new Integer(2),  new Double(2121.0), null},
            {"Book1", "A", "B",  new Integer(3),  new Double(121.0), null},
            {"Book1", "A", "B",  new Integer(23),  new Double(212.0), null},
            {"Book1", "A", "B",  new Integer(12),  new Double(21221.0), null},
            {"Book1", "A", "B",  new Integer(12),  new Double(112.0), null},
            {"Book1", "A", "B",  new Integer(21),  new Double(121.0), null},
            {"Book1", "A", "B",  new Integer(12), null, null}
        },
        new String [] {
            "Title", "Author", "Publisher", "Quantity", "Sale price", "Action"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Object.class
        };
        boolean[] canEdit = new boolean [] {
            false, false, false, false, false, true
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    table.setToolTipText("");
    table.setFocusable(false);
    table.setRowHeight(40);
    table.getTableHeader().setReorderingAllowed(false);
    scrollBar.setViewportView(table);
    if (table.getColumnModel().getColumnCount() > 0) {
        table.getColumnModel().getColumn(0).setMinWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(20);
        table.getColumnModel().getColumn(4).setPreferredWidth(20);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
    }

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
                            .addGap(48, 48, 48)
                            .addComponent(createBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(createBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(filterBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(scrollBar, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
            .addGap(30, 30, 30))
    );
    }// </editor-fold>//GEN-END:initComponents

    private void searchBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchBarActionPerformed

    private void createBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_createBtnActionPerformed

    private void filterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_filterBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bookLabel;
    private javax.swing.JButton createBtn;
    private javax.swing.JButton filterBtn;
    private javax.swing.JScrollPane scrollBar;
    private javax.swing.JTextField searchBar;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
