import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String DATA_FILE = "transactions.dat";
    
    public static void saveTransactions(List<Transaction> transactions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(transactions));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static List<Transaction> loadTransactions() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(DATA_FILE))) {
            return (List<Transaction>) ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
} 