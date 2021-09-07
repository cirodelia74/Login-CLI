
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginCLI {

//	----------------------------------------------------------
//	recupero i parametri di connessione al DB 
//	da un file esterno al progetto
	private static Properties getConnectionData() {

        Properties props = new Properties();

        String fileName = "resources/db.properties";

        try (FileInputStream in = new FileInputStream(fileName)) {
            props.load(in);
        } catch (IOException ex) {
            Logger lgr = Logger.getLogger("Lettura dati connessione DB");
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return props;
    }
//	----------------------------------------------------------	
	
	public static Properties props = getConnectionData();

	public static String DB_URL = props.getProperty("db.url");
	public static String DB_NAME = props.getProperty("db.name");
	public static String DB_USER = props.getProperty("db.user");
	public static String DB_PASS = props.getProperty("db.passwd");
	
	public static void main(String[] args) {
		
		LoginTable loginTable = null;
		String username, password, password1;
		Integer sceltaMenu = 0;
		int contLoginFail = 0;
		
		Scanner input = new Scanner(System.in);
		
		try {

			loginTable = new LoginTable(DB_URL, DB_NAME, DB_USER,DB_PASS);
			
			// gestione menu applicazione
			do {
				menuApplicazione();
				try {
					sceltaMenu = Integer.parseInt(input.nextLine());
					
					switch (sceltaMenu) {
					
					case 1:
						System.out.println("\n    Effettua login");
						System.out.println("==========================");
						System.out.print("\nInserisci USERNAME: ");
						username = input.nextLine();
						System.out.print("Inserisci PASSWORD: ");
						password = input.nextLine();
				
						if (loginTable.verificaUser(username)) {
							if (loginTable.verificaAccount(username)) {
								contLoginFail += loginTable.login(username, password);				
								if (contLoginFail == 3) {
									loginTable.bloccaAccount(username);
								}
								else {							
									System.out.println("\nPremere INVIO per tornare al menu'...");
								    input.nextLine();							
								}
							}
							else {
								loginTable.accountBloccato();
								sceltaMenu = 0;
							}
						}
						else {
							usernameAssente();
							contLoginFail++;
						}
						break;
					case 2:
						System.out.println("\nRegistrazione nuovo utente");
						System.out.println("==========================");
						System.out.print("Inserisci USERNAME: ");
						username = input.nextLine();
						System.out.print("Inserisci PASSWORD: ");
						password = input.nextLine();
						System.out.print("Reinserisci PASSWORD: ");
						password1 = input.nextLine();
						if (password.equals(password1)) {
							loginTable.registrazione(username, password);
						}
						else {
							passwordDiverse();
						}
						System.out.println("\nPremere INVIO per tornare al menu'...");
					    input.nextLine();
						break;
					case 0:					
						break;
					default:
						selezioneErrata();		
					}
				}
				catch (NumberFormatException exc) {
					selezioneErrata();
				}
						
			} while (sceltaMenu != 0 && contLoginFail < 3);		
			System.out.println("\nAPPLICAZIONE TERMINATA");
			input.close();
			
		} catch (SQLException e) {
			System.out.println("ERRORE DATABASE: " + e.toString());	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (loginTable != null)
				try {
					loginTable.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

	}
	
	public static void menuApplicazione() {
		
		System.out.println("\n==============================");
		System.out.println("     GESTIONE LOGIN UTENTI");
		System.out.println("==============================");
		System.out.println(" 1 - Login utente");
		System.out.println(" 2 - Registrazione nuovo utente");
		System.out.println(" 0 - Esci");
		System.out.print("\n Operazione da eseguire: ");
	}
	
	public static void selezioneErrata() {
		System.out.println("\n======================");
		System.out.println(" Selezione non valida");
		System.out.println("======================");
	}
	
	public static void passwordDiverse() {
		System.out.println("\n==========================================");
		System.out.println(" Le password inserite sono diverse. Riprova");
		System.out.println("==========================================");
	}
	
	public static void usernameAssente() {
		System.out.println("\n==============================");
		System.out.println(" L'username inserito non esiste");
		System.out.println("================================");
	}


}
