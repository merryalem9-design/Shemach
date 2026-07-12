package purchasegui;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class PurchaseGUI extends JFrame {
    private Connection connection;
    
    // GUI components
    private JComboBox<String> buyerTypeComboBox;
    private JComboBox<String> itemComboBox;
    private JTextField homeIdField;
    private JLabel homeIdLabel;
    private JTextField quantityField;
    private JLabel unitLabel;
    private JComboBox<String> employeeComboBox;
    private JButton submitButton;
    private JButton historyButton;
    private JButton quotaButton;
    private JTextArea outputArea;
    
    // Data holders
    private Map<Integer, String> itemsMap = new HashMap<>();
    private Map<Integer, Boolean> itemRestrictedMap = new HashMap<>();
    private Map<String, Integer> itemNameToIdMap = new HashMap<>();
    private Map<Integer, String> employeesMap = new HashMap<>();
    
    public PurchaseGUI() {
        initializeDatabaseConnection();
        initializeUI();
        loadItems();
        loadEmployees();
    }
    
    private void initializeDatabaseConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            String serverName = "localhost";
            String databaseName = "shemachadvdb";
            String username = "user_purchase";
            String password = "user123";
            
            String url = String.format(
                "jdbc:sqlserver://%s:1433;databaseName=%s;user=%s;password=%s;" +
                "encrypt=true;trustServerCertificate=true;loginTimeout=30;",
                serverName, databaseName, username, password
            );
            
            connection = DriverManager.getConnection(url);
            System.out.println("Connected to database successfully.");
            
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "JDBC Driver not found.\n" + e.getMessage(),
                "Driver Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection failed:\n" + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("ShemachAdv Purchase System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Purchase Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Buyer Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Buyer Type:"), gbc);
        
        gbc.gridx = 1;
        buyerTypeComboBox = new JComboBox<>(new String[]{"Homeowner", "Non-homeowner"});
        buyerTypeComboBox.addActionListener(e -> updateItemListAndHomeIdVisibility());
        formPanel.add(buyerTypeComboBox, gbc);
        
        // Home ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        homeIdLabel = new JLabel("Home ID:");
        formPanel.add(homeIdLabel, gbc);
        
        gbc.gridx = 1;
        homeIdField = new JTextField(15);
        formPanel.add(homeIdField, gbc);
        
        // Item
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Item:"), gbc);
        
        gbc.gridx = 1;
        itemComboBox = new JComboBox<>();
        itemComboBox.addActionListener(e -> updateUnitLabel());
        formPanel.add(itemComboBox, gbc);
        
        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Quantity:"), gbc);
        
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quantityField = new JTextField(10);
        quantityPanel.add(quantityField);
        
        unitLabel = new JLabel("kg");
        quantityPanel.add(unitLabel);
        gbc.gridx = 1;
        formPanel.add(quantityPanel, gbc);
        
        // Employee
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Employee:"), gbc);
        
        gbc.gridx = 1;
        employeeComboBox = new JComboBox<>();
        formPanel.add(employeeComboBox, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        submitButton = new JButton("Submit Purchase");
        submitButton.addActionListener(e -> submitPurchase());
        buttonPanel.add(submitButton);
        
        historyButton = new JButton("Purchase History");
        historyButton.addActionListener(e -> showPurchaseHistory());
        buttonPanel.add(historyButton);
        
        quotaButton = new JButton("Check Quota");
        quotaButton.addActionListener(e -> checkQuota());
        buttonPanel.add(quotaButton);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);
        
        // Output area
        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        
        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Set initial visibility
        updateItemListAndHomeIdVisibility();
        
        add(mainPanel);
        pack();
    }
    
    private void loadItems() {
        try {
            itemsMap.clear();
            itemRestrictedMap.clear();
            itemNameToIdMap.clear();
            itemComboBox.removeAllItems();
            
            String query = "SELECT item_id, item_name, is_restricted FROM item";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int id = rs.getInt("item_id");
                String name = rs.getString("item_name");
                boolean restricted = rs.getString("is_restricted").equals("Yes");
                
                itemsMap.put(id, name);
                itemRestrictedMap.put(id, restricted);
                itemNameToIdMap.put(name, id);
                itemComboBox.addItem(name);
            }
        } catch (SQLException e) {
            showError("Error loading items: " + e.getMessage());
        }
    }
    
    private void loadEmployees() {
        try {
            employeesMap.clear();
            employeeComboBox.removeAllItems();
            
            String query = "SELECT emp_id, emp_name FROM employee";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int id = rs.getInt("emp_id");
                String name = rs.getString("emp_name");
                
                employeesMap.put(id, name);
                employeeComboBox.addItem(id + " - " + name);
            }
        } catch (SQLException e) {
            showError("Error loading employees: " + e.getMessage());
        }
    }
    
    private void updateItemListAndHomeIdVisibility() {
        String buyerType = (String) buyerTypeComboBox.getSelectedItem();
        boolean isHomeowner = buyerType.equals("Homeowner");
        
        homeIdLabel.setVisible(isHomeowner);
        homeIdField.setVisible(isHomeowner);
        
        updateItemList();
    }
    
    private void updateItemList() {
        String buyerType = (String) buyerTypeComboBox.getSelectedItem();
        boolean isHomeowner = buyerType.equals("Homeowner");
        
        String selectedItem = (String) itemComboBox.getSelectedItem();
        itemComboBox.removeAllItems();
        
        for (Map.Entry<Integer, String> entry : itemsMap.entrySet()) {
            int itemId = entry.getKey();
            String itemName = entry.getValue();
            boolean restricted = itemRestrictedMap.get(itemId);
            
            if (isHomeowner || !restricted) {
                itemComboBox.addItem(itemName);
            }
        }
        
        // Restore selection if possible
        if (selectedItem != null && itemComboBox.getItemCount() > 0) {
            for (int i = 0; i < itemComboBox.getItemCount(); i++) {
                if (itemComboBox.getItemAt(i).equals(selectedItem)) {
                    itemComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        updateUnitLabel();
    }
    
    private void updateUnitLabel() {
        String selectedItem = (String) itemComboBox.getSelectedItem();
        if (selectedItem != null) {
            unitLabel.setText(selectedItem.equals("Oil") ? "liter" : "kg");
        }
    }
    
    private void submitPurchase() {
        try {
            String buyerType = (String) buyerTypeComboBox.getSelectedItem();
            boolean isHomeowner = buyerType.equals("Homeowner");
            
            Integer homeId = null;
            if (isHomeowner) {
                try {
                    homeId = Integer.parseInt(homeIdField.getText().trim());
                } catch (NumberFormatException e) {
                    showError("Please enter a valid Home ID.");
                    return;
                }
            }
            
            String selectedItem = (String) itemComboBox.getSelectedItem();
            if (selectedItem == null || selectedItem.isEmpty()) {
                showError("Please select an item.");
                return;
            }
            
            int itemId = itemNameToIdMap.get(selectedItem);
            
            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity <= 0) {
                    showError("Quantity must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid quantity.");
                return;
            }
            
            String selectedEmployee = (String) employeeComboBox.getSelectedItem();
            if (selectedEmployee == null || selectedEmployee.isEmpty()) {
                showError("Please select an employee.");
                return;
            }
            int empId = Integer.parseInt(selectedEmployee.split(" - ")[0]);
            
            java.sql.Date purchaseDate = new java.sql.Date(System.currentTimeMillis());
            
            String call = "{call Insert_Purchase_WithQuotaCheck(?, ?, ?, ?, ?, ?, ?)}";
            try (CallableStatement cstmt = connection.prepareCall(call)) {
                int purchaseId = getNextPurchaseId();
                
                cstmt.setInt(1, purchaseId);
                if (homeId != null) {
                    cstmt.setInt(2, homeId);
                } else {
                    cstmt.setNull(2, Types.INTEGER);
                }
                cstmt.setInt(3, itemId);
                cstmt.setInt(4, quantity);
                cstmt.setDate(5, purchaseDate);
                cstmt.setString(6, buyerType);
                cstmt.setInt(7, empId);
                
                boolean hasResults = cstmt.execute();
                
                if (hasResults) {
                    try (ResultSet rs = cstmt.getResultSet()) {
                        if (rs.next()) {
                            int remainingQuota = rs.getInt(1);
                            outputArea.append("Remaining quota: " + remainingQuota + "\n");
                        }
                    }
                }
                
                outputArea.append("Purchase submitted successfully!\n");
                outputArea.append("Item: " + selectedItem + "\n");
                outputArea.append("Quantity: " + quantity + " " + unitLabel.getText() + "\n");
                outputArea.append("Buyer: " + buyerType + 
                                 (isHomeowner ? " (Home ID: " + homeId + ")" : "") + "\n");
                outputArea.append("Processed by: " + selectedEmployee.split(" - ")[1] + "\n\n");
                
                if (!isHomeowner) {
                    homeIdField.setText("");
                }
                quantityField.setText("");
            }
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Remaining:")) {
                showError(errorMsg);
            } else {
                showError("Database error: " + errorMsg);
            }
        }
    }
    
    private void checkQuota() {
        try {
            String buyerType = (String) buyerTypeComboBox.getSelectedItem();
            boolean isHomeowner = buyerType.equals("Homeowner");
            
            Integer homeId = isHomeowner ? Integer.parseInt(homeIdField.getText().trim()) : null;
            String selectedItem = (String) itemComboBox.getSelectedItem();
            
            if (selectedItem == null || selectedItem.isEmpty()) {
                showError("Please select an item first.");
                return;
            }
            
            int itemId = itemNameToIdMap.get(selectedItem);
            Date currentDate = new Date();
            
            String query = "SELECT i.max_per_month - COALESCE(SUM(p.quantity), 0) AS remaining_quota " +
                           "FROM item i LEFT JOIN purchase p ON " +
                           "p.item_id = i.item_id AND " +
                           "p.buyer_type = ? AND " +
                           "MONTH(p.purchase_date) = MONTH(?) AND " +
                           "YEAR(p.purchase_date) = YEAR(?) " +
                           (isHomeowner ? "AND p.home_id = ? " : "") +
                           "WHERE i.item_id = ? " +
                           "GROUP BY i.max_per_month";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, buyerType);
            pstmt.setDate(2, new java.sql.Date(currentDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(currentDate.getTime()));
            if (isHomeowner) {
                pstmt.setInt(4, homeId);
                pstmt.setInt(5, itemId);
            } else {
                pstmt.setInt(4, itemId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int remaining = rs.getInt("remaining_quota");
                JOptionPane.showMessageDialog(this, 
                    "Remaining quota for " + selectedItem + ": " + remaining + " " + 
                    (selectedItem.equals("Oil") ? "liters" : "kg"),
                    "Quota Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // If no purchases made yet, get max quota directly
                query = "SELECT max_per_month FROM item WHERE item_id = ?";
                pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, itemId);
                rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int maxQuota = rs.getInt("max_per_month");
                    JOptionPane.showMessageDialog(this, 
                        "Full quota available for " + selectedItem + ": " + maxQuota + " " + 
                        (selectedItem.equals("Oil") ? "liters" : "kg"),
                        "Quota Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid Home ID");
        } catch (SQLException e) {
            showError("Error checking quota: " + e.getMessage());
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }
    
    private void showPurchaseHistory() {
        String buyerType = (String) buyerTypeComboBox.getSelectedItem();
        if (!buyerType.equals("Homeowner")) {
            showError("Purchase history is only available for homeowners");
            return;
        }
        
        try {
            int homeId = Integer.parseInt(homeIdField.getText().trim());
            
            String query = "SELECT p.purchase_id, i.item_name, p.quantity, " +
                          "CASE WHEN i.item_name = 'Oil' THEN 'liter' ELSE 'kg' END as unit, " +
                          "p.purchase_date, e.emp_name " +
                          "FROM purchase p " +
                          "JOIN item i ON p.item_id = i.item_id " +
                          "JOIN employee e ON p.emp_id = e.emp_id " +
                          "WHERE p.home_id = ? " +
                          "ORDER BY p.purchase_date DESC";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, homeId);
            ResultSet rs = pstmt.executeQuery();
            
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Item");
            model.addColumn("Quantity");
            model.addColumn("Unit");
            model.addColumn("Date");
            model.addColumn("Employee");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getString("unit"),
                    rs.getDate("purchase_date"),
                    rs.getString("emp_name")
                });
            }
            
            if (model.getRowCount() == 0) {
                showError("No purchase history found for Home ID: " + homeId);
                return;
            }
            
            JTable historyTable = new JTable(model);
            historyTable.setAutoCreateRowSorter(true);
            
            JScrollPane scrollPane = new JScrollPane(historyTable);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Purchase History for Home ID: " + homeId, JOptionPane.PLAIN_MESSAGE);
            
        } catch (NumberFormatException e) {
            showError("Please enter a valid Home ID");
        } catch (SQLException e) {
            showError("Error loading purchase history: " + e.getMessage());
        }
    }
    
    private int getNextPurchaseId() throws SQLException {
        String query = "SELECT MAX(purchase_id) FROM purchase";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1;
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                PurchaseGUI gui = new PurchaseGUI();
                gui.setVisible(true);
                gui.setSize(700, 600);
                gui.setLocationRelativeTo(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}