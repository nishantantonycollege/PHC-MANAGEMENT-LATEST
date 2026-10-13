public class MedicineFrame extends ManageFrame {
    public MedicineFrame() {
        super("Pharmacy Inventory", new String[] {"ID", "Name", "Category", "Batch No", "Quantity", "Price", "Expiry Date", "Reorder Level"}, "medicines.csv", new int[] {4, 5, 7});
    }
}
