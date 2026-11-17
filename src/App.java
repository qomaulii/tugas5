import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class App extends JFrame implements ActionListener, ListSelectionListener {
    static final Color TERRACOTTA = new Color(0x783E30);
    static final Color SAND = new Color(0xB39070);
    static final Color BROWN = new Color(0x3E211E);
    static final Color OLIVE = new Color(0x6E6755);

    ArrayList<Object[]> daftar = new ArrayList<>();
    DefaultTableModel model = new DefaultTableModel(
            new String[] {"Judul", "Episode", "Progres", "Status", "Rating"}, 0);
    JTable tabel = new JTable(model);
    JProgressBar bar = new JProgressBar();

    public App() {
        super("K-Drama Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 460);
        setLocationRelativeTo(null);
        
        tabel.setDefaultEditor(Object.class, null);
        tabel.setSelectionBackground(SAND);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getTableHeader().setBackground(TERRACOTTA);
        tabel.getTableHeader().setForeground(Color.WHITE);
        tabel.getSelectionModel().addListSelectionListener(this);
        bar.setStringPainted(true);
        bar.setForeground(TERRACOTTA);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        for (String nama : new String[] {"Tambah", "Edit", "Hapus", "Update Episode"}) {
            JButton tombol = new JButton(nama);
            tombol.setBackground(OLIVE);
            tombol.setForeground(Color.WHITE);
            tombol.addActionListener(this);
            panelTombol.add(tombol);
        }

        JPanel bawah = new JPanel(new GridLayout(2, 1, 0, 6));
        bawah.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        bawah.add(bar);
        bawah.add(panelTombol);
        
        setLayout(new BorderLayout(0, 8));
        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(bawah, BorderLayout.SOUTH);
        daftar.add(new Object[] {"Goblin", 16, 10, 0, false});
        isiTabel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String aksi = e.getActionCommand();
        int baris = tabel.getSelectedRow();
        if (aksi.equals("Tambah")) {
            bukaForm(-1);
        } else if (baris == -1) {
            pesan("Pilih drama pada tabel terlebih dahulu.");
        } else if (aksi.equals("Edit")) {
            bukaForm(baris);
        } else if (aksi.equals("Hapus")) {
            hapusDrama(baris);
        } else {
            updateEpisode(baris);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int baris = tabel.getSelectedRow();
        if (e.getValueIsAdjusting() || baris >= daftar.size()) {
            return;
        }
        if (baris == -1) {
            bar.setMaximum(100);
            bar.setValue(0);
            bar.setString("Pilih drama pada tabel untuk melihat progres");
            return;
        }
        Object[] d = daftar.get(baris);
        bar.setMaximum((int) d[1]);
        bar.setValue((int) d[2]);
        bar.setString(d[0] + ": " + d[2] + " dari " + d[1] + " episode (" + persen(d) + ")");
    }

    void bukaForm(int baris) {
        Object[] lama = baris == -1 ? new Object[] {"", "", 0, 0, false} : daftar.get(baris);
        JTextField txtJudul = new JTextField(String.valueOf(lama[0]), 18);
        JTextField txtTotal = new JTextField(String.valueOf(lama[1]));
        JTextField txtEpisode = new JTextField(String.valueOf(lama[2]));

        JComboBox<String> cmbRating = new JComboBox<>(new String[] {
            "Belum dirating", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        JComboBox<String> cmbStatus = new JComboBox<>(new String[] {"Otomatis", "Dropped"});
        cmbRating.setSelectedIndex((int) lama[3]);
        cmbStatus.setSelectedIndex((boolean) lama[4] ? 1 : 0);
        
        String[] label = {"Judul", "Total episode", "Episode terakhir", "Rating (1-10)", "Status"};
        Component[] isian = {txtJudul, txtTotal, txtEpisode, cmbRating, cmbStatus};
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        for (int i = 0; i < label.length; i++) {
            form.add(new JLabel(label[i]));
            form.add(isian[i]);
        }
        
        while (JOptionPane.showConfirmDialog(this, form, baris == -1 ? "Tambah Drama" : "Edit Drama",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                String judul = txtJudul.getText().trim();
                int total = Integer.parseInt(txtTotal.getText().trim());
                int episode = Integer.parseInt(txtEpisode.getText().trim());
    
                if (judul.isEmpty() || total < 1 || episode < 0 || episode > total) {
                    pesan("Judul wajib diisi, total episode minimal 1,\n"
                            + "dan episode terakhir harus antara 0 dan total episode.");
                    continue;
                }

                boolean kembar = false;
                for (int i = 0; i < daftar.size(); i++) {
                    if (i != baris && daftar.get(i)[0].toString().equalsIgnoreCase(judul)) {
                        kembar = true;
                        break;
                    }
                }

                if (kembar) {
                    pesan("Judul drama sudah terdaftar!");
                    continue;
                }

                Object[] drama = {judul, total, episode, cmbRating.getSelectedIndex(),
                    cmbStatus.getSelectedIndex() == 1};
                if (baris == -1) {
                    daftar.add(drama);
                } else {
                    daftar.set(baris, drama);
                }
                isiTabel();
                return;
            } catch (NumberFormatException ex) {
                pesan("Total episode dan episode terakhir harus berupa angka bulat.");
            }
        }
    }

    void hapusDrama(int baris) {
        int pilihan = JOptionPane.showConfirmDialog(this,
                "Hapus drama \"" + daftar.get(baris)[0] + "\"?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilihan == JOptionPane.YES_OPTION) {
            daftar.remove(baris);
            isiTabel();
        }
    }

    void updateEpisode(int baris) {
        Object[] d = daftar.get(baris);
        String input = JOptionPane.showInputDialog(this,
                "Episode terakhir yang ditonton (0 sampai " + d[1] + "):", String.valueOf(d[2]));
        if (input == null) {
            return;
        }
        try {
            int episode = Integer.parseInt(input.trim());
            if (episode < 0 || episode > (int) d[1]) {
                pesan("Episode harus antara 0 dan " + d[1] + ".");
                return;
            }
            d[2] = episode;
            isiTabel();
        } catch (NumberFormatException ex) {
            pesan("Episode harus berupa angka bulat.");
        }
    }

    String status(Object[] d) {
        if ((int) d[2] >= (int) d[1]) {
            return "Completed";
        }
        if ((boolean) d[4]) {
            return "Dropped";
        }
        return (int) d[2] > 0 ? "Watching" : "Plan to Watch";
    }

    String persen(Object[] d) {
        return String.format(Locale.forLanguageTag("id-ID"), "%.1f%%", (int) d[2] * 100.0 / (int) d[1]);
    }

    void isiTabel() {
        int terpilih = tabel.getSelectedRow();
        model.setRowCount(0);
        for (Object[] d : daftar) {
            model.addRow(new Object[] {d[0], d[2] + "/" + d[1], persen(d), status(d),
                (int) d[3] == 0 ? "-" : d[3] + "/10"});
        }
        if (terpilih >= 0 && terpilih < daftar.size()) {
            tabel.setRowSelectionInterval(terpilih, terpilih);
        }
    }

    void pesan(String teks) {
        JOptionPane.showMessageDialog(this, teks, "Validasi", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        UIManager.put("Panel.background", new Color(0xF4EDE3));
        UIManager.put("ProgressBar.selectionBackground", BROWN);
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
