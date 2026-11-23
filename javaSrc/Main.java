import servidor.Servidor;

class Main{

	public static void main(String[] args) {

		try{
			Servidor.init();
		} catch(Exception e) {
			e.printStackTrace();
		}


		TesteAP.initTeste();
	}

}