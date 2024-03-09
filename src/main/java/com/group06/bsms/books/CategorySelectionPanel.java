package com.group06.bsms.books;

//import com.formdev.flatlaf.FlatClientProperties;
import java.util.ArrayList;
import java.util.Arrays;

public class CategorySelectionPanel extends javax.swing.JPanel {

    ArrayList<String> listUnselected = null;
    ArrayList<String> listSelected = null;

    public CategorySelectionPanel() {
        initComponents();

        String[] categories = new String[]{
            "Comic",
            "Fiction",
            "Mystery",
            "Romance",
            "Thriller",
            "Travel",
            "History",
            "Poetry",
            "Business"
        };
        listUnselected = new ArrayList<>(Arrays.asList(categories));
        listSelected = new ArrayList<>();

        listSelected.add(categories[0]);
        listSelected.add(categories[6]);

        addButton.setToolTipText("Add category");
        updateAddButton();

        if (!listSelected.isEmpty()) {
            for (String categorySelected : listSelected) {
                createToggleButton(categorySelected);
            }
        }
    }

    private void createToggleButton(String name) {
        CategoryButton categoryButton = new CategoryButton(this, name);
//        listUnselected.remove(name);
//        updateAddButton();

        add(categoryButton);
    }

    private void updateAddButton() {
        for (String category : listUnselected) {
            addButton.addItem(category);
        }
    }

    public void deleteCategory(String name) {
        listSelected.remove(name);
//        listUnselected.add(name);
//        updateAddButton();

        revalidate();
        repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JComboBox<>();

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 6));

        addButton.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        addButton.setToolTipText("");
        addButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addButton.setMinimumSize(new java.awt.Dimension(28, 32));
        addButton.setName(""); // NOI18N
        addButton.setPreferredSize(new java.awt.Dimension(28, 32));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        add(addButton);
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String newCategory = String.valueOf(addButton.getSelectedItem());
        if (!listSelected.contains(newCategory)) {
            listSelected.add(newCategory);
            createToggleButton(newCategory);
            revalidate();
            repaint();
        }
    }//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> addButton;
    // End of variables declaration//GEN-END:variables
}
