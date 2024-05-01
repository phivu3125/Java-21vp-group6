/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.group06.bsms.order;

import com.group06.bsms.DB;
import com.group06.bsms.accounts.Account;
import com.group06.bsms.accounts.AccountRepository;
import com.group06.bsms.accounts.AccountService;
import com.group06.bsms.authors.AuthorRepository;
import com.group06.bsms.authors.AuthorService;
import com.group06.bsms.books.Book;
import com.group06.bsms.books.BookRepository;
import javax.swing.table.*;
import com.group06.bsms.books.BookService;
import com.group06.bsms.publishers.PublisherRepository;
import com.group06.bsms.publishers.PublisherService;
import com.group06.bsms.components.CustomTableCellRenderer;
import com.group06.bsms.dashboard.Dashboard;
import com.group06.bsms.members.MemberRepository;
import com.group06.bsms.members.MemberService;
import com.group06.bsms.members.Member;
import com.group06.bsms.utils.SVGHelper;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class AddOrderSheet extends javax.swing.JPanel {

    /**
     * Creates new form importSheetUI
     */
    private BookService bookService;
    private AccountService accountService;
    private OrderSheetService orderSheetService;
    private MemberService memberService;
    private Map<String, Book> bookMap;
    private boolean isSettingValue = false;
    private Account employee;
    private Member member;

    private OrderSheetCRUD orderSheetCRUD;

    public void setImportSheetCRUD(OrderSheetCRUD orderSheetCRUD) {
        this.orderSheetCRUD = orderSheetCRUD;
    }

    public AddOrderSheet() {

        this(new BookService(
                new BookRepository(DB.db()),
                new AuthorService(new AuthorRepository(DB.db())),
                new PublisherService(new PublisherRepository(DB.db()))),
                new OrderSheetService(
                        new OrderSheetRepository(DB.db(), new AccountRepository(DB.db()),
                                new MemberRepository(DB.db()))),
                new AccountService(new AccountRepository(DB.db())),
                new MemberService(new MemberRepository(DB.db())));

    }

    public AddOrderSheet(BookService bookService, OrderSheetService orderSheetService, AccountService accountService,
            MemberService memberService) {

        this.bookService = bookService;
        this.accountService = accountService;
        this.orderSheetService = orderSheetService;
        this.bookMap = new HashMap<>();

        initComponents();

        importBooksTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        importBooksTable.getActionMap().put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int editingRow = importBooksTable.getEditingRow();
                int editingColumn = importBooksTable.getEditingColumn();
                int rowCount = importBooksTable.getRowCount();
                if (editingColumn == 2) {
                    if (editingRow == rowCount - 1) {
                        DefaultTableModel model = (DefaultTableModel) importBooksTable.getModel();
                        model.addRow(new Object[model.getColumnCount()]);
                    }
                    importBooksTable.changeSelection(editingRow + 1, 0, false, false);
                    importBooksTable.editCellAt(editingRow + 1, 0);
                    importBooksTable.transferFocus();
                }
            }
        });

        DefaultTableModel model = (DefaultTableModel) importBooksTable.getModel();
        model.addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                updateTotalCost();
            }
        });

        importBooksTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        importBooksTable.getColumnModel().getColumn(0).setCellEditor(new AutoSuggestComboBoxEditor());

        importBooksTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField() {
            {
                addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent evt) {
                        char inputChar = evt.getKeyChar();

                        if (Character.isLetter(inputChar)) {
                            setEditable(false);
                        } else {
                            setEditable(true);
                        }

                    }
                });
            }
        }));

        importBooksTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField() {
            {
                addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent evt) {
                        char inputChar = evt.getKeyChar();
                        if (Character.isLetter(inputChar)) {
                            setEditable(false);
                        } else {
                            setEditable(true);
                        }

                    }
                });
            }
        }));

        importBooksTable.getModel().addTableModelListener((TableModelEvent e) -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();

                if (column == 0) {

                    if (!isSettingValue) {
                        String newTitle = (String) importBooksTable.getValueAt(row, column);
                        if (isDuplicateTitle(newTitle, row)) {

                            isSettingValue = true;
                            importBooksTable.setValueAt("", row, column);
                            isSettingValue = false;

                            JOptionPane.showMessageDialog(null, "There's already a " + newTitle + " row.", "BSMS Error",
                                    JOptionPane.ERROR_MESSAGE);
                            importBooksTable.requestFocusInWindow();

                        }
                    }
                }
                if (column == 1) {

                    if (!isSettingValue) {
                        String newQuantityStr = (String) importBooksTable.getValueAt(row, column);
                        try {
                            int newQuantity = Integer.parseInt(newQuantityStr);
                            if (newQuantity == 0) {

                                isSettingValue = true;
                                importBooksTable.setValueAt("", row, column);
                                isSettingValue = false;

                                JOptionPane.showMessageDialog(null, "Cannot have zero quantity", "BSMS Error",
                                        JOptionPane.ERROR_MESSAGE);
                                importBooksTable.requestFocusInWindow();
                            }

                        } catch (NumberFormatException nfe) {

                        }

                    }
                }
            }
        });

        importBooksTable.getTableHeader().setFont(new java.awt.Font("Segoe UI", 0, 16));
        importBooksTable.getTableHeader().setReorderingAllowed(false);
        importBooksTable.setShowVerticalLines(true);
    }

    public void loadEmployee(int id) {
        try {
            this.employee = accountService.selectAccount(id);
        } catch (Exception e) {
        }
        if (employee != null) {
            employeeField.setText(employee.phone);
        }
    }

    public void loadMember(int id) {
        try {
            this.member = memberService.selectMember(id);
        } catch (Exception e) {
        }
        if (employee != null) {
            memberField.setText(member.phone);
        }
    }

    private void updateTotalCost() {
        double totalCost = 0.0;
        DefaultTableModel model = (DefaultTableModel) importBooksTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {

            String quantityStr = (String) model.getValueAt(i, 1);
            String pricePerBookStr = (String) model.getValueAt(i, 2);
            if (quantityStr != null && !quantityStr.trim().isEmpty()
                    && pricePerBookStr != null && !pricePerBookStr.trim().isEmpty()) {
                try {
                    double quantity = Double.parseDouble(quantityStr);
                    double pricePerBook = Double.parseDouble(pricePerBookStr);
                    totalCost += quantity * pricePerBook;
                } catch (NumberFormatException ex) {

                }
            }
        }
        totalCostField.setText(String.format("%.2f", totalCost));
    }

    private boolean isDuplicateTitle(String newTitle, int currentRow) {
        DefaultTableModel model = (DefaultTableModel) importBooksTable.getModel();
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            if (i == currentRow) {
                continue;
            }
            String title = (String) model.getValueAt(i, 0);
            if (newTitle.equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        formScrollPane = new javax.swing.JScrollPane();
        groupFieldPanel = new javax.swing.JPanel();
        totalCostField = new javax.swing.JTextField();
        totalCostLabel = new javax.swing.JLabel();
        importBookScrollPane = new javax.swing.JScrollPane();
        importBooksTable = new javax.swing.JTable();
        saveButton = new javax.swing.JButton();
        employeeLabel = new javax.swing.JLabel();
        employeeField = new javax.swing.JTextField();
        importDatePicker = new com.group06.bsms.components.DatePickerPanel();
        importDateLabel = new javax.swing.JLabel();
        memberLabel = new javax.swing.JLabel();
        memberField = new javax.swing.JTextField();
        titleBar = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        pageName = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();

        setPreferredSize(new java.awt.Dimension(849, 661));
        setLayout(new java.awt.BorderLayout());

        formScrollPane.setBorder(null);
        formScrollPane.setPreferredSize(new java.awt.Dimension(893, 661));

        totalCostField.setEditable(false);
        totalCostField.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        totalCostField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        totalCostField.setFocusable(false);
        totalCostField.setMinimumSize(new java.awt.Dimension(440, 31));
        totalCostField.setPreferredSize(new java.awt.Dimension(440, 31));

        totalCostLabel.setDisplayedMnemonic(java.awt.event.KeyEvent.VK_T);
        totalCostLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        totalCostLabel.setText("Total sale price");

        importBooksTable.setModel(new com.group06.bsms.order.OrderedBooksTableModel());
        importBooksTable.setRowHeight(40);
        importBooksTable.setRowSelectionAllowed(false);
        importBookScrollPane.setViewportView(importBooksTable);

        saveButton.setBackground(new java.awt.Color(65, 105, 225));
        saveButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        saveButton.setForeground(new java.awt.Color(255, 255, 255));
        saveButton.setMnemonic(java.awt.event.KeyEvent.VK_A);
        saveButton.setText("Save");
        saveButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        employeeLabel.setDisplayedMnemonic(java.awt.event.KeyEvent.VK_T);
        employeeLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        employeeLabel.setText("Employee");

        employeeField.setEditable(false);
        employeeField.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        employeeField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        employeeField.setFocusable(false);
        employeeField.setMinimumSize(new java.awt.Dimension(440, 31));
        employeeField.setPreferredSize(new java.awt.Dimension(440, 31));

        importDatePicker.setMaximumSize(new java.awt.Dimension(215, 31));
        importDatePicker.setPlaceholder("dd/mm/yyyy");
        importDatePicker.setPreferredSize(new java.awt.Dimension(215, 31));

        importDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        importDateLabel.setText("Order Date");

        memberLabel.setDisplayedMnemonic(java.awt.event.KeyEvent.VK_T);
        memberLabel.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        memberLabel.setText("Member");

        memberField.setEditable(false);
        memberField.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        memberField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        memberField.setFocusable(false);
        memberField.setMinimumSize(new java.awt.Dimension(440, 31));
        memberField.setPreferredSize(new java.awt.Dimension(440, 31));
        memberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memberFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout groupFieldPanelLayout = new javax.swing.GroupLayout(groupFieldPanel);
        groupFieldPanel.setLayout(groupFieldPanelLayout);
        groupFieldPanelLayout.setHorizontalGroup(
            groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(groupFieldPanelLayout.createSequentialGroup()
                .addContainerGap(130, Short.MAX_VALUE)
                .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(importBookScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 839, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(groupFieldPanelLayout.createSequentialGroup()
                            .addGap(755, 755, 755)
                            .addComponent(saveButton))
                        .addGroup(groupFieldPanelLayout.createSequentialGroup()
                            .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(employeeField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(employeeLabel))
                            .addGap(18, 18, 18)
                            .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(memberField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(memberLabel))
                            .addGap(18, 18, 18)
                            .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(importDateLabel)
                                .addComponent(importDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(totalCostField, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(groupFieldPanelLayout.createSequentialGroup()
                                    .addGap(1, 1, 1)
                                    .addComponent(totalCostLabel))))))
                .addContainerGap(118, Short.MAX_VALUE))
        );
        groupFieldPanelLayout.setVerticalGroup(
            groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(groupFieldPanelLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(groupFieldPanelLayout.createSequentialGroup()
                        .addComponent(employeeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(employeeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupFieldPanelLayout.createSequentialGroup()
                        .addComponent(memberLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(memberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupFieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(groupFieldPanelLayout.createSequentialGroup()
                            .addComponent(importDateLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(importDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(groupFieldPanelLayout.createSequentialGroup()
                            .addComponent(totalCostLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(totalCostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(importBookScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(saveButton)
                .addContainerGap(937, Short.MAX_VALUE))
        );

        formScrollPane.setViewportView(groupFieldPanel);

        add(formScrollPane, java.awt.BorderLayout.CENTER);

        titleBar.setPreferredSize(new java.awt.Dimension(849, 57));

        backButton.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        backButton.setForeground(UIManager.getColor("mutedColor"));
        backButton.setIcon(SVGHelper.createSVGIconWithFilter(
            "icons/arrow-back.svg", 
            Color.white, Color.white,
            18, 18
        ));
        backButton.setMnemonic(java.awt.event.KeyEvent.VK_BACK_SPACE);
        backButton.setToolTipText("Back to previous page");
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backButton.setFocusable(false);
        backButton.setMargin(new java.awt.Insets(4, 14, 3, 14));
        backButton.setPreferredSize(new java.awt.Dimension(33, 33));
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButtonMouseExited(evt);
            }
        });
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        pageName.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        pageName.setText("Add order sheet");

        javax.swing.GroupLayout titleBarLayout = new javax.swing.GroupLayout(titleBar);
        titleBar.setLayout(titleBarLayout);
        titleBarLayout.setHorizontalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pageName)
                .addContainerGap(838, Short.MAX_VALUE))
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        titleBarLayout.setVerticalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarLayout.createSequentialGroup()
                .addGroup(titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(titleBarLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(pageName))
                    .addGroup(titleBarLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        add(titleBar, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void memberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memberFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_memberFieldActionPerformed

    private void backButtonMouseEntered(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_backButtonMouseEntered
        // TODO add your handling code here:
    }// GEN-LAST:event_backButtonMouseEntered

    private void backButtonMouseExited(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_backButtonMouseExited
        // TODO add your handling code here:
    }// GEN-LAST:event_backButtonMouseExited

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_backButtonActionPerformed
        Dashboard.dashboard.switchTab("importSheetCRUD");
    }// GEN-LAST:event_backButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveButtonActionPerformed

        int employeeInChargeId, memberId;
        Date orderDate;
        Double totalCost;

        boolean isTableValid = true;
        DefaultTableModel model = (DefaultTableModel) importBooksTable.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            String title = (String) model.getValueAt(i, 0);
           
            String quantityStr = (String) model.getValueAt(i, 1);
            String pricePerBookStr = (String) model.getValueAt(i, 2);

            if (!((title == null || title.equals(""))
                    && (quantityStr == null || quantityStr.equals(""))
                    && (pricePerBookStr == null || pricePerBookStr.equals("")))) {
                if (((title == null || title.equals(""))
                        || (quantityStr == null || quantityStr.equals(""))
                        || (pricePerBookStr == null || pricePerBookStr.equals("")))) {

                    isTableValid = false;
                    break;
                }
            }
        }

        if (isTableValid) {
            try {
                employeeInChargeId = Integer.parseInt(employeeField.getText());
                orderDate = new java.sql.Date(importDatePicker.getDate().getTime());
                memberId = Integer.parseInt(memberField.getText());
                
                if (totalCostField.getText().isEmpty()) {
                    throw new Exception("Please input the books sheet");
                }
                totalCost = Double.valueOf(totalCostField.getText());

                List<OrderedBook> orderedBooks = new ArrayList<>();

                for (int i = 0; i < model.getRowCount(); i++) {
                    String title = (String) model.getValueAt(i, 0);
                    String quantityStr = (String) model.getValueAt(i, 1);
                    String pricePerBookStr = (String) model.getValueAt(i, 2);
                    if (((title == null || title.equals(""))
                            && (quantityStr == null || quantityStr.equals(""))
                            && (pricePerBookStr == null || pricePerBookStr.equals("")))) {
                        continue;
                    }
                    Book book = bookMap.get((String) model.getValueAt(i, 0));
                    if (book == null) {
                        throw new Exception("Cannot find book: " + (String) model.getValueAt(i, 0));
                    }
                    int bookId = book.id;
                    int quantity = Integer.parseInt((String) model.getValueAt(i, 1));
                    Double pricePerBook = Double.parseDouble((String) model.getValueAt(i, 2));

                    OrderedBook orderedBook = new OrderedBook(bookId, title, quantity, pricePerBook);
                    orderedBooks.add(orderedBook);
                }

                OrderSheet orderSheet = new OrderSheet(employeeInChargeId, memberId, orderDate, totalCost,
                        orderedBooks);

                try {
                    orderSheetService.insertOrderSheet(orderSheet);
                    JOptionPane.showMessageDialog(null, "Order sheet added successfully.", "BSMS Information",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "An unspecified error occurred: " + e.getMessage(),
                            "BSMS Error",
                            JOptionPane.ERROR_MESSAGE);

                }
            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e.getMessage(), "BSMS Information",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please fill in all fields in the table.", "BSMS Information",
                    JOptionPane.ERROR_MESSAGE);
        }

    }// GEN-LAST:event_saveButtonActionPerformed

    private class AutoSuggestComboBoxEditor extends AbstractCellEditor implements TableCellEditor {

        private final JComboBox<String> comboBox = new JComboBox<>();
        private final Vector<String> suggestions = new Vector<>();

        public AutoSuggestComboBoxEditor() {
            comboBox.setEditable(true);
            comboBox.setFont(new Font("Segoe UI", 0, 16));
            JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
            textField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    EventQueue.invokeLater(() -> {
                        String text = textField.getText();
                        if (text.length() == 0) {
                            comboBox.hidePopup();
                            setModel(new DefaultComboBoxModel<>(suggestions), "");
                        } else {

                            java.util.List<Book> books;
                            try {

                                books = bookService.searchAvailableBooksByTitle(text);

                            } catch (Exception ex) {
                                books = null;

                            }
                            suggestions.clear();
                            bookMap.clear();
                            if (books != null) {
                                for (var book : books) {
                                    suggestions.add(book.title);
                                    bookMap.put(book.title, book);
                                }
                            }
                            DefaultComboBoxModel<String> model = getSuggestedModel(suggestions, text);
                            if (model.getSize() == 0) {
                                comboBox.hidePopup();
                            } else {
                                setModel(model, text);
                                comboBox.showPopup();
                            }
                        }
                    });
                }
            });
            comboBox.setMaximumRowCount(10);
            comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXX");
            comboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String selectedBookTitle = (String) comboBox.getSelectedItem();
                    if (selectedBookTitle != null) {
                        Book selectedBook = bookMap.get(selectedBookTitle);
                        if (selectedBook != null) {
                            int row = importBooksTable.getEditingRow();

                            importBooksTable.setValueAt(selectedBook.salePrice.toString(), row, 2);
                            DefaultTableModel model = (DefaultTableModel) importBooksTable.getModel();
                            model.addRow(new Object[model.getColumnCount()]);
                        }
                    }
                }
            });

        }

        private void setModel(DefaultComboBoxModel<String> model, String str) {
            comboBox.setModel(model);
            comboBox.setSelectedIndex(-1);
            comboBox.getEditor().setItem(str);
        }

        private DefaultComboBoxModel<String> getSuggestedModel(java.util.List<String> list, String text) {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (String s : list) {
                model.addElement(s);
            }
            return model;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            comboBox.setSelectedItem(value);

            return comboBox;
        }

        @Override
        public Object getCellEditorValue() {
            return comboBox.getEditor().getItem();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JTextField employeeField;
    private javax.swing.JLabel employeeLabel;
    private javax.swing.JScrollPane formScrollPane;
    private javax.swing.JPanel groupFieldPanel;
    private javax.swing.JScrollPane importBookScrollPane;
    private javax.swing.JTable importBooksTable;
    private javax.swing.JLabel importDateLabel;
    private com.group06.bsms.components.DatePickerPanel importDatePicker;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField memberField;
    private javax.swing.JLabel memberLabel;
    private javax.swing.JLabel pageName;
    private javax.swing.JButton saveButton;
    private javax.swing.JPanel titleBar;
    private javax.swing.JTextField totalCostField;
    private javax.swing.JLabel totalCostLabel;
    // End of variables declaration//GEN-END:variables
}
