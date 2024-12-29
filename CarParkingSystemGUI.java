import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;

class CarParkingSystem {
    private int capacity;
    private int[] slots;
    private String[] ownerNames;
    private String[] ownerContacts;
    private HashMap<Integer, Long> parkingStartTimes;

    private JFrame frame;
    private JTextField carNumberField, ownerNameField, ownerContactField, slotField;
    private JLabel[] slotLabels;

    public CarParkingSystem(int capacity) {
        this.capacity = capacity;
        this.slots = new int[capacity];
        this.ownerNames = new String[capacity];
        this.ownerContacts = new String[capacity];
        this.parkingStartTimes = new HashMap<>();
        this.slotLabels = new JLabel[capacity];
        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("Car Parking System");
        frame.setSize(800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel(new GridLayout(6, 2));
        JLabel carNumberLabel = new JLabel("Car Number:");
        JLabel ownerNameLabel = new JLabel("Owner Name:");
        JLabel ownerContactLabel = new JLabel("Owner Contact:");
        JLabel slotLabel = new JLabel("Slot Number:");

        carNumberField = new JTextField();
        ownerNameField = new JTextField();
        ownerContactField = new JTextField();
        slotField = new JTextField();

        JButton parkButton = new JButton("Park Car");
        JButton removeButton = new JButton("Remove Car");
        JButton resetButton = new JButton("Reset System");
        JButton showSlotsButton = new JButton("Show Slots");
        JButton showAllDetailsButton = new JButton("Show All Details");

        controlPanel.add(carNumberLabel);
        controlPanel.add(carNumberField);
        controlPanel.add(ownerNameLabel);
        controlPanel.add(ownerNameField);
        controlPanel.add(ownerContactLabel);
        controlPanel.add(ownerContactField);
        controlPanel.add(slotLabel);
        controlPanel.add(slotField);
        controlPanel.add(parkButton);
        controlPanel.add(removeButton);
        controlPanel.add(resetButton);
        controlPanel.add(showSlotsButton);
        controlPanel.add(showAllDetailsButton);

        JPanel slotPanel = new JPanel(new GridLayout(0, Math.min(10, capacity)));
        for (int i = 0; i < capacity; i++) {
            slotLabels[i] = new JLabel("<html>Slot " + (i + 1) + ":<br>Empty</html>", SwingConstants.CENTER);
            slotLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            slotLabels[i].setOpaque(true);
            slotLabels[i].setBackground(Color.GREEN);
            slotPanel.add(slotLabels[i]);
        }

        parkButton.addActionListener(e -> parkCar());
        removeButton.addActionListener(e -> removeCar());
        resetButton.addActionListener(e -> resetParkingLot());
        showSlotsButton.addActionListener(e -> showSlots());
        showAllDetailsButton.addActionListener(e -> showAllDetails());

        frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        frame.getContentPane().add(slotPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void parkCar() {
        try {
            int carNumber = Integer.parseInt(carNumberField.getText().trim());
            String ownerName = ownerNameField.getText().trim();
            String ownerContact = ownerContactField.getText().trim();
            int slot = Integer.parseInt(slotField.getText().trim()) - 1;

            if (slot < 0 || slot >= capacity) {
                JOptionPane.showMessageDialog(frame, "Invalid slot number. Enter a value between 1 and " + capacity + ".");
                return;
            }

            if (slots[slot] == 0) {
                slots[slot] = carNumber;
                ownerNames[slot] = ownerName;
                ownerContacts[slot] = ownerContact;
                parkingStartTimes.put(slot, System.currentTimeMillis());
                updateSlotVisual(slot, carNumber, ownerName);
                clearFields();
                JOptionPane.showMessageDialog(frame, "Car " + carNumber + " parked in slot " + (slot + 1));
                notifyAdjacentCars(slot);
            } else {
                JOptionPane.showMessageDialog(frame, "Slot " + (slot + 1) + " is already occupied.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input. Please check all fields.");
        }
    }

    private void notifyAdjacentCars(int slot) {
        StringBuilder adjacentMessage = new StringBuilder("Nearby cars parked:\n");

        if (slot > 0 && slots[slot - 1] != 0) {
            adjacentMessage.append("Car ").append(slots[slot - 1]).append(" is parked in slot ").append(slot).append("\n");
        }
        if (slot < capacity - 1 && slots[slot + 1] != 0) {
            adjacentMessage.append("Car ").append(slots[slot + 1]).append(" is parked in slot ").append(slot + 2).append("\n");
        }
        if (adjacentMessage.length() > 20) {
            JOptionPane.showMessageDialog(frame, adjacentMessage.toString());
        }
    }

    private void removeCar() {
        try {
            int carNumber = Integer.parseInt(carNumberField.getText().trim());
            boolean found = false;

            for (int i = 0; i < capacity; i++) {
                if (slots[i] == carNumber) {
                    long endTime = System.currentTimeMillis();
                    long startTime = parkingStartTimes.get(i);
                    long durationInHours = (endTime - startTime) / (1000 * 60 * 60);
                    int charges = (int) (durationInHours == 0 ? 20 : durationInHours * 20);

                    int option = JOptionPane.showConfirmDialog(frame, "Pay Rs. " + charges + " manually?", "Offline Payment",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(frame, "Payment confirmed!\nReceipt: \nCar Number: " + carNumber +
                                "\nSlot: " + (i + 1) + "\nTotal Charges: Rs. " + charges);
                        slots[i] = 0;
                        ownerNames[i] = null;
                        ownerContacts[i] = null;
                        parkingStartTimes.remove(i);
                        updateSlotVisualToEmpty(i);
                        found = true;
                        break;
                    } else {
                        JOptionPane.showMessageDialog(frame, "Payment not completed. Car removal canceled.");
                        return;
                    }
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(frame, "Car not found.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input. Please enter a valid car number.");
        }
    }

    private void showSlots() {
        StringBuilder slotDetails = new StringBuilder("Parking Slot Details:\n");

        for (int i = 0; i < capacity; i++) {
            if (slots[i] == 0) {
                slotDetails.append("Slot ").append(i + 1).append(": Empty\n");
            } else {
                slotDetails.append("Slot ").append(i + 1).append(" (Car No: ").append(slots[i])
                        .append(")\nOwner Name: ").append(ownerNames[i])
                        .append("\nOwner Contact: ").append(ownerContacts[i])
                        .append("\n\n");
            }
        }

        JOptionPane.showMessageDialog(frame, slotDetails.toString());
    }

    private void showAllDetails() {
        StringBuilder allDetails = new StringBuilder("All Parking Slot Details:\n");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        for (int i = 0; i < capacity; i++) {
            if (slots[i] == 0) {
                allDetails.append("Slot ").append(i + 1).append(": Empty\n");
            } else {
                allDetails.append("Slot ").append(i + 1)
                        .append("\nCar Number: ").append(slots[i])
                        .append("\nOwner Name: ").append(ownerNames[i])
                        .append("\nOwner Contact: ").append(ownerContacts[i])
                        .append("\nParking Start Time: ").append(sdf.format(new Date(parkingStartTimes.get(i))))
                        .append("\n\n");
            }
        }

        JOptionPane.showMessageDialog(frame, allDetails.toString());
    }

    private void updateSlotVisual(int slot, int carNumber, String ownerName) {
        slotLabels[slot].setText("<html>Car: " + carNumber + "<br>Owner: " + ownerName + "</html>");
        slotLabels[slot].setBackground(Color.RED);
    }

    private void updateSlotVisualToEmpty(int slot) {
        slotLabels[slot].setText("<html>Slot " + (slot + 1) + ":<br>Empty</html>");
        slotLabels[slot].setBackground(Color.GREEN);
    }

    private void clearFields() {
        carNumberField.setText("");
        ownerNameField.setText("");
        ownerContactField.setText("");
        slotField.setText("");
    }

    private void resetParkingLot() {
        for (int i = 0; i < capacity; i++) {
            slots[i] = 0;
            ownerNames[i] = null;
            ownerContacts[i] = null;
            parkingStartTimes.remove(i);
            updateSlotVisualToEmpty(i);
        }
        JOptionPane.showMessageDialog(frame, "Parking lot system has been reset.");
    }
}

public class CarParkingSystemGUI {
    public static void main(String[] args) {
        try {
            int capacity = Integer.parseInt(JOptionPane.showInputDialog("Enter parking lot capacity:"));
            new CarParkingSystem(capacity);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid parking lot capacity input.");
        }
    }
}
