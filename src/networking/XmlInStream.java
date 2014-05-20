package networking;

import generated.MazeCom;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class XmlInStream extends UTFInputStream {

	private Unmarshaller unmarshaller;

	public XmlInStream(InputStream in) {
		super(in);
		try {
			JAXBContext jc = JAXBContext.newInstance(MazeCom.class);
			this.unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			System.err.println("[ERROR]: Fehler beim initialisieren der JAXB-Komponenten");
		}
	}

	/**
	 * Liest eine Nachricht und gibt die entsprechende Instanz zurueck
	 * 
	 * @return
	 */
	public MazeCom readMazeCom() throws EOFException, SocketException {
		byte[] bytes = null;
		MazeCom result = null;
		try {
			String xml = this.readUTF8();
			System.out.println("Empfangen");
			System.out.println(xml);
			bytes = xml.getBytes();
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

			result = (MazeCom) this.unmarshaller.unmarshal(bais);
		} catch (JAXBException e) {
			e.printStackTrace();
			System.err.println("[ERROR]: Fehler beim unmarshallen der Nachricht");
		} catch (SocketException ex) {
			//weiterleiten der Exception => damit Spieler korrekt entfernt wird
			throw ex;
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("[ERROR]: Fehler beim lesen der Nachricht");
		} catch (NullPointerException e) {
			System.err.println("[ERROR]: Nullpointer beim lesen der Nachricht aufgrund weiterer Fehler");
		}
		return result;
	}

}