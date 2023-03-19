package modelo.ud3;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name="TERM_DEPOSIT_ACCOUNT ")
@PrimaryKeyJoinColumn(name="account_id")
public class TermDepositAccount extends Account {

	/**
	 * 
	 */
	private static final long serialVersionUID = -47483550163632943L;

	private float interes;
	
	private int plazo_meses;

	public float getInteres() {
		return interes;
	}

	public void setInteres(float interes) {
		this.interes = interes;
	}

	public int getPlazo_meses() {
		return plazo_meses;
	}

	public void setPlazo_meses(int plazo_meses) {
		this.plazo_meses = plazo_meses;
	}
	
	
}
