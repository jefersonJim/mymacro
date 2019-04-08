package br.com.mymacro;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rtextarea.RTextScrollPane;

public class EditorScreen extends JDialog {
	private static final long serialVersionUID = -6988778602565190481L;

	private Integer id;
	private JButton btnSalvar;
	private JPanel painelFundo;
	private RSyntaxTextArea textArea;
	private RTextScrollPane sp;
	private JComboBox<String> linguagem;
	private SQLiteJDBCDriverConnection conn = new SQLiteJDBCDriverConnection();

	public EditorScreen(Application application, String title, boolean b) {
		super(application, title, b);
		setLayout(new BorderLayout());
		
		
		painelFundo = new JPanel();
		painelFundo.setLayout(new BorderLayout());

		add(painelFundo);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void mount() {
		RSyntaxTextArea.setTemplatesEnabled(true);
		CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
		CodeTemplate ct = new ArtemisCodeTemplate("cmt", "/*", "<escreva aqui>", "*/");
		ctm.addTemplate(ct);
		textArea = new RSyntaxTextArea(20, 60);
		textArea.setCodeFoldingEnabled(true);
		
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);

		ac.install(textArea);

		sp = new RTextScrollPane(textArea);
		add(sp, BorderLayout.CENTER);

		List<String> ling = Arrays.asList(Constantes.SYNTAX);

		linguagem = new JComboBox<>();
		ling.forEach(item -> linguagem.addItem(item));

		linguagem.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				String item = (String) event.getItem();
				this.setSyntax(item);
			}
		});

		add(linguagem, BorderLayout.NORTH);

		btnSalvar = new JButton("Salvar");
		btnSalvar.addActionListener(action->{	
			try {
				String macro = textArea.getText();
				String syntax = (String)linguagem.getSelectedItem();
				if(this.id == null ) {
					ResultSet set = conn.select("SELECT (count(*) + 1) as ordem FROM MACRO");
					set.next();
					
					int ordem = set.getInt("ordem");
					set.close();
					
					conn.insert(macro, ordem, syntax);
				}else {
					conn.update(this.id, macro, syntax);
				}
				dispose();
				
			} catch (SQLException e) {
				System.out.println(e);
			}
		});
		add(btnSalvar, BorderLayout.SOUTH);
		
		this.setEditData(this.id);
	}

	public void setSyntax(String syntax) {
		if (syntax == null) {
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
			linguagem.setSelectedItem("text/plain");
		} else {
			textArea.setSyntaxEditingStyle(syntax);
			linguagem.setSelectedItem(syntax);
		}
	}
	
	public void setEditData(Integer id) {
		try {
			if(id != null) {
				ResultSet set = conn.select("SELECT * FROM MACRO WHERE ORDEM = "+id);
				if(set.next()) {
					this.setSyntax(set.getString("SYNTAX"));
					textArea.setText(set.getString("MACRO"));
				}
				set.close();
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	private CompletionProvider createCompletionProvider() {

		DefaultCompletionProvider provider = new DefaultCompletionProvider();

		provider.addCompletion(new BasicCompletion(provider, "(:param)"));
		return provider;

	}

}
