import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

class CalendarPicker extends JDialog {
    private JLabel monthLabel;
    private JPanel daysPanel;
    private Calendar calendar;
    private JTextField dateField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private String fromStation;
    private String toStation;

    public CalendarPicker(JFrame parent, JTextField dateField, String fromStation, String toStation) {
        super(parent, "Select Date", true);
        this.dateField = dateField;
        this.fromStation = fromStation;
        this.toStation = toStation;

        calendar = Calendar.getInstance();

        setSize(320, 350);
        setLayout(new BorderLayout());

        // Month navigation panel
        JPanel navPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("<");
        prevButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        JButton nextButton = new JButton(">");
        nextButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        monthLabel = new JLabel("", JLabel.CENTER);
        navPanel.add(prevButton, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        navPanel.add(nextButton, BorderLayout.EAST);

        // Days panel
        daysPanel = new JPanel(new GridLayout(0, 7));
        daysPanel.setPreferredSize(new Dimension(280, 200));
        updateCalendar();

        add(navPanel, BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);
    }

    private void updateCalendar() {
        daysPanel.removeAll();

        // Day headers
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName, JLabel.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            daysPanel.add(dayLabel);
        }

        monthLabel.setText(new SimpleDateFormat("MMMM yyyy").format(calendar.getTime()));

        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK); 
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty cells before the first day
        for (int i = 1; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        // Add day buttons
        for (int i = 1; i <= daysInMonth; i++) {
            JButton dayButton = new JButton(String.valueOf(i));
            dayButton.setFont(new Font("Arial", Font.BOLD, 14));
            dayButton.setMargin(new Insets(0, 0, 0, 0));
            dayButton.setPreferredSize(new Dimension(40, 40));

            final int day = i;
            calendar.set(Calendar.DAY_OF_MONTH, day);
            // Check train availability
            if (isTrainAvailable(fromStation, toStation)) {
                dayButton.setBackground(Color.GREEN);
                dayButton.setToolTipText("Train available");
            } else {
                dayButton.setBackground(Color.LIGHT_GRAY);
                dayButton.setToolTipText("No train on this route");
            }

            dayButton.addActionListener(e -> {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                dateField.setText(dateFormat.format(calendar.getTime()));
                dispose();
            });

            daysPanel.add(dayButton);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private boolean isTrainAvailable(String from, String to) {
        for (Train train : PakistanRailwaySystem.getAllTrains()) {
            if (train.schedule.containsKey(PakistanRailwaySystem.getStation(from)) &&
                    train.schedule.containsKey(PakistanRailwaySystem.getStation(to))) {
                return true;
            }
        }
        return false;
    }
}

class Station {
    String name;
    List<Track> tracks = new ArrayList<>();

    Station(String name) {
        this.name = name;
    }
}

class Track {
    Station from;
    Station to;
    int distance;

    Track(Station from, Station to, int distance) {
        this.from = from;
        this.to = to;
        this.distance = distance;
    }
}

class Train {
    String id;
    String name;
    Map<Station, String[]> schedule = new LinkedHashMap<>();
    int delay;
    Map<String, Integer> seatPrices = new HashMap<>(); 

    Train(String id, String name) {
        this.id = id;
        this.name = name;
        seatPrices.put("Economy", 500);
        seatPrices.put("Business", 1000);
        seatPrices.put("AC", 1500);
    }
}

class Booking {
    Train train;
    String from;
    String to;
    String date;
    String passengerName;
    String gender;
    String seatType;
    String paymentInfo;
    int price;
    String bookingId;

    Booking(Train train, String from, String to, String date, String passengerName,
            String gender, String seatType, String paymentInfo) {
        this.train = train;
        this.from = from;
        this.to = to;
        this.date = date;
        this.passengerName = passengerName;
        this.gender = gender;
        this.seatType = seatType;
        this.paymentInfo = paymentInfo;
        this.price = train.seatPrices.get(seatType);
        this.bookingId = generateBookingId();
    }

    private String generateBookingId() {
        return "PKR-" + System.currentTimeMillis() % 100000;
    }
}

public class PakistanRailwaySystem {
    private static Map<String, Station> stations = new HashMap<>();
    private static Map<String, Train> trains = new HashMap<>();
    private static List<Booking> bookings = new ArrayList<>();
    private static Train selectedTrain;
    private static String fromStation;
    private static String toStation;
    private static String travelDate;
    private static String currentUser = "";

    public static Station getStation(String name) {
        return stations.get(name);
    }

    public static Collection<Train> getAllTrains() {
        return trains.values();
    }

    public static void main(String[] args) {
        setupData();
        SwingUtilities.invokeLater(PakistanRailwaySystem::createWelcomePage);
    }

    private static void setupData() {
        Station karachi = new Station("Karachi");
        Station lahore = new Station("Lahore");
        Station islamabad = new Station("Islamabad");
        Station peshawar = new Station("Peshawar");
        Station quetta = new Station("Quetta");
        Station multan = new Station("Multan");
        Station faisalabad = new Station("Faisalabad");
        Station rawalpindi = new Station("Rawalpindi");
        Station hyderabad = new Station("Hyderabad");
        Station sialkot = new Station("Sialkot");
        Station sukker = new Station("Sukkur");
        Station bahawalpur = new Station("Bahawalpur");

        stations.put("Karachi", karachi);
        stations.put("Lahore", lahore);
        stations.put("Islamabad", islamabad);
        stations.put("Peshawar", peshawar);
        stations.put("Quetta", quetta);
        stations.put("Multan", multan);
        stations.put("Faisalabad", faisalabad);
        stations.put("Rawalpindi", rawalpindi);
        stations.put("Hyderabad", hyderabad);
        stations.put("Sialkot", sialkot);
        stations.put("Sukkur", sukker);
        stations.put("Bahawalpur", bahawalpur);

        karachi.tracks.add(new Track(karachi, hyderabad, 164));
        hyderabad.tracks.add(new Track(hyderabad, karachi, 164));

        karachi.tracks.add(new Track(karachi, sukker, 470));
        sukker.tracks.add(new Track(sukker, karachi, 470));

        sukker.tracks.add(new Track(sukker, multan, 400));
        multan.tracks.add(new Track(multan, sukker, 400));

        lahore.tracks.add(new Track(lahore, islamabad, 380));
        islamabad.tracks.add(new Track(islamabad, lahore, 380));

        lahore.tracks.add(new Track(lahore, peshawar, 520));
        peshawar.tracks.add(new Track(peshawar, lahore, 520));

        islamabad.tracks.add(new Track(islamabad, peshawar, 180));
        peshawar.tracks.add(new Track(peshawar, islamabad, 180));

        lahore.tracks.add(new Track(lahore, multan, 350));
        multan.tracks.add(new Track(multan, lahore, 350));

        multan.tracks.add(new Track(multan, bahawalpur, 90));
        bahawalpur.tracks.add(new Track(bahawalpur, multan, 90));

        multan.tracks.add(new Track(multan, quetta, 700));
        quetta.tracks.add(new Track(quetta, multan, 700));

        lahore.tracks.add(new Track(lahore, faisalabad, 130));
        faisalabad.tracks.add(new Track(faisalabad, lahore, 130));

        lahore.tracks.add(new Track(lahore, sialkot, 125));
        sialkot.tracks.add(new Track(sialkot, lahore, 125));

        // Pakistani trains with different classes
        Train greenLine = new Train("PK101", "Green Line Express");
        greenLine.schedule.put(karachi, new String[] { "08:00", "08:15" });
        greenLine.schedule.put(lahore, new String[] { "16:30", "16:45" });
        greenLine.schedule.put(islamabad, new String[] { "19:30", "19:45" });
        trains.put("PK101", greenLine);

        Train shalimarExpress = new Train("PK202", "Shalimar Express");
        shalimarExpress.schedule.put(lahore, new String[] { "07:00", "07:15" });
        shalimarExpress.schedule.put(karachi, new String[] { "19:30", "19:45" });
        trains.put("PK202", shalimarExpress);

        Train khyberMail = new Train("PK303", "Khyber Mail");
        khyberMail.schedule.put(karachi, new String[] { "09:00", "09:15" });
        khyberMail.schedule.put(peshawar, new String[] { "22:30", "22:45" });
        trains.put("PK303", khyberMail);

        Train awamExpress = new Train("PK404", "Awam Express");
        awamExpress.schedule.put(karachi, new String[] { "10:00", "10:15" });
        awamExpress.schedule.put(islamabad, new String[] { "18:30", "18:45" });
        trains.put("PK404", awamExpress);

        Train bolanMail = new Train("PK505", "Bolan Mail");
        bolanMail.schedule.put(karachi, new String[] { "11:00", "11:15" });
        bolanMail.schedule.put(quetta, new String[] { "20:30", "20:45" });
        trains.put("PK505", bolanMail);

        Train subakRaftar = new Train("PK606", "Subak Raftar");
        subakRaftar.schedule.put(lahore, new String[] { "12:00", "12:15" });
        subakRaftar.schedule.put(islamabad, new String[] { "15:30", "15:45" });
        trains.put("PK606", subakRaftar);

        Train pakBusiness = new Train("PK707", "Pak Business Express");
        pakBusiness.schedule.put(karachi, new String[] { "14:00", "14:15" });
        pakBusiness.schedule.put(lahore, new String[] { "21:30", "21:45" });
        trains.put("PK707", pakBusiness);
    }

    private static void createWelcomePage() {
        JFrame welcomeFrame = new JFrame("Pakistan Railways");
        welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeFrame.setSize(500, 700);
        welcomeFrame.setLayout(new BorderLayout());
        welcomeFrame.getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(500, 80));
        JLabel titleLabel = new JLabel("PAKISTAN RAILWAYS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        ImageIcon logoIcon = new ImageIcon("pak_rail_logo.png"); // Add your logo image
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Welcome to Railway Management System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton enterButton = new JButton("Enter System");
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterButton.setPreferredSize(new Dimension(150, 40));
        enterButton.setBackground(new Color(0, 102, 0));
        enterButton.setForeground(Color.WHITE);
        enterButton.setFont(new Font("Arial", Font.BOLD, 14));
        enterButton.addActionListener(e -> {
            welcomeFrame.dispose();
            createLoginPage();
        });

        mainPanel.add(logoLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(enterButton);

        welcomeFrame.add(headerPanel, BorderLayout.NORTH);
        welcomeFrame.add(mainPanel, BorderLayout.CENTER);
        welcomeFrame.setVisible(true);
    }

    private static void createLoginPage() {
        JFrame loginFrame = new JFrame("Pakistan Railways Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(500, 600);
        loginFrame.setLayout(new BorderLayout());
        loginFrame.getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(500, 80));
        JLabel titleLabel = new JLabel("PAKISTAN RAILWAYS LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 20));
        formPanel.setBackground(new Color(240, 240, 240));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JPasswordField passField = new JPasswordField();

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 102, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (isValidLogin(username, password)) {
                currentUser = username;
                loginFrame.dispose();
                createMainMenu();
            } else {
                messageLabel.setText("Invalid username or password");
            }
        });

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);
        formPanel.add(new JLabel("")); 
        formPanel.add(messageLabel);
        formPanel.add(new JLabel("")); 
        formPanel.add(loginButton);

        mainPanel.add(formPanel);

        loginFrame.add(headerPanel, BorderLayout.NORTH);
        loginFrame.add(mainPanel, BorderLayout.CENTER);
        loginFrame.setVisible(true);
    }

    private static boolean isValidLogin(String username, String password) {
        return ("admin".equals(username) && "admin".equals(password)) ||
                ("user".equals(username) && "user123".equals(password));
    }

    private static void createMainMenu() {
        JFrame menuFrame = new JFrame("Pakistan Railways - Main Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(500, 600);
        menuFrame.setLayout(new BorderLayout());
        menuFrame.getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(500, 80));
        JLabel titleLabel = new JLabel("PAKISTAN RAILWAYS - MAIN MENU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton bookTicketButton = new JButton("Book Ticket");
        bookTicketButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookTicketButton.setPreferredSize(new Dimension(200, 40));
        bookTicketButton.setBackground(new Color(0, 102, 0));
        bookTicketButton.setForeground(Color.WHITE);
        bookTicketButton.addActionListener(e -> {
            menuFrame.dispose();
            createSearchPage();
        });

        JButton viewBookingsButton = new JButton("View My Bookings");
        viewBookingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewBookingsButton.setPreferredSize(new Dimension(200, 40));
        viewBookingsButton.setBackground(new Color(0, 102, 0));
        viewBookingsButton.setForeground(Color.WHITE);
        viewBookingsButton.addActionListener(e -> {
            menuFrame.dispose();
            viewBookings();
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setPreferredSize(new Dimension(200, 40));
        logoutButton.addActionListener(e -> {
            menuFrame.dispose();
            createWelcomePage();
        });

        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(bookTicketButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(viewBookingsButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(logoutButton);

        menuFrame.add(headerPanel, BorderLayout.NORTH);
        menuFrame.add(mainPanel, BorderLayout.CENTER);
        menuFrame.setVisible(true);
    }

    private static void createSearchPage() {
        JFrame searchFrame = new JFrame("Search Trains - Pakistan Railways");
        searchFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        searchFrame.setSize(600, 600);
        searchFrame.setLayout(new BorderLayout());
        searchFrame.getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(600, 80));
        JLabel titleLabel = new JLabel("SEARCH TRAINS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 20));
        formPanel.setBackground(new Color(240, 240, 240));

        JLabel fromLabel = new JLabel("From Station:");
        fromLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JComboBox<String> fromField = new JComboBox<>(stations.keySet().toArray(new String[0]));

        JLabel toLabel = new JLabel("To Station:");
        toLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JComboBox<String> toField = new JComboBox<>(stations.keySet().toArray(new String[0]));

        JLabel dateLabel = new JLabel("Travel Date:");
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField dateField = new JTextField();
        dateField.setEditable(false);
        JButton dateButton = new JButton("Select Date");
        dateButton.addActionListener(e -> {
            CalendarPicker calendarPicker = new CalendarPicker(searchFrame, dateField,
                    (String) fromField.getSelectedItem(),
                    (String) toField.getSelectedItem());
            calendarPicker.setVisible(true);
        });

        JButton searchButton = new JButton("Search Trains");
        searchButton.setBackground(new Color(0, 102, 0));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> {
            fromStation = (String) fromField.getSelectedItem();
            toStation = (String) toField.getSelectedItem();
            travelDate = dateField.getText();

            if (fromStation.equals(toStation)) {
                JOptionPane.showMessageDialog(searchFrame, "Departure and arrival stations cannot be same!");
                return;
            }

            if (travelDate.isEmpty()) {
                JOptionPane.showMessageDialog(searchFrame, "Please select travel date!");
                return;
            }

            searchFrame.dispose();
            createTrainListPage();
        });

        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> {
            searchFrame.dispose();
            createMainMenu();
        });

        formPanel.add(fromLabel);
        formPanel.add(fromField);
        formPanel.add(toLabel);
        formPanel.add(toField);
        formPanel.add(dateLabel);
        formPanel.add(dateButton);
        formPanel.add(searchButton);
        formPanel.add(backButton);

        mainPanel.add(formPanel);

        searchFrame.add(headerPanel, BorderLayout.NORTH);
        searchFrame.add(mainPanel, BorderLayout.CENTER);
        searchFrame.setVisible(true);
    }

    private static void createTrainListPage() {
        JFrame trainListFrame = new JFrame("Available Trains - Pakistan Railways");
        trainListFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        trainListFrame.setSize(800, 600);
        trainListFrame.setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(800, 80));
        JLabel titleLabel = new JLabel("AVAILABLE TRAINS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[][] data = getTrainData(fromStation, toStation);
        String[] column = { "Train ID", "Train Name", "Departure", "Arrival", "Duration" };

        JTable trainListTable = new JTable(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        trainListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trainListTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(trainListTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton bookButton = new JButton("Book Selected Train");
        bookButton.setBackground(new Color(0, 102, 0));
        bookButton.setForeground(Color.WHITE);
        bookButton.addActionListener(e -> {
            int selectedRow = trainListTable.getSelectedRow();
            if (selectedRow >= 0) {
                String selectedTrainId = (String) trainListTable.getValueAt(selectedRow, 0);
                selectedTrain = trains.get(selectedTrainId);
                trainListFrame.dispose();
                createBookingPage();
            } else {
                JOptionPane.showMessageDialog(trainListFrame, "Please select a train first!");
            }
        });

        JButton backButton = new JButton("Back to Search");
        backButton.addActionListener(e -> {
            trainListFrame.dispose();
            createSearchPage();
        });

        buttonPanel.add(backButton);
        buttonPanel.add(bookButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        trainListFrame.add(headerPanel, BorderLayout.NORTH);
        trainListFrame.add(mainPanel, BorderLayout.CENTER);
        trainListFrame.setVisible(true);
    }

    private static String[][] getTrainData(String from, String to) {
        List<String[]> trainData = new ArrayList<>();
        for (Train train : trains.values()) {
            if (train.schedule.containsKey(stations.get(from)) && train.schedule.containsKey(stations.get(to))) {
                String[] scheduleFrom = train.schedule.get(stations.get(from));
                String[] scheduleTo = train.schedule.get(stations.get(to));

                String duration = calculateDuration(scheduleFrom[0], scheduleTo[1]);

                trainData.add(new String[] {
                        train.id,
                        train.name,
                        scheduleFrom[0] + " (" + from + ")",
                        scheduleTo[1] + " (" + to + ")",
                        duration
                });
            }
        }
        return trainData.toArray(new String[0][0]);
    }

    private static String calculateDuration(String departure, String arrival) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date departTime = format.parse(departure);
            Date arriveTime = format.parse(arrival);

            long diff = arriveTime.getTime() - departTime.getTime();
            long diffMinutes = diff / (60 * 1000);
            long hours = diffMinutes / 60;
            long minutes = diffMinutes % 60;

            return String.format("%dh %02dm", hours, minutes);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private static void createBookingPage() {
        JFrame bookingFrame = new JFrame("Book Ticket - Pakistan Railways");
        bookingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        bookingFrame.setSize(600, 700);
        bookingFrame.setLayout(new BorderLayout());
        bookingFrame.getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(600, 80));
        JLabel titleLabel = new JLabel("BOOK TICKET", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Train Info Panel
        JPanel trainInfoPanel = new JPanel(new GridLayout(3, 1, 10, 5));
        trainInfoPanel.setBorder(BorderFactory.createTitledBorder("Train Information"));

        JLabel trainNameLabel = new JLabel("Train: " + selectedTrain.name);
        JLabel routeLabel = new JLabel("Route: " + fromStation + " to " + toStation);
        JLabel dateLabel = new JLabel("Date: " + travelDate);

        trainInfoPanel.add(trainNameLabel);
        trainInfoPanel.add(routeLabel);
        trainInfoPanel.add(dateLabel);

        // Passenger Details Panel
        JPanel passengerPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        passengerPanel.setBorder(BorderFactory.createTitledBorder("Passenger Details"));

        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField();

        JLabel genderLabel = new JLabel("Gender:");
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup genderGroup = new ButtonGroup();
        JRadioButton maleButton = new JRadioButton("Male");
        JRadioButton femaleButton = new JRadioButton("Female");
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        genderPanel.add(maleButton);
        genderPanel.add(femaleButton);

        JLabel seatLabel = new JLabel("Seat Class:");
        JComboBox<String> seatCombo = new JComboBox<>(new String[] { "Economy", "Business", "AC" });
        seatCombo.addActionListener(e -> {
        });

        JLabel priceLabel = new JLabel("Price:");
        JLabel priceValueLabel = new JLabel("Select seat class to see price");
        seatCombo.addActionListener(e -> {
            String selectedClass = (String) seatCombo.getSelectedItem();
            priceValueLabel.setText("Rs. " + selectedTrain.seatPrices.get(selectedClass));
        });

        JLabel paymentLabel = new JLabel("Payment Method:");
        JComboBox<String> paymentCombo = new JComboBox<>(new String[] {
                "Credit Card", "Debit Card", "JazzCash", "EasyPaisa", "Bank Transfer", "Cash"
        });

        passengerPanel.add(nameLabel);
        passengerPanel.add(nameField);
        passengerPanel.add(genderLabel);
        passengerPanel.add(genderPanel);
        passengerPanel.add(seatLabel);
        passengerPanel.add(seatCombo);
        passengerPanel.add(priceLabel);
        passengerPanel.add(priceValueLabel);
        passengerPanel.add(paymentLabel);
        passengerPanel.add(paymentCombo);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton confirmButton = new JButton("Confirm Booking");
        confirmButton.setBackground(new Color(0, 102, 0));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(e -> {
            String passengerName = nameField.getText();
            String gender = maleButton.isSelected() ? "Male" : femaleButton.isSelected() ? "Female" : "";
            String seatType = (String) seatCombo.getSelectedItem();
            String paymentInfo = (String) paymentCombo.getSelectedItem();

            if (passengerName.isEmpty()) {
                JOptionPane.showMessageDialog(bookingFrame, "Please enter passenger name!");
                return;
            }

            if (gender.isEmpty()) {
                JOptionPane.showMessageDialog(bookingFrame, "Please select gender!");
                return;
            }

            Booking booking = new Booking(
                    selectedTrain, fromStation, toStation, travelDate,
                    passengerName, gender, seatType, paymentInfo);
            bookings.add(booking);
            updateBookingFile();
            bookingFrame.dispose();
            createThankYouPage(booking);
        });

        JButton backButton = new JButton("Back to Trains");
        backButton.addActionListener(e -> {
            bookingFrame.dispose();
            createTrainListPage();
        });

        buttonPanel.add(backButton);
        buttonPanel.add(confirmButton);

        mainPanel.add(trainInfoPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(passengerPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(buttonPanel);

        bookingFrame.add(headerPanel, BorderLayout.NORTH);
        bookingFrame.add(mainPanel, BorderLayout.CENTER);
        bookingFrame.setVisible(true);
    }

    private static void updateBookingFile() {
        try {
            FileWriter writer = new FileWriter("pakistan_railway_bookings.txt", true);
            for (Booking booking : bookings) {
                writer.write(
                        currentUser + "," +
                                booking.bookingId + "," +
                                booking.train.id + "," +
                                booking.train.name + "," +
                                booking.from + "," +
                                booking.to + "," +
                                booking.date + "," +
                                booking.passengerName + "," +
                                booking.gender + "," +
                                booking.seatType + "," +
                                booking.price + "," +
                                booking.paymentInfo + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void viewBookings() {
        JFrame bookingsFrame = new JFrame("My Bookings - Pakistan Railways");
        bookingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        bookingsFrame.setSize(900, 600);
        bookingsFrame.setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(900, 80));
        JLabel titleLabel = new JLabel("MY BOOKINGS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Read bookings from file
        List<String[]> bookingData = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new java.io.File("pakistan_railway_bookings.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 12 && parts[0].equals(currentUser)) {
                    bookingData.add(parts);
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] column = { "Booking ID", "Train", "From", "To", "Date", "Passenger", "Class", "Price", "Payment" };
        String[][] data = new String[bookingData.size()][column.length];

        for (int i = 0; i < bookingData.size(); i++) {
            String[] booking = bookingData.get(i);
            data[i][0] = booking[1]; // Booking ID
            data[i][1] = booking[3]; // Train name
            data[i][2] = booking[4]; // From
            data[i][3] = booking[5]; // To
            data[i][4] = booking[6]; // Date
            data[i][5] = booking[7]; // Passenger
            data[i][6] = booking[9]; // Class
            data[i][7] = "Rs. " + booking[10]; // Price
            data[i][8] = booking[11]; // Payment
        }

        JTable bookingsTable = new JTable(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> {
            bookingsFrame.dispose();
            createMainMenu(); 
        });
        buttonPanel.add(backButton);

        JButton cancelButton = new JButton("Cancel Selected Booking");
        cancelButton.setBackground(Color.RED);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> {
            int selectedRow = bookingsTable.getSelectedRow();
            if (selectedRow >= 0) {
                String bookingId = (String) bookingsTable.getValueAt(selectedRow, 0); 
                int confirm = JOptionPane.showConfirmDialog(bookingsFrame,
                        "Are you sure you want to cancel booking ID: " + bookingId + "?",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    cancelBooking(bookingId);
                    bookingsFrame.dispose();
                    viewBookings(); 
                }
            } else {
                JOptionPane.showMessageDialog(bookingsFrame, "Please select a booking to cancel.");
            }
        });

        buttonPanel.add(cancelButton);

        if (bookingData.isEmpty()) {
            mainPanel.add(new JLabel("No bookings found", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        bookingsFrame.add(headerPanel, BorderLayout.NORTH);
        bookingsFrame.add(mainPanel, BorderLayout.CENTER);
        bookingsFrame.setVisible(true);

    }

    private static void cancelBooking(String bookingId) {
        List<String> updatedBookings = new ArrayList<>();

        try (Scanner scanner = new Scanner(new java.io.File("pakistan_railway_bookings.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.contains(bookingId)) {
                    updatedBookings.add(line); 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter("pakistan_railway_bookings.txt", false)) {
            for (String updatedLine : updatedBookings) {
                writer.write(updatedLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createThankYouPage(Booking booking) {
        JFrame thankYouFrame = new JFrame("Thank You - Pakistan Railways");
        thankYouFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thankYouFrame.setSize(600, 600);
        thankYouFrame.setLayout(new BorderLayout());
        thankYouFrame.getContentPane().setBackground(new Color(240, 240, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 102, 0));
        headerPanel.setPreferredSize(new Dimension(600, 80));
        JLabel titleLabel = new JLabel("BOOKING CONFIRMED", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main Content Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        ImageIcon successIcon = new ImageIcon("success_icon.png"); // Add your success icon
        JLabel iconLabel = new JLabel(successIcon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel thanksLabel = new JLabel("Thank you for choosing Pakistan Railways!");
        thanksLabel.setFont(new Font("Arial", Font.BOLD, 16));
        thanksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 10, 5));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        detailsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        detailsPanel.add(new JLabel("Booking ID: " + booking.bookingId));
        detailsPanel.add(new JLabel("Train: " + booking.train.name));
        detailsPanel.add(new JLabel("Route: " + booking.from + " to " + booking.to));
        detailsPanel.add(new JLabel("Date: " + booking.date));
        detailsPanel.add(new JLabel("Passenger: " + booking.passengerName));
        detailsPanel.add(new JLabel("Gender: " + booking.gender));
        detailsPanel.add(new JLabel("Class: " + booking.seatType));
        detailsPanel.add(new JLabel("Price: Rs. " + booking.price));
        detailsPanel.add(new JLabel("Payment: " + booking.paymentInfo));

        JButton printButton = new JButton("Print Ticket");
        printButton.setBackground(new Color(0, 102, 0));
        printButton.setForeground(Color.WHITE);
        printButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        printButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(thankYouFrame, "Ticket sent to printer!");
        });

        JButton menuButton = new JButton("Back to Main Menu");
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.addActionListener(e -> {
            thankYouFrame.dispose();
            createMainMenu();
        });

        mainPanel.add(iconLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(thanksLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(detailsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(printButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(menuButton);

        thankYouFrame.add(headerPanel, BorderLayout.NORTH);
        thankYouFrame.add(mainPanel, BorderLayout.CENTER);
        thankYouFrame.setVisible(true);
    }
}