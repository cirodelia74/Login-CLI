
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LoginTable {
	
	private PreparedStatement stmtLeggiUser = null;
	private PreparedStatement stmtInserisciUtente = null;
	private PreparedStatement stmtBlocca = null;
	private String queryLeggi = "SELECT * FROM login WHERE username = ?";
	private String queryInserisci = "INSERT INTO login (username, password) VALUES (?, ?)";
	private String queryBlocca = "UPDATE login SET attivo = 0 WHERE username = ?";
	private Connection conn = null;
	private ArrayList<String> passSimple = new ArrayList<>();
	
	public LoginTable(String serverAddr, String dbName, String username, String password) throws SQLException, FileNotFoundException, IOException {
		String url = "jdbc:mysql://" + serverAddr + "/" + dbName;

		// Avvio la connessione con il database
		conn = DriverManager.getConnection(url, username, password);
		
		// Preparo gli statement
		stmtLeggiUser = conn.prepareStatement(queryLeggi);
		stmtInserisciUtente = conn.prepareStatement(queryInserisci);
		stmtBlocca = conn.prepareStatement(queryBlocca);
		
		// salvo le password semplici da evitare
		try (BufferedReader br = new BufferedReader(new FileReader("resources/SimplePass.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	passSimple.add(line);
		    }
		}
		
	}
	
	public int login(String username, String password) throws SQLException {
		
		ResultSet utenteRegistrato = null;
		
		// imposto lo statement per la ricerca dell'username
		stmtLeggiUser.setString(1, username);
		utenteRegistrato = stmtLeggiUser.executeQuery();
		
		Boolean controllaAccount = utenteRegistrato.next() && utenteRegistrato.getString("password").equals(password);
		if(controllaAccount) {
				loginOk();
				utenteRegistrato.close();
				return 0;
		}
		else {
			loginFallito();
			utenteRegistrato.close();
			return 1;
		}
	
	}

	public void registrazione(String username, String password) throws SQLException {
		
		ResultSet utentePresente = null;
		
		// verifico che l'sername scelto non sia presente
		stmtLeggiUser.setString(1, username);
		utentePresente = stmtLeggiUser.executeQuery();
		
		if(!utentePresente.next()) {			
			// verifico se la password scelta è tra quelle semplici
			if (!passSimple.contains((Object)password)) {
				stmtInserisciUtente.setString(1, username);
				stmtInserisciUtente.setString(2, password);
				
					if (stmtInserisciUtente.executeUpdate() == 1) {
						registrazioneOK();
					}
					else {
						registrazioneFallita();
				}
			}
			else {
				passwordFacile();
			}
		}
		else {
			usernameIndisponibile();
		}		
	}
	
	public void bloccaAccount(String user) throws SQLException {
		
		stmtBlocca.setString(1, user);
		if (stmtBlocca.executeUpdate() == 1) {
			accountBloccato();
		}
	}
	
	public boolean verificaUser(String username) throws SQLException {
		
		ResultSet user = null;
		stmtLeggiUser.setString(1, username);
		
		user = stmtLeggiUser.executeQuery();
		if (user.next())
			return true;
		else
			return false;
	}
	
	
	public boolean verificaAccount(String username) throws SQLException {
		
		ResultSet user = null;
		stmtLeggiUser.setString(1, username);
		
		user = stmtLeggiUser.executeQuery();
		user.next();
		return user.getBoolean("attivo");
		
//		if(user.next()) {
//			return user.getBoolean("attivo");
//		}
//		else {
//			loginFallito();
//			return false;
//		}
	}
	
	private void loginOk() {
		System.out.print("\n=============================");
		System.out.print("\nLOGIN EFFETTUATO CON SUCCESSO");
		System.out.print("\n=============================");
	}
	
	private void loginFallito() {
		System.out.print("\n================================");
		System.out.print("\n  USERNAME E/O PASSWORD ERRATA");
		System.out.print("\n================================");
	}
	
	private void registrazioneOK() {
		System.out.print("\n=============================");
		System.out.print("\n   NUOVO UTENTE REGISTRATO");
		System.out.print("\n=============================");
	}
	
	private void registrazioneFallita() {
		System.out.print("\n===========================");
		System.out.print("\n  ERRORE DI REGISTRAZIONE");
		System.out.print("\n===========================");
	}
	
	private void passwordFacile() {
		System.out.print("\n=========================================");
		System.out.print("\n PASSWORD NON SICURA: INSERIRNE UN'ALTRA");
		System.out.print("\n=========================================");
	}
	
	private void usernameIndisponibile() {
		System.out.print("\n============================");
		System.out.print("\n  USERNAME NON DISPONIBILE");
		System.out.print("\n============================");
	}
	
	public void accountBloccato() {
		System.out.print("\n============================");
		System.out.print("\n     ACCOUNT BLOCCATO");
		System.out.print("\n  CONTATTA L'AMMINISTRATORE");
		System.out.print("\n============================");
	}

	public void close() throws SQLException {
				
		if (conn != null)
			conn.close();
		if (stmtLeggiUser != null) 			
			stmtLeggiUser.close();
		if (stmtInserisciUtente != null)
			stmtInserisciUtente.close();
		if (stmtBlocca != null)
			stmtBlocca.close();
	}
}