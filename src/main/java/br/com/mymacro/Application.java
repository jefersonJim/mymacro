package br.com.mymacro;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 * 
 * @author Jeferson
 *
 */

public class Application extends JFrame{
    
	private static final long serialVersionUID = -3912683613032976006L;
	
	
	TrayIcon trayIcon;
    SystemTray tray;
    JPanel painelFundo;
    private JPanel painelBotoes;
    JTable tabela;
    JScrollPane barraRolagem;
    private JButton btInserir;
    private JButton btExcluir;
    private JButton btEditar;
    @SuppressWarnings("serial")
	private DefaultTableModel modelo = new DefaultTableModel() {
    	@Override
    	public boolean isCellEditable(int row, int column) {
    		return false;
    	};
    };
    private SQLiteJDBCDriverConnection conn = new SQLiteJDBCDriverConnection();
    
    Application() throws IOException{
        super("My Macro");
        setLocationRelativeTo(null);
        try{ 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            System.out.println("Unable to set LookAndFeel");
        }
        
        //Registrando Listeners
        Tools.registraListener();

        if(SystemTray.isSupported()){
            tray=SystemTray.getSystemTray();
            Image image = ImageIO.read(getClass().getResourceAsStream("/images/icon.png"));
            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Abrir");
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    setExtendedState(JFrame.NORMAL);
                }
            });
            popup.add(defaultItem);
            defaultItem = new MenuItem("Sair");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            
            trayIcon = new TrayIcon(image, "My Macro", popup);
            trayIcon.setImageAutoSize(true);
        }else{
            System.out.println("Sistema tray não suportado");
        }

        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if(e.getNewState() == ICONIFIED){
                    try {
                        tray.add(trayIcon);
                        setVisible(false);
                    } catch (AWTException ex) {
                        System.out.println("unable to add to tray");
                    }
                }
		        if(e.getNewState()==7){
		            try{
			            tray.add(trayIcon);
			            setVisible(false);
		            }catch(AWTException ex){
		                System.out.println("unable to add to system tray");
		            }
		        }
		        if(e.getNewState()==MAXIMIZED_BOTH){
                    tray.remove(trayIcon);
                    setVisible(true);
                }
                if(e.getNewState()==NORMAL){
                    tray.remove(trayIcon);
                    setVisible(true);
                }
            }
        });
        
        addWindowListener(new WindowAdapter() {
    	    @Override
    	    public void windowClosed(WindowEvent e) {
    	    	try {
    				GlobalScreen.unregisterNativeHook();
    			}
    			catch (NativeHookException ex) {
    				System.out.println(ex);
    			}
    	    }
    	});
        
        this.criaJTable();
        this.criaJanela();
        setIconImage(ImageIO.read(getClass().getResourceAsStream("/images/icon.png")));
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    
    public void criaJanela() {
        btInserir = new JButton("Adicionar");
        btEditar = new JButton("Editar");
        btExcluir = new JButton("Excluir");
        painelBotoes = new JPanel();
        barraRolagem = new JScrollPane(tabela);
        painelFundo = new JPanel();
        painelFundo.setLayout(new BorderLayout());
        painelFundo.add(BorderLayout.CENTER, barraRolagem);
        painelBotoes.add(btInserir);
        painelBotoes.add(btEditar);
        painelBotoes.add(btExcluir);
        painelFundo.add(BorderLayout.SOUTH, painelBotoes);
        
        btInserir.addActionListener((event) -> {
			this.editor(null);
		});
        
        btEditar.addActionListener((event)->{
        	int linhaSelecionada = -1;
            linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada >= 0) {
                int id = Integer.parseInt((String) tabela.getValueAt(linhaSelecionada, 0));
                this.editor(id);
                
            } else {
                JOptionPane.showMessageDialog(this, "É necesário selecionar uma linha.");
            }
        });
        
        btExcluir.addActionListener(event->{
        	int linhaSelecionada = -1;
            linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada >= 0) {
                int id = Integer.parseInt((String) tabela.getValueAt(linhaSelecionada, 0));
                try {
					conn.delete(id);
					modelo.removeRow(linhaSelecionada);
				} catch (SQLException e) {
					System.out.println(e);
				}
            } else {
                JOptionPane.showMessageDialog(this, "É necesário selecionar uma linha.");
            }
        });
 
        getContentPane().add(painelFundo);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setVisible(true);
    }
 
    private void criaJTable() {
        tabela = new JTable(modelo);
        modelo.addColumn("#");
        modelo.addColumn("Macro");
        modelo.addColumn("Sintaxe");
        tabela.getColumnModel().getColumn(0).setPreferredWidth(10);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        this.addDados();
    }
    
    private void addDados() {
    	modelo.setRowCount(0);
    	try {
			ResultSet set = conn.select("SELECT * FROM MACRO ORDER BY ORDEM");
			while (set.next()) {
				modelo.addRow(new Object[] {set.getString("ORDEM"), set.getString("MACRO"), set.getString("SYNTAX") });
			}
			set.close();
		} catch (SQLException e) {
			System.out.println(e);
		}
    }
    
    private void editor(Integer id) {
    	EditorScreen diag = new EditorScreen(this, "Editor de Macro", true);
    	diag.setId(id);
    	diag.mount();
    	diag.setDefaultCloseOperation(JDialog .DISPOSE_ON_CLOSE);
    	diag.setSize(600, 300);
    	diag.setLocationRelativeTo(this);
    	Application app = this;
    	diag.addWindowListener(new WindowAdapter() {
    	    @Override
    	    public void windowClosed(WindowEvent e) {
    	    	app.addDados();
    	    }
    	});
    	diag.setVisible(true);
    }
    
    public static void main(String[] args) throws IOException{
        new Application();
    }
}