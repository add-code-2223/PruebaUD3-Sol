package modelo.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import exceptions.SaldoInsuficienteException;
import modelo.ud3.AccMovement;
import modelo.ud3.Account;
import modelo.ud3.Departamento;
import modelo.ud3.Empleado;
import exceptions.InstanceNotFoundException;
import util.SessionFactoryUtil;

public class AccountServicio implements IAccountServicio {

	@Override
	public Account findAccountById(int accId) throws InstanceNotFoundException {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Account account = session.get(Account.class, accId);
		if (account == null) {
			throw new InstanceNotFoundException(Account.class.getName());
		}

		session.close();
		return account;
	}

	@Override
	public AccMovement transferir(int accOrigen, int accDestino, double cantidad)
			throws SaldoInsuficienteException, InstanceNotFoundException, UnsupportedOperationException {

		Transaction tx = null;
		Session session = null;
		AccMovement movement = null;

		try {

			if (cantidad <= 0) {
				throw new UnsupportedOperationException();
			}
			SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
			session = sessionFactory.openSession();

			Account accountOrigen = session.get(Account.class, accOrigen);
			if (accountOrigen == null) {
				throw new InstanceNotFoundException(Account.class.getName() + " origen id:" + accOrigen);
			}
			BigDecimal cantidadBD = new BigDecimal(cantidad);
			if (accountOrigen.getAmount().compareTo(cantidadBD) < 0) {
				throw new SaldoInsuficienteException("No hay saldo suficiente", accountOrigen.getAmount(), cantidadBD);
			}
			Account accountDestino = session.get(Account.class, accDestino);
			if (accountDestino == null) {
				throw new InstanceNotFoundException(Account.class.getName() + " destino id:" + accDestino);
			}

			tx = session.beginTransaction();

			accountOrigen.setAmount(accountOrigen.getAmount().subtract(cantidadBD));
			accountDestino.setAmount(accountDestino.getAmount().add(cantidadBD));

			movement = new AccMovement();
			movement.setAmount(cantidadBD);
			movement.setDatetime(LocalDateTime.now());

			// Relación bidireccional
			movement.setAccountOrigen(accountOrigen);
			movement.setAccountDestino(accountDestino);
			// Son prescindibles y no recomendables en navegación bidireccional porque una
			// Account puede tener numerosos movimientos
//					accountOrigen.getAccMovementsOrigen().add(movement);
//					accountDestino.getAccMovementsDest().add(movement);

//					session.saveOrUpdate(accountOrigen);
//					session.saveOrUpdate(accountDestino);
			session.save(movement);

			tx.commit();

		} catch (Exception ex) {
			System.out.println("Ha ocurrido una exception: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return movement;

	}

	@Override
	public AccMovement autoTransferir(int accNo, double cantidad) throws InstanceNotFoundException {

		Transaction tx = null;
		Session session = null;
		AccMovement movement = null;

		try {
			SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
			session = sessionFactory.openSession();

			Account account = session.get(Account.class, accNo);
			if (account == null) {
				throw new InstanceNotFoundException(Account.class.getName() + " origen id:" + accNo);
			}
			BigDecimal cantidadBD = new BigDecimal(cantidad);

			tx = session.beginTransaction();

			account.setAmount(account.getAmount().add(cantidadBD));

			movement = new AccMovement();
			movement.setAmount(cantidadBD);
			movement.setDatetime(LocalDateTime.now());

			// Relación bidireccional
			movement.setAccountOrigen(account);
			movement.setAccountDestino(account);

			session.save(movement);

			tx.commit();

		} catch (Exception ex) {
			System.out.println("Ha ocurrido una exception: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return movement;

	}

	public List<Account> getAccountsByEmpno(int empno) {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();

		List<Account> accounts = session
				.createQuery("select a from Account a " + "join a.employees e where e.empno = :empno")
				.setParameter("empno", empno).list();

		session.close();
		return accounts;

	}

	public Account saveOrUpdate(Account d) {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();

			session.saveOrUpdate(d);
			tx.commit();
		} catch (Exception ex) {
			System.out.println("Ha ocurrido una excepción en saveOrUpdate Account: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		} finally {
			session.close();
		}
		return d;
	}

	public Account addAccountToEmployee(int empno, Account acc) {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean exito = false;

		try {
			tx = session.beginTransaction();
			Empleado empleado = session.get(Empleado.class, empno);
			if (empleado != null) {
//				acc.getEmployees().add(empleado);
//				empleado.getAccounts().add(acc);
				empleado.addAccount(acc);
			}
			session.save(acc);
			session.saveOrUpdate(empleado);
			tx.commit();
			
		} catch (Exception ex) {
			System.out.println("Ha ocurrido una excepción en addAccountToEmployee : " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		} finally {
			session.close();
		}
		return acc;
	}
	
	
	public List<Empleado> getTitularesByAccountId(int accId)throws InstanceNotFoundException{
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean exito = false;
		List<Empleado> empleados =new ArrayList<>();
		try {
		
			Account account = session.get(Account.class, accId);
			if (account != null) {
				
				 empleados = account.getEmployees().stream().toList();
//				for (Empleado empleado : empleados) {
//					empleado.getEmpno()
//				}
			} else {
				throw new InstanceNotFoundException(Departamento.class.getName() + " id: " + accId);
			}
			
		} catch (Exception ex) {
			System.out.println("Ha ocurrido una excepción en create Dept: " + ex.getMessage());
			

			throw ex;
		} finally {
			session.close();
		}
		return empleados;
	}

	public boolean delete(int accId) throws InstanceNotFoundException {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean exito = false;

		try {
			tx = session.beginTransaction();
			Account account = session.get(Account.class, accId);
			if (account != null) {
				//Si no se usa cascade en Account.java para getAccMovementsX, se eliminan explícitamente
				for (AccMovement accs : account.getAccMovementsForAccountDestId()) {
					session.remove(accs);
				}
				
				for (AccMovement accs : account.getAccMovementsForAccountOriginId()) {
					session.remove(accs);
				}
				//Del conjunto de los titulares, eliminamos la cuenta.
				for(Empleado emp: account.getEmployees()){
					emp.getAccounts().remove(account);
				}
				session.remove(account);
			} else {
				throw new InstanceNotFoundException(Departamento.class.getName() + " id: " + accId);
			}
			tx.commit();
			exito = true;
		} catch (Exception ex) {
			System.out.println("Ha ocurrido una excepción en create Dept: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}

			throw ex;
		} finally {
			session.close();
		}
		return exito;
	}
}
