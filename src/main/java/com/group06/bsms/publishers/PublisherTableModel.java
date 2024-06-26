package com.group06.bsms.publishers;

import com.group06.bsms.books.BookCRUD;
import com.group06.bsms.components.ActionBtn;
import com.group06.bsms.components.TableActionEvent;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

class TableActionCellEditor extends DefaultCellEditor {

    private final TableActionEvent event;

    public TableActionCellEditor(TableActionEvent event) {
        super(new JCheckBox());
        this.event = event;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        PublisherTableModel model = (PublisherTableModel) table.getModel();
        int isHidden = model.getHiddenState(table.convertRowIndexToModel(row));
        int modelRow = table.convertRowIndexToModel(row);

        ActionBtn action = new ActionBtn(isHidden);
        action.initEvent(event, modelRow, isHidden);

        action.setBackground(Color.WHITE);

        return action;
    }
}

class TableActionCellRender extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        int modelRow = table.convertRowIndexToModel(row);

        int isHidden = ((PublisherTableModel) table.getModel()).getHiddenState(modelRow);

        ActionBtn action = new ActionBtn(isHidden);
        action.setBackground(Color.WHITE);

        return action;
    }
}

/**
 * A custom structure used to display a table
 */
public class PublisherTableModel extends AbstractTableModel {

    private List<Publisher> publishers = new ArrayList<>();
    private String[] columns = {"Name", "Email", "Address", "Actions"};
    public boolean editable = false;
    private final PublisherService publisherService;
    private final BookCRUD bookCRUD;

    public PublisherTableModel(PublisherService publisherService, BookCRUD bookCRUD) {
        this.bookCRUD = bookCRUD;
        this.publisherService = publisherService;
    }

    @Override
    public int getRowCount() {
        return publishers.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * @param row
     * @param col
     * @return value at [row, col] in the table
     */
    @Override
    public Object getValueAt(int row, int col) {
        if (row >= publishers.size()) {
            return null;
        }
        Publisher publisher = publishers.get(row);
        switch (col) {
            case 0:
                return publisher.name;
            case 1:
                return publisher.email;
            case 2:
                return publisher.address;
            default:
                return null;
        }
    }

    /**
     * @param val
     * @param row
     * @param col
     * @return value at [row, col] in the table
     */
    @Override
    public void setValueAt(Object val, int row, int col) {
        if (col == 3) {
            return;
        }

        if (!editable) {
            editable = true;
        }

        Publisher publisher = publishers.get(row);
        try {
            switch (col) {
                case 0:
                    if (!publisher.name.equals((String) val)) {
                        publisherService.updatePublisherAttributeById(publisher.id, "name", (String) val);
                        publisher.name = (String) val;

                        bookCRUD.loadPublisherInto();
                    }
                    break;
                case 1:
                    if (!((String) val).equals(publisher.email)) {
                        publisherService.updatePublisherAttributeById(
                                publisher.id,
                                "email",
                                ((String) val).equals("") ? null : (String) val
                        );
                        publisher.email = (String) val;
                    }
                    break;
                case 2:
                    if (!((String) val).equals(publisher.address)) {
                        publisherService.updatePublisherAttributeById(
                                publisher.id,
                                "address",
                                ((String) val).equals("") ? null : (String) val
                        );
                        publisher.address = (String) val;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            if (ex.getMessage().contains("publisher_name_key")) {
                JOptionPane.showMessageDialog(null, "A publisher with this name already exists", "BSMS Error", JOptionPane.ERROR_MESSAGE);
            } else if (ex.getMessage().contains("publisher_name_check")) {
                JOptionPane.showMessageDialog(null, "Name cannot be empty", "BSMS Error", JOptionPane.ERROR_MESSAGE);
            } else if (ex.getMessage().contains("publisher_email_check")) {
                JOptionPane.showMessageDialog(null, "Invalid email format", "BSMS Error", JOptionPane.ERROR_MESSAGE);
            } else if (ex.getMessage().contains("publisher_email_key")) {
                JOptionPane.showMessageDialog(null, "A publisher with this email already exists", "BSMS Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "BSMS Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        fireTableCellUpdated(row, col);
    }

    public Publisher getPublisher(int row) {
        return publishers.get(row);
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    public boolean contains(int id) {
        Optional<Publisher> foundPublisher = publishers.stream()
                .filter(publisher -> publisher.id == id)
                .findFirst();
        return foundPublisher.isPresent();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Boolean.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0 || columnIndex == 1 || columnIndex == 2 || columnIndex == 3);
    }

    public void reloadAllPublishers(List<Publisher> newPublishers) {
        if (newPublishers != null) {
            publishers.clear();
            fireTableDataChanged();
            for (var publisher : newPublishers) {
                if (!contains(publisher.id)) {
                    addRow(publisher);
                }
            }
        }
        editable = false;
    }

    public void loadNewPublishers(List<Publisher> newPublishers) {
        if (newPublishers != null) {
            for (var publisher : newPublishers) {
                if (!contains(publisher.id)) {
                    addRow(publisher);
                }
            }
        }
    }

    void addRow(Publisher publisher) {
        publishers.add(publisher);
    }

    void setHiddenState(int row) {
        publishers.get(row).isHidden = !publishers.get(row).isHidden;
    }

    int getHiddenState(int row) {
        Publisher publisher = publishers.get(row);
        if (publisher.isHidden) {
            return 1;
        }
        return 0;
    }
}
